package space.exploration.mars.rover.utils;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by sanket on 5/9/17.
 */
public class CameraUtil {

    public static byte[] convertImageToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(10000);
        ImageIO.write(image,"jpg", byteArrayOutputStream);
        byteArrayOutputStream.flush();

        String base64String = Base64.encode(byteArrayOutputStream.toByteArray());
        byteArrayOutputStream.close();

        return Base64.decode(base64String);
    }
}
