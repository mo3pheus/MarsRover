/**
 * 
 */
package space.exploration.mars.rover.animation;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.environment.Laser;
import space.exploration.mars.rover.environment.WallBuilder;
import space.exploration.mars.rover.sensor.Lidar;

/**
 * @author sanketkorgaonkar
 *
 */
public class LidarAnimationEngine {
	public static final Integer	LIDAR_DEPTH	= new Integer(101);
	private JFrame				marsSurface	= null;
	private List<Point>			lidarRadius	= null;
	private List<Laser>			laserBeams	= null;
	private Point				origin		= null;
	private Properties			marsConfig	= null;
	private Lidar				lidar		= null;
	private Cell				robot		= null;
	private Logger				logger		= LoggerFactory.getLogger(LidarAnimationEngine.class);

	public LidarAnimationEngine(Properties marsConfig, Cell robot) {
		laserBeams = new ArrayList<Laser>();
		this.marsConfig = marsConfig;
		this.robot = robot;
	}

	public void setLidar(Lidar lidar) {
		lidarRadius = lidar.getScanRadius();
		this.origin = lidar.getOrigin();
		this.lidar = lidar;
		generateLaserBeams();
	}

	public void setMarsSurface(JFrame surface) {
		this.marsSurface = surface;
	}
	
	public Cell getRobot(){
		return robot;
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

	public void activateLidar() {
		if (lidar == null || marsSurface == null) {
			logger.error("LidarAnimationEngine error - either lidar or marsSurface is null");
		}

		lidar.scanArea();
		renderLidarAnimation();
	}

	public void renderLidarAnimation() {
		int delayMs = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.LIDAR_ANIMATION_SCAN_DELAY));
		JLayeredPane contentPane = AnimationUtil.getContent(marsConfig);
		contentPane.add(robot, Cell.ROBOT_DEPTH);
		for (Laser laser : getLaserBeams()) {
			contentPane.add(laser, LidarAnimationEngine.LIDAR_DEPTH);
			marsSurface.setContentPane(contentPane);
			marsSurface.setVisible(true);
			try {
				Thread.sleep(delayMs);
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
			contentPane.remove(laser);
		}
	}
}
