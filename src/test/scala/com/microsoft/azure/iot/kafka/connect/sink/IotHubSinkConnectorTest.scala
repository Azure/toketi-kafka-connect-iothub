/*
 * // Copyright (c) Microsoft. All rights reserved.
 */

package com.microsoft.azure.iot.kafka.connect.sink

import com.microsoft.azure.iot.kafka.connect.source.JsonSerialization
import com.microsoft.azure.iot.kafka.connect.sink.testhelpers.SinkTestConfig
import org.apache.kafka.connect.errors.ConnectException
import org.scalatest.{FlatSpec, GivenWhenThen}

class IotHubSinkConnectorTest extends FlatSpec with GivenWhenThen with JsonSerialization {

  "IotHubSinkConnector" should "validate all input properties and generate right set of task config properties" in {
    Given("Valid set of input properties")
    val inputProperties1 = SinkTestConfig.sinkConnectorTestProps
    var connector = new IotHubSinkConnector

    When("Start and TaskConfig are called in right order")
    connector.start(inputProperties1)

    Then("The TaskConfigs have all the expected properties")
    var taskConfigs = connector.taskConfigs(2)
    assert(taskConfigs.size() == 2)
    for (i <- 0 until 2) {
      val taskConfig: java.util.Map[String, String] = taskConfigs.get(i)
      assert(taskConfig.containsKey(IotHubSinkConfig.IotHubConnectionString))
    }

    Given("Valid set of input properties")
    val inputProperties2 = SinkTestConfig.sinkConnectorTestProps2
    connector = new IotHubSinkConnector

    When("Start and TaskConfig are called in right order")
    connector.start(inputProperties2)

    Then("The TaskConfigs have all the expected properties")
    taskConfigs = connector.taskConfigs(2)
    assert(taskConfigs.size() == 2)
    for (i <- 0 until 2) {
      val taskConfig: java.util.Map[String, String] = taskConfigs.get(i)
      assert(taskConfig.containsKey(IotHubSinkConfig.IotHubConnectionString))
    }
  }

  it should "throw an exception if invalid config properties are supplied" in {
    Given("Input properties without the required values values")
    val inputPropertiesWithoutRequiredValues = SinkTestConfig.invalidSinkConnectorTestProps
    var connector = new IotHubSinkConnector

    When("Connector.Start throws a ConnectException")
    intercept[ConnectException] {
      connector.start(inputPropertiesWithoutRequiredValues)
    }

    Given("Input properties with invalid values")
    val inputPropertiesWithInvalidValue = SinkTestConfig.invalidSinkConnectorTestProps2
    connector = new IotHubSinkConnector

    When("Connector.Start throws a ConnectException")
    intercept[ConnectException] {
      connector.start(inputPropertiesWithInvalidValue)
    }
  }
}
