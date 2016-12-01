// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.kafka.connect

trait DataReceiver {
  def receiveData(batchSize: Int): Iterable[IotMessage]

  def close()
}
