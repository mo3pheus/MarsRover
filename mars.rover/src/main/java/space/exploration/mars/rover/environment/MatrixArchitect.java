package space.exploration.mars.rover.environment;

import java.awt.Point;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;

import space.exploration.mars.rover.environment.animation.LayeredAnimationEngine;

public class MatrixArchitect {
	private JFrame					matrixWorld		= null;
	private Properties				matrixConfig	= null;
	private LayeredAnimationEngine	animationEngine	= null;

	public MatrixArchitect(Properties matrixDefinition, List<Point> robotPath) {
		this.matrixConfig = matrixDefinition;
		this.matrixWorld = new JFrame();
		int frameHeight = Integer.parseInt(this.matrixConfig.getProperty(EnvironmentUtils.FRAME_HEIGHT_PROPERTY));
		int frameWidth = Integer.parseInt(this.matrixConfig.getProperty(EnvironmentUtils.FRAME_WIDTH_PROPERTY));

		matrixWorld.setSize(frameWidth, frameHeight);
		matrixWorld.setTitle("Matrix");
		this.animationEngine = new LayeredAnimationEngine(matrixConfig, matrixWorld, robotPath);
		animationEngine.renderAnimation();
	}
	
	public MatrixArchitect(Properties matrixDefinition) {
		this.matrixConfig = matrixDefinition;
		this.matrixWorld = new JFrame();
		int frameHeight = Integer.parseInt(this.matrixConfig.getProperty(EnvironmentUtils.FRAME_HEIGHT_PROPERTY));
		int frameWidth = Integer.parseInt(this.matrixConfig.getProperty(EnvironmentUtils.FRAME_WIDTH_PROPERTY));

		matrixWorld.setSize(frameWidth, frameHeight);
		matrixWorld.setTitle("Matrix");
		this.animationEngine = new LayeredAnimationEngine(matrixConfig, matrixWorld);
	}
	
	public void updateRobotPositions(List<Point> robotPath){
		animationEngine.updateRobotPosition(robotPath);
	}
}
