package com.groupit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            GroupHandler.loadGroupMem(context);

            Intent serviceIntent = new Intent(context, MessageService.class);
            context.startService(serviceIntent);

            MessageService.tempCon = context;
            GroupActivity.ID = new UserData(context).getID();
            MessageActivity.display = new NameHandler(null, context).getName();
        }
    }
}
