package com.groupit;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.AbsListView;

import com.parse.GetCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.text.ParseException;

import groupitapi.groupit.com.Main;

public class ClientMessage extends Service {

    private static Socket socket;
    private NotificationManager mNM;
    private BroadcastReceiver networkStateReceiver;

    public static Context con = null;
    public static Context tempCon;
    public static BufferedReader inputReader;
    private static PrintWriter outputWriter;
    private static final int NO_CONNECTION_TYPE = -1;
    private static int sLastType = NO_CONNECTION_TYPE;
    public static boolean firstTime = false;

    public class LocalBinder extends Binder {
        ClientMessage getService() {
            return ClientMessage.this;
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

        showNotification();

       networkStateReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager connectivityManager = (ConnectivityManager)
                        context.getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                final int currentType = activeNetworkInfo != null
                        ? activeNetworkInfo.getType() : NO_CONNECTION_TYPE;

                if (sLastType != currentType) {
                    if (activeNetworkInfo != null) {
                        boolean isConnectedOrConnecting = activeNetworkInfo.isConnectedOrConnecting();

                        if (firstTime == false) {
                            firstTime = true;
                        } else {
                            stopService(new Intent(ClientMessage.this, ClientMessage.class));
                            startService(new Intent(ClientMessage.this, ClientMessage.class));
                        }
                    }

                    sLastType = currentType;
                }
            }
        };

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkStateReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(InetAddress.getByName(new Main().getIP()), new Main().getPort());
                    inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    outputWriter = new PrintWriter(socket.getOutputStream(), true);

                    sendData(new JSONUtils().getJSONList());
                    openInput();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(networkStateReceiver);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveData() {
        try {
            String data;
            while ((data = inputReader.readLine()) != null)
            {
                inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
                final boolean[] isOwner = {false};

                if (con != null) {
                    ((Activity) con).runOnUiThread(new Runnable() {
                        public void run() {
                            if (id.equals(GroupActivity.ID)) {
                                isOwner[0] = true;
                            }

                            MessageHandler mh = new MessageHandler(group, data1, con);
                            mh.saveMessage();

                            if (isImage) {
                                if (!isOwner[0]) {
                                    FTPHandler ftp = new FTPHandler(message, FTPHandler.Type.Image, null, con, true);
                                    ftp.downloadFile(name, id, group, isImage);
                                }
                            } else {
                                if (!isOwner[0]) {
                                    MessageActivity.addMessage(isOwner[0], message, name, group);
                                }
                            }
                        }
                    });
                } else {
                    if (!(isImage)) {
                        MessageHandler mh = new MessageHandler(group, data1, tempCon);
                        mh.saveMessage();
                    }
                }

                if ((!MessageActivity.isLooking || !MessageActivity.currentGroup.equals(group))) {
                    if (!(id.equals(GroupActivity.ID))) {
                        if (isImage) {
                            Notification(name, "Image", group);
                        } else {
                            Notification(name, message, group);
                        }
                    }
                }
            }
        } catch (SocketException e) {
           e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void Notification(String notificationTitle, String notificationMessage, String group) {

        Intent myIntent = new Intent(this, GroupActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        myIntent.setAction(Long.toString(System.currentTimeMillis()));

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification myNotification = new NotificationCompat.Builder(this)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage)
                .setTicker(notificationTitle + " @ " + group)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.logo)
                .build();

        mNM.notify(0, myNotification);
    }

    public void openInput() {
        new Thread() {
            @Override
            public void run() {
                receiveData();
            }
        }.start();
    }

    public static void sendData(String messageToSend) {
        try {
            outputWriter = new PrintWriter(socket.getOutputStream(), true);

            System.out.println(messageToSend);

            messageToSend = new StringHandler(StringHandler.Type.COMPRESS, messageToSend).run();
            outputWriter.println(messageToSend);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showNotification() {
        Notification notification = new Notification(R.mipmap.logo, "Starting GroupIt",
                System.currentTimeMillis());

        Intent myIntent = new Intent(this, GroupActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        myIntent.setAction(Long.toString(System.currentTimeMillis()));

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notification.setLatestEventInfo(this, "GroupIt",
                "GroupIt is running", contentIntent);
        notification.flags|=Notification.FLAG_NO_CLEAR;

        startForeground(4756, notification);
    }
}