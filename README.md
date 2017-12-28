# MIDI Four Chords

Android app which plays the Four Chords, used in [the I-V-iv-IV progression][1] and [the 50s
progression][2], which appears [frequently][3] in [popular music][4].

This project is inspired by [4chord MIDI][5], a hardware device which does the same thing. It is
based on [android-midisuite][6]'s MidiKeyboard.

This software requires Android Marshmallow (6.0) or later, and [MIDI support on the device][9]. This
is optional, so not all device manufacturers ship system images with it.  This program should work
with any transport (USB host, USB gadget, BLE, and virtual).

## Usage

This software outputs MIDI using [Android's MIDI APIs][9], and requires an external synthesiser to
be able to listen to it.

### In USB peripheral mode

When you attach to a PC, select "Use USB to use device as MIDI".  This will then show in MIDI Four
Chords as "Android USB Peripheral Port".  You then need to run some software on the PC in order to
connect the Android device's MIDI to a synth.

### In USB host mode

When you attach a USB MIDI device (eg: keyboard) to the phone using a USB-OTG or USB-C-to-A cable,
the MIDI device should simply appear as an output device in _MIDI Four Chords_.

### In virtual mode (Android-based synthesisers)

One of these is needed to be able to play the sounds from the Android device itself.  However, any
synth which [implements Android's MIDI APIs][9] should be able to be used.

* [MidiSynthExample][7]: Simple sawtooth-wave based synth.
* [Volcano Mobile Fluidsynth][8]: Port of FluidSynth to Android. Allows downloading Soundfonts (ie:
  high quality voices).

## License

Apache 2.0 (same as [android-midisuite][6]).

[1]: https://en.wikipedia.org/wiki/I%E2%80%93V%E2%80%93vi%E2%80%93IV_progression
[2]: https://en.wikipedia.org/wiki/50s_progression
[3]: https://en.wikipedia.org/wiki/List_of_songs_containing_the_I%E2%80%93V%E2%80%93vi%E2%80%93IV_progression
[4]: https://www.youtube.com/watch?v=oOlDewpCfZQ
[5]: https://github.com/sgreg/4chord-midi
[6]: https://github.com/philburk/android-midisuite
[7]: https://play.google.com/store/apps/details?id=com.mobileer.midisynthexample
[8]: https://play.google.com/store/apps/details?id=net.volcanomobile.fluidsynthmidi
[9]: https://source.android.com/devices/audio/midi