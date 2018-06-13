package space.exploration.mars.rover.utils;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    private static       Logger logger       = LoggerFactory.getLogger(FileUtil.class);

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

    public static List<File> getLogFiles(DateTime startDate, DateTime endDate, String archiveLocation) {
        List<File> relevantFiles = new ArrayList<>();

        File file = new File(archiveLocation);
        for (File tempFile : file.listFiles()) {
            String fileName = tempFile.getName();

            try {
                String   fileTimeStamp = fileName.split("_")[1].replaceAll(".log", "");
                DateTime currentDate   = new DateTime(Long.parseLong(fileTimeStamp));
                if (currentDate.isAfter(startDate) && !currentDate.isAfter(endDate)) {
                    relevantFiles.add(tempFile);
                }
            } catch (Exception e) {
                logger.error("Error while walking log files.", e);
            }
        }

        return relevantFiles;
    }

    public static String getFileContent(File file) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String         dataLine;
        while ((dataLine = bufferedReader.readLine()) != null) {
            contentBuilder.append(dataLine);
            contentBuilder.append("\n");
        }

        bufferedReader.close();
        return contentBuilder.toString();
    }
}
