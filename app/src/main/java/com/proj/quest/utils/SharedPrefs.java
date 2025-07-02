package com.proj.quest.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefs {
    private static final String PREFS_NAME = "QuestPrefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_AVATAR_URL = "avatar_url";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences prefs;

    public SharedPrefs(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void setLoggedIn(boolean isLoggedIn) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void saveAvatarUrl(String url) {
        prefs.edit().putString(KEY_AVATAR_URL, url).apply();
    }

    public String getAvatarUrl() {
        return prefs.getString(KEY_AVATAR_URL, "");
    }

    public void saveUserId(int userId) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply();
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }

    // Универсальные методы для int
    public void putInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }
    public int getInt(String key, int defValue) {
        return prefs.getInt(key, defValue);
    }

    // Универсальные методы для long
    public void putLong(String key, long value) {
        prefs.edit().putLong(key, value).apply();
    }
    public long getLong(String key, long defValue) {
        return prefs.getLong(key, defValue);
    }

    // Удаление по ключу
    public void remove(String key) {
        prefs.edit().remove(key).apply();
    }

    // Универсальные методы для boolean
    public void putBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }
    public boolean getBoolean(String key, boolean defValue) {
        return prefs.getBoolean(key, defValue);
    }
}
