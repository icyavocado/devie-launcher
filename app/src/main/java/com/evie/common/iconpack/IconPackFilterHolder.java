package com.evie.common.iconpack;

/* JADX INFO: loaded from: classes.dex */
public class IconPackFilterHolder {
    private static IconPackFilterHolder instance;
    private IconPackFilter mIconPackFilter;

    public static synchronized IconPackFilterHolder getInstance() {
        if (instance == null) {
            instance = new IconPackFilterHolder();
        }
        return instance;
    }

    public IconPackFilter getIconPackFilter() {
        return this.mIconPackFilter;
    }

    public void setIconPackFilter(IconPackFilter iconPackFilter) {
        this.mIconPackFilter = iconPackFilter;
    }
}
