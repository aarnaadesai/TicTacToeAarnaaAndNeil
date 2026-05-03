package tictactoe;

import javax.sound.sampled.*;

public class SoundEngine {
    public static boolean enabled = true;
    public static void toggleMuted() { enabled = !enabled; }
    public static boolean isMuted()  { return !enabled; }

    public static void playClick() { if (!enabled) return; playTone(440, 60, 0.3f); }
    public static void playWin()   {
        if (!enabled) return;
        new Thread(() -> {
            playTone(523, 120, 0.4f);
            playTone(659, 120, 0.4f);
            playTone(784, 200, 0.5f);
        }).start();
    }
    public static void playDraw()  { if (!enabled) return; playTone(300, 300, 0.3f); }
    public static void playPlace() { if (!enabled) return; playTone(500, 80, 0.25f); }
    public static void playUndo()  { if (!enabled) return; playTone(220, 80, 0.25f); }
    public static void playHint()  { if (!enabled) return; playTone(880, 60, 0.2f); }

    private static void playTone(int hz, int ms, float vol) {
        try {
            AudioFormat fmt = new AudioFormat(44100, 16, 1, true, false);
            int samples = (44100 * ms) / 1000;
            byte[] buf = new byte[samples * 2];
            for (int i = 0; i < samples; i++) {
                double angle = 2.0 * Math.PI * i * hz / 44100;
                // Fade out last 20%
                double env = i < samples * 0.8 ? 1.0 : 1.0 - (i - samples * 0.8) / (samples * 0.2);
                short val = (short)(Math.sin(angle) * 32767 * vol * env);
                buf[i*2]   = (byte)(val & 0xFF);
                buf[i*2+1] = (byte)((val >> 8) & 0xFF);
            }
            SourceDataLine line = AudioSystem.getSourceDataLine(fmt);
            line.open(fmt);
            line.start();
            line.write(buf, 0, buf.length);
            line.drain();
            line.close();
        } catch (Exception ignored) {}
    }
}