package com.github.shaddysignal.revolut

import akka.http.scaladsl.server.{Directives, Route}
import com.github.shaddysignal.revolut.model.{Account, Transfer}
import com.github.shaddysignal.revolut.service.{AccountService, TransferService}
import de.heikoseeberger.akkahttpargonaut.ArgonautSupport
import org.slf4s.Logging

class RestResource(val accountService: AccountService, val transferService: TransferService)
  extends Directives with ArgonautSupport with Logging {

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

  def routes: Route = pathPrefix("api") {
    rejectEmptyResponse {
      pathPrefix("accounts") {
        get {
          path(LongNumber) { id =>
            complete(accountService.get(id))
          } ~ pathEndOrSingleSlash {
            complete(accountService.getAll)
          }
        } ~ post {
          entity(as[Account]) { account =>
            complete(accountService.create(account.amount))
          }
        }
      } ~
        pathPrefix("transfers") {
          get {
            path(LongNumber) { id =>
              complete(transferService.get(id))
            } ~ pathEndOrSingleSlash {
              complete(transferService.getAll)
            }
          } ~ post {
            entity(as[Transfer]) { transfer =>
              complete(transferService.create(transfer.sourceAccountId, transfer.destinationAccountId, transfer.amount))
            }
          }
        }
    }
  }

}
