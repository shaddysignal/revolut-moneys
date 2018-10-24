package com.github.shaddysignal.revolut.service

import java.util.concurrent.{Semaphore, TimeUnit}

import com.github.shaddysignal.revolut.model.Account
import com.github.shaddysignal.revolut.repo.Database
import org.slf4s.Logging

import scala.collection.parallel.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class TheAccountService(val database: Database[Account, Long])
                       (implicit val executionContext: ExecutionContext) extends AccountService with Logging {
  private val accountLocks: mutable.ParHashMap[Long, Semaphore] = mutable.ParHashMap()

  override def create(amount: BigDecimal): Future[Try[Account]] = {
    log.info(s"creating account(amount = $amount)")
    if (amount < 0) Future { Failure(new Error("new account should have amount >= 0")) }
    else database.create(Account(None, amount)).map(tryCreate =>
      for (account <- tryCreate) yield {
        account.id.foreach(accountLocks(_) = new Semaphore(1))
        account
      })
  }

  override def update(account: Account): Future[Try[Account]] = {
    log.info(s"updating account(${account.id})")
    database.update(account)
  }

  override def get(id: Long): Future[Option[Account]] = database.get(id)

  override def getAll: Future[Seq[Account]] = database.getAll

  override def lockById(id: Long): Try[Unit] = {
    log.info(s"locking account($id)")
    accountLocks.get(id).map(semaphore => {
      if (semaphore.tryAcquire(AWAIT_LOCK_MSEC, TimeUnit.MILLISECONDS)) Success()
      else Failure(new Error(s"can't acquire account($id)"))
    }).getOrElse(Failure(new Error(s"locking non existing account")))
  }

  override def unlockById(id: Long): Unit = {
    log.info(s"unlocking account($id)")
    accountLocks.get(id).foreach(_.release())
  }
}
