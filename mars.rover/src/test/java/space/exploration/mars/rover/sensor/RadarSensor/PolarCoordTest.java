package space.exploration.mars.rover.sensor.RadarSensor;

import space.exploration.mars.rover.utils.RadialContact;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by sanket on 6/10/17.
 */
public class PolarCoordTest {

    public static void main(String[] args) {
        Point a = new Point(0, 0);
        Point b = new Point(45, 45);
        Point c = new Point(100, 100);

        RadialContact bR = new RadialContact(a, b);
        RadialContact cR = new RadialContact(a, c);

        System.out.println("bR center = " + bR.getCenter().toString() + " contact= " + bR.getContactPoint().toString()
                + " bR polarPoint = " + bR.getPolarPoint().toString()
        );

        System.out.println(" Angle = " + Math.toDegrees(bR.getPolarPoint().getTheta()));

        System.out.println("cR center = " + cR.getCenter().toString() + " contact= " + cR.getContactPoint().toString()
                + " cR polarPoint = " + cR.getPolarPoint().toString()
        );

        System.out.println(" Angle = " + Math.toDegrees(cR.getPolarPoint().getTheta()));

        if (bR.compareTo(cR) > 0) {
            System.out.println(" b > c");
        } else {
            System.out.println(" c > b");
        }

        java.util.List<RadialContact> list = new ArrayList<>();
        list.add(bR);
        list.add(cR);

        Collections.sort(list);

        for (RadialContact r : list) {
            System.out.println(r.getPolarPoint().getR() + " " + r.getPolarPoint().getTheta());
        }

        System.out.println("========================================");

        Point center       = new Point(350, 350);
        Point contactPoint = new Point(343, 343);

        double r = contactPoint.distance(center);
        //double theta = Math.acos((center.getX() - contactPoint.getX()) / r);
        double theta = Math.acos((contactPoint.getX() - center.getX()) / r);
        theta = Math.toDegrees(theta);
        System.out.println(theta);

    }
}
