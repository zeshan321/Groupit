package com.groupit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            GroupActivity.loadGroupMem(context);
            Intent serviceIntent = new Intent(context, ClientMessage.class);
            context.startService(serviceIntent);
            ClientMessage.tempCon = context;
        }
    }
}
