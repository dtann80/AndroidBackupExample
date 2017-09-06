package com.dantann.backupexample;

import android.app.Application;

import timber.log.Timber;


public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree(){
            @Override
            protected String createStackElementTag(StackTraceElement element) {
                return super.createStackElementTag(element) + "." + element.getMethodName();
            }
        });

    }
}
