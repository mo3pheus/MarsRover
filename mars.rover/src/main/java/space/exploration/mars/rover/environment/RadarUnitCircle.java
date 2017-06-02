package space.exploration.mars.rover.environment;

import space.exploration.mars.rover.sensor.Radar;

import java.awt.*;
import java.util.Properties;

/**
 * Created by sanket on 6/1/17.
 */
public class RadarUnitCircle extends VirtualElement {
    private Properties config           = null;
    private double     radarScaleFactor = 0.0d;
    private double     scanRadius       = 0.0d;

    public RadarUnitCircle(double scanRadius, Properties config) {
        this.config = config;
        this.radarScaleFactor = Double.parseDouble(config.getProperty(Radar.RADAR_PREFIX + ".scaleFactor"));
        this.scanRadius = radarScaleFactor * scanRadius;
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.fillOval(0, 0, (int) scanRadius, (int) scanRadius);
    }

    @Override
    public Color getColor() {
        return EnvironmentUtils.findColor(config.getProperty(EnvironmentUtils.RADAR_SCAN_CIRCLE_COLOR));
    }

    @Override
    public void build() {
        super.setMatrixConfig(config);
        super.setLayout();
    }

    @Override
    public void paint(Graphics g) {
        draw((Graphics2D) g);
    }
}
