package space.exploration.mars.rover.sensor;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import space.exploration.mars.rover.environment.Wall;
import space.exploration.mars.rover.environment.WallBuilder;

public class Lidar {
	private static final int DELTA_THETA = 5;

	private WallBuilder	wallBuilder	= null;
	private Point[]		sweepPoints	= null;
	private List<Point>	contacts	= null;
	private List<Point>	scanRadius	= null;
	private Point		origin		= null;
	private double		radius		= 0.0d;
	private int			range		= 0;

	public Lidar(Point origin, int range) {
		sweepPoints = new Point[8];
		contacts = new ArrayList<Point>();
		this.origin = origin;
		this.range = range;
		this.radius = Math.sqrt((2.0d * range * range));
		computeScanRadius();
	}

	public void performSweep() {
		int count = 0;
		for (int x = origin.x - range; x <= (origin.x + range); x = x + range) {
			for (int y = origin.y - range; y <= (origin.y + range); y = y + range) {
				
				Point temp = new Point(x, y);
				System.out.println(" Point under eval = " + temp);
				if (temp.equals(origin)) {
					continue;
				} else {
					sweepPoints[count] = temp;
					for (Wall w : wallBuilder.getWalls()) {
						if (w.intersects(temp)) {
							contacts.add(temp);
						}
					}
					count++;
				}
			}
		}
	}

	public void setWallBuilder(WallBuilder wallBuilder) {
		this.wallBuilder = wallBuilder;
	}

	public List<Point> getContacts() {
		return contacts;
	}

	public Point[] sweepPoints() {
		return sweepPoints;
	}

	public List<Point> getScanRadius() {
		return scanRadius;
	}

	public Point getOrigin() {
		return origin;
	}

	private void computeScanRadius() {
		scanRadius = new ArrayList<Point>();
		for (int theta = 0; theta < 360; theta = theta + DELTA_THETA) {
			double thetaR = Math.PI/180.0d * theta;
			int x = (int) (radius * Math.cos(thetaR));
			int y = (int) (radius * Math.sin(thetaR));
			Point temp = new Point(origin.x + x, origin.y + y);
			System.out.println("Scan Points = " + temp);
			scanRadius.add(temp);
		}
	}
}
