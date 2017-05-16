/**
 *
 */
package space.exploration.mars.rover.kernel;

import space.exploration.mars.rover.communication.RoverStatusOuterClass.RoverStatus;
import space.exploration.mars.rover.communication.RoverStatusOuterClass.RoverStatus.Location;
import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.robot.RobotPositionsOuterClass.RobotPositions;

/**
 * @author sanketkorgaonkar
 */
public class ExploringState implements State {

    private Rover rover = null;

    public ExploringState(Rover rover) {
        this.rover = rover;
    }

    public void receiveMessage(byte[] message) {
        rover.getInstructionQueue().add(message);
    }

    public void transmitMessage(byte[] message) {
    }

    public void exploreArea() {
        MarsArchitect marsArchitect = rover.getMarsArchitect();
        Cell          robot         = marsArchitect.getRobot();
        rover.configureSpectrometer(robot.getLocation());
        rover.getSpectrometer().setCellWidth(marsArchitect.getCellWidth());
        rover.getSpectrometer().setSurfaceComp(marsArchitect.getSoilCompositionMap());
        rover.getSpectrometer().processSurroundingArea();

        marsArchitect.setSpectrometerAnimationEngine(rover.getSpectrometer());
        marsArchitect.getSpectrometerAnimationEngine().activateSpectrometer();
        marsArchitect.returnSurfaceToNormal();

        Location.Builder lBuilder = Location.newBuilder().setX(robot.getLocation().x).setY(robot.getLocation().y);

        RoverStatus.Builder rBuilder = RoverStatus.newBuilder();
        RoverStatus status = rBuilder.setBatteryLevel(rover.getBattery().getPrimaryPowerUnits())
                .setSolNumber(rover.getSol()).setLocation(lBuilder.build()).setNotes("Spectroscope engaged!")
                .setModuleMessage(rover.getSpectrometer().getSpectrometerReading().toByteString())
                .setSCET(System.currentTimeMillis()).setModuleReporting(ModuleDirectory.Module.SCIENCE.getValue())
                .build();

        rover.state = rover.transmittingState;
        rover.transmitMessage(status.toByteArray());
    }

    public void activateCamera() {
    }

    public void move(RobotPositions positions) {
    }

    public void hibernate() {
    }

    public void rechargeBattery() {
    }

    public void scanSurroundings() {
    }

    public void performDiagnostics() {
    }

}
