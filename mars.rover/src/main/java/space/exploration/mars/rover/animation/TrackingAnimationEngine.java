/**
 * 
 */
package space.exploration.mars.rover.animation;

import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;

import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.sensor.Lidar;
import space.exploration.mars.rover.environment.*;

/**
 * @author sanketkorgaonkar
 *
 */
public class TrackingAnimationEngine {
	private static final Integer	ROBOT_DEPTH				= new Integer(100);
	private LidarAnimationEngine	lidarAnimationEngine	= null;
	private Properties				matrixConfig			= null;
	private JFrame					frame					= null;
	private List<Point>				robotPositions			= null;
	private List<Point>				hotContacts				= null;
	private Cell					robot					= null;

	public TrackingAnimationEngine(Properties matrixConfig, JFrame frame, List<Point> robotPositions) {
		if (robotPositions == null || robotPositions.isEmpty()) {
			return;
		}
		this.matrixConfig = matrixConfig;
		this.frame = frame;
		this.robotPositions = robotPositions;
		this.lidarAnimationEngine = null;
		this.robot = new Cell(matrixConfig);
		this.hotContacts = new ArrayList<Point>();
	}

	public TrackingAnimationEngine(Properties matrixConfig, JFrame frame) {
		this.matrixConfig = matrixConfig;
		this.frame = frame;
	}

	public void updateRobotPosition(List<Point> robotPositions) {
		this.robotPositions = robotPositions;
		renderRobotAnimation();
	}

	public void activateLidar() {
		int radius = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));
		Lidar lidar = new Lidar(robot.getLocation(), radius);
		lidar.setWallBuilder(new WallBuilder(matrixConfig));
		lidar.performSweep();
		this.hotContacts = lidar.getContacts();
		lidarAnimationEngine = new LidarAnimationEngine(lidar, matrixConfig);
		renderLidarAnimation();
	}

	public void renderLidarAnimation() {
		int delayMs = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.LIDAR_ANIMATION_SCAN_DELAY));
		JLayeredPane contentPane = getContent();
		contentPane.add(this.robot, ROBOT_DEPTH);
		for (Laser laser : lidarAnimationEngine.getLaserBeams()) {
			contentPane.add(laser, LidarAnimationEngine.LIDAR_DEPTH);
			frame.setContentPane(contentPane);
			frame.setVisible(true);
			try {
				Thread.sleep(delayMs);
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
			contentPane.remove(laser);
		}
	}

	public void renderRobotAnimation() {
		int delayMs = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.ANIMATION_PACE_DELAY));
		JLayeredPane contentPane = getContent();
		for (Point position : robotPositions) {
			this.robot = new Cell(matrixConfig);
			this.robot.setLocation(position);
			this.robot.setCellWidth(14);
			this.robot.setColor(EnvironmentUtils.findColor(matrixConfig.getProperty(EnvironmentUtils.ROBOT_COLOR)));

			contentPane.add(this.robot, ROBOT_DEPTH);
			frame.setContentPane(contentPane);
			frame.setVisible(true);
			try {
				Thread.sleep(delayMs);
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
			contentPane.remove(this.robot);
		}
		activateLidar();
	}

	public List<Point> getHotContacts() {
		return this.hotContacts;
	}

	public static Cell getStartLocation(Properties matrixConfig) {
		Cell start = new Cell(matrixConfig);
		int sourceX = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.ROBOT_START_LOCATION).split(",")[0]);
		int sourceY = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.ROBOT_START_LOCATION).split(",")[1]);
		start.setLocation(new Point(sourceX, sourceY));
		start.setColor(EnvironmentUtils.findColor(matrixConfig.getProperty(EnvironmentUtils.START_CELL_COLOR)));
		return start;
	}

	private JLayeredPane getContent() {
		JLayeredPane matrixPane = new JLayeredPane();
		List<Component> content = new ArrayList<Component>();

		Grid grid = new Grid(matrixConfig);
		WallBuilder wallBuilder = new WallBuilder(matrixConfig);
		Cell destinationCell = new Cell(matrixConfig);
		Cell startingCell = getStartLocation(matrixConfig);

		content.add(grid);
		content.add(wallBuilder);
		content.add(startingCell);
		content.add(destinationCell);

		for (int i = 0; i < content.size(); i++) {
			matrixPane.add(content.get(i), new Integer(i));
		}

		return matrixPane;
	}
}
