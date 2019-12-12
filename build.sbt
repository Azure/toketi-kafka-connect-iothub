// Copyright (c) Microsoft. All rights reserved.

val iotHubKafkaConnectVersion = "0.7.0"

name := "kafka-connect-iothub"
organization := "com.microsoft.azure.iot"
version := iotHubKafkaConnectVersion

scalaVersion := "2.13.1"

scalacOptions ++= Seq("-deprecation", "-explaintypes", "-unchecked", "-feature")

libraryDependencies ++= {

  val kafkaVersion = "2.3.0"
  val azureEventHubSDKVersion = "3.0.2"
  val scalaLoggingVersion = "3.9.2"
  val logbackClassicVersion = "1.2.3"
  val scalaTestVersion = "3.1.0"
  val configVersion = "1.3.1"
  val json4sVersion = "3.6.7"
  val iotHubServiceClientVersion = "1.19.0"
  Seq(
    "org.apache.kafka" % "connect-api" % kafkaVersion % "provided",
    "org.apache.kafka" % "connect-json" % kafkaVersion % "provided",
    "ch.qos.logback" % "logback-classic" % logbackClassicVersion % "provided",
    "com.microsoft.azure" % "azure-eventhubs" % azureEventHubSDKVersion,
    "org.json4s" %% "json4s-jackson" % json4sVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
    "com.microsoft.azure.sdk.iot" % "iot-service-client" % iotHubServiceClientVersion,
    "jakarta.ws.rs" % "jakarta.ws.rs-api" % "2.1.6",

    // Test dependencies
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
    "com.typesafe" % "config" % configVersion % "test"
  ).map(_ exclude("javax.ws.rs", "javax.ws.rs-api"))
}

assemblyJarName in assembly := s"kafka-connect-iothub-assembly_2.11-$iotHubKafkaConnectVersion.jar"

publishArtifact in Test := true
publishArtifact in(Compile, packageDoc) := true
publishArtifact in(Compile, packageSrc) := true
publishArtifact in(Compile, packageBin) := true

fork in run := true

licenses += ("MIT", url("https://github.com/Azure/toketi-kafka-connect-iothub/blob/master/LICENSE"))
publishMavenStyle := true

// Bintray: Organization > Repository > Package > Version
// bintrayOrganization := Some("microsoftazuretoketi")
// bintrayRepository := "toketi-repo"
// bintrayPackage := "kafka-connect-iothub"
// bintrayReleaseOnPublish in ThisBuild := true

// Required in Sonatype
pomExtra :=
    <url>https://github.com/Azure/toketi-kafka-connect-iothub</url>
    <scm><url>https://github.com/Azure/toketi-kafka-connect-iothub</url></scm>
    <developers><developer><id>microsoft</id><name>Microsoft</name></developer></developers>
