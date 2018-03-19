// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.kafka.connect.source

import java.text.SimpleDateFormat
import java.time.Instant

import com.microsoft.azure.eventhubs.impl.AmqpConstants
import com.microsoft.azure.iot.kafka.connect.source.testhelpers.DeviceTemperature
import org.apache.kafka.connect.data.Struct
import org.json4s.jackson.Serialization._
import org.scalatest.{FlatSpec, GivenWhenThen}

import scala.collection.mutable
import scala.util.Random

class IotMessageConverterTest extends FlatSpec with GivenWhenThen with JsonSerialization {

  private val random: Random = new Random

  "IotMessage Converter" should "populate right values for kafka message struct fields" in {

    Given("IotMessage object")
    val deviceTemp = DeviceTemperature(100.01, "F")
    val deviceTempStr = write(deviceTemp)

    val sequenceNumber = random.nextLong()
    val correlationId = random.nextString(10)
    val offset = random.nextString(10)
    val enqueuedDate = new SimpleDateFormat("MM/dd/yyyy").parse("12/01/2016")
    val systemProperties = mutable.Map[String, Object](
      "iothub-connection-device-id" → "device10",
      AmqpConstants.SEQUENCE_NUMBER_ANNOTATION_NAME → sequenceNumber.asInstanceOf[Object],
      AmqpConstants.AMQP_PROPERTY_CORRELATION_ID → correlationId,
      AmqpConstants.OFFSET_ANNOTATION_NAME → offset,
      AmqpConstants.ENQUEUED_TIME_UTC_ANNOTATION_NAME → enqueuedDate)

    val timestamp = Instant.now().toString
    val messageProperties = mutable.Map[String, Object](
      "timestamp" → timestamp,
      "contentType" → "temperature"
    )

    val iotMessage = IotMessage(deviceTempStr, systemProperties, messageProperties)

    When("getIotMessageStruct is called with IotMessage object")
    val kafkaMessageStruct: Struct = IotMessageConverter.getIotMessageStruct(iotMessage)

    Then("The struct has all the expected properties")
    assert(kafkaMessageStruct.getString("deviceId") == "device10")
    assert(kafkaMessageStruct.getString("offset") == offset)
    assert(kafkaMessageStruct.getString("contentType") == "temperature")
    assert(kafkaMessageStruct.getString("enqueuedTime") == enqueuedDate.toInstant.toString)
    assert(kafkaMessageStruct.getInt64("sequenceNumber") == sequenceNumber)
    assert(kafkaMessageStruct.getString("content") == deviceTempStr)

    val structSystemProperties = kafkaMessageStruct.getMap[String, String]("systemProperties")
    assert(structSystemProperties != null)
    assert(structSystemProperties.size == 1)
    assert(structSystemProperties.get(AmqpConstants.AMQP_PROPERTY_CORRELATION_ID) == correlationId)

    val structProperties = kafkaMessageStruct.getMap[String, String]("properties")
    assert(structProperties != null)
    assert(structProperties.size == 1)
    assert(structProperties.get("timestamp") == timestamp)
  }

  it should "use default values for missing properties" in {

    val deviceTemp = DeviceTemperature(100.01, "F")
    val deviceTempStr = write(deviceTemp)

    val systemProperties = mutable.Map.empty[String, Object]
    val messageProperties = mutable.Map.empty[String, Object]

    val iotMessage = IotMessage(deviceTempStr, systemProperties, messageProperties)

    When("getIotMessageStruct is called with IotMessage object")
    val kafkaMessageStruct: Struct = IotMessageConverter.getIotMessageStruct(iotMessage)

    Then("The struct has all the expected properties")
    assert(kafkaMessageStruct.getString("deviceId") == "")
    assert(kafkaMessageStruct.getString("offset") == "")
    assert(kafkaMessageStruct.getString("contentType") == "")
    assert(kafkaMessageStruct.getString("enqueuedTime") == "")
    assert(kafkaMessageStruct.getInt64("sequenceNumber") == 0)
    assert(kafkaMessageStruct.getString("content") == deviceTempStr)

    val structSystemProperties = kafkaMessageStruct.getMap[String, String]("systemProperties")
    assert(structSystemProperties != null)
    assert(structSystemProperties.size == 0)

    val structProperties = kafkaMessageStruct.getMap[String, String]("properties")
    assert(structProperties != null)
    assert(structProperties.size == 0)
  }
}
