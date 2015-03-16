package com.groupit;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.AbsListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class ClientMessage {

    public static Socket socket = null;
    public static ClientMessage cm = null;

    private BufferedReader inputReader;
    private PrintWriter outputWriter;
    public static Context con;

    public ClientMessage(Context cont) {
        con = cont;
        connectToServer();
        sendData(JSONUtils.getJSONList());
        cm = this;
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
            String data;
            while ((data = inputReader.readLine()) != null)
            {

                    if (JSONUtils.canUseMessage(data) == false) {
                        return;
                    }

                    final String data1 = data;
                    final String message = JSONUtils.getMessage(data);
                    final String name = JSONUtils.getName(data);
                    final String id = JSONUtils.getID(data);
                    final String group = JSONUtils.getGroupID(data);
                    final Boolean isImage = JSONUtils.isImage(data);

                    ((Activity) con).runOnUiThread(new Runnable() {
                        public void run() {

                            MessageHandler mh = new MessageHandler(group, data1, con);
                            mh.saveMessage();
                            if (MessageActivity.currentGroup.equals(group) && id.equals(GroupActivity.ID) == false) {
                                if (JSONUtils.getID(data1).equals(GroupActivity.ID)) {
                                    if (isImage) {
                                        MessageActivity.myAdapter.add(new ChatMessage(true, message, name, true, null, true));

                                        MessageActivity.chatMsg.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                                        MessageActivity.chatMsg.setAdapter(MessageActivity.myAdapter);
                                    } else {
                                        MessageActivity.addMessage(true, message, name, group);
                                    }
                                } else {
                                    if (isImage) {
                                        MessageActivity.myAdapter.add(new ChatMessage(false, message, name, true, null, true));

                                        MessageActivity.chatMsg.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                                        MessageActivity.chatMsg.setAdapter(MessageActivity.myAdapter);
                                    } else {
                                        MessageActivity.addMessage(false, message, name, group);
                                    }
                                }
                            }
                        }
                    });

                    if (MessageActivity.isLooking == false || MessageActivity.currentGroup.equals(group) == false && id.equals(GroupActivity.ID) == false) {
                        if (isImage) {
                            Notification(name, "Image", group);
                        } else {
                            Notification(name, message, group);
                        }
                    }
            }
        } catch (SocketException e) {
            ClientMessage.closeSocket();
            MessageActivity.cm = new ClientMessage(MessageActivity.con);
        } catch (IOException e) {
            ClientMessage.closeSocket();
            MessageActivity.cm = new ClientMessage(MessageActivity.con);
        } catch (NullPointerException e) {
        ClientMessage.closeSocket();
        MessageActivity.cm = new ClientMessage(MessageActivity.con);
    }
    }

    public static void Notification(String notificationTitle, String notificationMessage, String group) {

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
                .setTicker(notificationTitle + " @ " + group)
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
        MessageActivity.allowReConnect = false;
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