/*
 * // Copyright (c) Microsoft. All rights reserved.
 */

package com.microsoft.azure.iot.kafka.connect.sink

import java.time.Instant
import java.util.Date

import com.microsoft.azure.iot.kafka.connect.source.JsonSerialization
import org.apache.kafka.connect.data.{Schema, SchemaBuilder, Struct}
import org.apache.kafka.connect.errors.ConnectException
import org.apache.kafka.connect.sink.SinkRecord
import org.json4s.jackson.Serialization._

import scala.collection.JavaConverters._

object C2DMessageConverter extends JsonSerialization {

  private val messageIdKey  = "messageId"
  private val messageKey    = "message"
  private val deviceIdKey   = "deviceId"
  private val expiryTimeKey = "expiry"
  private val schemaName    = "iothub.kafka.connect.cloud2device.message"
  private val schemaVersion = 1

  // Public for testing purposes
  lazy val expectedSchema: Schema = SchemaBuilder.struct()
    .name(schemaName)
    .version(schemaVersion)
    .field(deviceIdKey, Schema.STRING_SCHEMA)
    .field(messageKey, Schema.STRING_SCHEMA)
    .field(messageIdKey, Schema.STRING_SCHEMA)
    .field(expiryTimeKey, Schema.OPTIONAL_STRING_SCHEMA)

  def validateSchemaAndGetMessage(record: SinkRecord): C2DMessage = {
    val schema = record.valueSchema()
    schema.`type`() match {
      case Schema.Type.STRING ⇒ deserializeMessage(record, schema)
      case Schema.Type.STRUCT ⇒ validateStructSchemaAndGetMessage(record, schema)
      case schemaType ⇒ throw new ConnectException(s"Schema of Kafka record is of type ${schema.`type`().toString}, " +
        s"while the supported schemas are 'struct' and 'string'")
    }
  }

  // Public for testing
  def deserializeMessage(record: SinkRecord, schema: Schema): C2DMessage = {
    try {
      val stringValue = record.value().asInstanceOf[String]
      val c2DMessage = read[C2DMessage](stringValue)
      c2DMessage
    } catch {
      case e: Exception ⇒ throw new ConnectException(s"Unable to convert record with " +
        s"schema ${schema.`type`().toString} and value ${record.value().toString} to C2D message"
      )
    }

  }

  // Public for testing purposes
  def validateStructSchema(schema: Schema): Unit = {
    if (schema.`type`() != expectedSchema.`type`()) {
      throw new ConnectException(s"Schema of Kafka record is of type ${schema.`type`().toString}, while expected " +
        s"schema of type ${expectedSchema.`type`().toString}")
    }

    for (expectedField ← expectedSchema.fields().asScala) {
      val field = schema.field(expectedField.name())
      if (field != null) {
        val expectedFieldSchema = expectedField.schema()
        val fieldSchema = field.schema()
        if (fieldSchema.`type`() != expectedFieldSchema.`type`()) {
          throw new ConnectException(s"Schema type of Kafka record field ${field.name()} - ${fieldSchema.`type`()} " +
            s"does not match the expected schema type ${expectedFieldSchema.`type`()}")
        }
      }
      else if (!expectedField.schema().isOptional) {
        throw new ConnectException(s"Schema of Kafka record does not contain required field ${expectedField.name()}")
      }
    }
  }

  private def validateStructSchemaAndGetMessage(record: SinkRecord, schema: Schema): C2DMessage = {
    validateStructSchema(schema)

    val structValue = record.value().asInstanceOf[Struct]
    val deviceId = structValue.getString(deviceIdKey)
    val message = structValue.getString(messageKey)
    val messageId = structValue.getString(messageIdKey)
    var expiryTime: Option[Date] = None
    if (schema.field(expiryTimeKey) != null) {
      val expiryTimeString = structValue.getString(expiryTimeKey)
      expiryTime = getExpiryTime(expiryTimeString)
    }
    C2DMessage(messageId, message, deviceId, expiryTime)
  }

  private def getExpiryTime(expiryTimeString: String): Option[Date] = {
    if (expiryTimeString != null) {
      try {
        val startTime = Instant.parse(expiryTimeString.trim)
        return Some(Date.from(startTime))
      } catch {
        case e: Exception => throw new ConnectException(s"ExpiryTime string $expiryTimeString} cannot be parsed to " +
          s"Instant object. Expected format is YYYY-MM-DDThh:mm:ssZ.", e)
      }
    }
    None
  }
}
