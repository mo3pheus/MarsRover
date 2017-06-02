package space.exploration.mars.rover.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.environment.RoverCell;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.kernel.Rover;
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
    private             int         radarRadius    = 0;
    private             int         lifeSpan       = 0;
    private             Point       origin         = null;
    private             Rover       rover          = null;
    private             boolean     endOfLife      = false;
    private             Logger      logger         = LoggerFactory.getLogger(Radar.class);
    private             List<Point> previousRovers = null;

    public Radar(Rover rover) {
        this.rover = rover;
        this.lifeSpan = Integer.parseInt(rover.getMarsConfig().getProperty(RADAR_PREFIX + ".lifeSpan"));
        this.previousRovers = new ArrayList<>();
        this.origin = rover.getMarsArchitect().getRobot().getLocation();

        int frameWidth = Integer.parseInt(rover.getMarsConfig().getProperty(EnvironmentUtils.FRAME_WIDTH_PROPERTY));
        this.radarRadius = (int) (Math.sqrt(Math.pow(frameWidth, 2.0d)));
        populateRoverPositions();
        RoverUtil.roverSystemLog(logger, "Radio configured:: ", "INFO");
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

    public int getRadarRadius() {
        return radarRadius;
    }

    public Point getOrigin() {
        return origin;
    }

    private void populateRoverPositions() {
        Map<Point, RoverCell> oldRovers = EnvironmentUtils.setUpOldRovers(rover.getMarsConfig());
        previousRovers.addAll(oldRovers.keySet());
    }
}
