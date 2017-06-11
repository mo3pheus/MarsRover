package space.exploration.mars.rover.power;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.kernel.Rover;
import space.exploration.mars.rover.utils.RoverUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by sanket on 6/11/17.
 */
public class BatteryMonitor {
    private Logger                   logger    = LoggerFactory.getLogger(Battery.class);
    private ScheduledExecutorService scheduler = null;
    private Rover                    rover     = null;

    public BatteryMonitor(int threadPoolCount, Rover rover) {
        this.rover = rover;
        this.scheduler = Executors.newScheduledThreadPool(threadPoolCount);
    }

    public void monitor() {
        Runnable bMonitor = new Runnable() {
            @Override
            public void run() {
                logger.info("BatteryMonitor performing due diligence. SCET = " + System.currentTimeMillis());
                if (rover.getState() == rover.getHibernatingState()) {
                    long timeInRecharge = System.currentTimeMillis() - rover.getInRechargingModeTime();
                    System.out.println("Rover is in hibernating state, timeInRecharge = " + timeInRecharge + " " +
                                               "required = " +
                                               rover.getBattery().getRechargeTime());
                    RoverUtil.roverSystemLog(logger, "Rover is in hibernating state, timeInRecharge = " +
                            timeInRecharge + " " +
                            "required = " +
                            rover.getBattery().getRechargeTime(), "INFO");

                    if (timeInRecharge > rover.getBattery().getRechargeTime()) {
                        rover.configureBattery();
                        rover.getMarsArchitect().getRobot().setColor(EnvironmentUtils.findColor(rover.getMarsConfig()
                                                                                                        .getProperty
                                                                                                                (EnvironmentUtils.ROBOT_COLOR)));

                        rover.setState(rover.getTransmittingState());
                        rover.transmitMessage(rover.getBootupMessage());
                    }
                } else {
                    if (rover.getBattery().getPrimaryPowerUnits() <= rover.getBattery().getAlertThreshold()) {
                        System.out.println("Going into hibernating mode!");
                        rover.setState(rover.getHibernatingState());
                        rover.transmitMessage(rover.getHibernatingAlertMessage());
                        rover.getMarsArchitect().getRobot().setColor(EnvironmentUtils.findColor("robotHibernate"));
                        rover.setInRechargingModeTime(System.currentTimeMillis());
                        RoverUtil.roverSystemLog(logger, ("Rover reporting powerConsumed, remaining power = " + rover
                                .getBattery()
                                .getPrimaryPowerUnits() + " " +
                                "at time = " + System.currentTimeMillis()), "INFO");
                    }
                }
                rover.getMarsArchitect().getMarsSurface().repaint();
            }
        };
        scheduler.scheduleAtFixedRate(bMonitor, 30, 30, TimeUnit.SECONDS);
    }
}