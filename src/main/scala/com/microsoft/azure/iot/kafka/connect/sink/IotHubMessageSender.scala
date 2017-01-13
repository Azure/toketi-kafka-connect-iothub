/*
 * // Copyright (c) Microsoft. All rights reserved.
 */

package com.microsoft.azure.iot.kafka.connect.sink

import java.util.concurrent.CompletableFuture

import com.microsoft.azure.iot.service.sdk.{IotHubServiceClientProtocol, Message, ServiceClient}

class IotHubMessageSender(connectionString: String) extends MessageSender {

  private val serviceClient = ServiceClient.createFromConnectionString(connectionString,
    IotHubServiceClientProtocol.AMQPS)
  this.serviceClient.open()

  def sendMessage(deviceId: String, message: Message): CompletableFuture[Void] = {
    this.serviceClient.sendAsync(deviceId, message)
  }

  def close(): Unit = {
    this.serviceClient.closeAsync()
  }
}
