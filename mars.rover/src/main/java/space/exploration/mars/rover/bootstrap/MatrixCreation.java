package space.exploration.mars.rover.bootstrap;

//import org.apache.log4j.FileAppender;
//import org.apache.log4j.Level;
//import org.apache.log4j.PatternLayout;
//import org.apache.log4j.Priority;
//import space.exploration.mars.rover.environment.MarsArchitect;
//import space.exploration.mars.rover.navigation.NavigationEngine;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class MatrixCreation {

    private static Properties config = null;

    public static Properties getConfig() throws IOException {
        URL             url      = MatrixCreation.class.getResource("/testMarsConfig.properties");
        FileInputStream propFile = new FileInputStream(url.getPath());
        config = new Properties();
        config.load(propFile);
        return config;
    }

    public static String getConfigFilePath(){
        URL             url      = MatrixCreation.class.getResource("/testMarsConfig.properties");
        return url.getPath();
    }

    public static Properties getRoverDBConfig() throws IOException {
        URL             url      = MatrixCreation.class.getResource("/roverDB.properties");
        FileInputStream propFile = new FileInputStream(url.getPath());
        config = new Properties();
        config.load(propFile);
        return config;
    }

    public static Properties getRoverDBTestConfig() throws IOException {
        URL             url      = MatrixCreation.class.getResource("/roverDBTest.properties");
        FileInputStream propFile = new FileInputStream(url.getPath());
        config = new Properties();
        config.load(propFile);
        return config;
    }

    public static Properties convertToPropertyFiles(String filePath) throws IOException {
        FileInputStream propFile = new FileInputStream(filePath);
        Properties      config   = new Properties();
        config.load(propFile);
        return config;
    }
}
