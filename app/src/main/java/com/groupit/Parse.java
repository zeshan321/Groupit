package com.groupit;

import android.app.Application;

import com.parse.ParseCrashReporting;
import com.parse.ParseInstallation;

import groupitapi.groupit.com.Main;

public class Parse extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        com.parse.Parse.enableLocalDatastore(this);
        ParseCrashReporting.enable(this);
        com.parse.Parse.initialize(this, new Main().getParse1(), new Main().getParse2());

        new GroupHandler(this).setup();
    }
}