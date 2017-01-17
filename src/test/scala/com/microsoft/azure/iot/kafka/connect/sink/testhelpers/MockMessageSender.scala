/*
 * // Copyright (c) Microsoft. All rights reserved.
 */

package com.microsoft.azure.iot.kafka.connect.sink.testhelpers


import java.util
import java.util.concurrent.{CompletableFuture, ConcurrentLinkedQueue, ExecutorService, Executors}

import com.microsoft.azure.iot.kafka.connect.sink.MessageSender
import com.microsoft.azure.iot.service.sdk.Message

class MockMessageSender(connectionString: String) extends MessageSender {

  private val executor: ExecutorService = Executors.newFixedThreadPool(5)
  private var messageList: Option[ConcurrentLinkedQueue[Message]] = Some(new ConcurrentLinkedQueue[Message])

  override def sendMessage(deviceId: String, message: Message): CompletableFuture[Void] = {
    val messageListValue = messageList.get
    val future = CompletableFuture.runAsync(new Runnable {
      override def run(): Unit = {
        Thread.sleep(2000)
        messageListValue.add(message)
      }
    }, executor)
    future
  }

  override def close(): Unit = {
    // Set message list to None so that any calls to sendMessage after close will throw
    messageList = None
  }

  def getSentMessages(): util.Collection[Message] = messageList.get
}