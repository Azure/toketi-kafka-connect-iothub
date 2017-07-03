/*
 * // Copyright (c) Microsoft. All rights reserved.
 */

package com.microsoft.azure.iot.kafka.connect.sink.testhelpers

import java.util

import com.microsoft.azure.iot.kafka.connect.sink.IotHubSinkConfig
import com.microsoft.azure.sdk.iot.service.DeliveryAcknowledgement
import com.typesafe.config.ConfigFactory

object SinkTestConfig {

  lazy val sinkTaskTestProps: util.Map[String, String] = {
    val props = new util.HashMap[String, String]()
    props.put(IotHubSinkConfig.IotHubConnectionString, iotHubConnStr)
    props.put(IotHubSinkConfig.IotHubMessageDeliveryAcknowledgement, DeliveryAcknowledgement.None.toString)
    props
  }

  lazy val sinkConnectorTestProps: util.Map[String, String] = {
    val props = new util.HashMap[String, String]()
    props.put(IotHubSinkConfig.IotHubConnectionString, iotHubConnStr)
    props
  }

  lazy val sinkConnectorTestProps2: util.Map[String, String] = {
    val props = new util.HashMap[String, String]()
    props.put(IotHubSinkConfig.IotHubConnectionString, iotHubConnStr)
    props.put(IotHubSinkConfig.IotHubMessageDeliveryAcknowledgement, DeliveryAcknowledgement.PositiveOnly.toString)
    props
  }

  lazy val invalidSinkConnectorTestProps: util.Map[String, String] = {
    val props = new util.HashMap[String, String]()
    props
  }

  lazy val invalidSinkConnectorTestProps2: util.Map[String, String] = {
    val props = new util.HashMap[String, String]()
    props.put(IotHubSinkConfig.IotHubConnectionString, iotHubConnStr)
    props.put(IotHubSinkConfig.IotHubMessageDeliveryAcknowledgement, "InvalidValue")
    props
  }

  lazy private val config           = ConfigFactory.load()
  lazy private val iotHubConfig     = config.getConfig("iothub")
  lazy private val iotHubConnStr    = iotHubConfig.getString("iotHubConnectionString")
}
