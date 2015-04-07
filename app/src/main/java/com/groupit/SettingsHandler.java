package com.groupit;

import android.content.Context;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class SettingsHandler {

    private File file;
    private Context con;
    private YamlConfiguration yaml = new YamlConfiguration();

    public static int limit = 0;
    public static HashMap<String, String> settings = new HashMap<>();


    public SettingsHandler(Context con) {
        this.con = con;
        this.file = new File(con.getFilesDir(), "settings.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.load();
        if (!yaml.contains("time")) {
            this.setSettings("0", "0", "0");
        }
    }

    private void load() {
        try {
            yaml.load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void incrementLimit() {
        limit++;
    }

    public void loadSettings() {
        String s1 = yaml.getString("time");
        String s2 = yaml.getString("limit");
        String s3 = yaml.getString("notifications");

        settings.clear();

        settings.put("time", s1);
        settings.put("limit", s2);
        settings.put("notifications", s3);
    }

    public void setSettings(String s1, String s2, String s3) {
        yaml.set("time", s1);
        yaml.set("limit", s2);
        yaml.set("notifications", s3);

        this.save();
        this.loadSettings();
    }

    public boolean sendNotification() {
        boolean b = true;
        if (settings.get("notifications").equals("1")) {
            b = false;
        }
        return b;
    }

    public String getString(String s) {
        return settings.get(s);
    }


    private void save() {
        try {
            this.yaml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
