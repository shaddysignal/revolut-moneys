package com.github.shaddysignal.revolut.repo

import java.util.concurrent.atomic.AtomicLong

class SimpleIdGenerator(value: Long = 0) extends IdGenerator[Long] {
  private val counter = new AtomicLong(value)

  override def getNextId: Long = counter.incrementAndGet()
}
