package com.groupit;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class ClientMessage {

    public static Socket socket = null;
    private BufferedReader inputReader;
    private PrintWriter outputWriter;
    private static Context con;

    public ClientMessage(Context cont) {
        con = cont;
        connectToServer();
        sendData(JSONUtils.getJSONList());
    }

    private void connectToServer() {
        try {
            socket = new Socket(InetAddress.getByName(MessageActivity.SocketAddress), MessageActivity.SocketServerPORT);
            socket.setKeepAlive(true);
            inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputWriter = new PrintWriter(socket.getOutputStream(), true);
            MessageActivity.finishedSetup = true;

        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread() {
            @Override
            public void run() {
                receiveData();
            }
        }.start();
    }

    private void receiveData() {
        try {
            while (true) {
                final String data = inputReader.readLine();

                if (data == null || data.equals("null") || con == null) {
                    return;
                }
                if (JSONUtils.canUseMessage(data) == false) {
                    return;
                }
                final String message = JSONUtils.getMessage(data);
                final String name = JSONUtils.getName(data);
                final String id = JSONUtils.getID(data);
                final String group = JSONUtils.getGroupID(data);

                ((Activity) con).runOnUiThread(new Runnable() {
                    public void run() {

                        MessageHandler mh = new MessageHandler(MessageActivity.currentGroup, data, con);
                        mh.saveMessage();
                        if (MessageActivity.currentGroup == Integer.parseInt(group)) {
                        if (JSONUtils.getID(data).equals(MessageActivity.getID())) {
                            MessageActivity.addMessage(true, message, name);
                        } else {
                            MessageActivity.addMessage(false, message, name);
                        }
                        }
                    }
                });

                if (MessageActivity.isLooking == false || MessageActivity.currentGroup == Integer.parseInt(group) == false) {
                    Notification(name, message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void Notification(String notificationTitle, String notificationMessage) {

        Intent myIntent = new Intent(con, MessageActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        myIntent.setAction(Long.toString(System.currentTimeMillis()));

        PendingIntent pendingIntent = PendingIntent.getActivity(
                con,
                0,
                myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification myNotification = new NotificationCompat.Builder(con)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage)
                .setTicker("GroupIt")
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.logo)
                .build();

        NotificationManager notificationManager =
                (NotificationManager)con.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, myNotification);
    }

    public void sendData(String messageToSend) {
        outputWriter.println(messageToSend);
    }

    public static boolean isClosed() {
        if (socket == null || socket.isClosed()) {
            return true;
        }
        return false;
    }

    public static ClientMessage reconnect() {
        return new ClientMessage(MessageActivity.con);
    }

    public static void closeSocket() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}