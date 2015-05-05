package com.groupit;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.AbsListView;

import java.io.File;
import java.sql.Timestamp;

public class MessageHandler {

    Context con = null;

    public MessageHandler(Context con) {
        this.con = con;
    }

    public void addMSG(final String line) {
        ((Activity) con).runOnUiThread(new Runnable() {
            public void run() {
                if (new JSONUtils().canUseMessage(line)) {
                    String message = new JSONUtils().getMessage(line);
                    boolean isImage = new JSONUtils().isImage(line);
                    String name = new JSONUtils().getName(line);
                    Timestamp ts = Timestamp.valueOf(new JSONUtils().getTimeStamp(line));

                    if (new JSONUtils().getID(line).equals(GroupActivity.ID)) {
                        if (isImage && new File(message).exists()) {
                            BitmapFactory.Options opts=new BitmapFactory.Options();
                            opts.inDither=false;
                            opts.inPurgeable=true;
                            opts.inInputShareable=true;
                            opts.inScaled = false;
                            opts.inTempStorage=new byte[32 * 1024];
                            Bitmap bitmap = BitmapFactory.decodeFile(message, opts);

                            MessageActivity.myAdapter.add(new ChatMessage(true, message, name, true, FTPHandler.getResizedBitmap(bitmap, con), true, ts, line));

                            MessageActivity.chatMsg.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                            MessageActivity.chatMsg.setAdapter(MessageActivity.myAdapter);
                        } else {
                            MessageActivity.addMessage(true, message, name, MessageActivity.currentGroup, ts, line);
                        }
                    } else {
                        if (isImage && new File(message).exists()) {
                            BitmapFactory.Options opts=new BitmapFactory.Options();
                            opts.inDither=false;
                            opts.inPurgeable=true;
                            opts.inInputShareable=true;
                            opts.inScaled = false;
                            opts.inTempStorage=new byte[32 * 1024];
                            Bitmap bitmap = BitmapFactory.decodeFile(message, opts);

                            MessageActivity.myAdapter.add(new ChatMessage(false, message, name, true, FTPHandler.getResizedBitmap(bitmap, con), true, ts, line));

                            MessageActivity.chatMsg.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                            MessageActivity.chatMsg.setAdapter(MessageActivity.myAdapter);
                        } else {
                            MessageActivity.addMessage(false, message, name, MessageActivity.currentGroup, ts, line);
                        }
                    }
                }
            }
        });
    }
}
