package com.proj.quest;

import android.app.Application;
import android.content.Context;

import org.osmdroid.config.Configuration;
import org.osmdroid.library.BuildConfig;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Инициализация OpenStreetMap
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, androidx.preference.PreferenceManager.getDefaultSharedPreferences(ctx));
        // Установка User-Agent, чтобы избежать блокировки
        Configuration.getInstance().setUserAgentValue(getPackageName());
    }
} 