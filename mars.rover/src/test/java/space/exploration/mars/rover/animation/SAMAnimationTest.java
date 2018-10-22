package space.exploration.mars.rover.animation;

import junit.framework.TestCase;
import org.junit.Test;
import space.exploration.mars.rover.bootstrap.MatrixCreation;
import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.environment.MarsArchitect;

import java.awt.*;
import java.io.IOException;
import java.util.Properties;

public class SAMAnimationTest extends TestCase {
    private SAMAnimationEngine samAnimationEngine;
    private MarsArchitect marsArchitect;
    private Properties marsConfig;

    @Override
    public void setUp() {
        try {
            marsConfig = MatrixCreation.getConfig();
            marsArchitect = new MarsArchitect(MatrixCreation.getConfig());
            samAnimationEngine = new SAMAnimationEngine(marsArchitect.getMarsSurface(), marsArchitect.getRobot(),
                    marsConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSAMAnimationEngine() {
        Cell robot = marsArchitect.getRobot();
        robot.setLocation(new Point(123, 234));
        samAnimationEngine = new SAMAnimationEngine(marsArchitect.getMarsSurface(), robot, marsConfig);
        samAnimationEngine.renderAnimation();
    }
}
