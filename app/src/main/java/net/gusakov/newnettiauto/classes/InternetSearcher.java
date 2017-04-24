package net.gusakov.newnettiauto.classes;

import android.util.Log;

import net.gusakov.newnettiauto.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

import timber.log.Timber;

/**
 * Created by hasana on 4/4/2017.
 */

public class InternetSearcher implements Runnable, Constants.InternetSearcherConstants {
    private int delay;
    private final URL url;
    private URLConnection urlConnection;
    private StringBuilder sb;
    private final BlockingQueue<InternetData> queue;
    private boolean isInterrupted = false;
    private Random random;
    private String userAgent;

    public InternetSearcher(String url, int delay, BlockingQueue<InternetData> queue) throws MalformedURLException {
        this.delay = delay;
        this.queue = queue;
        random = new Random();
        this.url = new URL(url);
        Timber.d("Initial InternetSearcher");
        userAgent = System.getProperty("http.agent");
    }

    @Override
    public void run() {
        while (!isInterrupted) {
            if (connect()) {
//                Log.d(TAG,"connected");
                BufferedReader in = null;
                sb = new StringBuilder();
                try {
                    in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "Cp1252"));
                    char[] buf = new char[4000];
                    int number;
                    while ((number = in.read(buf)) != -1) {
                        String str = new String(buf, 0, number);
                        sb.append(str);
//                        Log.d(TAG, String.valueOf(str));
                    }

                    InternetData internetData = new InternetData(sb.toString(), url.toString());
                    queue.put(internetData);

                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                } catch (InterruptedIOException e) {
                    setInterrupted();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    setInterrupted();
                } finally {
                    if (in != null) {
                        closeStream(in);
                    }
                }
//            Log.d(TAG,"data= "+sb.toString());
            } else {
                Timber.d("cannot connect");
            }
//            Log.v(TAG, "end");
            try {
                Thread.sleep(delay * (800 + random.nextLong() % 200));
            } catch (InterruptedException e) {
                e.printStackTrace();
                setInterrupted();
            }
        }

    }
    private boolean connect(){
        try {
            urlConnection=url.openConnection();
            urlConnection.setUseCaches(false);
            urlConnection.setReadTimeout(READ_TIMEOUT);
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
//            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("User-Agent",userAgent);
            urlConnection.connect();
        } catch (IOException e) {
            Timber.d("exception in connect");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Thread startInNewThread(){
        Thread thread= new Thread(this);
        thread.setName(InternetSearcher.class.getSimpleName());
        thread.start();
        return thread;
    }

    private void closeStream(BufferedReader in) {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setInterrupted() {
        Timber.d("interrupted flag is true");
        isInterrupted = true;
    }
}
