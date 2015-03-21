package com.groupit;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

public class UserData {

    Context con;

    public UserData(Context con) {
        this.con = con;
    }

    public String getID() {
        String myAndroidDeviceId = "";
        TelephonyManager mTelephony = (TelephonyManager) con.getSystemService(Context.TELEPHONY_SERVICE);
        if (mTelephony.getDeviceId() != null){
            myAndroidDeviceId = mTelephony.getDeviceId();
        }else{
            myAndroidDeviceId = Settings.Secure.getString(con.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return myAndroidDeviceId;
    }
}
