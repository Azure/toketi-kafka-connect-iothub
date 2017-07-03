/*
 * // Copyright (c) Microsoft. All rights reserved.
 */

package com.microsoft.azure.iot.kafka.connect.sink.testhelpers

import com.microsoft.azure.iot.kafka.connect.sink.{IotHubSinkTask, MessageSender}
import com.microsoft.azure.sdk.iot.service.{DeliveryAcknowledgement, Message}

import scala.collection.mutable.ArrayBuffer

class TestIotHubSinkTask extends IotHubSinkTask {

  def getSentMessages(): ArrayBuffer[Message] = this.messageSender.get.asInstanceOf[MockMessageSender].getSentMessages()

  def getDeliveryAcknowledgement(): DeliveryAcknowledgement = this.acknowledgement

  def getMessageSender(): Option[MessageSender] = this.messageSender

  override protected def getMessageSender(connectionString: String): MessageSender = {
    new MockMessageSender(connectionString)
  }
}