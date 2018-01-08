package space.exploration.mars.rover.service;

import space.exploration.communications.protocol.service.WeatherRDRData;
import space.exploration.mars.rover.utils.WeatherUtil;

import java.io.File;
import java.util.Map;

public class TestWeatherRead {
    public static void main(String[] args) {
        File                                            weatherFile = new File("outputWeatherFile.txt");
        Map<Double,WeatherRDRData.WeatherEnvReducedData> weatherEnvReducedDataMap = WeatherUtil.readWeatherDataFile(weatherFile);
        System.out.println("done");
    }
}
