/*
 * // Copyright (c) Microsoft. All rights reserved.
 */

package com.microsoft.azure.iot.kafka.connect.sink

import java.util

import com.microsoft.azure.iot.kafka.connect.source.JsonSerialization
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.common.config.{ConfigDef, ConfigException}
import org.apache.kafka.connect.connector.Task
import org.apache.kafka.connect.errors.ConnectException
import org.apache.kafka.connect.sink.SinkConnector

import scala.collection.JavaConverters._

class IotHubSinkConnector extends SinkConnector with LazyLogging with JsonSerialization {

  private[this] var props: Map[String, String] = _

  override def taskClass(): Class[_ <: Task] = classOf[IotHubSinkTask]

  override def taskConfigs(maxTasks: Int): util.List[util.Map[String, String]] = {
    (1 to maxTasks).map(_ => this.props.asJava).toList.asJava
  }

  override def stop(): Unit = {
    logger.info("Stopping IotHubSinkConnector")
  }

  override def config(): ConfigDef = IotHubSinkConfig.configDef

  override def start(props: util.Map[String, String]): Unit = {

    logger.info("Starting IotHubSinkConnector")

    try {
      val iotHubSinkConfig = IotHubSinkConfig.getConfig(props)
      this.props = Map[String, String](
        IotHubSinkConfig.IotHubConnectionString -> iotHubSinkConfig.getString(IotHubSinkConfig.IotHubConnectionString),
        IotHubSinkConfig.IotHubMessageDeliveryAcknowledgement →
          iotHubSinkConfig.getString(IotHubSinkConfig.IotHubMessageDeliveryAcknowledgement)
      )
    } catch {
      case ex: ConfigException ⇒ throw new ConnectException("Could not start IotHubSinkConnector due to a " +
        "configuration exception", ex)
    }
  }

  override def version(): String = getClass.getPackage.getImplementationVersion
}