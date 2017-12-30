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
package au.id.micolous.midi4chords;

/**
 * Represents a chord.
 */
public class Chord {
    /**
     * root notes array for each chord (I, V, vi, IV) in all keys (C..B)
     */
    private static final int[][] ROOT_NOTES = {
            {48, 43, 45, 41},   /* C3   G2  A2  F2  */
            {49, 44, 46, 42},   /* C#3  G#2 Bb2 F#2 */
            {50, 45, 47, 43},   /* D3   A2  B2  G2  */
            {51, 46, 48, 44},   /* D#3  Bb2 C3  G#2 */
            {52, 47, 49, 45},   /* E3   B2  C#3 A2  */
            {53, 48, 50, 46},   /* F3   C3  D3  Bb2 */
            {54, 49, 51, 47},   /* F#3  C#3 D#3 B2  */
            {43, 50, 52, 48},   /* G2   D3  E3  C3  */
            {44, 51, 53, 49},   /* G#2  D#3 F3  C#3 */
            {45, 52, 54, 50},   /* A2   E3  F#3 D3  */
            {46, 41, 43, 39},   /* Bb2  F2  G2  D#2 */
            {47, 42, 44, 40}    /* B2   F#2 G#2 E2  */
    };

    /**
     * list of offsets to the chord's major (I, V, IV) or minor (vi) third
     * */
    private static final int[] THIRD_OFFSET = {4, 4, 3, 4};

    /** offset to the chord's perfect fifth, same for all */
    private static final int FIFTH_OFFSET = 7;

    /** offset to the root octave */
    private static final int OCTAVE_OFFSET = 12;

    public final int root;
    public final int third;
    public final int fifth;
    public final int octave;

    public final int key;
    public final int chord_num;

    public Chord(int key, int chord_num) {
        if (key >= ROOT_NOTES.length) throw new AssertionError();
        if (chord_num >= 4) throw new AssertionError();

        root = ROOT_NOTES[key][chord_num];
        third = root + THIRD_OFFSET[chord_num];
        fifth = root + FIFTH_OFFSET;
        octave = root + OCTAVE_OFFSET;
        this.key = key;
        this.chord_num = chord_num;
    }

}
