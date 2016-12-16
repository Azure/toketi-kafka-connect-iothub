// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.kafka.connect.testhelpers

import java.time.Instant

import com.microsoft.azure.iot.kafka.connect.{DataReceiver, IotHubSourceTask}

class TestIotHubSourceTask extends IotHubSourceTask {
  override def getDataReceiver(connectionString: String, receiverConsumerGroup: String, partition: String,
                               partitionOffset: Option[String], partitionStartTime: Option[Instant]): DataReceiver = {
    new MockDataReceiver(connectionString, receiverConsumerGroup, partition, partitionOffset, partitionStartTime)
  }
}
