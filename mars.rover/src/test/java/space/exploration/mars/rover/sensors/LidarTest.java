/**
 * 
 */
package space.exploration.mars.rover.sensors;

import static org.junit.Assert.*;

import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import space.exploration.mars.rover.animation.TrackingAnimationEngine;
import space.exploration.mars.rover.bootstrap.MatrixCreation;
import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.environment.Grid;
import space.exploration.mars.rover.environment.Laser;
import space.exploration.mars.rover.environment.WallBuilder;
import space.exploration.mars.rover.sensor.Lidar;

/**
 * @author sanketkorgaonkar
 *
 */
public class LidarTest {

	Lidar		lidar			= null;
	Properties	matrixConfig	= null;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		lidar = new Lidar(new Point(75, 75), 25);
		matrixConfig = MatrixCreation.getMatrixConfig();
		lidar.setWallBuilder(new WallBuilder(matrixConfig));
		lidar.performSweep();
		for (Point p : lidar.getContacts()) {
			System.out.println(" Contacts = " + p);
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void test() {
		assertEquals(lidar.getScanRadius().size(), 72);

		JFrame frame = new JFrame();
		frame.setSize(700, 700);
		JLayeredPane content = getContent();
		
		for (Point dest : lidar.getScanRadius()) {
			Laser laser = new Laser(new Point(75, 75), dest, matrixConfig);
			content.add(laser, new Integer(101));
			try {
				Thread.sleep(40);
				frame.setContentPane(content);
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
			content.remove(laser);
		}

	}

	private JLayeredPane getContent() {
		JLayeredPane matrixPane = new JLayeredPane();
		List<Component> content = new ArrayList<Component>();

		Grid grid = new Grid(matrixConfig);
		WallBuilder wallBuilder = new WallBuilder(matrixConfig);
		Cell destinationCell = new Cell(matrixConfig);
		Cell startingCell = TrackingAnimationEngine.getStartLocation(matrixConfig);

		content.add(grid);
		content.add(wallBuilder);
		content.add(startingCell);
		content.add(destinationCell);

		for (int i = 0; i < content.size(); i++) {
			matrixPane.add(content.get(i), new Integer(i));
		}

		return matrixPane;
	}
}
