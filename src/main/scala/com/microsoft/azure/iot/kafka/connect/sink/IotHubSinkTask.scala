/*
 * // Copyright (c) Microsoft. All rights reserved.
 */

package com.microsoft.azure.iot.kafka.connect.sink

import java.util

import com.microsoft.azure.iot.kafka.connect.source.JsonSerialization
import com.microsoft.azure.sdk.iot.service.{DeliveryAcknowledgement, Message}
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.connect.sink.{SinkRecord, SinkTask}

import scala.collection.JavaConverters._

class IotHubSinkTask extends SinkTask with LazyLogging with JsonSerialization {

  // Protected for testing purposes
  protected     var messageSender     : Option[MessageSender]                = None
  protected     var acknowledgement   : DeliveryAcknowledgement              = DeliveryAcknowledgement.None
  private[this] var isClosing         : Boolean                              = false

  override def stop(): Unit = {
    logger.info("Stopping IotHubSink Task")
    if (this.messageSender.isDefined && !this.isClosing) {
      this.messageSender.synchronized {
        if (!this.isClosing) {
          this.isClosing = true
          logger.info("Closing IotHub clients")
          this.messageSender.get.close()
        }
      }
    }
  }

  override def put(records: util.Collection[SinkRecord]): Unit = {
    if (this.messageSender.isDefined && !this.isClosing) {
      this.messageSender.synchronized {
        if (!this.isClosing) {
          logger.info(s"Received ${records.size()} messages to be sent to devices. ")
          for (record: SinkRecord ← records.asScala) {
            val c2DMessage = C2DMessageConverter.validateSchemaAndGetMessage(record)
            this.sendMessage(c2DMessage)
          }
          logger.info(s"Started tasks to send ${records.size()} messages to devices.")
        }
      }
    } else {
      logger.info(s"Unable to send messages to devices - MessageSender is undefined " +
        s"= ${messageSender.isEmpty.toString}, isClosing = ${this.isClosing.toString}")
    }
  }

  private def sendMessage(c2DMessage: C2DMessage): Unit = {
    logger.info(s"Sending c2d message ${c2DMessage.toString}")
    val message = new Message(c2DMessage.message)
    message.setMessageId(c2DMessage.messageId)
    message.setDeliveryAcknowledgement(acknowledgement)
    if (c2DMessage.expiryTime.isDefined) {
      message.setExpiryTimeUtc(c2DMessage.expiryTime.get)
    }
    this.messageSender.get.sendMessage(c2DMessage.deviceId, message)
  }

  override def flush(offsets: util.Map[TopicPartition, OffsetAndMetadata]): Unit = {}

  override def start(props: util.Map[String, String]): Unit = {
    logger.info("Starting IotHub Sink")
    val connectionString = props.get(IotHubSinkConfig.IotHubConnectionString)
    this.messageSender = Some(this.getMessageSender(connectionString))
    this.acknowledgement =
      DeliveryAcknowledgement.valueOf(props.get(IotHubSinkConfig.IotHubMessageDeliveryAcknowledgement))
  }

  protected def getMessageSender(connectionString: String): MessageSender = {
    new IotHubMessageSender(connectionString)
  }

  override def version(): String = getClass.getPackage.getImplementationVersion
}
