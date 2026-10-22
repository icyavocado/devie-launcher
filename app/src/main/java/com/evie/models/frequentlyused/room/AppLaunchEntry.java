package com.evie.models.frequentlyused.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.evie.models.frequentlyused.SpecialAppsList;

/* JADX INFO: loaded from: classes.dex */
@Entity(tableName = "app_launch_entries")
public class AppLaunchEntry {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "package_name")
    private String packageName;

    @ColumnInfo(name = "class_name")
    private String className;

    public AppLaunchEntry() {
    }

    public AppLaunchEntry(int i, String str, String str2) {
        this.id = i;
        this.packageName = str;
        this.className = str2;
    }

    protected boolean canEqual(Object obj) {
        return obj instanceof AppLaunchEntry;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AppLaunchEntry)) {
            return false;
        }
        AppLaunchEntry appLaunchEntry = (AppLaunchEntry) obj;
        if (!appLaunchEntry.canEqual(this)) {
            return false;
        }
        String packageName = getPackageName();
        String packageName2 = appLaunchEntry.getPackageName();
        if (packageName != null ? !packageName.equals(packageName2) : packageName2 != null) {
            return false;
        }
        String className = getClassName();
        String className2 = appLaunchEntry.getClassName();
        return className != null ? className.equals(className2) : className2 == null;
    }

    public String getClassName() {
        if (SpecialAppsList.ALWAYS_USE_DEFAULT.contains(getPackageName())) {
            return null;
        }
        return this.className;
    }

    public int getId() {
        return this.id;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public int hashCode() {
        String packageName = getPackageName();
        int iHashCode = packageName == null ? 43 : packageName.hashCode();
        String className = getClassName();
        return ((iHashCode + 59) * 59) + (className != null ? className.hashCode() : 43);
    }

    public void setClassName(String str) {
        this.className = str;
    }

    public void setId(int i) {
        this.id = i;
    }

    public void setPackageName(String str) {
        this.packageName = str;
    }
}
