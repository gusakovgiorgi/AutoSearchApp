package net.gusakov.newnettiauto;

import android.app.Application;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * Created by hasana on 4/2/2017.
 */

public class MainApplication extends Application {

    @Override
    public void onCreate() {
//        if(BuildConfig.DEBUG) {
//        Timber.plant(new Timber.DebugTree());
//        }else{
        Timber.plant(new ReleaseTree());
//    }
        super.onCreate();
    }
    static class ReleaseTree extends Timber.DebugTree {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (priority==Log.DEBUG){
                return;
            }
            super.log(priority, tag, message, t);
        }
    }

}
