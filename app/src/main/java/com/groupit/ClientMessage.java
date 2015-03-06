package com.groupit;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

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
    }

    private void connectToServer() {
        try {
            socket = new Socket(InetAddress.getByName(MessageActivity.SocketAddress), MessageActivity.SocketServerPORT);
            socket.setKeepAlive(true);
            inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputWriter = new PrintWriter(socket.getOutputStream(), true);

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
                 String data = inputReader.readLine();

                if (data == null || data.equals("null") || con == null) {
                    return;
                }
                final String message = JSONUtils.getMessage(data);
                final String id = JSONUtils.getID(data);

                MessageActivity.msgLog.add(message);

                ((Activity) con).runOnUiThread(new Runnable() {
                    public void run() {

                        MessageHandler mh = new MessageHandler(MessageActivity.currentGroup, message, con);
                        mh.saveMessage();

                        MessageActivity.myAdapter.add(message);
                        MessageActivity.chatMsg.setAdapter(MessageActivity.myAdapter);
                        MessageActivity.scrollDown();
                    }
                });

                if (MessageActivity.isLooking == false) {
                    Notification("GroupIt", message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void Notification(String notificationTitle, String notificationMessage) {

        NotificationManager notificationManager = (NotificationManager) con
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification noti = new NotificationCompat.Builder(con)
                .setSmallIcon(R.mipmap.logo)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true).build();
        noti.setLatestEventInfo(con, "GroupIt",
                notificationMessage, PendingIntent.getActivity(con,
                        0, new Intent(con, MessageActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                        PendingIntent.FLAG_CANCEL_CURRENT));
        noti.flags |= Notification.FLAG_ONGOING_EVENT;
        int notifyID = 0;

        notificationManager.notify(notifyID, noti);
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
