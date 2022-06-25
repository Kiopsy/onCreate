package com.example.onCreate.utilities;

import android.app.Application;

import com.example.onCreate.models.Idea;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();

        // Register your parse models
        ParseObject.registerSubclass(Idea.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("GcnXOHr4sZ01BQdtfSKLyWOsKyuNteLEu76wuiRa")
                .clientKey("HFp3qLt3SbFauQVifQu7vZ4c3W5AT5ZeOuU4FWcr")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
