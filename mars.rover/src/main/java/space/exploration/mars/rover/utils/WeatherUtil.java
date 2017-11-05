package space.exploration.mars.rover.utils;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.service.SeasonalWeather;
import space.exploration.communications.protocol.service.WeatherData;

import java.util.ArrayList;
import java.util.List;

public class WeatherUtil {
    private static Logger logger = LoggerFactory.getLogger(WeatherUtil.class);

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
}
