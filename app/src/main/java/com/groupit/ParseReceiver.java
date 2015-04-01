package com.groupit;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;

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

        System.out.println(alertJson);

        String data = new JSONUtils().getAlert(alertJson.toString());
        data = new StringHandler(StringHandler.Type.DECOMPRESS, data).run();

        if (new JSONUtils().canUseMessage(data) == false) {
            return;
        }

        final String data1 = data;
        final String message = new JSONUtils().getMessage(data);
        final String name = new JSONUtils().getName(data);
        final String id = new JSONUtils().getID(data);
        final String group = new JSONUtils().getGroupID(data);
        final Boolean isImage = new JSONUtils().isImage(data);
        final Timestamp ts = Timestamp.valueOf(new JSONUtils().getTimeStamp(data));

        final Context con = MessageService.con;
        final Context tempCon = MessageService.tempCon;

        if (!GroupActivity.groups.contains(group)) {
            return;
        }

        if (con != null) {
            ((Activity) con).runOnUiThread(new Runnable() {
                public void run() {
                    boolean owner = false;
                    if (id.equals(GroupActivity.ID)) {
                        owner = true;
                    }

                    if (isImage) {
                        if (!id.equals(new UserData(con).getID())) {
                            FTPHandler ftp = new FTPHandler(message, FTPHandler.Type.Image, null, con, true);
                            ftp.downloadFile(name, id, group, isImage, true);
                        }
                    } else {
                        MessageHandler mh = new MessageHandler(group, data1, con);
                        mh.saveMessage();

                        if (!owner) {
                            if (group.equals(MessageActivity.currentGroup)) {
                                MessageActivity.addMessage(owner, message, name, group, ts);
                            }
                        }
                    }
                }
            });
        } else {
            if (isImage) {
                if (!id.equals(new UserData(con).getID())) {
                    FTPHandler ftp = new FTPHandler(message, FTPHandler.Type.Image, null, tempCon, true);
                    ftp.downloadFile(name, id, group, isImage, false);
                }
            } else {
                MessageHandler mh = new MessageHandler(group, data1, tempCon);
                mh.saveMessage();
            }
        }

        if ((!MessageActivity.isLooking || !MessageActivity.currentGroup.equals(group))) {
            if (!(id.equals(GroupActivity.ID))) {
                if (isImage) {
                    String groupDisplay = new GroupHandler(tempCon).idtoDisplay(group);
                    MessageService.Notification(context, name, "Image", groupDisplay, group);

                    if (MessageService.count.containsKey(groupDisplay)) {
                        MessageService.count.put(groupDisplay, MessageService.count.get(groupDisplay) + 1);
                    } else {
                        MessageService.count.put(groupDisplay, 0);
                    }
                } else {
                    String groupDisplay = new GroupHandler(tempCon).idtoDisplay(group);
                    MessageService.Notification(context, name, message, groupDisplay, group);

                    if (MessageService.count.containsKey(groupDisplay)) {
                        MessageService.count.put(groupDisplay, MessageService.count.get(groupDisplay) + 1);
                    } else {
                        MessageService.count.put(groupDisplay, 0);
                    }
                }
            }
        }
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
