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
import space.exploration.mars.rover.InstructionPayloadOuterClass.InstructionPayload;
import space.exploration.mars.rover.bootstrap.MatrixCreation;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author sanketkorgaonkar
 */
public class Receiver extends Thread {
    public final static int    RECEIVER_POWER_USAGE = 20;
    public final static String clientId             = "Curiosity";
    public final static String TUNED_CHANNEL        = "secure_com_from_earth_channel_3";

    private ConsumerConnector consumerConnector = null;
    private Radio             radio             = null;
    private long              lastReportTime    = 0l;
    private long              radioCheckPulse   = 0l;

    @Deprecated
    public Receiver() throws Exception {
        Properties properties = new Properties();
        properties.put("zookeeper.connect", "localhost:2181");
        properties.put("group.id", "nasa.command.earth.center");
        ConsumerConfig consumerConfig = new ConsumerConfig(properties);
        consumerConnector = Consumer.createJavaConsumerConnector(consumerConfig);
        MatrixCreation.configureLogging();
        Properties matrixConfig = new Properties();
        FileInputStream propFile = new FileInputStream(
                "/Users/sanketkorgaonkar/Documents/CodeRepos/AdvancedMatrix/advanced" +
                ".matrix/src/main/resources/mazeDefinition.properties");
        matrixConfig.load(propFile);
        this.lastReportTime = System.currentTimeMillis();
    }

    public Receiver(Properties comsConfig, Radio radio) {
        ConsumerConfig consumerConfig = new ConsumerConfig(comsConfig);
        consumerConnector = Consumer.createJavaConsumerConnector(consumerConfig);
        this.radio = radio;
        this.lastReportTime = System.currentTimeMillis();
    }

    public long getRadioCheckPulse() {
        return radioCheckPulse;
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
            System.out.println("Time Elapsed since last message = " + timeElapsed);
            if (timeElapsed > this.radioCheckPulse) {
                radio.reportPowerUsage(RECEIVER_POWER_USAGE);
                this.lastReportTime = System.currentTimeMillis();
            }

            try {
                InstructionPayload received = (InstructionPayload.parseFrom(it.next().message()));
                radio.receiveMessage(received);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }
}
