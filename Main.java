//get dependency https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.17/

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

public class Main {
    public static int port = 57110;
    public static Process scSynth;
    public static String absPath;

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

        OSCSender oscSender = new OSCSender();

        oscSender.loadBuffs();

        try {
            Thread.sleep(500);
        } catch (InterruptedException interEx) {
            System.out.println(interEx);
        }

        oscSender.addSynthDef();

        try {
            Thread.sleep(500);
        } catch (InterruptedException interEx) {
            System.out.println(interEx);
        }

        oscSender.setupDefaultGroup();
        oscSender.dumpOSC();
        // oscSender.testSynth();

        // oscSender.testSynth1();

        Sequencer seq = new Sequencer(0);
        int[][] lastChords = seq.getChords(new int[][] { { 75, 75, 75, 75, 75 }, { 75, 75, 75, 75, 75 } });
        for (int i = 0; i < 100; i++) {
            for (int note : lastChords[0]) {
                oscSender.playPnoNote(12 * note / (double) seq.TET);
                try {
                    Thread.sleep(62);
                } catch (InterruptedException interEx) {
                    System.out.println(interEx);
                }
            }
            try {
                Thread.sleep(62);
            } catch (InterruptedException interEx) {
                System.out.println(interEx);
            }
            for (int note : lastChords[1]) {
                oscSender.playPnoNote(12 * note / (double) seq.TET);
                try {
                    Thread.sleep(62);
                } catch (InterruptedException interEx) {
                    System.out.println(interEx);
                }
            }
            try {
                Thread.sleep(62);
            } catch (InterruptedException interEx) {
                System.out.println(interEx);
            }

            lastChords = seq.getChords(lastChords);
        }

        scSynth.destroy();

    }

    public static void launchSCSynth() throws URISyntaxException, IOException {
        String jarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        System.out.println(jarPath);
        if (jarPath.startsWith("/Users/maestro/Library/Application Support/Code/User/workspaceStorage")) {// vscode run
            jarPath = "/Users/maestro/Documents/Coding/Eternal_Present/Main.java";
        }
        absPath = new File(jarPath).getParentFile().getAbsolutePath();
        ProcessBuilder launchSCSynth = new ProcessBuilder(absPath + "/SuperCollider/Contents/Resources/scsynth",
                "-u", port + "");

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
}
