package space.exploration.mars.rover.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.ByteString;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.dataUplink.CameraPayload;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by sanket on 5/9/17.
 */
public class CameraUtil {

    private static Logger logger = LoggerFactory.getLogger(CameraUtil.class);

    public static void writeImageToFile(BufferedImage bufferedImage, String dataArchivePath) throws IOException {
        if (bufferedImage == null) {
            throw new RuntimeException("BufferedImage was null");
        }

        String fileName = dataArchivePath + ".jpg";
        System.out.println(fileName);
        File outputFile = new File(fileName);
        ImageIO.write(bufferedImage, "jpg", outputFile);
    }

    public static byte[] convertImageToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(10000);
        ImageIO.write(image, "jpg", byteArrayOutputStream);
        byteArrayOutputStream.flush();

        String base64String = Base64.encode(byteArrayOutputStream.toByteArray());
        byteArrayOutputStream.close();

        return Base64.decode(base64String);
    }

    public static CameraPayload.CamPayload convertToCamPayload(String payloadString, String dataArchiveLocation)
            throws IOException {
        CameraPayload.CamPayload.Builder cBuilder = CameraPayload.CamPayload.newBuilder();

        JsonParser jsonParser   = new JsonParser();
        JsonObject jsonObject   = (jsonParser.parse(payloadString)).getAsJsonObject();
        JsonArray  jsonElements = jsonObject.getAsJsonArray("photos");

        String sol       = "";
        String earthDate = "";
        for (JsonElement jsonElement : jsonElements) {
            sol = jsonElement.getAsJsonObject().get("sol").getAsString();
            earthDate = jsonElement.getAsJsonObject().get("earth_date").getAsString();
        }

        for (JsonElement jsonElement : jsonElements) {
            BufferedImage image = getImage(jsonElement);
            if (image != null) {
                writeImageToFile(image, dataArchiveLocation + "/" + sol + "_" + earthDate);
                cBuilder.putImageData(jsonElement.toString(), ByteString.copyFrom(convertImageToByteArray(image)));
            }
        }

        return cBuilder.build();
    }

    private static BufferedImage getImage(JsonElement imgElement) {
        JsonElement imgSrc = imgElement.getAsJsonObject().get("img_src");
        try {
            String actualURLStringSent = imgSrc.getAsString().replaceAll("\"", "");
            System.out.println(actualURLStringSent);
            return ImageIO.read(new URL(actualURLStringSent));
        } catch (MalformedURLException malFormedURL) {
            logger.error("Malformed URL ", malFormedURL);
        } catch (IOException io) {
            logger.error("IOException", io);
        }
        return null;
    }
}
