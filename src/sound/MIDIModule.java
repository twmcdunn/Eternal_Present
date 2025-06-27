package src.sound;

import java.util.ArrayList;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

public class MIDIModule implements Receiver {
    Transmitter trans;
    int[] activeNodes;
    static int[] pianoAMap, pianoBMap;
    boolean sustain;
    ArrayList<Integer> notesToEnd;
    OSCSender oscSender;

    // supercollider will ssometime report node not found
    // this is intended behavior. It happens if the note has already decayed
    // before the pedal is lifted.
    public MIDIModule(OSCSender sender) {
        if (sender == null) {
            oscSender = new OSCSender();
        } else {
            oscSender = sender;
        }
        generatePianoMaps();
        activeNodes = new int[128];
        sustain = false;
        notesToEnd = new ArrayList<Integer>();
        try {
            trans = MidiSystem.getTransmitter();
            trans.setReceiver(this);
        } catch (MidiUnavailableException midiUnEx) {

        }
    }

    // tuning logic accessible to other moduels
    public static void generatePianoMaps() {
        pianoAMap = new int[12];
        pianoBMap = new int[12];
        int rot = 1;
        int TET = 15;
        for (int i = 0; i < 12; i++) {
            pianoAMap[i] = (TET * i) / 12;
            pianoBMap[i] = ((TET * (i + rot)) / 12) - (TET * rot / 12);
        }
    }

    public static double convertNote(int note) {
        double TET = 15;
        if (note >= 60) {
            note -= 60;
            int oct = note / 12;
            int pc = note - 12 * oct;
            return (12 * pianoAMap[pc] / TET) + 12 * oct + 60;
        } else {
            note -= 24;
            int oct = note / 12;
            int pc = note - 12 * oct;
            return (12 * pianoBMap[pc] / TET) + 12 * oct + 60;
        }
    }

    public static int convertPC(int note) {
        double TET = 15;
        if (note >= 60) {
            note -= 60;
            int oct = note / 12;
            int pc = note - 12 * oct;
            return pianoAMap[pc];
        } else {
            note -= 24;
            int oct = note / 12;
            int pc = note - 12 * oct;
            return pianoBMap[pc];
        }
    }

    public void close() {
        // trans.close();
    }

    public void send(MidiMessage m, long timeStamp) {
        if (m instanceof ShortMessage) {
            ShortMessage sm = (ShortMessage) m;
            switch (sm.getCommand()) {
                case ShortMessage.NOTE_ON:
                    if (sm.getData2() > 0) {
                        activeNodes[sm.getData1()] = oscSender.playPnoNote(convertNote(sm.getData1()),
                                sm.getData2() / 128.0);
                    } else {
                        executeNoteOff(sm.getData1());
                    }
                    break;
                case ShortMessage.NOTE_OFF:
                    executeNoteOff(sm.getData1());
                    break;
                case ShortMessage.CONTROL_CHANGE:
                    if (sm.getData1() == 64) {
                        sustain = (sm.getData2() >= 64);
                        if (!sustain) {
                            endSustaing();
                        }
                    }
                    break;
            }
        }
    }

    public void executeNoteOff(int midiNum) {
        if (sustain) {
            notesToEnd.add(activeNodes[midiNum]);
        } else {
            oscSender.endNode(activeNodes[midiNum]);
        }
    }

    public void endSustaing() {
        while (notesToEnd.size() > 0) {
            oscSender.endNode(notesToEnd.remove(0));
        }
    }
}
