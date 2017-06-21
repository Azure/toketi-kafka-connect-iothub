/*
 * // Copyright (c) Microsoft. All rights reserved.
 */

package com.microsoft.azure.iot.kafka.connect.sink.testhelpers

import java.time.Instant
import java.util
import java.util.Date

import com.microsoft.azure.iot.kafka.connect.source.JsonSerialization
import com.microsoft.azure.iot.kafka.connect.sink.C2DMessage
import org.apache.kafka.connect.data.{Schema, SchemaBuilder, Struct}
import org.apache.kafka.connect.sink.SinkRecord

import scala.collection.JavaConverters._
import org.json4s.jackson.Serialization._

import scala.util.Random

object TestSinkRecords extends JsonSerialization {

  private val batchSize = 10
  private val topic     = "test"

  def getSinkSecords(): util.List[SinkRecord] = {
    generateSinkRecords((i: Int) ⇒ {
      val struct = new Struct(TestSchemas.validSchema)
        .put("deviceId", "device" + i)
        .put("message", "Turn off")
        .put("messageId", i.toString)
      (struct.schema(), struct)
    })
  }

  def getSinkRecordsWithMissingPropertySchema(): util.List[SinkRecord] = {
    generateSinkRecords((i: Int) ⇒ {
      val struct = new Struct(TestSchemas.missingFieldSchema)
        .put("deviceId", "device" + i)
        .put("message", "Turn off")
      (struct.schema(), struct)
    })
  }

  def getSinkRecordsWithInvalidSchema(): util.List[SinkRecord] = {
    generateSinkRecords((i: Int) ⇒ {
      (TestSchemas.invalidSchemaTypeSchema, "Foo")
    })
  }

  def getSinkRecordsWithStringSchema(): util.List[SinkRecord] = {
    generateSinkRecords((i: Int) ⇒ {
      val c2dMessage = C2DMessage(i.toString, "Turn Off", "messageId" + i.toString, Some(Date.from(Instant.now())))
      val value = write(c2dMessage)
      (TestSchemas.validStringSchema, value)
    })
  }

  private def generateSinkRecords(f: (Int) ⇒ (Schema, Object)): util.List[SinkRecord] = {
    val records = collection.mutable.ListBuffer.empty[SinkRecord]
    var offsetValue = 1
    for (i ← 0 until batchSize) {

      val valueAndSchema: (Schema, Object) = f(i)
      val record = new SinkRecord(topic, Random.nextInt(10), Schema.OPTIONAL_STRING_SCHEMA, null, valueAndSchema._1,
        valueAndSchema._2, offsetValue)
      records += record
      offsetValue += 1
    }

    records.asJava
  }

  def getSinkRecordsWithInvalidFieldTypeSchema(): util.List[SinkRecord] = {
    generateSinkRecords((i: Int) ⇒ {
      val struct = new Struct(TestSchemas.invalidFieldTypeSchema)
        .put("deviceId", "device" + i)
        .put("message", "Turn off")
        .put("messageId", 10)
      (struct.schema(), struct)
    })
  }

  def getStringSchemaRecord(): SinkRecord = {
    val value =
      """{"messageId":"message1","message":"Turn on","deviceId":"device1","expiryTime":"2017-01-17T19:25:50Z"}"""
    val record = new SinkRecord(topic, Random.nextInt(10), Schema.OPTIONAL_STRING_SCHEMA, null, Schema.STRING_SCHEMA,
      value, 0)
    record
  }

  def getStringSchemaRecord2(): SinkRecord = {
    val value =
      """{"messageId":"message1","message":"Turn on","deviceId":"device1"}"""
    val record = new SinkRecord(topic, Random.nextInt(10), Schema.OPTIONAL_STRING_SCHEMA, null, Schema.STRING_SCHEMA,
      value, 0)
    record
  }

  def getInvalidScringSchemaRecord(): SinkRecord = {
    val value =
      """{"msgId":"message1","message":"Turn on","id":"device1","expiryTime":"2017-01-17T19:25:50Z"}"""
    val record = new SinkRecord(topic, Random.nextInt(10), Schema.OPTIONAL_STRING_SCHEMA, null, Schema.STRING_SCHEMA,
      value, 0)
    record
  }
}

object TestSchemas {

  val validSchema: Schema = SchemaBuilder.struct()
    .field("deviceId", Schema.STRING_SCHEMA)
    .field("message", Schema.STRING_SCHEMA)
    .field("messageId", Schema.STRING_SCHEMA)
    .field("expiry", Schema.OPTIONAL_STRING_SCHEMA)

  val validSchemaWithMissingOptionalField: Schema = SchemaBuilder.struct()
    .field("deviceId", Schema.STRING_SCHEMA)
    .field("message", Schema.STRING_SCHEMA)
    .field("messageId", Schema.STRING_SCHEMA)

  val missingFieldSchema: Schema = SchemaBuilder.struct()
    .field("deviceId", Schema.STRING_SCHEMA)
    .field("message", Schema.STRING_SCHEMA)

  val invalidSchemaTypeSchema: Schema = Schema.OPTIONAL_STRING_SCHEMA

  val validStringSchema: Schema = Schema.STRING_SCHEMA

  val invalidFieldTypeSchema: Schema = SchemaBuilder.struct()
    .field("deviceId", Schema.STRING_SCHEMA)
    .field("message", Schema.STRING_SCHEMA)
    .field("messageId", Schema.INT32_SCHEMA)
}
