package space.exploration.mars.rover.utils;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.service.SeasonalWeather;
import space.exploration.communications.protocol.service.WeatherData;
import space.exploration.communications.protocol.service.WeatherRDRData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeatherUtil {
    private static Logger logger = LoggerFactory.getLogger(WeatherUtil.class);

    @Deprecated
    public static WeatherData.WeatherPayload getweatherPayload(String weatherData) {
        WeatherData.WeatherPayload.Builder wBuilder = WeatherData.WeatherPayload.newBuilder();
        wBuilder.setAbsoluteHumidity(getValue(weatherData, "\"abs_humidity\""));
        wBuilder.setAtmosphericOpacity(getValue(weatherData, "\"atmo_opacity"));

        try {
            int sol = Integer.parseInt(getValue(weatherData, "\"sol\":"));
            wBuilder.setSol(sol);
        } catch (NumberFormatException nfe) {
            logger.info("Sol information not present", nfe);
        }

        try {
            double ls = Double.parseDouble(getValue(weatherData, "\"ls\":"));
            wBuilder.setLs(ls);
        } catch (NumberFormatException nfe) {
            logger.info("ls information not present", nfe);
        }

        try {
            double minTemp = Double.parseDouble(getValue(weatherData, "\"min_temp\":"));
            wBuilder.setMinTemp(minTemp);
        } catch (NumberFormatException nfe) {
            logger.info("minTemp information not present", nfe);
        }

        try {
            wBuilder.setMaxTemp(Double.parseDouble(getValue(weatherData, "\"max_temp\":")));
        } catch (NumberFormatException nfe) {
            logger.info("maxTemp information not present", nfe);
        }

        try {
            wBuilder.setMinTemFahrenheit(Double.parseDouble(getValue(weatherData, "\"min_temp_fahrenheit\":")));
        } catch (NumberFormatException nfe) {
            logger.info("minTempFahrenheit information not present", nfe);
        }

        try {
            wBuilder.setMaxTempFahrenheit(Double.parseDouble(getValue(weatherData, "\"max_temp_fahrenheit\":")));
        } catch (NumberFormatException nfe) {
            logger.info("maxTempFahrenheit information not present", nfe);
        }

        try {
            wBuilder.setTerrestrialDate(getValue(weatherData, "\"terrestrial_date\":"));
        } catch (NumberFormatException nfe) {
            logger.info("terrestrialDate information not present", nfe);
        }

        try {
            wBuilder.setWindSpeed(Double.parseDouble(getValue(weatherData, "\"wind_speed\":")));
        } catch (NumberFormatException nfe) {
            logger.info("wind_speed information not present", nfe);
        }

        try {
            wBuilder.setWindDirection(Double.parseDouble(getValue(weatherData, "\"wind_direction\":")));
        } catch (NumberFormatException nfe) {
            logger.info("wind_direction information not present", nfe);
        }

        wBuilder.setPressureDescription(getValue(weatherData, "\"pressure_string\":"));
        wBuilder.setSeason(getValue(weatherData, "\"season\":"));
        wBuilder.setSunrise(getValue(weatherData, "\"sunrise\":"));
        wBuilder.setSunset(getValue(weatherData, "\"sunset\":"));

        logger.debug("Debug Message " + wBuilder.build().toString());

        return wBuilder.build();
    }

    @Deprecated
    public static String getValue(String body, String tag) {
        int    startPosn = body.indexOf(tag);
        String valString = body.substring(startPosn);
        int    endPosn   = valString.indexOf(",");

        if ((endPosn == -1) && (tag.equals("\"sunset\":"))) {
            endPosn = valString.lastIndexOf("}");
        }

        if (endPosn == -1) {
            endPosn = valString.lastIndexOf("\"");
        }

        String relevantText = body.substring(startPosn, startPosn + endPosn);
        relevantText = relevantText.replaceAll(tag, "");
        relevantText = relevantText.replaceAll("\"", "");
        relevantText = relevantText.replaceAll(":", "");
        relevantText = relevantText.replaceAll(" ", "");
        relevantText = relevantText.replaceAll("}", "");
        relevantText = relevantText.replaceAll("\\{", "");
        return relevantText;
    }

    @Deprecated
    public static List<WeatherData.WeatherPayload> getWeatherPayload(String weatherData) {
        List<WeatherData.WeatherPayload> tempWeatherData = new ArrayList<>();

        org.jsoup.nodes.Document document    = Jsoup.parseBodyFragment(weatherData);
        String                   textToParse = document.body().text();
        String content = textToParse.substring(textToParse.lastIndexOf("results"), textToParse
                .lastIndexOf("]"));
        for (String dayWeather : content.split("},")) {
            WeatherData.WeatherPayload weatherPayload = WeatherUtil.getweatherPayload(dayWeather);
            tempWeatherData.add(weatherPayload);
        }
        return tempWeatherData;
    }

    @Deprecated
    public static SeasonalWeather.SeasonalWeatherPayload getSeasonalWeatherPayload(String weatherData) {
        List<WeatherData.WeatherPayload> seasonalWeatherData = getWeatherPayload(weatherData);
        SeasonalWeather.SeasonalWeatherPayload.Builder sBuilder = SeasonalWeather.SeasonalWeatherPayload
                .newBuilder();

        for (WeatherData.WeatherPayload weatherPayload : seasonalWeatherData) {
            sBuilder.addWeatherReports(weatherPayload);
        }

        logger.debug(sBuilder.build().toString());
        return sBuilder.build();
    }

    public static Map<Double,WeatherRDRData.WeatherEnvReducedData> readWeatherDataFile(File weatherDataFile) {
        Map<Double, WeatherRDRData.WeatherEnvReducedData> weatherEnvReducedDataMap = new HashMap<>();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(weatherDataFile));
            String dataLine = null;
            while ((dataLine = bufferedReader.readLine()) != null) {
                try {
                    WeatherRDRData.WeatherEnvReducedData weatherEnvReducedData = extractWeatherData(dataLine);
                    System.out.println(weatherEnvReducedData);
                    weatherEnvReducedDataMap.put(weatherEnvReducedData.getEphemerisTime(),weatherEnvReducedData);
                } catch (Exception e) {
                    logger.debug("Failed to parseWeatherDataLine = " + dataLine);
                }
            }
            bufferedReader.close();
        } catch (IOException io) {
            logger.error("Error while reading weatherFile.", io);
        }
        return weatherEnvReducedDataMap;
    }

    /**
     * @param rawData
     * @return
     * @throws Exception Notes: As brittle as this function looks - NASA doesn't change their dataFormats often.
     *                   Refer to document ENVRDR6.FMT for details of this format.
     */
    public static WeatherRDRData.WeatherEnvReducedData extractWeatherData(String rawData) {
        rawData = rawData.replaceAll("\"", " ");
        rawData = rawData.replaceAll(" \"", " ");
        String[] weatherDataArr = rawData.split(",");

        WeatherRDRData.WeatherEnvReducedData.Builder weatherEnvReducedDataBuilder = WeatherRDRData
                .WeatherEnvReducedData.newBuilder();
        WeatherRDRData.WeatherEnvReducedData.Pressure.Builder pressureBuilder = WeatherRDRData.WeatherEnvReducedData
                .Pressure.newBuilder();
        WeatherRDRData.WeatherEnvReducedData.Humidity.Builder humidityBuilder = WeatherRDRData.WeatherEnvReducedData
                .Humidity.newBuilder();
        WeatherRDRData.WeatherEnvReducedData.BoomAirTemp.Builder boomAirTempBuilder = WeatherRDRData
                .WeatherEnvReducedData.BoomAirTemp.newBuilder();

        // set all the string fields
        weatherEnvReducedDataBuilder.setLocalMeanSolarTime(weatherDataArr[1]);
        weatherEnvReducedDataBuilder.setLocalTrueSolarTime(weatherDataArr[2]);
        weatherEnvReducedDataBuilder.setHsConfidenceLevel(weatherDataArr[62]);
        weatherEnvReducedDataBuilder.setPressureSensorConfig(weatherDataArr[63]);
        weatherEnvReducedDataBuilder.setPressureSensorConfidenceLevel(weatherDataArr[70]);

        try {
            weatherEnvReducedDataBuilder.setEphemerisTime(Double.parseDouble(weatherDataArr[0]));
        } catch (NumberFormatException nfe) {
            logger.debug("No meaningful ephemerisTime", nfe);
        }

        try {
            weatherEnvReducedDataBuilder.setHumiditySensorTemp(Double.parseDouble(weatherDataArr[61]));
        } catch (NumberFormatException nfe) {
            logger.debug("Can't parse humiditySensorTemp for ephemeris time = " + weatherEnvReducedDataBuilder
                    .getEphemerisTime(), nfe);
        }

        // parse AirTemp data boom1
        try {
            boomAirTempBuilder.setBoomAirTemp(Double.parseDouble(weatherDataArr[27]));
            boomAirTempBuilder.setBoomIntermediateAirTemp(Double.parseDouble(weatherDataArr[28]));
            boomAirTempBuilder.setBoomAtsAirTemp(Double.parseDouble(weatherDataArr[29]));
            boomAirTempBuilder.setInternalAirTempUncertainty(Double.parseDouble(weatherDataArr[30]));
            boomAirTempBuilder.setIntermediateTempUncertainty(Double.parseDouble(weatherDataArr[31]));
            boomAirTempBuilder.setTipAirTempUncertainty(Double.parseDouble(weatherDataArr[32]));
            boomAirTempBuilder.setAtsBoomConfidenceLevel(weatherDataArr[33]);
            weatherEnvReducedDataBuilder.setBoomOne(boomAirTempBuilder.build());
        } catch (NumberFormatException nfe) {
            logger.debug("Can't parse boomOneAirTempData for ephemeris time = " + weatherEnvReducedDataBuilder
                    .getEphemerisTime(), nfe);
        }

        // parse AirTemp data boom2
        try {
            boomAirTempBuilder = WeatherRDRData.WeatherEnvReducedData.BoomAirTemp.newBuilder();
            boomAirTempBuilder.setBoomAirTemp(Double.parseDouble(weatherDataArr[34]));
            boomAirTempBuilder.setBoomIntermediateAirTemp(Double.parseDouble(weatherDataArr[35]));
            boomAirTempBuilder.setBoomAtsAirTemp(Double.parseDouble(weatherDataArr[36]));
            boomAirTempBuilder.setInternalAirTempUncertainty(Double.parseDouble(weatherDataArr[37]));
            boomAirTempBuilder.setIntermediateTempUncertainty(Double.parseDouble(weatherDataArr[38]));
            boomAirTempBuilder.setTipAirTempUncertainty(Double.parseDouble(weatherDataArr[39]));
            boomAirTempBuilder.setAtsBoomConfidenceLevel(weatherDataArr[40]);
            weatherEnvReducedDataBuilder.setBoomTwo(boomAirTempBuilder.build());
        } catch (NumberFormatException nfe) {
            logger.debug("Can't parse boomTwoAirTempData for ephemeris time = " + weatherEnvReducedDataBuilder
                    .getEphemerisTime(), nfe);
        }

        // parse channel Humidity
        try {
            // channel A
            humidityBuilder.setPercentage(Double.parseDouble(weatherDataArr[55]));
            humidityBuilder.setUncertainty(Double.parseDouble(weatherDataArr[58]));
            weatherEnvReducedDataBuilder.setChannelAHumidity(humidityBuilder.build());

            //channel B
            humidityBuilder = WeatherRDRData.WeatherEnvReducedData.Humidity.newBuilder();
            humidityBuilder.setPercentage(Double.parseDouble(weatherDataArr[56]));
            humidityBuilder.setUncertainty(Double.parseDouble(weatherDataArr[59]));
            weatherEnvReducedDataBuilder.setChannelBHumidity(humidityBuilder.build());

            //channel C
            humidityBuilder = WeatherRDRData.WeatherEnvReducedData.Humidity.newBuilder();
            humidityBuilder.setPercentage(Double.parseDouble(weatherDataArr[57]));
            humidityBuilder.setUncertainty(Double.parseDouble(weatherDataArr[60]));
            weatherEnvReducedDataBuilder.setChannelCHumidity(humidityBuilder.build());

            weatherEnvReducedDataBuilder.setHumiditySensorTemp(Double.parseDouble(weatherDataArr[61]));
        } catch (NumberFormatException nfe) {
            logger.debug("Can't parse humidityData for ephemeris time = " + weatherEnvReducedDataBuilder
                    .getEphemerisTime(), nfe);
        }

        // parse Pressure Data
        try {
            // pressureSensor 1
            pressureBuilder.setThermoCapTemp(Double.parseDouble(weatherDataArr[64]));
            pressureBuilder.setBaroCapPressure((Double.parseDouble(weatherDataArr[66])));
            pressureBuilder.setBaroCapAbsoluteUncertainty((Double.parseDouble(weatherDataArr[68])));
            weatherEnvReducedDataBuilder.setPressureSensor1(pressureBuilder.build());

            // pressureSensor 2
            pressureBuilder = WeatherRDRData.WeatherEnvReducedData.Pressure.newBuilder();
            pressureBuilder.setThermoCapTemp(Double.parseDouble(weatherDataArr[65]));
            pressureBuilder.setBaroCapPressure((Double.parseDouble(weatherDataArr[67])));
            pressureBuilder.setBaroCapAbsoluteUncertainty((Double.parseDouble(weatherDataArr[69])));
            weatherEnvReducedDataBuilder.setPressureSensor2(pressureBuilder.build());
        } catch (NumberFormatException nfe) {
            logger.debug("Can't parse pressureData for ephemeris time = " + weatherEnvReducedDataBuilder
                    .getEphemerisTime(), nfe);
        }

        return weatherEnvReducedDataBuilder.build();
    }
}
