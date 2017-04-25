package space.exploration.mars.rover.sensor;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import space.exploration.mars.rover.environment.Wall;
import space.exploration.mars.rover.environment.WallBuilder;

public class Lidar {
	private static final int	LIDAR_STEP_SIZE			= 25;
	private static final int	LIDAR_SWEEP_RESOLUTION	= 5;
	private int					range					= 0;

	private WallBuilder			wallBuilder				= null;
	private List<Point>			sweepPoints				= null;
	private Point				origin					= null;

	public Lidar(Point origin, int range) {
		sweepPoints = new ArrayList<Point>();
		this.origin = origin;
		this.range = range;
	}

	private void performSweep() {
		for (int i = LIDAR_STEP_SIZE; i < range; i = i + LIDAR_STEP_SIZE) {
			for (int theta = 0; theta < 360; theta = theta + LIDAR_SWEEP_RESOLUTION) {
				int x = (int) (i * Math.cos(theta));
				int y = (int) (i * Math.sin(theta));
				sweepPoints.add(new Point(x + origin.x, y + origin.y));
			}
		}
		recordContacts();
	}
	
	private void recordContacts(){
		List<Point> contacts = new ArrayList<Point>();
		for(Point p: sweepPoints){
			Line2D line = new Line2D.Double(origin, p);
			for(Wall w:wallBuilder.getWalls()){
				
			}
		}
	}

	public List<Point> getLidarSweep() {
		return sweepPoints;
	}

	public void setWallBuilder(WallBuilder wallBuilder) {
		this.wallBuilder = wallBuilder;
		performSweep();
	}
}
