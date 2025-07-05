package src;
//get dependency https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.17/

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import src.sequencer.Sequencer;
import src.sequencer.SmoothVoiceLeading;
import src.sound.MIDIModule;
import src.sound.OSCSender;

public class Main {
    public static int port = 57110;
    public static Process scSynth;
    public static String absPath;
    public static double dir = 0.75;
    public static OSCSender oscSender;

    public static void main(String[] args) {
        /*
         * Sequencer seq = new Sequencer(0);
         * for (int i = 0; i < 100; i++) {
         * seq.getChords(null);
         * }
         */
        try {
            launchSCSynth();
        } catch (URISyntaxException uriSynEx) {
            System.out.println(uriSynEx);
        } catch (IOException ioEx) {
            System.out.println(ioEx);
        }

        oscSender = new OSCSender();

        oscSender.loadBuffs();

        try {
            Thread.sleep(500);
        } catch (InterruptedException interEx) {
            System.out.println(interEx);
        }

        oscSender.addSynthDefs();

        try {
            Thread.sleep(500);
        } catch (InterruptedException interEx) {
            System.out.println(interEx);
        }

        oscSender.setupDefaultGroup();

        new MIDIModule(oscSender);

        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> {
                    scSynth.destroy();
                }));

        permutatingProcess();

    }

    public static void initializeAbsPath() throws URISyntaxException {
        String jarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        System.out.println(jarPath);
        if (jarPath.contains("workspaceStorage")) {// vscode run
            TextIO.readFile("src/pwd.txt");
            jarPath = TextIO.getln();// "/Users/maestro/Documents/Coding/Eternal_Present/Main.java";
        }
        absPath = new File(jarPath).getParentFile().getParentFile().getAbsolutePath() + "/resources";

    }

    public static void launchSCSynth() throws URISyntaxException, IOException {
        initializeAbsPath();
        ProcessBuilder launchSCSynth = new ProcessBuilder(absPath + "/Headless_SCSynth/Resources/scsynth", // "/SuperCollider/Contents/Resources/scsynth",
                "-u",
                port + "");

        new ProcessBuilder("killall", "scsynth").start();// kill any existing scsynth processes to avoid port conflict
        scSynth = launchSCSynth.start();

        InputStream inputStream = scSynth.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        while (!line.equals("SuperCollider 3 server ready.")) {
            line = reader.readLine();
            if (line != null)
                System.out.println(line);
        }

        Thread thread = new Thread() {
            public void run() {
                try {
                    String line = null;
                    while (true) {
                        line = reader.readLine();
                        if (line != null)
                            System.out.println(line);
                    }
                } catch (Exception e) {
                    System.out.println(e);// trying to get to the bottom of why osc isn't sending (specifically phone
                                          // osc)... or is it?
                }
            }
        };
        thread.start();
    }

    public static boolean directionless = false;
    public static void permutatingProcess() {

        Sequencer seq = new Sequencer(1);
        int midC = (int)Math.rint(60 * seq.TET / 12);
        int chordCard = seq.triadDictionary[0].length;


        int[][] lastChords = new int[2][chordCard];// start at middle c
        for(int[] c: lastChords){
            for(int i = 0; i < c.length; i++){
                c[i] = midC;
            }
        }
        for (int i = 0; i < 100; i++) {
            int[][] newChords = seq.getChords();// progress chords
            /*
             * if ((i + 1) % 5 == 0) {
             * System.out.println("ADDING DELAY!");
             * oscSender.addDelay(0.25 + 0.75 * Math.random());
             * }
             */
            for (int n = 0; n < lastChords.length; n++) {
                double target = 8 * 12 * dir + 12;
                target *= seq.TET / 12.0;
                double aveNote = 0;
                for (int[] c : lastChords)
                    for (int note : c)
                        aveNote += note;
                aveNote /= lastChords.length * lastChords[0].length;

                if (directionless || Math.abs(aveNote - target) < seq.TET / 2) {// if chord is within half octave of target, don't apply
                                                          // direction to voiceleading
                    lastChords[n] = SmoothVoiceLeading.smoothVoiceLeading(lastChords[n], newChords[n], seq.TET);
                } else {
                    lastChords[n] = SmoothVoiceLeading.smoothVoiceLeadingToTarget(lastChords[n], newChords[n], target,
                            seq.TET);
                }
            }
            for (int note : lastChords[0]) {
                oscSender.playPnoNote(12 * note / (double) seq.TET, 0.5);
                try {
                    Thread.sleep(62);
                } catch (InterruptedException interEx) {
                    System.out.println(interEx);
                }
            }
            for (int note : lastChords[1]) {
                oscSender.playPnoNote(12 * note / (double) seq.TET, 0.5);
                try {
                    Thread.sleep(62);
                } catch (InterruptedException interEx) {
                    System.out.println(interEx);
                }
            }
        }
    }
}
