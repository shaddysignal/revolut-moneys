package com.github.shaddysignal.revolut

import com.github.shaddysignal.revolut.service.{AccountService, ServiceModule, TransferService}

import scala.concurrent.ExecutionContext

trait WebMainModule {
  this: ServiceModule =>

  import com.softwaremill.macwire._

  lazy val restResource: RestResource = wire[RestResource]

  def accountService: AccountService
  def transferService: TransferService

  implicit def executionContext: ExecutionContext
}
