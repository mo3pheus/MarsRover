package space.exploration.mars.rover.service;

import util.FileUtil;
import util.ImageUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestImageRetrieval {

    public static void main(String[] args) throws Exception {
        Path path = Paths.get(args[0]);
        System.out.println(path.toString());
        byte[] content = Files.readAllBytes(path);

        System.out.println("Content is " + content.length + " bytes long.");

        BufferedImage imag  = ImageIO.read(new ByteArrayInputStream(content));
        JFrame        frame = new JFrame();
        frame.setBounds(0, 0, 3000, 3000);
        frame.getContentPane().add(new ImageUtil(imag));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.repaint();

    }
}
