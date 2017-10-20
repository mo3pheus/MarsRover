package space.exploration.mars.rover.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeatherLongTermQueryService extends QueryService {
    private Logger logger = LoggerFactory.getLogger(WeatherQueryService.class);

    @Override
    public String getQueryString() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("http://marsweather.ingenology.com/v1/archive/?");
        queryBuilder.append("terrestrial_date_end=");
        queryBuilder.append(this.earthDateEnd).append("&");
        queryBuilder.append("terrestrial_date_start=").append(this.earthDate);
        return queryBuilder.toString();
    }

    @Override
    public Object getResponse() {
        Document document = Jsoup.parse(getResponseAsString());


        return null;
    }
}
