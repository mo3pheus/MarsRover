package space.exploration.mars.rover.bootstrap;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;

import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.navigation.NavigationEngine;

public class MatrixCreation {

	private static Properties matrixConfig = null;

	public static void main(String[] args) throws IOException {
		configureLogging();
		System.out.println("Hello to the Robo-Maze_World");
		NavigationEngine navEngine = new NavigationEngine(getMatrixConfig());
		new MarsArchitect(getMatrixConfig(), navEngine.getAnimationCalibratedRobotPath());
	}

	public static void configureLogging() {
		FileAppender fa = new FileAppender();
		fa.setFile("roverStatusReports/roverStatus_" + Long.toString(System.currentTimeMillis()) + ".txt");
		fa.setLayout(new PatternLayout("%-4r [%t] %-5p %c %x - %m%n"));
		fa.setThreshold(Level.toLevel(Priority.INFO_INT));
		fa.activateOptions();
		org.apache.log4j.Logger.getRootLogger().addAppender(fa);
	}

	public static Properties getMatrixConfig() throws IOException {
		URL url = MatrixCreation.class.getResource("/mazeDefinition.properties");
		FileInputStream propFile = new FileInputStream(url.getPath());
		matrixConfig = new Properties();
		matrixConfig.load(propFile);
		return matrixConfig;
	}
	
	public static Properties getComsConfig() throws IOException {
		URL url = MatrixCreation.class.getResource("/kafka.properties");
		FileInputStream propFile = new FileInputStream(url.getPath());
		matrixConfig = new Properties();
		matrixConfig.load(propFile);
		return matrixConfig;
	}
}
