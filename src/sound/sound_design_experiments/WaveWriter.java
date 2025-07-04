package src.sound.sound_design_experiments;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Write a description of class WaveWriter here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class WaveWriter {
    // instance variables - replace the example below with your own
    public MonoPcmInputStream mpis;
    public float[][] df;
    public static final int SAMPLE_RATE = 48000;
    public int length = SAMPLE_RATE * 60 * 30;
    public String fileName;

    /**
     * Constructor for objects of class WaveWriter
     */
    public WaveWriter(String name) {

        df = new float[8][length];
        fileName = name;

        // render();
    }

    public void render() {
        render(8);
    }

    // render wave file
    public void render(int chans) {
        double max = 0;
        for (int chan = 0; chan < chans; chan++) {
            for (int i = 0; i < df[chan].length; i++) {
                max = Math.max(max, Math.abs(df[chan][i]));
            }
        }
        for (int chan = 0; chan < chans; chan++) {
            for (int i = 0; i < df[chan].length; i++) {
                df[chan][i] /= max;
            }
        }
        for (int chan = 0; chan < chans; chan++) {
            mpis = new MonoPcmInputStream();
            /*
             * float peak = 0;
             * for(int i = 0; i < df[chan].length; i++){
             * peak = Math.max(peak, Math.abs(df[chan][i]));
             * }
             * for(int i = 0; i < df[chan].length; i++){
             * df[chan][i] /= peak;
             * }
             */

            for (int i = df[chan].length - 1; i >= 0; i--) {
                if (df[chan][i] != 0) {
                    df[chan] = Arrays.copyOfRange(df[chan], 0, i);
                    break;
                }
            }

            mpis.setDataFrames(df[chan]);

            AudioFormat af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, SAMPLE_RATE, 16, 1, 2, SAMPLE_RATE,
                    false);
            try {
                AudioSystem.write(new AudioInputStream(mpis, af, mpis.framesToRead), AudioFileFormat.Type.WAVE,
                        new File(fileName + chan + ".wav"));
            } catch (Exception e) {
            }
        }
    }

}
