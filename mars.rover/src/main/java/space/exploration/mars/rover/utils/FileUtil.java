package space.exploration.mars.rover.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class FileUtil {
    public static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static void processDirectories(String path) {
        logger.info("Trying to create file " + path);
        File temp = new File(path);
        if (!temp.exists()) {
            temp.mkdirs();
        }
    }

}
