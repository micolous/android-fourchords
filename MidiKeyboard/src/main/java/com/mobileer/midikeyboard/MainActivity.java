/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.mobileer.midikeyboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.midi.MidiManager;
import android.media.midi.MidiReceiver;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.mobileer.midifourchords.R;
import com.mobileer.midikeyboard.playbackmode.PMChord;
import com.mobileer.midikeyboard.playbackmode.PlaybackMode;
import com.mobileer.miditools.MidiConstants;
import com.mobileer.miditools.MidiInputPortSelector;

import java.io.IOException;

/**
 * Main activity for the fourchords app.
 */
public class MainActivity extends Activity implements View.OnTouchListener, MidiController {
    private static final String TAG = "MidiKeyboard";
    private static final int DEFAULT_VELOCITY = 64;

    /**
     * root notes array for each chord (I, V, vi, IV) in all keys (C..B)
     */
    static final int[][] ROOT_NOTES = {
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

    private static final PlaybackMode[] PLAYBACK_MODES = {
            new PMChord(),
    };

    private Button mButtonMajFirst;
    private Button mButtonPerFifth;
    private Button mButtonMinSixth;
    private Button mButtonPerFourth;

    private MidiInputPortSelector mKeyboardReceiverSelector;
    private Button mProgramButton;
    private MidiManager mMidiManager;
    private int mChannel; // ranges from 0 to 15
    private int mKey;
    private Chord mChord;
    private PlaybackMode mPlaybackMode;
    private boolean mPressed = false;
    private int mCount = 0;

    private int[] mPrograms = new int[MidiConstants.MAX_CHANNELS]; // ranges from 0 to 127
    private byte[] mByteBuffer = new byte[3];

    public class ChannelSpinnerActivity implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            mChannel = pos & 0x0F;
            updateProgramText();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    public class KeySpinnerActivity implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            mKey = (0 <= pos && pos <= 12) ? pos : 0;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    public class ModeSpinnerActivity implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            int mode = (0 <= pos && pos <= PLAYBACK_MODES.length) ? pos : 0;

            mPlaybackMode = PLAYBACK_MODES[mode];
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            setupMidi();
        } else {
            Toast.makeText(this, "MIDI not supported!", Toast.LENGTH_LONG)
                    .show();
        }

        mProgramButton = (Button) findViewById(R.id.button_program);

        Spinner spinner = (Spinner) findViewById(R.id.spinner_channels);
        spinner.setOnItemSelectedListener(new ChannelSpinnerActivity());

        mButtonMajFirst = (Button)findViewById(R.id.button_maj_first);
        mButtonPerFifth = (Button)findViewById(R.id.button_per_fifth);
        mButtonMinSixth = (Button)findViewById(R.id.button_min_sixth);
        mButtonPerFourth = (Button)findViewById(R.id.button_per_fourth);

        // Accessibility issue: performClick can't be handled here, because their presses are
        // instant.
        mButtonMajFirst.setOnTouchListener(this);
        mButtonPerFifth.setOnTouchListener(this);
        mButtonMinSixth.setOnTouchListener(this);
        mButtonPerFourth.setOnTouchListener(this);

        Spinner keySpinner = (Spinner)findViewById(R.id.spinner_keys);
        keySpinner.setOnItemSelectedListener(new KeySpinnerActivity());
        mKey = 0;

        Spinner modeSpinner = (Spinner)findViewById(R.id.spinner_modes);
        modeSpinner.setOnItemSelectedListener(new ModeSpinnerActivity());
        mPlaybackMode = PLAYBACK_MODES[0];
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int chord;
        if (view == mButtonMajFirst) {
            chord = 0;
        } else if (view == mButtonPerFifth) {
            chord = 1;
        } else if (view == mButtonMinSixth) {
            chord = 2;
        } else if (view == mButtonPerFourth) {
            chord = 3;
        } else {
            return false;
        }

        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            mChord = new Chord(mKey, chord);
            mPlaybackMode.start(this, mChord);
            mPressed = true;
            return true;
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            mPressed = false;
            mCount = 0;
            mPlaybackMode.stop(this, mChord);
            return true;
        }

        return false;
    }

    private void setupMidi() {
        mMidiManager = (MidiManager) getSystemService(MIDI_SERVICE);
        if (mMidiManager == null) {
            Toast.makeText(this, "MidiManager is null!", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // Setup Spinner that selects a MIDI input port.
        mKeyboardReceiverSelector = new MidiInputPortSelector(mMidiManager,
                this, R.id.spinner_receivers);

        /*
        mKeyboard = (MusicKeyboardView) findViewById(R.id.musicKeyboardView);
        mKeyboard.addMusicKeyListener(new MusicKeyboardView.MusicKeyListener() {
            @Override
            public void onKeyDown(int keyIndex) {
                noteOn(mChannel, keyIndex, DEFAULT_VELOCITY);
            }

            @Override
            public void onKeyUp(int keyIndex) {
                noteOff(mChannel, keyIndex, DEFAULT_VELOCITY);
            }
        });
        */
    }

    public void onProgramSend(View view) {
        midiCommand(MidiConstants.STATUS_PROGRAM_CHANGE + mChannel, mPrograms[mChannel]);
    }

    public void onProgramDelta(View view) {
        Button button = (Button) view;
        int delta = Integer.parseInt(button.getText().toString());
        changeProgram(delta);
    }

    private void changeProgram(int delta) {
        int program = mPrograms[mChannel];
        program += delta;
        if (program < 0) {
            program = 0;
        } else if (program > 127) {
            program = 127;
        }
        midiCommand(MidiConstants.STATUS_PROGRAM_CHANGE + mChannel, program);
        mPrograms[mChannel] = program;
        updateProgramText();
    }

    private void updateProgramText() {
        mProgramButton.setText("" + mPrograms[mChannel]);
    }

    public void noteOff(int pitch) {
        noteOff(mChannel, pitch, DEFAULT_VELOCITY);
    }

    private void noteOff(int channel, int pitch, int velocity) {
        midiCommand(MidiConstants.STATUS_NOTE_OFF + channel, pitch, velocity);
    }

    public void noteOn(int pitch) {
        noteOn(mChannel, pitch, DEFAULT_VELOCITY);
    }

    private void noteOn(int channel, int pitch, int velocity) {
        midiCommand(MidiConstants.STATUS_NOTE_ON + channel, pitch, velocity);
    }

    private void midiCommand(int status, int data1, int data2) {
        mByteBuffer[0] = (byte) status;
        mByteBuffer[1] = (byte) data1;
        mByteBuffer[2] = (byte) data2;
        long now = System.nanoTime();
        midiSend(mByteBuffer, 3, now);
    }

    private void midiCommand(int status, int data1) {
        mByteBuffer[0] = (byte) status;
        mByteBuffer[1] = (byte) data1;
        long now = System.nanoTime();
        midiSend(mByteBuffer, 2, now);
    }

    private void closeSynthResources() {
        if (mKeyboardReceiverSelector != null) {
            mKeyboardReceiverSelector.close();
            mKeyboardReceiverSelector.onDestroy();
        }
    }

    @Override
    public void onDestroy() {
        closeSynthResources();
        super.onDestroy();
    }

    private void midiSend(byte[] buffer, int count, long timestamp) {
        try {
            // send event immediately
            MidiReceiver receiver = mKeyboardReceiverSelector.getReceiver();
            if (receiver != null) {
                receiver.send(buffer, 0, count, timestamp);
            }
        } catch (IOException e) {
            Log.e(TAG, "mKeyboardReceiverSelector.send() failed " + e);
        }
    }
}
