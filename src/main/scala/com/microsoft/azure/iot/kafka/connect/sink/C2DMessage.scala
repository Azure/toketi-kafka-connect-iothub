/*
 * // Copyright (c) Microsoft. All rights reserved.
 */

package com.microsoft.azure.iot.kafka.connect.sink

import java.util.Date

case class C2DMessage(messageId: String, message: String, deviceId: String, expiryTime: Option[Date])
