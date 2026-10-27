package com.voxel.simplesearchlauncher.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.voxel.launcher3.BubbleTextView;
import com.voxel.launcher3.CellLayout;
import com.voxel.launcher3.CellParams;
import com.voxel.launcher3.DeviceProfile;
import com.voxel.launcher3.FolderIcon;
import com.voxel.launcher3.FolderInfo;
import com.voxel.launcher3.IconCache;
import com.voxel.launcher3.ItemInfo;
import com.voxel.launcher3.Launcher;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.LauncherAppWidgetInfo;
import com.voxel.launcher3.ShortcutInfo;
import com.voxel.launcher3.Utilities;
import com.voxel.simplesearchlauncher.utils.AlphaUtil;
import com.ovacoda.devie.R;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes2.dex */
public class MockLauncherController {
    private final DeviceProfile mDeviceProfile;
    private final View mHotseat;
    private final Drawable mHotseatBg;
    private final CellLayout mHotseatCellLayout;
    private final FrameLayout mHotseatGridLineContainer;
    private final GridLineController mHotseatGridLineController;
    private View mMockLauncher;
    private View mSearchBar;
    private final SpringySearchView mSpringySearchView;
    private Map<ItemInfo, Drawable> mWidgetImageMap = new HashMap();
    private final View mWorkspace;
    private final CellLayout mWorkspaceCellLayout;
    private final FrameLayout mWorkspaceGridLineContainer;
    private final GridLineController mWorkspaceGridLineController;

    public interface MockLauncherHolder {
        ViewGroup getRootView();
    }

    public MockLauncherController(View view, DeviceProfile deviceProfile) {
        this.mMockLauncher = view;
        this.mDeviceProfile = deviceProfile;
        this.mWorkspace = this.mMockLauncher.findViewById(R.id.workspace);
        this.mWorkspaceCellLayout = (CellLayout) this.mWorkspace.findViewById(R.id.cell_layout);
        this.mWorkspaceGridLineContainer = (FrameLayout) this.mWorkspace.findViewById(R.id.grid_line_container);
        this.mWorkspaceGridLineController = new GridLineController(this.mWorkspaceGridLineContainer);
        this.mHotseat = this.mMockLauncher.findViewById(R.id.hotseat);
        this.mHotseatBg = this.mHotseat.getBackground();
        this.mHotseatCellLayout = (CellLayout) this.mHotseat.findViewById(R.id.cell_layout);
        this.mHotseatGridLineContainer = (FrameLayout) this.mHotseat.findViewById(R.id.grid_line_container);
        this.mHotseatGridLineController = new GridLineController(this.mHotseatGridLineContainer);
        this.mSpringySearchView = (SpringySearchView) this.mMockLauncher.findViewById(R.id.qsb_search);
        this.mSpringySearchView.setEnabled(false);
        this.mSpringySearchView.findViewById(R.id.search_src_text).setEnabled(false);
        this.mSpringySearchView.setFocusable(false);
        this.mSpringySearchView.clearFocus();
        this.mWorkspaceCellLayout.setDeviceProfile(this.mDeviceProfile);
        this.mWorkspaceCellLayout.setCellParams(this.mDeviceProfile.getCellParamsForType(DeviceProfile.ParamType.WORKSPACE));
        this.mHotseatCellLayout.setDeviceProfile(this.mDeviceProfile);
        this.mHotseatCellLayout.setCellParams(this.mDeviceProfile.getCellParamsForType(DeviceProfile.ParamType.HOTSEAT));
    }

