/**
 * 
 */
package space.exploration.mars.rover.navigation;

import java.awt.Point;
import java.io.Serializable;
import java.util.List;

/**
 * @author sanketkorgaonkar
 *
 */
public class NavigationPath implements Serializable {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 66716259045931587L;
	private List<Point>			path				= null;

	public NavigationPath(List<Point> path) {
		this.path = path;
	}

	public List<Point> getPath() {
		return path;
	}
}
