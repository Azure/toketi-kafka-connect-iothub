// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.kafka.connect.source

import org.json4s.DefaultFormats

trait JsonSerialization {
  implicit val formats = DefaultFormats
}
