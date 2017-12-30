package au.id.micolous.midi4chords;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import com.mobileer.miditools.MidiConstants;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SetupFragment.OnSetupFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SetupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetupFragment extends Fragment {
    private OnSetupFragmentInteractionListener mListener;
    private Button mProgramButton;
    private Button mTempoButton;
    private int[] mPrograms = new int[MidiConstants.MAX_CHANNELS]; // ranges from 0 to 127

    public SetupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SetupFragment.
     */
    public static SetupFragment newInstance() {
        SetupFragment fragment = new SetupFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_setup, container, false);

        if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            mListener.setupMidi(v);
        } else {
            (new AlertDialog.Builder(getContext()))
                    .setMessage(R.string.no_midi)
                    .setTitle(R.string.no_midi_title)
                    .setPositiveButton(R.string.quit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            getActivity().finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
            return v;
        }

        mProgramButton = (Button) v.findViewById(R.id.button_program);
        mTempoButton = (Button) v.findViewById(R.id.button_tempo);

        Spinner spinner = (Spinner) v.findViewById(R.id.spinner_channels);
        spinner.setOnItemSelectedListener(new ChannelSpinnerActivity());

        Spinner modeSpinner = (Spinner) v.findViewById(R.id.spinner_modes);
        modeSpinner.setOnItemSelectedListener(new ModeSpinnerActivity());

        if (mListener != null) {
            updateProgramText(mListener.getChannel());
            updateTempoText();
        }

        return v;
    }

    public class ChannelSpinnerActivity implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            mListener.setChannel(pos & 0x0F);
            updateProgramText(pos & 0x0F);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    public class ModeSpinnerActivity implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            mListener.setPlaybackMode(pos);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnSetupFragmentInteractionListener) {
            mListener = (OnSetupFragmentInteractionListener) context;
            mListener.onAttachSetup(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPlayFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void updateProgramText(int channel) {
        mProgramButton.setText("" + mPrograms[channel]);
    }

    public void onProgramSend(View view) {
        int channel = mListener.getChannel();
        mListener.midiCommand(MidiConstants.STATUS_PROGRAM_CHANGE + channel, mPrograms[channel]);
    }

    public void onProgramDelta(View view) {
        Button button = (Button) view;
        int delta = Integer.parseInt(button.getText().toString());
        changeProgram(delta);
    }

    private void changeProgram(int delta) {
        int channel = mListener.getChannel();
        int program = mPrograms[channel];
        program += delta;
        if (program < 0) {
            program = 0;
        } else if (program > 127) {
            program = 127;
        }
        mListener.midiCommand(MidiConstants.STATUS_PROGRAM_CHANGE + channel, program);
        mPrograms[channel] = program;

        updateProgramText(channel);
    }


    public void onTempoDelta(View view) {
        Button button = (Button) view;
        int delta = Integer.parseInt(button.getText().toString());
        mListener.changeTempo(delta);
        updateTempoText();
    }

    private void updateTempoText() {
        mTempoButton.setText("" + mListener.getTempo());
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnSetupFragmentInteractionListener {
        void setChannel(int channel);
        int getChannel();
        void midiCommand(int status, int data1);
        void changeTempo(int delta);
        int getTempo();
        void setupMidi(View v);

        void setPlaybackMode(int i);

        void onAttachSetup(SetupFragment setupFragment);
    }
}
