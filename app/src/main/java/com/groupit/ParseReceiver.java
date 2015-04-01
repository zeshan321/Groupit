package com.groupit;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseReceiver extends ParsePushBroadcastReceiver {
    private final String TAG = "Parse Notification";

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        System.out.println("Received push!");

        String action = intent.getAction();
        String channel = intent.getExtras().getString("com.parse.Channel");
        JSONObject alertJson = null;
        try {
            alertJson = new JSONObject(intent.getExtras().getString("com.parse.Data"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println(new JSONUtils().getAlert(alertJson.toString()));
        super.onPushReceive(context, intent);
    }

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        Notification myNotification = new NotificationCompat.Builder(context)
                .build();

        myNotification.flags|=myNotification.FLAG_NO_CLEAR;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            myNotification.priority = Notification.PRIORITY_MIN;
        }

        return myNotification;
    }
}
