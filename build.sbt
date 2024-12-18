ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.1"

name := "Simple-Messenger-Server"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect"      % "3.5.4",

  "org.tpolecat" %% "doobie-core"      % "1.0.0-RC5",
  "org.tpolecat" %% "doobie-postgres"  % "1.0.0-RC5",
  "org.tpolecat" %% "doobie-scalatest" % "1.0.0-RC5" % "test"
)
