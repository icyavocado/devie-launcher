package com.voxel.simplesearchlauncher.importer;

import android.content.ContentValues;
import android.content.Context;
import android.util.Pair;
import com.voxel.launcher3.CellParams;
import com.voxel.launcher3.DeviceProfile;
import com.voxel.launcher3.FolderInfo;
import com.voxel.launcher3.ItemInfo;
import com.voxel.launcher3.LauncherModel;
import com.voxel.launcher3.LauncherSettings;
import com.voxel.launcher3.ShortcutInfo;
import com.voxel.simplesearchlauncher.settings.LayoutSettingsManager;

/* REWORK: per-launcher settings scraping (Nova, TouchWiz, LG, ...) removed. This helper now only
 * writes workspace screens/items + grid layout to the launcher DB — used by onboarding to place
 * default shortcuts. */
/* JADX INFO: loaded from: classes2.dex */
public class LauncherImportHelper {
    public static void importItems(Context context, ImportData importData, LayoutSettingsManager layoutSettingsManager) {
        for (Pair<Integer, Integer> pair : importData.screens) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("_id", (Integer) pair.first);
            contentValues.put("screenRank", (Integer) pair.second);
            context.getContentResolver().insert(LauncherSettings.WorkspaceScreens.CONTENT_URI, contentValues);
        }
        for (ItemInfo itemInfo : importData.itemInfos) {
            LauncherModel.addItemToDatabase(context, itemInfo, itemInfo.container, itemInfo.screenId, itemInfo.cellX, itemInfo.cellY, false);
            if (itemInfo instanceof FolderInfo) {
                FolderInfo folderInfo = (FolderInfo) itemInfo;
                for (ShortcutInfo shortcutInfo : folderInfo.contents) {
                    shortcutInfo.container = folderInfo.id;
                    shortcutInfo.screenId = 0L;
                    LauncherModel.addItemToDatabase(context, shortcutInfo, folderInfo.id, 0L, shortcutInfo.cellX, shortcutInfo.cellY, false);
                }
            }
        }
        CellParams cellParams = new CellParams();
        cellParams.numRows = importData.rows;
        cellParams.numColumns = importData.cols;
        cellParams.iconScale = importData.iconScale;
        cellParams.enabled = true;
        cellParams.showLabels = true;
        layoutSettingsManager.setLayout(context, DeviceProfile.ParamType.WORKSPACE, cellParams);
    }
}
