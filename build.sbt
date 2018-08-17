name := "fizzy"
organization := "com.woodpigeon"
version := "0.1.0"

scalaVersion := "2.12.5"
ensimeScalaVersion in ThisBuild := "2.12.5"

scalacOptions += "-Ypartial-unification"

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % "0.10.5",
  "co.fs2" %% "fs2-io" % "0.10.5",
  "io.circe" %% "circe-generic" % "0.8.0",
  "io.circe" %% "circe-fs2" % "0.8.0",
  "org.typelevel" %% "cats-core" % "1.1.0",
  "org.typelevel" %% "cats-effect" % "0.10",
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

