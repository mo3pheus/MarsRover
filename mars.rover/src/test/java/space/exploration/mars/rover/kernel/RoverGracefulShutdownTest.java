package space.exploration.mars.rover.kernel;

import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class RoverGracefulShutdownTest extends TestCase {
    public static final String SEPARATOR = "-------------------------------------------------------------------";
    private             Rover  rover     = null;

    @Override
    public void setUp() {
        System.out.println(SEPARATOR);
        System.out.println("Running gracefulShutdownTest.");
        System.out.println(SEPARATOR);
        rover = SetupTestRover.setupTestRover();
    }

    @Test
    public void testGracefulShutdown() {
        try {
            Thread.sleep(20000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        rover.setState(rover.listeningState);
        rover.gracefulShutdown();
        System.out.println(SEPARATOR);
        System.out.println("End of test.");
        System.out.println(SEPARATOR);
    }
}
