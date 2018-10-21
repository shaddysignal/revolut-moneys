package com.github.shaddysignal.revolut

import java.util.concurrent.{ExecutorService, Executors}

import akka.http.scaladsl.server.{HttpApp, Route}
import com.github.shaddysignal.revolut.model.{Account, Transfer}
import com.github.shaddysignal.revolut.repo.RepoModule
import com.github.shaddysignal.revolut.service.ServiceModule
import de.heikoseeberger.akkahttpargonaut.ArgonautSupport
import org.slf4s.Logging

import scala.concurrent.ExecutionContext

class WebApp
  extends HttpApp
    with ArgonautSupport
    with Logging {

  lazy val modules = new WebMainModule with RepoModule with ServiceModule {
    implicit val executionContext: ExecutionContext = systemReference.get().dispatcher
  }

  override protected def routes: Route = modules.restResource.routes

}
