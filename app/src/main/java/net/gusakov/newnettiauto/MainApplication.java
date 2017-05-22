package net.gusakov.newnettiauto;

import android.app.Application;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

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

        // Create global configuration and initialize ImageLoader with this config
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(this);
        config.threadPriority(Thread.NORM_PRIORITY - 1);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(20 * 1024 * 1024); // 50 MiB
        config.writeDebugLogs(); // Remove for release app

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
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
