# Kafka Connect Azure IoT Hub
________________________

Kafka Connect Azure IoT Hub consists of 2 connectors - a source connector and a sink connector. The source connector
is used to pump data from [Azure IoT Hub](https://azure.microsoft.com/en-us/services/iot-hub/) to
[Apache Kafka](https://kafka.apache.org/), whereas the sink connector reads messages from Kafka and sends them to IoT
 devices via [Azure IoT Hub](https://azure.microsoft.com/en-us/services/iot-hub/). When used in tandem, the 2
 connectors allow communicating with IoT devices by simply posting and reading messages to/from Kafka topics. This
 should
 make it easier for open source systems and other systems that already interface with Kafka to communicate with
 Azure IoT devices.

For more information on the capabilities of the connectors and how to use them, please refer to the links below -

### [Source Connector](README_Source.md)

### [Sink Connector](README_Sink.md)


## Contribute Code

If you want to contribute, we ask you to sign a [CLA](https://cla.microsoft.com/)(Contribution License Agreement). A
friendly bot will remind you about it when you submit a pull-request.

If you are sending a pull request, we kindly request to check the code style with IntelliJ IDEA, importing the settings
from `Codestyle.IntelliJ.xml`.
