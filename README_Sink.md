# Kafka Connect Sink Connector for Azure IoT Hub
________________________

The Sink connector allows you to send messages to Azure IoT devices by simply posting messages to Kafka topics. The
connector sends the messages to Azure IoT Hub, which in turn forwards them to the right devices. The
messages need to be in a specific format (details below), that allow the connector to extract the information
necessary to send them to the right devices.

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
IotHub.ConnectionString=HostName=Test.azure-devices.net;SharedAccessKeyName=service;SharedAccessKey=aBCdeBb5HffTTs/J9ikdcqab1JNMB0ot=
IotHub.MessageDeliveryAcknowledgement=None
```

> Note: At the moment, the Sink connector does not support getting feedback from the devices on the status of the
> messages (whether accepted or rejected). If you want to get feedback, you will need to do it manually. Please refer
> to the documentation [here](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-java-java-c2d) on how to do that.


### Data format

The sink connector expects the messages in the Kafka topic to have the schema below, so that it can extract the
information required to send the message to the IoT device. If a record is not in the expected format, the connector
will throw an exception.

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

### Building and running

#### Pre-requisites

* You need to have an Azure IoT Hub and a set of devices to which you want to send messages. Get started
[here](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-java-java-getstarted).

* You also need to have Apache Kafka v0.10 installation running, that contains messages to be sent to the IoT devices in
 one or more topics. Get started with Kafka [here](https://kafka.apache.org/documentation#quickstart).

* To insert the messages for the IoT devices in the Kafka topic, you can use a KafkaProducer. You can read more on
how to use the KafkaProducer [here](http://docs.confluent.io/3.0.0/clients/producer.html). Here is a sample code to
send such messages to a Kafka topic in the right format.

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

#### Steps

Kafka Connect is a generic tool to copy data between Kafka and other systems (like Azure IoT Hub). To copy data from
Azure IoT Hub to Kafka, you need to make the code from this repository available to Kafka Connect and configure it to
use the right Azure IoT Hub and Kafka settings. For more details on using Kafka Connect, you can refer to the
[Kafka Connect user guide](http://docs.confluent.io/3.0.0/connect/userguide.html).

1. Build the source code after cloning this repository using the following command, which will generate a jar file with
all the dependencies embedded in it.
```
sbt assembly
```
Alternatively, you can directly download the jar file for Kafka Connect IoT Hub from
[here](https://github.com/Azure/toketi-kafka-connect-iothub/releases/download/v0.5/kafka-connect-iothub-assembly_2.11-0.5.jar).

2. Binplace the jar file in the Kafka installation libs folder (usually under KAFKA_HOME/libs).

3. Update the config file "[connect-iothub-source.properties](connect-iothub-source.properties)" with the appropriate
values as described in the section above. Binplace the file "connect-iothub-source.properties" in the Kafka
installation config folder (usually under KAFKA_HOME/config).

4. Update the Kafka Connect configuration file ("config/connect-standalone.properties" or
"config/connect-distributed.properties") to point to the Kafka bootstrap servers.

5. After binplacing the connector jar and starting Kafka and Zookeeper, update the config file "[connect-iothub-sink
.properties](connect-iothub-sink.properties)" with the appropriate values as described in the section
above. Binplace the file "connect-iothub-sink.properties" in the Kafka installation config folder (usually under
KAFKA_HOME/config).

6. Start Kafka source connector in
[standalone mode](http://docs.confluent.io/3.0.0/connect/userguide.html#standalone-worker-configuration) to read
messages from Kafka topic and send them to the IoT Devices -

```
bin/connect-standalone.sh config/connect-standalone.properties config/connect-iothub-source.properties
```
For distributed mode, the connector configuration will stay the same. For the detailed steps on how to do this, please
follow the
[Confluent Kafka Connect documentation](http://docs.confluent.io/3.0.0/connect/userguide.html#distributed-worker-configuration)
on this topic.


## Future work

* Add support to get feedback on the messages sent to the Azure IoT Devices. 
