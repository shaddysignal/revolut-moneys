package com.github.shaddysignal.revolut.repo

import com.github.shaddysignal.revolut.model.Transfer

import scala.collection.parallel.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class InMemoryTransferDatabase(val idGenerator: IdGenerator[Long])
                              (implicit val executionContext: ExecutionContext) extends Database[Transfer, Long] {
  private val transfers: mutable.ParHashMap[Long, Transfer] = mutable.ParHashMap()

  override def create(entity: Transfer): Future[Try[Transfer]] = Future {
    entity.id match {
      case Some(_) => Failure(new Error(s"create with not new transfer(${entity.id})"))
      case None => {
        val id = idGenerator.getNextId
        val transfer = entity.copy(id = Some(id))

        transfers(id) = transfer
        Success(transfer)
      }
    }
  }

  override def update(entity: Transfer): Future[Try[Transfer]] = Future {
    entity.id.orElse(Some(Failure(new Error("update with new transfer"))))
      .flatMap({
        case id: Long => transfers.get(id).flatMap(_.id)
        case f: Failure[Nothing] => Some(f)
      }) // Transform to understand if account exist or not
      .map({
        case id: Long => transfers(id) = entity; Success(entity)
        case f: Failure[Nothing] => f
      }) // Actual update if necessary
      .getOrElse(Failure(new Error("update for non existing transfer")))
  }

  override def get(id: Long): Future[Option[Transfer]] = Future {
    transfers.get(id)
  }

  override def getAll: Future[Seq[Transfer]] = Future {
    transfers.values.toIndexedSeq
  }

  override def delete(id: Long): Future[Unit] = Future {
    transfers.remove(id)
  }
}
