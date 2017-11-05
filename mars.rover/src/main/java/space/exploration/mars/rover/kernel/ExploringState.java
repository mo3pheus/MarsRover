/**
 *
 */
package space.exploration.mars.rover.kernel;

import com.google.protobuf.InvalidProtocolBufferException;
import space.exploration.mars.rover.InstructionPayloadOuterClass;
import space.exploration.mars.rover.communication.RoverStatusOuterClass.RoverStatus;
import space.exploration.mars.rover.communication.RoverStatusOuterClass.RoverStatus.Location;
import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.service.WeatherQueryOuterClass;

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
        try {
            rover.writeSystemLog(InstructionPayloadOuterClass.InstructionPayload.parseFrom(message), rover
                    .getInstructionQueue().size());
        } catch (InvalidProtocolBufferException ipe) {
            rover.writeErrorLog("Invalid Protocol Buffer Exception", ipe);
        }
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

        marsArchitect.createSpectrometerAnimationEngine(rover.getSpectrometer());
        marsArchitect.getSpectrometerAnimationEngine().activateSpectrometer();
        marsArchitect.returnSurfaceToNormal();

        Location.Builder lBuilder = Location.newBuilder().setX(robot.getLocation().x).setY(robot.getLocation
                ().y);

        RoverStatus.Builder rBuilder = RoverStatus.newBuilder();
        RoverStatus status = rBuilder.setBatteryLevel(rover.getBattery().getPrimaryPowerUnits())
                .setSolNumber(rover.getSol()).setLocation(lBuilder.build()).setNotes("Spectroscope engaged!")
                .setModuleMessage(rover.getSpectrometer().getSpectrometerReading().toByteString())
                .setSCET(System.currentTimeMillis()).setModuleReporting(ModuleDirectory.Module.SCIENCE
                                                                                .getValue())
                .build();

        rover.state = rover.transmittingState;
        rover.transmitMessage(status.toByteArray());
    }

    public void move(InstructionPayloadOuterClass.InstructionPayload payload) {
    }

    public void hibernate() {
    }

    public void senseWeather(WeatherQueryOuterClass.WeatherQuery weatherQuery) {
    }

    public void scanSurroundings() {
    }

    @Override
    public void activateCameraById(String camId) {

    }

    public void activateCameraById() {
    }

    @Override
    public void performRadarScan() {

    }

    @Override
    public void sleep() {

    }

    @Override
    public void wakeUp() {

    }

    @Override
    public String getStateName() {
        return "Exploring State";
    }

}
