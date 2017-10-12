package space.exploration.mars.rover.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class CameraQueryEngine {
    public static final String QUERY_STRING = "https://api.nasa" +
            ".gov/mars-photos/api/v1/rovers/curiosity/photos?sol=1000";
    public static final String API_KEY      = "TLcQa8H1EH0nc7teUJQwP7cIJyqnXtwE25A2vXHP";

    private static Logger logger = LoggerFactory.getLogger(CameraQueryEngine.class);

    public static String getImages(String camId) {
        String         query  = QUERY_STRING + "&camera=" + camId + "&api_key=" + API_KEY;
        BufferedReader in;
        URL            urlObj = null;
        try {
            urlObj = new URL(query);

            HttpURLConnection dataLink = (HttpURLConnection) urlObj
                    .openConnection();
            dataLink.setRequestMethod("GET");
            int responseCode = dataLink.getResponseCode();

            logger.info("Response from weather service = " + Integer.toString(responseCode));
            logger.info("Content type is = " + dataLink.getContentType());

            in = new BufferedReader(new InputStreamReader(dataLink.getInputStream()));
            String       line     = null;
            StringBuffer response = new StringBuffer();
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
