import javax.sound.sampled.*;
import java.io.File;
import java.net.URL;

public class Sound {
    private Clip clip;

    public Sound(String filePath) {
        try {
            // Membuka file audio
            File soundFile = new File(filePath);
            if (soundFile.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundFile);
                clip = AudioSystem.getClip();
                clip.open(audioInput);
            } else {
                System.out.println("File audio tidak ditemukan: " + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (clip == null) return;

        // Hentikan jika sedang bunyi (untuk suara cepat berulang)
        if (clip.isRunning()) {
            clip.stop();
        }

        // Kembalikan ke posisi awal (rewind)
        clip.setFramePosition(0);

        // Mainkan
        clip.start();
    }
}