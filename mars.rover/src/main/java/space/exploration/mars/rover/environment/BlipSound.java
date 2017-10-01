package space.exploration.mars.rover.environment;

import space.exploration.mars.rover.bootstrap.MatrixCreation;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

/**
 * Created by sanket on 6/3/17.
 */
public class BlipSound extends Thread {
    private static AudioStream inputStream = null;

    @Override
    public void run() {
        try {
            inputStream = MatrixCreation.getBlipAudioPath();
            AudioPlayer.player.start(inputStream);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void mute() {
        AudioPlayer.player.stop(inputStream);
    }
}


