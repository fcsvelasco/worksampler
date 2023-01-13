package com.example.marasigan.worksampler.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.marasigan.worksampler.entities.Project;
import com.example.marasigan.worksampler.entities.Sample;

import java.util.Calendar;

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        Notification notification = intent.getParcelableExtra("Notification");
//        int notificationId = intent.getIntExtra("Notification ID", 0);
//        notificationManager.notify(notificationId, notification);

        Intent i = new Intent(context, GetSampleNotificationService.class);
        i.putExtra("Notification ID", intent.getIntExtra("Notification ID", 0));
        i.putExtra("Notification calendar", intent.getSerializableExtra("Notification calendar"));
        i.putExtra("Notification intent count", intent.getIntExtra("Notification intent count", 0));
        if (intent.getStringExtra("Object name") != null)
            i.putExtra("Object name", intent.getStringExtra("Object name"));
        i.putExtra("File name", intent.getStringExtra("File name"));
        context.startService(i);
    }
}
