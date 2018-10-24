package com.github.shaddysignal.revolut

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestDuration
import com.github.shaddysignal.revolut.model.{Account, Transfer}
import com.github.shaddysignal.revolut.repo.{IdGenerator, RepoModule, SimpleIdGenerator}
import com.github.shaddysignal.revolut.service.ServiceModule
import de.heikoseeberger.akkahttpargonaut.ArgonautSupport

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

class RestResourceSpec extends BaseSpec with ScalatestRouteTest {

  implicit val timeout = RouteTestTimeout(10.seconds dilated)

  trait Context extends WebMainModule with ServiceModule with RepoModule with ArgonautSupport {
    import argonaut.Argonaut._
    import argonaut._
    // Importing codecs
    import model.Account._
    import model.Transfer._

    implicit def seqCodec[T](implicit e: CodecJson[T]): CodecJson[Seq[T]] =
      CodecJson(
        seq => EncodeJson.VectorEncodeJson[T].apply(seq.toVector),
        cursor => DecodeJson.VectorDecodeJson[T].apply(cursor).map(_.toSeq)
      )

    val firstId = 1
    val secondId = 2
    val unusedId = 10
    val amount = 10.0

    val invalidAccount = Account(None, -amount)
    val newAccount = Account(None, amount)
    val firstAccount = Account(Some(firstId), amount)
    val secondAccount = Account(Some(secondId), amount)
    val invalidAccountJson = invalidAccount.asJson
    val newAccountJson = newAccount.asJson
    val firstAccountJson = firstAccount.asJson
    val secondAccountJson = secondAccount.asJson

    val invalidTransferSameAccount = Transfer(None, firstId, firstId, amount)
    val invalidTransferWithLargeAmount = Transfer(None, firstId, secondId, 2 * amount)
    val invalidTransferWithNegativeAmount = Transfer(None, firstId, secondId, -amount)
    val newTransfer = Transfer(None, firstId, secondId, amount)
    val firstTransfer = Transfer(Some(firstId), firstId, secondId, amount)
    val secondTransfer = Transfer(Some(secondId), firstId, secondId, amount)
    val newTransferJson = newTransfer.asJson
    val firstTransferJson = firstTransfer.asJson
    val secondTransferJson = secondTransfer.asJson
    val invalidTransferSameAccountJson = invalidTransferSameAccount.asJson
    val invalidTransferWithLargeAmountJson = invalidTransferWithLargeAmount.asJson
    val invalidTransferWithNegativeAmountJson = invalidTransferWithNegativeAmount.asJson


    def setupAccountsInDatabase: Future[Seq[Try[Account]]] = {
      Future.sequence(
        Seq(
          accountService.create(newAccount.amount),
          accountService.create(newAccount.amount)
        )
      )
    }

    def setupTransfersInDatabase: Future[Seq[Try[Transfer]]] = {
      Future.sequence(
        Seq(
          transferService.create(newTransfer.sourceAccountId, newTransfer.destinationAccountId, newTransfer.amount)
        )
      )
    }

    // Specifying generator for tests
    override def idGenerator: IdGenerator[Long] = new SimpleIdGenerator(0)

    override val executionContext: ExecutionContext = ExecutionContext.global
  }

