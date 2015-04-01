package com.groupit;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.SaveCallback;
import com.parse.SendCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import groupitapi.groupit.com.RestAPI;

public class UserData {

    Context con;

    public UserData(Context con) {
        this.con = con;
    }

    public String getID(){
        String myAndroidDeviceId = "";
        TelephonyManager mTelephony = (TelephonyManager) con.getSystemService(Context.TELEPHONY_SERVICE);
        if (mTelephony.getDeviceId() != null){
            myAndroidDeviceId = mTelephony.getDeviceId();
        }else{
            myAndroidDeviceId = Settings.Secure.getString(con.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return myAndroidDeviceId;
    }

    public void updateGroups() {
        List<String> list = new ArrayList<>();
        for (String s: new GroupHandler(con).getGroupsList()) {
            list.add(s);
        }

        ParseInstallation.getCurrentInstallation().put("channels", list);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    public void sendMessage(final String group, final String message, final String title) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new RestAPI(group, message, title).sendMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
