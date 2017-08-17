/**
 *
 */
package space.exploration.mars.rover.kernel;

import space.exploration.mars.rover.InstructionPayloadOuterClass;
import space.exploration.mars.rover.robot.RobotPositionsOuterClass.RobotPositions;

/**
 * @author sanketkorgaonkar
 */
public class TransmittingState implements State {
    private Rover rover = null;

    public TransmittingState(Rover rover) {
        this.rover = rover;
    }

    public void receiveMessage(byte[] message) {
        rover.getInstructionQueue().add(message);
    }

    public void transmitMessage(byte[] message) {
        rover.getRadio().sendMessage(message);
        rover.state = rover.listeningState;
    }

    @Override
    public String getStateName() {
        return "Transmitting State";
    }

    public void exploreArea() {
        // TODO Auto-generated method stub

    }

    public void activateCamera() {
        // TODO Auto-generated method stub

    }

    public void move(InstructionPayloadOuterClass.InstructionPayload payload) {
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

    public void performRadarScan() {

    }

}
