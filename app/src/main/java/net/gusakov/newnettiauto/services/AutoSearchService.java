package net.gusakov.newnettiauto.services;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.annotation.NonNull;

import net.gusakov.newnettiauto.Constants;
import net.gusakov.newnettiauto.MainActivity;
import net.gusakov.newnettiauto.R;
import net.gusakov.newnettiauto.classes.Auto;
import net.gusakov.newnettiauto.classes.ComponentHolder;
import net.gusakov.newnettiauto.classes.HTMLParser;
import net.gusakov.newnettiauto.classes.InternetData;
import net.gusakov.newnettiauto.classes.InternetSearcher;
import net.gusakov.newnettiauto.classes.NewAutoListener;
import net.gusakov.newnettiauto.classes.NotificationManager;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import timber.log.Timber;


public class AutoSearchService extends Service implements Constants.ServiceConstants{

    private SoundPool soundPool;
    private int newAutoSound;
    private CallBack mCallBack;
    private boolean isBounded;
    private static int foundAutosNumber=0;
    private String firstUrl;
    private String secondUrl;
    private int time;
    private Handler handler=new Handler();
    private ComponentHolder mComponentHolder = ComponentHolder.getInstance();
    ArrayList<Thread> threads=new ArrayList<>(4);
    private PowerManager.WakeLock mWakeLock;

    private BroadcastReceiver internetConnectionReceiver;
    private boolean isInternetConnected;

    public AutoSearchService() {
    }

    @Override
    public void onCreate() {
        Timber.d("onCreate");
        initSound();
        takePartialWakeLock();
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("onStartCommand. flags="+flags);
        if(intent==null){
            loadDataFromFile();
            prepareService();
        }else if (intent.getAction()!=null && intent.getAction().equals(Constants.ServiceConstants.STOP_ACTION)) {
            stopSelfAction();
        }else {
            firstUrl=intent.getStringExtra(INTENT_EXTRTRA_STRING_FIRST_URL);
            secondUrl=intent.getStringExtra(INTENT_EXTRTRA_STRING_SECOND_URL);
            time=intent.getIntExtra(INTENT_EXTRA_INT_POLL_FREQ_TIME,POLLING_FREQYENCY_DEFAULT);

            prepareService();
        }
        return START_STICKY;
    }

    private void prepareService() {

        mComponentHolder.setServiceRunningState(true);
        Timber.d("isServiceRunning=true");
        mComponentHolder.setServiceFreqTime(time);

        NotificationManager notificationManager= mComponentHolder.getNotificationManager(this);
        Notification foregroundNotidication = notificationManager.getForegroundNotification(SEARCH_STATUS);
        startForeground(NotificationManager.NOTIFICATION_FOREGROUND_ID,foregroundNotidication);

        registerConnectionStateReceiver();
    }

    private void loadDataFromFile() {
        SharedPreferences shared=getApplicationContext().getSharedPreferences(Constants.MainFragmentAndServiceSharedConstants.SHARED_PREF_NAME,Context.MODE_PRIVATE);
        firstUrl=shared.getString(Constants.MainFragmentAndServiceSharedConstants.SHARED_PARAMETER_STRING_URL1,Constants.MainFragmentAndServiceSharedConstants.FIRST_DEFAULT_URL);
        secondUrl=shared.getString(Constants.MainFragmentAndServiceSharedConstants.SHARED_PARAMETER_STRING_URL2,Constants.MainFragmentAndServiceSharedConstants.SECOND_DEFAULT_URL);
        time=shared.getInt(Constants.MainFragmentAndServiceSharedConstants.SHARED_PARAMETER_INT_TIME,POLLING_FREQYENCY_DEFAULT);
    }


    @Override
    public void onDestroy() {
        Timber.d("onDestroy()");
        for(int i=threads.size()-1;i>=0;i--){
            threads.remove(i).interrupt();
        }
        if(!mComponentHolder.isActivityRunning()) {
            mComponentHolder.releaseSoundPool();
        }
        mComponentHolder.setServiceRunningState(false);
        unregisterReceiver(internetConnectionReceiver);
        stopForeground(false);
        mWakeLock.release();
        mComponentHolder =null;
        super.onDestroy();
    }

