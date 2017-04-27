package space.exploration.mars.rover.sensor;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import space.exploration.mars.rover.environment.Wall;
import space.exploration.mars.rover.environment.WallBuilder;

public class Lidar {
	private static final int DELTA_THETA = 5;

	private Logger		logger		= LoggerFactory.getLogger(Lidar.class);
	private WallBuilder	wallBuilder	= null;
	private List<Point>	contacts	= null;
	private List<Point>	scanRadius	= null;
	private Point		origin		= null;
	private double		range		= 0;

	public Lidar(Point origin, int range) {
		contacts = new ArrayList<Point>();
		this.origin = origin;
		this.range = range;
		this.range = Math.sqrt((2.0d * range * range));
		logger.info("Lidar instantiated");
	}

	public void setWallBuilder(WallBuilder wallBuilder) {
		logger.info("Wallbuilder set for lidar");
		this.wallBuilder = wallBuilder;
	}

	public List<Point> getContacts() {
		return contacts;
	}

	public List<Point> getScanRadius() {
		return scanRadius;
	}

	public Point getOrigin() {
		return origin;
	}

	public void scanArea() {
		if (wallBuilder != null) {
			computeScanRadius();
		}
	}

	private boolean isPointOfContact(Point p) {
		for (Wall w : wallBuilder.getWalls()) {
			if (w.intersects(p)) {
				return true;
			}
		}
		return false;
	}

	private void computeScanRadius() {
		scanRadius = new ArrayList<Point>();
		int i = 0;
		for (int theta = 0; theta < 360; theta = theta + DELTA_THETA) {
			double thetaR = Math.PI / 180.0d * theta;
			int x = (int) (range * Math.cos(thetaR));
			int y = (int) (range * Math.sin(thetaR));
			Point temp = new Point(origin.x + x, origin.y + y);
			if (isPointOfContact(temp)) {
				contacts.add(temp);
				logger.info(
						"Lidar reporting contact designate Id:: " + i++ + " bearing:: " + theta + " range:: " + range);
			}

			scanRadius.add(temp);
		}
	}
}
