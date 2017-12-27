package com.mobileer.midikeyboard.playbackmode;

import com.mobileer.midikeyboard.Chord;
import com.mobileer.midikeyboard.MidiController;

/**
 * Created by michael on 2017-12-27.
 */

public class PMArpeggio implements PlaybackMode {
    @Override
    public void start(MidiController m, Chord c) {
        cycle(m, c, 0);
    }

    @Override
    public void stop(MidiController m, Chord c) {
        m.noteOff(c.root);
        m.noteOff(c.third);
        m.noteOff(c.fifth);
    }

    @Override
    public void cycle(MidiController m, Chord c, int count) {
        switch (count) {
            case 0:
            case 4:
                m.noteOff(c.third);
                m.noteOn(c.root);
                break;
            case 1:
            case 5:
                m.noteOff(c.root);
                m.noteOn(c.third);
                break;
            case 2:
            case 6:
                m.noteOff(c.third);
                m.noteOn(c.fifth);
                break;
            case 3:
            case 7:
                m.noteOff(c.fifth);
                m.noteOn(c.third);
                break;
        }
    }
}
