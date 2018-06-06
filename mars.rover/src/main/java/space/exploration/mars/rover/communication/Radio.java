package space.exploration.mars.rover.communication;

import com.google.protobuf.ByteString;
import com.yammer.metrics.core.Meter;
import communications.protocol.ModuleDirectory;
import encryption.EncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;
import space.exploration.communications.protocol.security.SecureMessage;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.kernel.Rover;
import space.exploration.mars.rover.utils.RoverUtil;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by sanketkorgaonkar on 4/27/17.
 */
public class Radio implements IsEquipment {
    public static final  String      LIFESPAN        = "mars.rover.radio.lifespan";
    private static final String      CERT_LOCATION   = "src/main/resources/certificates/server.ser";
    public static final  int         SOS_RESERVE     = 10;
    private              Rover       rover           = null;
    private              Transmitter transmitter     = null;
    private              Receiver    receiver        = null;
    private              Logger      logger          = LoggerFactory.getLogger(Radio.class);
    private              boolean     endOfLife       = false;
    private              double      timeScaleFactor = 0.0d;
    private              int         lifeSpan        = 0;
    private              boolean     bootUp          = true;
    private              Integer     comsDelay       = 0;
    private              Meter       requests        = null;
    protected            File        comsCertificate = null;

    public Radio(Properties comsConfig, Rover rover) {
        this.rover = rover;
        requests = this.rover.getMetrics().newMeter(Radio.class, "Radio", "requests", TimeUnit.HOURS);
        long radioCheckPulse = Long.parseLong(rover.getMarsConfig().getProperty("mars.rover.radio.check.pulse"));
        this.timeScaleFactor = Double.parseDouble(rover.getMarsConfig().getProperty("mars.rover.radio" +
                                                                                            ".timeScaleFactor"));

        this.transmitter = new Transmitter(comsConfig);
        this.receiver = new Receiver(comsConfig, this);
        receiver.setRadioCheckPulse(radioCheckPulse);
        receiver.start();

        this.lifeSpan = Integer.parseInt(rover.getMarsConfig().getProperty(LIFESPAN));
        RoverUtil.roverSystemLog(logger, "Radio configured:: " + RoverUtil.getPropertiesAsString(comsConfig), "INFO");

        this.comsCertificate = new File(CERT_LOCATION);
    }

    @Override
    public boolean isEndOfLife() {
        return endOfLife;
    }

    @Override
    public long getRequestMetric() {
        return requests.count();
    }

    @Override
    public String getEquipmentLifeSpanProperty() {
        return "mars.rover.radio.lifespan";
    }

    public void setEndOfLife(boolean endOfLife) {
        this.endOfLife = endOfLife;
    }

    public void receiveMessage(SecureMessage.SecureMessagePacket secureMessagePacket) {
        requests.mark();
        try {
            if (lifeSpan > SOS_RESERVE) {
                Thread.sleep(getComsDelaySecs());
                byte[] decryptedContents = EncryptionUtil.decryptContent(comsCertificate, secureMessagePacket);
                InstructionPayloadOuterClass.InstructionPayload instructionPayload = InstructionPayloadOuterClass
                        .InstructionPayload.parseFrom(decryptedContents);
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
            logger.error("The following instruction may have been lost." + secureMessagePacket.toString());
            RoverUtil.writeErrorLog(rover, "Houston, we have a problem!", e);
            rover.setState(rover.getListeningState());
        }
    }

    public void sendMessage(byte[] message) {
        requests.mark();
        try {
            if (lifeSpan > 0) {
                if (!bootUp) {
                    Thread.sleep(getComsDelaySecs());
                }
                SecureMessage.SecureMessagePacket secureMessagePacket = EncryptionUtil.encryptData(Rover.ROVER_NAME,
                                                                                                   comsCertificate,
                                                                                                   message);
                logger.info("Message encryption successful.");
                transmitter.transmitMessage(secureMessagePacket.toByteArray());
                lifeSpan--;
            } else {
                logger.error("Radio lifeSpan has ended.");
                RoverUtil.writeErrorLog(rover, "Radio lifeSpan has ended", null);
            }
        } catch (Exception e) {
            rover.setState(rover.getListeningState());
            logger.error("Exception", e);
            RoverUtil.writeErrorLog(rover, "Exception", e);
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

    private int getComsDelaySecs() {
        try {
            double owlt = rover.getPositionSensor().getPositionsData().getOwltMSLEarth();
            logger.info("One Way Light Time computed by SPICE :: " + Double.toString(owlt));
            comsDelay = (int) (owlt / timeScaleFactor);
        } catch (Exception e) {
            logger.error("Encountered exception when getting comsDelay. There may be a coverage gap at this time." +
                                 "Radio is returning the last known communications delay." + comsDelay + " utcTime = " +
                                 "" + rover.getSpacecraftClock().getUTCTime(), e);
        } finally {
            return comsDelay;
        }
    }

    public void stopRadio() {
        logger.info("Radio stopped");
        receiver.stopReceiver();
        transmitter = null;
    }
}
