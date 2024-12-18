ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.1"

name := "Simple-Messenger-Server"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl"          % "0.23.27",
  "org.http4s" %% "http4s-blaze-server" % "0.23.16",
  "org.http4s" %% "http4s-blaze-client" % "0.23.16",
  "org.http4s" %% "http4s-circe"        % "0.23.27",

  "io.circe"   %% "circe-generic"       % "0.14.7",

  "org.typelevel" %% "cats-effect"      % "3.5.4",

  "ch.qos.logback" % "logback-classic" % "1.5.6",

  "org.tpolecat" %% "doobie-core"      % "1.0.0-RC5",
  "org.tpolecat" %% "doobie-postgres"  % "1.0.0-RC5",
  "org.tpolecat" %% "doobie-scalatest" % "1.0.0-RC5" % "test"
)
