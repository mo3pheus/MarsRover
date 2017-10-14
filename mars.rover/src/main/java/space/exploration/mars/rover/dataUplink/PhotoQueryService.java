package space.exploration.mars.rover.dataUplink;

import com.google.gson.JsonObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class PhotoQueryService extends QueryService {
    public static final String            DATE_FORMAT       = "YYYY-MM-DD";
    private             String            camId             = "";
    private             DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT);
    private             Logger            logger            = LoggerFactory.getLogger(PhotoQueryService.class);
    private             HttpURLConnection dataLink          = null;


    public void setCamId(String camId) {
        this.camId = camId;
    }

    @Override
    public void setAuthenticationKey(String authenticationKey) {
        this.authenticationKey = authenticationKey;
    }

    @Override
    public void setEarthStartDate(long startMs) {
        this.earthStartDate = new DateTime(startMs);
        this.earthDate = dateTimeFormatter.print(earthStartDate.getMillis());
    }

    @Override
    public void setEarthEndDate(long endMs) {
        this.earthEndDate = new DateTime(endMs);
    }

    @Override
    public Object getQueryResponseType() {
        return dataLink.getContentType();
    }

    @Override
    public String getQueryString() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?earth_date=");
        queryBuilder.append(earthDate);
        queryBuilder.append("&camera=");
        queryBuilder.append(camId);
        queryBuilder.append("&api_key=");
        queryBuilder.append(authenticationKey);

        return queryBuilder.toString();
    }

    @Override
    public void executeQuery() {
        try {
            dataLink = (HttpURLConnection) getTargetUrl().openConnection();
            dataLink.setRequestMethod("GET");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getResponse() {
        try {
            return (JsonObject) dataLink.getContent();
        } catch (IOException e) {
            logger.error("IOException when getting queryResponse ", e);
            return null;
        }
    }

    @Override
    public String getResponseAsString() {
        BufferedReader reader          = null;
        StringBuilder  responseBuilder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(dataLink.getInputStream()));
            String dataLine = null;
            while ((dataLine = reader.readLine()) != null) {
                responseBuilder.append(dataLine);
            }
        } catch (IOException io) {
            logger.error("IOException", io);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                logger.error("IOException", e);
            }
        }
        return responseBuilder.toString();
    }

    private URL getTargetUrl() {
        URL url = null;
        try {
            url = new URL(getQueryString());
        } catch (MalformedURLException malFormedURL) {
            logger.error("URL was malformed.", malFormedURL);
        }
        return url;
    }
}