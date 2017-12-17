package space.exploration.mars.rover.communication;

import communications.protocol.ModuleDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.kernel.Rover;
import space.exploration.mars.rover.utils.RoverUtil;

import java.util.Properties;

/**
 * Created by sanketkorgaonkar on 4/27/17.
 */
public class Radio implements IsEquipment {
    public static final String      LIFESPAN        = "mars.rover.radio.lifeSpan";
    public static final int         SOS_RESERVE     = 10;
    private             Rover       rover           = null;
    private             Transmitter transmitter     = null;
    private             Receiver    receiver        = null;
    private             Logger      logger          = LoggerFactory.getLogger(Radio.class);
    private             boolean     endOfLife       = false;
    private             double      timeScaleFactor = 0.0d;
    private             int         lifeSpan        = 0;
    private             boolean     bootUp          = true;
    private             Integer     comsDelay       = 0;

    public Radio(Properties comsConfig, Rover rover) {
        this.rover = rover;
        long radioCheckPulse = Long.parseLong(rover.getMarsConfig().getProperty("mars.rover.radio.check.pulse"));
        this.timeScaleFactor = Double.parseDouble(rover.getMarsConfig().getProperty("mars.rover.radio" +
                                                                                            ".timeScaleFactor"));

        this.transmitter = new Transmitter(comsConfig);
        this.receiver = new Receiver(comsConfig, this);
        receiver.setRadioCheckPulse(radioCheckPulse);
        receiver.start();

        this.lifeSpan = Integer.parseInt(rover.getMarsConfig().getProperty(Radio.LIFESPAN));

        RoverUtil.roverSystemLog(logger, "Radio configured:: " + RoverUtil.getPropertiesAsString(comsConfig), "INFO");
    }

    @Override
    public boolean isEndOfLife() {
        return endOfLife;
    }

    public void setEndOfLife(boolean endOfLife) {
        this.endOfLife = endOfLife;
    }

    public void receiveMessage(InstructionPayloadOuterClass.InstructionPayload instructionPayload) {
        try {
            if (lifeSpan > SOS_RESERVE) {
                Thread.sleep(getComsDelaySecs());
                rover.receiveMessage(instructionPayload.toByteArray());
                lifeSpan--;
            } else {
                sendMessage(RoverUtil.getEndOfLifeMessage(ModuleDirectory.Module.COMS, "This is " +
                        Rover.ROVER_NAME + " Radio at " +
                        "end of life. Any last wishes " +
                        "Earth?", rover).toByteArray());
            }
        } catch (Exception e) {
            logger.error("Houston, we have a problem!", e);
            logger.error("The following instruction may have been lost." + instructionPayload.toString());
            rover.writeErrorLog("Houston, we have a problem!", e);
            rover.setState(rover.getListeningState());
        }
    }

    public void sendMessage(byte[] message) {
        try {
            if (lifeSpan > 0) {
                if (!bootUp) {
                    Thread.sleep(getComsDelaySecs());
                }
                transmitter.transmitMessage(message);
                lifeSpan--;
            } else {
                logger.error("Radio lifeSpan has ended.");
                rover.writeErrorLog("Radio lifeSpan has ended", null);
            }
        } catch (InterruptedException e) {
            logger.error("InterruptedException", e);
            rover.writeErrorLog("InterruptedException", e);
        } finally {
            bootUp = false;
        }
    }

    public int getLifeSpan() {
        return lifeSpan;
    }

    public void setLifeSpan(int lifeSpan) {
        this.lifeSpan = lifeSpan;
    }

    @Override
    public String getEquipmentName() {
        return "Radio";
    }

    public Receiver getReceiver() {
        return this.receiver;
    }

    /*
    This needs to be replaced by computed lightTime from SPICE Data

    Requirements:
    1) Radio should go black if Curiosity does not have line of sight with Earth DNS Station
    2) LightTime should be computed - include calculations packet with every telemetry signal
    3) Find the angle of separation between Curiosity Radio Mast and LOS to Earth
        LOS vs HGA, LOS vs LGA and LOS vs UHF
    4) For UHF figure out if MEO/MRO/MOO can deliver data.
    5) Transmit overhead pass time for each satellite along with telemetry.
    */
    private int getComsDelaySecs() {
        try {
            double owlt = rover.getPositionSensor().getPositionsData().getOwltMSLEarth();
            logger.info("One Way Light Time computed by SPICE :: " + Double.toString(owlt));
            comsDelay = (int) (owlt / timeScaleFactor);
        } catch (Exception e) {
            logger.error("Encountered exception when getting comsDelay. There may be a coverage gap at this time." +
                                 "Radio is returning the last known communications delay.", e);
        } finally {
            return comsDelay;
        }
    }
}
