package space.exploration.mars.rover.environment;

import space.exploration.mars.rover.animation.*;
import space.exploration.mars.rover.propulsion.TelemetryPacketOuterClass;
import space.exploration.mars.rover.propulsion.TelemetrySensor;
import space.exploration.mars.rover.sensor.Lidar;
import space.exploration.mars.rover.sensor.Spectrometer;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MarsArchitect {
    private Map<Point, SoilComposition> soilCompositionMap = null;
    private JFrame                      marsSurface        = null;
    private Properties                  marsConfig         = null;
    private TrackingAnimationEngine     propulsionEngine   = null;
    private LidarAnimationEngine        lidarEngine        = null;
    private SpectrometerAnimationEngine spectrometerEngine = null;
    private Cell                        robot              = null;
    private int                         cellWidth          = 0;
    private int                         robotStepSize      = 0;
    private TelemetrySensor             telemetrySensor    = null;

    public MarsArchitect(Properties matrixDefinition) {
        this.marsConfig = matrixDefinition;
        this.marsSurface = new JFrame();
        int frameHeight = Integer.parseInt(this.marsConfig.getProperty(EnvironmentUtils.FRAME_HEIGHT_PROPERTY));
        int frameWidth  = Integer.parseInt(this.marsConfig.getProperty(EnvironmentUtils.FRAME_WIDTH_PROPERTY));

        marsSurface.setSize(frameWidth, frameHeight);
        marsSurface.setTitle("Mars");
        this.cellWidth = Integer.parseInt(this.marsConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));
        this.robotStepSize = Integer.parseInt(this.marsConfig.getProperty(EnvironmentUtils.ANIMATION_STEP_SIZE));

        setUpSurface();
        this.propulsionEngine = new TrackingAnimationEngine(marsConfig, marsSurface, robot);
        this.telemetrySensor = new TelemetrySensor(propulsionEngine);
        soilCompositionMap = EnvironmentUtils.setUpSurfaceComposition(marsConfig);
    }

    public CameraAnimationEngine getCameraAnimationEngine(Point location) {
        long shutterSpeed = Long.parseLong(marsConfig.getProperty(EnvironmentUtils.CAMERA_SHUTTER_SPEED));
        CameraAnimationEngine cameraAnimationEngine = new CameraAnimationEngine(marsConfig, location, shutterSpeed,
                                                                                cellWidth);
        return cameraAnimationEngine;
    }

    public Map<Point, SoilComposition> getSoilCompositionMap() {
        return this.soilCompositionMap;
    }

    public SpectrometerAnimationEngine getSpectrometerAnimationEngine() {
        return this.spectrometerEngine;
    }

    public void setSpectrometerAnimationEngine(Spectrometer spectrometer) {
        this.spectrometerEngine = new SpectrometerAnimationEngine(marsConfig, robot, spectrometer, marsSurface);
    }

    public SoilComposition getSoilComposition(Point p) {
        return this.soilCompositionMap.get(p);
    }

    public void updateRobotPositions(List<Point> robotPath) {
        propulsionEngine.updateRobotPosition(robotPath);
    }

    public TelemetryPacketOuterClass.TelemetryPacket getTelemetryPayload() {
        return telemetrySensor.getTelemetryPacket();
    }

    public LidarAnimationEngine getLidarAnimationEngine() {
        return lidarEngine;
    }

    public void setLidarAnimationEngine(Lidar lidar) {
        this.lidarEngine = new LidarAnimationEngine(marsConfig, robot);
        lidarEngine.setMarsSurface(marsSurface);
        lidarEngine.setLidar(lidar);
    }

    public int getCellWidth() {
        return cellWidth;
    }

    public int getRobotStepSize() {
        return robotStepSize;
    }

    private void setUpSurface() {
        JLayeredPane content = AnimationUtil.getContent(marsConfig);

        int roboX = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.ROBOT_START_LOCATION).split(",")[0]);
        int roboY = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.ROBOT_START_LOCATION).split(",")[1]);
        this.robot = new Cell(marsConfig);
        AnimationUtil.updateRobot(marsConfig, new Point(roboX, roboY), robot);
        content.add(robot, Cell.ROBOT_DEPTH);

        marsSurface.setContentPane(content);
        marsSurface.setVisible(true);
        marsSurface.setName("Mars Surface");
    }

    public void returnSurfaceToNormal() {
        AnimationUtil.getStaticEnvironment(this.marsConfig, robot, marsSurface);
    }

    public Cell getRobot() {
        return robot;
    }

    public JFrame getMarsSurface() {
        return marsSurface;
    }
}
