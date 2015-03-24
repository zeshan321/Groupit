package com.groupit;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.AbsListView;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import groupitapi.groupit.com.Main;

public class FTPHandler {

    File file;
    String ID;
    Type type;
    Context con;
    Boolean send;

    public enum Type {
        Image, Video
    }

    public FTPHandler(String ID, Type type, File file, Context con, Boolean send) {
        this.file = file;
        this.ID = ID;
        this.type = type;
        this.con = con;
        this.send = send;
    }

    public void uploadFile() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                FTPClient client = new FTPClient();
                try {
                    try {
                        client.connect(new Main().getIP(), 21);
                        client.login(new Main().getUser(), new Main().getUP());
                        client.enterLocalPassiveMode();
                        client.setFileType(FTP.BINARY_FILE_TYPE);

                        FileInputStream fis = new FileInputStream(file);
                        switch (type) {
                            case Image:
                                client.changeWorkingDirectory("/Images");
                                client.storeFile(ID + ".jpg", fis);
                                break;
                            case Video:
                                break;
                        }


                    } finally {
                        client.logout();

                        if (send) {
                            ClientMessage.sendData(new JSONUtils().getJSONMessage(GroupActivity.ID, MessageActivity.currentGroup, ID, MessageActivity.display, true));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void downloadFile(final String name, final String ID, final String group, final boolean isImage) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                FTPClient client = new FTPClient();
                File dir = null;
                File img = null;

                try {
                    try {
                        client.connect(new Main().getIP(), 21);
                        client.login(new Main().getUser(), new Main().getUP());
                        client.enterLocalPassiveMode();

                        switch (type) {
                            case Image:
                                client.changeWorkingDirectory("/Images");
                                dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"GroupIt");
                                if (!dir.mkdir()) {
                                    dir.mkdir();
                                }

                                img = new File(dir, ID + ".jpg");
                                img.createNewFile();
                                break;
                            case Video:
                                break;
                        }

                        FileOutputStream fos = new FileOutputStream(img);
                        client.retrieveFile(ID + ".jpg", fos);
                        fos.close();

                    } finally {
                        System.out.println("Test: " + img.getPath());
                        System.out.println("Test 1: " + Uri.fromFile(img));
                        client.logout();
                        if (send) {
                            MessageActivity.myAdapter.add(new ChatMessage(false, "Image", name, true, Uri.fromFile(img), true));

                            MessageActivity.chatMsg.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                            MessageActivity.chatMsg.setAdapter(MessageActivity.myAdapter);

                            MessageHandler mh = new MessageHandler(group, new JSONUtils().getJSONMessage(ID, group, Uri.fromFile(img).toString(), name, true), con);
                            mh.saveMessage();
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
