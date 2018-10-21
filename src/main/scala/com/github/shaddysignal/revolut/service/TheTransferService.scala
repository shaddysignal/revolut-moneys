package com.github.shaddysignal.revolut.service

import com.github.shaddysignal.revolut.model.Transfer
import com.github.shaddysignal.revolut.repo.Database
import org.slf4s.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class TheTransferService(val database: Database[Transfer, Long], val accountService: AccountService)
                        (implicit val executionContext: ExecutionContext) extends TransferService with Logging {
  override def create(sourceAccountId: Long, destinationAccountId: Long, amount: BigDecimal): Future[Try[Transfer]] = {
    log.info(s"creating transfer(sourceAccount = $sourceAccountId, " +
      s"destinationAccount = $destinationAccountId, amount = $amount)")
    val srcFuture = accountService.get(sourceAccountId)
    val dstFuture = accountService.get(destinationAccountId)

    (for (
      srcOption <- srcFuture;
      dstOption <- dstFuture
    ) yield {
      (for (
        src <- srcOption;
        dst <- dstOption
      ) yield {
        if (src.amount < amount) Future { Failure(new Error(s"source account(${src.id}) doesn't have enough funds")) }
        else {
          val tryLockAndTransfer = for (
            srcLockSuccessful <- accountService.lockById(sourceAccountId);
            dstLockSuccessful <- accountService.lockById(destinationAccountId)
          ) yield {
            if (!srcLockSuccessful || !dstLockSuccessful)
              Future { Failure(new Error(s"can't lock accounts(${src.id}, ${dst.id}) in question")) }
            else {
              accountService.update(src.copy(amount = src.amount - amount))
              accountService.update(dst.copy(amount = dst.amount + amount))
              database.create(Transfer(None, sourceAccountId, destinationAccountId, amount))
            }
          }

          accountService.unlockById(sourceAccountId)
          accountService.unlockById(destinationAccountId)

          Future.fromTry(tryLockAndTransfer)
        }
      }).getOrElse(Future { Failure(new Error(s"one of the accounts($srcOption, $dstOption) does not exist")) })
    }).flatten
  }

  override def get(id: Long): Future[Option[Transfer]] = database.get(id)

  override def getAll: Future[Seq[Transfer]] = database.getAll
}
