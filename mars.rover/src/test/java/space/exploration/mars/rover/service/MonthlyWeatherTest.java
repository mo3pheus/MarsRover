package space.exploration.mars.rover.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import space.exploration.mars.rover.bootstrap.MarsMissionLaunch;
import space.exploration.mars.rover.utils.WeatherUtil;

public class MonthlyWeatherTest {

    private static final String                      URL                         = "http://marsweather.ingenology" +
            ".com/v1/archive/?";
    private static final String                      DATE_FORMAT                 = "yyyy-MM-dd";
    private static       WeatherLongTermQueryService weatherLongTermQueryService = new WeatherLongTermQueryService();

    public static void setUp() {
        weatherLongTermQueryService.setEarthStartDate("2016-01-01");
        weatherLongTermQueryService.setEarthEndDate("2016-05-30");
    }


    public static void main(String[] args) {
        MarsMissionLaunch.configureLogging(true);
        setUp();
        System.out.println(weatherLongTermQueryService.getQueryString());
        weatherLongTermQueryService.executeQuery();

        String queryAsString = weatherLongTermQueryService.getResponseAsString();
        System.out.println(queryAsString);

        Document document = Jsoup.parseBodyFragment(queryAsString);
        System.out.println("=====================================================");
        System.out.println(document.body().text());

        System.out.println("=====================================================");

        String textToParse = document.body().text();
        String content     = textToParse.substring(textToParse.lastIndexOf("results"), textToParse.lastIndexOf("]"));
        System.out.println("Proposed Content = " + content);

        for (String dayWeather : content.split("},")) {
            System.out.println(WeatherUtil.getweatherPayload(dayWeather));
        }
    }
}

