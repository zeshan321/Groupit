package com.groupit;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class NameHandler {

    String display = null;
    Context con;
    File file = null;

    public NameHandler (String name, Context con) {
        this.display = name;
        this.con = con;

        try {
            this.file = new File(con.getFilesDir(), "display");
        } catch (NullPointerException e) {
            try {
                new File(con.getFilesDir(), "display").createNewFile();
                this.file = new File(con.getFilesDir(), "display");
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }
    }

    public void saveName() {
        try {
            BufferedWriter stream = new BufferedWriter(new FileWriter(file, true));
            stream.write(display + "\n");
            stream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }

        InputStreamReader isr = new InputStreamReader(fis);
        final BufferedReader bufferedReader = new BufferedReader(isr);

        String line;
        String name = null;
        try {
            while ((line = bufferedReader.readLine()) != null) {
               name = line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return name;
    }
}
