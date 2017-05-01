package space.exploration.mars.rover.sensor;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private Point[]		gridCells	= new Point[8];

	private double	range		= 0;
	private String	status		= "";
	private int		cellWidth	= 0;

	public Lidar(Point origin, int range, int cellWidth) {
		contacts = new ArrayList<Point>();
		this.origin = origin;
		this.range = range;
		this.range = Math.sqrt((2.0d * range * range));
		logger.info("Lidar instantiated");
		status = "Instantiated";
		this.cellWidth = cellWidth;
		fillGridCells();
	}

	public void setWallBuilder(WallBuilder wallBuilder) {
		logger.info("Wallbuilder set for lidar");
		status += "\nWallBuilder set for lidar";
		this.wallBuilder = wallBuilder;

		scanGridCells();
	}

	public List<Point> getContacts() {
		if (wallBuilder == null) {
			logger.error("WallBuilder not set before asking for contacts");
			return null;
		}
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

	public String getStatus() {
		return status;
	}

	private void scanGridCells() {
		int i = 0;
		for (Point p : gridCells) {
			if (isPointOfContact(p)) {
				contacts.add(p);
				status += " Contact " + i++ + "::" + p.toString();
			}
		}
		logger.info(" Scanning at location = " + this.origin + " Length of contacts  = " + contacts.size());
	}

	private void fillGridCells() {
		int index = 0;
		for (int i = (origin.x - cellWidth); i <= (origin.x + cellWidth); i = i + cellWidth) {
			for (int j = origin.y - cellWidth; j <= (origin.y + cellWidth); j = j + cellWidth) {
				if (i < 0 || j < 0 || (new Point(i, j)).equals(origin)) {
					continue;
				} else {
					gridCells[index++] = new Point(i, j);
				}
			}
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
				logger.info(
						"Lidar reporting contact designate Id:: " + i++ + " bearing:: " + theta + " range:: " + range);
			}

			scanRadius.add(temp);
		}
	}
}
