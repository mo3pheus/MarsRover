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
    private boolean          blipSound    = false;
    private JLayeredPane     contentPane  = null;
    private RadarContactCell contact      = null;
    private Logger           logger       = LoggerFactory.getLogger(RadarContactBlip.class);
    private int              contactDepth = RadarAnimationEngine.RADAR_DEPTH.intValue() + 1;

    public RadarContactBlip(JLayeredPane contentPane, RadarContactCell contact, boolean blipSound) {
        this.contact = contact;
        this.contentPane = contentPane;
        this.blipSound = blipSound;
    }

    @Override
    public void run() {
        logger.info(contact.getLocation().toString());
        System.out.println("Contact at " + contact.getLocation().toString());
        contact.setColor(EnvironmentUtils.findColor("lawnGreen"));
        contentPane.add(contact, new Integer(contactDepth));
        try {
            if (blipSound) {
                (new BlipSound()).start();
            }
            Thread.sleep(700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        contentPane.remove(contact);

        contact.setColor(RadarContactCell.BLIP_COLOR);
        contact.reduceContactDiameter();
        contentPane.add(contact, new Integer(contactDepth));
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        contentPane.remove(contact);

        contact.setColor(RadarContactCell.BLIP_COLOR.darker());
        contact.reduceContactDiameter();
        contentPane.add(contact, new Integer(contactDepth));
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        contentPane.remove(contact);

        contact.setColor(EnvironmentUtils.findColor("darkOliveGreen"));
        contact.reduceContactDiameter();
        contentPane.add(contact, new Integer(contactDepth));
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        contentPane.remove(contact);

        contact.setColor(EnvironmentUtils.findColor("darkOliveGreen"));
        contact.reduceContactDiameter();
        contact.reduceContactDiameter();
        contentPane.add(contact, new Integer(contactDepth));
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        contentPane.remove(contact);

        contact.setColor(Color.darkGray);
        contentPane.add(contact, new Integer(contactDepth));
        try {
            Thread.sleep(450);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        contentPane.remove(contact);
        contact.setContactDiameter(8);
    }
}
