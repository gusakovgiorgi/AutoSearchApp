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
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * Created by hasana on 2/16/2017.
 */

public class HTMLParser implements Runnable,Constants.HTMLParserConstants{

    private Document doc;
//    private Context ctx;
    private final BlockingQueue<InternetData> queue;
    //    private Deque<Integer> idStack =new ArrayDeque<>();
    private ArrayList<Integer> allGettedIds;
    private ArrayList<Integer> idList;
    private Map<String,ArrayList> queueByUrl=new HashMap<>(2,1F);
    private Map<String,String> previousStringsMap=new HashMap<>(2,1F);
    private NewAutoListener listener;
    private Handler handler;
    private static final Pattern mPattern
            = Pattern.compile(" <li class=\"block_list_li \" data-pagenum=\"1\">.+?(?=<\\/li>)",Pattern.DOTALL);



    public HTMLParser(Context ctx, BlockingQueue<InternetData> queue,Handler handler) {
//        this.ctx = ctx;
        this.queue = queue;
        this.handler = handler;
    }

    public void parse() throws InterruptedException {
        InternetData internetData=queue.take();
        String currentHtmlString= getStringOnlyWithAutoHTMLData(internetData.getHTML());
        String previousHTMLString;
        if ((previousHTMLString=previousStringsMap.get(internetData.getURL()))!=null) {
            if (previousHTMLString.equals(currentHtmlString)) {
//                Timber.d("same html, return");
                return;
            }
        }
        previousStringsMap.put(internetData.getURL(),currentHtmlString);
        doc = Jsoup.parse(currentHtmlString);
        if((idList =queueByUrl.get(internetData.getURL()))==null){
            idList =new ArrayList<>(11);
            queueByUrl.put(internetData.getURL(), idList);
        }
//        Log.v(TAG, "parsing starting. URL="+doc.location());
        searchNormalAuros(internetData.getURL());
    }


