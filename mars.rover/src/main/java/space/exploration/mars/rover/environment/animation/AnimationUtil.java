package space.exploration.mars.rover.environment.animation;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

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
}
