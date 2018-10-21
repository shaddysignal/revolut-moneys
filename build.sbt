import Dependencies._

lazy val root = (project in file(".")).
  enablePlugins(JavaAppPackaging).
  settings(
    inThisBuild(List(
      organization := "com.github.shaddysignal.revolut",
      scalaVersion := "2.12.7",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "revolut-moneys",
    libraryDependencies ++= Seq(
      akkaHttp, akkaHttpArgonaut, macwire, slf4s, logback, akkaSlf4j,
      scalaTest % Test, akkaHttpTestkit % Test, scalaMock % Test
    )
  )
