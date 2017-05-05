package space.exploration.mars.rover.animation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private Logger     logger              = null;

    public RadioAnimationEngine(Properties marsConfig, JFrame marsSurface, Cell robot, boolean transmit) {
        this.marsConfig = marsConfig;
        this.marsSurface = marsSurface;
        this.robot = robot;
        this.transmit = transmit;
        this.logger = LoggerFactory.getLogger(RadioAnimationEngine.class);
        this.radioAnimationDelay = Integer.parseInt(marsConfig.getProperty("mars.rover.communication.radio.animation" +
                ".delay"));
    }

    public void activateRadio() {
        if (marsConfig == null || robot == null || robot == null || marsSurface == null) {
            logger.error("RadioAnimationEngine not set up correctly");
            logger.error("Need marsConfig,robot and marsSurface.");
            return;
        }

        Color radioColor = (transmit) ? TRANSMIT_COLOR : RECEIVE_COLOR;
        Point location   = new Point(robot.getLocation().x + 4, robot.getLocation().y + 4);

        JLayeredPane contentPane = AnimationUtil.getContent(marsConfig);
        contentPane.add(robot, Cell.ROBOT_DEPTH);

        Cell radioCell = new Cell(marsConfig);
        radioCell.setColor(radioColor);
        radioCell.setLocation(location);
        radioCell.setCellWidth(5);

        for (int i = 0; i < 10; i++) {
            contentPane.add(radioCell, new Integer(RADIO_DEPTH));
            marsSurface.setContentPane(contentPane);
            marsSurface.setVisible(true);
            try {
                Thread.sleep(radioAnimationDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            contentPane.remove(radioCell);
        }
        AnimationUtil.getStaticEnvironment(marsConfig, robot, marsSurface);
    }
}
