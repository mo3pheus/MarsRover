/**
 *
 */
package space.exploration.mars.rover.communication;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author sanketkorgaonkar
 */
public class Transmitter {
    private Producer<String, byte[]> earthChannel;
    private InputStream              inputStream;
    private Properties               kafkaProperties;

    public Transmitter(KafkaShipmentBuilder kafkaShipmentBuilder) {
        /*
         * Set up Kafka producer
		 */
        try {
            System.out.println(kafkaShipmentBuilder.getPropertyFileLocation());
            inputStream = new FileInputStream(kafkaShipmentBuilder.getPropertyFileLocation());
            kafkaProperties = new Properties();
            kafkaProperties.load(inputStream);
            kafkaProperties.put("sourceTopic", kafkaShipmentBuilder.sourceTopic);
            kafkaProperties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            earthChannel = new KafkaProducer<>(kafkaProperties);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Transmitter(Properties comsConfig) {
        kafkaProperties = comsConfig;
        kafkaProperties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        earthChannel = new KafkaProducer<>(kafkaProperties);
    }

    public void transmitMessage(byte[] message) throws InterruptedException, InvalidProtocolBufferException {
        for (int i = 0; i < 1; i++) {
            earthChannel.send(
                    new ProducerRecord<>(kafkaProperties.getProperty("destination.topic"), message));
            System.out.println("Sending information packet to " + kafkaProperties.getProperty("destination" +
                                                                                                      ".topic"));
        }
    }

    public void cleanUp() throws IOException {
        this.inputStream.close();
        this.earthChannel.close();
    }

    public static class KafkaShipmentBuilder {
        private String sourceTopic;
        private String propertyFileLocation;

        public KafkaShipmentBuilder withPropertyFileAt(String fileLocation) {
            this.propertyFileLocation = fileLocation;
            return this;
        }

        public KafkaShipmentBuilder withSourceTopic(String sourceTopic) {
            this.sourceTopic = sourceTopic;
            return this;
        }

        public KafkaShipmentBuilder build() throws Exception {
            return this;
        }

        public String getPropertyFileLocation() {
            return propertyFileLocation;
        }
    }
}
