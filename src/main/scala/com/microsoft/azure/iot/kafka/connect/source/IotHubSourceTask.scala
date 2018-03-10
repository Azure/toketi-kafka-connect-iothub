// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.kafka.connect.source

import java.time.{Duration, Instant}
import java.util

import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.connect.errors.ConnectException
import org.apache.kafka.connect.source.{SourceRecord, SourceTask}
import org.apache.kafka.connect.storage.OffsetStorageReader
import org.json4s.jackson.Serialization.read

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.control.NonFatal

class IotHubSourceTask extends SourceTask with LazyLogging with JsonSerialization {

  // Public for testing
  val partitionSources = mutable.ListBuffer.empty[IotHubPartitionSource]

  override def poll(): util.List[SourceRecord] = {

    logger.debug("Polling for data")
    val list = mutable.ListBuffer.empty[SourceRecord]
    try {
      for (partitionSource <- this.partitionSources) {
        logger.debug(s"Polling for data in partition ${partitionSource.partition}")
        val sourceRecordsList = partitionSource.getRecords
        logger.info(s"Polling for data - Obtained ${sourceRecordsList.length} SourceRecords " +
          s"from ${partitionSource.eventHubName}:${partitionSource.partition}")
        list ++= sourceRecordsList
      }
    } catch {
      case NonFatal(e) =>
        val errorMsg = s"Error while polling for data. Exception - ${e.toString} Stack trace - ${e.printStackTrace()}"
        logger.error(errorMsg)
        throw new ConnectException("Error while polling for data", e)
    }
    list.asJava
  }

  override def start(props: util.Map[String, String]): Unit = {

    logger.debug(s"Starting IotHubSourceTask")

    val partitionOffsetsMapString = props.get(IotHubSourceConfig.TaskPartitionOffsetsMap)
    logger.debug(s"PartitionOffsetsMap is $partitionOffsetsMapString")
    val partitionOffsetsMap = read[Map[String, String]](partitionOffsetsMapString)
    val partitionsString = partitionOffsetsMap.keySet.mkString(",")
    logger.info(s"Starting IotHubSourceTask for partitions $partitionsString")

    val connectionString = props.get(IotHubSourceConfig.EventHubCompatibleConnectionString)
    val receiverConsumerGroup = props.get(IotHubSourceConfig.IotHubConsumerGroup)
    val topic = props.get(IotHubSourceConfig.KafkaTopic)
    val batchSize = props.get(IotHubSourceConfig.BatchSize).toInt
    val startTime = getStartTime(props.get(IotHubSourceConfig.IotHubStartTime))
    val eventHubName = props.get(IotHubSourceConfig.EventHubCompatibleName)
    val receiveTimeout = Duration.ofSeconds(props.get(IotHubSourceConfig.ReceiveTimeout).toInt)

    val offsetStorageReader: OffsetStorageReader = if (this.context != null) {
      this.context.offsetStorageReader()
    } else {
      null
    }

    for ((partition, initialOffset) <- partitionOffsetsMap) {

      val sourcePartition = Map(
        "EventHubName" → eventHubName,
        "EventHubPartition" → partition).asJava

      var partitionStartTime: Option[Instant] = None
      var partitionOffset: Option[String] = getSavedOffset(offsetStorageReader, sourcePartition)
      if (partitionOffset.isDefined) {
        logger.info(s"Setting up partition receiver $partition with previously saved offset ${partitionOffset.get}")
      } else if (startTime.isDefined) {
        partitionStartTime = startTime
        logger.info(s"Setting up partition receiver $partition with start time ${startTime}")
      } else {
        partitionOffset = Some(initialOffset)
        logger.info(s"Setting up partition receiver $partition with offset ${partitionOffset.get}")
      }

      val dataReceiver = getDataReceiver(connectionString, receiverConsumerGroup, partition,
        partitionOffset, partitionStartTime, receiveTimeout)
      val partitionSource = new IotHubPartitionSource(dataReceiver, partition, topic, batchSize,
        eventHubName, sourcePartition)
      this.partitionSources += partitionSource
    }
  }

  protected def getDataReceiver(connectionString: String, receiverConsumerGroup: String, partition: String,
      partitionOffset: Option[String], partitionStartTime: Option[Instant], receiveTimeout: Duration): DataReceiver = {
    new EventHubReceiver(connectionString, receiverConsumerGroup,
      partition, partitionOffset, partitionStartTime, receiveTimeout)
  }

  private def getStartTime(startTimeString: String): Option[Instant] = {
    if (startTimeString != null) {
      try {
        val startTime = Instant.parse(startTimeString.trim)
        return Some(startTime)
      } catch {
        case e: Exception =>
      }
    }
    None
  }

  private def getSavedOffset(offsetStorageReader: OffsetStorageReader, sourcePartition: util.Map[String, String]):
  Option[String] = {

    if (offsetStorageReader != null) {
      logger.debug(s"Attempting to get saved offset for SourcePartition: ${sourcePartition.toString}")
      val offsetMap = offsetStorageReader.offset(sourcePartition)
      if (offsetMap != null && offsetMap.containsKey("EventHubOffset")) {
        offsetMap.get("EventHubOffset") match {
          case storedOffset: String if storedOffset != null && storedOffset.trim.length > 0 =>
            return Some(storedOffset.trim)
        }
      }
    } else {
      logger.debug(s"Cannot get saved offset as OffsetStorageReader is null")
    }
    None
  }

  override def stop(): Unit = {
    logger.info("Stopping IotHubSourceTask")
    for (partitionSource <- this.partitionSources) {
      partitionSource.dataReceiver.close()
    }
  }

  override def version(): String = getClass.getPackage.getImplementationVersion
}
