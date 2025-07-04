package src.sound.sound_design_experiments;

import java.io.InputStream;
import java.io.IOException;
public class MonoPcmInputStream extends InputStream
{
    private float[] dataFrames;
    private int framesCounter;
    private int cursor;
    private int pcmOut;
    private int[] frameBytes = new int[2];
    private int idx;

    public int framesToRead;

    public void setDataFrames(float[] dataFrames)
    {
        this.dataFrames = dataFrames;
        framesToRead = dataFrames.length;
    }

    @Override
    public int read() throws IOException
    {
        while(available() > 0)
        {
            idx &= 1;//3; 
            if (idx == 0) // set up next frame's worth of data
            {
                framesCounter++; // count elapsing frames

                // scale to 16 bits
                pcmOut = (int)(dataFrames[cursor++] * Short.MAX_VALUE);

                // output as unsigned bytes, in range [0..255]
                frameBytes[0] = (char)pcmOut;
                frameBytes[1] = (char)(pcmOut >> 8);

            }
            
            return frameBytes[idx++]; 
        }
        return -1;
    }

    @Override 
    public int available()
    {
        // NOTE: not concurrency safe.
        // 1st half of sum: there are 4 reads available per frame to be read
        // 2nd half of sum: the # of bytes of the current frame that remain to be read
        return 2 * ((framesToRead - 1) - framesCounter) 
        + (2 - (idx % 2));
    }    

    @Override
    public void reset()
    {
        cursor = 0;
        framesCounter = 0;
        idx = 0;
    }

    @Override
    public void close()
    {
        System.out.println(
            "StereoPcmInputStream stopped after reading frames:" 
            + framesCounter);
    }
}