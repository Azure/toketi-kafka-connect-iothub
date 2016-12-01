// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.kafka.connect.testhelpers

import com.microsoft.azure.iot.kafka.connect.{DataReceiver, IotMessage, IotMessageData, JsonSerialization}
import org.json4s.jackson.Serialization.write

import scala.util.Random

class MockDataReceiver extends DataReceiver with JsonSerialization {

  private val random : Random = new Random

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

    val deviceId = "device" + random.nextInt(10)
    val offset = random.nextString(10)
    val systemProperties = Map[String, String]("iothub-connection-device-id" -> deviceId, "x-opt-offset" -> offset)
    val messageProperties = Map[String, String]("model" -> "TestModel")
    val iotMessageData = IotMessageData(deviceTempStr, systemProperties, messageProperties)
    val iotMessage = IotMessage(iotMessageData, deviceId, offset)
    iotMessage
  }

  override def close(): Unit = {}
}
