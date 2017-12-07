package space.exploration.mars.rover.bootstrap;

import communications.protocol.KafkaConfig;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import space.exploration.mars.rover.kernel.Rover;

import java.io.IOException;
import java.util.Properties;

public class MarsMissionLaunch {

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                configureLogging(true);
                new Rover(MatrixCreation.getConfig(), MatrixCreation.getComsConfig(), MatrixCreation
                        .getRoverDBConfig());
            } else {
                configureLogging(false);
                Properties marsConfig       = MatrixCreation.convertToPropertyFiles(args[0]);
                Properties dbConfig         = MatrixCreation.convertToPropertyFiles(args[1]);
                String     camCacheLocation = args[2];
                String     archiveLocation  = args[3];

                new Rover(marsConfig, KafkaConfig.getKafkaConfig("Rover"), dbConfig, camCacheLocation, archiveLocation);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void configureLogging(boolean debug) {
        FileAppender fa = new FileAppender();

        if (!debug) {
            fa.setThreshold(Level.toLevel(Priority.INFO_INT));
            fa.setFile("roverStatusReports/roverStatus_" + Long.toString(System.currentTimeMillis()) + ".log");
        } else {
            fa.setThreshold(Level.toLevel(Priority.DEBUG_INT));
            fa.setFile("analysisLogs/roverStatus_" + Long.toString(System.currentTimeMillis()) + ".log");
        }

        fa.setLayout(new PatternLayout("%d [%t] %p %c %x - %m%n"));

        fa.activateOptions();
        org.apache.log4j.Logger.getRootLogger().addAppender(fa);
    }

    public static void configureLogging(boolean debug, String className) {
        FileAppender fa = new FileAppender();

        if (!debug) {
            fa.setThreshold(Level.toLevel(Priority.INFO_INT));
            fa.setFile(className + "logStatus_" + Long.toString(System.currentTimeMillis()) + ".log");
        } else {
            fa.setThreshold(Level.toLevel(Priority.DEBUG_INT));
            fa.setFile("analysisLogs/" + className + "logStatus_" + Long.toString(System.currentTimeMillis()) + ".log");
        }

        fa.setLayout(new PatternLayout("%d [%t] %p %c %x - %m%n"));

        fa.activateOptions();
        org.apache.log4j.Logger.getRootLogger().addAppender(fa);
    }
}
