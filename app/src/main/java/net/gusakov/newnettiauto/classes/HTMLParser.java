package net.gusakov.newnettiauto.classes;

import android.content.Context;
import android.os.Handler;

import net.gusakov.newnettiauto.Constants;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class HTMLParser implements Runnable, Constants.HTMLParserConstants {
    private final BlockingQueue<InternetData> queue;
    private ArrayList<Integer> allGettedIds;
    private ArrayList<Integer> idList;
    private Map<String, ArrayList<Integer>> queueByUrl = new HashMap<>(2, 1F);
    private Map<String, String> previousStringsMap = new HashMap<>(2, 1F);
    private NewAutoListener listener;
    private Handler handler;
    private static final Pattern autoRelatedDataPattern
            = Pattern.compile("<div class=\"listingVifUrl tricky_link_listing listing_nl.+?(?=<div class=\"listingVifUrl)", Pattern.DOTALL);


    public HTMLParser(Context ctx, BlockingQueue<InternetData> queue, Handler handler) {
//        this.ctx = ctx;
        this.queue = queue;
        this.handler = handler;
    }

    public void parse() throws InterruptedException {
        InternetData internetData = queue.take();
        String previousData;
        if ((previousData = previousStringsMap.get(internetData.getURL())) != null) {
            if (previousData.equals(internetData.getHTML())) {
                Timber.d("same html, return");
                return;
            }
        }
        previousStringsMap.put(internetData.getURL(), internetData.getHTML());
        if ((idList = queueByUrl.get(internetData.getURL())) == null) {
            idList = new ArrayList<>(11);
            queueByUrl.put(internetData.getURL(), idList);
        }
//        Log.v(TAG, "parsing starting. URL="+doc.location());
        searchNormalAuros(internetData);
    }


    private void searchNormalAuros(InternetData internetData) {
        int numberoOfSeenCars = 0;
        List<Auto> autos = extractAutos(internetData.getHTML());
        StringBuilder sb = new StringBuilder();
        allGettedIds = new ArrayList<Integer>(11);

        for (int i = 0; i < autos.size(); i++) {
            if (i == QUEUE_MAX_CAPACITY) {
                break;
            }
            allGettedIds.add(autos.get(i).getId());
        }
        for (Auto auto : autos) {
            int id = auto.getId();
            auto.getPhoneNumberURI();
            if (Auto.isValidId(id)) {
                if (normalAutofirstTimeSearch()) {
                    Timber.i("first search, url=" + internetData.getURL());
                    rememberToQueue(id);
                    if (idList.size() < QUEUE_MAX_CAPACITY) {
                        continue;
                    } else {
                        return;
                    }
                } else if (isNewAuto(id)) {
                    Timber.d(sb.toString());
                    Timber.d("new auto");
                    // load phone number
                    auto.getPhoneNumberURI();
                    Timber.d("id=" + id + ", car name=" + auto.getName() + ", description=" + auto.getDescription() + ", price=" + auto.getPrice() +
                            ", yearAndMileage=" + auto.getYearAndMileage() +
                            ", seller=" + auto.getSeller() + ", dealer=" + auto.isDealer() + ", url=" + auto.getLink());
                    invokeNewAutoListeners(auto);
                    rememberToQueue(id);
                    numberoOfSeenCars++;
                    if (numberoOfSeenCars >= QUEUE_MAX_CAPACITY) {
                        break;
                    }
                } else {
                    numberoOfSeenCars++;
                    if (numberoOfSeenCars >= QUEUE_MAX_CAPACITY) {
                        break;
                    }
                }
            }
        }
    }


    private String getPhoneNumberURI(String linkHref) {
        URLConnection urlConnection;
        BufferedReader in = null;
        StringBuilder sb = new StringBuilder();
        try {
            urlConnection = new URL(linkHref).openConnection();
            Timber.d("connect to " + linkHref);
            urlConnection.setReadTimeout(READ_TIMEOUT);
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
            urlConnection.setUseCaches(false);
//            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("User-Agent", System.getProperty("http.agent"));
            urlConnection.connect();
            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "Cp1252"));
            char[] buf = new char[4000];
            int number;
            while ((number = in.read(buf)) != -1) {
                String str = new String(buf, 0, number);
                sb.append(str);
//                        Log.d(TAG, String.valueOf(str));
            }

        } catch (IOException e) {
            Timber.d(" get exception");
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                    urlConnection = null;
                } catch (IOException e) {
                    Timber.d(" get exception");
                    e.printStackTrace();
                }
            }
        }

        String telNumber = null;
        Document doc = Jsoup.parse(sb.toString());
        Elements elements = doc.getElementsByAttributeValueContaining("href", "textToImageCall.php");
        Timber.d("elements size : " + elements.size());
        if (!elements.isEmpty()) {
            Element element = elements.first();
//            Log.v(TAG,"get link for phone param");
            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(element.attr("href")).openConnection();
                httpURLConnection.setInstanceFollowRedirects(false);
                httpURLConnection.setConnectTimeout(CONNECT_TIMEOUT);
                httpURLConnection.setRequestProperty("User-Agent", System.getProperty("http.agent"));
                httpURLConnection.setUseCaches(false);
                httpURLConnection.connect();
                Timber.d("responceCode = " + httpURLConnection.getContent());
//                httpURLConnection.getResponseMessage();
                telNumber = httpURLConnection.getHeaderField("Location");
                Timber.d("get number uri " + telNumber);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                    httpURLConnection = null;
                }
            }
