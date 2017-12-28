package space.exploration.mars.rover.sensor;

import communications.protocol.ModuleDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.environment.Wall;
import space.exploration.mars.rover.environment.WallBuilder;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.kernel.Rover;
import space.exploration.mars.rover.utils.RoverUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Lidar implements IsEquipment {
    public static final  String LIFESPAN       = "mars.rover.lidar.lifeSpan";
    private static final int    DELTA_THETA    = 5;
    private static final int    NUM_LAST_SCANS = 100;

    private Logger      logger      = LoggerFactory.getLogger(Lidar.class);
    private Rover       rover       = null;
    private WallBuilder wallBuilder = null;
    private List<Point> contacts    = null;
    private List<Point> scanRadius  = null;
    private Point       origin      = null;
    private Point[]     gridCells   = new Point[8];

    private double  range            = 0;
    private String  status           = "";
    private int     cellWidth        = 0;
    private int     lifeSpan         = 0;
    private int     powerConsumption = 0;
    private boolean endOfLife        = false;

    public Lidar(Point origin, int range, int cellWidth, Rover rover) {
        this.rover = rover;
        contacts = new ArrayList<Point>();
        this.origin = origin;
        this.range = Math.sqrt((2.0d * range * range));

        RoverUtil.roverSystemLog(logger, "Lidar initialized and ready!", "INFO");
        status = "Instantiated";
        this.cellWidth = cellWidth;
        this.powerConsumption = Integer.parseInt(rover.getMarsConfig().getProperty(EnvironmentUtils
                                                                                           .LIDAR_POWER_CONSUMPTION));
        fillGridCells();
    }

    public void setWallBuilder(WallBuilder wallBuilder) {
        logger.debug("Wallbuilder set for lidar");
        status += "\nWallBuilder set for lidar";
        this.wallBuilder = wallBuilder;

        scanGridCells();
    }

    public List<Point> getContacts() {
        if (wallBuilder == null) {
            logger.error("WallBuilder not set before asking for contacts");
            rover.writeErrorLog("WallBuilder not set before asking for contacts", null);
            return null;
        }
        return contacts;
    }


    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }

    public int getCellWidth() {
        return cellWidth;
    }

    public void setCellWidth(int cellWidth) {
        this.cellWidth = cellWidth;
    }

    public List<Point> getScanRadius() {
        return scanRadius;
    }

    public Point getOrigin() {
        return origin;
    }

    public void setOrigin(Point origin) {
        this.origin = origin;
    }

    public void scanArea() {
        if (lifeSpan <= 0) {
            return;
        }

        if (lifeSpan < NUM_LAST_SCANS && !endOfLife) {
            endOfLife = true;
            rover.setEquipmentEOL(endOfLife);
            rover.authorizeTransmission(ModuleDirectory.Module.SENSOR_LIDAR, RoverUtil.getEndOfLifeMessage
                    (ModuleDirectory.Module.SENSOR_LIDAR, " Lidar at end of life. Num of scans remaining = " +
                            NUM_LAST_SCANS + " Please confirm scan command. Next " +
                            "command will be honored!", rover).toByteArray());
            return;
        }

        if (wallBuilder != null) {
            computeScanRadius();
            lifeSpan--;
        }
    }

    public boolean isEndOfLife() {
        return endOfLife;
    }

    @Override
    public long getRequestMetric() {
        return rover.getSensingState().getRequests().count();
    }

    public void setEndOfLife(boolean endOfLife) {
        this.endOfLife = endOfLife;
    }

    public String getStatus() {
        return status;
    }

    public int getLifeSpan() {
        return lifeSpan;
    }

    public void setLifeSpan(int lifeSpan) {
        this.lifeSpan = lifeSpan;
    }

    @Override
    public String getEquipmentName() {
        return "Lidar";
    }

    private void scanGridCells() {
        int i = 0;
        for (Point p : gridCells) {
            if (p != null && isPointOfContact(p)) {
                contacts.add(p);
                status += " Contact " + i++ + "::" + p.toString();
            }
        }
        logger.debug(" Scanning at location = " + this.origin + " Length of contacts  = " + contacts.size());
    }

    private void fillGridCells() {
        int index = 0;
        for (int i = (origin.x - cellWidth); i <= (origin.x + cellWidth); i = i + cellWidth) {
            for (int j = origin.y - cellWidth; j <= (origin.y + cellWidth); j = j + cellWidth) {
                if (i < 0 || j < 0 || (new Point(i, j)).equals(origin)) {
                    continue;
                } else {
                    gridCells[index++] = new Point(i, j);
                }
            }
        }
    }

    private boolean isPointOfContact(Point p) {
        for (Wall w : wallBuilder.getWalls()) {
            if (w.intersects(p)) {
                return true;
            }
        }
        return false;
    }

    private void computeScanRadius() {
        scanRadius = new ArrayList<Point>();
        int i = 0;
        for (int theta = 0; theta < 360; theta = theta + DELTA_THETA) {
            double thetaR = Math.PI / 180.0d * theta;
            int    x      = (int) (range * Math.cos(thetaR));
            int    y      = (int) (range * Math.sin(thetaR));
            Point  temp   = new Point(origin.x + x, origin.y + y);
            if (isPointOfContact(temp)) {
                logger.debug(
                        "Lidar reporting contact designate Id:: " + i++ + " bearing:: " + theta + " range:: " + range);
            }

            scanRadius.add(temp);
        }
    }
}
