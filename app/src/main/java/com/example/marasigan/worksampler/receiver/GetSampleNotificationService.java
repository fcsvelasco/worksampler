package com.example.marasigan.worksampler.receiver;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.example.marasigan.worksampler.MainActivity;
import com.example.marasigan.worksampler.R;
import com.example.marasigan.worksampler.entities.Project;
import com.example.marasigan.worksampler.entities.ProjectDebugMode;
import com.example.marasigan.worksampler.entities.Sample;

import java.util.Calendar;

public class GetSampleNotificationService extends IntentService {
    private static final String CHANNEL_ID = "Maroon Worksampler channel ID";
    Calendar notifyWhen;
    int notificationID, notificationIntentCount;

    public GetSampleNotificationService(){
        super("GetSampleNotificationService");
    }

    public GetSampleNotificationService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        notifyWhen = (Calendar) intent.getSerializableExtra("Notification calendar");
        notificationID = intent.getIntExtra("Notification ID", 0);
        notificationIntentCount = intent.getIntExtra("Notification intent count", 0);

//        String ns = Context.NOTIFICATION_SERVICE;
//        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

        Context context = getApplicationContext();

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.worksamplerlogo_small)
//                .setStyle()
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setWhen(notifyWhen.getTimeInMillis())
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setSound(alarmSound);

        if (notificationIntentCount == Project.MAX_NOTIFICATIONS){
            mBuilder.setTicker("You've been missing observations!");
            mBuilder.setContentTitle("Maroon Worksampler");
            mBuilder.setContentText("You've been missing observations!");
        }else {
            mBuilder.setTicker("Time to get sample!");
            mBuilder.setContentTitle("Maroon Worksampler");
            mBuilder.setContentText("Get sample for " + intent.getStringExtra("Object name"));
        }

        Intent i = new Intent(context, MainActivity.class);
        i.putExtra(Project.IS_FROM_PENDING_INTENT, true);
        i.putExtra("File name", intent.getStringExtra("File name"));

        PendingIntent pendingIntent = PendingIntent.getActivity(context,notificationID,i,PendingIntent.FLAG_UPDATE_CURRENT| PendingIntent.FLAG_ONE_SHOT);
        mBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(notificationID, notification);
    }
}
