package com.groupit;

import android.app.Application;

public class Parse extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        com.parse.Parse.enableLocalDatastore(this);
        com.parse.Parse.initialize(this, "Z3eykoUuP71TBbOAagQryHbPnntPajAVQiNQGgOD", "xeQS9Hd3x9LS97GGoA0nbQenLB0qjIafjzWVKyem");
    }
}