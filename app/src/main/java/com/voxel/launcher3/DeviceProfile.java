package com.voxel.launcher3;

import android.appwidget.AppWidgetHostView;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.voxel.simplesearchlauncher.utils.AlphaUtil;
import com.voxel.simplesearchlauncher.view.HotseatImageView;
import com.voxel.simplesearchlauncher.view.PullDownRecyclerView;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class DeviceProfile {
    private static float MAX_ICON_TEXT_SCALE = 1.5f;
    private static float MIN_ICON_TEXT_SCALE = 0.75f;
    public int allAppsButtonVisualSize;
    public int allAppsCellPaddingPx;
    public int availableHeightPx;
    public int availableWidthPx;
    private Map<ParamType, CellParams> cellParamsMap;
    public int defaultLayoutId;
    public int defaultNoAllAppsLayoutId;
    public int defaultPageSpacingPx;
    public Rect defaultWidgetPadding;
    public int desiredWorkspaceLeftRightMarginPx;
    float dragViewScale;
    public int edgeMarginPx;
    public int folderPaddingPx;
    public int heightPx;
    public int hotseatAllAppsRank;
    public int hotseatBarHeightPx;
    private float hotseatIconSize;
    private int iconDrawablePaddingOriginalPx;
    public float iconSize;
    private float iconTextSize;
    public boolean isLandscape;
    public boolean isLargeTablet;
    public boolean isLayoutRtl;
    public boolean isTablet;
    private ArrayList<DeviceProfileCallbacks> mCallbacks = new ArrayList<>();
    public float minHeightDps;
    public float minWidthDps;
    public String name;
    private int navBarHeightPx;
    public float numBaseColumns;
    public float numBaseHotseatIcons;
    public float numBaseRows;
    public int overviewModeBarItemWidthPx;
    public int overviewModeBarSpacerWidthPx;
    public float overviewModeIconZoneRatio;
    public int overviewModeMaxIconZoneHeightPx;
    public int overviewModeMinIconZoneHeightPx;
    public float overviewModeScaleFactor;
    public int pageIndicatorHeightPx;
    public boolean searchBarEnabled;
    public int searchBarHeightPx;
    public int searchBarSpaceHeightPx;
    public int searchBarSpaceMaxWidthPx;
    public int searchBarSpaceWidthPx;
    public int springySearchBarMaxHeightDistancePx;
    public boolean transposeLayoutWithOrientation;
    public int unscaledIconSizePx;
    public int widthPx;

    public interface DeviceProfileCallbacks {
        void onAvailableSizeChanged(DeviceProfile deviceProfile);
    }

    public enum ParamType {
        WORKSPACE,
        HOTSEAT,
        FOLDER,
        ALLAPPS
    }

    DeviceProfile() {
    }

    public DeviceProfile(Context context, ArrayList<DeviceProfile> arrayList, float f, float f2, int i, int i2, int i3, int i4, Map<ParamType, CellParams> map, Resources resources, boolean z) {
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        ArrayList<DeviceProfileQuery> arrayList2 = new ArrayList<>();
        this.transposeLayoutWithOrientation = resources.getBoolean(R.bool.hotseat_transpose_layout_with_orientation);
        this.minWidthDps = f;
        this.minHeightDps = f2;
        this.searchBarEnabled = z;
        this.defaultWidgetPadding = AppWidgetHostView.getDefaultPaddingForWidget(context, new ComponentName(context.getPackageName(), getClass().getName()), null);
        this.edgeMarginPx = resources.getDimensionPixelSize(R.dimen.dynamic_grid_edge_margin);
        this.desiredWorkspaceLeftRightMarginPx = this.edgeMarginPx * 2;
        this.pageIndicatorHeightPx = resources.getDimensionPixelSize(R.dimen.dynamic_grid_page_indicator_height) + this.edgeMarginPx;
        this.defaultPageSpacingPx = resources.getDimensionPixelSize(R.dimen.dynamic_grid_workspace_page_spacing);
        this.allAppsCellPaddingPx = resources.getDimensionPixelSize(R.dimen.dynamic_grid_all_apps_cell_padding);
        this.overviewModeMinIconZoneHeightPx = resources.getDimensionPixelSize(R.dimen.dynamic_grid_overview_min_icon_zone_height);
        this.overviewModeMaxIconZoneHeightPx = resources.getDimensionPixelSize(R.dimen.dynamic_grid_overview_max_icon_zone_height);
        this.overviewModeBarItemWidthPx = resources.getDimensionPixelSize(R.dimen.dynamic_grid_overview_bar_item_width);
        this.overviewModeBarSpacerWidthPx = resources.getDimensionPixelSize(R.dimen.dynamic_grid_overview_bar_spacer_width);
        this.overviewModeIconZoneRatio = resources.getInteger(R.integer.config_dynamic_grid_overview_icon_zone_percentage) / 100.0f;
        this.overviewModeScaleFactor = resources.getInteger(R.integer.config_dynamic_grid_overview_scale_percentage) / 100.0f;
        this.folderPaddingPx = resources.getDimensionPixelSize(R.dimen.folder_padding);
        this.searchBarSpaceMaxWidthPx = resources.getDimensionPixelSize(R.dimen.dynamic_grid_search_bar_max_width);
        this.searchBarHeightPx = resources.getDimensionPixelSize(R.dimen.dynamic_grid_search_bar_height);
        this.springySearchBarMaxHeightDistancePx = resources.getDimensionPixelSize(R.dimen.dynamic_grid_springy_search_bar_max_height_distance);
        Iterator<DeviceProfile> it = arrayList.iterator();
        while (it.hasNext()) {
            arrayList2.add(new DeviceProfileQuery(it.next(), 0.0f));
        }
        DeviceProfile deviceProfileFindClosestDeviceProfile = findClosestDeviceProfile(f, f2, arrayList2);
        arrayList2.clear();
        for (DeviceProfile deviceProfile : arrayList) {
            arrayList2.add(new DeviceProfileQuery(deviceProfile, deviceProfile.iconSize));
        }
        this.iconSize = invDistWeightedInterpolate(f, f2, arrayList2);
        this.unscaledIconSizePx = DynamicGrid.pxFromDp(this.iconSize, displayMetrics);
        float dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.dragViewScale);
        int i5 = this.unscaledIconSizePx;
        this.dragViewScale = (i5 + dimensionPixelSize) / i5;
        arrayList2.clear();
        for (DeviceProfile deviceProfile2 : arrayList) {
            arrayList2.add(new DeviceProfileQuery(deviceProfile2, deviceProfile2.iconTextSize));
        }
        this.iconTextSize = invDistWeightedInterpolate(f, f2, arrayList2);
        this.iconDrawablePaddingOriginalPx = resources.getDimensionPixelSize(R.dimen.dynamic_grid_icon_drawable_padding);
        arrayList2.clear();
        for (DeviceProfile deviceProfile3 : arrayList) {
            arrayList2.add(new DeviceProfileQuery(deviceProfile3, deviceProfile3.hotseatIconSize));
        }
        this.hotseatIconSize = invDistWeightedInterpolate(f, f2, arrayList2);
        this.cellParamsMap = new HashMap();
        for (ParamType paramType : ParamType.values()) {
            this.cellParamsMap.put(paramType, new CellParams());
        }
        this.numBaseRows = deviceProfileFindClosestDeviceProfile.numBaseRows;
        this.numBaseColumns = deviceProfileFindClosestDeviceProfile.numBaseColumns;
        this.numBaseHotseatIcons = deviceProfileFindClosestDeviceProfile.numBaseHotseatIcons;
        this.hotseatAllAppsRank = (int) (this.numBaseHotseatIcons / 2.0f);
        CellParams cellParamsForType = getCellParamsForType(ParamType.WORKSPACE);
        cellParamsForType.numRows = this.numBaseRows;
        cellParamsForType.numColumns = this.numBaseColumns;
        cellParamsForType.enabled = true;
        cellParamsForType.showLabels = true;
        cellParamsForType.iconScale = 1.0f;
        CellParams cellParamsForType2 = getCellParamsForType(ParamType.HOTSEAT);
        cellParamsForType2.numRows = 1.0f;
        cellParamsForType2.numColumns = this.numBaseHotseatIcons;
        cellParamsForType2.enabled = false;
        cellParamsForType2.showLabels = false;
        cellParamsForType2.backgroundBlur = false;
        cellParamsForType2.iconScale = 1.0f;
        CellParams cellParamsForType3 = getCellParamsForType(ParamType.FOLDER);
        int iRound = Math.round(Math.min(cellParamsForType.numRows, cellParamsForType.numColumns) - 1.0f);
        float f3 = iRound == 3 ? 4 : iRound;
        cellParamsForType3.numRows = f3;
        cellParamsForType3.numColumns = f3;
        cellParamsForType3.enabled = true;
        cellParamsForType3.showLabels = true;
        cellParamsForType3.iconScale = 1.0f;
        CellParams cellParamsForType4 = getCellParamsForType(ParamType.ALLAPPS);
        cellParamsForType4.numColumns = this.numBaseColumns;
        cellParamsForType4.iconScale = 0.9f;
        cellParamsForType4.iconSizePx = Math.round(this.unscaledIconSizePx * cellParamsForType4.iconScale);
        cellParamsForType4.iconTextSizePx = DynamicGrid.pxFromDp(this.iconTextSize, displayMetrics);
        cellParamsForType4.showLabels = true;
        if (map != null) {
            for (Map.Entry<ParamType, CellParams> entry : map.entrySet()) {
                updateCellParamsImpl(entry.getKey(), entry.getValue());
            }
        }
        this.defaultLayoutId = deviceProfileFindClosestDeviceProfile.defaultLayoutId;
        this.defaultNoAllAppsLayoutId = deviceProfileFindClosestDeviceProfile.defaultNoAllAppsLayoutId;
        updateFromConfiguration(context, resources, i, i2, i3, i4);
        updateAvailableDimensions(context);
        computeAllAppsButtonSize(context);
    }

    DeviceProfile(String str, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int i, int i2) {
        if (!LauncherAppState.isDisableAllApps() && f7 % 2.0f == 0.0f) {
            throw new RuntimeException("All Device Profiles must have an odd number of hotseat spaces");
        }
        this.name = str;
        this.minWidthDps = f;
        this.minHeightDps = f2;
        this.numBaseRows = f3;
        this.numBaseColumns = f4;
        this.iconSize = f5;
        this.iconTextSize = f6;
        this.numBaseHotseatIcons = f7;
        this.hotseatIconSize = f8;
        /* REWORK: jadx dropped the two trailing int params (the workspace xml ids the
         * grid table passes down); restore the param wiring. */
        this.defaultLayoutId = i;
        this.defaultNoAllAppsLayoutId = i2;
    }

    private void computeAllAppsButtonSize(Context context) {
        float integer = context.getResources().getInteger(R.integer.config_allAppsButtonPaddingPercent) / 100.0f;
        LauncherAppState.getInstance();
        this.allAppsButtonVisualSize = (int) (getCellParamsForType(ParamType.HOTSEAT).iconSizePx * (1.0f - integer));
    }

    private float dist(PointF pointF, PointF pointF2) {
        return (float) Math.sqrt(((pointF2.x - pointF.x) * (pointF2.x - pointF.x)) + ((pointF2.y - pointF.y) * (pointF2.y - pointF.y)));
    }

    private DeviceProfile findClosestDeviceProfile(float f, float f2, ArrayList<DeviceProfileQuery> arrayList) {
        return findClosestDeviceProfiles(f, f2, arrayList).get(0).profile;
    }

    private ArrayList<DeviceProfileQuery> findClosestDeviceProfiles(float f, float f2, ArrayList<DeviceProfileQuery> arrayList) {
        final PointF pointF = new PointF(f, f2);
        Collections.sort(arrayList, new Comparator() { // from class: com.voxel.launcher3.-$$Lambda$DeviceProfile$CvB_cznEVbDz_JtRDYDQg4IhfeU
            @Override // java.util.Comparator
            public final int compare(Object obj, Object obj2) {
                return DeviceProfile.this.lambda$findClosestDeviceProfiles$0( pointF, (DeviceProfileQuery) obj, (DeviceProfileQuery) obj2);
            }
        });
        return arrayList;
    }

    private void inheritWorkspaceParamsIfNeeded(ParamType paramType) {
        CellParams cellParamsForType = getCellParamsForType(ParamType.WORKSPACE);
        CellParams cellParamsForType2 = getCellParamsForType(paramType);
        if (cellParamsForType2.inheritWorkspace) {
            switch (paramType) {
                case HOTSEAT:
                    cellParamsForType2.numColumns = cellParamsForType.numColumns;
                    cellParamsForType2.iconScale = cellParamsForType.iconScale;
                    break;
                case FOLDER:
                    float fRound = Math.round(Math.min(cellParamsForType.numRows, cellParamsForType.numColumns));
                    cellParamsForType2.numRows = fRound;
                    cellParamsForType2.numColumns = fRound;
                    cellParamsForType2.iconScale = cellParamsForType.iconScale;
                    break;
                case ALLAPPS:
                    cellParamsForType2.numColumns = cellParamsForType.numColumns;
                    cellParamsForType2.iconScale = cellParamsForType.iconScale;
                    break;
            }
        }
    }

    private float invDistWeightedInterpolate(float f, float f2, ArrayList<DeviceProfileQuery> arrayList) {
        PointF pointF = new PointF(f, f2);
        ArrayList<DeviceProfileQuery> arrayListFindClosestDeviceProfiles = findClosestDeviceProfiles(f, f2, arrayList);
        float fWeight = 0.0f;
        float f3 = 0.0f;
        for (int i = 0; i < arrayListFindClosestDeviceProfiles.size(); i++) {
            DeviceProfileQuery deviceProfileQuery = arrayListFindClosestDeviceProfiles.get(i);
            if (i < 3.0f) {
                float fWeight2 = weight(pointF, deviceProfileQuery.dimens, 5.0f);
                if (fWeight2 == Float.POSITIVE_INFINITY) {
                    return deviceProfileQuery.value;
                }
                f3 += fWeight2;
            }
        }
        for (int i2 = 0; i2 < arrayListFindClosestDeviceProfiles.size(); i2++) {
            DeviceProfileQuery deviceProfileQuery2 = arrayListFindClosestDeviceProfiles.get(i2);
            if (i2 < 3.0f) {
                fWeight += (weight(pointF, deviceProfileQuery2.dimens, 5.0f) * deviceProfileQuery2.value) / f3;
            }
        }
        return fWeight;
    }

    public int lambda$findClosestDeviceProfiles$0(PointF pointF, DeviceProfileQuery deviceProfileQuery, DeviceProfileQuery deviceProfileQuery2) {
        return (int) (this.dist(pointF, deviceProfileQuery.dimens) - this.dist(pointF, deviceProfileQuery2.dimens));
    }

    private void updateAllAppsSize(Resources resources, DisplayMetrics displayMetrics) {
        CellParams cellParamsForType = getCellParamsForType(ParamType.ALLAPPS);
        float fMax = Math.max(MIN_ICON_TEXT_SCALE, Math.min(cellParamsForType.iconScale, MAX_ICON_TEXT_SCALE));
        cellParamsForType.iconSizePx = DynamicGrid.pxFromDp(this.iconSize * cellParamsForType.iconScale, displayMetrics);
        cellParamsForType.iconTextSizePx = DynamicGrid.pxFromSp(this.iconTextSize * fMax, displayMetrics);
        cellParamsForType.iconDrawablePaddingPx = Math.round(this.iconDrawablePaddingOriginalPx);
        cellParamsForType.cellContentWidthPx = cellParamsForType.iconSizePx;
        cellParamsForType.cellContentHeightPx = cellParamsForType.iconSizePx + calculateTextContentHeight(cellParamsForType);
        cellParamsForType.cellWidthPx = cellParamsForType.cellContentWidthPx;
        cellParamsForType.cellHeightPx = cellParamsForType.cellContentHeightPx;
    }

    private void updateCellParamsImpl(ParamType paramType, CellParams cellParams) {
        CellParams cellParamsForType = getCellParamsForType(paramType);
        cellParamsForType.iconScale = cellParams.iconScale;
        cellParamsForType.showLabels = cellParams.showLabels;
        cellParamsForType.enabled = cellParams.enabled;
        cellParamsForType.backgroundAlpha = cellParams.backgroundAlpha;
        cellParamsForType.backgroundBlur = cellParams.backgroundBlur;
        cellParamsForType.inheritWorkspace = cellParams.inheritWorkspace;
        switch (paramType) {
            case WORKSPACE:
                cellParamsForType.numRows = cellParams.numRows != -1.0f ? cellParams.numRows : this.numBaseRows;
                cellParamsForType.numColumns = cellParams.numColumns != -1.0f ? cellParams.numColumns : this.numBaseColumns;
                for (ParamType paramType2 : ParamType.values()) {
                    inheritWorkspaceParamsIfNeeded(paramType2);
                }
                break;
            case HOTSEAT:
                cellParamsForType.numRows = cellParams.numRows != -1.0f ? cellParams.numRows : 1.0f;
                cellParamsForType.numColumns = cellParams.numColumns != -1.0f ? cellParams.numColumns : this.numBaseHotseatIcons;
                break;
            default:
                cellParamsForType.numRows = cellParams.numRows != -1.0f ? cellParams.numRows : this.numBaseRows;
                cellParamsForType.numColumns = cellParams.numColumns != -1.0f ? cellParams.numColumns : this.numBaseColumns;
                break;
        }
        inheritWorkspaceParamsIfNeeded(paramType);
    }

    private void updateFolderSize(DisplayMetrics displayMetrics) {
        CellParams cellParamsForType = getCellParamsForType(ParamType.FOLDER);
        float fMax = Math.max(MIN_ICON_TEXT_SCALE, Math.min(cellParamsForType.iconScale, MAX_ICON_TEXT_SCALE));
        cellParamsForType.iconSizePx = DynamicGrid.pxFromDp(this.iconSize * cellParamsForType.iconScale, displayMetrics);
        cellParamsForType.iconTextSizePx = DynamicGrid.pxFromSp(this.iconTextSize * fMax, displayMetrics);
        cellParamsForType.iconDrawablePaddingPx = Math.round(this.iconDrawablePaddingOriginalPx);
        cellParamsForType.cellContentWidthPx = cellParamsForType.iconSizePx;
        cellParamsForType.cellContentHeightPx = cellParamsForType.iconSizePx + calculateTextContentHeight(cellParamsForType);
        int i = this.edgeMarginPx;
        int iMax = (int) Math.max(0.0f, Math.min(i * 4, (((this.availableWidthPx - (cellParamsForType.numColumns * cellParamsForType.cellContentWidthPx)) - (this.folderPaddingPx * 2)) - ((i * 2) * 2.0f)) / cellParamsForType.numColumns));
        cellParamsForType.cellWidthPx = cellParamsForType.cellContentWidthPx + iMax;
        cellParamsForType.cellHeightPx = cellParamsForType.cellContentHeightPx + iMax;
    }

    private void updateHotseatSize(DisplayMetrics displayMetrics) {
        CellParams cellParamsForType = getCellParamsForType(ParamType.HOTSEAT);
        float fMax = Math.max(MIN_ICON_TEXT_SCALE, Math.min(cellParamsForType.iconScale, MAX_ICON_TEXT_SCALE));
        cellParamsForType.iconSizePx = DynamicGrid.pxFromDp(this.hotseatIconSize * cellParamsForType.iconScale, displayMetrics);
        cellParamsForType.iconTextSizePx = DynamicGrid.pxFromSp(this.iconTextSize * fMax, displayMetrics);
        cellParamsForType.iconDrawablePaddingPx = Math.round(this.iconDrawablePaddingOriginalPx);
        cellParamsForType.cellContentWidthPx = cellParamsForType.iconSizePx;
        cellParamsForType.cellContentHeightPx = cellParamsForType.iconSizePx + calculateTextContentHeight(cellParamsForType);
        cellParamsForType.cellWidthPx = cellParamsForType.cellContentWidthPx;
        cellParamsForType.cellHeightPx = cellParamsForType.cellContentHeightPx;
        if (cellParamsForType.enabled) {
            this.hotseatBarHeightPx = cellParamsForType.cellHeightPx + (this.edgeMarginPx * 4);
        } else {
            this.hotseatBarHeightPx = 0;
        }
    }

    private void updateWorkspaceSize(DisplayMetrics displayMetrics) {
        CellParams cellParamsForType = getCellParamsForType(ParamType.WORKSPACE);
        float fMax = Math.max(MIN_ICON_TEXT_SCALE, Math.min(cellParamsForType.iconScale, MAX_ICON_TEXT_SCALE));
        cellParamsForType.iconSizePx = DynamicGrid.pxFromDp(this.iconSize * cellParamsForType.iconScale, displayMetrics);
        cellParamsForType.iconTextSizePx = DynamicGrid.pxFromSp(this.iconTextSize * fMax, displayMetrics);
        cellParamsForType.iconDrawablePaddingPx = Math.round(this.iconDrawablePaddingOriginalPx);
        cellParamsForType.cellContentWidthPx = cellParamsForType.iconSizePx;
        cellParamsForType.cellContentHeightPx = cellParamsForType.iconSizePx + calculateTextContentHeight(cellParamsForType);
        cellParamsForType.cellWidthPx = cellParamsForType.cellContentWidthPx;
        cellParamsForType.cellHeightPx = cellParamsForType.cellContentHeightPx;
    }

    private float weight(PointF pointF, PointF pointF2, float f) {
        float fDist = dist(pointF, pointF2);
        if (fDist == 0.0f) {
            return Float.POSITIVE_INFINITY;
        }
        return (float) (1.0d / Math.pow(fDist, f));
    }

    void addCallback(DeviceProfileCallbacks deviceProfileCallbacks) {
        this.mCallbacks.add(deviceProfileCallbacks);
        deviceProfileCallbacks.onAvailableSizeChanged(this);
    }

    int calculateCellHeight(int i, int i2) {
        return i / i2;
    }

    int calculateCellWidth(int i, int i2) {
        return i / i2;
    }

    int calculateOverviewModeWidth(int i) {
        return (this.overviewModeBarItemWidthPx * i) + ((i - 1) * this.overviewModeBarSpacerWidthPx);
    }

    public int calculateTextContentHeight(CellParams cellParams) {
        if (!cellParams.showLabels) {
            return 0;
        }
        Paint paint = new Paint();
        paint.setTextSize(cellParams.iconTextSizePx);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return cellParams.iconDrawablePaddingPx + ((int) Math.ceil(fontMetrics.bottom - fontMetrics.top));
    }

    public CellParams getCellParamsForType(ParamType paramType) {
        return this.cellParamsMap.get(paramType);
    }

    public Map<ParamType, CellParams> getCellParamsMap() {
        return this.cellParamsMap;
    }

    public Rect getHotseatPadding() {
        Rect rect = new Rect();
        if (isVerticalBarLayout()) {
            int i = this.edgeMarginPx;
            rect.set(0, i * 2, 0, i * 2);
        } else if (isTablet()) {
            Rect workspacePadding = getWorkspacePadding();
            rect.set(this.edgeMarginPx + workspacePadding.left, 0, this.edgeMarginPx + workspacePadding.right, this.edgeMarginPx * 2);
        } else {
            Rect workspacePadding2 = getWorkspacePadding();
            rect.set(workspacePadding2.left, 0, workspacePadding2.right, 0);
        }
        return rect;
    }

    Rect getHotseatRect() {
        return isVerticalBarLayout() ? new Rect(this.availableWidthPx - this.hotseatBarHeightPx, 0, Integer.MAX_VALUE, this.availableHeightPx) : new Rect(0, this.availableHeightPx - this.hotseatBarHeightPx, this.availableWidthPx, Integer.MAX_VALUE);
    }

    public float getMaxIconScaleForType(Context context, ParamType paramType) {
        return paramType != ParamType.HOTSEAT /* REWORK: was a compiled switch-map; HOTSEAT maps to 1 */ ? getMaxIconScaleVerticallyUnconstrainedForType(context, paramType) : getMaxWorkspaceIconScale(context);
    }

    public float getMaxIconScaleVerticallyUnconstrainedForType(Context context, ParamType paramType) {
        Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        CellParams cellParamsForType = getCellParamsForType(paramType);
        Point point = new Point();
        Point point2 = new Point();
        Point point3 = new Point();
        defaultDisplay.getSize(point);
        defaultDisplay.getCurrentSizeRange(point2, point3);
        return (((int) ((this.availableWidthPx - (this.edgeMarginPx * 4)) / cellParamsForType.numColumns)) - cellParamsForType.iconDrawablePaddingPx) / DynamicGrid.pxFromDp(this.iconSize, displayMetrics);
    }

    public float getMaxWorkspaceIconScale(Context context) {
        Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        CellParams cellParamsForType = getCellParamsForType(ParamType.WORKSPACE);
        Point point = new Point();
        Point point2 = new Point();
        Point point3 = new Point();
        defaultDisplay.getSize(point);
        defaultDisplay.getCurrentSizeRange(point2, point3);
        int i = configuration.orientation == 2 ? point2.y : point3.y;
        int i2 = cellParamsForType.iconDrawablePaddingPx;
        Rect workspacePadding = getWorkspacePadding();
        int i3 = ((int) (((i - workspacePadding.top) - workspacePadding.bottom) / cellParamsForType.numRows)) - i2;
        int i4 = ((int) (((this.availableWidthPx - workspacePadding.left) - workspacePadding.right) / cellParamsForType.numColumns)) - i2;
        Paint paint = new Paint();
        paint.setTextSize(DynamicGrid.pxFromSp(this.iconTextSize, displayMetrics));
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        int iPxFromDp = DynamicGrid.pxFromDp(this.iconSize, displayMetrics) + ((int) Math.ceil(fontMetrics.bottom - fontMetrics.top));
        float fPxFromDp = i4 / DynamicGrid.pxFromDp(this.iconSize, displayMetrics);
        float fMin = Math.min(i3 / iPxFromDp, fPxFromDp);
        if (fMin >= MIN_ICON_TEXT_SCALE) {
            return fMin;
        }
        paint.setTextSize(DynamicGrid.pxFromSp(this.iconTextSize * 0.75f, displayMetrics));
        Paint.FontMetrics fontMetrics2 = paint.getFontMetrics();
        return Math.min((i3 - ((int) Math.ceil(fontMetrics2.bottom - fontMetrics2.top))) / DynamicGrid.pxFromDp(this.iconSize, displayMetrics), fPxFromDp);
    }

    Rect getOverviewModeButtonBarRect() {
        int iMin = Math.min(this.overviewModeMaxIconZoneHeightPx, Math.max(this.overviewModeMinIconZoneHeightPx, (int) (this.overviewModeIconZoneRatio * this.availableHeightPx)));
        int i = this.availableHeightPx;
        return new Rect(0, i - iMin, 0, i);
    }

    float getOverviewModeScale() {
        Rect workspacePadding = getWorkspacePadding();
        Rect overviewModeButtonBarRect = getOverviewModeButtonBarRect();
        int i = (this.availableHeightPx - workspacePadding.top) - workspacePadding.bottom;
        return (this.overviewModeScaleFactor * (i - overviewModeButtonBarRect.height())) / i;
    }

    Rect getSearchBarBounds(int i) {
        if (!this.searchBarEnabled) {
            return new Rect(0, 0, 0, 0);
        }
        Rect rect = new Rect();
        if (i == 0 && this.transposeLayoutWithOrientation) {
            if (this.isLayoutRtl) {
                int i2 = this.availableWidthPx;
                int i3 = i2 - this.searchBarSpaceHeightPx;
                int i4 = this.edgeMarginPx;
                rect.set(i3, i4, i2, this.availableHeightPx - i4);
            } else {
                int i5 = this.edgeMarginPx;
                rect.set(0, i5, this.searchBarSpaceHeightPx, this.availableHeightPx - i5);
            }
        } else if (isTablet()) {
            int iMax = i == 0 ? Math.max(this.widthPx, this.heightPx) : Math.min(this.widthPx, this.heightPx);
            CellParams cellParamsForType = getCellParamsForType(ParamType.WORKSPACE);
            int i6 = (int) (((iMax - (this.edgeMarginPx * 2)) - (cellParamsForType.numColumns * cellParamsForType.cellWidthPx)) / ((cellParamsForType.numColumns + 1.0f) * 2.0f));
            rect.set(this.edgeMarginPx + i6, getSearchBarTopOffset(), this.availableWidthPx - (this.edgeMarginPx + i6), this.searchBarSpaceHeightPx);
        } else {
            rect.set(this.edgeMarginPx * 2, getSearchBarTopOffset(), this.availableWidthPx - (this.edgeMarginPx * 2), this.searchBarSpaceHeightPx);
        }
        return rect;
    }

    public int getSearchBarTopOffset() {
        return (!isTablet() || isVerticalBarLayout()) ? this.edgeMarginPx * 1 : this.edgeMarginPx * 2;
    }

    int getVisibleChildCount(ViewGroup viewGroup) {
        int i = 0;
        for (int i2 = 0; i2 < viewGroup.getChildCount(); i2++) {
            if (viewGroup.getChildAt(i2).getVisibility() != 8) {
                i++;
            }
        }
        return i;
    }

    public Rect getWorkspacePadding() {
        return getWorkspacePadding(!this.isLandscape ? 1 : 0);
    }

    public Rect getWorkspacePadding(int i) {
        Rect searchBarBounds = getSearchBarBounds(i);
        Rect rect = new Rect();
        if (i == 0 && this.transposeLayoutWithOrientation) {
            if (this.isLayoutRtl) {
                rect.set(this.hotseatBarHeightPx, this.edgeMarginPx, searchBarBounds.width(), this.edgeMarginPx);
            } else {
                int iWidth = searchBarBounds.width();
                int i2 = this.edgeMarginPx;
                rect.set(iWidth, i2, this.hotseatBarHeightPx, i2);
            }
        } else if (isTablet()) {
            CellParams cellParamsForType = getCellParamsForType(ParamType.WORKSPACE);
            float f = ((this.dragViewScale - 1.0f) / 2.0f) + 1.0f;
            int iMax = i == 0 ? Math.max(this.widthPx, this.heightPx) : Math.min(this.widthPx, this.heightPx);
            int iMax2 = i != 0 ? Math.max(this.widthPx, this.heightPx) : Math.min(this.widthPx, this.heightPx);
            int i3 = searchBarBounds.bottom;
            int i4 = this.hotseatBarHeightPx + this.pageIndicatorHeightPx;
            int iMax3 = Math.max(0, iMax - ((int) ((cellParamsForType.numColumns * cellParamsForType.cellWidthPx) + ((cellParamsForType.numColumns * f) * cellParamsForType.cellWidthPx)))) / 2;
            int iMax4 = Math.max(0, ((iMax2 - i3) - i4) - ((int) ((cellParamsForType.numRows * 2.0f) * cellParamsForType.cellHeightPx))) / 2;
            rect.set(iMax3, i3 + iMax4, iMax3, i4 + iMax4);
        } else {
            rect.set(this.desiredWorkspaceLeftRightMarginPx - this.defaultWidgetPadding.left, searchBarBounds.bottom, this.desiredWorkspaceLeftRightMarginPx - this.defaultWidgetPadding.right, this.hotseatBarHeightPx + this.pageIndicatorHeightPx);
        }
        return rect;
    }

    int getWorkspacePageSpacing(int i) {
        return ((i == 0 && this.transposeLayoutWithOrientation) || isLargeTablet()) ? this.defaultPageSpacingPx : Math.max(this.defaultPageSpacingPx, getWorkspacePadding().left * 2);
    }

    boolean isLargeTablet() {
        return this.isLargeTablet;
    }

    boolean isTablet() {
        return this.isTablet;
    }

    public boolean isVerticalBarLayout() {
        return this.isLandscape && this.transposeLayoutWithOrientation;
    }

    public void layout(Launcher launcher) {
        launcher.getResources();
        boolean zIsVerticalBarLayout = isVerticalBarLayout();
        SearchDropTargetBar searchBar = launcher.getSearchBar();
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) searchBar.getLayoutParams();
        if (zIsVerticalBarLayout) {
            layoutParams.gravity = 51;
            layoutParams.width = this.searchBarSpaceHeightPx;
            layoutParams.height = -2;
            searchBar.setPadding(0, 0, 0, this.edgeMarginPx * 2);
            ((LinearLayout) searchBar.findViewById(R.id.drag_target_bar)).setOrientation(1);
        } else {
            layoutParams.gravity = 49;
            layoutParams.width = this.searchBarSpaceWidthPx;
            layoutParams.height = this.searchBarSpaceHeightPx;
            searchBar.setPadding(this.edgeMarginPx * 2, getSearchBarTopOffset(), this.edgeMarginPx * 2, 0);
        }
        searchBar.setLayoutParams(layoutParams);
        View viewFindViewById = launcher.findViewById(R.id.workspace_delete_drop_target);
        if (viewFindViewById != null) {
            FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) viewFindViewById.getLayoutParams();
            layoutParams2.gravity = layoutParams.gravity;
            layoutParams2.width = -2;
            layoutParams2.height = layoutParams.height;
            viewFindViewById.setLayoutParams(layoutParams2);
            viewFindViewById.setPadding(searchBar.getPaddingLeft(), searchBar.getPaddingTop(), searchBar.getPaddingRight(), searchBar.getPaddingBottom());
        }
        PullDownRecyclerView pullDownRecyclerView = launcher.getPullDownRecyclerView();
        FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) pullDownRecyclerView.getLayoutParams();
        if (zIsVerticalBarLayout) {
            layoutParams3.gravity = 51;
            layoutParams3.width = -1;
            layoutParams3.height = -1;
            layoutParams3.setMargins(0, this.searchBarSpaceHeightPx, 0, 0);
        } else {
            layoutParams3.gravity = 51;
            layoutParams3.width = -1;
            layoutParams3.height = -1;
            layoutParams3.setMargins(0, this.searchBarSpaceHeightPx, 0, 0);
        }
        pullDownRecyclerView.setLayoutParams(layoutParams3);
        PagedView pagedView = (PagedView) launcher.findViewById(R.id.workspace);
        FrameLayout.LayoutParams layoutParams4 = (FrameLayout.LayoutParams) pagedView.getLayoutParams();
        layoutParams4.gravity = 17;
        int i = 1 ^ (this.isLandscape ? 1 : 0);
        Rect workspacePadding = getWorkspacePadding(i);
        pagedView.setLayoutParams(layoutParams4);
        pagedView.setPadding(workspacePadding.left, workspacePadding.top, workspacePadding.right, workspacePadding.bottom);
        pagedView.setPageSpacing(getWorkspacePageSpacing(i));
        View viewFindViewById2 = launcher.findViewById(R.id.hotseat);
        if (viewFindViewById2 != null) {
            FrameLayout.LayoutParams layoutParams5 = (FrameLayout.LayoutParams) viewFindViewById2.getLayoutParams();
            if (zIsVerticalBarLayout) {
                layoutParams5.gravity = 8388613;
                layoutParams5.width = this.hotseatBarHeightPx;
                layoutParams5.height = -1;
            } else if (isTablet()) {
                layoutParams5.gravity = 80;
                layoutParams5.width = -1;
                layoutParams5.height = this.hotseatBarHeightPx;
            } else {
                layoutParams5.gravity = 80;
                layoutParams5.width = -1;
                layoutParams5.height = this.hotseatBarHeightPx;
            }
            viewFindViewById2.setLayoutParams(layoutParams5);
            Rect hotseatPadding = getHotseatPadding();
            viewFindViewById2.setPadding(hotseatPadding.left, hotseatPadding.top, hotseatPadding.right, hotseatPadding.bottom);
            HotseatImageView hotseatImageView = (HotseatImageView) ((Hotseat) viewFindViewById2).getBackgroundView();
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) hotseatImageView.getLayoutParams();
            CellParams cellParamsForType = getCellParamsForType(ParamType.HOTSEAT);
            if (cellParamsForType.enabled) {
                marginLayoutParams.height = -1;
                int i2 = this.availableHeightPx;
                marginLayoutParams.topMargin = i2 - this.hotseatBarHeightPx;
                marginLayoutParams.bottomMargin = -i2;
                hotseatImageView.setLayoutParams(marginLayoutParams);
                Drawable whiteDrawable = hotseatImageView.getWhiteDrawable();
                if (whiteDrawable == null) {
                    whiteDrawable = hotseatImageView.getContext().getResources().getDrawable(R.drawable.hotseat_bg);
                }
                hotseatImageView.enableBlurredBackground(cellParamsForType.backgroundBlur);
                hotseatImageView.setDefaultAlpha(AlphaUtil.floatToIntAlpha(cellParamsForType.backgroundAlpha));
                hotseatImageView.setImageDrawables(whiteDrawable, null);
            } else {
                marginLayoutParams.height = 0;
                hotseatImageView.setLayoutParams(marginLayoutParams);
                hotseatImageView.setBackground(null);
            }
        }
        View viewFindViewById3 = launcher.findViewById(R.id.indicator_container);
        if (viewFindViewById3 != null) {
            if (zIsVerticalBarLayout) {
                viewFindViewById3.setVisibility(8);
            } else {
                FrameLayout.LayoutParams layoutParams6 = (FrameLayout.LayoutParams) viewFindViewById3.getLayoutParams();
                layoutParams6.gravity = 81;
                layoutParams6.height = this.pageIndicatorHeightPx;
                layoutParams6.bottomMargin = this.hotseatBarHeightPx;
                viewFindViewById3.setLayoutParams(layoutParams6);
            }
        }
        ViewGroup overviewPanel = launcher.getOverviewPanel();
        if (overviewPanel != null) {
            Rect overviewModeButtonBarRect = getOverviewModeButtonBarRect();
            FrameLayout.LayoutParams layoutParams7 = (FrameLayout.LayoutParams) overviewPanel.getLayoutParams();
            layoutParams7.gravity = 81;
            layoutParams7.width = Math.min(this.availableWidthPx, calculateOverviewModeWidth(getVisibleChildCount(overviewPanel)));
            layoutParams7.height = overviewModeButtonBarRect.height();
            overviewPanel.setLayoutParams(layoutParams7);
        }
    }

    boolean shouldFadeAdjacentWorkspaceScreens() {
        return isVerticalBarLayout() || isLargeTablet();
    }

    public void updateAvailableDimensions(Context context) {
        Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        Point point = new Point();
        Point point2 = new Point();
        Point point3 = new Point();
        defaultDisplay.getSize(point);
        defaultDisplay.getCurrentSizeRange(point2, point3);
        this.availableWidthPx = point.x;
        if (configuration.orientation == 2) {
            this.availableHeightPx = point2.y;
        } else {
            this.availableHeightPx = point3.y;
        }
        int identifier = resources.getIdentifier("navigation_bar_height", "dimen", "android" /* REWORK: was facebook SDK AbstractSpiCall constant */);
        if (identifier > 0) {
            this.navBarHeightPx = identifier > 0 ? resources.getDimensionPixelSize(identifier) : 0;
        }
        this.searchBarSpaceWidthPx = Math.min(this.searchBarSpaceMaxWidthPx, this.widthPx);
        this.searchBarSpaceHeightPx = this.searchBarHeightPx + this.springySearchBarMaxHeightDistancePx + getSearchBarTopOffset() + this.edgeMarginPx;
        updateWorkspaceSize(displayMetrics);
        updateHotseatSize(displayMetrics);
        updateFolderSize(displayMetrics);
        updateAllAppsSize(resources, displayMetrics);
        float maxWorkspaceIconScale = getMaxWorkspaceIconScale(context);
        if (maxWorkspaceIconScale < getCellParamsForType(ParamType.WORKSPACE).iconScale) {
            getCellParamsForType(ParamType.WORKSPACE).iconScale = maxWorkspaceIconScale;
            updateWorkspaceSize(displayMetrics);
        }
        Iterator<DeviceProfileCallbacks> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onAvailableSizeChanged(this);
        }
    }

    public void updateCellParams(Context context, ParamType paramType, CellParams cellParams) {
        updateCellParamsImpl(paramType, cellParams);
        updateAvailableDimensions(context);
    }

    void updateFromConfiguration(Context context, Resources resources, int i, int i2, int i3, int i4) {
        Configuration configuration = resources.getConfiguration();
        this.isLandscape = configuration.orientation == 2;
        this.isTablet = resources.getBoolean(R.bool.is_tablet);
        this.isLargeTablet = resources.getBoolean(R.bool.is_large_tablet);
        if (Build.VERSION.SDK_INT >= 17) {
            this.isLayoutRtl = configuration.getLayoutDirection() == 1;
        } else {
            this.isLayoutRtl = false;
        }
        this.widthPx = i;
        this.heightPx = i2;
        this.availableWidthPx = i3;
        this.availableHeightPx = i4;
        updateAvailableDimensions(context);
    }
}
