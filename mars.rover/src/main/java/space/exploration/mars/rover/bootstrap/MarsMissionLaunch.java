package space.exploration.mars.rover.bootstrap;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import space.exploration.mars.rover.kernel.Rover;

import java.io.IOException;

public class MarsMissionLaunch {

    public static void main(String[] args) {
        configureLogging();
        try {
            new Rover(MatrixCreation.getMatrixConfig(), MatrixCreation.getComsConfig(), MatrixCreation
                    .getRoverDBConfig());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void configureLogging() {
        FileAppender fa = new FileAppender();

        boolean debug = false;

        if (!debug) {
            fa.setThreshold(Level.toLevel(Priority.INFO_INT));
            fa.setFile("roverStatusReports/roverStatus_" + Long.toString(System.currentTimeMillis()) + ".log");
        } else {
            fa.setThreshold(Level.toLevel(Priority.DEBUG_INT));
            fa.setFile("analysisLogs/roverStatus_" + Long.toString(System.currentTimeMillis()) + ".log");
        }

        //fa.setLayout(new PatternLayout("%-4r [%t] %-5p %c %x - %m%n"));
        fa.setLayout(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN));

        fa.activateOptions();
        org.apache.log4j.Logger.getRootLogger().addAppender(fa);
    }

}
