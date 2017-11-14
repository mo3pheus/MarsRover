/**
 *
 */
package space.exploration.mars.rover.communication;

import com.google.protobuf.InvalidProtocolBufferException;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author sanketkorgaonkar
 */
public class Receiver extends Thread {
    public final static String TUNED_CHANNEL = "earth_to_curiosity_4";

    private Logger            logger            = LoggerFactory.getLogger(Receiver.class);
    private ConsumerConnector consumerConnector = null;
    private Radio             radio             = null;
    private long              lastReportTime    = 0l;
    private long              radioCheckPulse   = 0l;

    public Receiver(Properties comsConfig, Radio radio) {
        ConsumerConfig consumerConfig = new ConsumerConfig(comsConfig);
        consumerConnector = Consumer.createJavaConsumerConnector(consumerConfig);
        this.radio = radio;
        this.lastReportTime = System.currentTimeMillis();
    }

    public void setRadioCheckPulse(long radioCheckPulse) {
        this.radioCheckPulse = radioCheckPulse;
    }

    @Override
    public void run() {
        System.out.println("Rover Listening ...");
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(TUNED_CHANNEL, new Integer(1));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumerConnector
                .createMessageStreams(topicCountMap);
        KafkaStream<byte[], byte[]>      stream = consumerMap.get(TUNED_CHANNEL).get(0);
        ConsumerIterator<byte[], byte[]> it     = stream.iterator();

        while (it.hasNext()) {
            long timeElapsed = System.currentTimeMillis() - this.lastReportTime;
            logger.info("Time Elapsed since last message = " + timeElapsed);
            if (timeElapsed > this.radioCheckPulse) {
                this.lastReportTime = System.currentTimeMillis();
            }

            try {
                InstructionPayloadOuterClass.InstructionPayload received = (InstructionPayloadOuterClass
                        .InstructionPayload.parseFrom(it.next().message()));
                radio.receiveMessage(received);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }
}
