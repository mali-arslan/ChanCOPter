import sbt.addCompilerPlugin

name := "ChanCOPter"

version := "0.1"

scalaVersion := "2.12.10"

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)


val circeVersion = "0.12.3"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies += "io.circe" %% "circe-generic-extras" %"0.12.2"
libraryDependencies += "org.typelevel" %% "cats-effect" % "2.0.0"
