import javax.sound.sampled.*;
import java.io.File;

public class SoundManager {
    private Clip bgmClip;

    public void playSfx(String filename) {
        new Thread(() -> {
            try {
                File soundFile = new File(filename);
                if (soundFile.exists()) {
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioIn);
                    clip.start();
                }
            } catch (Exception e) { /* Silent Fail */ }
        }).start();
    }

    public void playBgm(String filename) {
        new Thread(() -> {
            try {
                File soundFile = new File(filename);
                if (soundFile.exists()) {
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                    bgmClip = AudioSystem.getClip();
                    bgmClip.open(audioIn);
                    bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                    bgmClip.start();
                }
            } catch (Exception e) { /* Silent Fail */ }
        }).start();
    }

    public void stopBgm() {
        if (bgmClip != null && bgmClip.isRunning()) bgmClip.stop();
    }
}