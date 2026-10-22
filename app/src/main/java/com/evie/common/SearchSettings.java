package com.evie.common;

import android.content.Context;
import android.content.SharedPreferences;

/* JADX INFO: loaded from: classes.dex */
public class SearchSettings {
    private static SearchSettings instance;
    private SharedPreferences mSharedPreferences;

    private SearchSettings(Context context) {
        this.mSharedPreferences = context.getSharedPreferences("com.evie.search", 0);
    }

    public static SearchSettings getInstance(Context context) {
        if (instance == null) {
            instance = new SearchSettings(context.getApplicationContext());
        }
        return instance;
    }

    public boolean getIncludeFrequentlyUsed() {
        return this.mSharedPreferences.getBoolean("include_frequently_used", true);
    }

    public int getLastDecay() {
        return this.mSharedPreferences.getInt("last_decay", 0);
    }

    public int getMaxRecents() {
        return this.mSharedPreferences.getInt("max_recents", 3);
    }

    public boolean getOpenWebResultsInBrowser() {
        return this.mSharedPreferences.getBoolean("open_web_results_in_browser", true);
    }

    public boolean getRecentsIncludeWebResults() {
        return this.mSharedPreferences.getBoolean("recents_include_web_results", false);
    }

    public int getSearchEngine() {
        return this.mSharedPreferences.getInt("search_engine", 0);
    }

    public void setIncludeFrequentlyUsed(boolean z) {
        this.mSharedPreferences.edit().putBoolean("include_frequently_used", z).apply();
    }

    public void setLastDecay(int i) {
        this.mSharedPreferences.edit().putInt("last_decay", i).apply();
    }

    public void setMaxRecents(int i) {
        this.mSharedPreferences.edit().putInt("max_recents", i).apply();
    }

    public void setOpenWebResultsInBrowser(boolean z) {
        this.mSharedPreferences.edit().putBoolean("open_web_results_in_browser", z).apply();
    }

    public void setRecentsIncludeWebResults(boolean z) {
        this.mSharedPreferences.edit().putBoolean("recents_include_web_results", z).apply();
    }

    public void setSearchEngine(int i) {
        this.mSharedPreferences.edit().putInt("search_engine", i).apply();
    }
}
