package space.exploration.mars.rover.power;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.utils.RoverUtil;

import java.util.Properties;
import java.util.concurrent.Semaphore;

public class Battery implements IsEquipment {
    private final Semaphore accessLock = new Semaphore(1, true);
    private int     primaryPowerUnits;
    private int     auxiliaryPowerUnits;
    private int     alertThreshold;
    private int     rechargeTime;
    private int     lifeSpan;
    private boolean endOfLife;
    private Logger logger = LoggerFactory.getLogger(Battery.class);

    public Battery(Properties batteryConfig) {
        try {
            this.alertThreshold = Integer.parseInt(batteryConfig.getProperty("mars.rover.battery.alertThreshold"));
            this.rechargeTime = Integer.parseInt(batteryConfig.getProperty("mars.rover.battery.rechargeTime"));
            this.primaryPowerUnits = Integer.parseInt(batteryConfig.getProperty("mars.rover.battery" +
                                                                                        ".primaryPowerUnits"));
            this.lifeSpan = Integer.parseInt(batteryConfig.getProperty("mars.rover.battery.lifeSpan"));
            this.auxiliaryPowerUnits = Integer.parseInt(batteryConfig.getProperty("mars.rover.battery" +
                                                                                          ".auxiliaryPowerUnits"));
            RoverUtil.roverSystemLog(logger, "Battery configured, batteryLife:primaryPowerUnits = " + primaryPowerUnits
                    + " auxiliaryPowerUnits = " + auxiliaryPowerUnits + " alertThreshold: " +
                    alertThreshold + " rechargeTime = " + rechargeTime, "INFO"
            );
        } catch (NumberFormatException nfe) {
            logger.error("Battery config is corrupt - please recheck marsConfig.properties", nfe);
        }
    }

    @Override
    public boolean isEndOfLife() {
        return endOfLife;
    }

    public void setEndOfLife(boolean endOfLife) {
        this.endOfLife = endOfLife;
    }

    public synchronized int getPrimaryPowerUnits() {
        return primaryPowerUnits;
    }

    public synchronized void acquireAccessLock(String requestingParty) {
        logger.debug("Access Lock acquired by " + requestingParty);
        try {
            accessLock.acquire();
        } catch (InterruptedException e) {
            logger.error("Exception while setting battery level", e);
        }
    }

    public synchronized void releaseAccessLock(String releasingParty) {
        logger.debug("Access Lock release by " + releasingParty);
        accessLock.release();
    }

    public synchronized void setPrimaryPowerUnits(int primaryPowerUnits) {
        this.primaryPowerUnits = primaryPowerUnits;
    }

    public synchronized int getAuxiliaryPowerUnits() {
        return auxiliaryPowerUnits;
    }

    public synchronized void setAuxiliaryPowerUnits(int auxiliaryPowerUnits) {
        this.auxiliaryPowerUnits = auxiliaryPowerUnits;
    }

    public synchronized boolean requestPower(int powerUnitsRequested, boolean critical) {
        boolean powerAvailable = false;
        if (!critical) {
            powerAvailable = (primaryPowerUnits - auxiliaryPowerUnits) > powerUnitsRequested;
        } else {
            powerAvailable = (primaryPowerUnits > powerUnitsRequested);
        }

        if (!powerAvailable) {
            logger.error("Battery insufficient at time = " + System.currentTimeMillis() + " Power requested = " +
                                 powerUnitsRequested + " Critical = " + critical);
            lifeSpan--;
        }

        return powerAvailable;
    }

    public int getRechargeTime() {
        return rechargeTime;
    }

    public int getAlertThreshold() {
        return alertThreshold;
    }

    public int getLifeSpan() {
        return lifeSpan;
    }

    public void setLifeSpan(int lifeSpan) {
        this.lifeSpan = lifeSpan;
    }

    @Override
    public String getEquipmentName() {
        return "Battery";
    }
}
