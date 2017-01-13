/*
 * // Copyright (c) Microsoft. All rights reserved.
 */

package com.microsoft.azure.iot.kafka.connect.sink

import com.microsoft.azure.iot.kafka.connect.JsonSerialization
import com.microsoft.azure.iot.kafka.connect.sink.testhelpers.TestSchemas
import org.apache.kafka.connect.errors.ConnectException
import org.scalatest.{FlatSpec, GivenWhenThen}

class C2DMessageConverterTest extends FlatSpec with GivenWhenThen with JsonSerialization {

  "C2DMessageConverter" should "validate the schema of a record against the expected schema" in {
    Given("A valid record schema")
    var schema = TestSchemas.validSchema
    When("ValidateSchema is called")
    Then("No exception is thrown")
    C2DMessageConverter.validateSchema(schema)

    Given("A valid record schema")
    schema = TestSchemas.validSchemaWithMissingOptionalField
    When("ValidateSchema is called")
    Then("No exception is thrown")
    C2DMessageConverter.validateSchema(schema)

    Given("A schema with an invalid type")
    schema = TestSchemas.invalidSchemaTypeSchema
    When("ValidateSchema is called")
    Then("A ConnectException is thrown")
    intercept[ConnectException] {
      C2DMessageConverter.validateSchema(schema)
    }

    Given("A schema with an invalid field type")
    schema = TestSchemas.invalidFieldTypeSchema
    When("ValidateSchema is called")
    Then("No exception is thrown")
    intercept[ConnectException] {
      C2DMessageConverter.validateSchema(schema)
    }

    Given("A schema with a missing field")
    schema = TestSchemas.missingFieldSchema
    When("ValidateSchema is called")
    Then("No exception is thrown")
    intercept[ConnectException] {
      C2DMessageConverter.validateSchema(schema)
    }
  }
}
