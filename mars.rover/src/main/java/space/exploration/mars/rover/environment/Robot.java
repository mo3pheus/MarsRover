package space.exploration.mars.rover.environment;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.imageio.ImageIO;

public class Robot extends VirtualElement {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8487913539143758790L;
	private static final String roboImageLocation = "robot-tool.png";
	private Properties			matrixConfig		= null;
	private Point				location			= null;
	private BufferedImage		robotImage			= null;

	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public Robot(Properties matrixConfig) {
		this.matrixConfig = matrixConfig;
		build();
	}

	@Override
	public void build() {
		super.setMatrixConfig(matrixConfig);
		super.setLayout();
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(Robot.class.getClassLoader().getResource(roboImageLocation).getPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		robotImage = (BufferedImage) img;

		location = new Point(
				Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.ROBOT_START_LOCATION).split(",")[0]),
				Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.ROBOT_START_LOCATION).split(",")[1]));
	}

	@Override
	public Color getColor() {
		return null;
	}

	@Override
	public void draw(Graphics2D g2) {
		AffineTransform at = new AffineTransform();
		int cellWidth = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));
		at.scale(cellWidth, cellWidth);
		g2.drawImage(robotImage, null, location.x, location.y);
	}

	@Override
	public void paint(Graphics g) {
		draw((Graphics2D) g);
	}
}