//                    Log.d("TestInternet", urlConnection.getHeaderField("Location"));
        }
        return telNumber;
    }

    public Thread startInNewThread() {
        Thread thread = new Thread(this);
        thread.setName(HTMLParser.class.getSimpleName());
        thread.start();
        return thread;
    }

    private void invokeNewAutoListeners(final Auto auto) {

        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.newAutoEvent(auto);
                }
            });
        }
    }

    private boolean isNewAuto(int id) {
//        return db.isNewAuto(id);
        Iterator<Integer> idIterator = idList.iterator();
        while (idIterator.hasNext()) {
            if (id == idIterator.next()) {
                return false;
            }
        }
        return true;
    }

    private void rememberToQueue(int id/*,String url*/) {
        Timber.i("rememeber id = " + id);
//        db.rememberToQueue(id,url);
        if (idList.size() >= QUEUE_MAX_CAPACITY) {
            removeItemFromQueue(idList, allGettedIds);
            idList.add(id);
        } else {
            idList.add(id);
        }
        Iterator<Integer> iterator = idList.iterator();
        StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()) {
            sb.append(iterator.next()).append(" , ");
        }
        Timber.d("queue is " + sb);
    }

    private void removeItemFromQueue(ArrayList<Integer> idList, ArrayList<Integer> allGettedIds) {
        boolean find = false;
        int id;
        for (int i = 0; i < idList.size(); i++) {
            id = idList.get(i);
            for (int j = 0; j < allGettedIds.size(); j++) {
                if (id == allGettedIds.get(j)) {
                    find = true;
                    continue;
                }
            }
            if (!find) {
                idList.remove(i);
                return;
            }
        }
    }

    private boolean normalAutofirstTimeSearch() {
        return idList.size() < QUEUE_MAX_CAPACITY;
    }

    private List<Auto> extractAutos(String html) {
        Matcher matcher = autoRelatedDataPattern.matcher(html);
        List<Auto> autoRelatedData = new LinkedList<>();
        int numberOfGettingAuotoData = 0;
        while (matcher.find()) {
            if (matcher.group(0) != null) {
                String data = matcher.group(0);
                if (!data.contains("upsellAd")) {
                    autoRelatedData.add(new Auto(matcher.group(0)));
                } else {
                    continue;
                }
            }
            if (++numberOfGettingAuotoData >= QUEUE_MAX_CAPACITY) {
                break;
            }
        }
        return autoRelatedData;
    }

    @Override
    public void run() {
        Thread curThread = Thread.currentThread();
        while (!curThread.isInterrupted()) {
            try {
                parse();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Timber.i("interrupted()");
                break;
            }
        }
    }

    public void setOnNewAutoListener(NewAutoListener listener) {
        this.listener = listener;
    }
}

