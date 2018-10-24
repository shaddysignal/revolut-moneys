package com.github.shaddysignal.revolut.service

import com.github.shaddysignal.revolut.model.Account

import scala.concurrent.Future
import scala.util.Try

trait AccountService extends Service[Account, Long] {
  def create(amount: BigDecimal): Future[Try[Account]]
  def update(account: Account): Future[Try[Account]]

  def lockById(id: Long): Try[Unit]
  def unlockById(id: Long): Unit
}
