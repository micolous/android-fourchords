package com.mobileer.midikeyboard;

/**
 * Created by michael on 2017-12-27.
 */

public interface MidiController {
    void noteOn(int note);
    void noteOff(int note);
}
