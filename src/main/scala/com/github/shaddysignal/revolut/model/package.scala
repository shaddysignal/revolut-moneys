package com.github.shaddysignal.revolut

import argonaut.CodecJson

package object model {
  import argonaut.Argonaut._

  case class Account(id: Option[Long], amount: BigDecimal)
  case class Transfer(id: Option[Long], sourceAccountId: Long, destinationAccountId: Long, amount: BigDecimal)

  object Account {
    implicit lazy val accountCodec: CodecJson[Account] = CodecJson.derive[Account]
  }

  object Transfer {
    implicit lazy val transferCodec: CodecJson[Transfer] = CodecJson.derive[Transfer]
  }

}