/*
 * Copyright 2017 Michael Farrell <micolous+git@gmail.com>
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
package au.id.micolous.midi4chords;


import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.media.midi.MidiManager;
import android.media.midi.MidiReceiver;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

public class MainActivity extends Activity implements ActionBar.TabListener, MidiController, SetupFragment.OnSetupFragmentInteractionListener, PlayFragment.OnPlayFragmentInteractionListener {

    private static final String TAG = "MidiKeyboard";
    private static final int DEFAULT_VELOCITY = 64;

    private static final String TEMPO = "tempo";
    private static final String KEY = "key";
    private static final String MODE = "mode";

    private static final PlaybackMode[] PLAYBACK_MODES = {
            new PMChord(),
            new PMChordArpeggio(),
            new PMChordArpeggioOctave(),
            new PMArpeggio(),
            new PMArpeggioOctave(),
    };


    private MidiInputPortSelector mKeyboardReceiverSelector;
    private MidiManager mMidiManager;
    private int mChannel; // ranges from 0 to 15
    private int mKey;
    private int mNextKey;
    private Chord mChord = null;
    private PlaybackMode mPlaybackMode;
    private int mPlaybackModeOffset;
    private int mCount = 0;
    private int mTempo = 120;
    private Timer mTimer = null;

    private static final int MAX_KEYS = 12;

    private SetupFragment mSetupFragment;
    private PlayFragment mPlayFragment;

    @Override
    public void setChannel(int channel) {
        mChannel = channel;
    }

    public void onProgramDelta(View view) {
        mSetupFragment.onProgramDelta(view);
    }

    public void onProgramSend(View view) {
        mSetupFragment.onProgramSend(view);
    }

    public void onTempoDelta(View view) {
        mSetupFragment.onTempoDelta(view);
    }

    public void onKeyChangeDelta(View view) {
        mPlayFragment.onKeyChangeDelta(view);
    }

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        if (savedInstanceState == null) {
            mSetupFragment = SetupFragment.newInstance();
            mPlayFragment = PlayFragment.newInstance();


            mKey = mNextKey = 0;
            mPlaybackMode = PLAYBACK_MODES[0];
            mPlaybackModeOffset = 0;
        } else {
            mKey = mNextKey = savedInstanceState.getInt(KEY, 0);
            mPlaybackModeOffset = savedInstanceState.getInt(MODE, 0);
            mPlaybackMode = PLAYBACK_MODES[mPlaybackModeOffset];
            mTempo = savedInstanceState.getInt(TEMPO, 120);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY, mKey);
        outState.putInt(MODE,  mPlaybackModeOffset);
        outState.putInt(TEMPO, mTempo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return mSetupFragment;
                case 1:
                    return mPlayFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab_setup);
                case 1:
                    return getString(R.string.tab_play);
            }
            return null;
        }
    }


    @Override
    public void onAttachPlay(PlayFragment p) {
        mPlayFragment = p;
    }

    public void chordTouch(int chord, boolean down_press) {

        if (down_press) {
            destroyTimer();
            if (mChord != null) {
                // stop the previous chord now.
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
        } else {
            if (mChord.chord_num == chord) {
                // Only destroy if the currently playing chord is ours.
                // Otherwise we let the downpress clean up after us.
                destroyTimer();
                mPlaybackMode.stop(this, mChord);
            }

            doKeyChange();
        }

    }

    private void doKeyChange() {
        mKey = mNextKey;
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

    public void setupMidi(View v) {
        mMidiManager = (MidiManager) getSystemService(MIDI_SERVICE);
        if (mMidiManager == null) {
            Toast.makeText(this, "MidiManager is null!", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // Setup Spinner that selects a MIDI input port.
        mKeyboardReceiverSelector = new MidiInputPortSelector(mMidiManager,
                v, R.id.spinner_receivers);

    }

    @Override
    public void setPlaybackMode(int i) {
        mPlaybackModeOffset = (0 <= i && i < PLAYBACK_MODES.length) ? i : 0;
        mPlaybackMode = PLAYBACK_MODES[mPlaybackModeOffset];
    }

    @Override
    public void onAttachSetup(SetupFragment setupFragment) {
        mSetupFragment = setupFragment;
    }

    public void changeTempo(int delta) {
        mTempo += delta;
        if (mTempo < 1) {
            mTempo = 1;
        } else if (mTempo > 400) {
            mTempo = 400;
        }
    }

    public int changeKey(int delta) {
        mNextKey += delta;
        if (mNextKey < 0) {
            mNextKey = MAX_KEYS - 1;
        } else if (mNextKey >= MAX_KEYS) {
            mNextKey = 0;
        }

        if (mTimer == null) {
            doKeyChange();
        }
        return mNextKey;
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

    public void midiCommand(int status, int data1, int data2) {
        byte[] b = new byte[3];
        b[0] = (byte) status;
        b[1] = (byte) data1;
        b[2] = (byte) data2;
        long now = System.nanoTime();
        midiSend(b, b.length, now);
    }

    public void midiCommand(int status, int data1) {
        byte[] b = new byte[2];
        b[0] = (byte) status;
        b[1] = (byte) data1;
        long now = System.nanoTime();
        midiSend(b, b.length, now);
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

    public int getChannel() {
        return mChannel;
    }

    public int getTempo() {
        return mTempo;
    }

    public int getNextKey() { return mNextKey; }

}
