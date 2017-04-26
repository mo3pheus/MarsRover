/**
 * 
 */
package space.exploration.mars.rover.animation;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import space.exploration.mars.rover.environment.Laser;
import space.exploration.mars.rover.sensor.Lidar;

/**
 * @author sanketkorgaonkar
 *
 */
public class LidarAnimationEngine {
	public static final Integer	LIDAR_DEPTH	= new Integer(101);
	private List<Point>			lidarRadius	= null;
	private List<Laser>			laserBeams	= null;
	private Point				origin		= null;
	private Properties			marsConfig	= null;

	public LidarAnimationEngine(Lidar lidar, Properties marsConfig) {
		lidarRadius = lidar.getScanRadius();
		this.origin = lidar.getOrigin();
		laserBeams = new ArrayList<Laser>();
		this.marsConfig = marsConfig;
		generateLaserBeams();
	}

	private void generateLaserBeams() {
		for (Point p : lidarRadius) {
			Laser laser = new Laser(origin, p, marsConfig);
			laserBeams.add(laser);
		}
	}

	public List<Laser> getLaserBeams() {
		return laserBeams;
	}
}
