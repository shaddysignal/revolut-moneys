package com.github.shaddysignal.revolut.repo

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait Database[T, I] {
  implicit val executionContext: ExecutionContext
  val idGenerator: IdGenerator[I]

  def create(entity: T): Future[Try[T]]
  def update(entity: T): Future[Try[T]]
  def get(id: I): Future[Option[T]]
  def delete(id: I): Future[Unit]

  def getAll: Future[Seq[T]]
}
