// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.kafka.connect.testhelpers

import java.util

import com.microsoft.azure.iot.kafka.connect.IotHubSourceConfig
import com.microsoft.azure.servicebus.ConnectionStringBuilder
import com.typesafe.config.ConfigFactory

object TestConfig {

  lazy val sourceTaskTestProps: util.Map[String, String] = {
    val props = new util.HashMap[String, String]()
    props.put(IotHubSourceConfig.EventHubCompatibleConnectionString, connStr.toString)
    props.put(IotHubSourceConfig.IotHubConsumerGroup, "$Default")
    props.put(IotHubSourceConfig.TaskPartitionOffsetsMap, """{"0":"5","2":"10","3":"-1"}""")
    props.put(IotHubSourceConfig.KafkaTopic, "test")
    props.put(IotHubSourceConfig.BatchSize, "5")
    props
  }

  lazy val sourceSingleTaskTestProps: util.Map[String, String] = {
    val props = new util.HashMap[String, String]()
    props.put(IotHubSourceConfig.EventHubCompatibleConnectionString, connStr.toString)
    props.put(IotHubSourceConfig.IotHubConsumerGroup, "$Default")
    props.put(IotHubSourceConfig.TaskPartitionOffsetsMap, """{"0":"-1"}""")
    props.put(IotHubSourceConfig.KafkaTopic, "test")
    props.put(IotHubSourceConfig.BatchSize, "5")
    props
  }

  lazy val sourceConnectorTestProps: util.Map[String, String] = {
    val props = new util.HashMap[String, String]()
    props.put(IotHubSourceConfig.EventHubCompatibleName, iotHubName)
    props.put(IotHubSourceConfig.EventHubCompatibleNamespace, iotHubNamespace)
    props.put(IotHubSourceConfig.IotHubAccessKeyName, iotHubKeyName)
    props.put(IotHubSourceConfig.IotHubAccessKeyValue, iotHubKeyValue)
    props.put(IotHubSourceConfig.IotHubPartitions, iotHubPartitions.toString)
    props.put(IotHubSourceConfig.KafkaTopic, "test")
    props.put(IotHubSourceConfig.IotHubOffset, "-1,5,10,15,-1")
    props
  }

  lazy val invalidSourceConnectorTestProps: util.Map[String, String] = {
    val props = new util.HashMap[String, String]()
    props.put(IotHubSourceConfig.EventHubCompatibleName, iotHubName)
    props.put(IotHubSourceConfig.EventHubCompatibleNamespace, iotHubNamespace)
    props.put(IotHubSourceConfig.IotHubAccessKeyName, iotHubKeyName)
    props.put(IotHubSourceConfig.IotHubAccessKeyValue, iotHubKeyValue)
    props
  }

  lazy private val config           = ConfigFactory.load()
  lazy private val iotHubConfig     = config.getConfig("iothub")
  lazy private val iotHubName       = iotHubConfig.getString("name")
  lazy private val iotHubNamespace  = iotHubConfig.getString("namespace")
  lazy private val iotHubKeyName    = iotHubConfig.getString("keyName")
  lazy private val iotHubKeyValue   = iotHubConfig.getString("key")
  lazy private val iotHubPartitions = iotHubConfig.getInt("partitions")
  lazy private val connStr          = new ConnectionStringBuilder(iotHubNamespace, iotHubName, iotHubKeyName, iotHubKeyValue)
}
