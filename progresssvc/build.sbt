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

// Default versions
val akka = "2.3.6"
val swagger = "0.5.1"
val spray = "1.3.3"

libraryDependencies ++= Seq (
  // -- json --
  "org.json4s" %% "json4s-jackson" % "3.3.0"

  // -- Logging --
  ,"ch.qos.logback" % "logback-classic" % "1.1.3"

  // -- Akka --
  ,"com.typesafe.akka" %% "akka-actor" % akka
  ,"com.typesafe.akka" %% "akka-slf4j" % akka

  // -- Spray --
  ,"io.spray" %% "spray-routing" % spray
  ,"io.spray" %% "spray-client" % spray

  // -- JBCrypt --
  ,"org.mindrot" % "jbcrypt" % "0.3m"

  // -- Swagger --
  ,"com.gettyimages" %% "spray-swagger" % swagger

  // -- Database --
  ,"mysql" % "mysql-connector-java" % "5.1.36"
  ,"com.typesafe.play" %% "play-slick" % "1.1.1"
  ,"com.typesafe.slick" %% "slick-codegen" % "3.1.0"

  // -- config --
  ,"com.typesafe" % "config" % "1.2.1"

  // -- Testing --
  ,"org.scalatest" %% "scalatest" % "2.2.6" % "test"
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

