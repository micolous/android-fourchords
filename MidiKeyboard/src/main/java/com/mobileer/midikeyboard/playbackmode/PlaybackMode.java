package com.mobileer.midikeyboard.playbackmode;

import com.mobileer.midikeyboard.Chord;
import com.mobileer.midikeyboard.MidiController;

/**
 * Created by michael on 2017-12-27.
 */

public interface PlaybackMode {
    void start(MidiController m, Chord c);
    void stop(MidiController m, Chord c);
    void cycle(MidiController m, Chord c, int count);
}
