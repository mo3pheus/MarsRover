/**
 * 
 */
package space.exploration.mars.rover.communication;

import java.awt.Point;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import space.exploration.mars.rover.communication.MatrixCommunication.MatrixCall;
import space.exploration.mars.rover.environment.PortableMatrixConfig;
import space.exploration.mars.rover.navigation.NavigationEngine;
import space.exploration.mars.rover.navigation.NavigationPath;

/**
 * @author sanketkorgaonkar
 *
 */
public class Transmitter {
	private Producer<String, byte[]>	earthChannel;
	private InputStream					inputStream;
	private Properties					kafkaProperties;

	private List<byte[]> generatePathSplits() {
		PortableMatrixConfig matrixConfig = new PortableMatrixConfig();
		NavigationEngine navEngine = new NavigationEngine(matrixConfig.getMatrixConfig());
		List<Point> fullPath = navEngine.getAnimationCalibratedRobotPath();
		List<byte[]> messageSequence = new ArrayList<byte[]>();
		List<NavigationPath> navSequence = new ArrayList<NavigationPath>();

		List<Point> tempPoints = new ArrayList<Point>();
		for (int i = 0; i < fullPath.size(); i++) {
			if ((i % 10 == 0) || (i % 10 < 100)) {
				NavigationPath navPath = new NavigationPath(tempPoints);
				navSequence.add(navPath);
				tempPoints = new ArrayList<Point>();
			}
			tempPoints.add(fullPath.get(i));
		}

		for (int i = 0; i < navSequence.size(); i++) {
			MatrixCall.Builder matrixCallMaker = MatrixCall.newBuilder();
			MatrixCall call = matrixCallMaker.setCommandCenter("zion.main.frame").setMessageType(2)
					.setPath(ByteString.copyFrom(SerializationUtil.serialize(navSequence.get(i)))).build();
			messageSequence.add(call.toByteArray());
		}

		return messageSequence;
	}

	private static byte[] makeCall() {
		PortableMatrixConfig matrixConfig = new PortableMatrixConfig();
		MatrixCall.Builder matrixCallMaker = MatrixCall.newBuilder();
		MatrixCall call = matrixCallMaker.setCommandCenter("zion.main.frame").setMessageType(1)
				.setMatrixConfig(ByteString.copyFrom(SerializationUtil.serialize(matrixConfig))).build();
		return call.toByteArray();
	}

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
			earthChannel = new Producer<String, byte[]>(new ProducerConfig(kafkaProperties));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Transmitter(Properties comsConfig) {
		kafkaProperties = comsConfig;
		earthChannel = new Producer<String, byte[]>(new ProducerConfig(kafkaProperties));
	}

	public void sendMessage() throws InterruptedException, InvalidProtocolBufferException {
		for (int i = 0; i < 1; i++) {
			byte[] outputKafkaBytes = makeCall();
			earthChannel.send(
					new KeyedMessage<String, byte[]>(kafkaProperties.getProperty("sourceTopic"), outputKafkaBytes));
			System.out.println(" Sending canned interrupt messages to " + kafkaProperties.getProperty("sourceTopic"));
			System.out.println(" Sent message = " + MatrixCall.parseFrom(outputKafkaBytes));
			Thread.sleep(1500l);
		}
	}
	
	public void transmitMessage(byte[] message) throws InterruptedException, InvalidProtocolBufferException {
		for (int i = 0; i < 1; i++) {
			earthChannel.send(
					new KeyedMessage<String, byte[]>(kafkaProperties.getProperty("destination.topic"), message));
			System.out.println(" Sending canned interrupt messages to " + kafkaProperties.getProperty("destination.topic"));
			Thread.sleep(1500l);
		}
	}

	public void sendMessages() throws Exception {
		List<byte[]> messages = generatePathSplits();
		for (int i = 0; i < messages.size(); i++) {
			byte[] outputKafkaBytes = messages.get(i);
			earthChannel.send(
					new KeyedMessage<String, byte[]>(kafkaProperties.getProperty("sourceTopic"), outputKafkaBytes));
			System.out.println(" Sending canned interrupt messages to " + kafkaProperties.getProperty("sourceTopic"));
			System.out.println(" Sent message = " + MatrixCall.parseFrom(outputKafkaBytes));
			// Thread.sleep(20l);
		}
	}

	public void cleanUp() throws IOException {
		this.inputStream.close();
		this.earthChannel.close();
	}

	public static class KafkaShipmentBuilder {
		private String	sourceTopic;
		private String	propertyFileLocation;

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
