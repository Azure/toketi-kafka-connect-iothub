// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.kafka.connect.source

import java.time.Instant

import com.microsoft.azure.iot.kafka.connect.source.testhelpers.TestConfig
import org.apache.kafka.connect.errors.ConnectException
import org.json4s.jackson.Serialization.read
import org.scalatest.{FlatSpec, GivenWhenThen}

class IotHubSourceConnectorTest extends FlatSpec with GivenWhenThen with JsonSerialization {

  "IotHubSourceConnector" should "validate all input properties and generate right set of task config properties" in {
    Given("Valid set of input properties")
    val inputProperties = TestConfig.sourceConnectorTestProps
    val connector = new IotHubSourceConnector

    When("Start and TaskConfig are called in right order")
    connector.start(inputProperties)
    val taskConfigs = connector.taskConfigs(3)

    Then("The TaskConfigs have all the expected properties")
    assert(taskConfigs.size() == 3)
    for (i <- 0 until 3) {
      val taskConfig: java.util.Map[String, String] = taskConfigs.get(i)
      assert(taskConfig.containsKey(IotHubSourceConfig.EventHubCompatibleConnectionString))
      assert(taskConfig.containsKey(IotHubSourceConfig.IotHubConsumerGroup))
      assert(taskConfig.containsKey(IotHubSourceConfig.KafkaTopic))
      assert(taskConfig.containsKey(IotHubSourceConfig.BatchSize))
      assert(taskConfig.containsKey(IotHubSourceConfig.ReceiveTimeout))
      assert(taskConfig.containsKey(IotHubSourceConfig.TaskPartitionOffsetsMap))

      assert(taskConfig.get(IotHubSourceConfig.EventHubCompatibleConnectionString) != "")
      assert(taskConfig.get(IotHubSourceConfig.BatchSize) == "100")
      assert(taskConfig.get(IotHubSourceConfig.ReceiveTimeout) == "45")
      assert(taskConfig.get(IotHubSourceConfig.IotHubOffset) == "-1,5,10,15,-1")
    }

    val task0PartitionOffset =
      read[Map[String, String]](taskConfigs.get(0).get(IotHubSourceConfig.TaskPartitionOffsetsMap))
    assert(task0PartitionOffset.size == 2)
    assert(task0PartitionOffset("0") == "-1")
    assert(task0PartitionOffset("3") == "15")

    val task1PartitionOffset =
      read[Map[String, String]](taskConfigs.get(1).get(IotHubSourceConfig.TaskPartitionOffsetsMap))
    assert(task1PartitionOffset.size == 1)
    assert(task1PartitionOffset("1") == "5")

    val task2PartitionOffset =
      read[Map[String, String]](taskConfigs.get(2).get(IotHubSourceConfig.TaskPartitionOffsetsMap))
    assert(task2PartitionOffset.size == 1)
    assert(task2PartitionOffset("2") == "10")
  }

  "IotHubSourceConnector" should "validate all input properties, including start time and generate right set of task" +
    " config properties" in {
    Given("Valid set of input properties")
    val inputProperties = TestConfig.sourceConnectorTestPropsStartTime
    val connector = new IotHubSourceConnector

    When("Start and TaskConfig are called in right order")
    connector.start(inputProperties)
    val taskConfigs = connector.taskConfigs(3)

    Then("The TaskConfigs have all the expected properties")
    assert(taskConfigs.size() == 3)
    for (i <- 0 until 3) {
      val taskConfig: java.util.Map[String, String] = taskConfigs.get(i)
      assert(taskConfig.containsKey(IotHubSourceConfig.EventHubCompatibleConnectionString))
      assert(taskConfig.containsKey(IotHubSourceConfig.IotHubConsumerGroup))
      assert(taskConfig.containsKey(IotHubSourceConfig.KafkaTopic))
      assert(taskConfig.containsKey(IotHubSourceConfig.BatchSize))
      assert(taskConfig.containsKey(IotHubSourceConfig.ReceiveTimeout))
      assert(taskConfig.containsKey(IotHubSourceConfig.TaskPartitionOffsetsMap))

      assert(taskConfig.get(IotHubSourceConfig.EventHubCompatibleConnectionString) != "")
      assert(taskConfig.get(IotHubSourceConfig.BatchSize) == "100")
      assert(taskConfig.get(IotHubSourceConfig.ReceiveTimeout) == "60")
      assert(taskConfig.get(IotHubSourceConfig.IotHubOffset) == "")
      assert(taskConfig.get(IotHubSourceConfig.IotHubStartTime) == "2016-12-10T00:00:00Z")
    }

    val task0StartTime = taskConfigs.get(0).get(IotHubSourceConfig.IotHubStartTime)
    assert(task0StartTime == "2016-12-10T00:00:00Z")
    val startTime = Instant.parse(task0StartTime)
    assert(startTime != null)
    assert(startTime.isAfter(Instant.MIN))

    val task1StartTime = taskConfigs.get(1).get(IotHubSourceConfig.IotHubStartTime)
    assert(task1StartTime == "2016-12-10T00:00:00Z")
    val task2StartTime = taskConfigs.get(2).get(IotHubSourceConfig.IotHubStartTime)
    assert(task2StartTime == "2016-12-10T00:00:00Z")
  }

  it should "throw exception if invalid set of input properties are passed in" in {
    Given("Invalid set of input properties")
    val inputProperties = TestConfig.invalidSourceConnectorTestProps
    val connector = new IotHubSourceConnector

    When("IotHubSourceConnector.Start is called, ConfigException is thrown")
    intercept[ConnectException] {
      connector.start(inputProperties)
    }
  }
}
