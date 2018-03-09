// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.kafka.connect.source

import java.util.{Collections, Map}

import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.errors.ConnectException
import org.apache.kafka.connect.source.SourceRecord

import scala.collection.mutable.ListBuffer
import scala.util.control.NonFatal

class IotHubPartitionSource(val dataReceiver: DataReceiver,
    val partition: String,
    val topic: String,
    val batchSize: Int,
    val eventHubName: String,
    val sourcePartition: Map[String, String])
  extends LazyLogging
    with JsonSerialization {

  def getRecords: List[SourceRecord] = {

    logger.debug(s"Polling for data from eventHub $eventHubName partition $partition")
    val list = ListBuffer.empty[SourceRecord]
    try {
      val messages: Iterable[IotMessage] = this.dataReceiver.receiveData(batchSize)

      if (messages.isEmpty) {
        logger.debug(s"Finished processing all messages from eventHub $eventHubName " +
          s"partition ${this.partition}")
      } else {
        logger.debug(s"Received ${messages.size} messages from eventHub $eventHubName " +
          s"partition ${this.partition} (requested $batchSize batch)")

        for (msg: IotMessage <- messages) {

          val kafkaMessage: Struct = IotMessageConverter.getIotMessageStruct(msg)
          val sourceOffset = Collections.singletonMap("EventHubOffset",
            kafkaMessage.getString(IotMessageConverter.offsetKey))
          val sourceRecord = new SourceRecord(sourcePartition, sourceOffset, this.topic, kafkaMessage.schema(),
            kafkaMessage)
          list += sourceRecord
        }
      }
    } catch {
      case NonFatal(e) =>
        val errorMsg = s"Error while getting SourceRecords for eventHub $eventHubName " +
          s"partition $partition. Exception - ${e.toString} Stack trace - ${e.printStackTrace()}"
        logger.error(errorMsg)
        throw new ConnectException(errorMsg, e)
    }
    logger.debug(s"Obtained ${list.length} SourceRecords from IotHub")
    list.toList
  }
}
