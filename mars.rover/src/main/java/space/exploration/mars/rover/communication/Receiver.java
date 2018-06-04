/**
 *
 */
package space.exploration.mars.rover.communication;

import com.google.protobuf.InvalidProtocolBufferException;
import encryption.EncryptionUtil;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;
import space.exploration.communications.protocol.security.SecureMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author sanketkorgaonkar
 */
public class Receiver extends Thread {
    public final static String CHANNEL_PROPERTY = "source.topic";

    private          Logger            logger            = LoggerFactory.getLogger(Receiver.class);
    private          ConsumerConnector consumerConnector = null;
    private          Radio             radio             = null;
    private          long              lastReportTime    = 0l;
    private          long              radioCheckPulse   = 0l;
    private          String            tunedChannel      = "";
    private volatile boolean           runThread         = true;

    public Receiver(Properties comsConfig, Radio radio) {
        super("radio");
        ConsumerConfig consumerConfig = new ConsumerConfig(comsConfig);
        consumerConnector = Consumer.createJavaConsumerConnector(consumerConfig);
        this.radio = radio;
        this.lastReportTime = System.currentTimeMillis();
        this.tunedChannel = comsConfig.getProperty(CHANNEL_PROPERTY);
    }

    public void setRadioCheckPulse(long radioCheckPulse) {
        this.radioCheckPulse = radioCheckPulse;
    }

    @Override
    public void run() {
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(tunedChannel, new Integer(1));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumerConnector
                .createMessageStreams(topicCountMap);
        KafkaStream<byte[], byte[]>      stream = consumerMap.get(tunedChannel).get(0);
        ConsumerIterator<byte[], byte[]> it     = stream.iterator();

        while (it.hasNext() && runThread) {
            long timeElapsed = System.currentTimeMillis() - this.lastReportTime;
            logger.info("Time Elapsed since last message = " + timeElapsed);
            if (timeElapsed > this.radioCheckPulse) {
                this.lastReportTime = System.currentTimeMillis();
            }

            try {
                SecureMessage.SecureMessagePacket received = (SecureMessage.SecureMessagePacket.parseFrom(it.next().message()));
//                InstructionPayloadOuterClass.InstructionPayload received = (InstructionPayloadOuterClass
//                        .InstructionPayload.parseFrom(it.next().message()));
                radio.receiveMessage(received);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        logger.info("Radio receiver stopped.");
    }

    public void stopReceiver() {
        runThread = false;
    }
}
