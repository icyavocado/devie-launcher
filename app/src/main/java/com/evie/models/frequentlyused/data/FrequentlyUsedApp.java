package com.evie.models.frequentlyused.data;

import android.content.ComponentName;
import com.evie.models.frequentlyused.SpecialAppsList;

/* JADX INFO: loaded from: classes.dex */
public class FrequentlyUsedApp {

    public final String className;

    public final String packageName;

    public FrequentlyUsedApp(String str, String str2) {
        this.packageName = str;
        this.className = str2;
    }

    public boolean equals(Object obj) {
        return (obj instanceof FrequentlyUsedApp) && ((FrequentlyUsedApp) obj).packageName.equals(this.packageName);
    }

    public String getClassName() {
        if (SpecialAppsList.ALWAYS_USE_DEFAULT.contains(getPackageName())) {
            return null;
        }
        return this.className;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public int hashCode() {
        return this.packageName.hashCode();
    }

    public ComponentName toComponentName() {
        return new ComponentName(this.packageName, this.className);
    }

    public String toString() {
        return "FrequentlyUsedApp(packageName=" + getPackageName() + ", className=" + getClassName() + ")";
    }
}
