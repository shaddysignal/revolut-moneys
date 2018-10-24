package com.github.shaddysignal.revolut.service

import java.util.NoSuchElementException

import com.github.shaddysignal.revolut.model.Transfer
import com.github.shaddysignal.revolut.repo.Database
import org.slf4s.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class TheTransferService(val database: Database[Transfer, Long], val accountService: AccountService)
                        (implicit val executionContext: ExecutionContext) extends TransferService with Logging {
  override def create(sourceAccountId: Long, destinationAccountId: Long, amount: BigDecimal): Future[Try[Transfer]] = {
    log.info(s"creating transfer(sourceAccount = $sourceAccountId, " +
      s"destinationAccount = $destinationAccountId, amount = $amount)")
    val srcFuture = accountService.get(sourceAccountId)
    val dstFuture = accountService.get(destinationAccountId)

    (for (
      srcOption <- srcFuture;
      dstOption <- dstFuture;
      src <- Future { srcOption.get };
      dst <- Future { dstOption.get }
    ) yield { // ensuring that accounts exist
      val tryLockAndTransfer = for (
        _ <- accountService.lockById(sourceAccountId);
        _ <- accountService.lockById(destinationAccountId)
      ) yield { // Locking accounts
        if (src.amount < amount)
          Future { Failure(new Error(s"source account(${src.id}) doesn't have enough funds")) }
        else {
          (for (
            tryUpdateSrc <- accountService.update(src.copy(amount = src.amount - amount));
            tryUpdateDst <- accountService.update(dst.copy(amount = dst.amount + amount));
            _ <- Future.fromTry(tryUpdateSrc);
            _ <- Future.fromTry(tryUpdateDst)
          ) yield { // ensuring that updates successful
            database.create(Transfer(None, sourceAccountId, destinationAccountId, amount))
          }).flatten
        }
      }

      Future.fromTry(tryLockAndTransfer).flatten.andThen({
        case _ => {
          accountService.unlockById(sourceAccountId)
          accountService.unlockById(destinationAccountId)
        }
      })
    }).transform({
      case Failure(_: NoSuchElementException) => Failure(new Error("one of the accounts does not exist"))
      case f@Failure(_) => f
      case s@Success(_) => s
    }).flatten
  }

  override def get(id: Long): Future[Option[Transfer]] = database.get(id)

  override def getAll: Future[Seq[Transfer]] = database.getAll
}
