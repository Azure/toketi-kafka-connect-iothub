/*
 * // Copyright (c) Microsoft. All rights reserved.
 */

package com.microsoft.azure.iot.kafka.connect.sink

import java.util.Map

import com.microsoft.azure.sdk.iot.service.DeliveryAcknowledgement
import org.apache.kafka.common.config.ConfigDef.{Importance, Type, Width}
import org.apache.kafka.common.config.{AbstractConfig, ConfigDef}

object IotHubSinkConfig {

  val IotHubConnectionString    = "IotHub.ConnectionString"
  val IotHubMessageDeliveryAcknowledgement = "IotHub.MessageDeliveryAcknowledgement"

  private val IotHubConnectionStringDoc =
    """IoT Hub ConnectionString. (see "IoT Hub" >> your hub >> "Shared access policies" >> "service" >> """ +
      """"Connection string")"""
  private val IotHubMessageDeliveryAcknowledgementDoc = "The type of delivery acknowledgement for a C2D message. " +
    "Valid values are None, Full, NegativeOnly, PositiveOnly"
  private val iotConfigGroup = "Azure IoT Hub"
  private val validDeliveryAcknowledgementString = ConfigDef.ValidString.in(
    DeliveryAcknowledgement.None.toString,
    DeliveryAcknowledgement.Full.toString,
    DeliveryAcknowledgement.PositiveOnly.toString,
    DeliveryAcknowledgement.NegativeOnly.toString)

  lazy val configDef = new ConfigDef()
    .define(IotHubConnectionString, Type.STRING, Importance.HIGH, IotHubConnectionStringDoc, iotConfigGroup, 1,
      Width.MEDIUM, "IoT Hub Connection String")
    .define(IotHubMessageDeliveryAcknowledgement, Type.STRING, DeliveryAcknowledgement.None.toString,
      validDeliveryAcknowledgementString, Importance.HIGH, IotHubMessageDeliveryAcknowledgementDoc, iotConfigGroup, 1,
      Width.MEDIUM, "Delivery acknowledgement")

  def getConfig(configValues: Map[String, String]): IotHubSinkConfig = {
    new IotHubSinkConfig(configDef, configValues)
  }
}

class IotHubSinkConfig(configDef: ConfigDef, configValues: Map[String, String])
  extends AbstractConfig(configDef, configValues)

