package com.groupit;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseInstallation;

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
        return new SettingsHandler(con).getID();
    }

    public void updateGroups() {
        List<String> list = new ArrayList<>();
        for (String s: new GroupHandler(con).getGroupsList()) {
            list.add(s);
        }

        ParseInstallation.getCurrentInstallation().put("channels", list);
        ParseInstallation.getCurrentInstallation().saveInBackground();

        new SettingsHandler(con).setID(ParseInstallation.getCurrentInstallation().getInstallationId());
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

    public void sendToast(String message) {
        LayoutInflater inflater = ((Activity)con).getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout,
                (ViewGroup) ((Activity)con).findViewById(R.id.toast_layout_root));

        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(message);

        Toast toast = new Toast(con);
        toast.setGravity(Gravity.BOTTOM, 0, 160);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
