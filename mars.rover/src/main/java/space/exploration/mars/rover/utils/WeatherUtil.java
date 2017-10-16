package space.exploration.mars.rover.utils;

import space.exploration.mars.rover.dataUplink.WeatherData;

public class WeatherUtil {

    private static final String[] WEATHER_ATTRS = {"\"terrestrial_date\":",
                                                   "\"sol\":",
                                                   "\"ls\":",
                                                   "\"min_temp\":",
                                                   "\"min_temp_fahrenheit\":",
                                                   "\"max_temp\":",
                                                   "\"max_temp_fahrenheit\":",
                                                   "\"pressure\":",
                                                   "\"pressure_string\":",
                                                   "\"abs_humidity\":",
                                                   "\"wind_speed\":",
                                                   "\"wind_direction\":",
                                                   "\"atmo_opacity\":",
                                                   "\"season\":",
                                                   "\"sunrise\":",
                                                   "\"sunset\":",
                                                   };

    public static WeatherData.WeatherPayload getweatherPayload(String weatherData) {


        return null;
    }

    public static String getValue(String body, String tag) {
        int    startPosn = body.indexOf(tag);
        String valString = body.substring(startPosn);
        int    endPosn   = valString.indexOf(",");
        return body.substring(startPosn, startPosn + endPosn);
    }

    private static Object extractValue(String body, String tag) {
        int    startPosn = body.indexOf(tag);
        String valString = body.substring(startPosn);
        int    endPosn   = valString.indexOf(",");
        return body.substring(startPosn, startPosn + endPosn);
    }
}
