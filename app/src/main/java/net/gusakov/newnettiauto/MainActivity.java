package net.gusakov.newnettiauto;

import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import net.gusakov.newnettiauto.classes.Auto;
import net.gusakov.newnettiauto.classes.ComponentHolder;
import net.gusakov.newnettiauto.fragments.AutoListFragment;
import net.gusakov.newnettiauto.fragments.MainFragment;
import net.gusakov.newnettiauto.services.AutoSearchService;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class MainActivity extends AppCompatActivity implements AutoSearchService.CallBack, MainFragment.OnFragmentClickEventListener {

    private ServiceConnection mServiceConnection;
    private MainFragment mainFragment;
    private AutoListFragment autoListFragment;
    private FragmentTransaction fTrans;
    private ComponentHolder mComponentHolder = ComponentHolder.getInstance();
    private Timer mTimer;
    private TimerTask mTask;

    @BindView(R.id.frgmCont)
    FrameLayout mFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

//        FirebaseDatabase db = FirebaseDatabase.getInstance();
//        db.getReference().setValue(ServerValue.TIMESTAMP);
//        db.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Timber.d("timestamp=" + (Long) dataSnapshot.getValue());
//                mProgressBar.setVisibility(View.INVISIBLE);
//                long curTimestamp=(Long) dataSnapshot.getValue();
//                if ( curTimestamp> Constants.END_TIMESTAMP) {
//                    initBlockFragment();
//                }else{
//                    initFragments();
//                    sheduleBlockTime(curTimestamp);
//                }
//                mFrameLayout.setVisibility(View.VISIBLE);
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Timber.d("get timestamp cancelled. " + databaseError);
//            }
//        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        bindToMyService();
        initFragments();

    }


    @Override
    protected void onStart() {
        Timber.d("onStart()");
        bindService(new Intent(this, AutoSearchService.class), mServiceConnection, 0);
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Timber.d("onReStart()");
        super.onRestart();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Timber.d("onNewIntent()");
        if (intent.getAction() != null && intent.getAction().equals(Constants.OPEN_LIST_ACTION)) {
            openListFragment();
        }
        super.onNewIntent(intent);
    }


    @Override
    protected void onStop() {
        Timber.d("onStop");
        unbindService(mServiceConnection);
        if (!mComponentHolder.isServiceRunning()) {
            mComponentHolder.releaseSoundPool();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Timber.d("onDestroy()");
        mComponentHolder = null;
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
        super.onDestroy();
    }


    @Override
    public void newAutoDetect(Auto newAuto, int autoSequentialNumberAfterStartSearching) {
        if (mainFragment.isVisible()) {
            mainFragment.updateStatusEditText(autoSequentialNumberAfterStartSearching);
        }
    }

    @Override
    public void stopServiceAction() {
        if (mComponentHolder.isServiceRunning()) {
            Intent stopIntent = new Intent(this, AutoSearchService.class);
            stopService(stopIntent);
        }
    }

    @Override
    public void moreButtonClickEvent() {
        openListFragment();
    }

    @Override
    public void showTrialPeriod() {

    }

    private void openListFragment() {
        if (!autoListFragment.isVisible()) {
            fTrans = getFragmentManager().beginTransaction();
            fTrans.replace(R.id.frgmCont, autoListFragment);
            fTrans.addToBackStack(null);
            fTrans.commit();
        }
    }


    private void bindToMyService() {
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Timber.i("Connect to service");
                AutoSearchService.LocalBinder binder = (AutoSearchService.LocalBinder) service;
                AutoSearchService autoSearchService = binder.getService();
                autoSearchService.setCallBack(MainActivity.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Timber.i("Disconnect from service");
            }
        };
    }


    private void initFragments() {
        mainFragment = new MainFragment();
        autoListFragment = new AutoListFragment();
        fTrans = getFragmentManager().beginTransaction();
        fTrans.replace(R.id.frgmCont, mainFragment);
        fTrans.commit();
        if (mComponentHolder.isServiceRunning()) {
            moreButtonClickEvent();
        }
    }
}

