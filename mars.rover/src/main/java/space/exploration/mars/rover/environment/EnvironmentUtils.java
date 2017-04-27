package space.exploration.mars.rover.environment;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class EnvironmentUtils {
	public static final String	FRAME_HEIGHT_PROPERTY				= "maze.environment.frame.height";
	public static final String	FRAME_WIDTH_PROPERTY				= "maze.environment.frame.width";
	public static final String	CELL_WIDTH_PROPERTY					= "maze.environment.cell.width";
	public static final String	NUM_WALLS_PROPERTY					= "maze.environment.num.walls";
	public static final String	WALL_DEFS_PROPERTY					= "maze.environment.wall.definitions.";
	public static final String	START_CELL_POSN						= "maze.environment.starting.position";
	public static final String	START_CELL_COLOR					= "maze.environment.starting.position.color";
	public static final String	START_POSN_PROPERTY					= "maze.environment.start.position";
	public static final String	DESTN_POSN_PROPERTY					= "maze.environment.destination.position";
	public static final String	ROBOT_START_LOCATION				= "maze.environment.robot.position";
	public static final String	ROBOT_COLOR							= "maze.environment.robot.color";
	public static final String	DESTN_COLOR_PROPERTY				= "maze.environment.destination.color";
	public static final String	ANIMATION_PACE_DELAY				= "maze.environment.animation.pace.delay";
	public static final String	ANIMATION_STEP_SIZE					= "maze.environment.animation.step.size";
	public static final String	ANIMATION_PROFILE_NUMBER_POSITIONS	= "maze.environment.animation.number.positions";
	public static final String	ANIMATION_PROFILE_POSITION_HEADER	= "maze.environment.animation.position.";
	public static final String	LIDAR_ANIMATION_SCAN_DELAY			= "mars.rover.lidar.scan.delay";
	public static final String	MARS_SURFACE_COLOR					= "mars.surface.color";

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
		} else if (color.equals("darkGreen")) {
			return new Color(0, 153, 51);
		} else if (color.equals("brown")) {
			//return new Color(102, 51, 0);
			return new Color(139,69,0);
		} else if (color.equals("darkBrown") ){
			return new Color(139,90,43);
		}else if (color.equals("royalBlue")) {
			return new Color(0, 102, 255);
		} else if (color.equals("lavendar")) {
			return new Color(123, 104, 238);
		} else if (color.equals("marsSurfaceRed")) {
			return new Color(205, 133, 0);
		} else if (color.equals("black")) {
			return Color.BLACK;
		}

		else {
			System.out.println(" Color is unknown - known choices are red, lightGray, darkGray, blue, green " + color);
			return null;
		}
	}

	public static Properties getMatrixConfig() throws IOException {
		URL url = EnvironmentUtils.class.getResource("/marsDefinition.properties");
		FileInputStream propFile = new FileInputStream(url.getPath());
		Properties matrixConfig = new Properties();
		matrixConfig.load(propFile);
		return matrixConfig;
	}
}
