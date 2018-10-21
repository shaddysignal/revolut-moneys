package com.github.shaddysignal.revolut.service

import com.github.shaddysignal.revolut.model.{Account, Transfer}
import com.github.shaddysignal.revolut.repo.{Database, RepoModule}

import scala.concurrent.ExecutionContext

trait ServiceModule {
  this: RepoModule =>

  import com.softwaremill.macwire._

  lazy val accountService: AccountService = wire[TheAccountService]
  lazy val transferService: TransferService = wire[TheTransferService]

  def accountDatabase: Database[Account, Long]
  def transferDatabase: Database[Transfer, Long]

  implicit def executionContext: ExecutionContext
}
