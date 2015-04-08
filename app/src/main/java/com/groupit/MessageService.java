package com.groupit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.util.HashMap;

import groupitapi.groupit.com.GroupData;

public class MessageService extends Service{

    public static NotificationManager mNM;

    public static Context con = null;
    public static Context tempCon;
    public static HashMap<String, Integer> count = new HashMap<String, Integer>();

    public class LocalBinder extends Binder {
        MessageService getService() {
            return MessageService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate() {
        GroupActivity.ID = new UserData(this).getID();
        MessageActivity.display = new NameHandler(null, this).getName();

        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        final Context tc = this;

        if (GroupActivity.groups.toString().equals("[]")) {
            GroupHandler.loadGroupMem(tc);
        }

        if (tempCon == null) {
            tempCon = tc;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

    }

    public static void Notification(Context con, String notificationTitle, String notificationMessage, String group, String ID) {
        if (!new SettingsHandler(con).sendNotification()) {
            return;
        }

        new SettingsHandler(con).incrementLimit();

        Intent myIntent = new Intent(con, GroupActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        myIntent.setAction(Long.toString(System.currentTimeMillis()));
        myIntent.putExtra("transferName", group);
        myIntent.putExtra("transferCode", ID);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                con,
                0,
                myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification myNotification = new NotificationCompat.Builder(con)
                .setContentTitle(group)
                .setContentText(notificationTitle + ": " + notificationMessage)
                .setTicker(notificationTitle + " @ " + group)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.logo)
                .build();

        //mNM.notify(new GroupData(ID).getID(), myNotification);
        mNM.notify(404, myNotification);
    }


    public static void sendData(String group, String messageToSend) {
        try {
            messageToSend = new StringHandler(StringHandler.Type.COMPRESS, messageToSend).run();

            new UserData(null).sendMessage(group, messageToSend, group);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}