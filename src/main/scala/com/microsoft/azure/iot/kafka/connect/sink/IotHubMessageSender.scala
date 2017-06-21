/*
 * // Copyright (c) Microsoft. All rights reserved.
 */

package com.microsoft.azure.iot.kafka.connect.sink

import com.microsoft.azure.sdk.iot.service.{IotHubServiceClientProtocol, Message, ServiceClient}

class IotHubMessageSender(connectionString: String) extends MessageSender {

  private val serviceClient = ServiceClient.createFromConnectionString(connectionString,
    IotHubServiceClientProtocol.AMQPS)
  this.serviceClient.open()

  def sendMessage(deviceId: String, message: Message): Unit = {
    this.serviceClient.send(deviceId, message)
  }

  def close(): Unit = {
    this.serviceClient.close()
  }
}
