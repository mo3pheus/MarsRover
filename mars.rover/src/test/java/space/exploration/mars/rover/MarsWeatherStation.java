package space.exploration.mars.rover;

import space.exploration.mars.rover.sensor.WeatherSensor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by skorgao on 10/10/2017.
 */
public class MarsWeatherStation {

    /**
     * Created by sanketkorgaonkar on 5/12/17.
     */
    public static class ImageUtil extends JComponent {
        private BufferedImage image = null;

        public ImageUtil(BufferedImage image) {
            this.image = image;
        }

        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage(image, 10, 10, this);
            g2.finalize();
        }
    }

    public static void main(String[] args) {
        URL url = null;
        try {
            url = new URL("http://mars.jpl.nasa.gov/msl-raw-images/proj/msl/redops/ods/surface/sol/01000/opgs/edr/fcam/FLB_486265257EDR_F0481570FHAZ00323M_.JPG");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            BufferedImage imag  = ImageIO.read(url);
            JFrame        frame = new JFrame();
            frame.setBounds(0, 0, 1000, 1000);
            frame.getContentPane().add(new ImageUtil(imag));
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
            Thread.sleep(30000);
            frame.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        WeatherSensor marsWeatherSensor = new WeatherSensor
//                ("http://marsweather.ingenology.com/v1/latest/");
    }
}
