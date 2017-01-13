/*
 * // Copyright (c) Microsoft. All rights reserved.
 */

package com.microsoft.azure.iot.kafka.connect.sink

import java.util.concurrent.CompletableFuture

import com.microsoft.azure.iot.service.sdk.Message

trait MessageSender {
  def sendMessage(deviceId: String, message: Message): CompletableFuture[Void]

  def close()
}
