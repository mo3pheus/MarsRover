package space.exploration.mars.rover.Radar;

import space.exploration.mars.rover.animation.RadarAnimationEngine;
import space.exploration.mars.rover.bootstrap.MatrixCreation;
import space.exploration.mars.rover.environment.RadarContactCell;

import java.awt.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by sanket on 6/3/17.
 */
public class RadarAnimationTest {

    public static void main(String[] args) throws Exception {
        Properties marsConfig = MatrixCreation.getMatrixConfig();
        RadarAnimationEngine             radarAnimationEngine = new RadarAnimationEngine(marsConfig);
        java.util.List<RadarContactCell> contacts             = new ArrayList<>();
        contacts.add(new RadarContactCell(marsConfig, new Point(175, 175), Color.green));
        contacts.add(new RadarContactCell(marsConfig, new Point(200, 100), Color.green));
        contacts.add(new RadarContactCell(marsConfig, new Point(225, 150), Color.green));
        contacts.add(new RadarContactCell(marsConfig, new Point(100, 150), Color.green));
        radarAnimationEngine.setContacts(contacts);
        radarAnimationEngine.renderLaserAnimation();
    }
}
