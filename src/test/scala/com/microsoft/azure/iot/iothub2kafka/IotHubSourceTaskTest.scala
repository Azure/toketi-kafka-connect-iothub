// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.kafka.connect

import java.util

import com.microsoft.azure.iot.kafka.connect.testhelpers.{MockDataReceiver, TestConfig, TestIotHubSourceTask}
import org.apache.kafka.connect.data.Schema
import org.json4s.jackson.Serialization.read
import org.scalatest.{FlatSpec, GivenWhenThen}

class IotHubSourceTaskTest extends FlatSpec with GivenWhenThen with JsonSerialization {

  // This test is ignored as the configuration in the test application.conf has no values.
  // After updating the application.conf with the right values, this test can be run.
  "IotHubSourceTask start"
  it should "initialize all properties" in {

    Given("A list of properties for IotHubSourceTask")
    val props: util.Map[String, String] = TestConfig.sourceTaskTestProps

    When("IotHubSourceTask is started")
    val task = new TestIotHubSourceTask
    task.start(props)

    Then("Data receiver should be properly initialized")
    assert(task.partitionSources.length == 3)
    assert(!task.partitionSources.exists(s => s.dataReceiver == null))
  }

  "IotHubSourceTask poll" should "return a list of SourceRecords with the right format" in {

    Given("IotHubSourceTask instance")

    val dataReceiver = new MockDataReceiver()
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
      assert(record.valueSchema() == Schema.STRING_SCHEMA)
      val kafkaMessageString = record.value().asInstanceOf[String]
      val kafkaMessage = read[IotMessageData](kafkaMessageString)

      assert(kafkaMessage.content != "")
      assert(kafkaMessage.systemProperties != null)
      assert(kafkaMessage.systemProperties.size == 2)
      assert(kafkaMessage.properties != null)
      assert(kafkaMessage.properties.size == 1)
    }
  }
}
