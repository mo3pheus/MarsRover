/**
 *
 */
package space.exploration.mars.rover.kernel;

import space.exploration.mars.rover.robot.RobotPositionsOuterClass.RobotPositions;

/**
 * @author sanketkorgaonkar
 */
public class RechargingState implements State {
    private Rover rover = null;

    public RechargingState(Rover rover) {
        this.rover = rover;
    }

    public void receiveMessage(byte[] message) {
        // TODO Auto-generated method stub

    }

    public void transmitMessage(byte[] message) {
        // TODO Auto-generated method stub

    }

    public void exploreArea() {
        // TODO Auto-generated method stub

    }

    public void activateCamera() {
        // TODO Auto-generated method stub

    }

    public void move(RobotPositions positions) {
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

    @Override
    public void performRadarScan() {

    }

}
