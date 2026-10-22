package com.voxel.simplesearchlauncher.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import com.voxel.launcher3.CellParams;
import com.voxel.launcher3.DeviceProfile;
import com.voxel.launcher3.FolderIcon;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.simplesearchlauncher.utils.UpgradeUtil;
import java.util.HashMap;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class LayoutSettingsManager {
    private static final String ICON_PREF_NAME = LayoutSettingsManager.class.getCanonicalName() + ".pref";
    private DataSetObservable mDataSetObservable;
    private SharedPreferences mSharedPreferences;
    private final UpgradeUtil.UpgradableComponent mUpgradeComponent = new UpgradeUtil.UpgradableComponent() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$LayoutSettingsManager$SbY0NVEv4pQbMcIu41vrz5lfGn0
        @Override // com.voxel.simplesearchlauncher.utils.UpgradeUtil.UpgradableComponent
        public final void onUpgrade(int i, int i2) {
            LayoutSettingsManager.lambda$new$0(i, i2);
        }
    };

    public LayoutSettingsManager(Context context) {
        this.mSharedPreferences = context.getSharedPreferences(ICON_PREF_NAME, 0);
        upgradeIfNeeded();
        this.mDataSetObservable = new DataSetObservable();
        migrateOldLayoutSettingsIfAny();
    }

    private String getPrefixForType(DeviceProfile.ParamType paramType) {
        switch (paramType) {
            case HOTSEAT:
                return "dock_";
            case WORKSPACE:
                return "desktop_";
            case ALLAPPS:
                return "allapps_";
            case FOLDER:
                return "folder_";
            default:
                throw new UnsupportedOperationException();
        }
    }

    public static String getSharedPreferencesKey() {
        return ICON_PREF_NAME;
    }

    private void internalSetBackgroundAlpha(DeviceProfile.ParamType paramType, float f) {
        this.mSharedPreferences.edit().putFloat(getPrefixForType(paramType) + "background_alpha", f).apply();
    }

    private void internalSetBackgroundBlur(DeviceProfile.ParamType paramType, boolean z) {
        this.mSharedPreferences.edit().putBoolean(getPrefixForType(paramType) + "background_blur", z).apply();
    }

    private void internalSetColCount(DeviceProfile.ParamType paramType, int i) {
        this.mSharedPreferences.edit().putInt(getPrefixForType(paramType) + "col", i).apply();
    }

    private void internalSetContainerEnabled(DeviceProfile.ParamType paramType, boolean z) {
        this.mSharedPreferences.edit().putBoolean(getPrefixForType(paramType) + "container_enabled", z).apply();
    }

    private void internalSetInheritDesktop(DeviceProfile.ParamType paramType, boolean z) {
        this.mSharedPreferences.edit().putBoolean(getPrefixForType(paramType) + "inherit_desktop", z).apply();
    }

    private void internalSetRowCount(DeviceProfile.ParamType paramType, int i) {
        this.mSharedPreferences.edit().putInt(getPrefixForType(paramType) + "row", i).apply();
    }

    private void internalSetScale(DeviceProfile.ParamType paramType, float f) {
        this.mSharedPreferences.edit().putFloat(getPrefixForType(paramType) + "scale", f).apply();
    }

    private void internalSetShowLabel(DeviceProfile.ParamType paramType, boolean z) {
        this.mSharedPreferences.edit().putBoolean(getPrefixForType(paramType) + "show_label", z).apply();
    }

    static /* synthetic */ void lambda$new$0(int i, int i2) {
    }

    private void migrateOldLayoutSettingsIfAny() {
        boolean z;
        int i = this.mSharedPreferences.getInt("row", -1);
        int i2 = this.mSharedPreferences.getInt("col", -1);
        float f = this.mSharedPreferences.getFloat("scale", -1.0f);
        if (i == -1 || getRowCount(DeviceProfile.ParamType.WORKSPACE) != -1) {
            z = false;
        } else {
            internalSetRowCount(DeviceProfile.ParamType.WORKSPACE, i);
            z = true;
        }
        if (i2 != -1 && getColCount(DeviceProfile.ParamType.WORKSPACE) == -1) {
            internalSetColCount(DeviceProfile.ParamType.WORKSPACE, i2);
            z = true;
        }
        if (f != -1.0f && getScale(DeviceProfile.ParamType.WORKSPACE) == -1.0f) {
            internalSetScale(DeviceProfile.ParamType.WORKSPACE, f);
            z = true;
        }
        if (z) {
            SharedPreferences.Editor editorEdit = this.mSharedPreferences.edit();
            editorEdit.remove("row");
            editorEdit.remove("col");
            editorEdit.remove("scale");
            editorEdit.apply();
        }
    }

    private void notifyDataChanged() {
        this.mDataSetObservable.notifyChanged();
    }

    private void reload(Context context, DeviceProfile.ParamType paramType, CellParams cellParams) {
        LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().updateCellParams(context, paramType, cellParams);
        FolderIcon.onDeviceProfileChanged();
        LauncherAppState.getInstance().getModel().forceReload();
        notifyDataChanged();
    }

    public float getBackgroundAlpha(DeviceProfile.ParamType paramType) {
        return this.mSharedPreferences.getFloat(getPrefixForType(paramType) + "background_alpha", 1.0f);
    }

    public boolean getBackgroundBlur(DeviceProfile.ParamType paramType) {
        return this.mSharedPreferences.getBoolean(getPrefixForType(paramType) + "background_blur", false);
    }

    public int getColCount(DeviceProfile.ParamType paramType) {
        return this.mSharedPreferences.getInt(getPrefixForType(paramType) + "col", -1);
    }

    public boolean getContainerEnabled(DeviceProfile.ParamType paramType) {
        boolean z = paramType != DeviceProfile.ParamType.HOTSEAT /* REWORK: was a compiled switch-map; HOTSEAT maps to 1 */;
        return this.mSharedPreferences.getBoolean(getPrefixForType(paramType) + "container_enabled", z);
    }

    public boolean getInheritDesktop(DeviceProfile.ParamType paramType) {
        return this.mSharedPreferences.getBoolean(getPrefixForType(paramType) + "inherit_desktop", false);
    }

    public CellParams getParamsForType(DeviceProfile.ParamType paramType) {
        CellParams cellParams = new CellParams();
        cellParams.numColumns = getColCount(paramType);
        cellParams.numRows = getRowCount(paramType);
        cellParams.iconScale = getScale(paramType);
        cellParams.showLabels = getShowLabel(paramType);
        cellParams.enabled = getContainerEnabled(paramType);
        cellParams.backgroundAlpha = getBackgroundAlpha(paramType);
        cellParams.backgroundBlur = getBackgroundBlur(paramType);
        cellParams.inheritWorkspace = getInheritDesktop(paramType);
        return cellParams;
    }

    public int getRowCount(DeviceProfile.ParamType paramType) {
        return this.mSharedPreferences.getInt(getPrefixForType(paramType) + "row", -1);
    }

    public float getScale(DeviceProfile.ParamType paramType) {
        return this.mSharedPreferences.getFloat(getPrefixForType(paramType) + "scale", 1.0f);
    }

    public boolean getShowLabel(DeviceProfile.ParamType paramType) {
        boolean z = paramType != DeviceProfile.ParamType.HOTSEAT /* REWORK: was a compiled switch-map; HOTSEAT maps to 1 */;
        return this.mSharedPreferences.getBoolean(getPrefixForType(paramType) + "show_label", z);
    }

    public Map<DeviceProfile.ParamType, CellParams> getStoredCellParams() {
        HashMap map = new HashMap();
        map.put(DeviceProfile.ParamType.WORKSPACE, getParamsForType(DeviceProfile.ParamType.WORKSPACE));
        map.put(DeviceProfile.ParamType.HOTSEAT, getParamsForType(DeviceProfile.ParamType.HOTSEAT));
        map.put(DeviceProfile.ParamType.ALLAPPS, getParamsForType(DeviceProfile.ParamType.ALLAPPS));
        map.put(DeviceProfile.ParamType.FOLDER, getParamsForType(DeviceProfile.ParamType.FOLDER));
        return map;
    }

    public void refreshDeviceProfile(Context context) {
        DeviceProfile.ParamType paramType = DeviceProfile.ParamType.WORKSPACE;
        LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().updateCellParams(context, paramType, getParamsForType(paramType));
        DeviceProfile.ParamType paramType2 = DeviceProfile.ParamType.HOTSEAT;
        LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().updateCellParams(context, paramType2, getParamsForType(paramType2));
        FolderIcon.onDeviceProfileChanged();
        LauncherAppState.getInstance().getModel().forceReload();
        notifyDataChanged();
    }

    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        try {
            this.mDataSetObservable.registerObserver(dataSetObserver);
        } catch (IllegalStateException unused) {
        }
    }

    public void setLayout(Context context, DeviceProfile.ParamType paramType, CellParams cellParams) {
        boolean z;
        if (getRowCount(paramType) == cellParams.numRows || cellParams.numRows <= 0.0f) {
            z = false;
        } else {
            internalSetRowCount(paramType, Math.round(cellParams.numRows));
            z = true;
        }
        if (getColCount(paramType) != cellParams.numColumns && cellParams.numColumns > 0.0f) {
            internalSetColCount(paramType, Math.round(cellParams.numColumns));
            z = true;
        }
        if (getScale(paramType) != cellParams.iconScale && cellParams.iconScale > 0.0f) {
            internalSetScale(paramType, cellParams.iconScale);
            z = true;
        }
        if (getShowLabel(paramType) != cellParams.showLabels) {
            internalSetShowLabel(paramType, cellParams.showLabels);
            z = true;
        }
        if (getContainerEnabled(paramType) != cellParams.enabled) {
            internalSetContainerEnabled(paramType, cellParams.enabled);
            z = true;
        }
        if (getBackgroundAlpha(paramType) != cellParams.backgroundAlpha) {
            internalSetBackgroundAlpha(paramType, cellParams.backgroundAlpha);
            z = true;
        }
        if (getBackgroundBlur(paramType) != cellParams.backgroundBlur) {
            internalSetBackgroundBlur(paramType, cellParams.backgroundBlur);
            z = true;
        }
        if (getInheritDesktop(paramType) != cellParams.inheritWorkspace) {
            internalSetInheritDesktop(paramType, cellParams.inheritWorkspace);
            z = true;
        }
        if (z) {
            reload(context, paramType, cellParams);
        }
    }

    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        try {
            this.mDataSetObservable.unregisterObserver(dataSetObserver);
        } catch (IllegalStateException unused) {
        }
    }

    public void upgradeIfNeeded() {
        UpgradeUtil.processUpgrade(this.mSharedPreferences, this.mUpgradeComponent, 1);
    }
}
