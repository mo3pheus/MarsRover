/**
 *
 */
package space.exploration.mars.rover.environment;

import space.exploration.mars.rover.kernel.ModuleDirectory;
import space.exploration.mars.rover.sensor.Radar;
import space.exploration.mars.rover.utils.RadialContact;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.Properties;

/**
 * @author sanketkorgaonkar
 */
public class Laser extends VirtualElement implements Comparable<Laser> {

    /**
     *
     */
    private static final long   serialVersionUID = -8465625316296649774L;
    private              double angle            = 0.0d;
    private              Point  startPoint       = null;
    private              Point  endPoint         = null;
    private              Color  color            = Color.ORANGE;

    private Line2D laserBeam;

    public Laser(Point a, Point b, Properties marsConfig, ModuleDirectory.Module module) {
        startPoint = a;
        endPoint = b;
        laserBeam = new Line2D.Double(a, b);

        if (module == ModuleDirectory.Module.SENSOR_LIDAR) {
            color = EnvironmentUtils.findColor(marsConfig.getProperty("mars.rover.lidar.color", "green"));
        } else if (module == ModuleDirectory.Module.RADAR) {
            color = EnvironmentUtils.findColor(marsConfig.getProperty(Radar.RADAR_PREFIX + ".laser.color"));
        }

        super.setMatrixConfig(marsConfig);
        super.setLayout();
    }

    public Line2D getBeam() {
        return laserBeam;
    }

    @Override
    public void paint(Graphics g) {
        draw((Graphics2D) g);
    }

    public String toString() {
        return (" Line going from point " + laserBeam.getX1() + "," + laserBeam.getY1() + " to point "
                + laserBeam.getX2() + "," + laserBeam.getY2());
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(color);
        g2.draw(laserBeam);
    }

    @Override
    public void build() {
    }

    @Override
    public Color getColor() {
        return color;
    }

    public RadialContact getPolarCoordinate() {
        return new RadialContact(startPoint, endPoint);
    }

    @Override
    public int compareTo(Laser laser) {
        if( this.getAngle() > laser.getAngle() ){
            return 1;
        } else if(this.getAngle() < laser.getAngle() ){
            return -1;
        } else {
            return 0;
        }
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
}
