package com.github.shaddysignal.revolut.service

import com.github.shaddysignal.revolut.repo.Database

import scala.concurrent.{ExecutionContext, Future}

trait Service[T, I] {
  val AWAIT_LOCK_MSEC = 5000L

  val database: Database[T, I]
  implicit val executionContext: ExecutionContext

  def get(id: I): Future[Option[T]]

  def getAll: Future[Seq[T]]
}
