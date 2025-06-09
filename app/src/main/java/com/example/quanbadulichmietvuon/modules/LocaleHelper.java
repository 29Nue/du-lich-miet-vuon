package com.example.quanbadulichmietvuon.modules;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;

public class LocaleHelper {
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_LANGUAGE = "language";

    public static Context setLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String langCode = prefs.getString(KEY_LANGUAGE, "vi"); // mặc định là tiếng việt

        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        context.getResources().updateConfiguration(config, resources.getDisplayMetrics());
        return context;
    }

    public static void saveLanguage(Context context, String langCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply();
    }
}
