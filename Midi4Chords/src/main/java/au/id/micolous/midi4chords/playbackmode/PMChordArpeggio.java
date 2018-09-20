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
package au.id.micolous.midi4chords.playbackmode;

import au.id.micolous.midi4chords.Chord;
import au.id.micolous.midi4chords.MidiController;

/**
 * Implements Chord + Arpeggio playback:
 * <p>
 * 1. Play the whole chord
 * 2. Play the third
 * 3. Play the fifth
 * 4. Play the third
 * 5. Play the root
 * 6. Play the third
 * 7. Play the fifth
 * 8. Play the third
 */

public class PMChordArpeggio implements PlaybackMode {
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
                m.noteOn(c.third);
                m.noteOn(c.fifth);
                m.noteOn(c.octave);
                /* fall through */
            case 4:
                m.noteOn(c.root);
                break;
            case 1:
            case 5:
                m.noteOn(c.third);
                break;
            case 2:
            case 6:
                m.noteOn(c.fifth);
                break;
            case 3:
            case 7:
                m.noteOn(c.third);
                break;
        }
    }
}
