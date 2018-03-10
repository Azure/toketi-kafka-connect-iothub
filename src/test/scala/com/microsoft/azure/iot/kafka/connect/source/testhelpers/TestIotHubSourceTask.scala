// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.kafka.connect.source.testhelpers

import java.time.{Duration, Instant}

import com.microsoft.azure.iot.kafka.connect.source.{DataReceiver, IotHubSourceTask}

class TestIotHubSourceTask extends IotHubSourceTask {
  override def getDataReceiver(connectionString: String, receiverConsumerGroup: String, partition: String,
                               partitionOffset: Option[String], partitionStartTime: Option[Instant],
                               receiveTimeout: Duration): DataReceiver = {
    new MockDataReceiver(connectionString, receiverConsumerGroup, partition, partitionOffset, partitionStartTime,
      receiveTimeout)
  }
}
