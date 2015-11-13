package com.domotrix.domotrixdemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TestReceiver extends BroadcastReceiver {
    private final static String TAG = "BroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "*********************************************");
        Log.d(TAG,"*********************************************");
        Log.d(TAG,"BROADCAST RECEIVER CALLED");
        Log.d(TAG,"*********************************************");
        Log.d(TAG,"*********************************************");

        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);
        // Build notification
        // Actions are just fake
        Notification noti = new Notification.Builder(context)
                .setContentTitle("Found")
                .setContentText("connected into Domotrix Realm").setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        noti.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, noti);
    }
}
