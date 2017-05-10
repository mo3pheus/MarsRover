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

import space.exploration.mars.rover.animation.AnimationUtil;
import space.exploration.mars.rover.animation.LidarAnimationEngine;
import space.exploration.mars.rover.bootstrap.MatrixCreation;
import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.environment.Grid;
import space.exploration.mars.rover.environment.Laser;
import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.environment.WallBuilder;
import space.exploration.mars.rover.sensor.Lidar;

/**
 * @author sanketkorgaonkar
 *
 */
public class LidarTest {

	Lidar			lidar			= null;
	Properties		matrixConfig	= null;
	MarsArchitect	marsArchitect	= null;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		matrixConfig = MatrixCreation.getMatrixConfig();
		marsArchitect = new MarsArchitect(matrixConfig);
		int cellWidth = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));
		lidar = new Lidar(new Point(75, 75), cellWidth, cellWidth);
		lidar.setWallBuilder(new WallBuilder(matrixConfig));
		lidar.scanArea();
		marsArchitect.setLidarAnimationEngine(lidar);
		marsArchitect.getLidarAnimationEngine().activateLidar();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void test() {
		assertEquals(72, lidar.getScanRadius().size());
	}
}
