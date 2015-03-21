package com.groupit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            loadGroupMem(context);
            Intent serviceIntent = new Intent(context, ClientMessage.class);
            context.startService(serviceIntent);
            new ClientMessage().tempCon = context;

            new GroupActivity().ID = new UserData(context).getID();
        }
    }

    public void loadGroupMem(Context con) {
        File file = new File(con.getFilesDir(), "groups");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);

        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                String id = new JSONUtils().getGroupID(line);
                new JSONUtils().groups.add(id);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