  "RestResource" can {
    "manage accounts" should {
      "successfully create account" in new Context {
        Post("/api/accounts", newAccountJson) ~> restResource.routes ~> check {
          status shouldBe StatusCodes.OK
          responseAs[Account] shouldBe firstAccount
        }
      }

      "successfully create account ignoring incoming id" in new Context {
        Post("/api/accounts", secondAccountJson) ~> restResource.routes ~> check {
          status shouldBe StatusCodes.OK

          val response = responseAs[Account]
          response.id.contains(secondId) shouldBe false
          response shouldBe firstAccount
        }
      }

      "return 500 when creating with invalid account" in new Context {
        Post("/api/accounts", invalidAccountJson) ~> restResource.routes ~> check {
          status shouldBe StatusCodes.InternalServerError
        }
      }

      "get account by id" in new Context {
        Await.result(setupAccountsInDatabase, 5 seconds)

        Get(s"/api/accounts/$firstId") ~> restResource.routes ~> check {
          status shouldBe StatusCodes.OK
          responseAs[Account] shouldBe firstAccount
        }
      }

      "return 404 for account that does not exist" in new Context {
        Get(s"/api/accounts/$firstId") ~> Route.seal(restResource.routes) ~> check {
          status shouldBe StatusCodes.NotFound
        }
      }

      "get all accounts" in new Context {
        Await.result(setupAccountsInDatabase, 5 seconds)

        Get("/api/accounts") ~> restResource.routes ~> check {
          status shouldBe StatusCodes.OK
          responseAs[Seq[Account]] should contain theSameElementsAs Seq(firstAccount, secondAccount)
        }
      }
    }

    "manage transfers" should {
      "successfully create transfer" in new Context {
        Await.result(setupAccountsInDatabase, 5 seconds)

        println(Await.result(accountService.get(1), 5 seconds))

        Post("/api/transfers", newTransferJson) ~> restResource.routes ~> check {
          status shouldBe StatusCodes.OK
          responseAs[Transfer] shouldBe firstTransfer
        }
      }

      "successfully create transfer ignoring incoming id" in new Context {
        Await.result(setupAccountsInDatabase, 5 seconds)

        Post("/api/transfers", secondTransferJson) ~> restResource.routes ~> check {
          status shouldBe StatusCodes.OK

          val response = responseAs[Transfer]
          response.id.contains(secondId) shouldBe false
          response shouldBe firstTransfer
        }
      }

      "return 500 when creating transfer with same source and destination" in new Context {
        Await.result(setupAccountsInDatabase, 5 seconds)

        Post("/api/transfers", invalidTransferSameAccountJson) ~> restResource.routes ~> check {
          status shouldBe StatusCodes.InternalServerError
        }
      }

      "return 500 when creating transfer with not existing accounts" in new Context {
        Post("/api/transfers", newTransferJson) ~> restResource.routes ~> check {
          status shouldBe StatusCodes.InternalServerError
        }
      }

      "return 500 when creating transfer with too large amount" in new Context {
        Await.result(setupAccountsInDatabase, 5 seconds)

        Post("/api/transfers", invalidTransferWithLargeAmountJson) ~> restResource.routes ~> check {
          status shouldBe StatusCodes.InternalServerError
        }
      }

      "return 500 when creatung transfer with negative amount" in new Context {
        Await.result(setupAccountsInDatabase, 5 seconds)

        Post("/api/transfers", invalidTransferWithNegativeAmountJson) ~> Route.seal(restResource.routes) ~> check {
          status shouldBe StatusCodes.InternalServerError
        }
      }

      "get transfer by id" in new Context {
        Await.result(setupAccountsInDatabase, 5 seconds)
        Await.result(setupTransfersInDatabase, 5 seconds)

        Get(s"/api/transfers/$firstId") ~> restResource.routes ~> check {
          status shouldBe StatusCodes.OK
          responseAs[Transfer] shouldBe firstTransfer
        }
      }

      "return 404 for transfer that does not exist" in new Context {
        Get(s"/api/transfers/$firstId") ~> Route.seal(restResource.routes) ~> check {
          status shouldBe StatusCodes.NotFound
        }
      }

      "get all transfers" in new Context {
        Await.result(setupAccountsInDatabase, 5 seconds)
        Await.result(setupTransfersInDatabase, 5 seconds)

        Get("/api/transfers") ~> restResource.routes ~> check {
          status shouldBe StatusCodes.OK
          responseAs[Seq[Transfer]] should contain theSameElementsAs Seq(firstTransfer)
        }
      }
    }
  }

}
