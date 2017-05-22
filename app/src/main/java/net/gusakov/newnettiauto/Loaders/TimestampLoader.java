package net.gusakov.newnettiauto.Loaders;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import net.gusakov.newnettiauto.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

import timber.log.Timber;

/**
 * Created by notbl on 5/4/2017.
 */

public class TimestampLoader extends AsyncTaskLoader<Long> {

    private Long mData;

    public TimestampLoader(Context context) {
        super(context);
    }

    @Override
    public Long loadInBackground() {
        Timber.d("loadinBackground");
        HttpURLConnection con = null;
        Long data = -1L;
        try {
            URL url = new URL(Constants.DATE_TIMESTAMP_LINK);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            con.connect();

            JSONObject jsonObj = new JSONObject(convertStreamToString(con.getInputStream()));
            data = jsonObj.getLong("timestamp");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return data;
    }

    @Override
    public void deliverResult(Long data) {
        Timber.d("deliverResult");
        if (isReset()) {
            Timber.v("isReset");
            // The Loader has been reset; ignore the result and invalidate the data.
            releaseResources(data);
            return;
        }

        mData = data;

        if (isStarted()) {
            Timber.d("isStarted");
            // If the Loader is in a started state, deliver the results to the
            // client. The superclass method does this for us.
            super.deliverResult(data);
        }

    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        Timber.d("onStartLoading");
        if(mData==null) {
            forceLoad();
        }

    }

    @Override
    protected void onStopLoading() {
        Timber.d("onStopLoading");
//        //try cancell
//        Log.v(TAG,"invoke cancelLoad()");
//        cancelLoad();
        //Continue download data and save it when activity start becouse it is not long and heavy operation
        super.onStopLoading();
    }

    @Override
    protected void onReset() {
        Timber.d("onReset");
//        // Ensure the loader has been stopped.
//        onStopLoading();

        // At this point we can release the resources associated with 'mData'.
        if (mData != null) {
            releaseResources(mData);
        }
    }

    @Override
    public void onCanceled(Long data) {
        Timber.d("onCanceled");
        // Attempt to cancel the current asynchronous load.
        super.onCanceled(data);

        // The load has been canceled, so we should release the resources
        // associated with 'data'.
        releaseResources(data);
    }


    private void releaseResources(Long data) {
        data = null;
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
