package space.exploration.mars.rover.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.radar.PolarCoord;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.environment.RoverCell;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.kernel.Rover;
import space.exploration.mars.rover.utils.RadialContact;
import space.exploration.mars.rover.utils.RoverUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sanket on 5/30/17.
 */
public class Radar implements IsEquipment {
    public static final  String      RADAR_PREFIX   = "mars.rover.radar";
    private static final String      SCALE_FACTOR   = "mars.rover.radarContact.scaleFactor";
    private              int         lifeSpan       = 0;
    private              double      scaleFactor    = 0.0d;
    private              Point       origin         = null;
    private              Rover       rover          = null;
    private              boolean     endOfLife      = false;
    private              Logger      logger         = LoggerFactory.getLogger(Radar.class);
    private              List<Point> previousRovers = null;
    private              List<Point> relativeRovers = null;
    private              Point       center         = null;

    public Radar(Rover rover) {
        this.rover = rover;
        this.scaleFactor = Double.parseDouble(rover.getMarsConfig().getProperty(SCALE_FACTOR));
        this.lifeSpan = Integer.parseInt(rover.getMarsConfig().getProperty(RADAR_PREFIX + ".lifeSpan"));
        this.previousRovers = new ArrayList<>();
        this.relativeRovers = new ArrayList<>();

        int frameWidth = Integer.parseInt(rover.getMarsConfig().getProperty(EnvironmentUtils.FRAME_WIDTH_PROPERTY));
        this.center = new Point(frameWidth / 2, frameWidth / 2);
        this.origin = center;
        populateRoverPositions();
        RoverUtil.roverSystemLog(logger, "Radar configured:: ", "INFO");
    }

    public int getLifeSpan() {
        return lifeSpan;
    }

    public void setLifeSpan(int lifeSpan) {
        this.lifeSpan = lifeSpan;
    }

    public String getEquipmentName() {
        return "Radar";
    }

    @Override
    public boolean isEndOfLife() {
        return endOfLife;
    }

    @Override
    public long getRequestMetric() {
        return rover.getRadarScanningState().getRequests().count();
    }

    public void setEndOfLife(boolean endOfLife) {
        this.endOfLife = endOfLife;
    }

    public List<Point> getPreviousRovers() {
        return previousRovers;
    }

    public void setPreviousRovers(List<Point> previousRovers) {
        this.previousRovers = previousRovers;
    }

    public Point getOrigin() {
        return origin;
    }

    public List<Point> getRelativeRovers() {
        return relativeRovers;
    }

    public List<RadialContact> getRadialContacts() {
        if (previousRovers.isEmpty()) {
            logger.error("List of previous rovers is null!");
            rover.writeErrorLog("List of previous rovers is null", null);
            return null;
        }

        lifeSpan--;
        List<Point> temp = new ArrayList<>();
        for (Point contact : previousRovers) {
            logger.debug("Rover position at RadarScan =>" + rover.getMarsArchitect().getRobot().getLocation
                    ().toString());
            contact = transformPoint(rover.getMarsArchitect().getRobot().getLocation(), origin, contact);
            temp.add(contact);
        }

        scaleFactor = computeScaleFactor(temp);
        List<RadialContact> contacts = new ArrayList<>();
        relativeRovers.clear();
        for (Point contact : temp) {
            contact = scalePoint(origin, contact, scaleFactor);
            relativeRovers.add(contact);
            PolarCoord.PolarPoint.Builder pBuilder = PolarCoord.PolarPoint.newBuilder();
            pBuilder.setR(origin.distance(contact));
            pBuilder.setTheta(getTheta(origin, contact));

            RadialContact rContact = new RadialContact(origin, contact);
            rContact.setPolarPoint(pBuilder.build());
            contacts.add(rContact);
        }

        return contacts;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    private void populateRoverPositions() {
        Map<Point, RoverCell> oldRovers = EnvironmentUtils.setUpOldRovers(rover.getMarsConfig());
        previousRovers.addAll(oldRovers.keySet());
        rover.setPreviousRovers(oldRovers);
    }

    private double computeScaleFactor(List<Point> contacts) {
        double maxDistance = 0.0d;

        for (Point point : contacts) {
            double dist = point.distance(center);
            maxDistance = (dist > maxDistance) ? dist : maxDistance;
        }

        return ((double) (center.x - 10) / maxDistance);
    }

    private int getQuadrant(Point a, Point b) {
        if (b.getY() >= a.getY() && b.getX() >= a.getX()) {
            return 1;
        } else if (b.getY() >= a.getY() && b.getX() <= a.getX()) {
            return 2;
        } else if (b.getY() <= a.getY() && b.getX() <= a.getX()) {
            return 3;
        } else {
            return 4;
        }
    }

    /**
     * @param a
     * @param b
     * @return returns angle between a,b in degrees
     */
    private double getTheta(Point a, Point b) {
        double r     = a.distance(b);
        double theta = 0.0d;
        switch (getQuadrant(a, b)) {
            case 1: {
                theta = Math.acos((b.getX() - a.getX()) / r);
            }
            break;
            case 2: {
                theta = Math.acos((a.getX() - b.getX()) / r);
                theta = Math.PI - theta;
            }
            break;
            case 3: {
                theta = Math.acos((a.getX() - b.getX()) / r);
                theta += Math.PI;
            }
            break;
            default: {
                theta = Math.acos((b.getX() - a.getX()) / r);
                theta = (2.0d * Math.PI) - theta;
            }
        }
        return Math.toDegrees(theta);
    }

    private Point transformPoint(Point origin, Point center, Point contact) {
        int xDiff = center.x - origin.x;
        int yDiff = center.y - origin.y;

        return new Point(contact.x + xDiff, contact.y + yDiff);
    }

    private Point scalePoint(Point start, Point end, double scaleFactor) {
        double r = start.distance(end);
        r *= scaleFactor;

        double theta = getTheta(start, end);
        theta = Math.toRadians(theta);
        int quadrant = getQuadrant(start, end);

        int x = 0;
        int y = 0;
        switch (quadrant) {
            case 1: {
                x = (int) (start.x + r * Math.cos(theta));
                y = (int) (start.y + r * Math.sin(theta));
            }
            break;
            case 2: {
                x = (int) (start.x - r * Math.cos(Math.PI - theta));
                y = (int) (start.y + r * Math.sin(Math.PI - theta));
            }
            break;
            case 3: {
                x = (int) (start.x - r * Math.cos(theta - Math.PI));
                y = (int) (start.y - r * Math.sin(theta - Math.PI));
            }
            break;
            default: {
                x = (int) (start.x + r * Math.cos(2.0d * Math.PI - theta));
                y = (int) (start.y - r * Math.sin(2.0d * Math.PI - theta));
            }
        }
        return new Point(x, y);
    }

}
