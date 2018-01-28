package space.exploration.mars.rover.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {
    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static void processDirectories(String path) {
        File temp = new File(path);
        if (!temp.exists()) {
            logger.info("File " + path + " does not exist. Trying to create it now.");
            temp.mkdirs();

            if (temp.exists()) {
                logger.info("Successfully created " + path);
            }
        }
    }

    public static void cleanUpDirectories(String path) {
        File temp = new File(path);
        if (temp.exists()) {
            logger.info("File at " + path + " exists - will delete it now.");
            try {
                Files.delete(Paths.get(path));
            } catch (IOException e) {
                logger.error("FileSystem permissions issue :: " + path, e);
            }
        }
    }
}