    private void startInternetSearching() {
        Timber.d("start internet searching");
        InternetSearcher firstInternetSearcher = null;
        InternetSearcher secondInternetSearcher = null;
        HTMLParser parser = null;
        BlockingQueue<InternetData> queue = new ArrayBlockingQueue<InternetData>(10);
        if (firstUrl != null && secondUrl != null) {
            try {
                if (!firstUrl.isEmpty()) {
                    firstInternetSearcher = new InternetSearcher(firstUrl, time, queue);
                    threads.add(firstInternetSearcher.startInNewThread());
                }
                if (!secondUrl.isEmpty()) {
                    secondInternetSearcher = new InternetSearcher(secondUrl, time, queue);
                    threads.add(secondInternetSearcher.startInNewThread());
                }
                parser = new HTMLParser(getApplicationContext(), queue, handler);
                parser.setOnNewAutoListener(new NewAutoListener() {
                    @Override
                    public void newAutoEvent(Auto auto) {
                        playSound(newAutoSound);
                        setFoundAutosNumber(++foundAutosNumber);
                        save(auto);
                        mComponentHolder.getNotificationManager(getApplicationContext()).notify(auto);
                        if(mCallBack!=null){
                            mCallBack.newAutoDetect(auto,foundAutosNumber);
                        }else{
                            Timber.d("openActivity()");
                            Intent openActivityIntent=new Intent(AutoSearchService.this, MainActivity.class);
                            PowerManager.WakeLock tempWakeLock=null;

                            if(!mComponentHolder.isScreenOn(AutoSearchService.this)){
                                tempWakeLock = getWakeLock();
                                openActivityIntent.setAction(Constants.OPEN_LIST_ACTION);
                            }

                            openActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(openActivityIntent);


                            releaseWakeLock(tempWakeLock);
                        }
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
                stopSelfAction();
            }

            threads.add(parser.startInNewThread());
        } else {
            Timber.i("INVALID PARAMETERS OF URL, finish service");
            stopSelfAction();
        }
    }

    private void releaseWakeLock(PowerManager.WakeLock tempWakeLock) {
        if(tempWakeLock!=null){tempWakeLock.release();}
    }

    @NonNull
    private PowerManager.WakeLock getWakeLock() {
        PowerManager.WakeLock tempWakeLock;
        tempWakeLock= mComponentHolder.getPowerManager(AutoSearchService.this).newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), AutoSearchService.class.getSimpleName());
        tempWakeLock.acquire();
        return tempWakeLock;
    }


    private void stopSelfAction() {
        if(mComponentHolder.isActivityRunning()){
            mCallBack.stopServiceAction();
        }else{
            Timber.i("stopSelf");
//            WakefulBroadcastReceiver.completeWakefulIntent(new Intent(getApplicationContext(),AutoSearchService.class));
            stopSelf();
        }
    }

    private void save(Auto auto) {
        ContentValues cv=new ContentValues();
        cv.put(Constants.DBConstants.TB_AUTO_ID,auto.getId());
        cv.put(Constants.DBConstants.TB_AUTO_NAME,auto.getName());
        cv.put(Constants.DBConstants.TB_AUTO_DESCRIPTION,auto.getDescription());
        cv.put(Constants.DBConstants.TB_AUTO_LINK,auto.getLink());
        cv.put(Constants.DBConstants.TB_AUTO_PHONE_URI,auto.getPhoneNumberURI());
        cv.put(Constants.DBConstants.TB_AUTO_PRICE,auto.getPrice());
        cv.put(Constants.DBConstants.TB_AUTO_SELLER,auto.getSeller());
        cv.put(Constants.DBConstants.TB_AUTO_TIMESTAMP,auto.getTimestamp());
        cv.put(Constants.DBConstants.TB_AUTO_YEAR_AND_MILLEAGE,auto.getYearAndMileage());
        cv.put(Constants.DBConstants.TB_AUTO_IMAGE_URL,auto.getImageUrlString());
        getContentResolver().insert(Constants.ProviderConstants.AUTO_CONTENT_URI,cv);
    }

    private void takePartialWakeLock() {
        PowerManager powerManager = mComponentHolder.getPowerManager(this);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                AutoSearchService.class.getSimpleName());
        mWakeLock.acquire();
    }

    private void registerConnectionStateReceiver() {
        internetConnectionReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager cm = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();

                if(isConnected){
                    Timber.d("internet connected. flag="+isInternetConnected);
                    if(!isInternetConnected) {
                        startInternetSearching();
                        mComponentHolder.getNotificationManager(AutoSearchService.this).updateForegroundServiceNotification(SEARCH_STATUS);
                    }
                }else {
                    Timber.d("internet disconected");
                    for(int i=threads.size()-1;i>=0;i--){
                        threads.get(i).interrupt();
                    }
                    isInternetConnected=false;
                    mComponentHolder.getNotificationManager(AutoSearchService.this).updateForegroundServiceNotification(WAIT_STATUS);
                }
            }
        };
        IntentFilter intentFilter=new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(internetConnectionReceiver,intentFilter);
    }


    @Override
    public IBinder onBind(Intent intent) {
        Timber.i("onBind");
        bounded();
        return new LocalBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        unBounded();
        Timber.i("onUnbind");
        return callOnRebindInFuture();
    }

    @Override
    public void onRebind(Intent intent) {
        bounded();
        Timber.i("onRebind()");
    }

    public static int getFoundAutosNumber(){
        return foundAutosNumber;
    }
    public static void setFoundAutosNumber(int number){
        foundAutosNumber=number;
    }

    private void playSound(int soundId) {
        Timber.d("stream id="+soundPool.play(soundId, 0.5f, 0.5f, 1, 0, 1f));
    }
    private void initSound(){
        soundPool= mComponentHolder.getSoundPool();
        newAutoSound=soundPool.load(this, R.raw.new_auto, 1);
    }
    private void bounded(){
        isBounded=true;
        mComponentHolder.setActivityRunningState(isBounded);
    }
    private void unBounded(){
        isBounded=false;
        mCallBack=null;
        mComponentHolder.setActivityRunningState(isBounded);
    }
    private boolean callOnRebindInFuture() {
        return true;
    }

    public void setCallBack(CallBack callback){
        mCallBack=callback;
    }


    //################################### interface ######################################################

    public interface CallBack{
        void newAutoDetect(Auto newAuto,int autoSequentialNumberAfterStartSearching);
        void stopServiceAction();
    }

    //################################### inner class #####################################################
    public class LocalBinder extends Binder{
        public AutoSearchService getService(){
            return AutoSearchService.this;
        }
    }
}
