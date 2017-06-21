/*
 * // Copyright (c) Microsoft. All rights reserved.
 */

package com.microsoft.azure.iot.kafka.connect.sink.testhelpers

import com.microsoft.azure.iot.kafka.connect.sink.MessageSender
import com.microsoft.azure.sdk.iot.service.Message

import scala.collection.mutable.ArrayBuffer

class MockMessageSender(connectionString: String) extends MessageSender {

  private var messageList: Option[ArrayBuffer[Message]] = Some(ArrayBuffer.empty[Message])

  override def sendMessage(deviceId: String, message: Message): Unit = {
      messageList.get += message
  }

  override def close(): Unit = {
    // Set message list to None so that any calls to sendMessage after close will throw
    messageList = None
  }

  def getSentMessages(): ArrayBuffer[Message] = messageList.get
}