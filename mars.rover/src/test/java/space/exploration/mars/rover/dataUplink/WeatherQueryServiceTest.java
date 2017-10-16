package space.exploration.mars.rover.dataUplink;

import junit.framework.TestCase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import space.exploration.mars.rover.utils.WeatherUtil;

public class WeatherQueryServiceTest extends TestCase {
    private WeatherQueryService weatherQueryService = null;

    @Override
    public void setUp() {
        weatherQueryService = new WeatherQueryService();
        weatherQueryService.executeQuery();
    }

    @Test
    public void testNormalWeather() {
        System.out.println(weatherQueryService.getQueryString());

        String temp = weatherQueryService.getResponseAsString();
        System.out.println(temp);

        System.out.println("=====================================================");
        System.out.println("Parsed content");
        Document document = Jsoup.parse(temp);

        System.out.println(document);
        System.out.println("=====================================================");

//        Element bodyElement = document.children().first().children().first();
////        int i = 0;
////        for(Element element: document.children()){
////            //System.out.println(" id " + i++ +":: " + element.tagName() + " "  + element.text());
////            for(Element childElement:element.children()){
////                System.out.println(" id " + i++ +":: " + childElement.tagName() + " "  + childElement.text());
////            }
////        }
//        System.out.println(bodyElement);

//        System.out.println(document.body().text());
//        String targetTag = "\"report\":";
//        String bodyText = document.body().text();
//        int targetPosn = bodyText.indexOf(targetTag);
//        String meat = bodyText.substring(183);
//        System.out.println(meat);
//        String[] elements = meat.split(",");
//        System.out.println("=====================================================");
//        for(String s:elements){
//            //System.out.println(s);
//            for(String p:s.split(":")){
//              System.out.println(p);
//            }
//        }

        String[] attributes = {"\"terrestrial_date\":",
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

        String bodyContent = document.body().text();
        for (String target : attributes) {
            if (!target.equals("\"sunset\":")) {
                System.out.println("=====================================================");
                System.out.println("Parsed Value = " + WeatherUtil.getValue(bodyContent, target));
            }
        }

        //System.out.println(getValue(bodyContent, "\"sunset\""));

    }


}
