package space.exploration.mars.rover.power;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.diagnostics.Pacemaker;
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
    private Logger                   logger    = LoggerFactory.getLogger(BatteryMonitor.class);
    private ScheduledExecutorService scheduler = null;
    private Rover                    rover     = null;

    public BatteryMonitor(Rover rover) {
        this.rover = rover;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void monitor() {
        Runnable bMonitor = new Runnable() {
            @Override
            public void run() {
                logger.info("BatteryMonitor performing due diligence. SCET = " + System.currentTimeMillis() + " " +
                                     "Rover" +
                                     " state = " + rover.getState().getStateName());
                if (rover.getState() == rover.getHibernatingState()) {
                    long timeInRecharge = System.currentTimeMillis() - rover.getInRechargingModeTime();
                    System.out.println("Rover is in hibernating state, timeInRecharge = " + timeInRecharge + " " +
                                               "required = " +
                                               rover.getBattery().getRechargeTime());
                    RoverUtil.roverSystemLog(logger, "Rover is in " + rover.getState().getStateName() +
                            ", timeInRecharge = " +
                            timeInRecharge + " " +
                            "required = " +
                            rover.getBattery().getRechargeTime() + " Rover instructionQueue length = " + rover
                            .getInstructionQueue().size() + " Max Pending messages allowed = " + Pacemaker
                            .MAX_PENDING_MSGS, "INFO");

                    if (timeInRecharge > rover.getBattery().getRechargeTime()) {
                        interrupt();
                        rover.configureBattery(true);
                        rover.getMarsArchitect().getRobot().setColor(EnvironmentUtils.findColor(rover.getMarsConfig()
                                                                                                        .getProperty
                                                                                                                (EnvironmentUtils.ROBOT_COLOR)));

                        rover.setState(rover.getTransmittingState());
                        rover.transmitMessage(rover.getBootupMessage());
                    }
                } else {
                    if ((rover.getState() == rover.getListeningState()) && (rover.getBattery().getPrimaryPowerUnits()
                            <= rover.getBattery().getAlertThreshold())) {
                        logger.error("Battery Monitor has put the rover into hibernating mode.");
                        rover.setState(rover.getHibernatingState());
                        rover.getMarsArchitect().getRobot().setColor(EnvironmentUtils.findColor("robotHibernate"));
                        rover.setInRechargingModeTime(System.currentTimeMillis());
                    }
                }
                rover.getMarsArchitect().getMarsSurface().repaint();
            }
        };
        scheduler.scheduleAtFixedRate(bMonitor, 10, 30, TimeUnit.SECONDS);
    }

    public void interrupt() {
        scheduler.shutdown();
    }
}