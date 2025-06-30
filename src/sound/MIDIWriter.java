package src.sound;

import javax.sound.midi.*;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.plaf.FileChooserUI;

import src.Main;

import java.io.File;

/**
 * Write a description of class MIDIWriter here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class MIDIWriter {
    public static Sequence midiSequence, midiInputSequence;
    public static int res = 256;// 2048;//128;
    public static boolean selectFile = false;
    public static boolean isPianoB = false;

    public static void main(String[] args) throws Exception {
        File inFile = null;

        if (selectFile) {
            Main.initializeAbsPath();
            JFileChooser fileChooser = new JFileChooser(new File(Main.absPath).getParentFile().getAbsolutePath());
            fileChooser.showOpenDialog(null);
        } else {
            inFile = new File("/Users/peanutbutter/NFS_Share/Eternal_Present.mid");
        }
        openMidi(inFile);
        printOutMidiFile();
        File outFile = new File(inFile.getParentFile().getAbsolutePath() + "/filtered_"
                + inFile.getName());
        writeMidiFile(outFile);

    }

    public static void openMidi(File file) {
        try {
            midiInputSequence = MidiSystem.getSequence(file);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "ERROR: " + e);
        }
    }

    public static void printOutMidiFile() {
        Track[] tracks = midiInputSequence.getTracks();
        for (int t = 0; t < tracks.length; t++) {
            System.out.println("TRACK " + t);
            for (int i = 0; i < tracks[t].size(); i++) {
                MidiEvent ev = tracks[t].get(i);
                MidiMessage m = ev.getMessage();
                if (m instanceof ShortMessage && ((ShortMessage) m).getCommand() == ShortMessage.NOTE_ON) {
                    System.out.println("    " + ((ShortMessage) m).getData1() + "," + ev.getTick());
                }
            }
        }
    }

    public static void writeMidiFile(File file) throws Exception {
        MIDIModule.generatePianoMaps();
        try {
            midiSequence = new Sequence(midiInputSequence.getDivisionType(), midiInputSequence.getResolution(), 16);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "ERROR: " + e);
        }

        Track[] tracks = midiInputSequence.getTracks();
        processTrack(tracks[2], (event, sm, isNoteOnOrOff) -> {
            if (isNoteOnOrOff) {
                int outTrackNum = MIDIModule.convertPC(sm.getData1());
                // double note = MIDIModule.convertNote(sm.getData1());
                int midiNote = convertOct(sm.getData1());// (int) Math.rint(note);
                try {
                    sm.setMessage(sm.getStatus(), midiNote, sm.getData2());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "ERROR: " + e);
                }
                midiSequence.getTracks()[outTrackNum].add(event);
            } else {
                for (int i = 0; i < 15; i++) {
                    midiSequence.getTracks()[i].add(event);
                }
            }
        });

        isPianoB = false;
        processTrack(tracks[1], (event, sm, isNoteOnOff) -> {
            if (sm != null && sm.getCommand() == ShortMessage.PITCH_BEND) {// assums the nex message will be the corresponding note on
                isPianoB = true;
                return;
            }

            if (isNoteOnOff && sm.getData2() > 0 && sm.getCommand() == ShortMessage.NOTE_ON) {
                int pc = MIDIModule.convertPC(sm.getData1());
                if (isPianoB) {
                    pc++;// assuming only 4 9 an 14 are used here
                }
                int dev = (int) Math.rint(-0.01 * ((pc % 5) * 20) * 32);
                try {
                    ShortMessage pbMessage = new ShortMessage(ShortMessage.PITCH_BEND, 0, 64, 64 + dev);
                    MidiEvent pbEvent = new MidiEvent(pbMessage, event.getTick() - 1);
                    midiSequence.getTracks()[15].add(pbEvent);
                } catch (InvalidMidiDataException e) {
                    e.printStackTrace();
                }
            }
            midiSequence.getTracks()[15].add(event);
            isPianoB = false;
        });

        processTrack(tracks[0], (event, sm, isNoteOnOrOff) -> {
            if (!isNoteOnOrOff) {
                midiSequence.getTracks()[0].add(event);
            }
        });

        for (int i = 0; i < 15; i++) {
            try {
                int dev = (int) Math.rint(-0.01 * ((i % 5) * 20) * 32);
                if (i % 5 == 4) {
                    dev = (int) (32 * (.2));
                }
                System.out.println(dev);
                ShortMessage pbMessage = new ShortMessage(ShortMessage.PITCH_BEND, 0, 64, 64 + dev);
                midiSequence.getTracks()[i].add(new MidiEvent(pbMessage, 1L));
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        MidiSystem.write(midiSequence, 1, file);
    }

    public static int convertOct(int note) {
        if (note < 60) {
            note -= 24;
            return note + 60;
        }
        return note;
    }

    @FunctionalInterface
    interface ProcessEvent {
        void process(MidiEvent event, ShortMessage sm, boolean isNoteOnOrOff);
    }

    public static void processTrack(Track track, ProcessEvent p) {
        for (int i = 0; i < track.size(); i++) {
            MidiEvent event = track.get(i);
            MidiMessage message = event.getMessage();
            if (message instanceof ShortMessage
                    && (((ShortMessage) message).getCommand() == ShortMessage.NOTE_ON
                            || ((ShortMessage) message).getCommand() == ShortMessage.NOTE_OFF)) {
                ShortMessage sm = (ShortMessage) message;
                p.process(event, sm,
                        sm.getCommand() == ShortMessage.NOTE_ON || sm.getCommand() == ShortMessage.NOTE_OFF);
            } else {
                p.process(event, null, false); // midiSequence.getTracks()[0].add(event);
            }
        }
    }

    public static void initializeMidiSequence() {
        // midiSequence = new Sequence();
        try {
            MetaMessage tempo = new MetaMessage(0x51, new byte[] { 0x03, (byte) 0xD0, (byte) 0x90 }, 3);// 0x07,
                                                                                                        // (byte)0xA1,
                                                                                                        // 0x20 = 120
                                                                                                        // pbm
            // include in byte array the status and the number of bytes? or already covered
            // by other parameters?

            MetaMessage timeSignature = new MetaMessage(0x58, new byte[] { 0x05, 0x03, 0x18, 0x04 }, 4);// 24 metro
                                                                                                        // clicks per
                                                                                                        // midi clock

            midiSequence = new Sequence(Sequence.PPQ, res, 38);// 22050, 12); // my math suggest 1764 not 588
            for (int i = 0; i < 38; i++) {
                byte[] name = new byte[] { (byte) (48 + i) };
                if (i > 9)
                    name = new byte[] { (byte) (49), (byte) (48 + (i % 10)) };

                midiSequence.getTracks()[i].add(new MidiEvent(new MetaMessage(0x03, name, name.length), 0L));
                midiSequence.getTracks()[i]
                        .add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, 64, 127), 0L));
            }
            midiSequence.getTracks()[0].add(new MidiEvent(tempo, 0L));
            midiSequence.getTracks()[0].add(new MidiEvent(timeSignature, 0L));

        } catch (Exception ex) {
        }
    }

    public static void recordMidiNote(int midiNum, double beatNum, double dur, int track) {
        System.out.println("track = " + track);
        System.out.println("dur = " + dur);
        try {
            midiSequence.getTracks()[track].add(
                    new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, midiNum, 64), (long) (beatNum * res / 2)));
            midiSequence.getTracks()[track].add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, 0, midiNum, 0),
                    (long) ((beatNum + dur) * res / 2)));
        } catch (Exception ex) {

        }
    }

    public static void recordMidiNote(int midiNum, double vol, double beatNum, double dur, int track) {
        System.out.println("track = " + track);
        System.out.println("dur = " + dur);
        try {
            midiSequence.getTracks()[track]
                    .add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, midiNum, (int) Math.rint(127 * vol)),
                            (long) (beatNum * res / 2)));
            midiSequence.getTracks()[track].add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, 0, midiNum, 0),
                    (long) ((beatNum + dur) * res / 2)));
        } catch (Exception ex) {

        }
    }

    public static void saveMidi(String fileName) {
        try {

            MidiSystem.write(midiSequence, 1, new File(fileName + ".mid"));
        } catch (Exception ex) {
            System.out.println(ex);
        }
        initializeMidiSequence();
    }
}
