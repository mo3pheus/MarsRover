package space.exploration.mars.rover.kernel;

import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import communications.protocol.ModuleDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass.InstructionPayload.*;
import space.exploration.communications.protocol.communication.RoverStatusOuterClass;
import space.exploration.communications.protocol.service.SamQueryOuterClass;
import space.exploration.communications.protocol.service.WeatherQueryOuterClass;
import space.exploration.communications.protocol.softwareUpdate.SwUpdatePackageOuterClass;
import space.exploration.kernel.diagnostics.LogRequest;
import space.exploration.mars.rover.bootstrap.MarsMissionLaunch;
import space.exploration.mars.rover.communication.Radio;
import space.exploration.mars.rover.diagnostics.Pacemaker;
import space.exploration.mars.rover.diagnostics.RoverGarbageCollector;
import space.exploration.mars.rover.diagnostics.SleepMonitor;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.environment.RoverCell;
import space.exploration.mars.rover.kernel.config.RoverConfig;
import space.exploration.mars.rover.navigation.NavigationEngine;
import space.exploration.mars.rover.power.Battery;
import space.exploration.mars.rover.power.BatteryMonitor;
import space.exploration.mars.rover.sensor.*;
import space.exploration.mars.rover.utils.RoverUtil;

import java.awt.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static space.exploration.mars.rover.kernel.config.RoverConfig.ROVER_NAME;

public class Rover {
    volatile State state = null;

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
    State danSensingState;
    State sampleAnalysisState;
    State maintenanceState;

    /* Status messages */
    private long    creationTime = 0l;
    private boolean equipmentEOL = false;
    private Point   location     = null;

    /* Logging Details */
    private Connection logDBConnection = null;
    private ResultSet  resultSet       = null;
    private ResultSet  errorSet        = null;
    private Logger     logger          = null;
    private Statement  statement       = null;
    private Statement  errorStatement  = null;

    /* Configuration */
    private volatile MarsArchitect marsArchitect = null;
    private volatile RoverConfig   roverConfig   = null;

    /* Equipment Stack */
    private volatile Battery          battery          = null;
    private volatile Radio            radio            = null;
    private          Lidar            lidar            = null;
    private          Camera           camera           = null;
    private          Radar            radar            = null;
    private          WeatherSensor    weatherSensor    = null;
    private          NavigationEngine navigationEngine = null;
    private          ApxsSpectrometer apxsSpectrometer = null;
    private          DANSpectrometer  danSpectrometer  = null;
    private          SamSensor        samSensor        = null;

    /* Kernel Sensors   */
    private volatile SpacecraftClock spacecraftClock = null;
    private volatile PositionSensor  positionSensor  = null;

    /* Contingency Stack */
    private          Map<Point, RoverCell> previousRovers       = null;
    private volatile List<byte[]>          instructionQueue     = null;
    private volatile long                  inRechargingModeTime = 0l;
    private volatile long                  timeMessageReceived  = 0l;
    private volatile boolean               gracefulShutdown     = false;
    private volatile Process               launcherProcess      = null;

    /* Resource Management Stack */
    private volatile Semaphore             accessLock            = new Semaphore(1);
    private volatile Pacemaker             pacemaker             = null;
    private volatile BatteryMonitor        batteryMonitor        = null;
    private volatile SleepMonitor          sleepMonitor          = null;
    private volatile RoverGarbageCollector roverGarbageCollector = null;

    /* Metrics Configuration */
    private final MetricsRegistry metrics               = new MetricsRegistry();
    private       Gauge<Integer>  batteryGauge          = null;
    private       Gauge<Integer>  instructionQueueGauge = null;

    public Rover(Properties marsConfig, Properties comsConfig, Properties logsDBConfig, String marsConfigLocation) {
        this.roverConfig = new RoverConfig(marsConfig, comsConfig, logsDBConfig, "src/main/resources/",
                                           "dataArchives/", marsConfigLocation);
        bootUp();
    }

