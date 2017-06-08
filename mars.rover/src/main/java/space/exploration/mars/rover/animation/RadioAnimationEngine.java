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
    public static final Color TRANSMIT_COLOR     = Color.orange;
    public static final Color RECEIVE_COLOR      = Color.green;
    public static final int   RADIO_DEPTH        = 102;
    public static final int   RADIO_LIGHT_WIDTH  = 4;
    public static final int   RADIO_LIGHT_OFFSET = 3;
    public static final int   RADIO_BLINKS       = 10;

    private Properties marsConfig          = null;
    private JFrame     marsSurface         = null;
    private Cell       robot               = null;
    private Logger     logger              = null;
    private boolean    transmit            = false;
    private int        radioAnimationDelay = 0;

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
        if (marsConfig == null || robot == null || marsSurface == null) {
            logger.error("RadioAnimationEngine not set up correctly");
            logger.error("Need marsConfig,robot and marsSurface.");
            return;
        }

        Color radioColor = (transmit) ? TRANSMIT_COLOR : RECEIVE_COLOR;
        Point location = new Point(robot.getLocation().x + RADIO_LIGHT_OFFSET, robot.getLocation().y +
                RADIO_LIGHT_OFFSET);

        JLayeredPane contentPane = AnimationUtil.getContent(marsConfig);
        contentPane.add(robot, Cell.ROBOT_DEPTH);

        for (int i = 0; i < RADIO_BLINKS; i++) {
            Cell radioCell = new Cell(marsConfig);
            radioCell.setColor(radioColor);
            radioCell.setLocation(location);
            radioCell.setCellWidth(RADIO_LIGHT_WIDTH);
            contentPane.add(radioCell, new Integer(RADIO_DEPTH));
            marsSurface.setContentPane(contentPane);
            marsSurface.setVisible(true);
            try {
                Thread.sleep(radioAnimationDelay);
                contentPane.remove(radioCell);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            radioCell = new Cell(marsConfig);
            radioCell.setColor(Color.black);
            radioCell.setLocation(location);
            radioCell.setCellWidth(RADIO_LIGHT_WIDTH);
            contentPane.add(radioCell, new Integer(RADIO_DEPTH));
            marsSurface.setContentPane(contentPane);
            marsSurface.setVisible(true);
            try {
                Thread.sleep(radioAnimationDelay);
                contentPane.remove(radioCell);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        AnimationUtil.getStaticEnvironment(marsConfig, robot, marsSurface);
    }
}
