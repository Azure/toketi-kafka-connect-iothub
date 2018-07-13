// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.kafka.connect.source

import java.time.{Duration, Instant}
import java.util.concurrent.Executors

import com.microsoft.azure.eventhubs.{EventHubClient, EventPosition, PartitionReceiver}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class EventHubReceiver(val connectionString: String, val receiverConsumerGroup: String, val partition: String,
    var offset: Option[String], val startTime: Option[Instant], val receiveTimeout: Duration) extends DataReceiver {

  private[this] var isClosing = false

  private val executorService = Executors.newSingleThreadExecutor()
  private val eventHubClient = EventHubClient.createSync(connectionString, executorService)
  if (eventHubClient == null) {
    throw new IllegalArgumentException("Unable to create EventHubClient from the input parameters.")
  }

  private val eventPosition = if (startTime.isDefined) {
    EventPosition.fromEnqueuedTime(startTime.get)
  }  else {
    EventPosition.fromOffset(offset.get)
  }
  private val eventHubReceiver: PartitionReceiver = eventHubClient.createReceiverSync(
    receiverConsumerGroup, partition.toString, eventPosition)
  if (this.eventHubReceiver == null) {
    throw new IllegalArgumentException("Unable to create PartitionReceiver from the input parameters.")
  }
  this.eventHubReceiver.setReceiveTimeout(receiveTimeout)

  override def close(): Unit = {
    if (this.eventHubReceiver != null) {
      this.eventHubReceiver.synchronized {
        this.isClosing = true
        eventHubReceiver.close().join()
      }
    }
  }

  override def receiveData(batchSize: Int): Iterable[IotMessage] = {
    var iotMessages = ListBuffer.empty[IotMessage]
      var curBatchSize = batchSize
      var endReached = false
      // Synchronize on the eventHubReceiver object, and make sure the task is not closing,
      // in which case, the eventHubReceiver might be closed.
      while (curBatchSize > 0 && !endReached && !this.isClosing) {
        this.eventHubReceiver.synchronized {
          if(!this.isClosing) {
            val batch = this.eventHubReceiver.receiveSync(curBatchSize)
            if (batch != null) {
              val batchIterable = batch.asScala
              iotMessages ++= batchIterable.map(e => {
                val content = new String(e.getBytes)
                val iotDeviceData = IotMessage(content, e.getSystemProperties.asScala, e.getProperties.asScala)
                iotDeviceData
              })
              curBatchSize -= batchIterable.size
            } else {
              endReached = true
            }
          }
        }
    }
    iotMessages
  }
}
