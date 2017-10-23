package space.exploration.mars.rover.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import junit.framework.TestCase;
import space.exploration.mars.rover.bootstrap.MarsMissionLaunch;
import java.util.concurrent.TimeUnit;

public class PhotoQueryServiceTest extends TestCase {
    private PhotoQueryService photoQueryService = new PhotoQueryService();

    public void setUp() {
        MarsMissionLaunch.configureLogging(true);
        photoQueryService.setAuthenticationKey("DEMO_KEY");
        photoQueryService.setEarthStartDate(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365));
    }

    @org.junit.Test
    public void testFHAZCamera() {
        photoQueryService.setCamId("FHAZ");
        System.out.println("Formatted query = " + photoQueryService.getQueryString());
        photoQueryService.executeQuery();
        System.out.println(photoQueryService.getQueryResponseType().toString());

        String     responseString = photoQueryService.getResponseAsString();
        JsonParser jsonParser     = new JsonParser();
        JsonObject jsonObject     = (jsonParser.parse(responseString)).getAsJsonObject();

        jsonObject.getAsJsonArray("photos");
        JsonArray jsonElements = jsonObject.getAsJsonArray("photos");
        for (JsonElement jsonElement : jsonElements) {
            System.out.println("=================================");
            System.out.println(jsonElement);
        }
    }
}
