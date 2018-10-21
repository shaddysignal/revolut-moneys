package com.github.shaddysignal.revolut.repo

trait IdGenerator[T] {
  def getNextId: T
}
