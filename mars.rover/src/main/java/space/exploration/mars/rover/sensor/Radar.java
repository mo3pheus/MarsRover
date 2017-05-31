package space.exploration.mars.rover.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.kernel.Rover;
import space.exploration.mars.rover.utils.RoverUtil;

/**
 * Created by sanket on 5/30/17.
 */
public class Radar implements IsEquipment {
    private int     lifeSpan  = 0;
    private Rover   rover     = null;
    private boolean endOfLife = false;
    private Logger  logger    = LoggerFactory.getLogger(Radar.class);

    public Radar(Rover rover) {
        this.rover = rover;
        RoverUtil.roverSystemLog(logger, "Radio configured:: ", "INFO");
    }

    public int getLifeSpan() {
        return lifeSpan;
    }

    public String getEquipmentName() {
        return "Radar";
    }

    @Override
    public boolean isEndOfLife() {
        return endOfLife;
    }

    public void setLifeSpan(int lifeSpan) {
        this.lifeSpan = lifeSpan;
    }

    public void setEndOfLife(boolean endOfLife) {
        this.endOfLife = endOfLife;
    }
}
