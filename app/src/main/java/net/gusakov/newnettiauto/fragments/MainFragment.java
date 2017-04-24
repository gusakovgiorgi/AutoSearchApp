package net.gusakov.newnettiauto.fragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.SoundPool;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.gusakov.newnettiauto.Constants;
import net.gusakov.newnettiauto.R;
import net.gusakov.newnettiauto.classes.ComponentHolder;
import net.gusakov.newnettiauto.services.AutoSearchService;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static net.gusakov.newnettiauto.Constants.MainFragmentConstants.*;


public class MainFragment extends Fragment {
    @BindView(R.id.firstUrlPasteButtonId)
    public Button firstPastebtn;
    @BindView(R.id.secondUrlPasteButtonId)
    public Button secondPastebtn;
    @BindView(R.id.firstUrlEditTextId)
    public EditText firstEditText;
    @BindView(R.id.secondUrlEditTextId)
    public EditText secondEditText;
    @BindView(R.id.startAndStopButtonId)
    public Button startAndStopBtn;
    @BindView(R.id.statusTextViewId)
    public TextView statusTextView;
    @BindView(R.id.moreBtnId)
    public Button moreBtn;
    @BindView(R.id.plusBtnId)
    public Button plusBtn;
    @BindView(R.id.minusBtnId)
    public Button minusBtn;
    @BindView(R.id.pollFreqId)
    public EditText pollFreqEditText;

    private SoundPool soundPool;
    private int buttonClickSound;
    private int pasteSound;

    private Intent autoSearchServiceIntent;


