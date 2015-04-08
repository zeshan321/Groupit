package com.groupit;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class SettingsHandler {

    private Context con;
    private SharedPreferences settings;

    public static int limit = 0;


    public SettingsHandler(Context con) {
        this.con = con;
        this.settings = PreferenceManager.getDefaultSharedPreferences(con);

        if (!settings.contains("time")) {
            setSettings(0, 0, true);
        }
    }


    public void incrementLimit() {
        limit++;
    }


    public void setSettings(int s1, int s2, boolean s3) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("time", s1);
        editor.putInt("limit", s2);
        editor.putBoolean("notifications", s3);
        editor.apply();
    }

    public boolean sendNotification() {
        return true;
    }

    public int getInt(String s) {
        return settings.getInt(s, 0);
    }
}
