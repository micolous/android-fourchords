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
 * Mode that plays a complete chord once.
 */

public class PMChord extends PlaybackMode {
    @Override
    public void start(MidiController m, Chord c) {
        m.noteOn(c.root);
        m.noteOn(c.third);
        m.noteOn(c.fifth);
        m.noteOn(c.octave);
    }
}
