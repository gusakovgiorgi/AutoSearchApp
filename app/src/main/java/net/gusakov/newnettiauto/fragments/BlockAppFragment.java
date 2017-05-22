package net.gusakov.newnettiauto.fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.gusakov.newnettiauto.Constants;
import net.gusakov.newnettiauto.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BlockAppFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BlockAppFragment extends Fragment implements Constants.BlockAppFragmentConstants {

    public static final String TAG = BlockAppFragment.class.getSimpleName();
    @BindView(R.id.trialPeriodTvId)
    public TextView mTextView;


    public BlockAppFragment() {
        // Required empty public constructor
    }

    public static BlockAppFragment newInstance(Long currentTimestamp) {
        BlockAppFragment fragment = new BlockAppFragment();
        if (currentTimestamp != null) {
            Bundle args = new Bundle();
            args.putLong(INTENT_EXTRA_LONG_TIMESTAMP, currentTimestamp);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_block_app, container, false);
        ButterKnife.bind(this, view);
        if (getArguments() != null) {
            long curTimestamp = getArguments().getLong(INTENT_EXTRA_LONG_TIMESTAMP);
            showLeftDays(mTextView, curTimestamp);
        }
        return view;

    }


    public void refreshScreen(Long curTimestamp) {
        if (curTimestamp != null) {
            showLeftDays(mTextView, curTimestamp);
        } else {
            mTextView.setText(getResources().getString(R.string.block_fragment_main_text));
        }
    }

    private void showLeftDays(TextView mTextView, long curTimestamp) {
        int daysLeft = (int) ((Constants.END_TIMESTAMP - curTimestamp) / 60 / 60 / 24);
        mTextView.setText("Days Left: " + daysLeft);
    }

}
