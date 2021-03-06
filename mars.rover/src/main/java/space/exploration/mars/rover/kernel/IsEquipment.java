package space.exploration.mars.rover.kernel;

/**
 * Created by sanketkorgaonkar on 5/16/17.
 */
public interface IsEquipment {
    int getLifeSpan();

    String getEquipmentName();

    boolean isEndOfLife();

    long getRequestMetric();

    String getEquipmentLifeSpanProperty();
}
