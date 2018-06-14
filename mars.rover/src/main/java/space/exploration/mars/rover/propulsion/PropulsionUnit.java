package space.exploration.mars.rover.propulsion;

import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.kernel.Rover;

import java.awt.*;

public abstract class PropulsionUnit {
    Rover                 rover                   = null;
    int                   powerConsumptionPerUnit = 0;
    Point                 source                  = null;
    Point                 destination             = null;
    java.util.List<Point> trajectory              = null;
    boolean               trajectoryValid         = false;

    public PropulsionUnit(Rover rover, Point source, Point destination) {
        this.rover = rover;
        this.source = source;
        this.destination = destination;
        powerConsumptionPerUnit = Integer.parseInt(rover.getRoverConfig().getMarsConfig().getProperty(EnvironmentUtils
                                                                                                              .PROPULSION_POWER_CONSUMPTION));
    }

    public int getPowerConsumptionPerUnit() {
        return powerConsumptionPerUnit;
    }

    public java.util.List<Point> getTrajectory() {
        return trajectory;
    }

    public boolean isTrajectoryValid() {
        return trajectoryValid;
    }

    abstract void requestPropulsion();

    protected void computeTrajValidity() {
        int trajectoryLength = trajectory.size();
        trajectoryValid = (trajectoryLength > 1) ? (trajectory.get(trajectoryLength - 1).equals(destination)) :
                trajectory.get(0).equals(destination);
    }
}
