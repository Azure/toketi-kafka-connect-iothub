/*
 * // Copyright (c) Microsoft. All rights reserved.
 */

package com.microsoft.azure.iot.kafka.connect.sink

import java.time.Instant
import java.util.Date

import com.microsoft.azure.iot.kafka.connect.sink.testhelpers.{TestSchemas, TestSinkRecords}
import com.microsoft.azure.iot.kafka.connect.source.JsonSerialization
import org.apache.kafka.connect.errors.ConnectException
import org.scalatest.{FlatSpec, GivenWhenThen}

class C2DMessageConverterTest extends FlatSpec with GivenWhenThen with JsonSerialization {

  "C2DMessageConverter" should "validate the schema of a struct record against the expected schema" in {
    Given("A valid record schema")
    var schema = TestSchemas.validSchema
    When("ValidateSchema is called")
    Then("No exception is thrown")
    C2DMessageConverter.validateStructSchema(schema)

    Given("A valid record schema")
    schema = TestSchemas.validSchemaWithMissingOptionalField
    When("ValidateSchema is called")
    Then("No exception is thrown")
    C2DMessageConverter.validateStructSchema(schema)

    Given("A schema with an invalid type")
    schema = TestSchemas.invalidSchemaTypeSchema
    When("ValidateSchema is called")
    Then("A ConnectException is thrown")
    intercept[ConnectException] {
      C2DMessageConverter.validateStructSchema(schema)
    }

    Given("A schema with an invalid field type")
    schema = TestSchemas.invalidFieldTypeSchema
    When("ValidateSchema is called")
    Then("A ConnectException is thrown")
    intercept[ConnectException] {
      C2DMessageConverter.validateStructSchema(schema)
    }

    Given("A schema with a missing field")
    schema = TestSchemas.missingFieldSchema
    When("ValidateSchema is called")
    Then("A ConnectException is thrown")
    intercept[ConnectException] {
      C2DMessageConverter.validateStructSchema(schema)
    }
  }

  "C2DMessageConverter" should "deserialize sink records of String schema and return the C2D Message" in {
    Given("A valid record of string schema")
    var record = TestSinkRecords.getStringSchemaRecord()
    When("DeserializeMessage is called")
    var c2DMessage = C2DMessageConverter.deserializeMessage(record, record.valueSchema())
    Then("A valid C2D message is obtained")
    assert(c2DMessage != null)
    assert(c2DMessage.deviceId == "device1")
    assert(c2DMessage.messageId == "message1")
    assert(c2DMessage.message == "Turn on")
    assert(c2DMessage.expiryTime.isDefined)
    assert(c2DMessage.expiryTime.get.after(Date.from(Instant.parse("2016-01-01T00:00:00Z"))))

    Given("A valid record of string schema")
    record = TestSinkRecords.getStringSchemaRecord2()
    When("DeserializeMessage is called")
    c2DMessage = C2DMessageConverter.deserializeMessage(record, record.valueSchema())
    Then("A valid C2D message is obtained")
    assert(c2DMessage != null)
    assert(c2DMessage.deviceId == "device1")
    assert(c2DMessage.messageId == "message1")
    assert(c2DMessage.message == "Turn on")
    assert(c2DMessage.expiryTime.isEmpty)
  }

  "C2DMessageConverter" should "throw an exception if record with string schema has invalid data" in {
    Given("A record of string schema with invalid data")
    val record = TestSinkRecords.getInvalidScringSchemaRecord()
    When("DeserializeMessage is called")
    Then("Then a ConnectException is called")
    intercept[ConnectException] {
      C2DMessageConverter.deserializeMessage(record, record.valueSchema())
    }
  }
}
