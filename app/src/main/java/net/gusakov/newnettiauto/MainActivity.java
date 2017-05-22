package net.gusakov.newnettiauto;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.IBinder;
import android.app.FragmentTransaction;
import android.support.annotation.MainThread;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;


import net.gusakov.newnettiauto.Loaders.TimestampLoader;
import net.gusakov.newnettiauto.classes.Auto;
import net.gusakov.newnettiauto.classes.ComponentHolder;
import net.gusakov.newnettiauto.fragments.AutoListFragment;
import net.gusakov.newnettiauto.fragments.BlockAppFragment;
import net.gusakov.newnettiauto.fragments.MainFragment;
import net.gusakov.newnettiauto.services.AutoSearchService;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class MainActivity extends AppCompatActivity implements AutoSearchService.CallBack, MainFragment.OnFragmentClickEventListener,
        LoaderManager.LoaderCallbacks<Long>{

    private ServiceConnection mServiceConnection;
    private MainFragment mainFragment;
    private AutoListFragment autoListFragment;
    private FragmentTransaction fTrans;
    private ComponentHolder mComponentHolder = ComponentHolder.getInstance();
    private Timer mTimer;
    private TimerTask mTask;
    private long mCurrentTimestamp;
    private boolean isBlocked;

    @BindView(R.id.progressBarId)
    ProgressBar mProgressBar;
    @BindView(R.id.frgmCont)
    FrameLayout mFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        getSupportLoaderManager().initLoader(0,null,this);

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
//        initFragments();

    }

    private void sheduleBlockTime(long curTimestamp) {
        if(mTask!=null){
            mTask.cancel();
            mTask=new CustomTimerTask();
        }else {
            mTask=new CustomTimerTask();
            mTimer = new Timer();
        }
        mTimer.schedule(mTask,(Constants.END_TIMESTAMP-curTimestamp)*1000);
        Timber.d("shedulled in "+(Constants.END_TIMESTAMP -curTimestamp));
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
    public void onBackPressed() {
        if(isBlocked){
            finish();
        }else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        Timber.d("onDestroy()");
        mComponentHolder = null;
        if(mTask!=null) {
            mTask.cancel();
            mTask=null;
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
        if(mComponentHolder.isServiceRunning()) {
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
        initBlockFragment(false,mCurrentTimestamp);
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

    @MainThread
    private void initBlockFragment(boolean blocking,Long currentTimeStamp) {
        isBlocked=blocking;
        BlockAppFragment blockAppFragment;
        if((blockAppFragment=(BlockAppFragment) getFragmentManager().findFragmentByTag(BlockAppFragment.TAG))!=null){
            blockAppFragment.refreshScreen(currentTimeStamp);
            if(blocking){
                stopServiceAction();
            }
        }else {
            fTrans = getFragmentManager().beginTransaction();
            fTrans.replace(R.id.frgmCont, BlockAppFragment.newInstance(currentTimeStamp), BlockAppFragment.TAG);
            if (!blocking) {
                fTrans.addToBackStack(null);
            }else {
                stopServiceAction();
            }
            fTrans.commit();}
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


    class CustomTimerTask extends TimerTask{
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initBlockFragment(true,null);
                }
            });
        }
    }


    @Override
    public Loader<Long> onCreateLoader(int id, Bundle args) {
        Timber.d("create loader");
        return new TimestampLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Long> loader, Long data) {
        Timber.d("finishedLoading. data="+data);
        mProgressBar.setVisibility(View.INVISIBLE);
        mCurrentTimestamp=data;
        if ( mCurrentTimestamp> Constants.END_TIMESTAMP) {
            initBlockFragment(true,null);
        }else{
            initFragments();
            sheduleBlockTime(mCurrentTimestamp);
        }
        mFrameLayout.setVisibility(View.VISIBLE);

    }

    @Override
    public void onLoaderReset(Loader<Long> loader) {
        Timber.d("reset loader");
    }
}

