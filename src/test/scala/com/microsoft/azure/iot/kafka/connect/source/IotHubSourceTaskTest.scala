// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.kafka.connect.source

import java.time.{Duration, Instant}
import java.util

import com.microsoft.azure.iot.kafka.connect.source.testhelpers.{DeviceTemperature, MockDataReceiver, TestConfig, TestIotHubSourceTask}
import org.apache.kafka.connect.data.Struct
import org.json4s.jackson.Serialization.read
import org.scalatest.{FlatSpec, GivenWhenThen}

class IotHubSourceTaskTest extends FlatSpec with GivenWhenThen with JsonSerialization {

  "IotHubSourceTask poll" should "return a list of SourceRecords with the right format" in {

    Given("IotHubSourceTask instance")

    val iotHubSourceTask = new TestIotHubSourceTask
    iotHubSourceTask.start(TestConfig.sourceTaskTestProps)

    When("IotHubSourceTask.poll is called")
    val sourceRecords = iotHubSourceTask.poll()

    Then("It returns a list of SourceRecords")
    assert(sourceRecords != null)
    assert(sourceRecords.size() == 15)
    for (i <- 0 until 15) {
      val record = sourceRecords.get(i)
      assert(record.topic() == TestConfig.sourceTaskTestProps.get(IotHubSourceConfig.KafkaTopic))
      assert(record.valueSchema() == IotMessageConverter.schema)
      val messageStruct = record.value().asInstanceOf[Struct]
      assert(messageStruct.getString("deviceId").startsWith("device"))
      assert(messageStruct.getString("contentType") == "temperature")
      val enqueuedTime = Instant.parse(messageStruct.getString("enqueuedTime"))
      assert(enqueuedTime.isAfter(Instant.parse("2016-11-20T00:00:00Z")))

      val systemProperties = messageStruct.getMap[String, String]("systemProperties")
      assert(systemProperties != null)
      assert(systemProperties.get("sequenceNumber") != "")
      assert(systemProperties.get("correlationId") != "")

      val properties = messageStruct.getMap[String, String]("properties")
      assert(properties != null)
      assert(properties.get("timestamp") != "")

      val deviceTemperature = read[DeviceTemperature](messageStruct.get("content").asInstanceOf[String])
      assert(deviceTemperature != null)
      assert(deviceTemperature.unit == "F")
      assert(deviceTemperature.value != 0)
    }
  }

  "IotHubSourceTask start" should "initialize all properties" in {

    Given("A list of properties for IotHubSourceTask")
    val props: util.Map[String, String] = TestConfig.sourceTaskTestProps

    When("IotHubSourceTask is started")
    val task = new TestIotHubSourceTask
    task.start(props)

    Then("Data receiver should be properly initialized")
    assert(task.partitionSources.length == 3)
    assert(!task.partitionSources.exists(s => s.dataReceiver == null))
    for (ps ← task.partitionSources) {
      val dataReceiver = ps.dataReceiver.asInstanceOf[MockDataReceiver]
      assert(dataReceiver.offset.isDefined)
      assert(dataReceiver.startTime.isEmpty)
      assert(dataReceiver.connectionString != "")
      assert(dataReceiver.receiverConsumerGroup != "")
      assert(dataReceiver.receiveTimeout == Duration.ofSeconds(5))
    }
  }

  it should "initialize start time correctly on the data receiver when it is passed in the config" in {

    Given("A list of properties with StartTime for IotHubSourceTask")
    val props: util.Map[String, String] = TestConfig.sourceTaskTestPropsStartTime

    When("IotHubSourceTask is started")
    val task = new TestIotHubSourceTask
    task.start(props)

    Then("Data receiver should be properly initialized, with StartTime, while Offsets value should be ignored")
    assert(task.partitionSources.length == 3)
    assert(!task.partitionSources.exists(s => s.dataReceiver == null))
    for (ps ← task.partitionSources) {
      val dataReceiver = ps.dataReceiver.asInstanceOf[MockDataReceiver]
      assert(dataReceiver.offset.isEmpty)
      assert(dataReceiver.startTime.isDefined)
      assert(dataReceiver.startTime.get == Instant.parse("2016-12-10T00:00:00Z"))
      assert(dataReceiver.connectionString != "")
      assert(dataReceiver.receiverConsumerGroup != "")
    }
  }
}

