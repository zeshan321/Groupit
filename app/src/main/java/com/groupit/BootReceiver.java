package com.groupit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.telephony.TelephonyManager;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            new GroupHandler(context).loadGroupMem(context);
            Intent serviceIntent = new Intent(context, ClientMessage.class);
            context.startService(serviceIntent);
            ClientMessage.tempCon = context;

            GroupActivity.ID = new UserData(context).getID();
            MessageActivity.display = new NameHandler(null, context).getName();
        }
    }
}
