// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.kafka.connect

import java.util
import java.util.Collections

import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.source.SourceRecord
import org.json4s.jackson.Serialization.write

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import scala.util.control.NonFatal

class IotHubPartitionSource(val dataReceiver: DataReceiver,
    val partition: String,
    val topic: String,
    val batchSize: Int)
  extends LazyLogging
    with JsonSerialization {

  def getRecords: util.List[SourceRecord] = {

    logger.debug("Polling for data")
    val list = ListBuffer.empty[SourceRecord]
    try {
      val messages: Iterable[IotMessage] = this.dataReceiver.receiveData(batchSize)

      if (messages.isEmpty) {
        logger.debug(s"Finished processing all messages from partition ${this.partition}")
      } else {
        logger.debug(s"Received ${messages.size} messages from partition ${this.partition} " +
          s"(requested $batchSize batch)")

        val sourcePartitionKey = Collections.singletonMap("EventHubPartitionKey", this.partition)

        for (msg: IotMessage <- messages) {

          val kafkaMessage = write(msg.data)
          val sourceOffset = Collections.singletonMap("EventHubOffset", msg.offset)
          // Using DeviceId as the Key for the Kafka Message.
          // This can be made configurable in the future
          val sourceRecord = new SourceRecord(sourcePartitionKey, sourceOffset, this.topic,
            Schema.STRING_SCHEMA, msg.deviceId, Schema.STRING_SCHEMA, kafkaMessage)
          list.add(sourceRecord)
        }
      }
    } catch {
      case NonFatal(e) =>
        val errorMsg = s"Error while getting SourceRecords for partition ${this.partition}. " +
          s"Exception - ${e.toString} Stack trace - ${e.printStackTrace()}"
        logger.error(errorMsg)
        throw e
    }
    logger.debug(s"Obtained ${list.length} SourceRecords from IotHub")
    list
  }
}
