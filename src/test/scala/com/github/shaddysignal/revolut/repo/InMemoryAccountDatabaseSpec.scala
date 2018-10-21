package com.github.shaddysignal.revolut.repo

import com.github.shaddysignal.revolut.{BaseSpec, model}

import scala.concurrent.{ExecutionContext, Future}

class InMemoryAccountDatabaseSpec extends BaseSpec {

  trait Context extends RepoModule {
    val accountId = 1
    val amount = 10.0
    val newAmount = 20.0

    val newAccount = model.Account(None, amount)
    val accountWithId = model.Account(Some(accountId), amount)
    val updatedAccount = accountWithId.copy(amount = newAmount)

    override val idGenerator: IdGenerator[Long] = stub[IdGenerator[Long]]
    (idGenerator.getNextId _).when().returns(accountId)

    override lazy val accountDatabase: Database[model.Account, Long] = new InMemoryAccountDatabase(idGenerator)

    override implicit val executionContext: ExecutionContext = ExecutionContext.global
  }

  "InMemoryAccountDatabase" can {
    "create" should {
      "return successful result for new account" in new Context {
        accountDatabase.create(newAccount).map(tryCreate => {
          assert(tryCreate.isSuccess === true, "create should be successful")
        })
      }

      "return result with newly created account for new account" in new Context {
        for {
          tryCreate <- accountDatabase.create(newAccount)
          returnedAccount <- Future.fromTry(tryCreate)
        } yield {
          assert(returnedAccount === accountWithId, "returned and expected accounts do not match")
        }
      }

      "insert account in database for new account" in new Context {
        for {
          _ <- accountDatabase.create(newAccount)
          maybeAccount <- accountDatabase.get(accountId)
        } yield {
          assert(maybeAccount === Some(accountWithId), "account should have been inserted")
        }
      }

      "fail for account with id" in new Context {
        accountDatabase.create(accountWithId).map(tryCreate => {
          assert(tryCreate.isFailure === true, "create should fail")
        })
      }
    }

    "update" should {
      "return successful result for account with id" in new Context {
        for {
          _ <- accountDatabase.create(newAccount)
          tryUpdate <- accountDatabase.update(updatedAccount)
        } yield {
          assert(tryUpdate.isSuccess === true, "update should be successful")
        }
      }

      "return result with updated account for account with id" in new Context {
        for {
          _ <- accountDatabase.create(newAccount)
          tryUpdate <- accountDatabase.update(updatedAccount)
          returnedAccount <- Future.fromTry(tryUpdate)
        } yield {
          assert(returnedAccount === updatedAccount, "returned and expected account do not match")
        }
      }

      "update account in database for account with id" in new Context {
        for {
          _ <- accountDatabase.create(newAccount)
          _ <- accountDatabase.update(updatedAccount)
          maybeAccount <- accountDatabase.get(accountId)
        } yield {
          assert(maybeAccount === Some(updatedAccount), "account should be updated")
        }
      }

      "fail for new account" in new Context {
        accountDatabase.update(newAccount).map(tryUpdate => {
          assert(tryUpdate.isFailure === true, "update should fail")
        })
      }

      "fail for non existing account" in new Context {
        accountDatabase.update(accountWithId).map(tryUpdate => {
          assert(tryUpdate.isFailure === true, "update should fail")
        })
      }
    }

    "get" should {
      "return account if it exist in database" in new Context {
        for {
          _ <- accountDatabase.create(newAccount)
          maybeAccount <- accountDatabase.get(accountId)
        } yield {
          assert(maybeAccount === Some(accountWithId), "returned account should much expected")
        }
      }

      "return None if account does not exist in database" in new Context {
        accountDatabase.get(accountId).map(maybeAccount => {
          assert(maybeAccount.isEmpty === true, "returned option should be empty")
        })
      }
    }
  }

}
