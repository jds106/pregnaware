organization := "graviditate"

name := "ProgressSVC"

version := "1.0"

scalaVersion := "2.11.7"

// The general setup here is designed to make using Jenkins easier.
// See here:
//    http://yeghishe.github.io/2015/02/28/continuous-integration-for-scala-projects.html
//    http://www.whiteboardcoder.com/2014/01/jenkins-and-sbt.html

// Use the latest version of Scapegoat (static code analyser) - see https://github.com/sksamuel/scapegoat
scapegoatVersion := "1.1.1"

// ScalaStyle config - see http://www.scalastyle.org
scalastyleConfig := new sbt.File("project/scalastyle-config.xml")

// Don't produce the HTML report (not needed), but do produce the Cobertura file for Jenkins
coverageOutputHTML := false
coverageOutputCobertura := true

// Logging dependencies
libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
)

// Scalatra
libraryDependencies ++= Seq(
  "org.scalatra" %% "scalatra" % "2.4.+",
  "org.scalatra" %% "scalatra-json" % "2.4.+",
  "org.scalatra" %% "scalatra-swagger"% "2.4.+"
)

// JSON
libraryDependencies ++= Seq(
  "org.json4s"   %% "json4s-jackson" % "3.3.0"
)

// Jetty
libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-webapp" % "9.3.7.v20160115",
  "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016"
)

// Unit testing
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.6"
)

// Discard unnecessary duplicate files
assemblyMergeStrategy in assembly := {
  case other => (assemblyMergeStrategy in assembly).value(other)
}

scalacOptions ++= Seq(
  "-encoding", "UTF-8", "-deprecation", "-unchecked", "-feature", "-Xlint", "-Yinline-warnings", "-Ywarn-infer-any")

javacOptions  ++= Seq(
  "-Xlint:unchecked", "-Xlint:deprecation")

// Enable improved incremental compilation feature in 2.11.X.
// see http://www.scala-lang.org/news/2.11.1
incOptions := incOptions.value.withNameHashing(true)

