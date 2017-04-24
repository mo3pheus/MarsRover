package space.exploration.mars.rover.navigation;

import java.awt.Point;
import java.util.List;

public interface PerformsNavigation {
	List<Point> navigate(NavCell start, NavCell end);
}
