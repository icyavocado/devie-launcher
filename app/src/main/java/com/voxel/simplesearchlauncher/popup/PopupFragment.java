package com.voxel.simplesearchlauncher.popup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.StatusBarNotification;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.evie.common.dragndrop.DragObjectAbstraction;
import com.evie.common.dragndrop.DragSource;
import com.voxel.launcher3.BubbleTextView;
import com.voxel.launcher3.DropTarget;
import com.voxel.launcher3.FolderInfo;
import com.voxel.launcher3.IconCache;
import com.voxel.launcher3.ItemInfo;
import com.voxel.launcher3.Launcher;
import com.voxel.launcher3.LauncherAppWidgetHost;
import com.voxel.launcher3.LauncherAppWidgetHostView;
import com.voxel.launcher3.LauncherAppWidgetInfo;
import com.voxel.launcher3.LauncherModel;
import com.voxel.launcher3.ShortcutInfo;
import com.voxel.launcher3.Utilities;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.dragndrop.DragController;
import com.voxel.launcher3.dragndrop.DragOptions;
import com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat;
import com.voxel.launcher3.shortcuts.DeepShortcutManager;
import com.voxel.launcher3.shortcuts.ShortcutDragPreviewProvider;
import com.voxel.launcher3.shortcuts.ShortcutFilter;
import com.voxel.launcher3.util.ComponentKey;
import com.voxel.simplesearchlauncher.appoptions.AppOptionsFragment;
import com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.voxel.simplesearchlauncher.notification.NotificationContent;
import com.voxel.simplesearchlauncher.notification.NotificationController;
import com.voxel.simplesearchlauncher.notification.NotificationKeyData;
import com.voxel.simplesearchlauncher.notification.Notifier;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import timber.log.Timber;

/* JADX INFO: loaded from: classes2.dex */
public class PopupFragment extends LauncherStackableFragment implements PopupContainerWithArrow.ActionHandler {

    View mBackgroundWash;
    DeepShortcutManager mDeepShortcutManager;
    private View mDeferredDragIcon;
    private DragController mDragController;
    private ItemInfo mItemInfo;
    private View mItemView;
    Launcher mLauncher;
    private DragSource mOriginalDragSource;