    public Rover(Properties marsConfig, Properties comsConfig, Properties logsDBConfig, String
            cameraImageCacheLocation, String dataArchiveLocation, String marsConfigLocation) {
        this.roverConfig = new RoverConfig(marsConfig, comsConfig, logsDBConfig, cameraImageCacheLocation,
                                           dataArchiveLocation, marsConfigLocation);
        bootUp();
    }

    public static long getOneSolDuration() {
        /* Time scaled by a factor of 60. */
        long time = TimeUnit.HOURS.toMillis(24);
        time += TimeUnit.MINUTES.toMillis(39);
        time += TimeUnit.SECONDS.toMillis(35);
        time += 244;
        return time;
    }

    public synchronized RoverConfig getRoverConfig() {
        return roverConfig;
    }

    public synchronized void processPendingMessageQueue() {
        state = listeningState;
        receiveMessage(instructionQueue.remove(0));
    }

    public Connection getLogDBConnection() {
        return logDBConnection;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public ResultSet getErrorSet() {
        return errorSet;
    }

    public Logger getLogger() {
        return logger;
    }

    public Statement getStatement() {
        return statement;
    }

    public void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public void setErrorSet(ResultSet errorSet) {
        this.errorSet = errorSet;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void setRadio(Radio radio) {
        this.radio = radio;
    }

    public void setLidar(Lidar lidar) {
        this.lidar = lidar;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public Process getLauncherProcess() {
        return launcherProcess;
    }

    public void setLauncherProcess(Process launcherProcess) {
        this.launcherProcess = launcherProcess;
    }

    public void setRadar(Radar radar) {
        this.radar = radar;
    }

    public void setInstructionQueue(List<byte[]> instructionQueue) {
        this.instructionQueue = instructionQueue;
    }

    public void setBattery(Battery battery) {
        this.battery = battery;
    }

    public boolean isGracefulShutdown() {
        return gracefulShutdown;
    }

    public synchronized void setGracefulShutdown(boolean gracefulShutdown) {
        this.gracefulShutdown = gracefulShutdown;
    }

    public MetricsRegistry getMetrics() {
        return metrics;
    }

    public synchronized void acquireAceessLock(String acquiringParty) throws InterruptedException {
        accessLock.acquire();
        logger.info("Rover accessLock acquired by " + acquiringParty);
        logger.info(" Rover's current state = " + getState().getStateName());
    }

    public synchronized void releaseAccessLock(String releasingParty) {
        accessLock.release();
        logger.info("Rover accessLock released by " + releasingParty);
        logger.info(" Rover's current state = " + getState().getStateName());
    }

    public synchronized long getInRechargingModeTime() {
        return inRechargingModeTime;
    }

    public synchronized void setInRechargingModeTime(long inRechargingModeTime) {
        this.inRechargingModeTime = inRechargingModeTime;
    }

    public synchronized void shootNeutrons() {
        state.shootNeutrons();
    }

    public long getTimeMessageReceived() {
        return timeMessageReceived;
    }

    public void setTimeMessageReceived(long timeMessageReceived) {
        this.timeMessageReceived = timeMessageReceived;
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

    public synchronized State getHibernatingState() {
        return hibernatingState;
    }

    public synchronized State getDanSensingState() {
        return danSensingState;
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

    /**
     * The clock sends this signal to the rover, so it can update all its sensors.
     *
     * @param sol
     */
    public synchronized void updateSensors(int sol) {
        weatherSensor.calibrateREMS(sol);
        danSpectrometer.calibrateDanSensor(sol);
        apxsSpectrometer.calibrateApxsSpectrometer(sol);
        samSensor.updateDataCache(sol);
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

    public synchronized void move(TargetPackage payload) {
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

    public synchronized void synchronizeClocks(String utcTime) {
        state.synchronizeClocks(utcTime);
    }

    public synchronized void gracefulShutdown() {
        state.gracefulShutdown();
    }

    public synchronized void distressShutdown() {
        logger.error("Shutting down rover in distress. System status:: " + pacemaker.generateHeartBeat());
        shutdownSystem();
    }

    public synchronized void sampleAnalysis(SamQueryOuterClass.SamQuery samQuery) {
        state.sampleAnalysis(samQuery);
    }

    public synchronized void updateSoftware(SwUpdatePackageOuterClass.SwUpdatePackage softwarePackage) {
        state.updateSoftware(softwarePackage);
    }

    public synchronized void authorizeTransmission(ModuleDirectory.Module module, byte[] message) {
        /* Choose to filter upon modules here */
        logger.debug("Module " + module.getValue() + " overriding rover state to authorize transmission. endOfLife " +
                             "set" +
                             " to " + Boolean.toString(equipmentEOL));
        state = transmittingState;
        transmitMessage(message);
    }

    public synchronized void setPaceMaker(Pacemaker pacemaker) {
        this.pacemaker = pacemaker;
    }

    public synchronized Radio getRadio() {
        return radio;
    }

    public synchronized MarsArchitect getMarsArchitect() {
        return marsArchitect;
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

    public synchronized DANSpectrometer getDanSpectrometer() {
        return danSpectrometer;
    }

    public synchronized ApxsSpectrometer getApxsSpectrometer() {
        return apxsSpectrometer;
    }

    public synchronized State getState() {
        return state;
    }

    public synchronized void setState(State state) {
        this.state = state;
    }

    public synchronized void setEquipmentEOL(boolean equipmentEOL) {
        this.equipmentEOL = equipmentEOL;
    }

    public SpacecraftClock getSpacecraftClock() {
        return spacecraftClock;
    }

    public synchronized Pacemaker getPacemaker() {
        return this.pacemaker;
    }

    public synchronized RoverGarbageCollector getGarbageCollector() {
        return this.roverGarbageCollector;
    }

    public synchronized SamSensor getSamSensor() {
        return this.samSensor;
    }

    public synchronized void requestLogs(LogRequest.LogRequestPacket logRequestPacket) {
        state.requestLogs(logRequestPacket);
    }

    public synchronized void setGarbageCollector(RoverGarbageCollector garbageCollector) {
        this.roverGarbageCollector = garbageCollector;
    }

    public synchronized boolean isDiagnosticFriendly() {
        return ((this.state == this.listeningState)
                && (instructionQueue.isEmpty()));
    }

    public synchronized void saveOffSensorLifespans() {
        for (IsEquipment equipment : getEquimentList()) {
            if (gracefulShutdown) {
                roverConfig.getMarsConfig().replace(equipment.getEquipmentLifeSpanProperty(), "1000");
            } else {
                roverConfig.getMarsConfig().replace(equipment.getEquipmentLifeSpanProperty(), Integer.toString
                        (equipment.getLifeSpan()));
            }
        }
    }

    public synchronized void configureDB() {
        if (!roverConfig.isDbLoggingEnabled()) {
            return;
        }

        try {
            logDBConnection = DriverManager
                    .getConnection("jdbc:mysql://" + roverConfig.getLogDBConfig().getProperty("mars.rover.database" +
                                                                                                      ".host")
                                           + "/" + roverConfig.getLogDBConfig().getProperty("mars.rover.database" +
                                                                                                    ".dbName")
                                           + "?user=" + roverConfig.getDbUserName() + "&password=" + roverConfig
                            .getDbPassword());
            statement = logDBConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet
                    .CONCUR_UPDATABLE);
            resultSet = statement
                    .executeQuery("SELECT * FROM " + roverConfig.getLogDBConfig().getProperty("mars.rover.database" +
                                                                                                      ".logTableName"));
            errorStatement = logDBConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet
                    .CONCUR_UPDATABLE);
            errorSet = errorStatement
                    .executeQuery("SELECT * FROM " + roverConfig.getLogDBConfig().getProperty("mars.rover.database" +
                                                                                                      ".errorTableName"));
            if (resultSet == null || errorSet == null || errorSet == null || statement == null) {
                logger.error("Error encountered while configuring database.");
                return;
            }

        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public synchronized void configureLidar(Point origin, int cellWidth, int range) {
        boolean lidarEOL = lidar.isEndOfLife();
        this.lidar = new Lidar(origin, cellWidth, range, this);
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

        this.battery = new Battery(roverConfig.getMarsConfig());
        if (recharged) {
            battery.setLifeSpan(lifeSpan - 1);
            int fullBatteryLifeSpan = Integer.parseInt(roverConfig.getMarsConfig().getProperty(Battery.LIFESPAN));
            battery.setPrimaryPowerUnits(battery.getPrimaryPowerUnits() - (fullBatteryLifeSpan - battery.getLifeSpan
                    ()));
        }

        this.batteryMonitor = new BatteryMonitor(this);
        batteryMonitor.monitor();
        RoverUtil.roverSystemLog(logger, "Battery Monitor configured!", "INFO");
    }

    private synchronized void configureRadio() {
        this.radio = new Radio(roverConfig.getComsConfig(), this);
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
        List<IsEquipment> equipmentList = new ArrayList<>();
        equipmentList.add(this.battery);
        equipmentList.add(this.radio);
        equipmentList.add(this.lidar);
        equipmentList.add(this.camera);
        equipmentList.add(this.apxsSpectrometer);
        equipmentList.add(this.radar);
        equipmentList.add(this.weatherSensor);
        equipmentList.add(this.spacecraftClock);
        equipmentList.add(this.positionSensor);
        equipmentList.add(this.danSpectrometer);
        equipmentList.add(this.samSensor);
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
        rBuilder.setNotes("Rover " + ROVER_NAME + " reporting to earth after a restart - How are you earth?");
        return rBuilder.build().toByteArray();
    }

    public synchronized WeatherSensor getWeatherSensor() {
        return weatherSensor;
    }

    public synchronized State getWeatherSensingState() {
        return weatherSensingState;
    }

    public PositionSensor getPositionSensor() {
        return positionSensor;
    }

    private void setUpGauges() {
        batteryGauge = new Gauge<Integer>() {
            @Override
            public Integer value() {
                return battery.getAuxiliaryPowerUnits();
            }
        };

        instructionQueueGauge = new Gauge<Integer>() {
            @Override
            public Integer value() {
                return instructionQueue.size();
            }
        };
    }

    public State getSensingState() {
        return sensingState;
    }

    public State getPhotoGraphingState() {
        return photoGraphingState;
    }

    public State getRadarScanningState() {
        return radarScanningState;
    }

    public synchronized void reflectRoverState() {
        logger.info("Trying to reflect rover state." + state.getStateName() + " Software Version - " +
                            (roverConfig.getSoftwareVersion()));
        marsArchitect.getMarsSurface().setTitle(state.getStateName() + " Software Version - " +
                                                        (roverConfig.getSoftwareVersion()));
        marsArchitect.getMarsSurface().repaint();
    }

    public synchronized void bootUp() {
        Thread.currentThread().setName("roverMain");

        MarsMissionLaunch.configureLogging(roverConfig.isDebugEnabled());

        this.logger = LoggerFactory.getLogger(Rover.class);
        RoverUtil.cleanOutOldLogFiles(this);

        try {
            logger.info("marsRover launched with software version = " + roverConfig.getSoftwareVersion());
        } catch (NumberFormatException nfe) {
            logger.error("Could not get software version information", nfe);
            logger.error("Mars Config content = ");
            for (String key : roverConfig.getMarsConfig().stringPropertyNames()) {
                logger.error(" Key = " + key + " Value = " + roverConfig.getMarsConfig().getProperty(key));
            }
        }

        setUpGauges();
        metrics.newGauge(new MetricName(Rover.class, "RoverBattery"), batteryGauge);
        metrics.newGauge(new MetricName(Rover.class, "RoverInstructionQueue"), instructionQueueGauge);

        this.creationTime = System.currentTimeMillis();
        this.previousRovers = new HashMap<>();
        this.weatherSensor = new WeatherSensor(this);
        this.danSpectrometer = new DANSpectrometer(this);

        this.spacecraftClock = new SpacecraftClock(roverConfig.getMarsConfig());
        spacecraftClock.setRover(this);
        spacecraftClock.start();

        this.positionSensor = new PositionSensor(roverConfig.getMarsConfig());
        positionSensor.start();

        this.marsArchitect = new MarsArchitect(roverConfig.getMarsConfig());
        this.listeningState = new ListeningState(this);
        this.hibernatingState = new HibernatingState(this);
        this.maintenanceState = new MaintenanceState(this);
        this.exploringState = new ApxsSensingState(this);
        this.movingState = new MovingState(this);
        this.photoGraphingState = new PhotographingState(this);
        this.sensingState = new LidarSensingState(this);
        this.transmittingState = new TransmittingState(this);
        this.radarScanningState = new RadarScanningState(this);
        this.weatherSensingState = new WeatherSensingState(this);
        this.sleepingState = new SleepingState(this);
        this.sclkBeepingState = new SclkTimingState(this);
        this.danSensingState = new DANSensingState(this);
        this.sampleAnalysisState = new SampleAnalysisState(this);

        this.instructionQueue = new ArrayList<>();
        RoverUtil.roverSystemLog(logger, "Rover + " + ROVER_NAME + " states initialized. ", "INFO ");
        logger.info("Rover + " + ROVER_NAME + " states initialized. ");

        this.pacemaker = new Pacemaker(this);
        pacemaker.pulse();
        logger.info("Pacemaker initialized. ");

        sleepMonitor = new SleepMonitor(this);
        logger.info("SleepMonitor initialized. ");

        roverGarbageCollector = new RoverGarbageCollector(this);
        roverGarbageCollector.start();
        logger.info("RoverGC initialized. ");

        String[] stPosition = roverConfig.getMarsConfig().getProperty(EnvironmentUtils.ROBOT_START_LOCATION).split(",");
        location = new Point(Integer.parseInt(stPosition[0]), Integer.parseInt(stPosition[1]));
        logger.info("Rover current position is = " + location.toString());

        int cellWidth = Integer.parseInt(roverConfig.getMarsConfig().getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));

        this.lidar = new Lidar(location, cellWidth, cellWidth, this);

        this.apxsSpectrometer = new ApxsSpectrometer(this);
        this.camera =
                (roverConfig.getCameraImageCacheLocation() == null) ? new Camera(roverConfig.getMarsConfig(), this) :
                        new Camera(roverConfig.getMarsConfig(),
                                   this,
                                   roverConfig.getCameraImageCacheLocation());
        this.radar = new Radar(this);
        this.navigationEngine = new NavigationEngine(roverConfig.getMarsConfig());
        this.samSensor = new SamSensor(roverConfig.getMarsConfig());

        configureDB();
        configureBattery(false);
        configureRadio();
        configureRadar();

        state = transmittingState;
        transmitMessage(getBootupMessage());
    }

    public void shutdownRover() {
        logger.info("Stopping all daemon processes.");

        logger.info(" 1. Stopping Pacemaker.");
        pacemaker.hardInterrupt();

        logger.info(" 2. Stopping GarbageCollector.");
        roverGarbageCollector.hardInterrupt();

        logger.info(" 3. Stopping BatteryMonitor. ");
        batteryMonitor.hardInterrupt();

        logger.info(" 4. Stopping SleepMonitor. ");
        sleepMonitor.hardInterrupt();

        logger.info(" 5. Stopping PositionSensor.");
        positionSensor.hardInterrupt();

        logger.info("6. Saving properties file. ");

        String utcTime = spacecraftClock.getUTCTime();
        roverConfig.getMarsConfig().replace(SpacecraftClock.SCLK_START_TIME, utcTime);
        try {
            logger.info("Writing properties file to :: " + roverConfig.getMarsConfigLocation());
            RoverUtil.saveOffProperties(roverConfig.getMarsConfig(), roverConfig.getMarsConfigLocation());
        } catch (IOException e) {
            logger.error("Encountered an exception when writing properties file during shutdown.", e);
        }

        logger.info(" 7. Stopping SpacecraftClock. ");
        spacecraftClock.hardInterrupt();

        logger.info(" 8. Shutting down graphics.");
        marsArchitect.getMarsSurface().dispose();

        logger.info(" 9. Stopping Radio Communications.");
        radio.stopRadio();
    }

    protected void shutdownSystem() {
        logger.info("Houston this is Curiosity saying goodbye! Hope I did OK!");
        Runtime.getRuntime().halt(0);
    }
}
