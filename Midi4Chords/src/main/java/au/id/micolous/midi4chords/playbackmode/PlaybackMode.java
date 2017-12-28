package au.id.micolous.midi4chords.playbackmode;

import au.id.micolous.midi4chords.Chord;
import au.id.micolous.midi4chords.MidiController;

/**
 * Interface for playback modes.
 *
 * These should be simple and not contain state, as they will all be instanciated immediately, and
 * reused throughout the execution of the program.
 */

public interface PlaybackMode {
    /**
     * Called when playback starts.  If your playback mode has multiple "frames", then this should
     * call this.cycle(m, c, 0);
     * @param m Interface to the MIDI device
     * @param c Chord to play
     */
    void start(MidiController m, Chord c);

    /**
     * Called to end all playback. You should let go of all of your notes here.
     * @param m Interface to the MIDI device
     * @param c Chord to stop playing.
     */
    void stop(MidiController m, Chord c);

    /**
     * Called for each frame.  The first "frame" that will be played after "start" is frame 1.
     * The range of frames is 0 - 7, depending on time signature.
     * @param m Interface to the MIDI device
     * @param c Chord to play
     * @param count Frame counter
     */
    void cycle(MidiController m, Chord c, int count);
}
