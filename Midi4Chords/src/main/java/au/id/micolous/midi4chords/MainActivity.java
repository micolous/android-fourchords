/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

import com.mobileer.miditools.MidiConstants;
import com.mobileer.miditools.MidiInputPortSelector;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import au.id.micolous.midi4chords.playbackmode.PMArpeggio;
import au.id.micolous.midi4chords.playbackmode.PMArpeggioOctave;
import au.id.micolous.midi4chords.playbackmode.PMChord;
import au.id.micolous.midi4chords.playbackmode.PMChordArpeggio;
import au.id.micolous.midi4chords.playbackmode.PMChordArpeggioOctave;
import au.id.micolous.midi4chords.playbackmode.PlaybackMode;

/**
 * Main activity for the fourchords app.
 */
public class MainActivity extends Activity implements View.OnTouchListener, MidiController {
    private static final String TAG = "MidiKeyboard";
    private static final int DEFAULT_VELOCITY = 64;

    private static final PlaybackMode[] PLAYBACK_MODES = {
            new PMChord(),
            new PMChordArpeggio(),
            new PMChordArpeggioOctave(),
            new PMArpeggio(),
            new PMArpeggioOctave(),
    };

    private Button mButtonMajFirst;
    private Button mButtonPerFifth;
    private Button mButtonMinSixth;
    private Button mButtonPerFourth;

    private MidiInputPortSelector mKeyboardReceiverSelector;
    private Button mProgramButton;
    private Button mTempoButton;
    private MidiManager mMidiManager;
    private int mChannel; // ranges from 0 to 15
    private int mKey;
    private int mNextKey;
    private Chord mChord = null;
    private PlaybackMode mPlaybackMode;
    private int mCount = 0;
    private int mTempo = 120;
    private Timer mTimer = null;
    private Spinner mKeySpinner;

    private int[] mPrograms = new int[MidiConstants.MAX_CHANNELS]; // ranges from 0 to 127
    private byte[] mByteBuffer = new byte[3];
    private static final int MAX_KEYS = 12;
    private Button mNextKeyButton;

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
            mKey = (0 <= pos && pos < MAX_KEYS) ? pos : 0;
            mNextKey = mKey;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    public class ModeSpinnerActivity implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            int mode = (0 <= pos && pos < PLAYBACK_MODES.length) ? pos : 0;

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
            (new AlertDialog.Builder(this))
                    .setMessage(R.string.no_midi)
                    .setTitle(R.string.no_midi_title)
                    .setPositiveButton(R.string.quit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            MainActivity.this.finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
            return;
        }

        mProgramButton = (Button) findViewById(R.id.button_program);
        mTempoButton = (Button)findViewById(R.id.button_tempo);
        mNextKeyButton = (Button)findViewById(R.id.button_keychange);

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

        mKeySpinner = (Spinner)findViewById(R.id.spinner_keys);
        mKeySpinner.setOnItemSelectedListener(new KeySpinnerActivity());
        mKey = mNextKey = 0;
        updateNextKeyText();

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
            destroyTimer();
            if (mChord != null) {
                mPlaybackMode.stop(this, mChord);
            }
            mCount = 0;

            mChord = new Chord(mKey, chord);
            mPlaybackMode.start(this, mChord);
            mTimer = new Timer("mytimer");
            mTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    mCount++;
                    if (mCount >= 8 /* 4/4 (common time) */) {
                        mCount = 0;
                    }

                    mPlaybackMode.cycle(MainActivity.this, mChord, mCount);
                }
            }, 1, playbackInterval());
            return true;
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            if (mChord.chord_num == chord) {
                // Only destroy if the currently playing chord is ours.
                // Otherwise we let the downpress clean up after us.
                destroyTimer();
                mPlaybackMode.stop(this, mChord);
            }

            doKeyChange();
            return true;
        }

        return false;
    }

    private void doKeyChange() {
        mKey = mNextKey;
        mKeySpinner.setSelection(mKey);
    }

    private void destroyTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

    /**
     * Gets the playback interval, in milliseconds.
     * @return milliseconds
     */
    private int playbackInterval() {
        int interval = 60000 / mTempo;
        return interval >> 1;
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

    public void onTempoDelta(View view) {
        Button button = (Button) view;
        int delta = Integer.parseInt(button.getText().toString());
        changeTempo(delta);
    }

    public void onTempoSend(View view) {
        // does nothing yet
    }

    private void changeTempo(int delta) {
        mTempo += delta;
        if (mTempo < 1) {
            mTempo = 1;
        } else if (mTempo > 400) {
            mTempo = 400;
        }
        updateTempoText();
    }

    private void updateTempoText() {
        mTempoButton.setText("" + mTempo);
    }

    public void onKeyChangeDelta(View view) {
        Button button = (Button) view;
        int delta = Integer.parseInt(button.getText().toString());
        changeKey(delta);
    }

    private void changeKey(int delta) {
        mNextKey += delta;
        if (mNextKey < 0) {
            mNextKey = MAX_KEYS - 1;
        } else if (mNextKey >= MAX_KEYS) {
            mNextKey = 0;
        }

        updateNextKeyText();
        if (mTimer == null) {
            doKeyChange();
        }
    }

    private void updateNextKeyText() {
        String[] keys = getResources().getStringArray(R.array.keys);
        mNextKeyButton.setText(keys[mNextKey]);
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
