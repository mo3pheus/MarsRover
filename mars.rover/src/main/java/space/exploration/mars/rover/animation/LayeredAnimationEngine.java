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
import space.exploration.mars.rover.environment.*;

/**
 * @author sanketkorgaonkar
 *
 */
public class LayeredAnimationEngine {
	private static final Integer	ROBOT_DEPTH		= new Integer(100);
	private Properties				matrixConfig	= null;
	private JFrame					frame			= null;
	private List<Point>				robotPositions	= null;

	public LayeredAnimationEngine(Properties matrixConfig, JFrame frame, List<Point> robotPositions) {
		if (robotPositions == null || robotPositions.isEmpty()) {
			return;
		}
		this.matrixConfig = matrixConfig;
		this.frame = frame;
		this.robotPositions = robotPositions;
	}

	public LayeredAnimationEngine(Properties matrixConfig, JFrame frame) {
		this.matrixConfig = matrixConfig;
		this.frame = frame;
	}

	public void updateRobotPosition(List<Point> robotPositions) {
		this.robotPositions = robotPositions;
		renderAnimation();
	}

	public void renderAnimation() {
		int delayMs = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.ANIMATION_PACE_DELAY));
		JLayeredPane contentPane = getContent();
		for (Point position : robotPositions) {
			Cell robo = new Cell(matrixConfig);
			robo.setLocation(position);
			robo.setCellWidth(14);
			robo.setColor(EnvironmentUtils.findColor(matrixConfig.getProperty(EnvironmentUtils.ROBOT_COLOR)));
			contentPane.add(robo, ROBOT_DEPTH);
			frame.setContentPane(contentPane);
			frame.setVisible(true);
			try {
				Thread.sleep(delayMs);
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
			contentPane.remove(robo);
		}
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

	private static Cell getStartLocation(Properties matrixConfig) {
		Cell start = new Cell(matrixConfig);
		int sourceX = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.ROBOT_START_LOCATION).split(",")[0]);
		int sourceY = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.ROBOT_START_LOCATION).split(",")[1]);
		start.setLocation(new Point(sourceX, sourceY));
		start.setColor(EnvironmentUtils.findColor(matrixConfig.getProperty(EnvironmentUtils.START_CELL_COLOR)));
		return start;
	}
}
