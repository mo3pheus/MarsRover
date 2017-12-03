package space.exploration.mars.rover.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.service.WeatherData;
import space.exploration.mars.rover.utils.WeatherUtil;

public class WeatherQueryService extends QueryService {
    private Logger logger = LoggerFactory.getLogger(WeatherQueryService.class);

    @Override
    public String getQueryString() {
        return "http://marsweather.ingenology.com/v1/archive/?";
    }

    @Override
    public Object getResponse() {
        Document                   document       = Jsoup.parse(getResponseAsString());
        return WeatherUtil.getweatherPayload(document.body().text());
    }
}
