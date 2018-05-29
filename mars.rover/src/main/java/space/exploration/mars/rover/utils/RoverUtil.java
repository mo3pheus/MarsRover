package space.exploration.mars.rover.utils;

import com.google.protobuf.InvalidProtocolBufferException;
import communications.protocol.ModuleDirectory;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;
import space.exploration.communications.protocol.communication.RoverStatusOuterClass;
import space.exploration.communications.protocol.softwareUpdate.SwUpdatePackageOuterClass;
import space.exploration.mars.rover.kernel.Rover;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by sanket on 5/15/17.
 */
public class RoverUtil {

    /**
     * @param logger   - logger of the unit in which error occurs.
     * @param message  - message from the unit.
     * @param severity - INFO/ ERROR.
     */
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

    public static void cleanOutOldLogFiles(Rover rover) {
        boolean isDirectory = Files.isDirectory(Paths.get("roverStatusReports/"));
        rover.getLogger().info("IsDirectory result = " + isDirectory);

        int                  count = 0;
        java.util.List<Path> paths = null;
        try {
            paths = Files.walk(Paths.get("roverStatusReports/")).collect(Collectors.toList());
        } catch (IOException e) {
            rover.getLogger().error("Could not find directory roverStatusReports ");
            return;
        }

        for (Path path : paths) {
            String fileName = path.toString();
            fileName = fileName.replace("roverStatus", "");
            fileName = fileName.replace(".log", "");

            Long timeStamp = 0l;
            try {
                timeStamp = Long.parseLong(fileName);
            } catch (NumberFormatException nfe) {
                rover.getLogger().info("Could not get timestamp for :: " + timeStamp);
                continue;
            }

            DateTime currentDate = new DateTime();
            DateTime fileDate    = new DateTime(timeStamp);

            int daysBetween = (int) TimeUnit.MILLISECONDS.toDays(currentDate.getMillis() - fileDate.getMillis());

            if (daysBetween >= 10) {
                rover.getLogger().debug("Deleting file " + path.toString());
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    rover.getLogger().error("Could not delete file " + path);
                }
                count++;
            }
        }
        rover.getLogger().info("Deleted " + count + " old log files.");
    }

    public static void deleteAllJarFiles(Rover rover) throws IOException {
        List<Path> paths = Files.walk(Paths.get(rover.getWorkingDirectory())).collect(Collectors.toList());
        for (Path path : paths) {
            if (path.toString().contains(".jar")) {
                rover.getLogger().info("Deleting file " + path);
                Files.delete(path);
            }
        }
    }

    public static void processSoftwareUpdate(Rover rover, SwUpdatePackageOuterClass.SwUpdatePackage swUpdatePackage) {
        try {
            deleteAllJarFiles(rover);
        } catch (IOException io) {
            rover.getLogger().error("Could not delete old jar files. Software update abandoned!", io);
            return;
        }

        String jarFileLocation = swUpdatePackage.getJarFileLocation();
        String scriptLocation  = swUpdatePackage.getLaunchScriptLocation();
        String jarFilename     = swUpdatePackage.getJarFileName();
        String scriptFilename  = swUpdatePackage.getScriptFileName();

        try {
            byte[] jarFileContent    = ServiceUtil.download(jarFileLocation);
            byte[] scriptFileContent = ServiceUtil.download(scriptLocation);

            File jarFile    = new File(jarFilename);
            File scriptFile = new File(scriptFilename);

            FileOutputStream jarFos    = new FileOutputStream(jarFile);
            FileOutputStream scriptFos = new FileOutputStream(scriptFile);

            jarFos.write(jarFileContent);
            scriptFos.write(scriptFileContent);

            Set<PosixFilePermission> permissions = new HashSet<>();

            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OTHERS_READ);
            permissions.add(PosixFilePermission.OTHERS_WRITE);
            permissions.add(PosixFilePermission.OWNER_WRITE);
            Files.setPosixFilePermissions(jarFile.toPath(), permissions);

            permissions.add(PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(scriptFile.toPath(), permissions);

            jarFos.close();
            scriptFos.close();

            rover.getLogger().info("Downloaded new jar file :: " + jarFilename);
            rover.getLogger().info("Downloaded new launch script :: " + scriptFilename);
        } catch (IOException e) {
            rover.getLogger().error("Error during software update. Reverting to original software.", e);
        }
    }

    public static RoverStatusOuterClass.RoverStatus.Builder generateUpdateStatus() {
        RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus
                .newBuilder();
        rBuilder.setModuleReporting(ModuleDirectory.Module.DIAGNOSTICS.getValue());

        rBuilder.setNotes("Rover is going to shutdown after a software update.");
        return rBuilder;
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

    public static RoverStatusOuterClass.RoverStatus getEndOfLifeMessage(ModuleDirectory.Module module, String
            message, Rover rover) {
        RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus.newBuilder();
        rBuilder.setSCET(System.currentTimeMillis());
        rBuilder.setBatteryLevel(rover.getBattery().getPrimaryPowerUnits() + rover.getBattery()
                .getAuxiliaryPowerUnits());

        rBuilder.setLocation(RoverStatusOuterClass.RoverStatus.Location.newBuilder().
                setX(rover.getMarsArchitect().getRobot().getLocation().x).setY(rover.getMarsArchitect().getRobot()
                                                                                       .getLocation().y).build());
        rBuilder.setSolNumber(rover.getSpacecraftClock().getSol());
        rBuilder.setMslPositionsPacket(rover.getPositionSensor().getPositionsData());
        rBuilder.setNotes(message);
        rBuilder.setModuleReporting(module.getValue());

        return rBuilder.build();
    }

    public static String getDatabaseCredentials(boolean password) {
        Scanner scanner = new Scanner(System.in);
        if (password) {
            System.out.println("Please enter the password:");
        } else {
            System.out.println("Please enter the database username:");
        }
        return scanner.nextLine();
    }

    public static String getNasaCredentials() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter the NASA API Authentication key::");
        System.out.println("If you don't know what this is enter :: DEMO_KEY");
        return scanner.nextLine();
    }

    public static RoverStatusOuterClass.RoverStatus.Location getLocation(Point point) {
        RoverStatusOuterClass.RoverStatus.Location.Builder lBuilder = RoverStatusOuterClass.RoverStatus.Location
                .newBuilder();
        lBuilder.setX((int) point.getX());
        lBuilder.setY((int) point.getY());
        return lBuilder.build();
    }


    /**
     * @param start
     * @param end
     * @return start and end are in awt.Point, so +x,+y mean accordingly
     */
    public static double getHeading(Point start, Point end) {
        if (start.getX() == end.getX()) {
            if (end.getY() > start.getY()) {
                return 180.0d;
            } else {
                return 0.0d;
            }
        }

        if (start.getX() > end.getX()) {
            return 270.0d;
        } else {
            return 90.0d;
        }
    }

    public static void saveOffProperties(Properties marsConfig, String filePath) throws IOException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath))) {
            for (Object key : marsConfig.keySet()) {
                String value = marsConfig.getProperty((String) key);
                bufferedWriter.write((String) key + "=" + value + "\n");
            }
        }
    }

    public static String getInstructionQueue(Rover rover) {
        String errorMessage = "";
        for (byte[] message : rover.getInstructionQueue()) {
            try {
                InstructionPayloadOuterClass.InstructionPayload instructionPayload =
                        InstructionPayloadOuterClass.InstructionPayload.parseFrom(message);
                boolean gracefulShutdown = false;
                for (InstructionPayloadOuterClass.InstructionPayload.TargetPackage tp : instructionPayload
                        .getTargetsList()) {
                    if (tp.getAction().equals("Graceful Shutdown.") && tp.getRoverModule() == ModuleDirectory.Module
                            .KERNEL.getValue()) {
                        gracefulShutdown = true;
                        rover.setGracefulShutdown(true);
                        break;
                    }
                }
                if (!gracefulShutdown) {
                    errorMessage += instructionPayload.toString();
                }
            } catch (InvalidProtocolBufferException invalidProtocol) {
                RoverUtil.writeErrorLog(rover, "Invalid protocol buffer for instructionPayload", invalidProtocol);
            }
        }
        return errorMessage;
    }

    public static final InstructionPayloadOuterClass.InstructionPayload getGracefulShutdownCommand() {
        InstructionPayloadOuterClass.InstructionPayload.Builder iBuilder = InstructionPayloadOuterClass
                .InstructionPayload.newBuilder();
        iBuilder.setTimeStamp(System.currentTimeMillis());
        iBuilder.setSOS(false);

        InstructionPayloadOuterClass.InstructionPayload.TargetPackage.Builder tBuilder = InstructionPayloadOuterClass
                .InstructionPayload.TargetPackage.newBuilder();
        tBuilder.setAction("Graceful Shutdown.");
        tBuilder.setRoverModule(ModuleDirectory.Module.KERNEL.getValue());
        tBuilder.setEstimatedPowerUsage(0);
        iBuilder.addTargets(tBuilder.build());
        return iBuilder.build();
    }

    public static void writeSystemLog(Rover rover, InstructionPayloadOuterClass.InstructionPayload.TargetPackage
            targetPackage, int
                                              instructionQueueLength) {

        if (!rover.isDbLoggingEnabled()) {
            return;
        }

        try {
            if (rover.getResultSet().isClosed()) {
                rover.setResultSet(rover.getStatement().executeQuery("SELECT * FROM " + rover.getLogDBConfig()
                        .getProperty("mars.rover.database.logTableName")));
            }
            rover.getResultSet().moveToInsertRow();
            rover.getResultSet().updateTimestamp("EVENT_TIME", new Timestamp(System.currentTimeMillis()));
            Blob blob = rover.getLogDBConnection().createBlob();
            blob.setBytes(1, targetPackage.toByteArray());
            rover.getResultSet().updateBlob("MESSAGE_DETAILS", blob);
            rover.getResultSet().updateString("MESSAGE", targetPackage.getAction() + " Instruction Queue length = " +
                    instructionQueueLength);
            rover.getResultSet().insertRow();
        } catch (SQLException e) {
            rover.getLogger().error("SQLException", e);
        }
    }

    public static void writeSystemLog(Rover rover, InstructionPayloadOuterClass.InstructionPayload
            instructionPayload, int
                                              instructionQueueLength) {

        if (!rover.isDbLoggingEnabled()) {
            return;
        }

        try {
            if (rover.getResultSet().isClosed()) {
                rover.setResultSet(rover.getStatement().executeQuery("SELECT * FROM " + rover.getLogDBConfig()
                        .getProperty("mars.rover.database.logTableName")));
            }
            rover.getResultSet().moveToInsertRow();
            rover.getResultSet().updateTimestamp("EVENT_TIME", new Timestamp(System.currentTimeMillis()));
            Blob blob = rover.getLogDBConnection().createBlob();
            blob.setBytes(1, instructionPayload.toByteArray());
            rover.getResultSet().updateBlob("MESSAGE_DETAILS", blob);
            rover.getResultSet().updateString("MESSAGE", "message added to instruction queue" + " Instruction Queue " +
                    "length = " +
                    instructionQueueLength);
            rover.getResultSet().insertRow();
        } catch (SQLException e) {
            rover.getLogger().error("SQLException", e);
        }
    }

    public static void writeSystemLog(Rover rover, String message, int instructionQueueLength) {
        if (!rover.isDbLoggingEnabled()) {
            return;
        }

        try {
            if (rover.getResultSet().isClosed()) {
                rover.setResultSet(rover.getStatement().executeQuery("SELECT * FROM " + rover.getLogDBConfig()
                        .getProperty("mars.rover.database.logTableName")));
            }
            rover.getResultSet().moveToInsertRow();
            rover.getResultSet().updateTimestamp("EVENT_TIME", new Timestamp(System.currentTimeMillis()));
            rover.getResultSet().updateString("MESSAGE", message + " Instruction Queue length = " +
                    instructionQueueLength);
            rover.getResultSet().insertRow();
        } catch (SQLException e) {
            rover.getLogger().error("SQLException", e);
        }
    }

    public static void writeErrorLog(Rover rover, String message, Exception e) {

        if (!rover.isDbLoggingEnabled()) {
            return;
        }

        try {
            if (rover.getErrorSet().isClosed()) {
                rover.setErrorSet(rover.getStatement().executeQuery("SELECT * FROM " + rover.getLogDBConfig()
                        .getProperty("mars.rover.database.errorTableName")));
            }
            rover.getErrorSet().moveToInsertRow();
            rover.getErrorSet().updateTimestamp("EVENT_TIME", new Timestamp(System.currentTimeMillis()));
            rover.getErrorSet().updateString("MESSAGE", message);

            if (e != null) {
                String stackTrace = "";
                for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                    stackTrace += stackTraceElement.toString() + "|";
                }
                rover.getErrorSet().updateString("MESSAGE_DETAILS", stackTrace);
            }
            rover.getErrorSet().insertRow();
        } catch (Exception exception) {
            rover.getLogger().error("Exception while writing errorLog", e);
        }
    }
}
