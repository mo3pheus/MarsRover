package space.exploration.mars.rover.kernel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.InstructionPayloadOuterClass;
import space.exploration.mars.rover.communication.Radio;
import space.exploration.mars.rover.communication.RoverStatusOuterClass.RoverStatus;
import space.exploration.mars.rover.communication.RoverStatusOuterClass.RoverStatus.Location;
import space.exploration.mars.rover.diagnostics.Pacemaker;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.environment.RoverCell;
import space.exploration.mars.rover.learning.ReinforcementLearner;
import space.exploration.mars.rover.power.Battery;
import space.exploration.mars.rover.power.BatteryMonitor;
import space.exploration.mars.rover.robot.RobotPositionsOuterClass.RobotPositions;
import space.exploration.mars.rover.sensor.Camera;
import space.exploration.mars.rover.sensor.Lidar;
import space.exploration.mars.rover.sensor.Radar;
import space.exploration.mars.rover.sensor.Spectrometer;
import space.exploration.mars.rover.utils.RoverUtil;

import java.awt.*;
import java.util.*;
import java.util.List;
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
    State photoGraphingState;
    State radarScanningState;

    /* Status messages */
    private RoverStatus status       = null;
    private long        creationTime = 0l;
    private Logger      logger       = null;
    private boolean     equipmentEOL = false;

    /* Configuration */
    private Properties           marsConfig    = null;
    private Properties           comsConfig    = null;
    private MarsArchitect        marsArchitect = null;
    private Point                location      = null;
    private ReinforcementLearner rlNavEngine   = null;

    /* Equipment Stack */
    private Battery        battery        = null;
    private BatteryMonitor batteryMonitor = null;
    private Radio          radio          = null;
    private Lidar          lidar          = null;
    private Spectrometer   spectrometer   = null;
    private Camera         camera         = null;
    private Radar          radar          = null;

    /* Contingency Stack */
    private Map<Point, RoverCell> previousRovers       = null;
    private List<byte[]>          instructionQueue     = null;
    private long                  inRechargingModeTime = 0l;
    private Pacemaker             pacemaker            = null;

    /* Thread safety */
    private boolean stopHeartBeat = false;

    /* Sets up the rover and the boot-up sequence */
    public Rover(Properties marsConfig, Properties comsConfig) {
        this.marsConfig = marsConfig;
        this.comsConfig = comsConfig;
        bootUp(false);
    }

    public void processPendingMessageQueue() {
        if (!instructionQueue.isEmpty() && state != hibernatingState) {
            state = listeningState;
            receiveMessage(instructionQueue.remove(0));
        }
    }

    public static long getOneSolDuration() {
        /* Time scaled by a factor of 60. */
        long time = TimeUnit.MINUTES.toMillis(24);
        time += TimeUnit.SECONDS.toMillis(39);
        time += 35;
        time += 244;
        return time;
    }

    public long getInRechargingModeTime() {
        return inRechargingModeTime;
    }

    public void setInRechargingModeTime(long inRechargingModeTime) {
        this.inRechargingModeTime = inRechargingModeTime;
    }

    public void configureRLEngine() {
        this.rlNavEngine = new ReinforcementLearner(marsConfig);
    }

    public State getListeningState() {
        return listeningState;
    }

    public void setListeningState(State listeningState) {
        this.listeningState = listeningState;
    }

    public State getSensingState() {
        return sensingState;
    }

    public void setSensingState(State sensingState) {
        this.sensingState = sensingState;
    }

    public State getMovingState() {
        return movingState;
    }

    public void setMovingState(State movingState) {
        this.movingState = movingState;
    }

    public State getExploringState() {
        return exploringState;
    }

    public void setExploringState(State exploringState) {
        this.exploringState = exploringState;
    }

    public State getTransmittingState() {
        return transmittingState;
    }

    public void setTransmittingState(State transmittingState) {
        this.transmittingState = transmittingState;
    }

    public State getHibernatingState() {
        return hibernatingState;
    }

    public void setHibernatingState(State hibernatingState) {
        this.hibernatingState = hibernatingState;
    }

    public State getRechargingState() {
        return rechargingState;
    }

    public void setRechargingState(State rechargingState) {
        this.rechargingState = rechargingState;
    }

    public State getPhotoGraphingState() {
        return photoGraphingState;
    }

    public void setPhotoGraphingState(State photoGraphingState) {
        this.photoGraphingState = photoGraphingState;
    }

    public State getRadarScanningState() {
        return radarScanningState;
    }

    public void setRadarScanningState(State radarScanningState) {
        this.radarScanningState = radarScanningState;
    }

    public ReinforcementLearner getRlNavEngine() {
        return rlNavEngine;
    }

    public Map<Point, RoverCell> getPreviousRovers() {
        return previousRovers;
    }

    public void setPreviousRovers(Map<Point, RoverCell> previousRovers) {
        this.previousRovers = previousRovers;
    }

    public Camera getCamera() {
        return camera;
    }

    public void receiveMessage(byte[] message) {
        state.receiveMessage(message);
    }

    public void activateCamera() {
        state.activateCamera();
    }

    public void scanSurroundings() {
        state.scanSurroundings();
    }

    public void move(InstructionPayloadOuterClass.InstructionPayload payload) {
        state.move(payload);
    }

    public void performRadarScan() {
        state.performRadarScan();
    }

    public void exploreArea() {
        state.exploreArea();
    }

    public void transmitMessage(byte[] message) {
        state.transmitMessage(message);
    }

    public boolean isStopHeartBeat() {
        return stopHeartBeat;
    }

    public void setStopHeartBeat(boolean stopHeartBeat) {
        this.stopHeartBeat = stopHeartBeat;
    }

    public void authorizeTransmission(ModuleDirectory.Module module, byte[] message) {
        /* Choose to filter upon modules here */
        logger.info("Module " + module.getValue() + " overriding rover state to authorize transmission. endOfLife set" +
                            " to " + Boolean.toString(equipmentEOL));
        state = transmittingState;
        transmitMessage(message);
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

    public List<byte[]> getInstructionQueue() {
        return this.instructionQueue;
    }

    public Battery getBattery() {
        return battery;
    }

    public Lidar getLidar() {
        return lidar;
    }

    public Spectrometer getSpectrometer() {
        return spectrometer;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean isEquipmentEOL() {
        return equipmentEOL;
    }

    public void setEquipmentEOL(boolean equipmentEOL) {
        this.equipmentEOL = equipmentEOL;
    }

    public void configureSpectrometer(Point origin) {
        int     spectrometerLifeSpan = spectrometer.getLifeSpan();
        boolean spectrometerEOL      = spectrometer.isEndOfLife();
        this.spectrometer = new Spectrometer(origin, this);
        spectrometer.setLifeSpan(spectrometerLifeSpan);
        spectrometer.setEndOfLife(spectrometerEOL);
    }

    public int getSol() {
        long diff  = System.currentTimeMillis() - creationTime;
        long solMs = getOneSolDuration();
        return Math.round(diff / solMs);
    }

    public boolean isDiagnosticFriendly() {
        return ((this.state == this.listeningState)
                && (instructionQueue.isEmpty()));
    }

    public void configureLidar(Point origin, int cellWidth, int range) {
        int     lidarLifespan = lidar.getLifeSpan();
        boolean lidarEOL      = lidar.isEndOfLife();
        this.lidar = new Lidar(origin, cellWidth, range, this);
        lidar.setLifeSpan(lidarLifespan);
        lidar.setEndOfLife(lidarEOL);
    }

    public void configureRadar() {
        int     radarLifespan = radar.getLifeSpan();
        boolean radarEOL      = radar.isEndOfLife();
        this.radar = new Radar(this);
        radar.setLifeSpan(radarLifespan);
        radar.setEndOfLife(radarEOL);
    }

    public Radar getRadar() {
        return radar;
    }

    public void powerCheck(int powerConsumed) {
        battery.setPrimaryPowerUnits(battery.getPrimaryPowerUnits() - powerConsumed);
    }

    public byte[] getLaggingAlertMsg() {
        Location location = Location.newBuilder().setX(marsArchitect.getRobot().getLocation().x)
                .setY(marsArchitect.getRobot().getLocation().y).build();
        RoverStatus.Builder rBuilder = RoverStatus.newBuilder();
        rBuilder.setModuleReporting(ModuleDirectory.Module.KERNEL.getValue());
        rBuilder.setSCET(System.currentTimeMillis());
        rBuilder.setLocation(location);
        rBuilder.setBatteryLevel(this.getBattery().getPrimaryPowerUnits());
        rBuilder.setSolNumber(getSol());
        rBuilder.setNotes("Rover " + ROVER_NAME + " is currently lagging for message processing by a count of " +
                                  this.getInstructionQueue().size());
        return rBuilder.build().toByteArray();
    }

    public List<IsEquipment> getEquimentList() {
        List<IsEquipment> equipmentList = new ArrayList<IsEquipment>();
        equipmentList.add(this.battery);
        equipmentList.add(this.radio);
        equipmentList.add(this.lidar);
        equipmentList.add(this.camera);
        equipmentList.add(this.spectrometer);
        equipmentList.add(this.radar);
        return equipmentList;
    }

    public byte[] getHibernatingAlertMessage() {
        Location location = Location.newBuilder().setX(marsArchitect.getRobot().getLocation().x)
                .setY(marsArchitect.getRobot().getLocation().y).build();
        RoverStatus.Builder rBuilder = RoverStatus.newBuilder();
        rBuilder.setModuleReporting(ModuleDirectory.Module.KERNEL.getValue());
        rBuilder.setSCET(System.currentTimeMillis());
        rBuilder.setLocation(location);
        rBuilder.setBatteryLevel(this.getBattery().getPrimaryPowerUnits());
        rBuilder.setSolNumber(getSol());
        rBuilder.setNotes("Rover " + ROVER_NAME + " is currently shutting down for recharging, expect the next " +
                                  "communication at " + System.currentTimeMillis() + battery.getRechargeTime());
        return rBuilder.build().toByteArray();
    }

    public byte[] getBootupMessage() {
        Location location = Location.newBuilder().setX(marsArchitect.getRobot().getLocation().x)
                .setY(marsArchitect.getRobot().getLocation().y).build();
        RoverStatus.Builder rBuilder = RoverStatus.newBuilder();
        rBuilder.setModuleReporting(ModuleDirectory.Module.KERNEL.getValue());
        rBuilder.setSCET(System.currentTimeMillis());
        rBuilder.setLocation(location);
        rBuilder.setBatteryLevel(this.getBattery().getPrimaryPowerUnits());
        rBuilder.setSolNumber(getSol());
        rBuilder.setNotes("Rover " + ROVER_NAME + " reporting to earth after a restart - How are you earth?");
        return rBuilder.build().toByteArray();
    }

    public void configureBattery(boolean recharged) {
        int batteryAlertThreshold = Integer.parseInt(marsConfig.getProperty("mars.rover.battery.alertThreshold"));
        int rechargeTime          = Integer.parseInt(marsConfig.getProperty("mars.rover.battery.rechargeTime"));
        int lifeSpan              = Integer.parseInt(marsConfig.getProperty(Battery.LIFESPAN));

        if (this.battery != null) {
            lifeSpan = battery.getLifeSpan();
        }

        this.battery = new Battery(batteryAlertThreshold, rechargeTime);
        if (recharged) {
            battery.setLifeSpan(lifeSpan - 1);
        } else {
            battery.setLifeSpan(lifeSpan);
        }

        this.batteryMonitor = new BatteryMonitor(1, this);
        batteryMonitor.monitor();
        RoverUtil.roverSystemLog(logger, "Battery Monitor configured!", "INFO");
    }

    private void configureRadio() {
        this.radio = new Radio(comsConfig, this);
        long radioCheckPulse = Long.parseLong(marsConfig.getProperty("mars.rover.radio.check.pulse"));
        int  lifeSpan        = Integer.parseInt(marsConfig.getProperty(Radio.LIFESPAN));
        radio.getReceiver().setRadioCheckPulse(radioCheckPulse);
        radio.setLifeSpan(lifeSpan);
    }

    private byte[] getErrorRecoveryMessage(int instructionLength) {
        Location location = Location.newBuilder().setX(marsArchitect.getRobot().getLocation().x)
                .setY(marsArchitect.getRobot().getLocation().y).build();
        RoverStatus.Builder rBuilder = RoverStatus.newBuilder();
        rBuilder.setModuleReporting(ModuleDirectory.Module.KERNEL.getValue());
        rBuilder.setSCET(System.currentTimeMillis());
        rBuilder.setLocation(location);
        rBuilder.setBatteryLevel(this.getBattery().getPrimaryPowerUnits());
        rBuilder.setSolNumber(getSol());
        rBuilder.setNotes("Rover " + ROVER_NAME + " reporting to earth after a restart following failure. " +
                                  "Potential messages lost = " + instructionLength + " Check logs and diagnostics for" +
                                  " further information.");
        return rBuilder.build().toByteArray();
    }

    public void bootUp(boolean fatalError) {
        int instructionQueueLength = 0;

        this.creationTime = System.currentTimeMillis();
        this.previousRovers = new HashMap<>();
        this.listeningState = new ListeningState(this);
        this.hibernatingState = new HibernatingState(this);
        this.exploringState = new ExploringState(this);
        this.movingState = new MovingState(this);
        this.rechargingState = new RechargingState(this);
        this.photoGraphingState = new PhotographingState(this);
        this.sensingState = new SensingState(this);
        this.transmittingState = new TransmittingState(this);
        this.radarScanningState = new RadarScanningState(this);
        this.marsArchitect = new MarsArchitect(marsConfig);

        if (this.instructionQueue != null) {
            instructionQueueLength = instructionQueue.size();
        }

        this.instructionQueue = new ArrayList<byte[]>();
        this.logger = LoggerFactory.getLogger(Rover.class);
        RoverUtil.roverSystemLog(logger, "Rover + " + ROVER_NAME + " states initialized. ", "INFO ");

        this.pacemaker = new Pacemaker(10, this);
        pacemaker.pulse();
        RoverUtil.roverSystemLog(logger, "Pacemaker initialized. ", "INFO ");

        String[] stPosition = marsConfig.getProperty(EnvironmentUtils.ROBOT_START_LOCATION).split(",");
        this.location = new Point(Integer.parseInt(stPosition[0]), Integer.parseInt(stPosition[1]));
        RoverUtil.roverSystemLog(logger, "Rover current position is = " + location.toString(), "INFO");

        int cellWidth = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));

        this.lidar = new Lidar(location, cellWidth, cellWidth, this);
        lidar.setLifeSpan(Integer.parseInt(marsConfig.getProperty(Lidar.LIFESPAN)));

        this.spectrometer = new Spectrometer(location, this);
        spectrometer.setLifeSpan(Integer.parseInt(marsConfig.getProperty(Spectrometer.LIFESPAN)));

        this.camera = new Camera(this.marsConfig, this);
        this.radar = new Radar(this);

        configureBattery(false);
        configureRadio();
        configureRadar();

        state = transmittingState;
        if (!fatalError) {
            transmitMessage(getBootupMessage());
        } else {
            getErrorRecoveryMessage(instructionQueueLength);
        }
    }
}
