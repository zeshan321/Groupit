package com.groupit;

import android.app.Activity;
import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class MessageHandler {

    String message;
    int group;
    File file = null;
    Context con = null;


    public MessageHandler(int group, String message, Context con) {
        this.file = new File(MessageActivity.con.getFilesDir(), String.valueOf(group));
        this.message = message;
        this.group = group;
        this.con = con;

        if (file.exists() == false) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveMessage() {
        BufferedWriter stream = null;
        try {
            stream = new BufferedWriter(new FileWriter(file, true));
            stream.write(message + "\n");
            stream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMessages() {
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
                        while ((line = bufferedReader.readLine()) != null) {
                            System.out.println(line);
                            MessageActivity.myAdapter.add(line);
                            MessageActivity.chatMsg.setAdapter(MessageActivity.myAdapter);

                        }

                        if (MessageActivity.display == null) {
                            String[] displayn = line.split(":");
                            MessageActivity.display = displayn[0];
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } finally {
            System.out.println("Loaded.");
        }
    }
}
