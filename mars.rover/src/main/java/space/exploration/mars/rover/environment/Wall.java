package space.exploration.mars.rover.environment;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Wall extends VirtualElement {
    private static final long serialVersionUID = 4408125108099073421L;

    private int[]      definition   = new int[4];
    private Properties matrixConfig = null;
    private int frameWidth;
    private int frameHeight;
    private int cellWidth;
    private Color wallColor = null;

    public Wall(Properties matrixConfig) {
        this.matrixConfig = matrixConfig;
        build();
    }

    private Rectangle2D getWall() {
        return new Rectangle2D.Double((double) definition[0], (double) definition[1], (double) definition[2],
                (double) definition[3]);
    }

    public int[] getDefinition() {
        return definition;
    }

    public String toString() {
        return " Origin at " + definition[0] + "," + definition[1] + " width = " + definition[2] + " height = "
                + definition[3];
    }

    /**
     * This function returns a list of points that make up the wall.
     *
     * @return
     */
    public List<Point> getWallPoints() {
        List<Point> wallPoints = new ArrayList<Point>();
        Point       wallStart  = new Point(definition[0], definition[1]);
        int         hCells     = definition[3] / cellWidth;
        int         wCells     = definition[2] / cellWidth;

        for (int i = 0; i < hCells; i++) {
            for (int j = 0; j < wCells; j++) {
                Point temp = new Point(wallStart.x + (j * cellWidth), wallStart.y + (i * cellWidth));
                wallPoints.add(temp);
            }
        }

        return wallPoints;
    }

    public boolean intersects(Point p) {
        Rectangle2D cell = new Rectangle2D.Double(p.x, p.y, cellWidth, cellWidth);
        Rectangle2D wl   = getWall();
        return cell.intersects(wl);
    }

    public void setDefinition(int[] definition, String color) throws IllegalWallDefinitionException {
        if (definition.length == 4 || definition[0] + definition[3] >= frameWidth
                || definition[1] + definition[2] >= frameHeight) {
            this.definition = definition;
        } else {
            throw new IllegalWallDefinitionException(
                    " Invalid definition passed in. definition[] should be of length = 4"
                            + " \n Format is as follows: 0,1 are x0,y0 point of origin, 2,3 are height, width of the " +
                            "wall");
        }
        this.wallColor = EnvironmentUtils.findColor(color);
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(getColor());
        g2.fill(getWall());
    }

    @Override
    public void build() {
        this.frameHeight = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.FRAME_HEIGHT_PROPERTY));
        this.frameWidth = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.FRAME_WIDTH_PROPERTY));
        this.cellWidth = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));
    }

    @Override
    public Color getColor() {
        return wallColor;
    }

    public static class IllegalWallDefinitionException extends Exception {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public IllegalWallDefinitionException(String message) {
            super(message);
        }
    }
}
