package com.github.shaddysignal.revolut.repo

import com.github.shaddysignal.revolut.model.Account

import scala.collection.parallel.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class InMemoryAccountDatabase(val idGenerator: IdGenerator[Long])
                             (implicit val executionContext: ExecutionContext) extends Database[Account, Long] {
  private val accounts: mutable.ParHashMap[Long, Account] = mutable.ParHashMap()

  override def create(entity: Account): Future[Try[Account]] = Future {
    entity.id match {
      case Some(_) => Failure(new Error(s"create with existing account(${entity.id})"))
      case None => {
        val id = idGenerator.getNextId
        val account = entity.copy(id = Some(id))

        accounts(id) = account
        Success(account)
      }
    }
  }

  override def update(entity: Account): Future[Try[Account]] = Future {
    entity.id.orElse(Some(Failure(new Error("update with new account"))))
      .flatMap({
        case id: Long => accounts.get(id).flatMap(_.id)
        case f: Failure[Nothing] => Some(f)
      }) // Transform to understand if account exist or not
      .map({
        case id: Long => accounts(id) = entity; Success(entity)
        case f: Failure[Nothing] => f
      }) // Actual update if necessary
      .getOrElse(Failure(new Error("update for non existing account")))
  }

  override def get(id: Long): Future[Option[Account]] = Future {
    accounts.get(id)
  }

  override def getAll: Future[Seq[Account]] = Future {
    accounts.values.toIndexedSeq
  }

  override def delete(id: Long): Future[Unit] = Future {
    accounts.remove(id)
  }
}