    private void searchNormalAuros(String url) {
        int numberoOfSeenCars=0;
        Elements autos = doc.getElementsByClass(AUTO_MAIN_ELEMENT_CLASS_NAME).not("."+AUTO_MAIN_RECLAME_ELEMENT_CLASS_NAME);
        doc=null;
        StringBuilder sb=new StringBuilder();
        allGettedIds=new ArrayList<Integer>(11);
        for (int i=0;i<autos.size();i++) {
            Element element = autos.get(i);
            sb.append(getId(element)).append(" , ");
            if(i<QUEUE_MAX_CAPACITY) {
                allGettedIds.add(getId(element));
            }
        }
        for (Element element : autos) {
            int id = getId(element);
            if (Auto.isValidId(id)) {
                if (normalAutofirstTimeSearch(url)) {
                    Timber.i("first search, url="+url);
                    rememberToQueue(id);
                    if(idList.size()<QUEUE_MAX_CAPACITY){
                        continue;
                    }else{
                        return;
                    }
                } else if (isNewAuto(id)) {
                    Timber.d(sb.toString());
                    Timber.d("new auto");

                    String linkHref = getLink(element);
                    String phoneNumberURI = getPhoneNumberURI(linkHref);
                    Elements detailBox = element.getElementsByClass(AUTO_DETAIL_BOX_CLASS_NAME);
                    Elements thumbBox=element.getElementsByClass(AUTO_THUMB_BOX_CLASS_NAME);
                    if (!detailBox.isEmpty()) {
                        String autoName = getAutoName(detailBox);
                        String autoDescription = getAutoDescription(detailBox);
                        int price = getAutoPrice(detailBox);
                        String yearAndmiliage = getYearAndMilliage(detailBox);
                        String seller = getSeller(detailBox);
                        boolean dealer = getDealer(detailBox);
                        String imageUrl=getImageUrl(thumbBox);
                        Auto auto = new Auto(id, autoName, autoDescription, price, yearAndmiliage, seller, linkHref,phoneNumberURI, dealer,System.currentTimeMillis(),imageUrl);
                        invokeNewAutoListeners(auto);
                        Timber.d("id=" + id + ", car name=" + autoName + ", description=" + autoDescription + ", price=" + price + ", year=" + yearAndmiliage +
                                ", seller=" + seller + ", dealer=" + dealer + ", url=" + linkHref);
                    } else {
                        Timber.i("cann't get detailBox");
                    }
                    rememberToQueue(id);
                    numberoOfSeenCars++;
                    if(numberoOfSeenCars>=QUEUE_MAX_CAPACITY){
                        break;
                    }
                }else{
                    numberoOfSeenCars++;
                    if(numberoOfSeenCars>=QUEUE_MAX_CAPACITY){
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
            Timber.d("connect to "+linkHref);
            urlConnection.setReadTimeout(READ_TIMEOUT);
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
            urlConnection.setUseCaches(false);
//            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("User-Agent",System.getProperty("http.agent"));
            urlConnection.connect();
            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"Cp1252"));
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
        }finally {
            if(in!=null){
                try {
                    in.close();
                    urlConnection=null;
                } catch (IOException e) {
                    Timber.d(" get exception");
                    e.printStackTrace();
                }
            }
        }

        String telNumber=null;
        Document doc=Jsoup.parse(sb.toString());
        Elements elements = doc.getElementsByAttributeValueContaining("href", "textToImageCall.php");
        Timber.d("elements size : "+elements.size());
        if(!elements.isEmpty()) {
            Element element = elements.first();
//            Log.v(TAG,"get link for phone param");
            HttpURLConnection httpURLConnection=null;
            try {
                httpURLConnection = (HttpURLConnection)new URL(element.attr("href")).openConnection();
                httpURLConnection.setInstanceFollowRedirects(false);
                httpURLConnection.setConnectTimeout(CONNECT_TIMEOUT);
                httpURLConnection.setRequestProperty("User-Agent",System.getProperty("http.agent"));
                httpURLConnection.setUseCaches(false);
                httpURLConnection.connect();
                Timber.d("responceCode = "+httpURLConnection.getContent());
//                httpURLConnection.getResponseMessage();
                telNumber=httpURLConnection.getHeaderField("Location");
                Timber.d("get number uri "+telNumber);

            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(httpURLConnection!=null) {
                    httpURLConnection.disconnect();
                    httpURLConnection=null;
                }
            }
//                    Log.d("TestInternet", urlConnection.getHeaderField("Location"));
        }
        return telNumber;

    }

    public Thread startInNewThread(){
        Thread thread= new Thread(this);
        thread.setName(HTMLParser.class.getSimpleName());
        thread.start();
        return thread;
    }

    private void invokeNewAutoListeners(final Auto auto) {

        if(listener!=null) {
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
        Iterator<Integer> idIterator= idList.iterator();
        while(idIterator.hasNext()){
            if(id==idIterator.next()){
                return false;
            }
        }
        return true;
    }

    private void rememberToQueue(int id/*,String url*/) {
        Timber.i("rememeber id = "+id);
//        db.rememberToQueue(id,url);
        if(idList.size()>=QUEUE_MAX_CAPACITY){
            removeItemFromQueue(idList,allGettedIds);
            idList.add(id);
        }else {
            idList.add(id);
        }
        Iterator<Integer> iterator= idList.iterator();
        StringBuilder sb=new StringBuilder();
        while (iterator.hasNext()){
            sb.append(iterator.next()).append(" , ");
        }
        Timber.d("queue is "+ sb);
    }

    private void removeItemFromQueue(ArrayList<Integer> idList, ArrayList<Integer> allGettedIds) {
        boolean find=false;
        int id;
        for(int i=0;i<idList.size();i++){
            id=idList.get(i);
            for(int j=0;j<allGettedIds.size();j++){
                if(id==allGettedIds.get(j)){
                    find=true;
                    continue;
                }
            }
            if(!find){
                idList.remove(i);
                return;
            }
        }
    }
//    private void rememberToDeQueue(int id){
//        idStack.push(id);
//        Iterator<Integer> iterator= idStack.iterator();
//        StringBuilder sb=new StringBuilder();
//        while (iterator.hasNext()){
//            sb.append(iterator.next()).append(" , ");
//        }
//        Log.i(TAG, "queue is"+ sb);
//    }

//    private void

    private boolean normalAutofirstTimeSearch(String url) {
        return idList.size()<QUEUE_MAX_CAPACITY;
    }

    private int getId(Element element) {
        String linkStr = getLink(element);
        Pattern pattern = Pattern.compile("(\\d+)\\?");
        Matcher m = pattern.matcher(linkStr);
        if (m.find()) {
//            Log.v(TAG, m.group(0));
            try {
                return Integer.valueOf(m.group(1));
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                return -1;
            }
        }
        return -1;
    }

    private boolean getDealer(Elements detailBox) {
        try {
            if (detailBox.get(0).getElementsByClass(AUTO_DETAIL_DEALER_NAME).isEmpty()) {
                return true;
            }
            return false;
        } catch (NullPointerException e) {
            return false;
        }
    }

    private String getSeller(Elements detailBox) {
        try {
            if(detailBox.get(0).children().size()>5) {
                return detailBox.get(0).child(5).text();
            }
        } catch (NullPointerException e) {

        }
        return "";
    }

    private String getYearAndMilliage(Elements detailBox) {
        try {
            Node node = detailBox.get(0).childNode(8);
//            Log.v(TAG, "childNode=" + node.toString());
            if (node instanceof TextNode) {
                return ((TextNode) node).text();
            }
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    private int getAutoPrice(Elements detailBox) {
        try {
            return getPrice(detailBox.get(0).child(3).text());
        } catch (NullPointerException e) {
            return -1;
        }
    }

    private String getAutoDescription(Elements detailBox) {
        try {
//            Timber.d("childnodes="+detailBox.get(0).child(2).childNodeSize());
            return detailBox.get(0).child(2).text();
        } catch (NullPointerException e) {
            return null;
        }
    }

    private String getAutoName(Elements detailBox) {
        try {
            return detailBox.get(0).child(0).text();
        } catch (NullPointerException e) {
            return null;
        }

    }

    private String getLink(Element element) {
        try {
            return element.child(0).child(0).attr("href");
        } catch (NullPointerException e) {
            return null;
        }
    }

    private String getImageUrl(Elements element) {
        try {
            return element.get(0).child(2).attr("src");
        } catch (NullPointerException e) {
            return null;
        }
    }

    private int getPrice(String str) {
        str=str.replaceAll("\\s+","");
        Pattern pattern = Pattern.compile("\\d+");
        Matcher m = pattern.matcher(str);
        if (m.find()) {
//            Log.v(TAG, m.group(0));
            try {
                return Integer.valueOf(m.group(0));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    private String getStringOnlyWithAutoHTMLData(String html) {
        Matcher matcher=mPattern.matcher(html);
        StringBuilder stringBuilder=new StringBuilder();
        int numberOfGettingAuotoData=0;
        while (matcher.find()){
            if(matcher.group(0)!=null){
                stringBuilder.append(matcher.group(0));
            }
            if (++numberOfGettingAuotoData>=QUEUE_MAX_CAPACITY){break; }
        }
        return stringBuilder.toString();
    }

    @Override
    public void run() {
        Thread curThread=Thread.currentThread();
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

    public interface NewAutoListener {
        void newAutoEvent(Auto auto);
    }

    public void setOnNewAutoListener(NewAutoListener listener) {
        this.listener=listener;
    }
}
