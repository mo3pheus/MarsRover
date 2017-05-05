package space.exploration.mars.rover.kernel;

import space.exploration.mars.rover.communication.Radio;
import space.exploration.mars.rover.communication.RoverStatusOuterClass.RoverStatus;
import space.exploration.mars.rover.communication.RoverStatusOuterClass.RoverStatus.Location;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.power.Battery;
import space.exploration.mars.rover.robot.RobotPositionsOuterClass.RobotPositions;
import space.exploration.mars.rover.sensor.Lidar;
import space.exploration.mars.rover.sensor.Spectrometer;

import java.awt.*;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class Rover {
    public static final String ROVER_NAME = "Curiosity";
    State state = null;

    /* States supported */
    State listeningState;
    State sensingState;
    State movingState;
    State exploringState;
    State transmittingState;
    State hibernatingState;
    State rechargingState;

    /* Status messages */
    RoverStatus status       = null;
    long        creationTime = 0l;

    /* Configuration */
    private Properties    marsConfig    = null;
    private Properties    comsConfig    = null;
    private MarsArchitect marsArchitect = null;
    private Point         location      = null;

    /* Equipment Stack */
    private Battery      battery      = new Battery();
    private Radio        radio        = null;
    private Lidar        lidar        = null;
    private Spectrometer spectrometer = null;

    /* Contingency Stack */
    private Queue<byte[]> instructionQueue = null;

    /* Sets up the rover and the boot-up sequence */
    public Rover(Properties marsConfig, Properties comsConfig) {
        this.creationTime = System.currentTimeMillis();
        this.marsConfig = marsConfig;
        this.comsConfig = comsConfig;
        this.listeningState = new ListeningState(this);
        this.hibernatingState = new HibernatingState(this);
        this.exploringState = new ExploringState(this);
        this.movingState = new MovingState(this);
        this.rechargingState = new RechargingState(this);
        this.sensingState = new SensingState(this);
        this.transmittingState = new TransmittingState(this);
        this.radio = new Radio(comsConfig, this);
        this.marsArchitect = new MarsArchitect(marsConfig);
        this.instructionQueue = new java.util.PriorityQueue<byte[]>();

        String[] stPosition = marsConfig.getProperty(EnvironmentUtils.ROBOT_START_LOCATION).split(",");
        this.location = new Point(Integer.parseInt(stPosition[0]), Integer.parseInt(stPosition[1]));

        int cellWidth = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));

        this.lidar = new Lidar(location, cellWidth, cellWidth);
        this.spectrometer = new Spectrometer(location);
        state = transmittingState;
        transmitMessage(getBootupMessage());
    }

    public void receiveMessage(byte[] message) {
        state.receiveMessage(message);
    }

    public void scanSurroundings() {
        state.scanSurroundings();
    }

    public void move(RobotPositions positions) {
        state.move(positions);
    }

    public void exploreArea() {
        state.exploreArea();
    }

    public void transmitMessage(byte[] message) {
        state.transmitMessage(message);
    }

    public Radio getRadio() {
        return radio;
    }

    public MarsArchitect getMarsArchitect() {
        return marsArchitect;
    }

    public void setStatus(RoverStatus status) {
        this.status = status;
    }

    public Properties getMarsConfig() {
        return marsConfig;
    }

    public long getOneSolDuration() {
        /* Time scaled by a factor of 60. */
        long time = TimeUnit.MINUTES.toMillis(24);
        time += TimeUnit.SECONDS.toMillis(39);
        time += 35;
        time += 244;
        return time;
    }

    public Queue<byte[]> getInstructionQueue() {
        return this.instructionQueue;
    }

    public Battery getBatter() {
        return battery;
    }

    public int getSol() {
        long diff  = System.currentTimeMillis() - creationTime;
        long solMs = getOneSolDuration();
        return Math.round(diff / solMs);
    }

    public Lidar getLidar() {
        return lidar;
    }

    public void configureLidar(Point origin, int cellWidth, int range) {
        this.lidar = new Lidar(origin, cellWidth, range);
    }

    public Spectrometer getSpectrometer() {
        return spectrometer;
    }

    public void configureSpectrometer(Point origin) {
        this.spectrometer = new Spectrometer(origin);
    }

    public byte[] getLaggingAlertMsg() {
        Location location = Location.newBuilder().setX(marsArchitect.getRobot().getLocation().x)
                .setY(marsArchitect.getRobot().getLocation().y).build();
        RoverStatus.Builder rBuilder = RoverStatus.newBuilder();
        rBuilder.setModuleReporting(ModuleDirectory.Module.KERNEL.getValue());
        rBuilder.setScet(System.currentTimeMillis());
        rBuilder.setLocation(location);
        rBuilder.setBatteryLevel(this.getBatter().getPrimaryPowerUnits());
        rBuilder.setSolNumber(getSol());
        rBuilder.setNotes("Rover " + ROVER_NAME + " is currently lagging for message processing by a count of " +
                this.getInstructionQueue().size());
        return rBuilder.build().toByteArray();
    }

    private byte[] getBootupMessage() {
        Location location = Location.newBuilder().setX(marsArchitect.getRobot().getLocation().x)
                .setY(marsArchitect.getRobot().getLocation().y).build();
        RoverStatus.Builder rBuilder = RoverStatus.newBuilder();
        rBuilder.setModuleReporting(ModuleDirectory.Module.KERNEL.getValue());
        rBuilder.setScet(System.currentTimeMillis());
        rBuilder.setLocation(location);
        rBuilder.setBatteryLevel(this.getBatter().getPrimaryPowerUnits());
        rBuilder.setSolNumber(getSol());
        rBuilder.setNotes("Rover " + ROVER_NAME + " reporting to earth after a restart - How are you earth?");
        return rBuilder.build().toByteArray();
    }
}
