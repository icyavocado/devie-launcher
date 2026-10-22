package com.voxel.launcher3.shortcuts;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.voxel.launcher3.util.MultiHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/* JADX INFO: loaded from: classes.dex */
public class DeepShortcutConversionList {
    private MultiHashMap<String, Matcher> mMatchers = new MultiHashMap<>();

    class Matcher {
        Intent mConvertToIntent;
        List<String> mMatchExtraKeys;
        Intent mMatchIntent;

        Matcher(Intent intent, List<String> list, Intent intent2) {
            this.mMatchIntent = intent;
            this.mMatchExtraKeys = list;
            this.mConvertToIntent = intent2;
        }

        boolean matches(Intent intent) {
            if (!this.mMatchIntent.filterEquals(intent)) {
                return false;
            }
            Bundle extras = this.mMatchIntent.getExtras();
            Bundle extras2 = intent.getExtras();
            for (String str : this.mMatchExtraKeys) {
                if (!Objects.equals(extras.get(str), extras2.get(str))) {
                    return false;
                }
            }
            return true;
        }
    }

    public DeepShortcutConversionList() {
        addGmailConverters();
        addGMapsConverters();
    }

    private void addGMapsConverters() {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setClassName("com.google.android.apps.maps", "com.google.android.apps.maps.LauncherShortcutActivity");
        intent.putExtra("extra_destination_home_key", true);
        ArrayList arrayList = new ArrayList();
        arrayList.add("extra_destination_home_key");
        Intent intent2 = new Intent("android.intent.action.VIEW");
        intent2.setData(Uri.parse("google.navigation:/?target=n&mode=d&q=Home&entry=dw"));
        intent2.setPackage("com.google.android.apps.maps");
        this.mMatchers.addToList("com.google.android.apps.maps", new Matcher(intent, arrayList, intent2));
        Intent intent3 = new Intent("android.intent.action.VIEW");
        intent3.setClassName("com.google.android.apps.maps", "com.google.android.apps.maps.LauncherShortcutActivity");
        intent3.putExtra("extra_destination_work_key", true);
        ArrayList arrayList2 = new ArrayList();
        arrayList2.add("extra_destination_work_key");
        Intent intent4 = new Intent("android.intent.action.VIEW");
        intent4.setData(Uri.parse("google.navigation:/?target=n&mode=d&q=Work&entry=dw"));
        intent4.setPackage("com.google.android.apps.maps");
        this.mMatchers.addToList("com.google.android.apps.maps", new Matcher(intent3, arrayList2, intent4));
    }

    private void addGmailConverters() {
        Intent intent = new Intent("com.android.mail.intent.action.LAUNCH_COMPOSE");
        Intent intent2 = new Intent("android.intent.action.SEND");
        intent2.setType("text/plain");
        intent2.setPackage("com.google.android.gm");
        this.mMatchers.addToList("com.google.android.gm", new Matcher(intent, Collections.emptyList(), intent2));
    }

    public List<Matcher> getConversionsForPackage(String str) {
        return (List) this.mMatchers.get(str);
    }
}
