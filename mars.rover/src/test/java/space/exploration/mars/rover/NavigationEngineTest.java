package space.exploration.mars.rover;

import communications.protocol.ModuleDirectory;
import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;
import space.exploration.communications.protocol.robot.RobotPositionsOuterClass;
import space.exploration.mars.rover.bootstrap.MarsMissionLaunch;
import space.exploration.mars.rover.kernel.Rover;
import space.exploration.mars.rover.kernel.SetupTestRover;
import space.exploration.mars.rover.navigation.NavigationEngine;

@Ignore
public class NavigationEngineTest extends TestCase {
    private NavigationEngine navigationEngine = null;
    private Rover            rover            = null;
    private Logger           logger           = LoggerFactory.getLogger(NavigationEngineTest.class);

    @Override
    public void setUp() {
        MarsMissionLaunch.configureLogging(false);
        try {
            rover = SetupTestRover.setupTestRover();
            rover.getBattery().setPrimaryPowerUnits(200);
            navigationEngine = rover.getNavigationEngine();
        } catch (Exception e) {
            logger.error("Test rover gave an exception.", e);
        }
    }

    @Test
    public void testInvalidPaths() {
        rover.getInstructionQueue().add(buildMoveCommand());
        try {
            Thread.sleep(4 * 60000);
        } catch (InterruptedException e) {
            logger.error("Exception thrown while sleeping.", e);
        }
    }

    private static byte[] buildMoveCommand() {
        InstructionPayloadOuterClass.InstructionPayload.Builder iBuilder = InstructionPayloadOuterClass
                .InstructionPayload.newBuilder();
        iBuilder.setTimeStamp(System.currentTimeMillis());
        iBuilder.setSOS(false);

        InstructionPayloadOuterClass.InstructionPayload.TargetPackage.Builder tBuilder = InstructionPayloadOuterClass
                .InstructionPayload.TargetPackage.newBuilder();
        tBuilder.setAction("Move");
        tBuilder.setRoverModule(ModuleDirectory.Module.PROPULSION.getValue());
        tBuilder.setEstimatedPowerUsage(40);

        RobotPositionsOuterClass.RobotPositions.Point targetPosition = RobotPositionsOuterClass.RobotPositions.Point
                .newBuilder().setX(0).setY(50).build();

        tBuilder.setAuxiliaryData(RobotPositionsOuterClass.RobotPositions.newBuilder().addPositions(targetPosition)
                                          .build()
                                          .toByteString());
        iBuilder.addTargets(tBuilder.build());
        return iBuilder.build().toByteArray();
    }
}
