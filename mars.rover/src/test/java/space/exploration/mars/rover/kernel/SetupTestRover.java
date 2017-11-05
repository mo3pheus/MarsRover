package space.exploration.mars.rover.kernel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.bootstrap.MatrixCreation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SetupTestRover {
    private static Logger logger = LoggerFactory.getLogger(SetupTestRover.class);

    public static Rover setupTestRover() {
        Rover       rover = null;
        try {
            rover = new Rover(MatrixCreation.getConfig(), MatrixCreation.getComsConfig(), MatrixCreation
                    .getRoverDBTestConfig());
        } catch (IOException e) {
            logger.error("Failed to set up test rover", e);
        } finally {
            return rover;
        }
    }
}
