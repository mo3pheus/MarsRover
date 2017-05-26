package space.exploration.mars.rover.utils;

import org.slf4j.Logger;
import space.exploration.mars.rover.communication.RoverStatusOuterClass;
import space.exploration.mars.rover.kernel.ModuleDirectory;
import space.exploration.mars.rover.kernel.Rover;

import java.util.Properties;

/**
 * Created by sanket on 5/15/17.
 */
public class RoverUtil {

    public static void roverSystemLog(Logger logger, String message, String severity) {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            //do nothing
        }

        if (severity.equals("INFO")) {
            System.out.println(logger.getName() + " INFO ::" + message);
            logger.info(message);
        } else if (severity.equals("ERROR")) {
            System.out.println(logger.getName() + " ERROR ::" + message);
            logger.error(message);
        }
    }

    public static String getPropertiesAsString(Properties properties) {
        String propAsString = "";
        for (Object key : properties.keySet()) {
            propAsString += "\n";
            propAsString += (String) key;
            propAsString += "::" + properties.getProperty((String) key);
        }
        return propAsString;
    }

    public static final RoverStatusOuterClass.RoverStatus getEndOfLifeMessage(ModuleDirectory.Module module, String
            message, Rover rover) {
        RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus.newBuilder();
        rBuilder.setSCET(System.currentTimeMillis());
        rBuilder.setBatteryLevel(rover.getBattery().getPrimaryPowerUnits() + rover.getBattery()
                .getAuxiliaryPowerUnits());

        rBuilder.setLocation(RoverStatusOuterClass.RoverStatus.Location.newBuilder().
                setX(rover.getMarsArchitect().getRobot().getLocation().x).setY(rover.getMarsArchitect().getRobot()
                .getLocation().y).build());
        rBuilder.setSolNumber(rover.getSol());
        rBuilder.setNotes(message);
        rBuilder.setModuleReporting(module.getValue());
        
        return rBuilder.build();
    }
}
