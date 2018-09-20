/*
 * Copyright 2017 Michael Farrell <micolous+git@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.id.micolous.midi4chords.music;

/**
 * Interface for playback modes.
 * <p>
 * These should be simple and not contain state, as they will all be instantiated immediately, and
 * reused throughout the execution of the program.
 */

public abstract class PlaybackMode {
    /**
     * Called when playback starts.  If your playback mode has multiple "frames", then the default
     * implementation should be fine, and you don't need to override this.
     *
     * @param m Interface to the MIDI device
     * @param c Chord to play
     */
    public void start(MidiController m, Chord c) {
        cycle(m, c, 0);
    }

    /**
     * Called to end all playback. You should let go of all of your notes here.
     *
     * By default, this will release all notes in the chord.  Normally, that should be enough.
     *
     * @param m Interface to the MIDI device
     * @param c Chord to stop playing.
     */
    public void stop(MidiController m, Chord c) {
        m.noteOff(c.root);
        m.noteOff(c.third);
        m.noteOff(c.fifth);
        m.noteOff(c.octave);
    }

    /**
     * Called for each frame.  The first "frame" that will be played after "start" is frame 1.
     * The range of frames is 0 - 7, depending on time signature.
     *
     * By default, this does nothing. If your playback doesn't have frames, then you don't need to
     * do anything here.
     *
     * @param m     Interface to the MIDI device
     * @param c     Chord to play
     * @param count Frame counter
     */
    public void cycle(MidiController m, Chord c, int count) {
    }
}
