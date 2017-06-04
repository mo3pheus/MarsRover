package space.exploration.mars.rover.animation;

import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.environment.Laser;
import space.exploration.mars.rover.environment.RadarContactCell;
import space.exploration.mars.rover.environment.RadarScanArea;
import space.exploration.mars.rover.kernel.ModuleDirectory;
import space.exploration.mars.rover.sensor.Radar;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by sanket on 5/30/17.
 */
public class RadarAnimationEngine {
    private static final Integer     RADAR_DEPTH = new Integer(0);
    private static final int         LASER_DELAY = 40;
    private              JFrame      radarWindow = null;
    private              int         numRings    = 0;
    private              int         laserRadius = 0;
    private              double      scaleFactor = 0.0d;
    private              double      windowWidth = 0.0d;
    private              Properties  radarConfig = null;
    private              Point       origin      = null;
    private              List<Laser> laserBeams  = null;

    public RadarAnimationEngine(Properties radarConfig) {
        this.radarConfig = radarConfig;
        laserBeams = new ArrayList<>();
    }

    public void setUpRadarWindow() {
        int fWidth = Integer.parseInt(radarConfig.getProperty(EnvironmentUtils.FRAME_WIDTH_PROPERTY));
        scaleFactor = Double.parseDouble(radarConfig.getProperty(Radar.RADAR_PREFIX + ".scaleFactor"));
        windowWidth = scaleFactor * fWidth;
        laserRadius = (int) (windowWidth - (2 * RadarScanArea.RING_OFFSET));
        radarWindow = new JFrame();
        radarWindow.setSize((int) windowWidth, (int) windowWidth);
        radarWindow.setVisible(true);
        radarWindow.setTitle("Radar Scan Window");
        int originCo = (int) ((windowWidth) / 2.0d);
        origin = new Point(originCo, originCo);
    }

    private RadarContactCell[] getContactPing(Point location) {
        RadarContactCell[] ping = new RadarContactCell[EnvironmentUtils.RADAR_CONTACT_COLORS.length];

        for (int i = 0; i < ping.length; i++) {
            ping[i] = new RadarContactCell(radarConfig, new Point(location.x + i, location.y + i), EnvironmentUtils
                    .RADAR_CONTACT_COLORS[i]);
        }

        return ping;
    }

    public JFrame getRadarWindow() {
        return radarWindow;
    }

    private JLayeredPane getRadarSurface() {
        JLayeredPane contentPane = new JLayeredPane();
        contentPane.add(new RadarScanArea(radarConfig, (int) windowWidth, origin), RADAR_DEPTH);
        return contentPane;
    }

    public void renderLaserAnimation() {
        java.util.List<Point> circumference = new ArrayList<>();
        double                angleStep     = 1.0d;
        for (double a = 0.0d; a < (3.0d * 360.0d); a += angleStep) {
            double thetaR = Math.PI / 180.0d * a;
            int    x      = (int) (0.5d * laserRadius * Math.cos(thetaR));
            int    y      = (int) (0.5d * laserRadius * Math.sin(thetaR));
            Point  temp   = new Point(origin.x + x, origin.y + y);
            circumference.add(temp);
        }

        for (Point p : circumference) {
            Laser laser = new Laser(origin, p, radarConfig, ModuleDirectory.Module.RADAR);
            laserBeams.add(laser);
        }

        JLayeredPane contentPane = getRadarSurface();
        for (Laser laser : laserBeams) {
            contentPane.add(laser, new Integer(RADAR_DEPTH.intValue() + 1));
            radarWindow.setContentPane(contentPane);
            radarWindow.setVisible(true);
            try {
                Thread.sleep(LASER_DELAY);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
            contentPane.remove(laser);
        }
    }

    public void renderPingAnimation(Point location, JFrame frame) {
        JLayeredPane contentPane = getRadarSurface();
        for (int i = 0; i < 4; i++) {
            RadarContactCell contact = new RadarContactCell(radarConfig, new Point(location.x, location.y),
                    EnvironmentUtils
                            .RADAR_CONTACT_COLORS[i]);
            contentPane.add(contact, new Integer(i));
            frame.setContentPane(contentPane);
            frame.setVisible(true);
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
            contentPane.remove(contact);
        }
    }

    public void destroy() {
        radarWindow.dispose();
    }
}
