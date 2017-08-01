package space.exploration.mars.rover.animation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.environment.*;
import space.exploration.mars.rover.kernel.ModuleDirectory;
import space.exploration.mars.rover.sensor.Radar;
import space.exploration.mars.rover.utils.RadialContact;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Created by sanket on 5/30/17.
 */
public class RadarAnimationEngine {
    public static final  Integer RADAR_DEPTH = JLayeredPane.DEFAULT_LAYER;
    private static final int     LASER_DELAY = 30;

    private JFrame                 radarWindow    = null;
    private int                    numRevs        = 0;
    private int                    laserDiameter  = 0;
    private double                 scaleFactor    = 0.0d;
    private double                 windowWidth    = 0.0d;
    private boolean                blipSound      = false;
    private Properties             radarConfig    = null;
    private Point                  origin         = null;
    private List<Laser>            laserBeams     = null;
    private List<RadialContact>    radialContacts = null;
    private List<RadarContactCell> contacts       = null;
    private Logger                 logger         = LoggerFactory.getLogger(RadarAnimationEngine.class);

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
        laserDiameter = (int) (windowWidth - (2 * RadarScanArea.RING_OFFSET));
        radarWindow = new JFrame();
        radarWindow.setSize((int) windowWidth, (int) windowWidth);
        radarWindow.setVisible(true);
        radarWindow.setTitle("Radar Scan Window");
        int originCo = (int) ((windowWidth) / 2.0d);
        origin = new Point(originCo, originCo);

        double angleStep = 0.5d;
        for (double a = 0.0d; a < (numRevs * 360.0d); a += angleStep) {
            double thetaR = Math.toRadians(a);
            int    x      = (int) (0.5d * laserDiameter * Math.cos(thetaR));
            int    y      = (int) (0.5d * laserDiameter * Math.sin(thetaR));
            Point  temp   = new Point(origin.x + x, origin.y + y);

            Laser laser = new Laser(origin, temp, radarConfig, ModuleDirectory.Module.RADAR);
            laser.setAngle(a);
            laserBeams.add(laser);
        }
    }

    private void augmentLaserBeams() {
        List<Laser> contactLasers = new ArrayList<>();
        for (int i = 0; i < numRevs; i++) {
            for (RadialContact r : radialContacts) {
                Laser laser = new Laser(r.getCenter(), r.getContactPoint(), radarConfig, ModuleDirectory.Module
                        .RADAR);
                double angleAdditionFactor = (i * 360.0d);
                laser.setAngle(angleAdditionFactor + r.getPolarPoint().getTheta());
                System.out.println("Creating lasers now - " + r.getContactPoint().toString() + " Angle = " + laser
                        .getAngle() + " r = " + laser.getLaserLength());

                contactLasers.add(laser);
            }
        }

        if (contactLasers.isEmpty()) {
            return;
        }

        List<Laser> augmentedBeams = new ArrayList<>();
        augmentedBeams.addAll(laserBeams);
        augmentedBeams.addAll(contactLasers);

        Collections.sort(augmentedBeams);
        laserBeams.clear();
        laserBeams.addAll(augmentedBeams);
    }

    public JFrame getRadarWindow() {
        return radarWindow;
    }

    private JLayeredPane getRadarSurface() {
        JLayeredPane contentPane = new JLayeredPane();
        contentPane.add(new RadarScanArea(radarConfig, (int) windowWidth, origin), RADAR_DEPTH);
        return contentPane;
    }

    public void setRadialContacts(List<RadialContact> radialContacts) {
        this.radialContacts = new ArrayList<>();
        this.radialContacts.addAll(radialContacts);
    }

    public void renderLaserAnimation() {
        augmentLaserBeams();
        JLayeredPane contentPane = getRadarSurface();
        for (Laser laser : laserBeams) {
            contentPane.add(laser, new Integer(RADAR_DEPTH.intValue() + 1));
            reflectContacts(laser, contentPane);
            radarWindow.setContentPane(contentPane);
            radarWindow.setVisible(true);
            try {

                if (Math.abs((laserDiameter * 0.5d) - laser.getLaserLength()) > 5.0d) {
                    Thread.sleep(LASER_DELAY);
                }

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
            }
        }
    }
}
