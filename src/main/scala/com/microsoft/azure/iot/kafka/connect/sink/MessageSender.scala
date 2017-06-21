/*
 * // Copyright (c) Microsoft. All rights reserved.
 */

package com.microsoft.azure.iot.kafka.connect.sink

import com.microsoft.azure.sdk.iot.service.Message

trait MessageSender {
  def sendMessage(deviceId: String, message: Message)

  def close()
}
