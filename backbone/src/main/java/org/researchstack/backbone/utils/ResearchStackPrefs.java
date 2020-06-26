package org.researchstack.backbone.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ResearchStackPrefs {

    public static final String NULL_FIELD = "";
    private static final String OMRON_DEVICE_ID = "omron_device_id";

    private final SharedPreferences prefs;

    public ResearchStackPrefs(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private void put(String key, String val) {
        prefs.edit().putString(key, val).apply();
    }

    private void put(String key, boolean val) {
        prefs.edit().putBoolean(key, val).apply();
    }

    private void put(String key, long val) {
        prefs.edit().putLong(key, val).apply();
    }

    @Nullable
    public String getOmronDeviceId() {
        return prefs.getString(OMRON_DEVICE_ID, NULL_FIELD);
    }

    public void updateOmronDeviceId(@NonNull String did) {
        put(OMRON_DEVICE_ID, did);
    }
}