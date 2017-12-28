package au.id.micolous.midi4chords;

/**
 * Interface that represents simple MIDI commands.
 */

public interface MidiController {
    void noteOn(int note);
    void noteOff(int note);
}
