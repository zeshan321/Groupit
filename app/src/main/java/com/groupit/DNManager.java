package com.groupit;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DNManager {

    private String name = null;
    private File file;
    static DNManager obj;

    public DNManager(String Filename)  {
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

    public void setName(String value) {
        try {
            YamlWriter writer = new YamlWriter(new FileWriter(file));

            writer.write("Display: " + value);
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
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line = br.readLine();
            line = line.replace("Display: ", "").replace("'", "");

            s = line;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
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

            if (object.toString().startsWith("Display: ")) {
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
