package com.groupit;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Environment;
import android.widget.AbsListView;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
                BitmapFactory.Options opts=new BitmapFactory.Options();
                opts.inDither=false;
                opts.inPurgeable=true;
                opts.inInputShareable=true;
                opts.inScaled = false;
                opts.inTempStorage=new byte[32 * 1024];

                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);

                String json = new JSONUtils().getJSONMessage(new Timestamp(new Date().getTime()), new UserData(con).getID(), MessageActivity.currentGroup, file.getAbsolutePath(), MessageActivity.display, true);

                MessageActivity.myAdapter.add(new ChatMessage(true, file.getAbsolutePath(), MessageActivity.display, true, getResizedBitmap(bitmap), true, new Timestamp(new Date().getTime()), json));
                MessageActivity.chatMsg.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                MessageActivity.chatMsg.setAdapter(MessageActivity.myAdapter);

                DatabaseHandler db = new DatabaseHandler(con);
                db.addMessage(MessageActivity.currentGroup, json);
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
                            MessageService.sendData(MessageActivity.currentGroup, new JSONUtils().getJSONMessage(new Timestamp(new Date().getTime()), GroupActivity.ID, MessageActivity.currentGroup, IMGID, MessageActivity.display, true));
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
                File dir;
                File img = null;
                FileOutputStream fos = null;
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

                        fos = new FileOutputStream(img);
                        client.retrieveFile(IMGID + ".jpg", fos);

                    } finally {
                        if (fos != null) {
                            fos.close();
                        }
                        client.logout();
                        if (send) {
                            final File img1 = img;
                            final Timestamp ts = new Timestamp(new Date().getTime());
                            ((Activity) con).runOnUiThread(new Runnable() {
                                public void run() {

                                    if (update) {
                                        BitmapFactory.Options opts=new BitmapFactory.Options();
                                        opts.inDither=false;
                                        opts.inPurgeable=true;
                                        opts.inInputShareable=true;
                                        opts.inScaled = false;
                                        opts.inTempStorage=new byte[32 * 1024];

                                        Bitmap bitmap = BitmapFactory.decodeFile(img1.getAbsolutePath(), opts);

                                        String json = new JSONUtils().getJSONMessage(ts, ID, group, img1.getAbsolutePath(), name, true);

                                        MessageActivity.myAdapter.add(new ChatMessage(false, img1.getAbsolutePath(), name, true, getResizedBitmap(bitmap), true, ts, json));
                                        MessageActivity.chatMsg.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                                        MessageActivity.chatMsg.setAdapter(MessageActivity.myAdapter);
                                    }
                                }
                            });
                            DatabaseHandler db = new DatabaseHandler(con);
                            db.addMessage(group, new JSONUtils().getJSONMessage(ts, ID, group, img1.getAbsolutePath(), name, true));
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public static Bitmap getResizedBitmap(Bitmap bm) {
        if (bm == null) {
            return bm;
        }

        int width = bm.getWidth();
        int height = bm.getHeight();

        float scaleWidth = ((float) 500) / width;
        float scaleHeight = ((float) 500) / height;

        Matrix matrix = new Matrix();
        matrix.setRectToRect(new RectF(0, 0, width, height), new RectF(0, 0, scaleWidth, scaleHeight), Matrix.ScaleToFit.CENTER);

        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
    }
}
