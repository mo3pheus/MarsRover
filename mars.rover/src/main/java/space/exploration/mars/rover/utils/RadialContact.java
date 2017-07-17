package space.exploration.mars.rover.utils;

import space.exploration.mars.rover.radar.PolarCoord;

import java.awt.*;

/**
 * Created by sanketkorgaonkar on 6/8/17.
 */
public class RadialContact implements Comparable<RadialContact> {
    private Point                 contactPoint         = null;
    private Point                 center               = null;
    private PolarCoord.PolarPoint polarPoint           = null;
    private boolean               compareOnRadiusFirst = false;

    public RadialContact(Point center, Point contactPoint) {
        this.center = center;
        this.contactPoint = contactPoint;
        this.polarPoint = computePolarPoint();
    }

    public void setPolarPoint(PolarCoord.PolarPoint polarPoint) {
        this.polarPoint = polarPoint;
    }

    public Point getContactPoint() {
        return contactPoint;
    }

    public void setContactPoint(Point contactPoint) {
        this.contactPoint = contactPoint;
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public boolean isCompareOnRadiusFirst() {
        return compareOnRadiusFirst;
    }

    public void setCompareOnRadiusFirst(boolean compareOnRadiusFirst) {
        this.compareOnRadiusFirst = compareOnRadiusFirst;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * <p>
     * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)
     * <p>
     * <p>The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.
     * <p>
     * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.
     * <p>
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     * <p>
     * <p>In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(RadialContact o) {
        int ownTheta     = (int) Math.toDegrees(this.getPolarPoint().getTheta());
        int contactTheta = (int) Math.toDegrees(o.getPolarPoint().getTheta());

        return ownTheta - contactTheta;
    }

    private PolarCoord.PolarPoint computePolarPoint() {
        if (this.center == null || this.contactPoint == null) {
            return null;
        }

        double r = contactPoint.distance(center);
        double theta = Math.acos((center.getX() - contactPoint.getX()) / r);
        theta = Math.toDegrees(theta);

        return PolarCoord.PolarPoint.newBuilder().setR(r).setTheta(theta).build();
    }

    public PolarCoord.PolarPoint getPolarPoint() {
        return polarPoint;
    }
}
