package space.exploration.mars.rover.Radar;

import space.exploration.mars.rover.utils.RadialContact;

import java.awt.*;

/**
 * Created by sanket on 6/19/17.
 */
public class RadialContactsTest {

    public static void main(String[] args) {
        RadialContact radialContact = new RadialContact(new Point(100, 100), new Point(100, 125));
        System.out.println(" Angle = " + radialContact.getPolarPoint().getTheta());
    }
}
