package com.voxel.simplesearchlauncher.appoptions;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
public class IconSearchHelper {
    public static List<String> queryList(String str, List<String> list) {
        if (TextUtils.isEmpty(str)) {
            return list;
        }
        String[] strArrSplit = str.trim().split("\\s+");
        ArrayList arrayList = new ArrayList(list);
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            String lowerCase = ((String) it.next()).toLowerCase();
            if (TextUtils.isEmpty(lowerCase)) {
                it.remove();
            } else {
                for (String str2 : strArrSplit) {
                    if (!lowerCase.contains(str2)) {
                        it.remove();
                        break;
                    }
                }
            }
        }
        return arrayList;
    }
}
