package com.govmap.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by MediumMG on 18.10.2015.
 */
public class AppPreferences {

    public static final String KEY_IS_TC_SHOWED = "is_T&C_showed";

    private static final String PREFERECE_NAME = "com.govmap.preferences";

    private static SharedPreferences mPreferences;

    private static SharedPreferences getSharedPreferences(Context context) {
        if (mPreferences == null) {
            synchronized (AppPreferences.class) {
                if (mPreferences == null)
                    mPreferences = context.getSharedPreferences(PREFERECE_NAME, Context.MODE_PRIVATE);
            }
        }
        return mPreferences;
    }

    public static void setBoolean(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    public static boolean getBoolean(Context context, String key) {
        return getSharedPreferences(context).getBoolean(key, false);
    }
}
