# Kafka Connect Sink Connector for Azure IoT Hub
________________________

The Sink connector allows you to send messages to Azure IoT devices by simply posting messages to Kafka topics. The
connector sends the messages to Azure IoT Hub, which in turn forwards them to the right devices. The
messages need to be in a specific format (details below), that allows the connector to extract the information
necessary to send them to the right devices.

> Note: At the moment, the Sink connector only supports C2D messages, and does not support other means of communication.
> See
> [Cloud-to-device communications guidance](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-c2d-guidance)
> to see what that means. Also, the connector does not support getting feedback from the devices on the status of the
> messages (whether accepted, rejected or expired). If you want to get feedback, you will need to do it manually.
> Please refer to the documentation [here](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-java-java-c2d) on
> how to do that.

### Configuration

All the configuration settings required to run Kafka sink connector for IoT Hub are in the
"[connect-iothub-sink.properties](connect-iothub-sink.properties)" file. The properties related to Azure IoT Hub
can be obtained from the [Azure Portal](https://portal.azure.com). For more information on how to get the IoT Hub
settings, please refer to the documentation
[here](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-java-java-getstarted) and
[here](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-java-java-c2d).

Here are the configurable values -

| Config | Value Type | Required | Default Value | Description |
|-------------|-------------|-------------|-------------|----------------|
| topics | String | Yes |  | The list of topics from which messages will be retrieved to send to IoT devices. |
| tasks.max | Int | Yes |  | Maximum number of tasks that should be created for this connector. |
| IotHub.ConnectionString | String | Yes |  | The IoT Hub connection string. In the Azure Portal, you can find the value under to "IoT Hub" >> your hub >> "Shared access policies" >> "service" >> "Connection string" |
| IotHub.MessageDeliveryAcknowledgement | String | No | None | The type of delivery acknowledgement you want for the messages sent to the devices. The valid values are None, Full, PositiveOnly and NegativeOnly.|

Here is a sample connect-iothub-sink.properties file. (Some of these values are not configurable, and hence omitted from
 the list above)

```
connector.class=com.microsoft.azure.iot.kafka.connect.sink.IotHubSinkConnector
name=AzureIotHubSinkConnector
tasks.max=1
topics=testtopic
IotHub.ConnectionString=HostName=Test.azure-devices.net;SharedAccessKeyName=service;SharedAccessKey=aBCdeBsdfwfTTs/isuwselskab1Jksjdsot=
IotHub.MessageDeliveryAcknowledgement=None
```

### Data format

The sink connector expects the messages in the Kafka topic to have the schema below, so that it can extract the
information required to send the message to the IoT device. If a record is not in the expected format, the connector
will throw an exception, and will have to be re-started manually.

```json
{
  "schema": {
    "type": "struct",
    "fields": [
      {
        "type": "string",
        "optional": false,
        "field": "deviceId"
      },
      {
        "type": "string",
        "optional": false,
        "field": "message"
      },
      {
        "type": "string",
        "optional": false,
        "field": "messageId"
      },
      {
        "type": "string",
        "optional": true,
        "field": "expiry"
      }
    ],
    "optional": false,
    "name": "iothub.kafka.connect.cloud2device.message",
    "version": 1
  }
}
```

Here are the details of each field above -

| Field | Info |
|-------|------|
| deviceId | The ID of the device to which you want to send the message |
| message | The actual message to be sent to the device |
| messageId | The ID of the message that is sent. This ID can be matched with the ID on the feedback channel to get the status of the message sent. |
| expiry | The time before which the message should be sent. If not, the message is considered expired and discarded by IoT Hub. This value should be in UTC and in the format YYYY-MM-DDThh:mm:ssZ |

The messages in Kafka topic can be Avro Records, or JSON serialized strings. See below on how to insert objects of
these types in Kafka topics.

### Building and running

Kafka Connect is a generic tool to copy data between Kafka and other systems (like Azure IoT Hub). To copy data from
Azure IoT Hub to Kafka, you need to make the code from this repository available to Kafka Connect and configure it to
use the right Azure IoT Hub and Kafka settings. For more details on using Kafka Connect, you can refer to the
[Kafka Connect user guide](http://docs.confluent.io/3.2.2/connect/userguide.html).

#### Pre-requisites

* You need to have an Azure IoT Hub and a set of devices to which you want to send messages. Get started
[here](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-java-java-getstarted).

* You also need to have Apache Kafka v0.10 installation running, that contains messages to be sent to the IoT devices in
 one or more topics. Get started with Kafka [here](http://docs.confluent.io/3.2.2/quickstart.html).

#### Steps

Here are the steps to run the Kafka Connect IoT Hub Sink Connector in
[standalone mode](http://docs.confluent.io/3.2.2/connect/userguide.html#standalone-worker-configuration). For
[distributed mode](http://docs.confluent.io/3.2.2/connect/userguide.html#distributed-worker-configuration), the
connector configuration will stay the same.

The steps to insert messages in Kafka topics and to run the Sink connector depend on whether you are using the
[Schema Registry](http://docs.confluent.io/3.2.2/schema-registry/docs/) along with the Confluent platform. So please
follow the right set of steps below depending on your choice.

##### Steps when using Schema Registry

When using [Schema Registry](http://docs.confluent.io/3.2.2/schema-registry/docs/) along with the
[Confluent Platform](http://docs.confluent.io/3.2.2/platform.html), messages are inserted in Kafka topic as Avro records
 which include the schema of the messages.

 1. Build the source code after cloning this repository using the following command, which will generate a jar file with
 all the dependencies embedded in it.
 ```
 sbt assembly
 ```
 Alternatively, you can directly download the jar file for Kafka Connect IoT Hub from
 [here](https://github.com/Azure/toketi-kafka-connect-iothub/releases/download/v0.6/kafka-connect-iothub-assembly_2.11-0.6.jar).

 2. Binplace the jar file in the Kafka installation libs folder (usually under KAFKA_HOME/libs).

 3. Update the config file "[connect-iothub-sink.properties](connect-iothub-sink.properties)" with the appropriate
 values as described in the section above. Binplace the file "connect-iothub-sink.properties" in the Kafka
 installation config folder (usually under KAFKA_HOME/etc).

 4. Make the following updates to the Kafka Connect configuration file (typically "etc/kafka/connect-standalone.properties)" -
   * Update bootstrap.servers to point to the Kafka server.
   * Add the following setting to the file. This will make sure that the Kafka sink connector handles only 10 records
     at a time, preventing Kafka connect from timing out the operation.
 ```
 consumer.max.poll.records=10
 ```

 5. Make sure Kafka server, Zookeeper, and Schema Registry are running, as described
 [here](http://docs.confluent.io/3.2.2/quickstart.html)

 6. Start Kafka source connector in
 [standalone mode](http://docs.confluent.io/3.2.2/connect/userguide.html#standalone-worker-configuration) to read
 messages from Kafka topic and send them to the IoT Devices -

 ```
 bin/connect-standalone.sh config/connect-standalone.properties config/connect-iothub-sink.properties
 ```

7. Insert messages to be sent to the IoT devices in the Kafka topic as Avro records. One way you can do that is using a
[KafkaProducer](http://docs.confluent.io/3.2.2/clients/producer.html). Here is some sample code to send such messages to
 a Kafka topic in the right format.

```java
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("acks", "all")
    props.put("retries", "0")
    props.put("key.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer")
    props.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer")
    props.put("schema.registry.url", "http://localhost:8081")

    val schemaString = "{\"namespace\": \"iothub.kafka.connect.cloud2device\", " +
      "\"type\": \"record\", " +
      "\"name\": \"message\"," +
      "\"fields\": [" +
      "{\"name\": \"deviceId\", \"type\": \"string\"}," +
      "{\"name\": \"message\", \"type\": \"string\"}," +
      "{\"name\": \"messageId\", \"type\": \"string\"}" +
      "]}";
    val producer = new KafkaProducer[String, GenericRecord](props)

    val schema = new Schema.Parser().parse(schemaString)
    val rec = new GenericData.Record(schema)
    rec.put("deviceId", "device1-123456")
    rec.put("message", "Cloud to device message")
    rec.put("messageId", "message1")
    val producerRecord = new ProducerRecord[String, GenericRecord](topic, "device1-123456", rec)
    producer.send(producerRecord)
```

##### Steps when not using Schema Registry

If you are using the standard Apache Kakfa without the Schema Registry integration, messages are inserted in Kafka
topics as JSON strings, which are then deserialized by the Sink connector.

1. Build the source code after cloning this repository using the following command, which will generate a jar file with
all the dependencies embedded in it.
```
sbt assembly
```
Alternatively, you can directly download the jar file for Kafka Connect IoT Hub from
[here](https://github.com/Azure/toketi-kafka-connect-iothub/releases/download/v0.6/kafka-connect-iothub-assembly_2.11-0.6.jar).

2. Binplace the jar file in the Kafka installation libs folder (usually under KAFKA_HOME/libs).

3. Update the config file "[connect-iothub-sink.properties](connect-iothub-sink.properties)" with the appropriate
values as described in the section above. Binplace the file "connect-iothub-sink.properties" in the Kafka
installation config folder (usually under KAFKA_HOME/config).

4. Make the following updates to the Kafka Connect configuration file (typically "config/connect-standalone.properties") -
  * Update bootstrap.servers to point to the Kafka server.
  * Update key.converter and value.converter to use org.apache.kafka.connect.storage.StringConverter (instead of the
    default org.apache.kafka.connect.json.JsonConverter)
  * Add the following setting to the file. This will make sure that the Kafka sink connector handles only 10 records
    at a time, preventing Kafka connect from timing out the operation.
```
consumer.max.poll.records=10
```

5. Make sure Kafka server and Zookeeper are running, as described [here](https://kafka.apache.org/documentation#quickstart)

6. Start Kafka source connector in
[standalone mode](http://docs.confluent.io/3.2.2/connect/userguide.html#standalone-worker-configuration) to read
messages from Kafka topic and send them to the IoT Devices -

```
bin/connect-standalone.sh config/connect-standalone.properties config/connect-iothub-sink.properties
```

7. Insert messages to be sent to the IoT devices in the Kafka topic as JSON strings. One way you can do that is using a
[KafkaProducer](https://kafka.apache.org/documentation/#producerapi). Here is some sample code to send such messages to
 a Kafka topic in the right format.

```java
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("acks", "all")
    props.put("retries", "0")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](this.props)

    val message =
      """{"messageId":"msg1","message":"Turn On","deviceId":"device1-123456", "expiryTime":"2017-01-19T19:25:50Z"}"""
    val producerRecord = new ProducerRecord[String, GenericRecord](topic, "device1-123456", message)
    producer.send(producerRecord)
```

## Future work

* Add support to get feedback on the messages sent to the Azure IoT Devices.
* Add support for other means of communication from cloud-to-device (for e.g. direct methods)
