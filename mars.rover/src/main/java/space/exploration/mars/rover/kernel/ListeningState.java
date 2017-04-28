package space.exploration.mars.rover.kernel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;

import space.exploration.mars.rover.InstructionPayloadOuterClass.InstructionPayload;

public class ListeningState implements State {

	private Logger	logger	= LoggerFactory.getLogger(ListeningState.class);
	private Rover	rover	= null;

	public ListeningState(Rover rover) {
		this.rover = rover;
	}

	public void receiveMessage(byte[] message) {
		InstructionPayload payload = null;
		try {
			System.out.println("This is the listening module");
			payload = InstructionPayload.parseFrom(message);
			System.out.println(payload);
			logger.info(payload.toString());
			
			rover.setStatus("Sending a message to earth!");
		} catch (InvalidProtocolBufferException e) {
			logger.error("InvalidProtocolBufferException");
			logger.error(e.getMessage());
		}
	}

	public void transmitMessage(byte[] message) {
		logger.error("Can not transmit message while in the listening state");
	}

	public void exploreArea() {
		// TODO Auto-generated method stub

	}

	public void performExperiments() {
		// TODO Auto-generated method stub

	}

	public void move() {
		// TODO Auto-generated method stub

	}

	public void hibernate() {
		// TODO Auto-generated method stub

	}

	public void rechargeBattery() {
		// TODO Auto-generated method stub

	}

	public void scanSurroundings() {
		// TODO Auto-generated method stub

	}

	public void performDiagnostics() {
		// TODO Auto-generated method stub

	}

}
