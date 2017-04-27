/**
 * 
 */
package space.exploration.mars.rover.animation;

import java.awt.Point;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;

import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.environment.EnvironmentUtils;

/**
 * @author sanketkorgaonkar
 *
 */
public class TrackingAnimationEngine {
	private Properties				marsRoverConfig	= null;
	private JFrame					frame			= null;
	private List<Point>				robotPositions	= null;
	private Cell					robot			= null;

	public TrackingAnimationEngine(Properties matrixConfig, JFrame frame, List<Point> robotPositions) {
		if (robotPositions == null || robotPositions.isEmpty()) {
			return;
		}
		this.marsRoverConfig = matrixConfig;
		this.frame = frame;
		this.robotPositions = robotPositions;
		this.robot = new Cell(matrixConfig);
	}

	public TrackingAnimationEngine(Properties matrixConfig, JFrame frame) {
		this.marsRoverConfig = matrixConfig;
		this.frame = frame;
	}

	public void updateRobotPosition(List<Point> robotPositions) {
		this.robotPositions = robotPositions;
		renderRobotAnimation();
	}

	public void renderRobotAnimation() {
		int delayMs = Integer.parseInt(marsRoverConfig.getProperty(EnvironmentUtils.ANIMATION_PACE_DELAY));
		JLayeredPane contentPane = AnimationUtil.getContent(marsRoverConfig);
		for (Point position : robotPositions) {
			this.robot = AnimationUtil.getRobot(marsRoverConfig, position);
			contentPane.add(this.robot, Cell.ROBOT_DEPTH);
			frame.setContentPane(contentPane);
			frame.setVisible(true);
			try {
				Thread.sleep(delayMs);
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
			contentPane.remove(this.robot);
		}
	}
}
