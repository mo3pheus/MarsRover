package space.exploration.mars.rover.communication;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.forkjoin.ThreadLocalRandom;
import space.exploration.mars.rover.InstructionPayloadOuterClass.InstructionPayload;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.kernel.ModuleDirectory;
import space.exploration.mars.rover.kernel.Rover;
import space.exploration.mars.rover.utils.RoverUtil;

import java.util.Properties;

/**
 * Created by sanketkorgaonkar on 4/27/17.
 */
public class Radio implements IsEquipment {
    public static final String      LIFESPAN    = "mars.rover.radio.lifeSpan";
    public static final int         SOS_RESERVE = 10;
    private             Rover       rover       = null;
    private             Transmitter transmitter = null;
    private             Receiver    receiver    = null;
    private             Logger      logger      = LoggerFactory.getLogger(Radio.class);
    private             boolean     endOfLife   = false;
    private             int         lifeSpan    = 0;

    public Radio(Properties comsConfig, Rover rover) {
        this.rover = rover;
        long radioCheckPulse = Long.parseLong(rover.getMarsConfig().getProperty("mars.rover.radio.check.pulse"));

        this.transmitter = new Transmitter(comsConfig);
        this.receiver = new Receiver(comsConfig, this);
        receiver.setRadioCheckPulse(radioCheckPulse);
        receiver.start();

        int lifeSpan = Integer.parseInt(rover.getMarsConfig().getProperty(Radio.LIFESPAN));
        this.lifeSpan = lifeSpan;

        RoverUtil.roverSystemLog(logger, "Radio configured:: " + RoverUtil.getPropertiesAsString(comsConfig), "INFO");
    }

    @Override
    public boolean isEndOfLife() {
        return endOfLife;
    }

    public void setEndOfLife(boolean endOfLife) {
        this.endOfLife = endOfLife;
    }

    public void receiveMessage(InstructionPayload instructionPayload) {
        try {
            if (lifeSpan > SOS_RESERVE) {
                System.out.println("Alert! Alert! Incoming message...");
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
            System.out.println("Radio receive operation has an exception");
            logger.error("Radio receive operation encountered an exception", e);
            rover.writeErrorLog("Radio receive operation encountered an exception", e);
        }
    }

    public void sendMessage(byte[] message) {
        try {
            if (lifeSpan > 0) {
                Thread.sleep(getComsDelaySecs());
                transmitter.transmitMessage(message);
                lifeSpan--;
            } else {
                logger.error("Radio lifeSpan has ended.");
                rover.writeErrorLog("Radio lifeSpan has ended", null);
            }
        } catch (InvalidProtocolBufferException e) {
            System.out.println("Transmit module is in exception - invalidProtocolBuffer ");
            logger.error("InvalidProtocolBufferException error - common guys send me a good message!", e);
            rover.writeErrorLog("InvalidProtocolBufferException error - common guys send me a good message!", e);
        } catch (InterruptedException e) {
            System.out.println("Transmit module is in exception - interruptedException ");
            logger.error("InterruptedException", e);
            rover.writeErrorLog("InterruptedException", e);
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

    private int getComsDelaySecs() {
        return ThreadLocalRandom.current().nextInt(3000, 22000);
    }
}
