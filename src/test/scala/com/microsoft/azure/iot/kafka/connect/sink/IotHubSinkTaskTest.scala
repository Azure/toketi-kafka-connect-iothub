/*
 * // Copyright (c) Microsoft. All rights reserved.
 */

package com.microsoft.azure.iot.kafka.connect.sink

import java.util.function._

import com.microsoft.azure.iot.kafka.connect.source.JsonSerialization
import com.microsoft.azure.iot.kafka.connect.sink.testhelpers.{SinkTestConfig, TestIotHubSinkTask, TestSinkRecords}
import com.microsoft.azure.sdk.iot.service.DeliveryAcknowledgement
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.errors.ConnectException
import org.apache.kafka.connect.sink.SinkRecord
import org.scalatest.{FlatSpec, GivenWhenThen}

import scala.language.implicitConversions

class IotHubSinkTaskTest extends FlatSpec with GivenWhenThen with JsonSerialization {

  implicit def toJavaPredicate[A](f: Function1[A, Boolean]) = new Predicate[A] {
    override def test(a: A): Boolean = f(a)
  }

  "IotHubSinkTask put" should "validate schema of SinkRecords and send them to the right destination" in {

    Given("IotHubSinkTask instance")
    val iotHubSinkTask = new TestIotHubSinkTask
    val sinkRecords = TestSinkRecords.getSinkSecords()
    assert(sinkRecords != null)
    assert(sinkRecords.size() > 0)

    When("IotHubSinkTask.start is called")
    iotHubSinkTask.start(SinkTestConfig.sinkTaskTestProps)

    Then("It initializes all the properties")
    assert(iotHubSinkTask.getDeliveryAcknowledgement() == DeliveryAcknowledgement.None)
    assert((iotHubSinkTask.getMessageSender()).isDefined)

    When("IotHubSinkTask.put is called")
    iotHubSinkTask.put(sinkRecords)

    Then("It sends the records to the right destination")
    Thread.sleep(1000)
    val sentRecords = iotHubSinkTask.getSentMessages()

    assert(sentRecords.size == sinkRecords.size())
    for (sentRecord ← sentRecords) {
      val predicate = (r: SinkRecord) ⇒ r.value().asInstanceOf[Struct].getString("messageId") == sentRecord.getMessageId
      val sinkRecord = sinkRecords.stream().filter(predicate).findAny()
      assert(sinkRecord != null && sinkRecord.isPresent)
    }
  }

  it should "deserialize records of string schema and send messages to IoT devices" in {
    Given("IotHubSinkTask instance and sinkRecords with schema type String ")

    val iotHubSinkTask = new TestIotHubSinkTask
    iotHubSinkTask.start(SinkTestConfig.sinkTaskTestProps)
    val sinkRecords = TestSinkRecords.getSinkRecordsWithStringSchema()
    assert(sinkRecords != null)
    assert(sinkRecords.size() > 0)

    When("IotHubSinkTask.put is called")
    iotHubSinkTask.put(sinkRecords)

    Then("It sends the records to the right destination")
    Thread.sleep(1000)
    val sentRecords = iotHubSinkTask.getSentMessages()

    assert(sentRecords.size == sinkRecords.size())
    for (sentRecord ← sentRecords) {
      val predicate = (r: SinkRecord) ⇒ r.value().asInstanceOf[String].contains(sentRecord.getMessageId)
      val sinkRecord = sinkRecords.stream().filter(predicate).findAny()
      assert(sinkRecord != null && sinkRecord.isPresent)
    }
  }

  it should "Throw an exception if records schema doesn't contain one of the required properties" in {

    Given("IotHubSinkTask instance and sinkRecords with schema that doesn't contain the required messageId property")

    val iotHubSinkTask = new TestIotHubSinkTask
    iotHubSinkTask.start(SinkTestConfig.sinkTaskTestProps)
    val sinkRecords = TestSinkRecords.getSinkRecordsWithMissingPropertySchema()
    assert(sinkRecords != null)
    assert(sinkRecords.size() > 0)

    When("IotHubSinkTask.put is called, it throws a ConnectException")
    intercept[ConnectException] {
      iotHubSinkTask.put(sinkRecords)
    }
  }

  it should "Throw an exception if records schema type that doesn't match the expected schema" in {

    Given("IotHubSinkTask instance and sinkRecords with schema type Int ")

    val iotHubSinkTask = new TestIotHubSinkTask
    iotHubSinkTask.start(SinkTestConfig.sinkTaskTestProps)
    val sinkRecords = TestSinkRecords.getSinkRecordsWithInvalidSchema()
    assert(sinkRecords != null)
    assert(sinkRecords.size() > 0)

    When("IotHubSinkTask.put is called, it throws a ConnectException")
    intercept[ConnectException] {
      iotHubSinkTask.put(sinkRecords)
    }
  }

  it should "Throw an exception if records contain a field with unexpected type" in {

    Given("IotHubSinkTask instance and sinkRecords with that contain messageId of type Int (instead of String)")

    val iotHubSinkTask = new TestIotHubSinkTask
    iotHubSinkTask.start(SinkTestConfig.sinkTaskTestProps)
    val sinkRecords = TestSinkRecords.getSinkRecordsWithInvalidFieldTypeSchema()
    assert(sinkRecords != null)
    assert(sinkRecords.size() > 0)

    When("IotHubSinkTask.put is called, it throws a ConnectException")
    intercept[ConnectException] {
      iotHubSinkTask.put(sinkRecords)
    }
  }
}
