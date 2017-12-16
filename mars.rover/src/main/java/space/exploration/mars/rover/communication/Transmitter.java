/**
 *
 */
package space.exploration.mars.rover.communication;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * @author sanketkorgaonkar
 */
public class Transmitter {
    private Producer<String, byte[]> earthChannel;
    private Logger logger = LoggerFactory.getLogger(Transmitter.class);
    private Properties kafkaProperties;

    public Transmitter(Properties comsConfig) {
        kafkaProperties = comsConfig;
        kafkaProperties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        earthChannel = new KafkaProducer<>(kafkaProperties);
    }

    public void transmitMessage(byte[] message) {
        earthChannel.send(
                new ProducerRecord<>(kafkaProperties.getProperty("destination.topic"), message));
        logger.info("Sending information packet to " + kafkaProperties.getProperty("destination.topic"));
    }
}
