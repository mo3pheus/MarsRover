package space.exploration.mars.rover.environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Properties;

public class Cell extends VirtualElement {
    public static final  String     CELL_WIDTH       = "maze.environment.cell.width";
    public static final  String     CELL_LOCATION    = "maze.environment.destination.position";
    public static final  String     CELL_COLOR       = "maze.environment.destination.color";
    public static final  Integer    ROBOT_DEPTH      = new Integer(100);
    /**
     *
     */
    private static final long       serialVersionUID = 185332095563001046L;
    private              Color      cellColor        = null;
    private              Point      location         = null;
    private              Properties matrixConfig     = null;
    private              Logger     logger           = LoggerFactory.getLogger(Cell.class);
    private int cellWidth;

    public Cell(Properties matrixConfig) {
        this.matrixConfig = matrixConfig;
        build();
    }

    private Rectangle2D getCell() {
        return new Rectangle2D.Double(location.getX(), location.getY(), (double) cellWidth, (double) cellWidth);
    }

    public void build() {
        super.setMatrixConfig(matrixConfig);
        super.setLayout();
        cellWidth = Integer.parseInt(matrixConfig.getProperty(CELL_WIDTH));
        location = new Point(Integer.parseInt(matrixConfig.getProperty(CELL_LOCATION).split(",")[0]),
                Integer.parseInt(matrixConfig.getProperty(CELL_LOCATION).split(",")[1]));
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public int getCellWidth() {
        return cellWidth;
    }

    public void setCellWidth(int cellWidth) {
        this.cellWidth = cellWidth;
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setColor((cellColor == null) ? getColor() : cellColor);
        g2.fill(getCell());
    }

    @Override
    public Color getColor() {
        return EnvironmentUtils.findColor(matrixConfig.getProperty(CELL_COLOR));
    }

    public void setColor(Color cellColor) {
        if (cellColor == EnvironmentUtils.findColor("lavendar")) {
            logger.info("Location = " + location.toString());
        }
        this.cellColor = cellColor;
    }

    @Override
    public void paint(Graphics g) {
        draw((Graphics2D) g);
    }
}