    private OnFragmentClickEventListener mListener;


    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate");
        initServiceIntent();
        initSound();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.main_fragment, null);
        ButterKnife.bind(this, v);
        initValues(firstEditText,secondEditText);
        setListeners();
        Timber.d("onCreateView");
        return v;
    }

    @Override
    public void onStart() {
        Timber.d("onStart");
        if (ComponentHolder.getInstance().isServiceRunning()) {
            setStopButton(true);
        } else {
            setStartButton();
        }
        super.onStart();
    }

    @Override
    public void onAttach(Context context) {
        Timber.d("onAttach");
        initFragmentClickEventListener(context);
        super.onAttach(context);
    }


    @Override
    public void onAttach(Activity activity) {
        Timber.d("onAttach(activity)");
        super.onAttach(activity);
        initFragmentClickEventListener(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Timber.d("onDetach");
        mListener = null;
    }

    private void startAutoSearchService() {
        String url1 = firstEditText.getText().toString();
        String url2 = secondEditText.getText().toString();
        int time = Integer.valueOf(pollFreqEditText.getText().toString());
        if (isValidLinks(url1, url2) && isValidTime(time)) {
            autoSearchServiceIntent.putExtra(INTENT_EXTRTRA_STRING_FIRST_URL, url1);
            autoSearchServiceIntent.putExtra(INTENT_EXTRTRA_STRING_SECOND_URL, url2);
            autoSearchServiceIntent.putExtra(INTENT_EXTRA_INT_POLL_FREQ_TIME, time);
//            WakefulBroadcastReceiver.startWakefulService(getActivity(),autoSearchServiceIntent);
            getActivity().startService(autoSearchServiceIntent);
            saveData(url1, url2,time);
            setStopButton(false);
        }
    }

    public void stopAutoSearchService() {
//        WakefulBroadcastReceiver.completeWakefulIntent(new Intent(getActivity(),AutoSearchService.class));
        getActivity().stopService(autoSearchServiceIntent);
        clearSavedData();
        setStartButton();
    }

    private void setStopButton(boolean initializing) {
        startAndStopBtn.setBackgroundColor(getResources().getColor(R.color.red));
        startAndStopBtn.setText(getResources().getString(R.string.main_activity_stop_button_text));
        startAndStopBtn.setTag(STOP_BUTTON_TAG);
        if (initializing) {
            updateStatusEditText(SEARCHING, AutoSearchService.getFoundAutosNumber());
        } else {
            AutoSearchService.setFoundAutosNumber(0);
            updateStatusEditText(SEARCHING, 0);
        }
        disablePollFrequencyFunc();
    }

    private void setStartButton() {
        startAndStopBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        startAndStopBtn.setText(getResources().getString(R.string.main_activity_start_button_text));
        startAndStopBtn.setTag(START_BUTTON_TAG);
        updateStatusEditText(STOPED, AutoSearchService.getFoundAutosNumber());
        enablePollFrequencyFunc();
    }


    public void onMoreButtonPressed() {
        if (mListener != null) {
            mListener.moreButtonClickEvent();
        }
    }

    public void updateStatusEditText(int foundAutoNumber) {
        updateStatusEditText(SEARCHING, foundAutoNumber);
    }

    private void updateStatusEditText(String searchStatus, int foundAutoNumber) {
        String text = "Search status:";
        String foundAutosStr = "Found Autos:";
        java.util.Formatter formatter = new java.util.Formatter();
        formatter.format("%1$-20s %2$s %n%3$-20s %4$d", text, searchStatus, foundAutosStr, foundAutoNumber);
        int foundAutosStart = formatter.toString().indexOf(foundAutosStr);
        final SpannableStringBuilder sb = new SpannableStringBuilder(formatter.toString());

        final StyleSpan boldFirst = new StyleSpan(android.graphics.Typeface.BOLD);
        final StyleSpan boldSecond = new StyleSpan(android.graphics.Typeface.BOLD);
        sb.setSpan(boldFirst, 0, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(boldSecond, foundAutosStart, foundAutosStart + 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        statusTextView.setText(sb);
    }

    private void saveData(String url1, String url2,int time) {
        SharedPreferences shared = getActivity().getSharedPreferences(Constants.MainFragmentAndServiceSharedConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = shared.edit();
        ed.putString(Constants.MainFragmentAndServiceSharedConstants.SHARED_PARAMETER_STRING_URL1, url1);
        ed.putString(Constants.MainFragmentAndServiceSharedConstants.SHARED_PARAMETER_STRING_URL2, url2);
        ed.putInt(Constants.MainFragmentAndServiceSharedConstants.SHARED_PARAMETER_INT_TIME,time);
        ed.apply();
    }

    private void clearSavedData() {
        SharedPreferences shared = getActivity().getSharedPreferences(Constants.MainFragmentAndServiceSharedConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = shared.edit();
        ed.clear();
        ed.apply();
    }

    private void initFragmentClickEventListener(Context context) {
        if (context instanceof OnFragmentClickEventListener) {
            mListener = (OnFragmentClickEventListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    private void initValues(EditText firstEditText, EditText secondEditText) {
        SharedPreferences shared=getActivity().getSharedPreferences(Constants.MainFragmentAndServiceSharedConstants.SHARED_PREF_NAME,Context.MODE_PRIVATE);
        firstEditText.setText(shared.getString(Constants.MainFragmentAndServiceSharedConstants.SHARED_PARAMETER_STRING_URL1,Constants.MainFragmentAndServiceSharedConstants.FIRST_DEFAULT_URL));
        secondEditText.setText(shared.getString(Constants.MainFragmentAndServiceSharedConstants.SHARED_PARAMETER_STRING_URL2,Constants.MainFragmentAndServiceSharedConstants.SECOND_DEFAULT_URL));
        if(ComponentHolder.getInstance().isServiceRunning()) {
            pollFreqEditText.setText(String.valueOf(ComponentHolder.getInstance().getServiceFreqTime()));
        }else {
            pollFreqEditText.setText(POLLING_FREQYENCY_DEFAULT + "");
        }
    }

    private void initServiceIntent() {
        autoSearchServiceIntent=new Intent(getActivity(),AutoSearchService.class);
    }

    private void initSound() {
        soundPool = ComponentHolder.getInstance().getSoundPool();
        buttonClickSound = soundPool.load(getActivity(), R.raw.neotone_drip1, 1);
        pasteSound = soundPool.load(getActivity(), R.raw.coins, 1);
    }

    private void playSound(int soundId) {
        soundPool.play(soundId, 0.5f, 0.5f, 1, 0, 1f);
    }

    private void disablePollFrequencyFunc() {
        pollFrequencyFunc(false);
    }

    private void enablePollFrequencyFunc() {
        pollFrequencyFunc(true);
    }

    private void pollFrequencyFunc(boolean enabled) {
        pollFreqEditText.setEnabled(enabled);
        plusBtn.setEnabled(enabled);
        minusBtn.setEnabled(enabled);
    }

    private void showShortMessage(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private boolean isValidTime(int time) {
        return (time > MINIMUM_TIME && time < MAXIMUM_TIME);
    }

    private boolean isValidLinks(String url1, String url2) {
        boolean isUrl1Empty = url1.isEmpty();
        boolean isUrl2Empty = url2.isEmpty();
        if (isUrl1Empty && isUrl2Empty) {
            showShortMessage("URLs is empty");
            return false;
        }
        if (url1.equals(url2)) {
            showShortMessage("You Entered identical urls");
            return false;
        }
        if (!isUrl1Empty && !url1.contains("sortCol=datecreate&ord=DESC")) {
            showShortMessage("Ivalid Link. First URL should contains \"sortCol=datecreate&ord=DESC\"");
            return false;
        }
        if (!isUrl2Empty && !url2.contains("sortCol=datecreate&ord=DESC")) {
            showShortMessage("Ivalid Link. Second URL should contains \"sortCol=datecreate&ord=DESC\"");
            return false;
        }
        return true;
    }

    //############################ listeners ###########################################
    private void setListeners() {
        startAndStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = (String) v.getTag();
                playSound(buttonClickSound);
                if (tag.equals(START_BUTTON_TAG)) {
                    startAutoSearchService();
                } else {
                    stopAutoSearchService();
                }
            }
        });
        firstPastebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                String pasteData = "";
                // If it does contain data, decide if you can handle the data.
                if (clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)) {
                    //since the clipboard contains plain text.
                    ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);

                    // Gets the clipboard as text.
                    pasteData = item.getText().toString();
                    playSound(pasteSound);

                }
                firstEditText.setText(pasteData);
            }
        });
        secondPastebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                String pasteData = "";

// If it does contain data, decide if you can handle the data.
                if (clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)) {
                    //since the clipboard contains plain text.
                    ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);

                    // Gets the clipboard as text.
                    pasteData = item.getText().toString();
                    playSound(pasteSound);
                }

                secondEditText.setText(pasteData);
            }
        });
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMoreButtonPressed();
            }
        });
        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String numberStr = pollFreqEditText.getText().toString();
                int number;
                if (numberStr.trim().isEmpty()) {
                    number = 0;
                } else {
                    number = Integer.valueOf(numberStr);
                }
                pollFreqEditText.setText(++number + "");
            }
        });
        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String numberStr = pollFreqEditText.getText().toString();
                int number;
                if (numberStr.trim().isEmpty()) {
                    number = 0;
                } else {
                    number = Integer.valueOf(numberStr);
                    if(number<=0){
                        return;
                    }
                }
                pollFreqEditText.setText(--number + "");

            }
        });
        pollFreqEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String numberStr = s.toString();
                    if (numberStr.trim().isEmpty()) {
                        return;
                    }
                    Integer.valueOf(numberStr);
                } catch (NumberFormatException e) {
                    showShortMessage("Invalid Input. Should be number");
                    pollFreqEditText.setText(POLLING_FREQYENCY_DEFAULT+"");
                }
            }
        });
    }

    //############################ interface ##########################################
    public interface OnFragmentClickEventListener {
        void moreButtonClickEvent();
    }

}
