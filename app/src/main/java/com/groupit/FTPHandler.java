package com.groupit;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.sql.Timestamp;
import java.util.Date;

import groupitapi.groupit.com.Main;

public class FTPHandler {

    File file;
    String IMGID;
    Type type;
    Context con;
    Boolean send;

    public enum Type {
        Image, Video
    }

    public FTPHandler(String IMGID, Type type, File file, Context con, Boolean send) {
        this.file = file;
        this.IMGID = IMGID;
        this.type = type;
        this.con = con;
        this.send = send;
    }

    public void uploadFile() {

        ((Activity) con).runOnUiThread(new Runnable() {
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                MessageActivity.myAdapter.add(new ChatMessage(true, "Image", MessageActivity.display, true, bitmap, true, new Timestamp(new Date().getTime())));
                MessageActivity.chatMsg.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                MessageActivity.chatMsg.setAdapter(MessageActivity.myAdapter);

                MessageHandler mh = new MessageHandler(MessageActivity.currentGroup, new JSONUtils().getJSONMessage(new Timestamp(new Date().getTime()), new UserData(con).getID(), MessageActivity.currentGroup, file.getAbsolutePath(), MessageActivity.display, true), con);
                mh.saveMessage();
            }
        });

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
                                client.storeFile(IMGID + ".jpg", fis);
                                break;
                            case Video:
                                break;
                        }


                    } finally {
                        client.logout();

                        if (send) {
                            ClientMessage.sendData(new JSONUtils().getJSONMessage(new Timestamp(new Date().getTime()), GroupActivity.ID, MessageActivity.currentGroup, IMGID, MessageActivity.display, true));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void downloadFile(final String name, final String ID, final String group, final boolean isImage, final boolean update) {
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
                                dir.mkdir();
                                dir.mkdirs();

                                img = new File(dir, IMGID + ".jpg");
                                img.createNewFile();
                                break;
                            case Video:
                                break;
                        }

                        FileOutputStream fos = new FileOutputStream(img);
                        client.retrieveFile(IMGID + ".jpg", fos);
                        fos.close();

                    } finally {
                        client.logout();
                        if (send) {
                            final File img1 = img;
                            final Timestamp ts = new Timestamp(new Date().getTime());
                            ((Activity) con).runOnUiThread(new Runnable() {
                                public void run() {

                                    if (update) {
                                        Bitmap bitmap = BitmapFactory.decodeFile(img1.getAbsolutePath());

                                        MessageActivity.myAdapter.add(new ChatMessage(false, "Image", name, true, bitmap, true, ts));
                                        MessageActivity.chatMsg.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                                        MessageActivity.chatMsg.setAdapter(MessageActivity.myAdapter);
                                    }
                                }
                            });
                            MessageHandler mh = new MessageHandler(group, new JSONUtils().getJSONMessage(ts, ID, group, img1.getAbsolutePath(), name, true), con);
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
