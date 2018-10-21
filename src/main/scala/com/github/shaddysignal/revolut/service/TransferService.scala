package com.github.shaddysignal.revolut.service

import com.github.shaddysignal.revolut.model.Transfer

import scala.concurrent.Future
import scala.util.Try

trait TransferService extends Service[Transfer, Long] {
  def create(sourceAccountId: Long, destinationAccountId: Long, amount: BigDecimal): Future[Try[Transfer]]
}
