package space.exploration.mars.rover.animation;

import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;
import space.exploration.mars.rover.bootstrap.MatrixCreation;
import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.environment.MarsArchitect;

import java.awt.*;
import java.io.IOException;
import java.util.Properties;

public class SpecAnimationFactoryTest extends TestCase {
    private SpecAnimationFactory specAnimationFactory        = null;
    private SpecAnimationEngine  spectrometerAnimationEngine = null;
    private MarsArchitect        marsArchitect               = null;
    private Properties           marsConfig                  = null;

    @Override
    public void setUp() {
        try {
            marsConfig = MatrixCreation.getConfig();
            marsArchitect = new MarsArchitect(MatrixCreation.getConfig());
            specAnimationFactory = new SpecAnimationFactory(marsArchitect.getMarsSurface(), marsConfig, marsArchitect
                    .getRobot());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testApxsAnimationAtEdge() {
        Cell robot = marsArchitect.getRobot();
        robot.setLocation(new Point(675, 50));
        specAnimationFactory = new SpecAnimationFactory(marsArchitect.getMarsSurface(), marsConfig, robot);
        spectrometerAnimationEngine = specAnimationFactory.getSpectrometerAnimationEngine("DAN");
        spectrometerAnimationEngine.renderAnimation();
        spectrometerAnimationEngine = specAnimationFactory.getSpectrometerAnimationEngine("APXS");
        spectrometerAnimationEngine.renderAnimation();
    }
}
