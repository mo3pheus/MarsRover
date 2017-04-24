package space.exploration.mars.rover.environment;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Grid extends VirtualElement {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 4717059892660341230L;
	private static final String	GRID_COLOR			= "maze.environment.grid.color";
	private List<Line>			gridLines			= null;
	private Properties			matrixConfig		= null;

	private class Line {
		public Point	x;
		public Point	y;

		public Line(int x1, int y1, int x2, int y2) {
			x = new Point(x1, y1);
			y = new Point(x2, y2);
		}
	}

	public Grid(Properties matrixConfig) {
		this.matrixConfig = matrixConfig;
		build();
	}

	public void build() {
		super.setMatrixConfig(matrixConfig);
		super.setLayout();
		gridLines = new ArrayList<Line>();

		int frameHeight = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.FRAME_HEIGHT_PROPERTY));
		int frameWidth = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.FRAME_WIDTH_PROPERTY));
		int cellWidth = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));

		for (int i = 0; i < frameHeight; i = i + cellWidth) {
			gridLines.add(new Line(i, 0, i, frameHeight));
			gridLines.add(new Line(0, i, frameWidth, i));
		}
	}

	@Override
	public Color getColor() {
		return EnvironmentUtils.findColor(matrixConfig.getProperty(GRID_COLOR));
	}

	@Override
	public void draw(Graphics2D g2) {
		g2.setColor(getColor());
		for (int i = 0; i < gridLines.size(); i++) {
			g2.drawLine(gridLines.get(i).x.x, gridLines.get(i).x.y, gridLines.get(i).y.x, gridLines.get(i).y.y);
		}
	}

	@Override
	public void paint(Graphics g) {
		draw((Graphics2D) g);
	}
}
