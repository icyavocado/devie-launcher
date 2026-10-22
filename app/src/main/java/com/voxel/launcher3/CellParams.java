package com.voxel.launcher3;

/* JADX INFO: loaded from: classes.dex */
public class CellParams {
    int cellContentHeightPx;
    int cellContentWidthPx;
    int cellHeightPx;
    int cellWidthPx;
    int iconDrawablePaddingPx;
    int iconSizePx;
    int iconTextSizePx;
    public float numColumns;
    public float numRows;
    public boolean showLabels;
    public boolean enabled = true;
    public float iconScale = 1.0f;
    public float backgroundAlpha = 1.0f;
    public boolean backgroundBlur = false;
    public boolean inheritWorkspace = false;

    public int getCellContentHeightPx() {
        return this.cellContentHeightPx;
    }

    public int getCellContentWidthPx() {
        return this.cellContentWidthPx;
    }

    public int getCellHeightPx() {
        return this.cellHeightPx;
    }

    public int getCellWidthPx() {
        return this.cellWidthPx;
    }

    public int getIconDrawablePaddingPx() {
        return this.iconDrawablePaddingPx;
    }

    public int getIconSizePx() {
        return this.iconSizePx;
    }

    public int getIconTextSizePx() {
        return this.iconTextSizePx;
    }

    public String toString() {
        return "CellParams(numRows=" + this.numRows + ", numColumns=" + this.numColumns + ", enabled=" + this.enabled + ", showLabels=" + this.showLabels + ", iconScale=" + this.iconScale + ", backgroundAlpha=" + this.backgroundAlpha + ", backgroundBlur=" + this.backgroundBlur + ", inheritWorkspace=" + this.inheritWorkspace + ", iconSizePx=" + getIconSizePx() + ", iconTextSizePx=" + getIconTextSizePx() + ", iconDrawablePaddingPx=" + getIconDrawablePaddingPx() + ", cellWidthPx=" + getCellWidthPx() + ", cellHeightPx=" + getCellHeightPx() + ", cellContentWidthPx=" + getCellContentWidthPx() + ", cellContentHeightPx=" + getCellContentHeightPx() + ")";
    }
}
