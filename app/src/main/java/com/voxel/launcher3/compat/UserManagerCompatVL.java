package com.voxel.launcher3.compat;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
@TargetApi(21)
public class UserManagerCompatVL extends UserManagerCompatV17 {
    private final Context mContext;
    private final PackageManager mPm;

    UserManagerCompatVL(Context context) {
        super(context);
        this.mPm = context.getPackageManager();
        this.mContext = context;
    }

    @Override // com.voxel.launcher3.compat.UserManagerCompatV16, com.voxel.launcher3.compat.UserManagerCompat
    public Drawable getBadgedDrawableForUser(Drawable drawable, UserHandleCompat userHandleCompat) {
        return this.mPm.getUserBadgedIcon(drawable, userHandleCompat.getUser());
    }

    @Override // com.voxel.launcher3.compat.UserManagerCompatV16, com.voxel.launcher3.compat.UserManagerCompat
    public CharSequence getBadgedLabelForUser(CharSequence charSequence, UserHandleCompat userHandleCompat) {
        return userHandleCompat == null ? charSequence : this.mPm.getUserBadgedLabel(charSequence, userHandleCompat.getUser());
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    @Override // com.voxel.launcher3.compat.UserManagerCompatV16, com.voxel.launcher3.compat.UserManagerCompat
    public List<UserHandleCompat> getUserProfiles() {
        synchronized (this) {
            if (this.mUsers != null) {
                ArrayList arrayList = new ArrayList();
                arrayList.addAll(this.mUserToSerialMap.keySet());
                return arrayList;
            }
            List<UserHandle> userProfiles = this.mUserManager.getUserProfiles();
            if (userProfiles == null) {
                return Collections.emptyList();
            }
            ArrayList arrayList2 = new ArrayList(userProfiles.size());
            Iterator<UserHandle> it = userProfiles.iterator();
            while (it.hasNext()) {
                arrayList2.add(UserHandleCompat.fromUser(it.next()));
            }
            return arrayList2;
        }
    }
}
