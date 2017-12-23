package space.exploration.mars.rover.kernel;

import communications.protocol.KafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.bootstrap.MarsMissionLaunch;
import space.exploration.mars.rover.bootstrap.MatrixCreation;

import java.io.IOException;

public class SetupTestRover {
    private static Logger logger = LoggerFactory.getLogger(SetupTestRover.class);

    public static Rover setupTestRover() {
        Rover rover = null;
        try {
            MarsMissionLaunch.configureLogging(false);
            rover = new Rover(MatrixCreation.getConfig(), KafkaConfig.getKafkaConfig("Rover"), MatrixCreation
                    .getRoverDBTestConfig(), MatrixCreation.getConfigFilePath());
        } catch (IOException e) {
            logger.error("Failed to set up test rover", e);
            e.printStackTrace();
        } finally {
            return rover;
        }
    }
}
