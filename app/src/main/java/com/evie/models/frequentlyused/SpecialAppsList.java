package com.evie.models.frequentlyused;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public final class SpecialAppsList {
    public static final List<String> ALWAYS_USE_DEFAULT;

    static {
        ArrayList arrayList = new ArrayList();
        arrayList.add("com.WhatsApp2Plus");
        arrayList.add("com.WhatsApp3Plus");
        arrayList.add("com.WhatsApp4Plus");
        arrayList.add("com.textra");
        arrayList.add("com.whatsapp");
        ALWAYS_USE_DEFAULT = Collections.unmodifiableList(arrayList);
    }
}