    public void addItemsToCellLayout(CellLayout cellLayout, Context context, List<ItemInfo> list) {
        View view;
        cellLayout.removeAllViews();
        LayoutInflater layoutInflaterFrom = LayoutInflater.from(context);
        IconCache iconCache = LauncherAppState.getInstance().getIconCache();
        for (ItemInfo itemInfo : list) {
            if (itemInfo.cellX + itemInfo.spanX <= cellLayout.getCountX() && itemInfo.cellX >= 0 && itemInfo.cellY + itemInfo.spanY <= cellLayout.getCountY()) {
                if (itemInfo.cellY >= 0) {
                    int i = itemInfo.itemType;
                    if (i != 4) {
                        switch (i) {
                            case 0:
                            case 1:
                                BubbleTextView bubbleTextView = (BubbleTextView) layoutInflaterFrom.inflate(R.layout.application, (ViewGroup) cellLayout, false);
                                bubbleTextView.setCellParams(cellLayout.getCellParams());
                                bubbleTextView.applyFromShortcutInfo((ShortcutInfo) itemInfo, iconCache, false, false);
                                if (itemInfo.container == -100 && cellLayout != null && cellLayout.isOccupied(itemInfo.cellX, itemInfo.cellY)) {
                                    String str = "Collision while binding workspace item: " + itemInfo + ". Collides with " + cellLayout.getChildAt(itemInfo.cellX, itemInfo.cellY).getTag();
                                } else {
                                    CellLayout.LayoutParams layoutParams = new CellLayout.LayoutParams(itemInfo.cellX, itemInfo.cellY, itemInfo.spanX, itemInfo.spanY);
                                    bubbleTextView.setLayoutParams(layoutParams);
                                    cellLayout.addViewToCellLayout(bubbleTextView, 0, Launcher.generateViewId(), layoutParams, true);
                                }
                                break;
                            case 2:
                                FolderInfo folderInfo = (FolderInfo) itemInfo;
                                FolderIcon folderIconFromXml = FolderIcon.fromXml(R.layout.folder_icon, context, cellLayout, folderInfo, iconCache, this.mDeviceProfile, false);
                                folderInfo.removeListener(folderIconFromXml);
                                folderInfo.removeListener(folderIconFromXml.getFolder());
                                folderIconFromXml.setCellParams(cellLayout.getCellParams());
                                CellLayout.LayoutParams layoutParams2 = new CellLayout.LayoutParams(itemInfo.cellX, itemInfo.cellY, itemInfo.spanX, itemInfo.spanY);
                                folderIconFromXml.setLayoutParams(layoutParams2);
                                cellLayout.addViewToCellLayout(folderIconFromXml, 0, Launcher.generateViewId(), layoutParams2, true);
                                break;
                        }
                    } else {
                        Drawable drawable = this.mWidgetImageMap.get(itemInfo);
                        if (drawable != null) {
                            ImageView imageView = new ImageView(context);
                            imageView.setImageDrawable(drawable);
                            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            view = imageView;
                        } else {
                            LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) itemInfo;
                            ViewGroup viewGroup = (ViewGroup) layoutInflaterFrom.inflate(R.layout.onboarding_import_mock_widget, (ViewGroup) cellLayout, false);
                            BubbleTextView bubbleTextView2 = (BubbleTextView) viewGroup.findViewById(R.id.text);
                            Intent component = new Intent().setComponent(launcherAppWidgetInfo.providerName);
                            bubbleTextView2.setCompoundDrawables(null, Utilities.createIconDrawable(iconCache.getIcon(component, launcherAppWidgetInfo.user)), null, null);
                            bubbleTextView2.setCompoundDrawablePadding(cellLayout.getCellParams().getIconDrawablePaddingPx());
                            bubbleTextView2.setText(iconCache.getTitle(component, launcherAppWidgetInfo.user));
                            view = viewGroup;
                        }
                        CellLayout.LayoutParams layoutParams3 = new CellLayout.LayoutParams(itemInfo.cellX, itemInfo.cellY, itemInfo.spanX, itemInfo.spanY);
                        layoutParams3.controlsOwnPadding = true;
                        cellLayout.addViewToCellLayout(view, 0, Launcher.generateViewId(), layoutParams3, true);
                    }
                }
            }
        }
    }

    public CellLayout getHotseatCellLayout() {
        return this.mHotseatCellLayout;
    }

    public GridLineController getHotseatGridLineController() {
        return this.mHotseatGridLineController;
    }

    public CellLayout getWorkspaceCellLayout() {
        return this.mWorkspaceCellLayout;
    }

    public GridLineController getWorkspaceGridLineController() {
        return this.mWorkspaceGridLineController;
    }

    public void layout() {
        this.mSearchBar = this.mMockLauncher.findViewById(R.id.qsb);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mSearchBar.getLayoutParams();
        if (this.mDeviceProfile.searchBarEnabled) {
            this.mSearchBar.setVisibility(0);
        } else {
            this.mSearchBar.setVisibility(4);
        }
        layoutParams.gravity = 49;
        layoutParams.width = this.mDeviceProfile.searchBarSpaceWidthPx;
        int extraTopInset = this.mDeviceProfile.edgeMarginPx * 2;
        layoutParams.height = this.mDeviceProfile.searchBarSpaceHeightPx + extraTopInset;
        this.mSearchBar.setPadding(this.mDeviceProfile.edgeMarginPx * 2, this.mDeviceProfile.getSearchBarTopOffset() + extraTopInset, this.mDeviceProfile.edgeMarginPx * 2, 0);
        this.mSearchBar.setLayoutParams(layoutParams);
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) this.mWorkspace.getLayoutParams();
        layoutParams2.gravity = 17;
        Rect workspacePadding = this.mDeviceProfile.getWorkspacePadding();
        workspacePadding.top += extraTopInset;
        this.mWorkspace.setLayoutParams(layoutParams2);
        this.mWorkspace.setPadding(workspacePadding.left, workspacePadding.top, workspacePadding.right, workspacePadding.bottom);
        CellParams cellParamsForType = this.mDeviceProfile.getCellParamsForType(DeviceProfile.ParamType.HOTSEAT);
        FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) this.mHotseat.getLayoutParams();
        layoutParams3.gravity = 80;
        layoutParams3.height = this.mDeviceProfile.hotseatBarHeightPx;
        Rect hotseatPadding = this.mDeviceProfile.getHotseatPadding();
        this.mHotseat.setPadding(hotseatPadding.left, hotseatPadding.top, hotseatPadding.right, hotseatPadding.bottom);
        this.mHotseatCellLayout.setGridSize((int) cellParamsForType.numColumns, 1);
        this.mHotseatBg.setAlpha(AlphaUtil.floatToIntAlpha(cellParamsForType.backgroundAlpha));
        this.mMockLauncher.requestLayout();
    }
}
