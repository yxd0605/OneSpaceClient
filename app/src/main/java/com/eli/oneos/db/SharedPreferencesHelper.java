package com.eli.oneos.db;

import android.content.Context;
import android.content.SharedPreferences;

import com.eli.oneos.MyApplication;

/**
 * SharedPreferences Helper
 * <p>
 * Created by gaoyun@eli-tech.com on 2016/4/20.
 */
public class SharedPreferencesHelper {
    private static final String SHARED_PREFERENCES_NAME = "OneSpace";

    /**
     * put value in {@link SharedPreferences} by key
     *
     * @param key
     * @param value
     * @return {@code true} if success, otherwise {@code false}
     */
    public static boolean put(String key, String value) {
        SharedPreferences preferences = MyApplication.getAppContext().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString(key, value);

        return edit.commit();
    }

    /**
     * get value from {@link SharedPreferences} by key
     *
     * @param key      value of key
     * @param defValue default value
     * @return value or {@code null}
     */
    public static String get(String key, String defValue) {
        SharedPreferences preferences = MyApplication.getAppContext().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return preferences.getString(key, defValue);
    }

    /**
     * put value in {@link SharedPreferences} by key
     *
     * @param key
     * @param value
     * @return {@code true} if success, otherwise {@code false}
     */
    public static boolean put(String key, boolean value) {
        SharedPreferences preferences = MyApplication.getAppContext().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean(key, value);

        return edit.commit();
    }

    /**
     * get value from {@link SharedPreferences} by key
     *
     * @param key      value of key
     * @param defValue default value
     * @return value or {@code null}
     */
    public static boolean get(String key, boolean defValue) {
        SharedPreferences preferences = MyApplication.getAppContext().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(key, defValue);
    }
}
