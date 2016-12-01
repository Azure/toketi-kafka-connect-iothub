// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.kafka.connect

import com.microsoft.azure.eventhubs.EventHubClient
import org.apache.kafka.common.config.ConfigDef

object IotHubSourceConfig {

  val EventHubCompatibleConnectionString = "IotHub.EventHubCompatibleConnectionString"
  val EventHubCompatibleName             = "IotHub.EventHubCompatibleName"
  val EventHubCompatibleNameDoc          =
    """EventHub compatible name ("IoT Hub" >> your hub >> "Messaging" >> "Event Hub-compatible name")"""
  val EventHubCompatibleNamespace        = "IotHub.EventHubCompatibleNamespace"
  val EventHubCompatibleNamespaceDoc     =
    """EventHub compatible namespace ("IoT Hub" >> your hub > "Messaging" >> "Event Hub-compatible endpoint")"""
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
  val IotHubOffset                       = "IotHub.Offsets"
  val IotHubOffsetDoc                    =
    "Offset for each partition in IotHub. This value is ignored if IotHubStartTime is specified."
  val IotHubStartTime                    = "IotHub.StartTime"
  val IotHubStartTimeDoc                 = "The time after which to process messages from IoT Hub If this value " +
    "is specified, IotHubOffset value is ignored."
  val TaskPartitionOffsetsMap            = "TaskPartitions"
  private val defaultBatchSize = 100

  def getConfig: ConfigDef = {

    val config = new ConfigDef
    config.define(EventHubCompatibleName, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, EventHubCompatibleNameDoc)
    config.define(EventHubCompatibleNamespace, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH,
      EventHubCompatibleNamespaceDoc)
    config.define(IotHubAccessKeyName, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, IotHubAccessKeyNameDoc)
    config.define(IotHubAccessKeyValue, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, IotHubAccessKeyValueDoc)
    config.define(IotHubConsumerGroup, ConfigDef.Type.STRING, EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,
      ConfigDef.Importance.MEDIUM, IotHubConsumerGroupDoc)
    config.define(IotHubPartitions, ConfigDef.Type.INT, ConfigDef.Importance.HIGH, IotHubPartitionsDoc)
    config.define(KafkaTopic, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, KafkaTopicDoc)
    config.define(IotHubOffset, ConfigDef.Type.STRING, "", ConfigDef.Importance.MEDIUM, IotHubOffsetDoc)
    config.define(IotHubStartTime, ConfigDef.Type.STRING, "", ConfigDef.Importance.MEDIUM, IotHubStartTimeDoc)
    config.define(BatchSize, ConfigDef.Type.INT, defaultBatchSize, ConfigDef.Importance.MEDIUM, IotHubOffsetDoc)
  }
}
