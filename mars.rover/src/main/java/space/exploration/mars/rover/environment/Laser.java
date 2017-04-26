/**
 * 
 */
package space.exploration.mars.rover.environment;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.Properties;

/**
 * @author sanketkorgaonkar
 *
 */
public class Laser extends VirtualElement {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -8465625316296649774L;
	private Point				startPoint			= null;
	private Point				endPoint			= null;
	private Color				color				= Color.ORANGE;

	private Line2D laserBeam;

	public Laser(Point a, Point b, Properties marsConfig) {
		startPoint = a;
		endPoint = b;
		laserBeam = new Line2D.Double(a, b);
		color = EnvironmentUtils.findColor(marsConfig.getProperty("mars.rover.lidar.color", "Black"));

		super.setMatrixConfig(marsConfig);
		super.setLayout();
	}

	public Line2D getBeam() {
		return laserBeam;
	}

	@Override
	public void paint(Graphics g) {
		draw((Graphics2D) g);
	}

	public String toString() {
		return (" Line going from point " + laserBeam.getX1() + "," + laserBeam.getY1() + " to point "
				+ laserBeam.getX2() + "," + laserBeam.getY2());
	}

	@Override
	public void draw(Graphics2D g2) {
		g2.setColor(color);
		g2.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
	}

	@Override
	public void build() {
	}

	@Override
	public Color getColor() {
		return color;
	}
}
