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
//        weatherLongTermQueryService.setEarthEndDate(1508475600000l);
//        weatherLongTermQueryService.setEarthStartDate(1505883600000l);
        weatherLongTermQueryService.setEarthStartDate("2017-09-01");
        weatherLongTermQueryService.setEarthEndDate("2017-09-30");
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

        for(String dayWeather: content.split("},")){
            System.out.println(WeatherUtil.getweatherPayload(dayWeather));
        }

//        int      i        = 0;
//        for (Element e : document.body().getAllElements()) {
//            System.out.println(i++ + " " + e.toString() + " Element tag name = " + e.tag());
//
//        }
    }

//
//    public List<WeatherData.WeatherPayload> getWeatherDataForWindow(String startDate, String endDate) throws
//            IllegalArgumentException {
//        List<WeatherData.WeatherPayload> weatherData = new ArrayList<>();
//
//        if (!validateDates(startDate, endDate)) {
//            throw new IllegalArgumentException("Dates passed in are invalid. startDate = " + startDate + " endDate
// = " +
//                                                       "" + endDate);
//        }
//
//        //Document document = Jsoup.parse(dataLink.get)
//        return null;
//    }
//
//
//    private boolean validateDates(String startDate, String endDate) {
//        DateTimeFormatter formatter   = DateTimeFormat.forPattern(DATE_FORMAT);
//        DateTime          stDateTime  = formatter.parseDateTime(startDate);
//        DateTime          endDateTime = formatter.parseDateTime(endDate);
//
//        boolean valid = (stDateTime.isBefore(endDateTime)) &&
//                (TimeUnit.MILLISECONDS.toDays(endDateTime.getMillis() - stDateTime.getMillis()) < 30);
//        return valid;
//    }
}

