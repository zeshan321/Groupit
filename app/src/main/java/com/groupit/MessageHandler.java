package com.groupit;

import android.app.Activity;
import android.content.Context;

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
        if (new File(ClientMessage.con.getFilesDir(), group).exists()) {
            this.file = new File(ClientMessage.con.getFilesDir(), group);
        } else {
            try {
                new File(ClientMessage.con.getFilesDir(), group).createNewFile();
                this.file = new File(ClientMessage.con.getFilesDir(), group);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.message = message;
        this.group = group;
        this.con = con;
    }

    public void saveMessage() {
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
        try {
            MessageActivity.myAdapter.clear();
            MessageActivity.chatMsg.setAdapter(MessageActivity.myAdapter);

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
                            while ((line = bufferedReader.readLine()) != null) {
                                if (line != null || line.equals("null") == false) {
                                    if (JSONUtils.canUseMessage(line)) {
                                        String message = JSONUtils.getMessage(line);
                                        name = JSONUtils.getName(line);
                                        if (JSONUtils.getID(line).equals(GroupActivity.ID)) {
                                            MessageActivity.addMessage(true, message, name, MessageActivity.currentGroup);
                                        } else {
                                            MessageActivity.addMessage(false, message, name, MessageActivity.currentGroup);
                                        }
                                    }
                                }
                            }

                            if (MessageActivity.display == null) {
                                MessageActivity.display = name;
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
