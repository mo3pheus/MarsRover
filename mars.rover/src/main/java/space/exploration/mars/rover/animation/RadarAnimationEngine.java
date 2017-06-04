package space.exploration.mars.rover.animation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.environment.*;
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
    public static final  Integer                RADAR_DEPTH = new Integer(0);
    private static final int                    LASER_DELAY = 60;
    private              JFrame                 radarWindow = null;
    private              int                    numRevs     = 0;
    private              int                    numRings    = 0;
    private              int                    laserRadius = 0;
    private              double                 scaleFactor = 0.0d;
    private              double                 windowWidth = 0.0d;
    private              boolean                blipSound   = false;
    private              Properties             radarConfig = null;
    private              Point                  origin      = null;
    private              List<Laser>            laserBeams  = null;
    private              List<RadarContactCell> contacts    = null;
    private              Logger                 logger      = LoggerFactory.getLogger(RadarAnimationEngine.class);

    public RadarAnimationEngine(Properties radarConfig) {
        this.radarConfig = radarConfig;
        laserBeams = new ArrayList<>();
        setUpRadarWindow();
    }

    public void setUpRadarWindow() {
        int fWidth = Integer.parseInt(radarConfig.getProperty(EnvironmentUtils.FRAME_WIDTH_PROPERTY));
        blipSound = Boolean.parseBoolean(radarConfig.getProperty(Radar.RADAR_PREFIX + ".blip.sound"));
        scaleFactor = Double.parseDouble(radarConfig.getProperty(Radar.RADAR_PREFIX + ".scaleFactor"));
        windowWidth = scaleFactor * fWidth;
        numRevs = Integer.parseInt(radarConfig.getProperty(Radar.RADAR_PREFIX + ".numberOfRevelutions"));
        laserRadius = (int) (windowWidth - (2 * RadarScanArea.RING_OFFSET));
        radarWindow = new JFrame();
        radarWindow.setSize((int) windowWidth, (int) windowWidth);
        radarWindow.setVisible(true);
        radarWindow.setTitle("Radar Scan Window");
        int originCo = (int) ((windowWidth) / 2.0d);
        origin = new Point(originCo, originCo);
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
        /* build circumference */
        java.util.List<Point> circumference = new ArrayList<>();
        double                angleStep     = 1.0d;
        for (double a = 0.0d; a < (numRevs * 360.0d); a += angleStep) {
            double thetaR = Math.PI / 180.0d * a;
            int    x      = (int) (0.5d * laserRadius * Math.cos(thetaR));
            int    y      = (int) (0.5d * laserRadius * Math.sin(thetaR));
            Point  temp   = new Point(origin.x + x, origin.y + y);
            circumference.add(temp);
        }

        /* build list of lasers */
        for (Point p : circumference) {
            Laser laser = new Laser(origin, p, radarConfig, ModuleDirectory.Module.RADAR);
            laserBeams.add(laser);
        }

        /* draw lasers */
        JLayeredPane contentPane = getRadarSurface();
        for (Laser laser : laserBeams) {
            contentPane.add(laser, new Integer(RADAR_DEPTH.intValue() + 1));
            reflectContacts(laser, contentPane);
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

    public Point getOrigin() {
        return origin;
    }

    public void destroy() {
        radarWindow.dispose();
    }

    public List<RadarContactCell> getContacts() {
        return contacts;
    }

    public void setContacts(List<RadarContactCell> contacts) {
        this.contacts = contacts;
    }

    private void reflectContacts(Laser laser, JLayeredPane contentPane) {
        if (contacts == null) {
            logger.info("Radar had no contacts for " + laser.getBeam().toString());
            return;
        }

        for (RadarContactCell contact : contacts) {
            if (contact.getContactRect().intersectsLine(laser.getBeam())) {
                RadarContactBlip blip = new RadarContactBlip(contentPane, contact, blipSound);
                blip.start();
                //blip.
            }
        }
    }
}
