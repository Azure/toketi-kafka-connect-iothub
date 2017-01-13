/*
 * // Copyright (c) Microsoft. All rights reserved.
 */

package com.microsoft.azure.iot.kafka.connect.sink.testhelpers

import java.util

import org.apache.kafka.connect.data.{Schema, SchemaBuilder, Struct}
import org.apache.kafka.connect.sink.SinkRecord

import scala.collection.JavaConverters._
import scala.util.Random

object TestSinkRecords {

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

  def getSinkRecordsWithStringSchema(): util.List[SinkRecord] = {
    generateSinkRecords((i: Int) ⇒ {
      val value =
        """{"deviceId": "device1",""" +
          """"deviceId": "Turn off", """ +
          """"messageId": "1"}"""
      (TestSchemas.invalidSchemaTypeSchema, value)
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

  val invalidSchemaTypeSchema: Schema = Schema.STRING_SCHEMA

  val invalidFieldTypeSchema: Schema = SchemaBuilder.struct()
    .field("deviceId", Schema.STRING_SCHEMA)
    .field("message", Schema.STRING_SCHEMA)
    .field("messageId", Schema.INT32_SCHEMA)
}
