package com.groupit;

import android.content.Context;
import android.provider.ContactsContract;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class FileManager {

    private String name = null;
    private File file;
    static FileManager obj;

    public FileManager(String Filename)  {
        this.name = Filename;

        file = new File(MainActivity.dir, Filename);

        if (file.exists() == false) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class Display {
        public String Display;
    }

    public void setName(String value) {
        try {
            Display contact = new Display();
            contact.Display = value;

            YamlWriter writer = new YamlWriter(new FileWriter(file));

            writer.write(contact);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        String s = null;
        try {
            YamlReader reader = new YamlReader(new FileReader(file));
            Object object = reader.read();

            Map map = (Map) object;
            s = map.get("Display").toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (YamlException e) {
            e.printStackTrace();
        }

        return s;
    }

    public boolean containsName() {
        boolean contains = false;
        try {
            YamlReader reader = new YamlReader(new FileReader(file));
            Object object = reader.read();

            if (object == null) {
                return contains;
            }

            Map map = (Map) object;

            if (map != null && map.isEmpty() != false && map.containsKey("Display")) {
                contains = true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (YamlException e) {
            e.printStackTrace();
        }
        return contains;
    }
}
