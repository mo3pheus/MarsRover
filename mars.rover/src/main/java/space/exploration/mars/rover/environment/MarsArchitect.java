package space.exploration.mars.rover.environment;

import java.awt.Point;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;

import space.exploration.mars.rover.animation.TrackingAnimationEngine;
import space.exploration.mars.rover.sensor.Lidar;
import space.exploration.mars.rover.animation.AnimationUtil;
import space.exploration.mars.rover.animation.LidarAnimationEngine;

public class MarsArchitect {
	private JFrame					matrixWorld		= null;
	private Properties				matrixConfig	= null;
	private TrackingAnimationEngine	animationEngine	= null;
	private LidarAnimationEngine	lidarEngine		= null;
	private Cell					robot			= null;
	private int						cellWidth		= 0;

	public MarsArchitect(Properties matrixDefinition, List<Point> robotPath) {
		this.matrixConfig = matrixDefinition;
		this.matrixWorld = new JFrame();
		int frameHeight = Integer.parseInt(this.matrixConfig.getProperty(EnvironmentUtils.FRAME_HEIGHT_PROPERTY));
		int frameWidth = Integer.parseInt(this.matrixConfig.getProperty(EnvironmentUtils.FRAME_WIDTH_PROPERTY));

		matrixWorld.setSize(frameWidth, frameHeight);
		matrixWorld.setTitle("Matrix");
		this.animationEngine = new TrackingAnimationEngine(matrixConfig, matrixWorld, robotPath);
		this.cellWidth = Integer.parseInt(this.matrixConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));
		animationEngine.renderRobotAnimation();
	}

	public MarsArchitect(Properties matrixDefinition) {
		this.matrixConfig = matrixDefinition;
		this.matrixWorld = new JFrame();
		int frameHeight = Integer.parseInt(this.matrixConfig.getProperty(EnvironmentUtils.FRAME_HEIGHT_PROPERTY));
		int frameWidth = Integer.parseInt(this.matrixConfig.getProperty(EnvironmentUtils.FRAME_WIDTH_PROPERTY));

		matrixWorld.setSize(frameWidth, frameHeight);
		matrixWorld.setTitle("Matrix");
		this.cellWidth = Integer.parseInt(this.matrixConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));
		this.animationEngine = new TrackingAnimationEngine(matrixConfig, matrixWorld);
	}

	public void setLidarAnimationEngine(Lidar lidar) {
		this.lidarEngine = new LidarAnimationEngine(matrixConfig);
		lidarEngine.setMarsSurface(matrixWorld);
		lidarEngine.setLidar(lidar);
	}

	public void updateRobotPositions(List<Point> robotPath) {
		animationEngine.updateRobotPosition(robotPath);
	}

	public LidarAnimationEngine getLidarAnimationEngine() {
		return lidarEngine;
	}

	public int getCellWidth() {
		return cellWidth;
	}

	public void setUpSurface() {
		JLayeredPane content = AnimationUtil.getContent(matrixConfig);

		int roboX = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.ROBOT_START_LOCATION).split(",")[0]);
		int roboY = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.ROBOT_START_LOCATION).split(",")[1]);
		robot = AnimationUtil.getRobot(matrixConfig, new Point(roboX, roboY));
		content.add(robot, Cell.ROBOT_DEPTH);

		matrixWorld.setContentPane(content);
		matrixWorld.setVisible(true);
		matrixWorld.setName("Mars Surface");
	}

	public Cell getRobot() {
		return robot;
	}
}
