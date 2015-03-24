package com.groupit;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.widget.AbsListView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;

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

                            if (MessageActivity.display == null) {
                                NameHandler nh = new NameHandler(null, con);
                                MessageActivity.display = nh.getName();
                            }

                            while ((line = bufferedReader.readLine()) != null) {
                                if (line != null || line.equals("null") == false) {
                                    line = new StringHandler(StringHandler.Type.DECOMPRESS, line).run();
                                    if (new JSONUtils().canUseMessage(line)) {
                                        String message = new JSONUtils().getMessage(line);
                                        boolean isImage = new JSONUtils().isImage(line);
                                        name = new JSONUtils().getName(line);
                                        if (new JSONUtils().getID(line).equals(GroupActivity.ID)) {
                                            if (isImage) {
                                                MessageActivity.myAdapter.add(new ChatMessage(true, "Image", name, true, Uri.parse(message), true));

                                                MessageActivity.chatMsg.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                                                MessageActivity.chatMsg.setAdapter(MessageActivity.myAdapter);
                                            } else {
                                                MessageActivity.addMessage(true, message, name, MessageActivity.currentGroup);
                                            }
                                        } else {
                                            if (isImage) {
                                                MessageActivity.myAdapter.add(new ChatMessage(false, "Image", name, true, Uri.parse(message), true));

                                                MessageActivity.chatMsg.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                                                MessageActivity.chatMsg.setAdapter(MessageActivity.myAdapter);
                                            } else {
                                                MessageActivity.addMessage(false, message, name, MessageActivity.currentGroup);
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

    public void removeGroup(int lineToRemove) {

        try {

            File inFile = file;

            if (!inFile.isFile()) {
                System.out.println("Parameter is not an existing file");
                return;
            }

            File tempFile = new File(inFile.getAbsolutePath() + ".tmp");

            BufferedReader br = new BufferedReader(new FileReader(inFile));
            PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

            String line = null;

            int i = -1;
            while ((line = br.readLine()) != null) {
                i++;
                if (i != lineToRemove) {

                    pw.println(line);
                    pw.flush();
                }
            }
            pw.close();
            br.close();

            //Delete the original file
            if (!inFile.delete()) {
                System.out.println("Could not delete file");
                return;
            }

            //Rename the new file to the filename the original file had.
            if (!tempFile.renameTo(inFile))
                System.out.println("Could not rename file");

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
