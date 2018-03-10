// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.kafka.connect.source

import com.microsoft.azure.eventhubs.EventHubClient
import org.apache.kafka.common.config.ConfigDef.{Importance, Type, Width}
import org.apache.kafka.common.config.{AbstractConfig, ConfigDef}

import java.util.Map

object IotHubSourceConfig {

  private val defaultBatchSize = 100
  private val defaultReceiveTimeout = 60
  private val iotConfigGroup   = "Azure IoT Hub"
  private val kafkaConfig      = "Kafka"

  val EventHubCompatibleConnectionString = "IotHub.EventHubCompatibleConnectionString"
  val EventHubCompatibleName             = "IotHub.EventHubCompatibleName"
  val EventHubCompatibleNameDoc          =
    """EventHub compatible name ("IoT Hub" >> your hub >> "Endpoints" >> "Events" >> "Event Hub-compatible name")"""
  val EventHubCompatibleEndpoint        = "IotHub.EventHubCompatibleEndpoint"
  val EventHubCompatibleEndpointDoc     =
    """EventHub compatible endpoint ("IoT Hub" >> your hub >> "Endpoints" >> "Events" >> "Event Hub-compatible """ +
      """endpoint")"""
  val IotHubAccessKeyName                = "IotHub.AccessKeyName"
  val IotHubAccessKeyNameDoc             =
    """IotHub access key name ("IoT Hub" >> your hub >> "Shared access policies", default is service)"""
  val IotHubAccessKeyValue               = "IotHub.AccessKeyValue"
  val IotHubAccessKeyValueDoc            =
    """IotHub access key value ("IoT Hub" >> your hub >> "Shared access policies" >> key name >> "Primary key")"""
  val IotHubConsumerGroup                = "IotHub.ConsumerGroup"
  val IotHubConsumerGroupDoc             = "The IoT Hub consumer group"
  val IotHubPartitions                   = "IotHub.Partitions"
  val IotHubPartitionsDoc                = "Number of IoT Hub partitions"
  val KafkaTopic                         = "Kafka.Topic"
  val KafkaTopicDoc                      = "Kafka topic to copy data to"
  val BatchSize                          = "BatchSize"
  val BatchSizeDoc                       = "The batch size for fetching records from IoT Hub"
  val ReceiveTimeout                     = "ReceiveTimeout"
  val ReceiveTimeoutDoc                  = "Max time to spend receiving messages from IoT Hub"
  val IotHubOffset                       = "IotHub.Offsets"
  val IotHubOffsetDoc                    =
    "Offset for each partition in IotHub, as a comma separated string. This value is ignored if IotHubStartTime is specified."
  val IotHubStartTime                    = "IotHub.StartTime"
  val IotHubStartTimeDoc                 = "The time after which to process messages from IoT Hub If this value " +
    "is specified, IotHubOffset value is ignored."
  val TaskPartitionOffsetsMap            = "TaskPartitions"

  lazy val configDef = new ConfigDef()
    .define(EventHubCompatibleName, Type.STRING, Importance.HIGH, EventHubCompatibleNameDoc, iotConfigGroup, 1, Width
      .MEDIUM, "Event Hub compatible name")
    .define(EventHubCompatibleEndpoint, Type.STRING, Importance.HIGH, EventHubCompatibleEndpointDoc,
      iotConfigGroup, 2, Width.MEDIUM, "Event Hub compatible endpoint")
    .define(IotHubAccessKeyName, Type.STRING, Importance.HIGH, IotHubAccessKeyNameDoc, iotConfigGroup, 3, Width.SHORT,
      "Access key name")
    .define(IotHubAccessKeyValue, Type.STRING, Importance.HIGH, IotHubAccessKeyValueDoc, iotConfigGroup, 4,
      Width.LONG, "Access key value")
    .define(IotHubConsumerGroup, Type.STRING, EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, Importance.MEDIUM,
      IotHubConsumerGroupDoc, iotConfigGroup, 5, Width.SHORT, "Consumer group")
    .define(IotHubPartitions, Type.INT, Importance.HIGH, IotHubPartitionsDoc, iotConfigGroup, 6, Width.SHORT,
      "IoT Hub partitions")
    .define(IotHubStartTime, Type.STRING, "", Importance.MEDIUM, IotHubStartTimeDoc, iotConfigGroup, 7, Width.MEDIUM,
      "Start time")
    .define(IotHubOffset, Type.STRING, "", Importance.MEDIUM, IotHubOffsetDoc, iotConfigGroup, 8, Width.MEDIUM,
      "Per partition offsets")
    .define(BatchSize, Type.INT, defaultBatchSize, Importance.MEDIUM, IotHubOffsetDoc, iotConfigGroup, 9, Width.SHORT,
      "Batch size")
    .define(ReceiveTimeout, Type.INT, defaultReceiveTimeout, Importance.MEDIUM, ReceiveTimeoutDoc, iotConfigGroup, 10,
      Width.SHORT, "Receive Timeout")
    .define(KafkaTopic, Type.STRING, Importance.HIGH, KafkaTopicDoc, kafkaConfig, 11, Width.MEDIUM, "Kafka topic")

  def getConfig(configValues: Map[String, String]): IotHubSourceConfig = {
    new IotHubSourceConfig(configDef, configValues)
  }

  def getEventHubCompatibleNamespace(eventHubCompatibleEndpoint: String): String = {
    eventHubCompatibleEndpoint.replaceFirst(".*://", "").replaceFirst("\\..*", "")
  }
}

class IotHubSourceConfig(configDef: ConfigDef, configValues: Map[String, String])
  extends AbstractConfig(configDef, configValues)

