package space.exploration.mars.rover.environment;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Properties;

/**
 * Created by sanket on 6/1/17.
 */
public class RadarScanArea extends VirtualElement {
    public static final int        RING_OFFSET = 5;
    private             Properties radarConfig = null;
    private             double     diameter    = 0.0d;
    private             int        cellWidth   = 0;
    private             Point      center      = null;

    public RadarScanArea(Properties radarConfig, int windowWidth, Point center) {
        this.radarConfig = radarConfig;
        diameter = windowWidth;
        cellWidth = 4 * Integer.parseInt(this.radarConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));
        int numRings = (int) (diameter / (double) (cellWidth));
        this.center = center;
        build();
    }

    @Override
    public void draw(Graphics2D g2) {
        //draw square of size d.
        g2.setColor(Color.BLACK);
        g2.fill(new Rectangle2D.Double(0, 0, diameter, diameter));

        g2.setColor(EnvironmentUtils.findColor("darkGreen"));
        int sqrWidth = (int) (diameter - (2 * RING_OFFSET));
        int x        = RING_OFFSET;
        int y        = RING_OFFSET;
        while (sqrWidth > 0) {
            g2.drawOval(x, y, sqrWidth, sqrWidth);
            x += (int) (cellWidth * 0.5d);
            y += (int) (cellWidth * 0.5d);
            sqrWidth -= (cellWidth);
        }

        double theta = 0.0d;
        while (theta < 360.0d) {
            double thetaR = Math.PI / 180.0d * theta;
            theta += 45;
            int   laserRadius = (int) (diameter - RING_OFFSET - RING_OFFSET);
            int   xr          = (int) (0.5d * laserRadius * Math.cos(thetaR));
            int   yr          = (int) (0.5d * laserRadius * Math.sin(thetaR));
            Point temp        = new Point(center.x + xr, center.y + yr);
            g2.drawLine(center.x, center.y, temp.x, temp.y);
        }
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public void build() {
        this.setMatrixConfig(radarConfig);
        this.setLayout();
    }

    @Override
    public void paint(Graphics g) {
        draw((Graphics2D) g);
    }

}
