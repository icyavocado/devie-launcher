package com.voxel.simplesearchlauncher.iconpack;

import android.content.Intent;

/* JADX INFO: loaded from: classes2.dex */
enum IconPackType {
    APEX_LAUNCHER("android.intent.action.MAIN", "com.anddoes.launcher.THEME"),
    GO_LAUNCHER("com.gau.go.launcherex.theme"),
    NOVA_LAUNCHER("com.novalauncher.THEME"),
    ADW_LAUNCHER("org.adw.launcher.icons.ACTION_PICK_ICON");

    public String action;
    public String category;

    IconPackType(String str) {
        this.action = str;
    }

    IconPackType(String str, String str2) {
        this.action = str;
        this.category = str2;
    }

    public Intent makeIntent() {
        Intent intent = new Intent(this.action);
        String str = this.category;
        if (str != null) {
            intent.addCategory(str);
        }
        return intent;
    }
}
