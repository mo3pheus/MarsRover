package space.exploration.mars.rover.kernel;

import communications.protocol.ModuleDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;
import space.exploration.communications.protocol.communication.RoverStatusOuterClass;
import space.exploration.communications.protocol.service.WeatherQueryOuterClass;
import space.exploration.mars.rover.communication.Radio;
import space.exploration.mars.rover.diagnostics.Pacemaker;
import space.exploration.mars.rover.diagnostics.RoverGarbageCollector;
import space.exploration.mars.rover.diagnostics.SleepMonitor;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.environment.RoverCell;
import space.exploration.mars.rover.navigation.NavigationEngine;
import space.exploration.mars.rover.power.Battery;
import space.exploration.mars.rover.power.BatteryMonitor;
import space.exploration.mars.rover.sensor.*;
import space.exploration.mars.rover.utils.RoverUtil;

import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Rover {
    public static final String ROVER_NAME = "Curiosity";
    State state = null;

    /* Kernel definition */
    State listeningState;
    State sensingState;
    State movingState;
    State exploringState;
    State transmittingState;
    State hibernatingState;
    State photoGraphingState;
    State radarScanningState;
    State sleepingState;
    State weatherSensingState;
    State sclkBeepingState;

    /* Status messages */
    private long    creationTime = 0l;
    private boolean equipmentEOL = false;

    /* Logging Details */
    private Connection logDBConnection  = null;
    private ResultSet  resultSet        = null;
    private ResultSet  errorSet         = null;
    private Logger     logger           = null;
    private String     dbUserName       = null;
    private String     dbPassword       = null;
    private Statement  statement        = null;
    private Statement  errorStatement   = null;
    private boolean    dbLoggingEnabled = false;

    /* Configuration */
    private Properties       marsConfig               = null;
    private Properties       comsConfig               = null;
    private Properties       logDBConfig              = null;
    private String           cameraImageCacheLocation = null;
    private String           dataArchiveLocation      = null;
    private MarsArchitect    marsArchitect            = null;
    private Point            location                 = null;
    private NavigationEngine navigationEngine         = null;
    private String           nasaApiAuthKey           = null;

    /* Equipment Stack */
    private Radio           radio           = null;
    private Lidar           lidar           = null;
    private Spectrometer    spectrometer    = null;
    private Camera          camera          = null;
    private Radar           radar           = null;
    private WeatherSensor   weatherSensor   = null;
    private SpacecraftClock spacecraftClock = null;
    private PositionSensor  positionSensor  = null;

    /* Contingency Stack */
    private Map<Point, RoverCell> previousRovers       = null;
    private List<byte[]>          instructionQueue     = null;
    private long                  inRechargingModeTime = 0l;
    private long                  timeMessageReceived  = 0l;

    /* Resource Management Stack */
    private Semaphore      accessLock     = new Semaphore(1);
    private Pacemaker      pacemaker      = null;
    private Battery        battery        = null;
    private BatteryMonitor batteryMonitor = null;

    public Rover(Properties marsConfig, Properties comsConfig, Properties logsDBConfig) {
        this.marsConfig = marsConfig;
        this.comsConfig = comsConfig;
        this.logDBConfig = logsDBConfig;
        this.dataArchiveLocation = "/dataArchives";
        this.nasaApiAuthKey = "DEMO_KEY";
        bootUp();
    }

    public Rover(Properties marsConfig, Properties comsConfig, Properties logsDBConfig, String
            cameraImageCacheLocation, String dataArchiveLocation) {
        this.marsConfig = marsConfig;
        this.comsConfig = comsConfig;
        this.logDBConfig = logsDBConfig;
        this.cameraImageCacheLocation = cameraImageCacheLocation;
        this.dataArchiveLocation = dataArchiveLocation;
        bootUp();
    }

    public static long getOneSolDuration() {
        /* Time scaled by a factor of 60. */
        long time = TimeUnit.MINUTES.toMillis(24);
        time += TimeUnit.SECONDS.toMillis(39);
        time += 35;
        time += 244;
        return time;
    }

    public synchronized void processPendingMessageQueue() {
        if (!instructionQueue.isEmpty() && state != hibernatingState) {
            try {
                acquireAceessLock("roverKernel");
            } catch (InterruptedException e) {
                logger.error("Error while acquiring accessLock", e);
            }

            if (state == sleepingState) {
                releaseAccessLock("roverKernel");
                wakeUp();
            } else {
                state = listeningState;
                releaseAccessLock("roverKernel");
                receiveMessage(instructionQueue.remove(0));
            }
        }
    }

    public void writeSystemLog(InstructionPayloadOuterClass.InstructionPayload.TargetPackage targetPackage, int
            instructionQueueLength) {

        if (!dbLoggingEnabled) {
            return;
        }

        try {
            if (resultSet.isClosed()) {
                resultSet = statement.executeQuery("SELECT * FROM " + logDBConfig.getProperty("mars.rover.database" +
                                                                                                      ".logTableName"));
            }
            resultSet.moveToInsertRow();
            resultSet.updateTimestamp("EVENT_TIME", new Timestamp(System.currentTimeMillis()));
            Blob blob = logDBConnection.createBlob();
            blob.setBytes(1, targetPackage.toByteArray());
            resultSet.updateBlob("MESSAGE_DETAILS", blob);
            resultSet.updateString("MESSAGE", targetPackage.getAction() + " Instruction Queue length = " +
                    instructionQueueLength);
            resultSet.insertRow();
        } catch (SQLException e) {
            logger.error("SQLException", e);
        }
    }

    public void writeSystemLog(InstructionPayloadOuterClass.InstructionPayload instructionPayload, int
            instructionQueueLength) {

        if (!dbLoggingEnabled) {
            return;
        }

        try {
            if (resultSet.isClosed()) {
                resultSet = statement.executeQuery("SELECT * FROM " + logDBConfig.getProperty("mars.rover.database" +
                                                                                                      ".logTableName"));
            }
            resultSet.moveToInsertRow();
            resultSet.updateTimestamp("EVENT_TIME", new Timestamp(System.currentTimeMillis()));
            Blob blob = logDBConnection.createBlob();
            blob.setBytes(1, instructionPayload.toByteArray());
            resultSet.updateBlob("MESSAGE_DETAILS", blob);
            resultSet.updateString("MESSAGE", "message added to instruction queue" + " Instruction Queue length = " +
                    instructionQueueLength);
            resultSet.insertRow();
        } catch (SQLException e) {
            logger.error("SQLException", e);
        }
    }

    public void writeSystemLog(String message, int instructionQueueLength) {

        if (!dbLoggingEnabled) {
            return;
        }

        try {
            if (resultSet.isClosed()) {
                resultSet = statement.executeQuery("SELECT * FROM " + logDBConfig.getProperty("mars.rover.database" +
                                                                                                      ".logTableName"));
            }
            resultSet.moveToInsertRow();
            resultSet.updateTimestamp("EVENT_TIME", new Timestamp(System.currentTimeMillis()));
            resultSet.updateString("MESSAGE", message + " Instruction Queue length = " +
                    instructionQueueLength);
            resultSet.insertRow();
        } catch (SQLException e) {
            logger.error("SQLException", e);
        }
    }

    public void writeErrorLog(String message, Exception e) {

        if (!dbLoggingEnabled) {
            return;
        }

        try {
            if (errorSet.isClosed()) {
                errorSet = statement.executeQuery("SELECT * FROM " + logDBConfig.getProperty("mars.rover.database" +
                                                                                                     ".errorTableName"));
            }
            errorSet.moveToInsertRow();
            errorSet.updateTimestamp("EVENT_TIME", new Timestamp(System.currentTimeMillis()));
            errorSet.updateString("MESSAGE", message);

            if (e != null) {
                String stackTrace = "";
                for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                    stackTrace += stackTraceElement.toString() + "|";
                }
                errorSet.updateString("MESSAGE_DETAILS", stackTrace);
            }
            errorSet.insertRow();
        } catch (Exception exception) {
            logger.error("Exception while writing errorLog", e);
        }
    }

    public synchronized void acquireAceessLock(String acquiringParty) throws InterruptedException {
        accessLock.acquire();
        logger.debug("Rover accessLock acquired by " + acquiringParty);
        logger.debug(" Rover's current state = " + getState().getStateName());
    }

    public synchronized void releaseAccessLock(String releasingParty) {
        accessLock.release();
        logger.debug("Rover accessLock released by " + releasingParty);
        logger.debug(" Rover's current state = " + getState().getStateName());
    }

    public synchronized long getInRechargingModeTime() {
        return inRechargingModeTime;
    }

    public synchronized void setInRechargingModeTime(long inRechargingModeTime) {
        this.inRechargingModeTime = inRechargingModeTime;
    }

    public String getNasaApiAuthKey() {
        return nasaApiAuthKey;
    }

    public long getTimeMessageReceived() {
        return timeMessageReceived;
    }

    public void setTimeMessageReceived(long timeMessageReceived) {
        this.timeMessageReceived = timeMessageReceived;
    }

    public String getDataArchiveLocation() {
        return dataArchiveLocation;
    }

    public synchronized State getListeningState() {
        return listeningState;
    }

    public synchronized State getTransmittingState() {
        return transmittingState;
    }

    public synchronized State getSleepingState() {
        return sleepingState;
    }

    public synchronized State getSclkBeepingState() {
        return sclkBeepingState;
    }

    public State getHibernatingState() {
        return hibernatingState;
    }

    public synchronized NavigationEngine getNavigationEngine() {
        return navigationEngine;
    }

    public synchronized void setPreviousRovers(Map<Point, RoverCell> previousRovers) {
        this.previousRovers = previousRovers;
    }

    public synchronized void senseWeather(WeatherQueryOuterClass.WeatherQuery weatherQuery) {
        state.senseWeather(weatherQuery);
    }

    public synchronized Camera getCamera() {
        return camera;
    }

    public synchronized void receiveMessage(byte[] message) {
        state.receiveMessage(message);
    }

    public synchronized void scanSurroundings() {
        state.scanSurroundings();
    }

    public synchronized void move(InstructionPayloadOuterClass.InstructionPayload payload) {
        state.move(payload);
    }

    public synchronized void performRadarScan() {
        state.performRadarScan();
    }

    public synchronized void wakeUp() {
        state.wakeUp();
    }

    public synchronized void sleep() {
        state.sleep();
    }

    public synchronized void getSclkInformation() {
        state.getSclkInformation();
    }

    public synchronized void exploreArea() {
        state.exploreArea();
    }

    public synchronized void transmitMessage(byte[] message) {
        state.transmitMessage(message);
    }

    public synchronized void authorizeTransmission(ModuleDirectory.Module module, byte[] message) {
        /* Choose to filter upon modules here */
        logger.debug("Module " + module.getValue() + " overriding rover state to authorize transmission. endOfLife " +
                             "set" +
                             " to " + Boolean.toString(equipmentEOL));
        state = transmittingState;
        transmitMessage(message);
    }

    public synchronized Radio getRadio() {
        return radio;
    }

    public synchronized MarsArchitect getMarsArchitect() {
        return marsArchitect;
    }

    public synchronized Properties getMarsConfig() {
        return marsConfig;
    }

    public synchronized List<byte[]> getInstructionQueue() {
        return this.instructionQueue;
    }

    public synchronized Battery getBattery() {
        return battery;
    }

    public synchronized Lidar getLidar() {
        return lidar;
    }

    public synchronized Spectrometer getSpectrometer() {
        return spectrometer;
    }

    public synchronized State getState() {
        return state;
    }

    public synchronized void setState(State state) {
        this.state = state;
    }

    public synchronized boolean isEquipmentEOL() {
        return equipmentEOL;
    }

    public synchronized void setEquipmentEOL(boolean equipmentEOL) {
        this.equipmentEOL = equipmentEOL;
    }

    public synchronized SpacecraftClock getSpacecraftClock() {
        return spacecraftClock;
    }

    /**
     * Note: This may be required if the clock primary partition fills or if there is a clock reset.
     *
     * @param spacecraftClock
     */
    public synchronized void setSpacecraftClock(SpacecraftClock spacecraftClock) {
        this.spacecraftClock.stopClock();
        this.spacecraftClock = spacecraftClock;
        this.spacecraftClock.start();
    }

    public int getSol() {
        long diff  = System.currentTimeMillis() - creationTime;
        long solMs = getOneSolDuration();
        return Math.round(diff / solMs);
    }

    public synchronized boolean isDiagnosticFriendly() {
        return ((this.state == this.listeningState)
                && (instructionQueue.isEmpty()));
    }

    public synchronized void getNasaApiCredentials() {
        this.nasaApiAuthKey = (this.nasaApiAuthKey == null) ? RoverUtil.getNasaCredentials() : "DEMO_KEY";
    }

    public synchronized void configureDB() {
        dbLoggingEnabled = Boolean.parseBoolean(logDBConfig.getProperty("mars.rover.database.logging.enable"));

        if (!dbLoggingEnabled) {
            return;
        }

        try {
            System.out.println("Configuring database");
            if (dbUserName == null || dbPassword == null) {
                dbUserName = RoverUtil.getDatabaseCredentials(false);
                dbPassword = RoverUtil.getDatabaseCredentials(true);
            }
            logDBConnection = DriverManager
                    .getConnection("jdbc:mysql://" + logDBConfig.getProperty("mars.rover.database.host")
                                           + "/" + logDBConfig.getProperty("mars.rover.database.dbName")
                                           + "?user=" + dbUserName + "&password=" + dbPassword);
            statement = logDBConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet
                    .CONCUR_UPDATABLE);
            resultSet = statement.executeQuery("SELECT * FROM " + logDBConfig.getProperty("mars.rover.database" +
                                                                                                  ".logTableName"));
            errorStatement = logDBConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet
                    .CONCUR_UPDATABLE);
            errorSet = errorStatement.executeQuery("SELECT * FROM " + logDBConfig.getProperty("mars.rover.database" +
                                                                                                      ".errorTableName"));
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public synchronized void configureSpectrometer(Point origin) {
        int     spectrometerLifeSpan = spectrometer.getLifeSpan();
        boolean spectrometerEOL      = spectrometer.isEndOfLife();
        this.spectrometer = new Spectrometer(origin, this);
        spectrometer.setLifeSpan(spectrometerLifeSpan);
        spectrometer.setEndOfLife(spectrometerEOL);
    }

    public synchronized void configureLidar(Point origin, int cellWidth, int range) {
        int     lidarLifespan = lidar.getLifeSpan();
        boolean lidarEOL      = lidar.isEndOfLife();
        this.lidar = new Lidar(origin, cellWidth, range, this);
        lidar.setLifeSpan(lidarLifespan);
        lidar.setEndOfLife(lidarEOL);
    }

    public synchronized void configureRadar() {
        int     radarLifespan = radar.getLifeSpan();
        boolean radarEOL      = radar.isEndOfLife();
        this.radar = new Radar(this);
        radar.setLifeSpan(radarLifespan);
        radar.setEndOfLife(radarEOL);
    }

    /**
     * Note: Battery charging capacity diminishes with usage.
     *
     * @param recharged
     */
    public synchronized void configureBattery(boolean recharged) {
        int lifeSpan = 0;

        if (this.battery != null) {
            lifeSpan = battery.getLifeSpan();
        }

        this.battery = new Battery(marsConfig);
        if (recharged) {
            battery.setLifeSpan(lifeSpan - 1);
            int fullBatteryLifeSpan = Integer.parseInt(marsConfig.getProperty("mars.rover.battery.lifeSpan"));
            battery.setPrimaryPowerUnits(battery.getPrimaryPowerUnits() - (fullBatteryLifeSpan - battery.getLifeSpan
                    ()));
        }

        this.batteryMonitor = new BatteryMonitor(this);
        batteryMonitor.monitor();
        RoverUtil.roverSystemLog(logger, "Battery Monitor configured!", "INFO");
    }

    private synchronized void configureRadio() {
        this.radio = new Radio(comsConfig, this);
    }

    public synchronized Radar getRadar() {
        return radar;
    }

    public synchronized void powerCheck(int powerConsumed) {
        battery.setPrimaryPowerUnits(battery.getPrimaryPowerUnits() - powerConsumed);
    }

    public synchronized void activateCameraById(String camId) {
        state.activateCameraById(camId);
    }

    public synchronized List<IsEquipment> getEquimentList() {
        List<IsEquipment> equipmentList = new ArrayList<IsEquipment>();
        equipmentList.add(this.battery);
        equipmentList.add(this.radio);
        equipmentList.add(this.lidar);
        equipmentList.add(this.camera);
        equipmentList.add(this.spectrometer);
        equipmentList.add(this.radar);
        equipmentList.add(this.weatherSensor);
        equipmentList.add(this.spacecraftClock);
        equipmentList.add(this.positionSensor);
        return equipmentList;
    }

    public synchronized byte[] getBootupMessage() {
        RoverStatusOuterClass.RoverStatus.Location location = RoverStatusOuterClass.RoverStatus.Location.newBuilder()
                .setX(marsArchitect.getRobot().getLocation().x)
                .setY(marsArchitect.getRobot().getLocation().y).build();
        RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus.newBuilder();
        rBuilder.setModuleReporting(ModuleDirectory.Module.KERNEL.getValue());
        rBuilder.setSCET(System.currentTimeMillis());
        rBuilder.setLocation(location);
        rBuilder.setBatteryLevel(this.getBattery().getPrimaryPowerUnits());
        rBuilder.setSolNumber(getSol());
        rBuilder.setNotes("Rover " + ROVER_NAME + " reporting to earth after a restart - How are you earth?");
        return rBuilder.build().toByteArray();
    }

    public synchronized WeatherSensor getWeatherSensor() {
        return weatherSensor;
    }

    public synchronized State getWeatherSensingState() {
        return weatherSensingState;
    }

    public synchronized PositionSensor getPositionSensor() {
        return positionSensor;
    }

    public synchronized void setWeatherSensingState(State weatherSensingState) {
        this.weatherSensingState = weatherSensingState;
    }

    public synchronized void bootUp() {
        this.creationTime = System.currentTimeMillis();
        this.previousRovers = new HashMap<>();
        this.spacecraftClock = new SpacecraftClock(marsConfig);
        spacecraftClock.start();

        this.positionSensor = new PositionSensor(this);
        positionSensor.start();

        this.listeningState = new ListeningState(this);
        this.hibernatingState = new HibernatingState(this);
        this.exploringState = new ExploringState(this);
        this.movingState = new MovingState(this);
        this.photoGraphingState = new PhotographingState(this);
        this.sensingState = new SensingState(this);
        this.transmittingState = new TransmittingState(this);
        this.radarScanningState = new RadarScanningState(this);
        this.weatherSensingState = new WeatherSensingState(this);
        this.sleepingState = new SleepingState(this);
        this.marsArchitect = new MarsArchitect(marsConfig);
        this.sclkBeepingState = new SclkTimingState(this);

        this.instructionQueue = new ArrayList<byte[]>();
        this.logger = LoggerFactory.getLogger(Rover.class);
        RoverUtil.roverSystemLog(logger, "Rover + " + ROVER_NAME + " states initialized. ", "INFO ");

        this.pacemaker = new Pacemaker(this);
        pacemaker.pulse();
        RoverUtil.roverSystemLog(logger, "Pacemaker initialized. ", "INFO ");

        new SleepMonitor(this);
        RoverUtil.roverSystemLog(logger, "SleepMonitor initialized. ", "INFO ");

        new RoverGarbageCollector(this);
        RoverUtil.roverSystemLog(logger, "RoverGC initialized. ", "INFO ");

        String[] stPosition = marsConfig.getProperty(EnvironmentUtils.ROBOT_START_LOCATION).split(",");
        location = new Point(Integer.parseInt(stPosition[0]), Integer.parseInt(stPosition[1]));
        RoverUtil.roverSystemLog(logger, "Rover current position is = " + location.toString(), "INFO");

        int cellWidth = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));

        this.lidar = new Lidar(location, cellWidth, cellWidth, this);
        lidar.setLifeSpan(Integer.parseInt(marsConfig.getProperty(Lidar.LIFESPAN)));

        this.spectrometer = new Spectrometer(location, this);
        spectrometer.setLifeSpan(Integer.parseInt(marsConfig.getProperty(Spectrometer.LIFESPAN)));

        this.camera = (cameraImageCacheLocation == null) ? new Camera(this.marsConfig, this) : new Camera(this.marsConfig,
                                                                                                          this,
                                                                                                          cameraImageCacheLocation);
        this.radar = new Radar(this);
        this.weatherSensor = new WeatherSensor(this);


        this.navigationEngine = new NavigationEngine(this.getMarsConfig());

        configureDB();
        getNasaApiCredentials();
        configureBattery(false);
        configureRadio();
        configureRadar();

        state = transmittingState;
        transmitMessage(getBootupMessage());
    }
}
