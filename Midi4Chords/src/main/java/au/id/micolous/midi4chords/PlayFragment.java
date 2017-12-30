package au.id.micolous.midi4chords;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnPlayFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayFragment extends Fragment implements View.OnTouchListener {

    private OnPlayFragmentInteractionListener mListener;

    private Button mNextKeyButton;
    private Button mButtonMajFirst;
    private Button mButtonPerFifth;
    private Button mButtonMinSixth;
    private Button mButtonPerFourth;

    public PlayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PlayFragment.
     */
    public static PlayFragment newInstance() {
        PlayFragment fragment = new PlayFragment();
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_play, container, false);

        mNextKeyButton = (Button) v.findViewById(R.id.button_keychange);
        mButtonMajFirst = (Button) v.findViewById(R.id.button_maj_first);
        mButtonPerFifth = (Button) v.findViewById(R.id.button_per_fifth);
        mButtonMinSixth = (Button) v.findViewById(R.id.button_min_sixth);
        mButtonPerFourth = (Button) v.findViewById(R.id.button_per_fourth);

        // Accessibility issue: performClick can't be handled here, because their presses are
        // instant.
        mButtonMajFirst.setOnTouchListener(this);
        mButtonPerFifth.setOnTouchListener(this);
        mButtonMinSixth.setOnTouchListener(this);
        mButtonPerFourth.setOnTouchListener(this);

        if (mListener != null) {
            updateNextKeyText(mListener.getNextKey());
        }
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPlayFragmentInteractionListener) {
            mListener = (OnPlayFragmentInteractionListener) context;
            mListener.onAttachPlay(this);
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

        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN || motionEvent.getAction() == MotionEvent.ACTION_UP) {
            mListener.chordTouch(chord, motionEvent.getAction() == MotionEvent.ACTION_DOWN);
            return true;
        }

        return false;
    }

    public void onKeyChangeDelta(View view) {
        Button button = (Button) view;
        int delta = Integer.parseInt(button.getText().toString());
        int next_key = mListener.changeKey(delta);
        updateNextKeyText(next_key);
    }

    private void updateNextKeyText(int next_key) {
        String[] keys = getResources().getStringArray(R.array.keys);
        mNextKeyButton.setText(keys[next_key]);
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
    public interface OnPlayFragmentInteractionListener {
        void onAttachPlay(PlayFragment p);
        void chordTouch(int chord, boolean down_press);
        int changeKey(int delta);
        int getNextKey();
    }
}
