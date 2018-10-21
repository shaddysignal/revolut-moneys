package com.github.shaddysignal.revolut.repo

import com.github.shaddysignal.revolut.model.{Account, Transfer}

import scala.concurrent.ExecutionContext

trait RepoModule {
  import com.softwaremill.macwire._

  def idGenerator: IdGenerator[Long] = {
    lazy val startId: Long = 0
    wire[SimpleIdGenerator]
  }

  lazy val accountDatabase: Database[Account, Long] = wire[InMemoryAccountDatabase]
  lazy val transferDatabase: Database[Transfer, Long] = wire[InMemoryTransferDatabase]

  implicit def executionContext: ExecutionContext
}
