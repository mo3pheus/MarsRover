package space.exploration.mars.rover.environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.forkjoin.ThreadLocalRandom;
import space.exploration.mars.rover.sensor.Radar;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class EnvironmentUtils {
    public static final String  FRAME_HEIGHT_PROPERTY          = "maze.environment.frame.height";
    public static final String  FRAME_WIDTH_PROPERTY           = "maze.environment.frame.width";
    public static final String  CELL_WIDTH_PROPERTY            = "maze.environment.cell.width";
    public static final String  NUM_WALLS_PROPERTY             = "maze.environment.num.walls";
    public static final String  WALL_DEFS_PROPERTY             = "maze.environment.wall.definitions.";
    public static final String  START_CELL_COLOR               = "maze.environment.starting.position.color";
    public static final String  DESTN_POSN_PROPERTY            = "maze.environment.destination.position";
    public static final String  ROBOT_START_LOCATION           = "maze.environment.robot.position";
    public static final String  ROBOT_COLOR                    = "maze.environment.robot.color";
    public static final String  ANIMATION_PACE_DELAY           = "maze.environment.animation.pace.delay";
    public static final String  ANIMATION_STEP_SIZE            = "maze.environment.animation.step.size";
    public static final String  LIDAR_ANIMATION_SCAN_DELAY     = "mars.rover.lidar.scan.delay";
    public static final String  SPECTROMETER_ANIMATION_DELAY   = "mars.rover.spectrometer.scan.delay";
    public static final String  MARS_SURFACE_COLOR             = "mars.surface.color";
    public static final String  SPECTROMETER_SCAN_COLOR        = "mars.rover.spectrometer.scan.color";
    public static final String  CAMERA_NUM_IMAGE_CACHES        = "mars.rover.camera.number.image.caches";
    public static final String  CAMERA_SHUTTER_SPEED           = "mars.rover.camera.shutterSpeed";
    public static final String  CAMERA_LOCATION_HEADER         = "mars.rover.camera.image.";
    public static final String  CAMERA_IMAGE_HEADER            = "/marsImages/mars00";
    public static final String  CAMERA_NUM_IMAGES              = "mars.rover.camera.number.images";
    public static final String  CAMERA_POWER_CONSUMPTION       = "mars.rover.camera.powerConsumption";
    public static final String  LIDAR_POWER_CONSUMPTION        = "mars.rover.lidar.powerConsumption";
    public static final String  SPECTROMETER_POWER_CONSUMPTION = "mars.rover.spectrometer.powerConsumption";
    public static final String  PROPULSION_POWER_CONSUMPTION   = "mars.rover.propulsion.powerConsumption";
    public static final String  OLD_ROVERS_COLOR               = "mars.rover.radar.oldRovers.color";
    public static final String  RADAR_SCAN_CIRCLE_COLOR        = "mars.rover.radar.scan.circle.color";
    public static final String  RADAR_CONTACT_COLOR            = "mars.rover.radar.scan.contact.color";
    public static final String  RADAR_CONTACT_PING_DELAY       = "mars.rover.radar.contact.ping.delay";
    public static final Color[] RADAR_CONTACT_COLORS           = {
            new Color(0, 250, 154),
            new Color(152, 251, 152),
            new Color(144, 238, 144),
            new Color(0, 102, 255)};
    public static       Logger  logger                         = LoggerFactory.getLogger(EnvironmentUtils.class);
    //new Color(34, 139, 34)};

    public static Color findColor(String color) {
        if (color.equals("red")) {
            return Color.red;
        } else if (color.equals("lightGray")) {
            return Color.lightGray;
        } else if (color.equals("darkGray")) {
            return Color.darkGray;
        } else if (color.equals("blue")) {
            return Color.blue;
        } else if (color.equals("green")) {
            return Color.green;
        } else if (color.equals("orange")) {
            return Color.orange;
        } else if (color.equals("darkOliveGreen")) {
            return new Color(85, 107, 47);
        } else if (color.equals("robotHibernate")) {
            return new Color(56, 142, 142);
        } else if (color.equals("darkGreen")) {
            //return new Color(34, 139, 34);
            return new Color(0, 100, 0);
        } else if (color.equals("brown")) {
            return new Color(139, 37, 0);
        } else if (color.equals("darkBrown")) {
            return new Color(205, 55, 0);
        } else if (color.equals("royalBlue")) {
            return new Color(0, 102, 255);
        } else if (color.equals("lavendar")) {
            return new Color(123, 104, 238);
        } else if (color.equals("purple")) {
            return new Color(128, 0, 128);
        } else if (color.equals("marsSurfaceRed")) {
            return new Color(238, 118, 0);
        } else if (color.equals("spectrometerScanColor")) {
            return new Color(227, 168, 105);
        } else if (color.equals("black")) {
            return Color.BLACK;
        } else {
            System.out.println(" Color is unknown - known choices are red, lightGray, darkGray, blue, green " + color);
            return null;
        }
    }

    public static Properties getMatrixConfig() throws IOException {
        URL             url          = EnvironmentUtils.class.getResource("/marsDefinition.properties");
        FileInputStream propFile     = new FileInputStream(url.getPath());
        Properties      matrixConfig = new Properties();
        matrixConfig.load(propFile);
        return matrixConfig;
    }

    public static Map<Point, SoilComposition> setUpSurfaceComposition(Properties marsConfig) {
        Map<Point, SoilComposition> surfaceComp = new HashMap<Point, SoilComposition>();

        int frameWidth  = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.FRAME_WIDTH_PROPERTY));
        int frameHeight = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.FRAME_HEIGHT_PROPERTY));
        int cellWidth   = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));

        if (frameHeight != frameWidth) {
            logger.error("Frame is not a square");
        }

        for (int i = 0; i <= (frameHeight - cellWidth); i += cellWidth) {
            for (int j = 0; j <= (frameHeight - cellWidth); j += cellWidth) {
                Point temp = new Point(i, j);
                SoilComposition soilComp = new SoilComposition(ThreadLocalRandom.current().nextDouble(0.0d, 0.8d),
                        ThreadLocalRandom.current().nextDouble(0.0d, 0.8d),
                        ThreadLocalRandom.current().nextDouble(0.0d, 0.8d),
                        ThreadLocalRandom.current().nextDouble(0.0d, 0.8d));
                surfaceComp.put(temp, soilComp);
            }
        }

		/*
         * System.out.println("Keyset for spectroscope = " +
		 * surfaceComp.keySet().size()); for (Point p : surfaceComp.keySet()) {
		 * System.out.println(" Point = " + p.toString() + " SoilComp = " +
		 * surfaceComp.get(p).toString()); }
		 */
        return surfaceComp;
    }

    public static Map<Point, RoverCell> setUpOldRovers(Properties marsConfig) {
        Map<Point, RoverCell> stationMap     = new HashMap<>();
        String                oldRoverPrefix = Radar.RADAR_PREFIX + ".prev.rover.";
        int numStations = Integer.parseInt(marsConfig.getProperty(Radar.RADAR_PREFIX + "" +
                                                                  ".numPrevUnits"));
        for (int i = 0; i < numStations; i++) {
            int       x     = Integer.parseInt(marsConfig.getProperty(oldRoverPrefix + Integer.toString(i)).split(",")[0]);
            int       y     = Integer.parseInt(marsConfig.getProperty(oldRoverPrefix + Integer.toString(i)).split(",")[1]);
            RoverCell rCell = new RoverCell(marsConfig);
            rCell.setLocation(new Point(x + 3, y + 3));
            rCell.setCellWidth(8);
            rCell.setColor(Color.BLUE);
            stationMap.put(new Point(x, y), rCell);
        }

        return stationMap;
    }
}
