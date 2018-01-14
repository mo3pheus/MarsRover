package space.exploration.mars.rover.animation;

import junit.framework.TestCase;
import org.junit.Test;
import space.exploration.mars.rover.bootstrap.MatrixCreation;
import space.exploration.mars.rover.environment.MarsArchitect;

import java.io.IOException;

public class DanAnimationEngineTest extends TestCase {
    private MarsArchitect      marsArchitect;
    private DanAnimationEngine danAnimationEngine;

    @Override
    public void setUp() {
        try {
            marsArchitect = new MarsArchitect(MatrixCreation.getConfig());
            danAnimationEngine = new DanAnimationEngine(MatrixCreation.getConfig(), marsArchitect
                    .getRobot().getLocation(), 200, marsArchitect.getMarsSurface());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAnimation() throws Exception {
        danAnimationEngine.renderDanAnimation();
    }
}
