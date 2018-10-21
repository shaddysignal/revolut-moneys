import sbt._

object Dependencies {
  lazy val akkaHttp =         "com.typesafe.akka"         %% "akka-http"            % "10.1.5"
  lazy val akkaHttpArgonaut = "de.heikoseeberger"         %% "akka-http-argonaut"   % "1.22.0"
  lazy val macwire =          "com.softwaremill.macwire"  %% "macros"               % "2.3.1"
  lazy val slf4s =            "org.slf4s"                 %% "slf4s-api"            % "1.7.25"
  lazy val logback =          "ch.qos.logback"            %  "logback-classic"      % "1.2.3"
  lazy val akkaSlf4j =        "com.typesafe.akka"         %% "akka-slf4j"           % "2.5.17"
  lazy val scalaTest =        "org.scalatest"             %% "scalatest"            % "3.0.5"
  lazy val akkaHttpTestkit =  "com.typesafe.akka"         %% "akka-http-testkit"    % "10.1.5"
  lazy val scalaMock =        "org.scalamock"             %% "scalamock"            % "4.1.0"
}
