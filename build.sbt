// Copyright (c) Microsoft. All rights reserved.

name := "kafka-connect-iothub"
organization := "com.microsoft.azure.iot"
version := "0.5"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-deprecation", "-explaintypes", "-unchecked", "-feature")

libraryDependencies ++= {

  val kafkaVersion = "0.10.0.1"
  val azureEventHubSDKVersion = "0.9.0"
  val scalaLoggingVersion = "3.5.0"
  val logbackClassicVersion = "1.1.7"
  val scalaTestVersion = "3.0.0"
  val configVersion = "1.3.1"
  val json4sVersion = "3.5.0"

  Seq(
    "org.apache.kafka" % "connect-api" % kafkaVersion % "provided",
    "org.apache.kafka" % "connect-json" % kafkaVersion % "provided",
    "com.microsoft.azure" % "azure-eventhubs" % azureEventHubSDKVersion,
    "org.json4s" %% "json4s-jackson" % json4sVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
    "ch.qos.logback" % "logback-classic" % logbackClassicVersion,

    // Test dependencies
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
    "com.typesafe" % "config" % configVersion % "test"
  )
}

assemblyJarName in assembly := "kafka-connect-iothub-assembly_2.11-0.5.jar"

publishArtifact in Test := true
publishArtifact in(Compile, packageDoc) := true
publishArtifact in(Compile, packageSrc) := true
publishArtifact in(Compile, packageBin) := true

fork in run := true

licenses += ("MIT", url("https://github.com/Azure/toketi-kafka-connect-iothub/blob/master/LICENSE"))
publishMavenStyle := true

// Bintray: Organization > Repository > Package > Version
bintrayOrganization := Some("microsoftazuretoketi")
bintrayRepository := "toketi-repo"
bintrayPackage := "kafka-connect-iothub"
bintrayReleaseOnPublish in ThisBuild := true

// Required in Sonatype
pomExtra :=
    <url>https://github.com/Azure/toketi-kafka-connect-iothub</url>
    <scm><url>https://github.com/Azure/toketi-kafka-connect-iothub</url></scm>
    <developers><developer><id>microsoft</id><name>Microsoft</name></developer></developers>
