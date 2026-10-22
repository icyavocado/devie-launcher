package com.voxel.launcher3.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class MultiHashMap<K, V> extends HashMap<K, ArrayList<V>> {
    public MultiHashMap() {
    }

    public MultiHashMap(int i) {
        super(i);
    }

    public void addToList(K k, V v) {
        ArrayList arrayList = (ArrayList) get(k);
        if (arrayList != null) {
            arrayList.add(v);
            return;
        }
        ArrayList arrayList2 = new ArrayList();
        arrayList2.add(v);
        put(k, arrayList2);
    }

    @Override // java.util.HashMap, java.util.AbstractMap
    public MultiHashMap<K, V> clone() {
        MultiHashMap<K, V> multiHashMap = new MultiHashMap<>(size());
        for (Map.Entry<K, ArrayList<V>> entry : entrySet()) { /* REWORK: entry values are ArrayList */
            multiHashMap.put(entry.getKey(), new ArrayList((Collection) entry.getValue()));
        }
        return multiHashMap;
    }
}
