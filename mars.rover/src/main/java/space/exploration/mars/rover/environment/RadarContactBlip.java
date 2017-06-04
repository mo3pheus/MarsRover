package space.exploration.mars.rover.environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.animation.RadarAnimationEngine;

import javax.swing.*;

/**
 * Created by sanket on 6/3/17.
 */
public class RadarContactBlip extends Thread {
    private boolean          blipSound   = false;
    private JLayeredPane     contentPane = null;
    private RadarContactCell contact     = null;
    private Logger           logger      = LoggerFactory.getLogger(RadarContactBlip.class);

    public RadarContactBlip(JLayeredPane contentPane, RadarContactCell contact, boolean blipSound) {
        this.contact = contact;
        this.contentPane = contentPane;
        this.blipSound = blipSound;
    }

    @Override
    public void run() {
        logger.info(contact.getLocation().toString());
        System.out.println("Contact at " + contact.getLocation().toString());
        contact.setColor(RadarContactCell.BLIP_COLOR);
        contentPane.add(contact, new Integer(RadarAnimationEngine.RADAR_DEPTH.intValue() + 1));
        try {
            if (blipSound) {
                (new BlipSound()).start();
            }
            Thread.sleep(800);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        contentPane.remove(contact);
    }
}
