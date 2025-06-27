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

    public static void main(String[] args) throws Exception {
        Main.initializeAbsPath();
        JFileChooser fileChooser = new JFileChooser(new File(Main.absPath).getParentFile().getAbsolutePath());
        fileChooser.showOpenDialog(null);
        openMidi(fileChooser.getSelectedFile());
        File outFile = new File(fileChooser.getSelectedFile().getParentFile().getAbsolutePath() + "/filtered_"
                + fileChooser.getSelectedFile().getName());
        writeMidiFile(outFile);

    }

    public static void openMidi(File file) {
        try {
            midiInputSequence = MidiSystem.getSequence(file);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "ERROR: " + e);
        }
    }

    public static void writeMidiFile(File file) throws Exception{
        MIDIModule.generatePianoMaps();
        try {
            midiSequence = new Sequence(midiInputSequence.getDivisionType(), midiInputSequence.getResolution(), 15);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "ERROR: " + e);
        }

        Track[] tracks = midiInputSequence.getTracks();
        for (int t = 0; t < 2; t++) {
            processTrack(tracks[t], (event, sm, isNoteOn) -> {
                int outTrackNum = MIDIModule.convertPC(sm.getData1());
                double note = MIDIModule.convertNote(sm.getData1());
                int midiNote = (int) Math.rint(note);
                try {
                    sm.setMessage(sm.getStatus(), midiNote, sm.getData2());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "ERROR: " + e);
                }
                midiSequence.getTracks()[outTrackNum].add(event);
            });
        }

        for (int i = 0; i < 15; i++) {
            try {
                int dev = 64 + (int) Math.rint(-0.01 * ((i % 5) * 20) * 32);
                ShortMessage pbMessage = new ShortMessage(ShortMessage.PITCH_BEND, 0, 64, 64 + dev);
                midiSequence.getTracks()[i].add(new MidiEvent(pbMessage, 1L));
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        MidiSystem.write(midiSequence, 1, file);
    }

    @FunctionalInterface
    interface ProcessEvent {
        void process(MidiEvent event, ShortMessage sm, boolean isNoteOn);
    }

    public static void processTrack(Track track, ProcessEvent p) {
        for (int i = 0; i < track.size(); i++) {
            MidiEvent event = track.get(i);
            MidiMessage message = event.getMessage();
            if (message instanceof ShortMessage
                    && (((ShortMessage) message).getCommand() == ShortMessage.NOTE_ON
                            || ((ShortMessage) message).getCommand() == ShortMessage.NOTE_OFF)) {
                ShortMessage sm = (ShortMessage) message;
                p.process(event, sm, sm.getCommand() == ShortMessage.NOTE_ON);
            } else {
                midiSequence.getTracks()[0].add(event);
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