    PopupContainerWithArrow mPopupContainer;
    private View mSourceView;
    private int mStartDragThreshold;
    private Handler mUiHandler;
    private View.OnClickListener mNotificationOnClickListener = new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.popup.-$$Lambda$PopupFragment$xOrCwN7C03Xz7r31aFMUoxQC97g
        @Override // android.view.View.OnClickListener
        public final void onClick(View view) {
            PopupFragment.this.lambda$new$2( view);
        }
    };
    private View.OnClickListener mShortcutOnClickListener = new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.popup.-$$Lambda$PopupFragment$uiC1aOgw_fKyjMoaU6huPbfW4Yc
        @Override // android.view.View.OnClickListener
        public final void onClick(View view) {
            PopupFragment.this.lambda$new$3( view);
        }
    };
    private DragController.DragListener mDragListener = new DragController.DragListener() { // from class: com.voxel.simplesearchlauncher.popup.PopupFragment.2
        @Override // com.voxel.launcher3.dragndrop.DragController.DragListener
        public void onDragEnd() {
            if (PopupFragment.this.mDeferredDragIcon != null) {
                PopupFragment.this.mDeferredDragIcon.setVisibility(0);
                PopupFragment.this.mDeferredDragIcon = null;
            }
            PopupFragment.this.mDragController.removeDragListener(this);
        }

        @Override // com.voxel.launcher3.dragndrop.DragController.DragListener
        public void onDragStart(DropTarget.DragObject dragObject, DragOptions dragOptions) {
            PopupFragment.this.runExitAnimation();
        }
    };
    private DragSource mDragSource = new DragSource() { // from class: com.voxel.simplesearchlauncher.popup.PopupFragment.3
        @Override // com.evie.common.dragndrop.DragSource
        public float getIntrinsicIconScaleFactor() {
            return 1.0f;
        }

        @Override // com.evie.common.dragndrop.DragSource
        public void onDropCompleted(View view, DragObjectAbstraction dragObjectAbstraction, boolean z, boolean z2) {
            DropTarget.DragObject dragObject = (DropTarget.DragObject) dragObjectAbstraction;
            if (z2) {
                return;
            }
            dragObject.setDeferDragViewCleanupPostAnimation(false);
            PopupFragment.this.mLauncher.showWorkspace(true);
        }

        @Override // com.evie.common.dragndrop.DragSource
        public void onFlingToDeleteCompleted() {
        }

        @Override // com.evie.common.dragndrop.DragSource
        public boolean supportsFlingToDelete() {
            return false;
        }

        @Override // com.evie.common.dragndrop.DragSource
        public boolean supportsRemoveDropTarget() {
            return false;
        }
    };
    private View.OnLongClickListener mShortcutOnLongClickListener = new View.OnLongClickListener() { // from class: com.voxel.simplesearchlauncher.popup.-$$Lambda$PopupFragment$zck-5hps8jAII1oY6Fae8d8l0P4
        @Override // android.view.View.OnLongClickListener
        public final boolean onLongClick(View view) {
            return PopupFragment.this.lambda$new$5( view);
        }
    };
    private BroadcastReceiver mNotificationDismissedReceiver = new BroadcastReceiver() { // from class: com.voxel.simplesearchlauncher.popup.PopupFragment.5
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (PopupFragment.this.mItemInfo == null || PopupFragment.this.mItemInfo.getIntent() == null || PopupFragment.this.mItemInfo.getIntent().getComponent() == null) {
                return;
            }
            String packageName = PopupFragment.this.mItemInfo.getIntent().getComponent().getPackageName();
            List<NotificationKeyData> notificationKeys = Notifier.getInstance(context).getNotificationKeys(packageName);
            ArrayList arrayList = new ArrayList(notificationKeys.size());
            Iterator<NotificationKeyData> it = notificationKeys.iterator();
            while (it.hasNext()) {
                arrayList.add(it.next().notificationKey);
            }
            PopupFragment.this.mPopupContainer.trimNotifications(PopupFragment.this.mUiHandler, arrayList, Notifier.getInstance(PopupFragment.this.getContext()).getCount(packageName));
        }
    };

    public enum Item {
        SHORTCUT(R.layout.popup_deep_shortcut_item, true),
        NOTIFICATION(R.layout.popup_notification_item, false),
        SYSTEM_SHORTCUT(R.layout.popup_deep_shortcut_item, true),
        SYSTEM_SHORTCUT_ICON(R.layout.popup_system_shortcut_icon_item, true);

        public final boolean isShortcut;
        public final int layoutId;

        Item(int i, boolean z) {
            this.layoutId = i;
            this.isShortcut = z;
        }
    }

    static class UnbadgedShortcutInfo extends ShortcutInfo {
        public final DeepShortcutInfoCompat mDetail;

        public UnbadgedShortcutInfo(DeepShortcutInfoCompat deepShortcutInfoCompat, DeepShortcutManager deepShortcutManager, Context context) {
            super(deepShortcutInfoCompat, deepShortcutManager, context);
            this.mDetail = deepShortcutInfoCompat;
        }

        @Override // com.voxel.launcher3.ShortcutInfo
        protected Bitmap getBadgedIcon(Bitmap bitmap, DeepShortcutInfoCompat deepShortcutInfoCompat, IconCache iconCache, Context context) {
            return bitmap;
        }
    }

    public PopupFragment() {
    }

    @SuppressLint({"ValidFragment"})
    public PopupFragment(View view, ItemInfo itemInfo, DragSource dragSource) {
        this.mSourceView = view;
        this.mItemInfo = itemInfo;
        this.mOriginalDragSource = dragSource;
    }

    public static boolean canProperlyShow(ItemInfo itemInfo) {
        return true;
    }

    private void deferDrag(View view) {
        this.mDeferredDragIcon = view;
        this.mDragController = this.mLauncher.getDragController();
        this.mDragController.addDragListener(this.mDragListener);
    }

    private void deleteItem(ItemInfo itemInfo) {
        if (isWorkspaceOrFolderApplication(itemInfo)) {
            LauncherModel.deleteItemFromDatabase(this.mLauncher, itemInfo);
            this.mLauncher.removeViewForItemInfo(itemInfo);
            return;
        }
        if (isWorkspaceFolder(itemInfo)) {
            FolderInfo folderInfo = (FolderInfo) itemInfo;
            this.mLauncher.removeFolder(folderInfo);
            this.mLauncher.removeViewForItemInfo(itemInfo);
            LauncherModel.deleteFolderContentsFromDatabase(this.mLauncher, folderInfo);
            return;
        }
        if (isWorkspaceOrFolderWidget(itemInfo)) {
            final LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) itemInfo;
            this.mLauncher.removeAppWidget(launcherAppWidgetInfo);
            this.mLauncher.removeViewForItemInfo(itemInfo);
            LauncherModel.deleteItemFromDatabase(this.mLauncher, itemInfo);
            final LauncherAppWidgetHost appWidgetHost = this.mLauncher.getAppWidgetHost();
            if (appWidgetHost == null || !launcherAppWidgetInfo.isWidgetIdValid()) {
                return;
            }
            Completable.fromAction(new Action() { // from class: com.voxel.simplesearchlauncher.popup.-$$Lambda$PopupFragment$ropvpEB7RUnr4KlqhfJHfliYp4M
                @Override // io.reactivex.functions.Action
                public final void run() throws Exception {
                    PopupFragment.lambda$deleteItem$4(launcherAppWidgetInfo, appWidgetHost);
                }
            }).subscribeOn(Schedulers.io()).subscribe();
        }
    }

    private boolean isDragSourceWorkspaceOrFolder(ItemInfo itemInfo) {
        return itemInfo.container != -1;
    }

    private boolean isWorkspaceFolder(ItemInfo itemInfo) {
        return isDragSourceWorkspaceOrFolder(itemInfo) && (itemInfo instanceof FolderInfo);
    }

    private boolean isWorkspaceOrFolderApplication(ItemInfo itemInfo) {
        return isDragSourceWorkspaceOrFolder(itemInfo) && (itemInfo instanceof ShortcutInfo);
    }

    private boolean isWorkspaceOrFolderWidget(ItemInfo itemInfo) {
        return isDragSourceWorkspaceOrFolder(itemInfo) && (itemInfo instanceof LauncherAppWidgetInfo);
    }

    static /* synthetic */ void lambda$deleteItem$4(LauncherAppWidgetInfo launcherAppWidgetInfo, LauncherAppWidgetHost launcherAppWidgetHost) throws Exception {
        Timber.d("================= Widget deleted " + launcherAppWidgetInfo.appWidgetId, new Object[0]);
        launcherAppWidgetHost.deleteAppWidgetId(launcherAppWidgetInfo.appWidgetId);
    }

    public void lambda$new$2(View view) {
        NotificationContent notificationContent = ((NotificationItemView) view).getNotificationContent();
        try {
            notificationContent.intent.send();
        } catch (PendingIntent.CanceledException unused) {
        }
        if (notificationContent.autoCancel) {
            NotificationController.getInstance(this.getContext()).dismissNotification(notificationContent.notificationKey);
        }
        this.runExitAnimation();
    }

    public void lambda$new$3(View view) {
        SystemShortcut systemShortcut;
        UnbadgedShortcutInfo unbadgedShortcutInfo = null;
        if (view instanceof ShortcutItemView) {
            ShortcutItemView shortcutItemView = (ShortcutItemView) view;
            unbadgedShortcutInfo = shortcutItemView.mInfo;
            systemShortcut = shortcutItemView.mSystemShortcut;
        } else {
            Object tag = view.getTag();
            systemShortcut = (tag == null || !(tag instanceof SystemShortcut)) ? null : (SystemShortcut) tag;
        }
        if (unbadgedShortcutInfo != null) {
            this.mLauncher.onClick(view);
            this.dismissWithoutAnimation();
        }
        if (systemShortcut == null) {
            this.dismissWithoutAnimation();
            return;
        }
        switch (systemShortcut) {
            case REMOVE:
                this.runExitAnimation();
                this.deleteItem(this.mItemInfo);
                this.mLauncher.getWorkspace().removeExtraEmptyScreen(true, true);
                break;
            case RESIZE:
                this.runExitAnimation();
                this.mLauncher.getDragLayer().addResizeFrame(this.mItemInfo, (LauncherAppWidgetHostView) this.mSourceView, this.mLauncher.getWorkspace().getCurrentDropLayout());
                break;
            case APP_OPTIONS:
                this.dismissWithoutAnimation();
                AppOptionsFragment appOptionsFragmentNewInstance = AppOptionsFragment.newInstance(this.mItemInfo);
                if (appOptionsFragmentNewInstance != null) {
                    this.mLauncher.pushStackFragment(appOptionsFragmentNewInstance);
                }
                break;
        }
    }

    public boolean lambda$new$5(View view) {
        ShortcutItemView shortcutItemView = (ShortcutItemView) view;
        if (!shortcutItemView.isInTouchMode() || shortcutItemView.mInfo == null || !this.mLauncher.isDraggingEnabled() || this.mLauncher.getDragController().isDragging()) {
            return false;
        }
        this.startShortcutDrag(shortcutItemView);
        return false;
    }

    public void lambda$onViewCreated$1(Notifier notifier, List list, ComponentName componentName, Context context, List list2, UserHandleCompat userHandleCompat, List list3) {
        List<StatusBarNotification> statusBarNotificationsForKeys = notifier.getStatusBarNotificationsForKeys(list);
        ArrayList arrayList = new ArrayList();
        int count = componentName != null ? notifier.getCount(componentName.getPackageName(), componentName.getClassName()) : 0;
        String shortcutId = null;
        for (StatusBarNotification statusBarNotification : statusBarNotificationsForKeys) {
            if (Utilities.isAtLeastO() && statusBarNotification.getNotification().getShortcutId() != null) {
                shortcutId = statusBarNotification.getNotification().getShortcutId();
            }
            arrayList.add(new NotificationContent(context, statusBarNotification));
        }
        ArrayList arrayList2 = new ArrayList();
        if (componentName != null) {
            arrayList2.addAll(ShortcutFilter.sortAndFilterShortcuts(this.mDeepShortcutManager.queryForShortcutsContainer(componentName, list2, userHandleCompat), shortcutId));
        }
        this.mPopupContainer.updateNotifications(this.mUiHandler, arrayList, count);
        this.mPopupContainer.updateShortcuts(this.mUiHandler, this.mDeepShortcutManager, arrayList2, list3);
    }

    private void startShortcutDrag(ShortcutItemView shortcutItemView) {
        this.mDragController.addDragListener(this.mDragListener);
        shortcutItemView.setIconVisible(false);
        Point point = new Point();
        point.x = shortcutItemView.mIconLastTouchPos.x - shortcutItemView.getIconCenter().x;
        point.y = shortcutItemView.mIconLastTouchPos.y - shortcutItemView.getIconView().getHeight();
        DragOptions dragOptions = new DragOptions();
        dragOptions.isDesktopLocked = SettingsStorage.getInstance().isDesktopLockedCurrently();
        this.mLauncher.getWorkspace().beginDragShared(shortcutItemView.getIconView(), this.mDragSource, shortcutItemView.getFinalInfo(), new ShortcutDragPreviewProvider(shortcutItemView.getIconView(), point), dragOptions).animateShiftFrom(-point.x, -point.y);
    }

    public DragOptions.DeferDragCondition createDeferDragCondition(final Runnable runnable) {
        return new DragOptions.DeferDragCondition() { // from class: com.voxel.simplesearchlauncher.popup.PopupFragment.1
            @Override // com.voxel.launcher3.dragndrop.DragOptions.DeferDragCondition
            public void onDeferredDragStart() {
                if (PopupFragment.this.mDeferredDragIcon != null) {
                    PopupFragment.this.mDeferredDragIcon.setVisibility(4);
                }
            }

            @Override // com.voxel.launcher3.dragndrop.DragOptions.DeferDragCondition
            public void onDragStart() {
                Runnable runnable2 = runnable;
                if (runnable2 != null) {
                    runnable2.run();
                }
            }

            @Override // com.voxel.launcher3.dragndrop.DragOptions.DeferDragCondition
            public void onDropBeforeDeferredDrag() {
            }

            @Override // com.voxel.launcher3.dragndrop.DragOptions.DeferDragCondition
            public boolean shouldStartDeferredDrag(double d) {
                return d > ((double) PopupFragment.this.mStartDragThreshold);
            }
        };
    }

    public void dismissWithoutAnimation() {
        notifyFragmentWillRequestClose();
        notifyFragmentRequestClose();
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void focusFragment(boolean z) {
    }

    public List<Item> getItemsToPopulate(List<String> list, List<SystemShortcut> list2, List<NotificationKeyData> list3) {
        ArrayList arrayList = new ArrayList();
        int iMin = Math.min(list.size(), 4);
        boolean z = list3.size() > 0;
        if (z && iMin > 2) {
            iMin = 2;
        }
        if (z) {
            arrayList.add(Item.NOTIFICATION);
        }
        for (int i = 0; i < iMin; i++) {
            arrayList.add(Item.SHORTCUT);
        }
        boolean z2 = !list.isEmpty();
        for (SystemShortcut systemShortcut : list2) {
            arrayList.add(z2 ? Item.SYSTEM_SHORTCUT_ICON : Item.SYSTEM_SHORTCUT);
        }
        return arrayList;
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment, androidx.fragment.app.Fragment
    public void onAttach(Activity activity) {
        AndroidSupportInjection.inject(this);
        super.onAttach(activity);
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mStartDragThreshold = getResources().getDimensionPixelSize(R.dimen.deep_shortcuts_start_drag_threshold);
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.mItemView = layoutInflater.inflate(R.layout.popup_fragment, viewGroup, false);
        return this.mItemView;
    }

    @Override // androidx.fragment.app.Fragment
    public void onDetach() {
        super.onDetach();
        this.mDragController.removeDragListener(this.mDragListener);
        View view = this.mSourceView;
        if (view instanceof BubbleTextView) {
            ((BubbleTextView) view).setBadgeEnabled(true);
        }
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void onPopLauncherStackedAppearance(FragmentActivity fragmentActivity, boolean z) {
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void onPushLauncherStackedAppearance(LauncherStackableFragment.OnFragmentInteractionListener onFragmentInteractionListener) {
    }

    @Override // androidx.fragment.app.Fragment
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(this.mNotificationDismissedReceiver, new IntentFilter("com.evie.notification_dismissed"));
    }

    @Override // androidx.fragment.app.Fragment
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(this.mNotificationDismissedReceiver);
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        if (getContext() == null || getActivity() == null) {
            throw new RuntimeException();
        }
        this.mPopupContainer = (PopupContainerWithArrow) view.findViewById(R.id.popup_container);
        this.mBackgroundWash = view.findViewById(R.id.background_wash);
        this.mLauncher = (Launcher) getActivity();
        this.mBackgroundWash.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.popup.-$$Lambda$PopupFragment$ECUxpG8xO8JgDH2Tl5QuJM6QRds
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                PopupFragment.this.runExitAnimation();
            }
        });
        deferDrag(this.mSourceView);
        View view2 = this.mSourceView;
        if (view2 instanceof BubbleTextView) {
            ((BubbleTextView) view2).setBadgeEnabled(false);
        }
        ItemInfo itemInfo = this.mItemInfo;
        if (itemInfo == null) {
            this.mDragController.removeDragListener(this.mDragListener);
            dismissWithoutAnimation();
            return;
        }
        Intent intent = itemInfo.getIntent();
        final ComponentName component = intent != null ? intent.getComponent() : null;
        final ArrayList arrayList = new ArrayList();
        if (this.mOriginalDragSource.supportsRemoveDropTarget()) {
            arrayList.add(SystemShortcut.REMOVE);
        }
        View view3 = this.mSourceView;
        if ((view3 instanceof LauncherAppWidgetHostView) && ((LauncherAppWidgetHostView) view3).getAppWidgetInfo() != null) {
            arrayList.add(SystemShortcut.RESIZE);
        }
        if (intent != null) {
            arrayList.add(SystemShortcut.APP_OPTIONS);
        }
        final ArrayList arrayList2 = new ArrayList();
        final ArrayList arrayList3 = new ArrayList();
        if (this.mItemInfo.itemType == 0) {
            arrayList2.addAll(this.mDeepShortcutManager.getShortcutIdsForComponentKey(new ComponentKey(this.mItemInfo.getTargetComponent(), this.mItemInfo.user)));
            arrayList3.addAll(Notifier.getInstance(getContext()).getNotificationKeys(this.mItemInfo.getTargetComponent().getPackageName()));
        }
        this.mPopupContainer.populateAndShow(this, this.mSourceView, this.mLauncher.getDragLayer(), getItemsToPopulate(arrayList2, arrayList, arrayList3), arrayList3.size());
        Looper workerLooper = LauncherModel.getWorkerLooper();
        this.mUiHandler = new Handler(Looper.getMainLooper());
        final UserHandleCompat userHandleCompat = this.mItemInfo.user;
        final Context context = getContext();
        final Notifier notifier = Notifier.getInstance(context);
        new Handler(workerLooper).postAtFrontOfQueue(new Runnable() { // from class: com.voxel.simplesearchlauncher.popup.-$$Lambda$PopupFragment$UdkXyvZalNfhaAtNif0t0ULpyUI
            @Override // java.lang.Runnable
            public final void run() {
                PopupFragment.this.lambda$onViewCreated$1( notifier, arrayList3, component, context, arrayList2, userHandleCompat, arrayList);
            }
        });
    }

    @Override // com.voxel.simplesearchlauncher.popup.PopupContainerWithArrow.ActionHandler
    public void registerNotificationActions(NotificationItemView notificationItemView) {
        notificationItemView.setOnClickListener(this.mNotificationOnClickListener);
    }

    @Override // com.voxel.simplesearchlauncher.popup.PopupContainerWithArrow.ActionHandler
    public void registerShortcutActions(View view) {
        view.setOnClickListener(this.mShortcutOnClickListener);
        if (view instanceof ShortcutItemView) {
            ((ShortcutItemView) view).setOnLongClickListener(this.mShortcutOnLongClickListener);
        }
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void runExitAnimation() {
        notifyFragmentWillRequestClose();
        this.mPopupContainer.animateClose(new AnimatorListenerAdapter() { // from class: com.voxel.simplesearchlauncher.popup.PopupFragment.4
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                PopupFragment.this.notifyFragmentRequestClose();
            }
        });
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void unfocusFragment(boolean z) {
    }
}
