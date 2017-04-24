package net.gusakov.newnettiauto.classes;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import net.gusakov.newnettiauto.Constants;
import net.gusakov.newnettiauto.MainActivity;
import net.gusakov.newnettiauto.R;
import net.gusakov.newnettiauto.services.AutoSearchService;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by hasana on 2/16/2017.
 */

public class NotificationManager implements Constants.NotificationManagerConstants {
    private android.app.NotificationManager nm;
    private Context ctx;



    public NotificationManager(Context ctx) {
        nm = (android.app.NotificationManager) ctx.getSystemService(NOTIFICATION_SERVICE);
        this.ctx=ctx;
    }

    public Notification getForegroundNotification(String text){
        Intent intent = new Intent(ctx, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        intent.putExtra(MainActivity.FILE_NAME, "somefile");
        PendingIntent pIntent = PendingIntent.getActivity(ctx, (int)System.currentTimeMillis(), intent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopIntent = new Intent(ctx, AutoSearchService.class);
        stopIntent.setAction(Constants.ServiceConstants.STOP_ACTION);
        PendingIntent pstopIntent = PendingIntent.getService(ctx, 0,
                stopIntent, 0);


        Notification notification = new NotificationCompat.Builder(ctx)
                .setContentTitle("Nettiauto Searcher")
                .setTicker("Nettiauto Searcher")
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .setOngoing(true)
                .addAction(new NotificationCompat.Action.Builder(android.R.drawable.ic_media_pause, "Stop",
                        pstopIntent).build()).build();

        return notification;
    }

    public void updateForegroundServiceNotification(String text){
        nm.notify(NOTIFICATION_FOREGROUND_ID, getForegroundNotification(text));
    }
    public void notify(Auto auto){
        Intent intent = new Intent(ctx, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(ctx, (int)System.currentTimeMillis(), intent,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(ctx);
        builder.setTicker("new Auto detected")
                .setContentTitle(auto.getName())
                .setContentText(auto.getDescription())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .setOngoing(false)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                .setSubText(auto.getYearAndMileage())
                .setContentInfo(/*auto.getPrice()+*/"1");

        Notification notif=builder.build();

        // sending
        nm.notify(NOTIFICATION_NOTIFY_ID, notif);
    }
}
