package com.voxel.launcher3;

import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import com.voxel.launcher3.util.FocusLogic;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class FocusHelper {

    public static class PagedFolderKeyEventListener implements View.OnKeyListener {
        private final Folder mFolder;

        public PagedFolderKeyEventListener(Folder folder) {
            this.mFolder = folder;
        }

        public void handleNoopKey(int i, View view) {
            if (i == 20) {
                this.mFolder.mFolderName.requestFocus();
                FocusHelper.playSoundEffect(i, view);
            }
        }

        @Override // android.view.View.OnKeyListener
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            boolean zShouldConsume = FocusLogic.shouldConsume(i);
            if (keyEvent.getAction() == 1) {
                return zShouldConsume;
            }
            if (!(view.getParent() instanceof ShortcutAndWidgetContainer)) {
                if (LauncherAppState.isDogfoodBuild()) {
                    throw new IllegalStateException("Parent of the focused item is not supported.");
                }
                return false;
            }
            ShortcutAndWidgetContainer shortcutAndWidgetContainer = (ShortcutAndWidgetContainer) view.getParent();
            CellLayout cellLayout = (CellLayout) shortcutAndWidgetContainer.getParent();
            int countX = cellLayout.getCountX();
            int countY = cellLayout.getCountY();
            int iIndexOfChild = shortcutAndWidgetContainer.indexOfChild(view);
            FolderPagedView folderPagedView = (FolderPagedView) cellLayout.getParent();
            int iIndexOfChild2 = folderPagedView.indexOfChild(cellLayout);
            int iHandleKeyEvent = FocusLogic.handleKeyEvent(i, countX, countY, FocusLogic.createSparseMatrix(cellLayout), iIndexOfChild, iIndexOfChild2, folderPagedView.getPageCount(), Utilities.isRtl(view.getResources()));
            if (iHandleKeyEvent == -1) {
                handleNoopKey(i, view);
                return zShouldConsume;
            }
            View adjacentChildInNextPage = null;
            switch (iHandleKeyEvent) {
                case -10:
                case -9:
                    int i2 = iIndexOfChild2 + 1;
                    ShortcutAndWidgetContainer cellLayoutChildrenForIndex = FocusHelper.getCellLayoutChildrenForIndex(folderPagedView, i2);
                    if (cellLayoutChildrenForIndex != null) {
                        folderPagedView.snapToPage(i2);
                        adjacentChildInNextPage = FocusLogic.getAdjacentChildInNextPage(cellLayoutChildrenForIndex, view, iHandleKeyEvent);
                    }
                    break;
                case -8:
                    int i3 = iIndexOfChild2 + 1;
                    ShortcutAndWidgetContainer cellLayoutChildrenForIndex2 = FocusHelper.getCellLayoutChildrenForIndex(folderPagedView, i3);
                    if (cellLayoutChildrenForIndex2 != null) {
                        folderPagedView.snapToPage(i3);
                        adjacentChildInNextPage = cellLayoutChildrenForIndex2.getChildAt(0, 0);
                    }
                    break;
                case -7:
                    adjacentChildInNextPage = folderPagedView.getLastItem();
                    break;
                case -6:
                    adjacentChildInNextPage = cellLayout.getChildAt(0, 0);
                    break;
                case -5:
                case -2:
                    int i4 = iIndexOfChild2 - 1;
                    ShortcutAndWidgetContainer cellLayoutChildrenForIndex3 = FocusHelper.getCellLayoutChildrenForIndex(folderPagedView, i4);
                    if (cellLayoutChildrenForIndex3 != null) {
                        int i5 = ((CellLayout.LayoutParams) view.getLayoutParams()).cellY;
                        folderPagedView.snapToPage(i4);
                        adjacentChildInNextPage = cellLayoutChildrenForIndex3.getChildAt((iHandleKeyEvent == -5) ^ cellLayoutChildrenForIndex3.invertLayoutHorizontally() ? 0 : countX - 1, i5);
                    }
                    break;
                case -4:
                    int i6 = iIndexOfChild2 - 1;
                    ShortcutAndWidgetContainer cellLayoutChildrenForIndex4 = FocusHelper.getCellLayoutChildrenForIndex(folderPagedView, i6);
                    if (cellLayoutChildrenForIndex4 != null) {
                        folderPagedView.snapToPage(i6);
                        adjacentChildInNextPage = cellLayoutChildrenForIndex4.getChildAt(countX - 1, countY - 1);
                    }
                    break;
                case -3:
                    int i7 = iIndexOfChild2 - 1;
                    ShortcutAndWidgetContainer cellLayoutChildrenForIndex5 = FocusHelper.getCellLayoutChildrenForIndex(folderPagedView, i7);
                    if (cellLayoutChildrenForIndex5 != null) {
                        folderPagedView.snapToPage(i7);
                        adjacentChildInNextPage = cellLayoutChildrenForIndex5.getChildAt(0, 0);
                    }
                    break;
                default:
                    adjacentChildInNextPage = shortcutAndWidgetContainer.getChildAt(iHandleKeyEvent);
                    break;
            }
            if (adjacentChildInNextPage != null) {
                adjacentChildInNextPage.requestFocus();
                FocusHelper.playSoundEffect(i, view);
            } else {
                handleNoopKey(i, view);
            }
            return zShouldConsume;
        }
    }

    static ShortcutAndWidgetContainer getCellLayoutChildrenForIndex(ViewGroup viewGroup, int i) {
        return ((CellLayout) viewGroup.getChildAt(i)).getShortcutsAndWidgets();
    }

    static boolean handleHotseatButtonKeyEvent(View view, int i, KeyEvent keyEvent) {
        int countX;
        int countY;
        int i2;
        int i3 = i;
        boolean zShouldConsume = FocusLogic.shouldConsume(i);
        if (keyEvent.getAction() == 1 || !zShouldConsume) {
            return zShouldConsume;
        }
        DeviceProfile deviceProfile = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
        ShortcutAndWidgetContainer cellLayoutChildrenForIndex = (ShortcutAndWidgetContainer) view.getParent();
        CellLayout cellLayout = (CellLayout) cellLayoutChildrenForIndex.getParent();
        Workspace workspace = (Workspace) view.getRootView().findViewById(R.id.workspace);
        int nextPage = workspace.getNextPage();
        int childCount = workspace.getChildCount();
        int iIndexOfChild = cellLayoutChildrenForIndex.indexOfChild(view);
        int i4 = ((CellLayout.LayoutParams) cellLayout.getShortcutsAndWidgets().getChildAt(iIndexOfChild).getLayoutParams()).cellX;
        CellLayout cellLayout2 = (CellLayout) workspace.getChildAt(nextPage);
        if (cellLayout2 == null) {
            return zShouldConsume;
        }
        ShortcutAndWidgetContainer shortcutsAndWidgets = cellLayout2.getShortcutsAndWidgets();
        View childAt = null;
        int[][] iArr = (int[][]) null;
        if (i3 == 19 && !deviceProfile.isVerticalBarLayout()) {
            int[][] iArrCreateSparseMatrix = FocusLogic.createSparseMatrix(cellLayout2, cellLayout, true, deviceProfile.hotseatAllAppsRank, i4 == deviceProfile.hotseatAllAppsRank);
            int childCount2 = iIndexOfChild + shortcutsAndWidgets.getChildCount();
            int countX2 = cellLayout2.getCountX();
            iArr = iArrCreateSparseMatrix;
            i2 = childCount2;
            countY = cellLayout2.getCountY() + cellLayout.getCountY();
            countX = countX2;
            cellLayoutChildrenForIndex = shortcutsAndWidgets;
        } else if (i3 == 21 && deviceProfile.isVerticalBarLayout()) {
            int[][] iArrCreateSparseMatrix2 = FocusLogic.createSparseMatrix(cellLayout2, cellLayout, false, deviceProfile.hotseatAllAppsRank, i4 == deviceProfile.hotseatAllAppsRank);
            int childCount3 = iIndexOfChild + shortcutsAndWidgets.getChildCount();
            iArr = iArrCreateSparseMatrix2;
            countX = cellLayout2.getCountX() + cellLayout.getCountX();
            countY = cellLayout2.getCountY();
            i2 = childCount3;
            cellLayoutChildrenForIndex = shortcutsAndWidgets;
        } else if (i3 == 22 && deviceProfile.isVerticalBarLayout()) {
            i3 = 93;
            i2 = iIndexOfChild;
            cellLayoutChildrenForIndex = null;
            countX = -1;
            countY = -1;
        } else {
            int[][] iArrCreateSparseMatrix3 = FocusLogic.createSparseMatrix(cellLayout);
            countX = cellLayout.getCountX();
            iArr = iArrCreateSparseMatrix3;
            countY = cellLayout.getCountY();
            i2 = iIndexOfChild;
        }
        int iHandleKeyEvent = FocusLogic.handleKeyEvent(i3, countX, countY, iArr, i2, nextPage, childCount, Utilities.isRtl(view.getResources()));
        if (iHandleKeyEvent == -8) {
            int i5 = nextPage + 1;
            cellLayoutChildrenForIndex = getCellLayoutChildrenForIndex(workspace, i5);
            childAt = cellLayoutChildrenForIndex.getChildAt(0);
            workspace.snapToPage(i5);
        }
        if (cellLayoutChildrenForIndex == shortcutsAndWidgets && iHandleKeyEvent >= shortcutsAndWidgets.getChildCount()) {
            iHandleKeyEvent -= shortcutsAndWidgets.getChildCount();
        }
        if (cellLayoutChildrenForIndex != null) {
            if (childAt == null && iHandleKeyEvent >= 0) {
                childAt = cellLayoutChildrenForIndex.getChildAt(iHandleKeyEvent);
            }
            if (childAt != null) {
                childAt.requestFocus();
                playSoundEffect(i3, view);
            }
        }
        return zShouldConsume;
    }

    /* JADX WARN: Code duplicated, block: B:55:0x01d8  */
    static boolean handleIconKeyEvent(View view, int i, KeyEvent keyEvent) {
        int i2;
        int i3;
        int[][] iArrCreateSparseMatrix;
        /* REWORK: jadx left this register blank; the switch below assigns every
         * reachable path, but default it to null to be safe. */
        View childAt = null;
        boolean zShouldConsume = FocusLogic.shouldConsume(i);
        if (keyEvent.getAction() == 1 || !zShouldConsume) {
            return zShouldConsume;
        }
        DeviceProfile deviceProfile = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
        ShortcutAndWidgetContainer shortcutAndWidgetContainer = (ShortcutAndWidgetContainer) view.getParent();
        CellLayout cellLayout = (CellLayout) shortcutAndWidgetContainer.getParent();
        Workspace workspace = (Workspace) cellLayout.getParent();
        ViewGroup viewGroup = (ViewGroup) workspace.getParent();
        ViewGroup viewGroup2 = (ViewGroup) viewGroup.findViewById(R.id.search_drop_target_bar);
        Hotseat hotseat = (Hotseat) viewGroup.findViewById(R.id.hotseat);
        int iIndexOfChild = shortcutAndWidgetContainer.indexOfChild(view);
        int iIndexOfChild2 = workspace.indexOfChild(cellLayout);
        int childCount = workspace.getChildCount();
        int countX = cellLayout.getCountX();
        int countY = cellLayout.getCountY();
        CellLayout layout = hotseat.getLayout();
        ShortcutAndWidgetContainer shortcutsAndWidgets = layout.getShortcutsAndWidgets();
        if (i == 20 && !deviceProfile.isVerticalBarLayout()) {
            i2 = countX;
            i3 = countY + 1;
            iArrCreateSparseMatrix = FocusLogic.createSparseMatrix(cellLayout, layout, true, deviceProfile.hotseatAllAppsRank, !hotseat.hasIcons());
        } else if (i == 22 && deviceProfile.isVerticalBarLayout()) {
            i2 = countX + 1;
            i3 = countY;
            iArrCreateSparseMatrix = FocusLogic.createSparseMatrix(cellLayout, layout, false, deviceProfile.hotseatAllAppsRank, !hotseat.hasIcons());
        } else {
            if (i == 67 || i == 112) {
                return zShouldConsume;
            }
            i2 = countX;
            i3 = countY;
            iArrCreateSparseMatrix = FocusLogic.createSparseMatrix(cellLayout);
        }
        int iHandleKeyEvent = FocusLogic.handleKeyEvent(i, i2, i3, iArrCreateSparseMatrix, iIndexOfChild, iIndexOfChild2, childCount, Utilities.isRtl(view.getResources()));
        switch (iHandleKeyEvent) {
            case -10:
            case -2:
                int i4 = iHandleKeyEvent == -10 ? iIndexOfChild2 + 1 : iIndexOfChild2 - 1;
                int i5 = ((CellLayout.LayoutParams) view.getLayoutParams()).cellY;
                ShortcutAndWidgetContainer cellLayoutChildrenForIndex = getCellLayoutChildrenForIndex(workspace, i4);
                workspace.snapToPage(i4);
                if (cellLayoutChildrenForIndex == null) {
                    childAt = null;
                } else {
                    workspace.snapToPage(i4);
                    CellLayout cellLayout2 = (CellLayout) cellLayoutChildrenForIndex.getParent();
                    childAt = cellLayoutChildrenForIndex.getChildAt(FocusLogic.handleKeyEvent(i, i2 + 1, i3, FocusLogic.createSparseMatrix(cellLayout2, cellLayout2.getCountX(), i5), 100, i4, childCount, Utilities.isRtl(view.getResources())));
                }
                break;
            case -9:
            case -5:
                int i6 = iHandleKeyEvent == -5 ? iIndexOfChild2 - 1 : iIndexOfChild2 + 1;
                workspace.snapToPage(i6);
                int i7 = ((CellLayout.LayoutParams) view.getLayoutParams()).cellY;
                ShortcutAndWidgetContainer cellLayoutChildrenForIndex2 = getCellLayoutChildrenForIndex(workspace, i6);
                if (cellLayoutChildrenForIndex2 == null) {
                    childAt = null;
                } else {
                    workspace.snapToPage(i6);
                    childAt = cellLayoutChildrenForIndex2.getChildAt(FocusLogic.handleKeyEvent(i, i2 + 1, i3, FocusLogic.createSparseMatrix((CellLayout) cellLayoutChildrenForIndex2.getParent(), -1, i7), 100, i6, childCount, Utilities.isRtl(view.getResources())));
                }
                break;
            case -8:
                int i8 = iIndexOfChild2 + 1;
                childAt = getCellLayoutChildrenForIndex(workspace, i8).getChildAt(0);
                workspace.snapToPage(i8);
                break;
            case -7:
                childAt = shortcutAndWidgetContainer.getChildAt(shortcutAndWidgetContainer.getChildCount() - 1);
                break;
            case -6:
                childAt = shortcutAndWidgetContainer.getChildAt(0);
                break;
            case -4:
                int i9 = iIndexOfChild2 - 1;
                ShortcutAndWidgetContainer cellLayoutChildrenForIndex3 = getCellLayoutChildrenForIndex(workspace, i9);
                childAt = cellLayoutChildrenForIndex3.getChildAt(cellLayoutChildrenForIndex3.getChildCount() - 1);
                workspace.snapToPage(i9);
                break;
            case -3:
                int i10 = iIndexOfChild2 - 1;
                childAt = getCellLayoutChildrenForIndex(workspace, i10).getChildAt(0);
                workspace.snapToPage(i10);
                break;
            case -1:
                if (i != 19) {
                    childAt = null;
                }
                break;
            default:
                if (iHandleKeyEvent >= 0 && iHandleKeyEvent < shortcutAndWidgetContainer.getChildCount()) {
                    childAt = shortcutAndWidgetContainer.getChildAt(iHandleKeyEvent);
                } else if (shortcutAndWidgetContainer.getChildCount() <= iHandleKeyEvent && iHandleKeyEvent < shortcutAndWidgetContainer.getChildCount() + shortcutsAndWidgets.getChildCount()) {
                    childAt = shortcutsAndWidgets.getChildAt(iHandleKeyEvent - shortcutAndWidgetContainer.getChildCount());
                } else {
                    childAt = null;
                }
                break;
        }
        if (childAt != null) {
            childAt = viewGroup2;
            childAt.requestFocus();
            playSoundEffect(i, view);
        }
        childAt = viewGroup2;
        return zShouldConsume;
    }

    static void playSoundEffect(int i, View view) {
        switch (i) {
            case 19:
            case 92:
            case 122:
                view.playSoundEffect(2);
                break;
            case 20:
            case 93:
            case 123:
                view.playSoundEffect(4);
                break;
            case 21:
                view.playSoundEffect(1);
                break;
            case 22:
                view.playSoundEffect(3);
                break;
        }
    }
}
