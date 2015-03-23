package com.groupit;

import android.app.Application;

import groupitapi.groupit.com.Main;

public class Parse extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        com.parse.Parse.enableLocalDatastore(this);
        com.parse.Parse.initialize(this, new Main().getParse1(), new Main().getParse2());
    }
}