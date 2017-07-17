package space.exploration.mars.rover.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.environment.RoverCell;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.kernel.Rover;
import space.exploration.mars.rover.radar.PolarCoord;
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
    public static final String      RADAR_PREFIX   = "mars.rover.radar";
    private             int         lifeSpan       = 0;
    private             Point       origin         = null;
    private             Rover       rover          = null;
    private             boolean     endOfLife      = false;
    private             Logger      logger         = LoggerFactory.getLogger(Radar.class);
    private             List<Point> previousRovers = null;
    private             Point       center         = null;

    public Radar(Rover rover) {
        this.rover = rover;
        this.lifeSpan = Integer.parseInt(rover.getMarsConfig().getProperty(RADAR_PREFIX + ".lifeSpan"));
        this.previousRovers = new ArrayList<>();

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

    private void populateRoverPositions() {
        Map<Point, RoverCell> oldRovers = EnvironmentUtils.setUpOldRovers(rover.getMarsConfig());
        previousRovers.addAll(oldRovers.keySet());
        rover.setPreviousRovers(oldRovers);
    }

    public List<Point> getContacts() {
        lifeSpan--;
        return new ArrayList<Point>();
    }

    public List<RadialContact> getRadialContacts() {
        if (previousRovers.isEmpty()) {
            logger.error("List of previous rovers is null!");
            return null;
        }

        lifeSpan--;
        List<RadialContact> contacts = new ArrayList<>();
        for (Point contact : previousRovers) {
            RadialContact rContact = new RadialContact(origin, contact);

            PolarCoord.PolarPoint.Builder pBuilder = PolarCoord.PolarPoint.newBuilder();
            pBuilder.setR(origin.distance(contact));
            pBuilder.setTheta(getTheta(origin, contact));
            rContact.setPolarPoint(pBuilder.build());
            contacts.add(rContact);
        }

        return contacts;
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
}
