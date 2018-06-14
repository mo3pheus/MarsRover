package space.exploration.mars.rover.kernel.config;

import java.util.Properties;

public class RoverConfig {
    public static final String ROVER_NAME        = "Curiosity";
    public static final String PROPULSION_CHOICE = "mars.rover.propulsion.engine.choice";

    protected String marsConfigLocation = null;

    /* Logging Details */
    private String  dbUserName         = null;
    private String  dbPassword         = null;
    private String  logArchiveLocation = null;
    private long    startOfLog         = 0l;
    private boolean dbLoggingEnabled   = false;
    private String  logLevel           = "info";
    private boolean debugEnabled       = false;

    /* Configuration */
    private Properties marsConfig               = null;
    private Properties comsConfig               = null;
    private Properties logDBConfig              = null;
    private String     cameraImageCacheLocation = null;
    private String     dataArchiveLocation      = null;
    private String     nasaApiAuthKey           = null;
    private String     workingDirectory         = null;
    private double     softwareVersion          = 0.0d;

    public RoverConfig(Properties marsConfig, Properties comsConfig, Properties logsDBConfig, String
            cameraImageCacheLocation, String dataArchiveLocation, String marsConfigLocation) {
        this.marsConfig = marsConfig;
        this.startOfLog = System.currentTimeMillis();
        this.logLevel = marsConfig.getProperty("mars.rover.kernel.log.level");
        this.softwareVersion = Double.parseDouble(marsConfig.getProperty("mars.rover.software.version"));
        this.logArchiveLocation = marsConfig.getProperty("mars.rover.kernel.log.archive");
        this.dbLoggingEnabled = Boolean.parseBoolean(logsDBConfig.getProperty("mars.rover.database.logging" +
                                                                                                 ".enable"));
        this.nasaApiAuthKey = marsConfig.getProperty("nasa.api.authentication.key");
        this.workingDirectory = System.getProperty("user.dir");
        this.marsConfig = marsConfig;
        this.comsConfig = comsConfig;
        this.logDBConfig = logsDBConfig;
        this.dbUserName = logDBConfig.getProperty("mars.rover.database.user");
        this.dbPassword = logDBConfig.getProperty("mars.rover.database.password");
        this.cameraImageCacheLocation = cameraImageCacheLocation;
        this.dataArchiveLocation = dataArchiveLocation;
        this.marsConfigLocation = marsConfigLocation;
    }

    public void setStartOfLog(long startOfLog) {
        this.startOfLog = startOfLog;
    }

    public void setSoftwareVersion(double softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public boolean isDebugEnabled() {
        return logLevel.equalsIgnoreCase("debug");
    }

    public String getMarsConfigLocation() {
        return marsConfigLocation;
    }

    public String getDbUserName() {
        return dbUserName;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getLogArchiveLocation() {
        return logArchiveLocation;
    }

    public long getStartOfLog() {
        return startOfLog;
    }

    public boolean isDbLoggingEnabled() {
        return dbLoggingEnabled;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public Properties getMarsConfig() {
        return marsConfig;
    }

    public Properties getComsConfig() {
        return comsConfig;
    }

    public Properties getLogDBConfig() {
        return logDBConfig;
    }

    public String getCameraImageCacheLocation() {
        return cameraImageCacheLocation;
    }

    public String getDataArchiveLocation() {
        return dataArchiveLocation;
    }

    public String getNasaApiAuthKey() {
        return nasaApiAuthKey;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public double getSoftwareVersion() {
        return softwareVersion;
    }
}
