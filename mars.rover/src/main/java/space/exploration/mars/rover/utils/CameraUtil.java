package space.exploration.mars.rover.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.ByteString;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.service.CameraPayload;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sanket on 5/9/17.
 */
public class CameraUtil {

    private static Logger logger = LoggerFactory.getLogger(CameraUtil.class);

    public static void writeImageToFile(BufferedImage bufferedImage, String dataArchivePath) {
        if (bufferedImage == null) {
            throw new RuntimeException("BufferedImage was null");
        }

        String fileName   = dataArchivePath + ".jpg";
        File   outputFile = new File(fileName);
        logger.debug("CameraUtil trying to write the following fileName = " + fileName);
        try {
            ImageIO.write(bufferedImage, "jpg", outputFile);
        } catch (IOException io) {
            logger.error("Cant write the following file =>" + fileName, io);
        }
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
        logger.debug("Camera Module - archiveLocation = " + dataArchiveLocation);
        CameraPayload.CamPayload.Builder cBuilder = CameraPayload.CamPayload.newBuilder();

        JsonParser jsonParser   = new JsonParser();
        JsonObject jsonObject   = (jsonParser.parse(payloadString)).getAsJsonObject();
        JsonArray  jsonElements = jsonObject.getAsJsonArray("photos");

        List<String> image_source_list = new ArrayList<>();
        String       earthDateDir      = "";
        String       id_Dir            = "";
        for (JsonElement jsonElement : jsonElements) {
            id_Dir = (id_Dir.equals("")) ? jsonElement.getAsJsonObject().get("id").getAsString() : id_Dir;
            earthDateDir = (earthDateDir.equals("")) ? jsonElement.getAsJsonObject().get("earth_date").getAsString()
                    : earthDateDir;

            String imgString = jsonElement.getAsJsonObject().get("img_src").getAsString();
            if (imgString != null) {
                image_source_list.add(imgString);
            }
        }
        processDirectories(dataArchiveLocation + "/" + earthDateDir);
        processDirectories(dataArchiveLocation + "/" + earthDateDir + "/" + id_Dir);

        int imgIndex = 0;
        for (JsonElement jsonElement : jsonElements) {
            BufferedImage image = getImage(jsonElement);
            if (image != null) {
                writeImageToFile(image, dataArchiveLocation + "/" + earthDateDir + "/" + id_Dir +
                        getImageFileName(image_source_list.get(imgIndex)));
                if (imgIndex < image_source_list.size()) {
                    imgIndex++;
                }
                cBuilder.putImageData(jsonElement.toString(), ByteString.copyFrom
                        (convertImageToByteArray(image)));
            }
        }

        return cBuilder.build();
    }

    private static BufferedImage getImage(JsonElement imgElement) {
        JsonElement imgSrc = imgElement.getAsJsonObject().get("img_src");
        try {
            String actualURLStringSent = imgSrc.getAsString().replaceAll("\"", "");
            return ImageIO.read(new URL(actualURLStringSent));
        } catch (MalformedURLException malFormedURL) {
            logger.error("Malformed URL ", malFormedURL);
        } catch (IOException io) {
            logger.error("IOException", io);
        }
        return null;
    }

    private static void processDirectories(String path) {
        File temp = new File(path);
        if (!temp.exists()) {
            temp.mkdir();
        }
    }

    private static String getImageFileName(String imageSource) throws IllegalArgumentException {
        int      endOfFilename   = -1;
        int      startOfFilename = -1;
        String[] fileExtensions  = {".jpg", ".JPG", ".png", ".PNG"};
        String   fileType        = "";
        for (String fileExtension : fileExtensions) {
            endOfFilename = imageSource.indexOf(fileExtension);
            if (endOfFilename != -1) {
                fileType = fileExtension;
                break;
            }
        }

        startOfFilename = imageSource.lastIndexOf("/");

        if ((endOfFilename == -1) || (startOfFilename == -1)) {
            throw new IllegalArgumentException("imageSource string is malformed. " + imageSource);
        }

        return (imageSource.substring(startOfFilename, endOfFilename) + fileType);
    }
}
