package net.gusakov.newnettiauto.classes;

import android.support.annotation.WorkerThread;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

import static net.gusakov.newnettiauto.Constants.HTMLParserConstants.CONNECT_TIMEOUT;
import static net.gusakov.newnettiauto.Constants.HTMLParserConstants.READ_TIMEOUT;

public class AutoInfoExtractor {
    private static final Pattern idPattern = Pattern.compile("data-id=\"(\\d+)\"", Pattern.DOTALL);
    private static final Pattern namePattern = Pattern.compile("<div class=\"make_model_link\">(.+?)<span.+?>(.+?)<\\/span>", Pattern.DOTALL);
    private static final Pattern imageUrlPattern = Pattern.compile("<span class=\"spacer pointer\">.+?src=\"(.+?)\"", Pattern.DOTALL);
    private static final Pattern descriptionPattern = Pattern.compile("<div class=\"checkLnesFlat\">(.+?)<\\/div>", Pattern.DOTALL);
    private static final Pattern pricePattern = Pattern.compile("data-price=\"(\\d+)\"", Pattern.DOTALL);
    private static final Pattern yearPattern = Pattern.compile("data-year=\"(\\d+)\"", Pattern.DOTALL);
    private static final Pattern mileagePattern = Pattern.compile("data-mileage=\"(\\d+)\"", Pattern.DOTALL);
    private static final Pattern sellerPattern = Pattern.compile("<span class=\"list_seller_info\".+?<span class=\"gray_text\">(.+?)<\\/span>", Pattern.DOTALL);
    private static final Pattern linkPattern = Pattern.compile("<a href=\"(.+?)\"", Pattern.DOTALL);
    private static final Pattern dealerPattern = Pattern.compile("<span class=\"delear_logo\">", Pattern.DOTALL);

    private String autoRelatedData;

    public AutoInfoExtractor(String autoRelatedData) {
        this.autoRelatedData = autoRelatedData;
    }

    public Integer getId() {
        return extractInt(idPattern);
    }

    public String getName() {
        Matcher matcher = namePattern.matcher(autoRelatedData);
        if (matcher.find()) {
            return matcher.group(1) + matcher.group(2);
        }
        return null;
    }

    public String getImageUrlString() {
        return extractString(imageUrlPattern);
    }

    public String getDescription() {
        return extractString(descriptionPattern);
    }

    public Integer getPrice() {
        return extractInt(pricePattern);
    }

    public String getYear() {
        return extractString(yearPattern);
    }

    public String getMileage() {
        return extractString(mileagePattern);
    }

    public String getSeller() {
        return extractString(sellerPattern);
    }

    public String getLink() {
        return extractString(linkPattern);
    }

    public Boolean isDealer() {
        Matcher matcher = dealerPattern.matcher(autoRelatedData);
        return matcher.find();
    }

    @WorkerThread
    public String getPhoneNumberUri(String link) {
        String mobileLink = link.replace("https://www.", "https://m.");
        URLConnection urlConnection;
        BufferedReader in = null;
        StringBuilder sb = new StringBuilder();
        try {
            urlConnection = new URL(mobileLink).openConnection();
            Timber.d("connect to " + mobileLink);
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

    private Integer extractInt(Pattern pattern) {
        Matcher matcher = pattern.matcher(autoRelatedData);
        if (matcher.find()) {
            return Integer.valueOf(matcher.group(1));
        }
        return null;
    }

    private String extractString(Pattern pattern) {
        Matcher matcher = pattern.matcher(autoRelatedData);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
