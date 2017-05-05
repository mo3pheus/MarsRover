package space.exploration.mars.rover.animation;

import space.exploration.mars.rover.environment.Cell;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

/**
 * Created by sanketkorgaonkar on 5/5/17.
 */
public class RadioAnimationEngine {
    public static final Color TRANSMIT_COLOR = Color.red;
    public static final Color RECEIVE_COLOR  = Color.green;
    public static final int   RADIO_DEPTH    = 102;

    private Properties marsConfig          = null;
    private JFrame     marsSurface         = null;
    private Cell       robot               = null;
    private boolean    transmit            = false;
    private int        radioAnimationDelay = 0;

    public RadioAnimationEngine(Properties marsConfig, JFrame marsSurface, Cell robot, boolean transmit) {
        this.marsConfig = marsConfig;
        this.marsSurface = marsSurface;
        this.robot = robot;
        this.transmit = transmit;

        this.radioAnimationDelay = Integer.parseInt(marsConfig.getProperty("mars.rover.communication.radio.animation" +
                ".delay"));
    }

    public void activateRadio() {
        System.out.println("In RadioAnimationEngine, transmit = " + transmit + " Location = " + robot.getLocation() +
                " radioAnimationDelay = " + radioAnimationDelay);
        Color radioColor = (transmit) ? TRANSMIT_COLOR : RECEIVE_COLOR;
        Point location   = new Point((int) (robot.getLocation().x + 4), (int) (robot.getLocation().y + 4));

        JLayeredPane content   = AnimationUtil.getContent(marsConfig);
        content.add(robot, Cell.ROBOT_DEPTH);
        Cell         radioCell = new Cell(marsConfig);
        radioCell.setColor(radioColor);
        radioCell.setLocation(location);
        radioCell.setCellWidth(5);

        for (int i = 0; i < 10; i++) {
            content.add(radioCell, RADIO_DEPTH);
            marsSurface.setContentPane(content);
            marsSurface.setVisible(true);
            try {
                Thread.sleep(radioAnimationDelay);
                System.out.println("Blink!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            content.remove(radioCell);
        }
        AnimationUtil.getStaticEnvironment(marsConfig, robot, marsSurface);
    }
}
