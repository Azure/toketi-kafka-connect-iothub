// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.kafka.connect.source

import java.net.URI
import java.util

import com.microsoft.azure.eventhubs.ConnectionStringBuilder
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.common.config.{ConfigDef, ConfigException}
import org.apache.kafka.connect.connector.Task
import org.apache.kafka.connect.errors.ConnectException
import org.apache.kafka.connect.source.SourceConnector
import org.json4s.jackson.Serialization.write

import scala.collection.JavaConverters._
import scala.collection.mutable

class IotHubSourceConnector extends SourceConnector with LazyLogging with JsonSerialization {

  var START_OF_STREAM = "-1"
  private[this] var props: Map[String, String] = _

  override def taskClass(): Class[_ <: Task] = classOf[IotHubSourceTask]

  override def taskConfigs(maxTasks: Int): util.List[util.Map[String, String]] = {
    val configList = new util.ArrayList[util.Map[String, String]]()

    val partitionsCount = this.props(IotHubSourceConfig.IotHubPartitions).toInt
    val offsets = if (this.props.contains(IotHubSourceConfig.IotHubOffset)) {
      this.props(IotHubSourceConfig.IotHubOffset).split(",").map(_.trim)
    } else {
      Array.empty[String]
    }

    // Here we divide the partitions amongst the tasks in a round robin fashion.
    // So, say there are 5 partitions and 3 tasks, then task 1 gets partitions 0 and 3,
    // task 2 gets partitions 1 and 4 and task 3 gets partition 2.
    for (i <- 0 until maxTasks) {

      var partitionOffsetsMap = mutable.Map.empty[String, String]
      var partition = i
      while (partition < partitionsCount) {
        val partitionOffset = if (partition < offsets.length && offsets(partition).trim.length > 0) {
          offsets(partition)
        }
        else {
          this.START_OF_STREAM
        }
        partitionOffsetsMap += (partition.toString -> partitionOffset)
        partition = partition + maxTasks
      }

      if (partitionOffsetsMap.nonEmpty) {
        val config = new util.HashMap[String, String](this.props.asJava)
        val partitionOffsetsStr = write(partitionOffsetsMap)
        config.put(IotHubSourceConfig.TaskPartitionOffsetsMap, partitionOffsetsStr)
        configList.add(config)
      }
    }
    configList
  }

  override def stop(): Unit = {
    logger.info("Stopping IotHubSourceConnector")
  }

  override def config(): ConfigDef = IotHubSourceConfig.configDef

  override def start(props: util.Map[String, String]): Unit = {

    logger.info("Starting IotHubSourceConnector")

    var iotHubSourceConfigOption: Option[IotHubSourceConfig] = None

    try {
      iotHubSourceConfigOption = Some(IotHubSourceConfig.getConfig(props))
    } catch {
      case ex: ConfigException ⇒ throw new ConnectException("Could not start IotHubSourceConnector due to a " +
        "configuration exception", ex)
    }

    val iotHubSourceConfig = iotHubSourceConfigOption.get
    val iotHubConnectionString = new ConnectionStringBuilder()
      .setEndpoint(new URI(iotHubSourceConfig.getString(IotHubSourceConfig.EventHubCompatibleEndpoint)))
      .setEventHubName(iotHubSourceConfig.getString(IotHubSourceConfig.EventHubCompatibleName))
      .setSasKeyName(iotHubSourceConfig.getString(IotHubSourceConfig.IotHubAccessKeyName))
      .setSasKey(iotHubSourceConfig.getString(IotHubSourceConfig.IotHubAccessKeyValue)).toString
    this.props = Map[String, String](
      IotHubSourceConfig.EventHubCompatibleConnectionString -> iotHubConnectionString,
      IotHubSourceConfig.IotHubOffset -> iotHubSourceConfig.getString(IotHubSourceConfig.IotHubOffset),
      IotHubSourceConfig.BatchSize -> iotHubSourceConfig.getInt(IotHubSourceConfig.BatchSize).toString,
      IotHubSourceConfig.ReceiveTimeout -> iotHubSourceConfig.getInt(IotHubSourceConfig.ReceiveTimeout).toString,
      IotHubSourceConfig.KafkaTopic -> iotHubSourceConfig.getString(IotHubSourceConfig.KafkaTopic),
      IotHubSourceConfig.IotHubConsumerGroup -> iotHubSourceConfig.getString(IotHubSourceConfig.IotHubConsumerGroup),
      IotHubSourceConfig.IotHubPartitions -> iotHubSourceConfig.getInt(IotHubSourceConfig.IotHubPartitions).toString,
      IotHubSourceConfig.IotHubStartTime -> iotHubSourceConfig.getString(IotHubSourceConfig.IotHubStartTime),
      IotHubSourceConfig.EventHubCompatibleName → iotHubSourceConfig.getString(IotHubSourceConfig.EventHubCompatibleName)
    )
  }

  override def version(): String = getClass.getPackage.getImplementationVersion
}
