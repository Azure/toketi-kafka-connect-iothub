// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.kafka.connect.source.testhelpers

import java.text.SimpleDateFormat
import java.time.{Duration, Instant}

import com.microsoft.azure.eventhubs.impl.AmqpConstants
import com.microsoft.azure.iot.kafka.connect.source.{DataReceiver, IotMessage, JsonSerialization}
import org.json4s.jackson.Serialization.write

import scala.collection.mutable
import scala.util.Random

class MockDataReceiver(val connectionString: String, val receiverConsumerGroup: String, val partition: String,
    var offset: Option[String], val startTime: Option[Instant], val receiveTimeout: Duration
    ) extends DataReceiver with JsonSerialization {

  private val random: Random = new Random

  override def receiveData(batchSize: Int): Iterable[IotMessage] = {
    val list = scala.collection.mutable.ListBuffer.empty[IotMessage]
    for (i <- 0 until batchSize) {
      list += generateIotMessage(i)
    }
    list
  }

  def generateIotMessage(index: Int): IotMessage = {
    val temp = 70 + random.nextInt(10) + random.nextDouble()
    val deviceTemp = DeviceTemperature(temp, "F")
    val deviceTempStr = write(deviceTemp)

    val systemProperties = mutable.Map[String, Object](
      "iothub-connection-device-id" → s"device$index",
      AmqpConstants.SEQUENCE_NUMBER_ANNOTATION_NAME → index.toLong.asInstanceOf[Object],
      AmqpConstants.AMQP_PROPERTY_CORRELATION_ID → random.nextString(10),
      AmqpConstants.OFFSET_ANNOTATION_NAME → random.nextString(10),
      AmqpConstants.ENQUEUED_TIME_UTC_ANNOTATION_NAME → new SimpleDateFormat("MM/dd/yyyy").parse("12/01/2016"))

    val messageProperties = mutable.Map[String, Object](
      "timestamp" → Instant.now().toString,
      "contentType" → "temperature"
    )

    val iotMessage = IotMessage(deviceTempStr, systemProperties, messageProperties)
    iotMessage
  }

  override def close(): Unit = {}
}
