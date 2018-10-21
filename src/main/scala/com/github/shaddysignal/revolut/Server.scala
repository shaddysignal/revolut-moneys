package com.github.shaddysignal.revolut

import akka.actor.ActorSystem
import akka.http.scaladsl.settings.ServerSettings

import scala.concurrent.Await

object Server extends App {
  import scala.concurrent.duration._

  implicit val system: ActorSystem = ActorSystem("revolut-system")
  new WebApp().startServer("localhost", 8080, ServerSettings(system), system)
  Await.ready(system.terminate(), 15.seconds)
}
