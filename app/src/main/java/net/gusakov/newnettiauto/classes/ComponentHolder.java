package net.gusakov.newnettiauto.classes;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.PowerManager;
import android.os.Vibrator;


/**
 * Created by hasana on 4/4/2017.
 * singleton class
 */

public class ComponentHolder {

    private static ComponentHolder mComponentHolder;
    private SoundPool mSoundPool;
    private boolean isServiceRunning;
    private boolean isActivityRunning;
    private int serviceFreqTime;
    private NotificationManager mNotificationManager;
    private PowerManager mPowerManager;
    private Vibrator mVibrator;


    private ComponentHolder(){}

    public static ComponentHolder getInstance(){
        if(mComponentHolder ==null){
            mComponentHolder =new ComponentHolder();
        }
        return mComponentHolder;
    }

    public boolean isActivityRunning() {
        return isActivityRunning;
    }

    public void setActivityRunningState(boolean activityRunning) {
        isActivityRunning = activityRunning;
    }

    public void setServiceRunningState(boolean serviceRunning){
        isServiceRunning=serviceRunning;
    }
    public boolean isServiceRunning(){
        return isServiceRunning;
    }

    public Vibrator getVibrator(Context context){
        if(mVibrator==null){
           mVibrator=(Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        }
        return mVibrator;
    }
    public int getServiceFreqTime() {
        return serviceFreqTime;
    }

    public void setServiceFreqTime(int serviceFreqTime) {
        this.serviceFreqTime = serviceFreqTime;
    }

    public SoundPool getSoundPool(){
        if(mSoundPool==null){
            mSoundPool=new SoundPool(3, AudioManager.STREAM_MUSIC, 0);

        }
        return mSoundPool;
    }
    public void releaseSoundPool(){
        if(mSoundPool!=null) {
            mSoundPool.release();
            mSoundPool = null;
        }
    }

    public NotificationManager getNotificationManager(Context context) {
        if(mNotificationManager==null){
            mNotificationManager=new NotificationManager(context);
        }
        return mNotificationManager;
    }
    public PowerManager getPowerManager(Context context){
        if (mPowerManager==null) {
            mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        }
        return mPowerManager;
    }
    public boolean isScreenOn(Context context){
        if (mPowerManager==null) {
            mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return mPowerManager.isInteractive();
        }else{
            return mPowerManager.isScreenOn();
        }

    }

}
