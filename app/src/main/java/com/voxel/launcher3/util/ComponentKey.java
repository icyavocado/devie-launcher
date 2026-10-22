package com.voxel.launcher3.util;

import android.content.ComponentName;
import com.voxel.launcher3.compat.UserHandleCompat;
import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
public class ComponentKey {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public final ComponentName componentName;
    private final int mHashCode;
    public final UserHandleCompat user;

    public ComponentKey(ComponentName componentName, UserHandleCompat userHandleCompat) {
        this.componentName = componentName;
        this.user = userHandleCompat;
        this.mHashCode = Arrays.hashCode(new Object[]{componentName, userHandleCompat});
    }

    public boolean equals(Object obj) {
        ComponentKey componentKey = (ComponentKey) obj;
        return componentKey.componentName.equals(this.componentName) && componentKey.user.equals(this.user);
    }

    public int hashCode() {
        return this.mHashCode;
    }
}
