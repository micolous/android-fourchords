package com.mobileer.midikeyboard.playbackmode;

import com.mobileer.midikeyboard.Chord;
import com.mobileer.midikeyboard.MidiController;

/**
 * Mode that plays a complete chord
 */

public class PMChord implements PlaybackMode {

    @Override
    public void start(MidiController m, Chord c) {
        m.noteOn(c.root);
        m.noteOn(c.third);
        m.noteOn(c.fifth);
        m.noteOn(c.octave);
    }

    @Override
    public void stop(MidiController m, Chord c) {
        m.noteOff(c.root);
        m.noteOff(c.third);
        m.noteOff(c.fifth);
        m.noteOff(c.octave);
    }

    @Override
    public void cycle(MidiController m, Chord c, int count) {

    }
}
