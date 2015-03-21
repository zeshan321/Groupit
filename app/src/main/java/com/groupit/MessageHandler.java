package com.groupit;

import android.app.Activity;
import android.content.Context;
import android.widget.AbsListView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class MessageHandler {

    String message;
    String group;
    File file = null;
    Context con = null;


    public MessageHandler(String group, String message, Context con) {
        this.con = con;
        this.message = message;
        this.group = group;

        if (new File(con.getFilesDir(), group).exists()) {
            this.file = new File(con.getFilesDir(), group);
        } else {
            try {
                new File(con.getFilesDir(), group).createNewFile();
                this.file = new File(con.getFilesDir(), group);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveMessage() {
        String message = new StringHandler(StringHandler.Type.COMPRESS, this.message).run();

        if (message == null == false && message.equals("null") == false) {
            try {
                BufferedWriter stream = new BufferedWriter(new FileWriter(file, true));
                stream.write(message + "\n");
                stream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadMessages() {
       final MessageActivity MA = new MessageActivity();
        try {
            MA.myAdapter.clear();
            MA.chatMsg.setAdapter(MA.myAdapter);

            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            InputStreamReader isr = new InputStreamReader(fis);
            final BufferedReader bufferedReader = new BufferedReader(isr);
            try {
                ((Activity) con).runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String line;
                            String name = null;

                            if (MA.display == null) {
                                NameHandler nh = new NameHandler(null, con);
                                MA.display = nh.getName();
                            }

                            while ((line = bufferedReader.readLine()) != null) {
                                if (line != null || line.equals("null") == false) {
                                    line = new StringHandler(StringHandler.Type.DECOMPRESS, line).run();
                                    if (new JSONUtils().canUseMessage(line)) {
                                        String message = new JSONUtils().getMessage(line);
                                        boolean isImage = new JSONUtils().isImage(line);
                                        name = new JSONUtils().getName(line);
                                        if (new JSONUtils().getID(line).equals(new GroupActivity().ID)) {
                                            if (isImage) {
                                                MA.myAdapter.add(new ChatMessage(true, message, name, true, null, true));

                                                MA.chatMsg.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                                                MA.chatMsg.setAdapter(MA.myAdapter);
                                            } else {
                                                new MessageActivity().addMessage(true, message, name, MA.currentGroup);
                                            }
                                        } else {
                                            if (isImage) {
                                                MA.myAdapter.add(new ChatMessage(false, message, name, true, null, true));

                                                MA.chatMsg.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                                                MA.chatMsg.setAdapter(MA.myAdapter);
                                            } else {
                                                new MessageActivity().addMessage(false, message, name, MA.currentGroup);
                                            }
                                        }
                                    }
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } finally {
                System.out.println("Loaded.");
            }
        } catch (NullPointerException e) {
            return;
        }
    }
}
