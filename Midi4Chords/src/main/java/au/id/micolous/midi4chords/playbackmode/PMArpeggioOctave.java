package au.id.micolous.midi4chords.playbackmode;

import au.id.micolous.midi4chords.Chord;
import au.id.micolous.midi4chords.MidiController;

/**
 * Implements an Arpeggio playback of the full octave:
 *
 * 1. Play the root
 * 2. Play the third
 * 3. Play the fifth
 * 4. Play the root 1 octave higher
 */

public class PMArpeggioOctave implements PlaybackMode {
    @Override
    public void start(MidiController m, Chord c) {
        cycle(m, c, 0);
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
        switch (count) {
            case 0:
            case 4:
                m.noteOff(c.octave);
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
                m.noteOn(c.octave);
                break;
        }
    }
}
