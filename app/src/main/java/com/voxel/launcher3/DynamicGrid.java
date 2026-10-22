package com.voxel.launcher3;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class DynamicGrid {
    static float DEFAULT_ICON_SIZE_DP = 60.0f;
    static float DEFAULT_ICON_SIZE_PX;
    private float mMinHeight;
    private float mMinWidth;
    private DeviceProfile mProfile;

    public DynamicGrid(Context context, Resources resources, int i, int i2, int i3, int i4, int i5, int i6, Map<DeviceProfile.ParamType, CellParams> map, boolean z) {
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        ArrayList arrayList = new ArrayList();
        boolean z2 = !LauncherAppState.isDisableAllApps();
        DEFAULT_ICON_SIZE_PX = pxFromDp(DEFAULT_ICON_SIZE_DP, displayMetrics);
        arrayList.add(new DeviceProfile("Super Short Stubby", 255.0f, 300.0f, 3.0f, 3.0f, 48.0f, 13.0f, z2 ? 3 : 5, 48.0f, R.xml.default_workspace_4x4, R.xml.default_workspace_4x4_no_all_apps));
        arrayList.add(new DeviceProfile("Shorter Stubby", 255.0f, 400.0f, 4.0f, 3.0f, 48.0f, 13.0f, z2 ? 3 : 5, 48.0f, R.xml.default_workspace_4x4, R.xml.default_workspace_4x4_no_all_apps));
        arrayList.add(new DeviceProfile("Short Stubby", 275.0f, 420.0f, 4.0f, 4.0f, 48.0f, 13.0f, 5, 48.0f, R.xml.default_workspace_4x4, R.xml.default_workspace_4x4_no_all_apps));
        arrayList.add(new DeviceProfile("Stubby", 255.0f, 450.0f, 4.0f, 4.0f, 48.0f, 13.0f, 5, 48.0f, R.xml.default_workspace_4x4, R.xml.default_workspace_4x4_no_all_apps));
        arrayList.add(new DeviceProfile("Nexus S", 296.0f, 491.33f, 5.0f, 4.0f, 48.0f, 13.0f, 5, 48.0f, R.xml.default_workspace_4x4, R.xml.default_workspace_4x4_no_all_apps));
        arrayList.add(new DeviceProfile("Nexus 4", 335.0f, 567.0f, 5.0f, 4.0f, DEFAULT_ICON_SIZE_DP, 13.0f, 5, 60.0f, R.xml.default_workspace_4x4, R.xml.default_workspace_4x4_no_all_apps));
        arrayList.add(new DeviceProfile("Nexus 5", 359.0f, 567.0f, 5.0f, 4.0f, DEFAULT_ICON_SIZE_DP, 13.0f, 5, 60.0f, R.xml.default_workspace_4x4, R.xml.default_workspace_4x4_no_all_apps));
        arrayList.add(new DeviceProfile("Large Phone", 406.0f, 694.0f, 6.0f, 5.0f, 64.0f, 14.4f, 5.0f, 64.0f, R.xml.default_workspace_5x5, R.xml.default_workspace_5x5_no_all_apps));
        arrayList.add(new DeviceProfile("Nexus 7", 575.0f, 904.0f, 5.0f, 6.0f, 72.0f, 14.4f, 7.0f, 72.0f, R.xml.default_workspace_5x6, R.xml.default_workspace_5x6_no_all_apps));
        arrayList.add(new DeviceProfile("Nexus 10", 727.0f, 1207.0f, 5.0f, 6.0f, 76.0f, 14.4f, 7.0f, 76.0f, R.xml.default_workspace_5x6, R.xml.default_workspace_5x6_no_all_apps));
        arrayList.add(new DeviceProfile("20-inch Tablet", 1527.0f, 2527.0f, 7.0f, 7.0f, 100.0f, 20.0f, 7.0f, 100.0f, R.xml.default_workspace_4x4, R.xml.default_workspace_4x4_no_all_apps));
        this.mMinWidth = dpiFromPx(i, displayMetrics);
        this.mMinHeight = dpiFromPx(i2, displayMetrics);
        this.mProfile = new DeviceProfile(context, (ArrayList<DeviceProfile>) arrayList, this.mMinWidth, this.mMinHeight, i3, i4, i5, i6, map, resources, z);
    }

    public static float dpiFromPx(int i, DisplayMetrics displayMetrics) {
        return i / (displayMetrics.densityDpi / 160.0f);
    }

    public static int pxFromDp(float f, DisplayMetrics displayMetrics) {
        return Math.round(TypedValue.applyDimension(1, f, displayMetrics));
    }

    public static int pxFromSp(float f, DisplayMetrics displayMetrics) {
        return Math.round(TypedValue.applyDimension(2, f, displayMetrics));
    }

    public DeviceProfile getDeviceProfile() {
        return this.mProfile;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("-------- DYNAMIC GRID ------- \nWd: " + this.mProfile.minWidthDps + ", Hd: " + this.mProfile.minHeightDps + ", W: " + this.mProfile.widthPx + ", H: " + this.mProfile.heightPx);
        for (DeviceProfile.ParamType paramType : DeviceProfile.ParamType.values()) {
            sb.append("\n" + paramType.name() + "\n");
            sb.append(this.mProfile.getCellParamsForType(paramType));
        }
        return sb.toString();
    }
}
