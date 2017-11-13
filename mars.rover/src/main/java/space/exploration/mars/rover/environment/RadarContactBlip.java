package space.exploration.mars.rover.environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.animation.RadarAnimationEngine;

import javax.swing.*;
import java.awt.*;

/**
 * Created by sanket on 6/3/17.
 */
public class RadarContactBlip extends Thread {
    private JLayeredPane     contentPane  = null;
    private RadarContactCell contact      = null;
    private Logger           logger       = LoggerFactory.getLogger(RadarContactBlip.class);
    private int              contactDepth = RadarAnimationEngine.RADAR_DEPTH.intValue() + 1;

    public RadarContactBlip(JLayeredPane contentPane, RadarContactCell contact ) {
        this.contact = contact;
        this.contentPane = contentPane;
    }

    @Override
    public void run() {
        logger.debug(contact.getLocation().toString());
        System.out.println("Contact at " + contact.getLocation().toString());
        contact.setColor(EnvironmentUtils.findColor("lawnGreen"));
        contentPane.add(contact, new Integer(contactDepth));
        try {
            Thread.sleep(900);
            contentPane.remove(contact);

            contact.setColor(RadarContactCell.BLIP_COLOR);
            contact.reduceContactDiameter();
            contentPane.add(contact, new Integer(contactDepth));
            Thread.sleep(500);

            contentPane.remove(contact);

            contact.setColor(RadarContactCell.BLIP_COLOR.darker());
            contact.reduceContactDiameter();
            contentPane.add(contact, new Integer(contactDepth));
            Thread.sleep(300);
            contentPane.remove(contact);

            contact.setColor(EnvironmentUtils.findColor("darkOliveGreen"));
            contact.reduceContactDiameter();
            contentPane.add(contact, new Integer(contactDepth));
            Thread.sleep(400);
            contentPane.remove(contact);

            contact.setColor(EnvironmentUtils.findColor("darkOliveGreen"));
            contact.reduceContactDiameter();
            contact.reduceContactDiameter();
            contentPane.add(contact, new Integer(contactDepth));
            Thread.sleep(500);
            contentPane.remove(contact);

            contact.setColor(Color.darkGray);
            contentPane.add(contact, new Integer(contactDepth));
            Thread.sleep(650);

            contentPane.remove(contact);
            contact.setContactDiameter(8);
        } catch (Exception e) {
            logger.error("Radar Contact Blip animation error", e);
        }
    }
}
