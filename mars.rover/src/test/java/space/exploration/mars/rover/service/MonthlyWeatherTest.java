package space.exploration.mars.rover.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class MonthlyWeatherTest {

    private static final String                      URL                         = "http://marsweather.ingenology" +
            ".com/v1/archive/?";
    private static final String                      DATE_FORMAT                 = "yyyy-MM-dd";
    private static       WeatherLongTermQueryService weatherLongTermQueryService = new WeatherLongTermQueryService();

    public static void setUp() {
        weatherLongTermQueryService.setEarthEndDate(1508475600000l);
        weatherLongTermQueryService.setEarthStartDate(1505883600000l);
    }


    public static void main(String[] args) {
        setUp();
        System.out.println(weatherLongTermQueryService.getQueryString());
        weatherLongTermQueryService.executeQuery();

        String queryAsString = weatherLongTermQueryService.getResponseAsString();
        System.out.println(weatherLongTermQueryService.getResponseAsString());

        Document document = Jsoup.parse(queryAsString);

        for (Element e : document.body().getAllElements()) {
            System.out.println(e);
        }
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

