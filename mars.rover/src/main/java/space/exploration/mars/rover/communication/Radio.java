package space.exploration.mars.rover.communication;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;

import space.exploration.mars.rover.InstructionPayloadOuterClass.InstructionPayload;

/**
 * Created by sanketkorgaonkar on 4/27/17.
 */
public class Radio {
	private Transmitter	transmitter	= null;
	private Receiver	receiver	= null;
	private Logger		logger		= LoggerFactory.getLogger(Radio.class);

	public Radio(Properties comsConfig) {
		this.transmitter = new Transmitter(comsConfig);
		this.receiver = new Receiver(comsConfig, this);
		receiver.start();
	}

	public byte[] receiveMessage(InstructionPayload instructionPayload) {
		return instructionPayload.toByteArray();
	}

	public void sendMessage(byte[] message) {
		try {
			transmitter.transmitMessage(message);
		} catch (InvalidProtocolBufferException e) {
			logger.error("InvalidProtocolBufferException error - common guys send me a good message!");
			logger.error(e.getMessage());
		} catch (InterruptedException e) {
			logger.error("InterruptedException");
			logger.error(e.getMessage());
		}
	}
}
