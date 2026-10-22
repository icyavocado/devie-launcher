package com.voxel.launcher3;

import android.graphics.PointF;

/* JADX INFO: compiled from: DeviceProfile.java */
/* JADX INFO: loaded from: classes.dex */
class DeviceProfileQuery {
    PointF dimens;
    float heightDps;
    DeviceProfile profile;
    float value;
    float widthDps;

    DeviceProfileQuery(DeviceProfile deviceProfile, float f) {
        this.widthDps = deviceProfile.minWidthDps;
        this.heightDps = deviceProfile.minHeightDps;
        this.value = f;
        this.dimens = new PointF(this.widthDps, this.heightDps);
        this.profile = deviceProfile;
    }
}
