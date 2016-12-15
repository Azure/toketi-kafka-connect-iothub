// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.kafka.connect

import java.time.Instant
import java.util
import java.util.Collections

import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.connect.errors.ConnectException
import org.apache.kafka.connect.source.{SourceRecord, SourceTask}
import org.json4s.jackson.Serialization.read

import scala.collection.JavaConversions._
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
        list ++= sourceRecordsList
      }
    } catch {
      case NonFatal(e) =>
        val errorMsg = s"Error while polling for data. Exception - ${e.toString} Stack trace - ${e.printStackTrace()}"
        logger.error(errorMsg)
        throw new ConnectException("Error while polling for data", e)
    }
    logger.info(s"Polling for data - Obtained ${list.length} SourceRecords from IotHub")
    list
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

    val previousOffsets = getSavedOffsets(partitionOffsetsMap.keys)
    for ((partition, initialOffset) <- partitionOffsetsMap) {

      var partitionStartTime: Option[Instant] = None
      var partitionOffset: Option[String] = None
      if (previousOffsets.contains(partition) && previousOffsets(partition).trim.length > 0) {
        partitionOffset = Some(previousOffsets(partition))
        logger.debug(s"Setting up partition $partition with previously saved offset ${partitionOffset.get.toString}")
      } else if (startTime.isDefined) {
        partitionStartTime = startTime
        logger.debug(s"Setting up partition $partition with start time ${startTime.toString}")
      } else {
        partitionOffset = Some(initialOffset)
        logger.debug(s"Setting up partition $partition with offset ${partitionOffset.get.toString}")
      }

      val dataReceiver = getDataReceiver(connectionString, receiverConsumerGroup, partition,
        partitionOffset, partitionStartTime)
      val partitionSource = new IotHubPartitionSource(dataReceiver, partition, topic, batchSize)
      this.partitionSources += partitionSource
    }
  }

  protected def getDataReceiver(connectionString: String, receiverConsumerGroup: String,
      partition: String, partitionOffset: Option[String],
      partitionStartTime: Option[Instant]): DataReceiver = {
    new EventHubReceiver(connectionString, receiverConsumerGroup,
      partition, partitionOffset, partitionStartTime)
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

  private def getSavedOffsets(partitions: Iterable[String]): mutable.Map[String, String] = {

    val partitionOffsetMap = mutable.Map.empty[String, String]
    this.logger.debug("Getting offsets from previous values, if available")
    if (this.context != null) {

      val storageReader = this.context.offsetStorageReader()
      if (storageReader != null && partitions != null) {

        for (partition <- partitions) {

          logger.debug(s"Getting saved offset for partition $partition")
          val partitionKey = Collections.singletonMap("EventHubPartitionKey", partition)
          val offsetMap = storageReader.offset(partitionKey)
          if (offsetMap != null) {

            if (offsetMap.containsKey("EventHubOffset")) {

              offsetMap.get("EventHubOffset") match {
                case storedOffset: String if storedOffset != null && storedOffset.trim.length > 0 =>
                  partitionOffsetMap(partition) = storedOffset.trim
                  this.logger.info(s"Setting offset from previous snapshot for partition $partition " +
                    s"to value - $storedOffset")
              }
            }
          }
        }
      }
    }
    partitionOffsetMap
  }

  override def stop(): Unit = {
    logger.info("Stopping IotHubSourceTask")
    for (partitionSource <- this.partitionSources) {
      partitionSource.dataReceiver.close()
    }
  }

  override def version(): String = getClass.getPackage.getImplementationVersion
}