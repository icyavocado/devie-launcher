package com.voxel.launcher3;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.text.Selection;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.evie.common.dragndrop.DragObjectAbstraction;
import com.evie.common.dragndrop.DragSource;
import com.voxel.launcher3.dragndrop.DragController;
import com.voxel.launcher3.dragndrop.DragOptions;
import com.voxel.simplesearchlauncher.lock.LockOverlayFragment;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.voxel.simplesearchlauncher.popup.PopupFragment;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class Folder extends LinearLayout implements View.OnClickListener, View.OnFocusChangeListener, View.OnLongClickListener, TextView.OnEditorActionListener, DragSource, DropTarget, FolderInfo.FolderListener, DragController.DragListener {
    private static String sDefaultFolderName;
    private static String sHintText;
    private ActionMode.Callback mActionModeCallback;
    private CellParams mCellParams;
    FolderPagedView mContent;
    View mContentWrapper;
    private ShortcutInfo mCurrentDragInfo;
    private View mCurrentDragView;
    int mCurrentScrollDir;
    private boolean mDeferDropAfterUninstall;
    Runnable mDeferredAction;
    private boolean mDeleteFolderOnDropCompleted;
    private boolean mDestroyed;
    private GradientDrawable mDragBackground;
    private Animator mDragBackgroundAlphaAnim;
    protected DragController mDragController;
    private boolean mDragInProgress;
    int mEmptyCellRank;
    private final int mExpandDuration;
    FolderIcon mFolderIcon;
    float mFolderIconPivotX;
    float mFolderIconPivotY;
    ExtendedEditText mFolderName;
    private int mFolderNameHeight;
    private View mFooter;
    private int mFooterHeight;
    protected FolderInfo mInfo;
    private final InputMethodManager mInputMethodManager;
    private boolean mIsEditingName;
    private boolean mIsExternalDrag;
    private boolean mItemAddedBackToSelfViaIcon;
    final ArrayList<View> mItemsInReadingOrder;
    boolean mItemsInvalidated;
    protected Launcher mLauncher;
    private final int mMaterialExpandDuration;
    private final int mMaterialExpandStagger;
    private final Alarm mOnExitAlarm;
    OnAlarmListener mOnExitAlarmListener;
    private final Alarm mOnScrollHintAlarm;
    int mPrevTargetRank;
    private boolean mRearrangeOnClose;
    private final Alarm mReorderAlarm;
    OnAlarmListener mReorderAlarmListener;
    private int mScrollAreaOffset;
    int mScrollHintDir;
    final Alarm mScrollPauseAlarm;
    int mState;
    private boolean mSuppressFolderDeletion;
    boolean mSuppressOnAdd;
    int mTargetRank;
    private boolean mUninstallSuccessful;
    private static final Rect sTempRect = new Rect();
    public static final Comparator<ItemInfo> ITEM_POS_COMPARATOR = new Comparator() { // from class: com.voxel.launcher3.-$$Lambda$Folder$WgKHSCDZp7tDi-9d-DRnsse-hjI
        @Override // java.util.Comparator
        public final int compare(Object obj, Object obj2) {
            return Folder.lambda$static$9((ItemInfo) obj, (ItemInfo) obj2);
        }
    };
    public static final Comparator<ItemInfo> ALPHA_COMPARATOR = new Comparator() { // from class: com.voxel.launcher3.-$$Lambda$Folder$12mRUzS7MMAtk135yLD9Nqe-CVE
        @Override // java.util.Comparator
        public final int compare(Object obj, Object obj2) {
            return Folder.lambda$static$10((ItemInfo) obj, (ItemInfo) obj2);
        }
    };

    private class OnScrollFinishedListener implements OnAlarmListener {
        private final DropTarget.DragObject mDragObject;

        OnScrollFinishedListener(DropTarget.DragObject dragObject) {
            this.mDragObject = dragObject;
        }

        @Override // com.voxel.launcher3.OnAlarmListener
        public void onAlarm(Alarm alarm) {
            Folder.this.onDragOver(this.mDragObject, 1);
        }
    }

    private class OnScrollHintListener implements OnAlarmListener {
        private final DropTarget.DragObject mDragObject;

        OnScrollHintListener(DropTarget.DragObject dragObject) {
            this.mDragObject = dragObject;
        }

        @Override // com.voxel.launcher3.OnAlarmListener
        public void onAlarm(Alarm alarm) {
            if (Folder.this.mCurrentScrollDir == 0) {
                Folder.this.mContent.scrollLeft();
                Folder.this.mScrollHintDir = -1;
            } else {
                if (Folder.this.mCurrentScrollDir != 1) {
                    return;
                }
                Folder.this.mContent.scrollRight();
                Folder.this.mScrollHintDir = -1;
            }
            Folder folder = Folder.this;
            folder.mCurrentScrollDir = -1;
            folder.mScrollPauseAlarm.setOnAlarmListener(Folder.this.new OnScrollFinishedListener(this.mDragObject));
            Folder.this.mScrollPauseAlarm.setAlarm(900L);
        }
    }

    public Folder(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mReorderAlarm = new Alarm();
        this.mOnExitAlarm = new Alarm();
        this.mOnScrollHintAlarm = new Alarm();
        this.mScrollPauseAlarm = new Alarm();
        this.mItemsInReadingOrder = new ArrayList<>();
        this.mState = -1;
        this.mRearrangeOnClose = false;
        this.mItemsInvalidated = false;
        this.mSuppressOnAdd = false;
        this.mDragInProgress = false;
        this.mDeleteFolderOnDropCompleted = false;
        this.mSuppressFolderDeletion = false;
        this.mItemAddedBackToSelfViaIcon = false;
        this.mIsEditingName = false;
        this.mScrollHintDir = -1;
        this.mCurrentScrollDir = -1;
        this.mActionModeCallback = new ActionMode.Callback() { // from class: com.voxel.launcher3.Folder.1
            @Override // android.view.ActionMode.Callback
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                return false;
            }

            @Override // android.view.ActionMode.Callback
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override // android.view.ActionMode.Callback
            public void onDestroyActionMode(ActionMode actionMode) {
            }

            @Override // android.view.ActionMode.Callback
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }
        };
        this.mReorderAlarmListener = new OnAlarmListener() { // from class: com.voxel.launcher3.Folder.2
            @Override // com.voxel.launcher3.OnAlarmListener
            public void onAlarm(Alarm alarm) {
                Folder.this.mContent.realTimeReorder(Folder.this.mEmptyCellRank, Folder.this.mTargetRank);
                Folder folder = Folder.this;
                folder.mEmptyCellRank = folder.mTargetRank;
            }
        };
        this.mOnExitAlarmListener = new OnAlarmListener() { // from class: com.voxel.launcher3.-$$Lambda$Folder$RjW4n87ADXXHAlDudFC9X0iaDv4
            @Override // com.voxel.launcher3.OnAlarmListener
            public final void onAlarm(Alarm alarm) {
                Folder.this.completeDragExit();
            }
        };
        setAlwaysDrawnWithCacheEnabled(false);
        this.mInputMethodManager = (InputMethodManager) getContext().getSystemService("input_method");
        Resources resources = getResources();
        this.mExpandDuration = resources.getInteger(R.integer.config_folderExpandDuration);
        this.mMaterialExpandDuration = resources.getInteger(R.integer.config_materialFolderExpandDuration);
        this.mMaterialExpandStagger = resources.getInteger(R.integer.config_materialFolderExpandStagger);
        if (sDefaultFolderName == null) {
            sDefaultFolderName = resources.getString(R.string.folder_name);
        }
        if (sHintText == null) {
            sHintText = resources.getString(R.string.folder_hint_text);
        }
        if (context instanceof Launcher) {
            this.mLauncher = (Launcher) context;
        }
        setFocusableInTouchMode(true);
        this.mCellParams = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getCellParamsForType(DeviceProfile.ParamType.FOLDER);
    }

    private boolean beginDrag(View view, boolean z) {
        Object tag = view.getTag();
        if (tag instanceof ShortcutInfo) {
            ShortcutInfo shortcutInfo = (ShortcutInfo) tag;
            if (!view.isInTouchMode()) {
                return false;
            }
            DragOptions dragOptions = new DragOptions();
            dragOptions.isDesktopLocked = SettingsStorage.getInstance().isDesktopLockedCurrently();
            if (PopupFragment.canProperlyShow(shortcutInfo)) {
                PopupFragment popupFragment = new PopupFragment(view, shortcutInfo, this);
                this.mLauncher.pushStackFragment(popupFragment);
                dragOptions.deferDragCondition = popupFragment.createDeferDragCondition(null);
            }
            this.mLauncher.getWorkspace().beginDragShared(view, this, dragOptions);
            this.mCurrentDragInfo = shortcutInfo;
            this.mEmptyCellRank = shortcutInfo.rank;
            this.mCurrentDragView = view;
            this.mContent.removeItem(this.mCurrentDragView);
            this.mInfo.remove(this.mCurrentDragInfo);
            this.mDragInProgress = true;
            this.mItemAddedBackToSelfViaIcon = false;
        }
        return true;
    }

    private void clearDragInfo() {
        this.mCurrentDragInfo = null;
        this.mCurrentDragView = null;
        this.mSuppressOnAdd = false;
        this.mIsExternalDrag = false;
    }

    @SuppressLint({"InflateParams"})
    public static Folder fromXml(Context context) {
        return (Folder) LayoutInflater.from(context).inflate(R.layout.user_folder, (ViewGroup) null);
    }

    private int getContentAreaWidth() {
        return Math.max(this.mContent.getDesiredWidth(), 5);
    }

    private int getFolderHeight(int i) {
        return getPaddingTop() + getPaddingBottom() + i + this.mFolderNameHeight + this.mFooterHeight;
    }

    private int getTargetRank(DropTarget.DragObject dragObject, float[] fArr) {
        float[] visualCenter = dragObject.getVisualCenter(fArr);
        return this.mContent.findNearestArea((((int) visualCenter[0]) - this.mContentWrapper.getLeft()) - this.mContent.getLeft(), (((int) visualCenter[1]) - this.mContentWrapper.getTop()) - this.mContent.getTop());
    }

    private View getViewForInfo(final ShortcutInfo shortcutInfo) {
        return this.mContent.iterateOverItems(new Workspace.ItemOperator() { // from class: com.voxel.launcher3.-$$Lambda$Folder$UoodmKmCO2mOsJN65KUh00F-5kM
            @Override // com.voxel.launcher3.Workspace.ItemOperator
            public final boolean evaluate(ItemInfo itemInfo, View view, View view2) {
                return Folder.lambda$getViewForInfo$6(shortcutInfo, itemInfo, view, view2);
            }
        });
    }

    public void lambda$bind$2() {
        if (this.getItemCount() <= 1) {
            this.replaceFolderWithFinalItem();
        }
    }

    static /* synthetic */ boolean lambda$getItemsInReadingOrder$7(List list, Map map, ItemInfo itemInfo, View view, View view2) {
        list.add((ShortcutInfo) itemInfo);
        map.put(itemInfo, view);
        return false;
    }

    public boolean lambda$getItemsInReadingOrder$8(ItemInfo itemInfo, View view, View view2) {
        this.mItemsInReadingOrder.add(view);
        return false;
    }

    static /* synthetic */ boolean lambda$getViewForInfo$6(ShortcutInfo shortcutInfo, ItemInfo itemInfo, View view, View view2) {
        return itemInfo == shortcutInfo;
    }

    public void lambda$onDropCompleted$4(View view, DropTarget.DragObject dragObject, boolean z, boolean z2) {
        this.onDropCompleted(view, dragObject, z, z2);
        this.mDeferredAction = null;
    }

    public void lambda$onFinishInflate$0(View view) {
        if (this.mLauncher == null || this.mContent.isPageMoving()) {
            return;
        }
        this.mLauncher.closeFolder();
    }

    public boolean lambda$onFinishInflate$1() {
        this.doneEditingFolderName(true);
        return false;
    }

    public void lambda$replaceFolderWithFinalItem$5() {
        View view;
        CellLayout cellLayout = this.mLauncher.getCellLayout(this.mInfo.container, this.mInfo.screenId);
        if (this.getItemCount() != 1 || cellLayout == null) {
            view = null;
        } else {
            ShortcutInfo shortcutInfo = this.mInfo.contents.get(0);
            View viewCreateShortcut = this.mLauncher.createShortcut(R.layout.application, cellLayout, shortcutInfo);
            LauncherModel.addOrMoveItemInDatabase(this.mLauncher, shortcutInfo, this.mInfo.container, this.mInfo.screenId, this.mInfo.cellX, this.mInfo.cellY);
            view = viewCreateShortcut;
        }
        if (this.getItemCount() <= 1) {
            LauncherModel.deleteItemFromDatabase(this.mLauncher, this.mInfo);
            if (cellLayout != null) {
                cellLayout.removeView(this.mFolderIcon);
            }
            ViewParent viewParent = this.mFolderIcon;
            if (viewParent instanceof DropTarget) {
                this.mDragController.removeDropTarget((DropTarget) viewParent);
            }
            this.mLauncher.removeFolder(this.mInfo);
        }
        if (view != null) {
            this.mLauncher.getWorkspace().addInScreenFromBind(view, this.mInfo.container, this.mInfo.screenId, this.mInfo.cellX, this.mInfo.cellY, this.mInfo.spanX, this.mInfo.spanY);
        }
    }

    static /* synthetic */ int lambda$static$10(ItemInfo itemInfo, ItemInfo itemInfo2) {
        CharSequence charSequence = itemInfo.title;
        CharSequence charSequence2 = itemInfo2.title;
        if (charSequence == null && charSequence2 == null) {
            return 0;
        }
        if (charSequence == null) {
            return 1;
        }
        if (charSequence2 == null) {
            return -1;
        }
        return String.CASE_INSENSITIVE_ORDER.compare(itemInfo.title.toString(), itemInfo2.title.toString());
    }

    static /* synthetic */ int lambda$static$9(ItemInfo itemInfo, ItemInfo itemInfo2) {
        if (itemInfo.rank != itemInfo2.rank) {
            return itemInfo.rank - itemInfo2.rank;
        }
        return itemInfo.cellY != itemInfo2.cellY ? itemInfo.cellY - itemInfo2.cellY : itemInfo.cellX - itemInfo2.cellX;
    }

    private void showScrollHint(int i, DropTarget.DragObject dragObject) {
        if (this.mScrollHintDir != i) {
            this.mContent.showScrollHint(i);
            this.mScrollHintDir = i;
        }
        if (this.mOnScrollHintAlarm.alarmPending() && this.mCurrentScrollDir == i) {
            return;
        }
        this.mCurrentScrollDir = i;
        this.mOnScrollHintAlarm.cancelAlarm();
        this.mOnScrollHintAlarm.setOnAlarmListener(new OnScrollHintListener(dragObject));
        this.mOnScrollHintAlarm.setAlarm(500L);
        this.mReorderAlarm.cancelAlarm();
        this.mTargetRank = this.mEmptyCellRank;
    }

    private void updateItemLocationsInDatabaseBatch() {
        if (this.mLauncher == null) {
            return;
        }
        ArrayList<View> itemsInReadingOrder = getItemsInReadingOrder();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < itemsInReadingOrder.size(); i++) {
            ItemInfo itemInfo = (ItemInfo) itemsInReadingOrder.get(i).getTag();
            itemInfo.rank = i;
            arrayList.add(itemInfo);
        }
        LauncherModel.moveItemsInDatabase(this.mLauncher, arrayList, this.mInfo.id, 0);
    }

    @Override // com.voxel.launcher3.DropTarget
    public boolean acceptDrop(DropTarget.DragObject dragObject) {
        ItemInfo itemInfo = dragObject.dragInfo;
        if (dragObject.isDesktopLocked) {
            return false;
        }
        int i = itemInfo.itemType;
        return (i == 0 || i == 1 || i == 6) && !isFull();
    }

    public void beginExternalDrag(ShortcutInfo shortcutInfo) {
        this.mCurrentDragInfo = shortcutInfo;
        this.mEmptyCellRank = this.mContent.allocateRankForNewItem(shortcutInfo);
        this.mIsExternalDrag = true;
        this.mDragInProgress = true;
    }

    public void bind(FolderInfo folderInfo, boolean z, boolean z2) {
        this.mInfo = folderInfo;
        ArrayList<ShortcutInfo> arrayList = folderInfo.contents;
        if (z2) {
            Collections.sort(arrayList, ALPHA_COMPARATOR);
        } else {
            Collections.sort(arrayList, ITEM_POS_COMPARATOR);
        }
        ArrayList<ShortcutInfo> arrayListBindItems = this.mContent.bindItems(arrayList, z);
        if (this.mLauncher != null) {
            for (ShortcutInfo shortcutInfo : arrayListBindItems) {
                this.mInfo.remove(shortcutInfo);
                LauncherModel.deleteItemFromDatabase(this.mLauncher, shortcutInfo);
            }
        }
        this.mItemsInvalidated = true;
        updateTextViewFocus();
        this.mInfo.addListener(this);
        if (this.mInfo.title == null || sDefaultFolderName.contentEquals(this.mInfo.title)) {
            this.mFolderName.setText("");
        } else {
            this.mFolderName.setText(this.mInfo.title);
        }
        if (this.mLauncher != null) {
            this.mFolderIcon.post(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Folder$2zJZH3sYoBN7n0dId1P4eH4KGKo
                @Override // java.lang.Runnable
                public final void run() {
                    Folder.this.lambda$bind$2();
                }
            });
        }
    }

    public void completeDragExit() {
        if (this.mInfo.opened) {
            this.mLauncher.closeFolder();
            this.mRearrangeOnClose = true;
        } else if (this.mState == 1) {
            this.mRearrangeOnClose = true;
        } else {
            rearrangeChildren();
            clearDragInfo();
        }
    }

    public void dismissEditingName() {
        this.mInputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        doneEditingFolderName(true);
    }

    @Override // android.view.View
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        return true;
    }

    public void doneEditingFolderName(boolean z) {
        if (SettingsStorage.getInstance().isDesktopLockedCurrently()) {
            this.mFolderName.setText(this.mInfo.title);
            return;
        }
        this.mFolderName.setHint(sHintText);
        String string = this.mFolderName.getText().toString();
        this.mInfo.setTitle(string);
        Launcher launcher = this.mLauncher;
        if (launcher != null) {
            LauncherModel.updateItemInDatabase(launcher, this.mInfo);
        }
        if (z) {
            sendCustomAccessibilityEvent(32, String.format(getContext().getString(R.string.folder_renamed), string));
        }
        requestFocus();
        Selection.setSelection(this.mFolderName.getText(), 0, 0);
        this.mIsEditingName = false;
    }

    public FolderPagedView getContent() {
        return this.mContent;
    }

    public int getContentAreaHeight() {
        return Math.max(Math.min((LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().availableHeightPx - this.mFooterHeight) - this.mFolderNameHeight, this.mContent.getDesiredHeight()), 5);
    }

    public View getEditTextRegion() {
        return this.mFolderName;
    }

    @Override // com.voxel.launcher3.DropTarget
    public void getHitRectRelativeToDragLayer(Rect rect) {
        getHitRect(rect);
        rect.left -= this.mScrollAreaOffset;
        rect.right += this.mScrollAreaOffset;
    }

    public FolderInfo getInfo() {
        return this.mInfo;
    }

    @Override // com.evie.common.dragndrop.DragSource
    public float getIntrinsicIconScaleFactor() {
        return 1.0f;
    }

    public int getItemCount() {
        return this.mContent.getItemCount();
    }

    public ArrayList<View> getItemsInReadingOrder() {
        if (this.mItemsInvalidated) {
            this.mItemsInReadingOrder.clear();
            if (SettingsStorage.getInstance().getFolderAlphabetSortEnabled()) {
                final ArrayList arrayList = new ArrayList();
                final HashMap map = new HashMap();
                this.mContent.iterateOverItems(new Workspace.ItemOperator() { // from class: com.voxel.launcher3.-$$Lambda$Folder$I5eS0Uira6uBjE3-q2P9pguPZT4
                    @Override // com.voxel.launcher3.Workspace.ItemOperator
                    public final boolean evaluate(ItemInfo itemInfo, View view, View view2) {
                        return Folder.lambda$getItemsInReadingOrder$7(arrayList, map, itemInfo, view, view2);
                    }
                });
                Collections.sort(arrayList, ALPHA_COMPARATOR);
                Iterator it = arrayList.iterator();
                while (it.hasNext()) {
                    this.mItemsInReadingOrder.add((View) map.get((ShortcutInfo) it.next()));
                }
            } else {
                this.mContent.iterateOverItems(new Workspace.ItemOperator() { // from class: com.voxel.launcher3.-$$Lambda$Folder$hMYExIYs_fTp1q9Ov38CJ4z5HBE
                    @Override // com.voxel.launcher3.Workspace.ItemOperator
                    public final boolean evaluate(ItemInfo itemInfo, View view, View view2) {
                        return Folder.this.lambda$getItemsInReadingOrder$8( itemInfo, view, view2);
                    }
                });
            }
            this.mItemsInvalidated = false;
        }
        return this.mItemsInReadingOrder;
    }

    float getPivotXForIconAnimation() {
        return this.mFolderIconPivotX;
    }

    float getPivotYForIconAnimation() {
        return this.mFolderIconPivotY;
    }

    public void hideItem(ShortcutInfo shortcutInfo) {
        getViewForInfo(shortcutInfo).setVisibility(4);
    }

    boolean isDestroyed() {
        return this.mDestroyed;
    }

    @Override // com.voxel.launcher3.DropTarget
    public boolean isDropEnabled() {
        return true;
    }

    public boolean isEditingName() {
        return this.mIsEditingName;
    }

    public boolean isFull() {
        return this.mContent.isFull();
    }

    @TargetApi(17)
    public boolean isLayoutRtl() {
        return getLayoutDirection() == 1;
    }

    public void notifyDrop() {
        if (this.mDragInProgress) {
            this.mItemAddedBackToSelfViaIcon = true;
        }
    }

    @Override // com.voxel.launcher3.FolderInfo.FolderListener
    public void onAdd(ShortcutInfo shortcutInfo) {
        if (this.mSuppressOnAdd) {
            return;
        }
        FolderPagedView folderPagedView = this.mContent;
        folderPagedView.createAndAddViewForRank(shortcutInfo, folderPagedView.allocateRankForNewItem(shortcutInfo));
        this.mItemsInvalidated = true;
        Launcher launcher = this.mLauncher;
        if (launcher != null) {
            LauncherModel.addOrMoveItemInDatabase(launcher, shortcutInfo, this.mInfo.id, 0L, shortcutInfo.cellX, shortcutInfo.cellY);
        }
        if (SettingsStorage.getInstance().getFolderAlphabetSortEnabled()) {
            rearrangeChildren();
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view.getTag() instanceof ShortcutInfo) {
            this.mLauncher.onClick(view);
        }
    }

    public void onCloseComplete() {
        ViewGroup viewGroup = (ViewGroup) getParent();
        if (viewGroup != null) {
            viewGroup.removeView(this);
        }
        this.mDragController.removeDropTarget(this);
        clearFocus();
        this.mFolderIcon.requestFocus();
        if (this.mRearrangeOnClose) {
            rearrangeChildren();
            this.mRearrangeOnClose = false;
        }
        if (getItemCount() <= 1) {
            if (!this.mDragInProgress && !this.mSuppressFolderDeletion) {
                replaceFolderWithFinalItem();
            } else if (this.mDragInProgress) {
                this.mDeleteFolderOnDropCompleted = true;
            }
        } else if (this.mContent.getPageCount() >= 1) {
            this.mContent.setCurrentPage(0);
        }
        this.mSuppressFolderDeletion = false;
        clearDragInfo();
    }

    @Override // com.voxel.launcher3.dragndrop.DragController.DragListener
    public void onDragEnd() {
        if (this.mIsExternalDrag && this.mDragInProgress) {
            completeDragExit();
        }
        Animator animator = this.mDragBackgroundAlphaAnim;
        if (animator != null) {
            animator.cancel();
            this.mDragBackgroundAlphaAnim = null;
        }
        if (!this.mInfo.opened) {
            this.mDragBackground.setAlpha(0);
            return;
        }
        this.mDragBackgroundAlphaAnim = LauncherAnimUtils.ofPropertyValuesHolder(this.mDragBackground, this, PropertyValuesHolder.ofInt("alpha", 0));
        this.mDragBackgroundAlphaAnim.setDuration(this.mMaterialExpandDuration);
        this.mDragBackgroundAlphaAnim.start();
    }

    @Override // com.voxel.launcher3.DropTarget
    public void onDragEnter(DropTarget.DragObject dragObject) {
        this.mPrevTargetRank = -1;
        this.mOnExitAlarm.cancelAlarm();
        this.mScrollAreaOffset = (dragObject.dragView.getDragRegionWidth() / 2) - dragObject.xOffset;
    }

    @Override // com.voxel.launcher3.DropTarget
    public void onDragExit(DropTarget.DragObject dragObject) {
        if (!dragObject.dragComplete) {
            this.mOnExitAlarm.setOnAlarmListener(this.mOnExitAlarmListener);
            this.mOnExitAlarm.setAlarm(400L);
        }
        this.mReorderAlarm.cancelAlarm();
        this.mOnScrollHintAlarm.cancelAlarm();
        this.mScrollPauseAlarm.cancelAlarm();
        if (this.mScrollHintDir != -1) {
            this.mContent.clearScrollHint();
            this.mScrollHintDir = -1;
        }
    }

    @Override // com.voxel.launcher3.DropTarget
    public void onDragOver(DropTarget.DragObject dragObject) {
        onDragOver(dragObject, 250);
    }

    void onDragOver(DropTarget.DragObject dragObject, int i) {
        if (this.mScrollPauseAlarm.alarmPending() || SettingsStorage.getInstance().getFolderAlphabetSortEnabled() || dragObject.isDesktopLocked) {
            return;
        }
        float[] fArr = new float[2];
        this.mTargetRank = getTargetRank(dragObject, fArr);
        if (this.mTargetRank != this.mPrevTargetRank) {
            this.mReorderAlarm.cancelAlarm();
            this.mReorderAlarm.setOnAlarmListener(this.mReorderAlarmListener);
            this.mReorderAlarm.setAlarm(250L);
            this.mPrevTargetRank = this.mTargetRank;
        }
        float f = fArr[0];
        int nextPage = this.mContent.getNextPage();
        float cellWidth = this.mContent.getCurrentCellLayout().getCellWidth() * 0.45f;
        boolean z = f < cellWidth;
        boolean z2 = f > ((float) getWidth()) - cellWidth;
        if (nextPage > 0 && (!this.mContent.mIsRtl ? !z : !z2)) {
            showScrollHint(0, dragObject);
            return;
        }
        if (nextPage < this.mContent.getPageCount() - 1 && (!this.mContent.mIsRtl ? !z2 : !z)) {
            showScrollHint(1, dragObject);
            return;
        }
        this.mOnScrollHintAlarm.cancelAlarm();
        if (this.mScrollHintDir != -1) {
            this.mContent.clearScrollHint();
            this.mScrollHintDir = -1;
        }
    }

    @Override // com.voxel.launcher3.dragndrop.DragController.DragListener
    public void onDragStart(DropTarget.DragObject dragObject, DragOptions dragOptions) {
        ShortcutInfo shortcutInfoMakeShortcut;
        if (dragObject.dragSource != this && this.mInfo.opened) {
            if (dragObject.dragInfo instanceof ShortcutInfo) {
                shortcutInfoMakeShortcut = (ShortcutInfo) dragObject.dragInfo;
            } else if (dragObject.dragInfo instanceof AppInfo) {
                shortcutInfoMakeShortcut = ((AppInfo) dragObject.dragInfo).makeShortcut();
                shortcutInfoMakeShortcut.spanX = 1;
                shortcutInfoMakeShortcut.spanY = 1;
            } else {
                shortcutInfoMakeShortcut = null;
            }
            if (shortcutInfoMakeShortcut != null) {
                if (isFull()) {
                    completeDragExit();
                } else {
                    beginExternalDrag(shortcutInfoMakeShortcut);
                }
            }
        }
        Animator animator = this.mDragBackgroundAlphaAnim;
        if (animator != null) {
            animator.cancel();
            this.mDragBackgroundAlphaAnim = null;
        }
        if (!this.mInfo.opened) {
            this.mDragBackground.setAlpha(255);
            return;
        }
        this.mDragBackgroundAlphaAnim = LauncherAnimUtils.ofPropertyValuesHolder(this.mDragBackground, this, PropertyValuesHolder.ofInt("alpha", 255));
        this.mDragBackgroundAlphaAnim.setDuration(this.mMaterialExpandDuration);
        this.mDragBackgroundAlphaAnim.start();
    }

    @Override // com.voxel.launcher3.DropTarget
    public void onDrop(DropTarget.DragObject dragObject) {
        View viewCreateAndAddViewForRank;
        if (!this.mContent.rankOnCurrentPage(this.mEmptyCellRank)) {
            this.mTargetRank = getTargetRank(dragObject, null);
            this.mReorderAlarmListener.onAlarm(this.mReorderAlarm);
            this.mOnScrollHintAlarm.cancelAlarm();
            this.mScrollPauseAlarm.cancelAlarm();
        }
        this.mContent.completePendingPageChanges();
        ShortcutInfo shortcutInfo = this.mCurrentDragInfo;
        if (this.mIsExternalDrag) {
            viewCreateAndAddViewForRank = this.mContent.createAndAddViewForRank(shortcutInfo, this.mEmptyCellRank);
            LauncherModel.addOrMoveItemInDatabase(this.mLauncher, shortcutInfo, this.mInfo.id, 0L, shortcutInfo.cellX, shortcutInfo.cellY);
            if (dragObject.dragSource != this) {
                updateItemLocationsInDatabaseBatch();
            }
            this.mIsExternalDrag = false;
        } else {
            viewCreateAndAddViewForRank = this.mCurrentDragView;
            this.mContent.addViewForRank(viewCreateAndAddViewForRank, shortcutInfo, this.mEmptyCellRank);
        }
        if (dragObject.dragView.hasDrawn()) {
            float scaleX = getScaleX();
            float scaleY = getScaleY();
            setScaleX(1.0f);
            setScaleY(1.0f);
            this.mLauncher.getDragLayer().animateViewIntoPosition(dragObject.dragView, viewCreateAndAddViewForRank, null, null);
            setScaleX(scaleX);
            setScaleY(scaleY);
        } else {
            dragObject.deferDragViewCleanupPostAnimation = false;
            viewCreateAndAddViewForRank.setVisibility(0);
        }
        this.mItemsInvalidated = true;
        rearrangeChildren();
        this.mSuppressOnAdd = true;
        this.mInfo.add(shortcutInfo);
        this.mSuppressOnAdd = false;
        this.mCurrentDragInfo = null;
        this.mDragInProgress = false;
        if (this.mContent.getPageCount() > 1) {
            this.mInfo.setOption(4, true, this.mLauncher);
        }
    }

    @Override // com.evie.common.dragndrop.DragSource
    public void onDropCompleted(final View view, DragObjectAbstraction dragObjectAbstraction, final boolean z, final boolean z2) {
        final DropTarget.DragObject dragObject = (DropTarget.DragObject) dragObjectAbstraction;
        if (this.mDeferDropAfterUninstall) {
            Log.d("Launcher.Folder", "Deferred handling drop because waiting for uninstall.");
            this.mDeferredAction = new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Folder$JG4J-l6ilK_X-J5BP2nNgCZ6hVs
                @Override // java.lang.Runnable
                public final void run() {
                    Folder.this.lambda$onDropCompleted$4( view, dragObject, z, z2);
                }
            };
            return;
        }
        boolean z3 = z2 && (!(this.mDeferredAction != null) || this.mUninstallSuccessful);
        if (z3) {
            if (this.mDeleteFolderOnDropCompleted && !this.mItemAddedBackToSelfViaIcon && view != this) {
                replaceFolderWithFinalItem();
            }
        } else if (this.mInfo.opened) {
            onDrop(dragObject);
        } else {
            ShortcutInfo shortcutInfo = (ShortcutInfo) dragObject.dragInfo;
            View view2 = this.mCurrentDragView;
            View viewCreateNewView = (view2 == null || view2.getTag() != shortcutInfo) ? this.mContent.createNewView(shortcutInfo, true) : this.mCurrentDragView;
            ArrayList<View> itemsInReadingOrder = getItemsInReadingOrder();
            itemsInReadingOrder.add(shortcutInfo.rank, viewCreateNewView);
            this.mContent.arrangeChildren(itemsInReadingOrder, itemsInReadingOrder.size());
            this.mItemsInvalidated = true;
            this.mSuppressOnAdd = true;
            this.mFolderIcon.onDrop(dragObject);
            this.mSuppressOnAdd = false;
        }
        if (view != this && this.mOnExitAlarm.alarmPending()) {
            this.mOnExitAlarm.cancelAlarm();
            if (!z3) {
                this.mSuppressFolderDeletion = true;
            }
            this.mScrollPauseAlarm.cancelAlarm();
            completeDragExit();
        }
        this.mDeleteFolderOnDropCompleted = false;
        this.mDragInProgress = false;
        this.mItemAddedBackToSelfViaIcon = false;
        this.mCurrentDragInfo = null;
        this.mCurrentDragView = null;
        this.mSuppressOnAdd = false;
        updateItemLocationsInDatabaseBatch();
        if (getItemCount() <= this.mContent.itemsPerPage()) {
            this.mInfo.setOption(4, false, this.mLauncher);
        }
    }

    @Override // android.widget.TextView.OnEditorActionListener
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i != 6) {
            return false;
        }
        dismissEditingName();
        return true;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mContentWrapper = findViewById(R.id.folder_content_wrapper);
        this.mContent = (FolderPagedView) findViewById(R.id.folder_content);
        this.mContent.setFolder(this);
        this.mContent.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.launcher3.-$$Lambda$Folder$4TIDrwsrd9Lp48tqDNK1gkZ00w0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                Folder.this.lambda$onFinishInflate$0( view);
            }
        });
        this.mDragBackground = (GradientDrawable) getResources().getDrawable(R.drawable.folder_drag_background);
        this.mDragBackground.setAlpha(0);
        this.mContent.setBackground(this.mDragBackground);
        this.mFolderName = (ExtendedEditText) findViewById(R.id.folder_name);
        this.mFolderName.setOnBackKeyListener(new ExtendedEditText.OnBackKeyListener() { // from class: com.voxel.launcher3.-$$Lambda$Folder$d4bl1_ecylp3Aqm18syuNo1cAes
            @Override // com.voxel.launcher3.ExtendedEditText.OnBackKeyListener
            public final boolean onBackKey() {
                return Folder.this.lambda$onFinishInflate$1();
            }
        });
        this.mFolderName.setOnFocusChangeListener(this);
        this.mFolderName.measure(0, 0);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mFolderName.getLayoutParams();
        this.mFolderNameHeight = this.mFolderName.getMeasuredHeight() + marginLayoutParams.bottomMargin + marginLayoutParams.topMargin;
        this.mFolderName.setCustomSelectionActionModeCallback(this.mActionModeCallback);
        this.mFolderName.setOnEditorActionListener(this);
        this.mFolderName.setSelectAllOnFocus(true);
        ExtendedEditText extendedEditText = this.mFolderName;
        extendedEditText.setInputType(extendedEditText.getInputType() | 524288 | 8192);
        this.mFooter = findViewById(R.id.folder_footer);
        this.mFooter.measure(0, 0);
        this.mFooterHeight = this.mFooter.getMeasuredHeight();
    }

    @Override // com.voxel.launcher3.DropTarget
    public void onFlingToDelete(DropTarget.DragObject dragObject, PointF pointF) {
    }

    @Override // com.evie.common.dragndrop.DragSource
    public void onFlingToDeleteCompleted() {
    }

    @Override // android.view.View.OnFocusChangeListener
    public void onFocusChange(View view, boolean z) {
        if (view == this.mFolderName && z) {
            if (!SettingsStorage.getInstance().isDesktopLockedCurrently()) {
                startEditingFolderName();
                return;
            }
            view.clearFocus();
            this.mContent.requestFocus();
            Launcher launcher = this.mLauncher;
            if (launcher != null) {
                launcher.pushStackFragment(new LockOverlayFragment());
            }
        }
    }

    public void onFolderIconAttachedToWindow() {
        DragController dragController = this.mDragController;
        if (dragController != null) {
            dragController.addDragListener(this);
        }
    }

    public void onFolderIconDetachedFromWindow() {
        DragController dragController = this.mDragController;
        if (dragController != null) {
            dragController.removeDragListener(this);
        }
    }

    @Override // com.voxel.launcher3.FolderInfo.FolderListener
    public void onItemsChanged() {
        updateTextViewFocus();
    }

    @Override // android.view.View.OnLongClickListener
    public boolean onLongClick(View view) {
        if (this.mLauncher.isDraggingEnabled()) {
            return beginDrag(view, false);
        }
        return true;
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        int contentAreaWidth = getContentAreaWidth();
        int contentAreaHeight = getContentAreaHeight();
        int iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec(contentAreaWidth, 1073741824);
        int iMakeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(contentAreaHeight, 1073741824);
        this.mContent.setFixedSize(contentAreaWidth, contentAreaHeight);
        this.mContentWrapper.measure(iMakeMeasureSpec, iMakeMeasureSpec2);
        if (this.mContent.getChildCount() > 0) {
            int cellWidth = (this.mContent.getPageAt(0).getCellWidth() - this.mCellParams.iconSizePx) / 2;
            this.mFooter.setPadding(this.mContent.getPaddingLeft() + cellWidth, this.mFooter.getPaddingTop(), this.mContent.getPaddingRight() + cellWidth, this.mFooter.getPaddingBottom());
        }
        int size = View.MeasureSpec.getSize(i);
        this.mFolderName.measure(View.MeasureSpec.makeMeasureSpec(size, 1073741824), View.MeasureSpec.makeMeasureSpec(this.mFolderNameHeight, Integer.MIN_VALUE));
        this.mFooter.measure(iMakeMeasureSpec, View.MeasureSpec.makeMeasureSpec(this.mFooterHeight, 1073741824));
        setMeasuredDimension(size, Math.max(getMinimumHeight(), getFolderHeight(contentAreaHeight)));
    }

    @Override // com.voxel.launcher3.FolderInfo.FolderListener
    public void onRemove(ShortcutInfo shortcutInfo) {
        this.mItemsInvalidated = true;
        if (shortcutInfo == this.mCurrentDragInfo) {
            return;
        }
        this.mContent.removeItem(getViewForInfo(shortcutInfo));
        if (this.mState == 1) {
            this.mRearrangeOnClose = true;
        } else {
            rearrangeChildren();
        }
        if (getItemCount() > 1 || this.mInfo.opened) {
            return;
        }
        replaceFolderWithFinalItem();
    }

    @Override // com.voxel.launcher3.FolderInfo.FolderListener
    public void onTitleChanged(CharSequence charSequence) {
    }

    @Override // android.view.View
    @SuppressLint({"ClickableViewAccessibility"})
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return false;
    }

    public void rearrangeChildren() {
        rearrangeChildren(-1);
    }

    public void rearrangeChildren(int i) {
        ArrayList<View> itemsInReadingOrder = getItemsInReadingOrder();
        this.mContent.arrangeChildren(itemsInReadingOrder, Math.max(i, itemsInReadingOrder.size()));
        this.mItemsInvalidated = true;
    }

    void replaceFolderWithFinalItem() {
        Runnable runnable = new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Folder$_vvrkBhfBMQ7i--krF-Wkqgx2DE
            @Override // java.lang.Runnable
            public final void run() {
                Folder.this.lambda$replaceFolderWithFinalItem$5();
            }
        };
        View lastItem = this.mContent.getLastItem();
        if (lastItem != null) {
            this.mFolderIcon.performDestroyAnimation(lastItem, runnable);
        } else {
            runnable.run();
        }
        this.mDestroyed = true;
    }

    void sendCustomAccessibilityEvent(int i, String str) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getContext().getSystemService("accessibility");
        if (accessibilityManager.isEnabled()) {
            AccessibilityEvent accessibilityEventObtain = AccessibilityEvent.obtain(i);
            onInitializeAccessibilityEvent(accessibilityEventObtain);
            accessibilityEventObtain.getText().add(str);
            accessibilityManager.sendAccessibilityEvent(accessibilityEventObtain);
        }
    }

    public void setCellParams(CellParams cellParams) {
        this.mCellParams = cellParams;
        requestLayout();
    }

    public void setDragController(DragController dragController) {
        this.mDragController = dragController;
    }

    public void setFolderIcon(FolderIcon folderIcon) {
        this.mFolderIcon = folderIcon;
    }

    public void showItem(ShortcutInfo shortcutInfo) {
        getViewForInfo(shortcutInfo).setVisibility(0);
    }

    public void startEditingFolderName() {
        this.mFolderName.setHint("");
        this.mIsEditingName = true;
    }

    @Override // com.evie.common.dragndrop.DragSource
    public boolean supportsFlingToDelete() {
        return true;
    }

    @Override // com.evie.common.dragndrop.DragSource
    public boolean supportsRemoveDropTarget() {
        return true;
    }

    public void updateTextViewFocus() {
        View lastItem = this.mContent.getLastItem();
        if (lastItem != null) {
            this.mFolderName.setNextFocusDownId(lastItem.getId());
            this.mFolderName.setNextFocusRightId(lastItem.getId());
            this.mFolderName.setNextFocusLeftId(lastItem.getId());
            this.mFolderName.setNextFocusUpId(lastItem.getId());
        }
    }
}
