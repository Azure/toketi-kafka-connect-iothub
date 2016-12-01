// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.kafka.connect

case class IotMessage(data: IotMessageData, deviceId: String, offset: String)

case class IotMessageData(content: String, systemProperties: Map[String, String], properties: Map[String, String])
