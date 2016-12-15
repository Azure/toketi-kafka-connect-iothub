// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.kafka.connect

import java.time.Instant

import com.microsoft.azure.eventhubs.{EventHubClient, PartitionReceiver}

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

class EventHubReceiver(val connectionString: String, val receiverConsumerGroup: String, val partition: String,
    var offset: Option[String], val startTime: Option[Instant]) extends DataReceiver {

  private val eventHubClient = EventHubClient.createFromConnectionStringSync(connectionString)
  if (eventHubClient == null) {
    throw new IllegalArgumentException("Unable to create EventHubClient from the input parameters.")
  }

  private val eventHubReceiver: PartitionReceiver = if (startTime.isDefined) {
    eventHubClient.createReceiverSync(receiverConsumerGroup, partition.toString, startTime.get)
  } else {
    eventHubClient.createReceiverSync(receiverConsumerGroup, partition.toString, offset.get)
  }

  if (this.eventHubReceiver == null) {
    throw new IllegalArgumentException("Unable to create PartitionReceiver from the input parameters.")
  }

  override def close(): Unit = {
    if (this.eventHubReceiver != null) {
      this.eventHubReceiver.close()
    }
  }

  override def receiveData(batchSize: Int): Iterable[IotMessage] = {
    var iotMessages = ListBuffer.empty[IotMessage]
    var curBatchSize = batchSize
    var endReached = false
    while (curBatchSize > 0 && !endReached) {
      val batch = this.eventHubReceiver.receiveSync(curBatchSize)
      if (batch != null && batch.nonEmpty) {
        iotMessages ++= batch.map(e => {
          val content = new String(e.getBody)
          val iotDeviceData = IotMessage(content, e.getSystemProperties, e.getProperties)
          iotDeviceData
        })
        curBatchSize -= batch.size
      } else {
        endReached = true
      }
    }
    iotMessages
  }
}
