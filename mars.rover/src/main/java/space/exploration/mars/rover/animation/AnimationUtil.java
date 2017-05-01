package space.exploration.mars.rover.animation;

import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JLayeredPane;

import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.environment.Grid;
import space.exploration.mars.rover.environment.WallBuilder;

public class AnimationUtil {
	public static List<Point> generateRobotPositions(Point start, Point end, int stepSize) {
		List<Point> positions = new ArrayList<Point>();

		int numXIterations = Math.abs(start.x - end.x) / stepSize;
		int lastStepSize = Math.abs(start.x - end.x) % stepSize;
		int startX = start.x;
		int startY = start.y;
		for (int i = 0; i < numXIterations; i++) {
			startX = (start.x > end.x) ? startX - stepSize : startX + stepSize;
			Point temp = new Point(startX, startY);
			positions.add(temp);
		}
		if (lastStepSize > 0) {
			startX = (start.x > end.x) ? startX - lastStepSize : startX + lastStepSize;
			Point temp = new Point(startX, startY);
			positions.add(temp);
		}

		int numYIterations = Math.abs(start.y - end.y) / stepSize;
		lastStepSize = Math.abs(start.y - end.y) % stepSize;
		for (int i = 0; i < numYIterations; i++) {
			startY = (start.y > end.y) ? startY - stepSize : startY + stepSize;
			Point temp = new Point(startX, startY);
			positions.add(temp);
		}
		if (lastStepSize > 0) {
			startY = (start.y > end.y) ? startY - lastStepSize : startY + lastStepSize;
			Point temp = new Point(startX, startY);
			positions.add(temp);
		}

		return positions;
	}

	public static Cell getStartLocation(Properties matrixConfig) {
		Cell start = new Cell(matrixConfig);
		int sourceX = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.ROBOT_START_LOCATION).split(",")[0]);
		int sourceY = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.ROBOT_START_LOCATION).split(",")[1]);
		start.setLocation(new Point(sourceX, sourceY));
		start.setColor(EnvironmentUtils.findColor(matrixConfig.getProperty(EnvironmentUtils.START_CELL_COLOR)));
		return start;
	}

	public static JLayeredPane getContent(Properties marsRoverConfig) {
		JLayeredPane marsSurface = new JLayeredPane();
		marsSurface.setBackground(
				EnvironmentUtils.findColor(marsRoverConfig.getProperty(EnvironmentUtils.MARS_SURFACE_COLOR)));
		marsSurface.setOpaque(true);
		List<Component> content = new ArrayList<Component>();

		Grid grid = new Grid(marsRoverConfig);
		WallBuilder wallBuilder = new WallBuilder(marsRoverConfig);
		Cell destinationCell = new Cell(marsRoverConfig);
		Cell startingCell = getStartLocation(marsRoverConfig);

		content.add(grid);
		content.add(wallBuilder);
		content.add(startingCell);
		content.add(destinationCell);

		for (int i = 0; i < content.size(); i++) {
			marsSurface.add(content.get(i), new Integer(i));
		}

		return marsSurface;
	}
	
	public static void getRobot(Properties marsRoverConfig, Point position, Cell robot){
		robot.setLocation(position);
		robot.setCellWidth(12);
		robot.setColor(EnvironmentUtils.findColor(marsRoverConfig.getProperty(EnvironmentUtils.ROBOT_COLOR)));
	}
}
