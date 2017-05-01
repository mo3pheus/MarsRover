package space.exploration.mars.rover.communication;

import java.util.Properties;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;

import scala.concurrent.forkjoin.ThreadLocalRandom;
import space.exploration.mars.rover.InstructionPayloadOuterClass.InstructionPayload;
import space.exploration.mars.rover.kernel.Rover;

/**
 * Created by sanketkorgaonkar on 4/27/17.
 */
public class Radio {
	private Rover		rover		= null;
	private Transmitter	transmitter	= null;
	private Receiver	receiver	= null;
	private Logger		logger		= LoggerFactory.getLogger(Radio.class);

	public Radio(Properties comsConfig, Rover rover) {
		this.transmitter = new Transmitter(comsConfig);
		this.rover = rover;
		this.receiver = new Receiver(comsConfig, this);
		receiver.start();
	}

	public void receiveMessage(InstructionPayload instructionPayload) {
		try {
			Thread.sleep(getComsDelaySecs());
			rover.receiveMessage(instructionPayload.toByteArray());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public void sendMessage(byte[] message) {
		try {
			Thread.sleep(getComsDelaySecs());
			transmitter.transmitMessage(message);
		} catch (InvalidProtocolBufferException e) {
			logger.error("InvalidProtocolBufferException error - common guys send me a good message!");
			logger.error(e.getMessage());
		} catch (InterruptedException e) {
			logger.error("InterruptedException");
			logger.error(e.getMessage());
		}
	}

	private int getComsDelaySecs() {
		return ThreadLocalRandom.current().nextInt(3, 22);
	}
}
