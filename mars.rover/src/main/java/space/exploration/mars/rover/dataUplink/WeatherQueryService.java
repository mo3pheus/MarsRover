package space.exploration.mars.rover.dataUplink;

public class WeatherQueryService extends QueryService {

    @Override
    public String getQueryString() {
        return "http://marsweather.ingenology.com/v1/latest/";
    }

    @Override
    public Object getResponse() {
        WeatherData.WeatherPayload.Builder wBuilder = WeatherData.WeatherPayload.newBuilder();

        return wBuilder.build();
    }
}


/**

 {
 "report": {
 "terrestrial_date": "2017-10-10",
 "sol": 1841,
 "ls": 72.0,
 "min_temp": -77.0,
 "min_temp_fahrenheit": -106.6,
 "max_temp": -32.0,
 "max_temp_fahrenheit": -25.6,
 "pressure": 870.0,
 "pressure_string": "Higher",
 "abs_humidity": null,
 "wind_speed": null,
 "wind_direction": "--",
 "atmo_opacity": "Sunny",
 "season": "Month 3",
 "sunrise": "2017-10-10T11:00:00Z",
 "sunset": "2017-10-10T22:43:00Z"
 }
 }**/