package space.exploration.mars.rover.propulsion;

import space.exploration.mars.rover.kernel.Rover;
import space.exploration.mars.rover.navigation.NavCell;
import space.exploration.mars.rover.navigation.NavUtil;
import space.exploration.mars.rover.navigation.NavigationEngine;

import java.awt.*;

/**
 * Created by sanket on 5/28/17.
 */
public class AStarPropulsionUnit extends PropulsionUnit {
    public AStarPropulsionUnit(Rover rover, Point source, Point destination) {
        super(rover,source,destination);
        requestPropulsion();
    }

    @Override
    void requestPropulsion() {
        NavigationEngine navigationEngine = rover.getNavigationEngine();
        NavCell start = navigationEngine.getGridMap().get(NavUtil.findNavId(navigationEngine
                                                                                    .getGridMap()
                , source));
        NavCell dest = navigationEngine.getGridMap().get(NavUtil.findNavId(navigationEngine
                                                                                   .getGridMap()
                , destination));
        trajectory = navigationEngine.navigate(start, dest);
        computeTrajValidity();
    }
}
