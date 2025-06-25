import com.illposed.osc.transport.*;
import com.illposed.osc.*;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Write a description of class OSCSender2 here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class OSCSender {
    private OSCPortOut sender;
    public static int[] midiNums = { 21, 23, 25, 27, 29, 31, 33, 35, 37, 39, 41, 43, 45, 47, 49, 51, 53, 55, 57, 59, 61,
            63, 65, 67, 69, 71,
            73, 75, 77, 79, 81, 83, 85, 87, 89, 91, 93, 95, 97, 99, 101, 103, 105, 107, 108 };
    public int curNodeID;
    int buffNumToLoad;
    public static ArrayList<ArrayList<int[]>> buffMidiData = new ArrayList<ArrayList<int[]>>();

    public OSCSender() {
        try {
            sender = new OSCPortOut(InetAddress.getLocalHost(), Main.port);// 57120
            System.out.println(InetAddress.getLocalHost());
        } catch (IOException ioEx) {
            System.out.println(ioEx);
        }
        curNodeID = 1001;
        buffNumToLoad = 0;
        for (int i = 0; i < 3; i++)
            buffMidiData.add(new ArrayList<int[]>());
    }

    public void sendTextureMessage(int voice, int status, float[] data) {
        try {
            List<Object> args = new ArrayList<>();
            // args.add("texture");
            args.add(status);
            args.add(voice);
            for (float f : data)
                args.add(f);
            OSCMessage msg = new OSCMessage("/texture", args);
            sender.send(msg);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void sendPhoneAdvanceMessage() {
        try {
            List<Object> args = new ArrayList<>();
            // args.add("texture");
            args.add(new Integer(1000));
            OSCMessage msg = new OSCMessage("/mrmr/pushbutton/0/jedermann", args);
            sender.send(msg);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void loadBuffs() {
        final String directoryPath = Main.absPath + "/piano_samples/samples/all";

        try {
            Files.walk(Paths.get(directoryPath))
                    .filter(path -> path.toString().toLowerCase().endsWith(".wav"))
                    .forEach(path -> {
                        String p = path.toString();
                        p = p.substring(p.lastIndexOf("/") + 1);
                        // store midi - buff num dictionary entries
                        int dyn = Integer.parseInt(p.substring(10, 11)) - 1;
                        int num = Integer.parseInt(p.substring(16, 19));
                        buffMidiData.get(dyn).add(new int[] { midiNums[num], buffNumToLoad });

                        List<Object> args = new ArrayList<>();
                        args.add(buffNumToLoad++);
                        args.add(path.toString());
                        OSCMessage msg = new OSCMessage("/b_allocRead", args);
                        try {
                            sender.send(msg);
                        } catch (IOException ioex) {
                            System.out.println(ioex);
                        } catch (OSCSerializeException oscSerEx) {
                            System.out.println(oscSerEx);
                        }

                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addSynthDef() {
        List<Object> args = new ArrayList<>();
        args.add("/Users/maestro/Documents/Coding/Eternal_Present/sc_data/pno.scsyndef");
        OSCMessage msg = new OSCMessage("/d_load", args);
        try {
            sender.send(msg);
        } catch (IOException ioex) {
            System.out.println(ioex);
        } catch (OSCSerializeException oscSerEx) {
            System.out.println(oscSerEx);
        }
    }

    public void setupDefaultGroup() {
        try {
            // Create default group with ID 1
            List<Object> groupArgs = new ArrayList<>();
            groupArgs.add(1); // group ID
            groupArgs.add(0); // add action (addToHead)
            groupArgs.add(0); // target (RootNode)

            OSCMessage groupMsg = new OSCMessage("/g_new", groupArgs);
            sender.send(groupMsg);

            // Small delay to ensure group is created
            Thread.sleep(10);

        } catch (Exception e) {
            System.out.println("Error creating default group: " + e.getMessage());
        }
    }

    public void dumpOSC() {
        List<Object> args = new ArrayList<>();
        args.add(1);
        OSCMessage msg = new OSCMessage("/dumpOSC", args);
        try {
            sender.send(msg);
        } catch (IOException ioex) {
            System.out.println(ioex);
        } catch (OSCSerializeException oscSerEx) {
            System.out.println(oscSerEx);
        }
    }

    public void testSynth1() {
        List<Object> args = new ArrayList<>();
        args.add("pno");// name
        args.add(curNodeID++);// id
        args.add(0);// add to head of target
        args.add(0);// target

        // testBuffnum
        args.add(0);
        args.add(0f);// "\\mid");
        // args.add(61.0f);// buffNum
        OSCMessage msg = new OSCMessage("/s_new", args);
        try {
            sender.send(msg);
        } catch (IOException ioex) {
            System.out.println(ioex);
        } catch (OSCSerializeException oscSerEx) {
            System.out.println(oscSerEx);
        }
    }

    public void testSynth() {
        List<Object> args = new ArrayList<>();
        args.add("pno");// name
        args.add(curNodeID++);// id
        args.add(0);// add to head of target
        args.add(1);// target

        // testBuffnum
        // args.add(61.0f);// buffNum
        OSCMessage msg = new OSCMessage("/s_new", args);
        try {
            sender.send(msg);
        } catch (IOException ioex) {
            System.out.println(ioex);
        } catch (OSCSerializeException oscSerEx) {
            System.out.println(oscSerEx);
        }
    }

    public void playPnoNote(double midiNum) {
       // midiNum *= 15;
        int closestMid = -1;
        double dist = Double.MAX_VALUE;
        int buffNum = 0;

        int dyn = 1;

        for (int[] datum : buffMidiData.get(dyn)) {
            int mid = datum[0];
            if (Math.abs(mid - midiNum) < dist) {
                dist = Math.abs(mid - midiNum);
                closestMid = mid;
                buffNum = datum[1];
            }
        }
        List<Object> args = new ArrayList<>();
        args.add("pno");// name
        args.add(curNodeID++);// id
        args.add(0);// add to head of target
        args.add(0);// target

        args.add("buff");
        args.add((float) buffNum);// buffNum
        args.add("origMid");
        args.add((float) closestMid);

        args.add("mid");
        args.add((float) midiNum);
        // vol and key?

        OSCMessage msg = new OSCMessage("/s_new", args);
        try {
            sender.send(msg);
        } catch (IOException ioex) {
            System.out.println(ioex);
        } catch (OSCSerializeException oscSerEx) {
            System.out.println(oscSerEx);
        }
    }
}
