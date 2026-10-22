package com.voxel.simplesearchlauncher.appoptions;

import timber.log.Timber;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import butterknife.ButterKnife;
import com.voxel.launcher3.AppInfo;
import com.voxel.launcher3.ExtendedEditText;
import com.voxel.launcher3.ItemInfo;
import com.voxel.launcher3.Launcher;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.LauncherModel;
import com.voxel.launcher3.PendingAddItemInfo;
import com.voxel.launcher3.ShortcutInfo;
import com.voxel.launcher3.compat.LauncherAppsCompat;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.compat.UserManagerCompat;
import com.voxel.recyclerview.widget.RecyclerView;
import com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment;
import com.voxel.simplesearchlauncher.fragment.RecyclerViewCardFragment;
import com.voxel.simplesearchlauncher.lock.LockOverlayFragment;
import com.voxel.simplesearchlauncher.model.AppsInfoDataHelper;
import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.voxel.simplesearchlauncher.view.OldEmptyViewHolder;
import dagger.android.support.AndroidSupportInjection;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
public class AppOptionsFragment extends RecyclerViewCardFragment {
    private static final String TAG = "AppOptionsFragment";
    boolean hasPopped = false;
    private AppOptionsAdapter mAdapter;
    private OnFragmentInteractionListener mFragmentInteractionListener;

    ImageView mIconImageView;

    ExtendedEditText mIconLabel;
    private View mIconTextGroup;
    private ItemInfo mItemInfo;
    LocalAppsManager mLocalAppsManager;

    View mRevertButton;

    /* JADX INFO: Access modifiers changed from: private */
    class AppOptionsAdapter extends RecyclerViewCardFragment.Adapter {
        private final int mItemHeight;
        private List<Integer> mItems;

        public AppOptionsAdapter() {
            ComponentName componentName;
            this.mItems = new ArrayList();
            this.mItemHeight = AppOptionsFragment.this.getResources().getDimensionPixelSize(R.dimen.search_list_item_height);
            this.mItems.add(0);
            if (AppOptionsFragment.this.mItemInfo instanceof AppInfo) {
                componentName = ((AppInfo) AppOptionsFragment.this.mItemInfo).componentName;
            } else if (AppOptionsFragment.this.mItemInfo instanceof ShortcutInfo) {
                Intent intent = ((ShortcutInfo) AppOptionsFragment.this.mItemInfo).intent;
                ComponentName component = intent.getComponent();
                componentName = (component != null || intent.getPackage() == null) ? component : new ComponentName(intent.getPackage(), "");
            } else {
                componentName = AppOptionsFragment.this.mItemInfo instanceof PendingAddItemInfo ? ((PendingAddItemInfo) AppOptionsFragment.this.mItemInfo).componentName : null;
            }
            if (componentName != null) {
                this.mItems.add(1);
            } else {
                Timber.e(new RuntimeException("Iteminfo: " + AppOptionsFragment.this.mItemInfo));
            }
            if (AppOptionsFragment.this.mItemInfo.itemType == 0) {
                if (componentName != null && AppOptionsFragment.this.mLocalAppsManager.getApp(componentName.getPackageName(), componentName.getClassName()) != null) {
                    this.mItems.add(2);
                }
                this.mItems.add(3);
            }
        }

        public void lambda$onBindViewHolder$0(View view) {
            ComponentName componentName;
            if (AppOptionsFragment.this.mItemInfo instanceof AppInfo) {
                componentName = ((AppInfo) AppOptionsFragment.this.mItemInfo).componentName;
            } else if (AppOptionsFragment.this.mItemInfo instanceof ShortcutInfo) {
                Intent intent = ((ShortcutInfo) AppOptionsFragment.this.mItemInfo).intent;
                ComponentName component = intent.getComponent();
                componentName = (component != null || intent.getPackage() == null) ? component : new ComponentName(intent.getPackage(), "");
            } else {
                componentName = AppOptionsFragment.this.mItemInfo instanceof PendingAddItemInfo ? ((PendingAddItemInfo) AppOptionsFragment.this.mItemInfo).componentName : null;
            }
            UserHandleCompat userHandleCompatMyUserHandle = AppOptionsFragment.this.mItemInfo instanceof ItemInfo ? AppOptionsFragment.this.mItemInfo.user : UserHandleCompat.myUserHandle();
            if (componentName != null) {
                try {
                    LauncherAppsCompat launcherAppsCompat = LauncherAppsCompat.getInstance(AppOptionsFragment.this.getActivity());
                    UserManagerCompat.getInstance(AppOptionsFragment.this.getActivity());
                    launcherAppsCompat.showAppDetailsForProfile(componentName, userHandleCompatMyUserHandle);
                } catch (ActivityNotFoundException unused) {
                    Toast.makeText(AppOptionsFragment.this.getActivity(), R.string.activity_not_found, 0).show();
                    Log.e(AppOptionsFragment.TAG, "Unable to launch settings");
                } catch (SecurityException unused2) {
                    Toast.makeText(AppOptionsFragment.this.getActivity(), R.string.activity_not_found, 0).show();
                    Log.e(AppOptionsFragment.TAG, "Launcher does not have permission to launch settings");
                }
            }
        }

        public void lambda$onBindViewHolder$2(View view) {
            UserHandleCompat userHandleCompat;
            ComponentName targetComponent;
            if (AppOptionsFragment.this.mItemInfo instanceof ShortcutInfo) {
                ShortcutInfo shortcutInfo = (ShortcutInfo) AppOptionsFragment.this.mItemInfo;
                int i = shortcutInfo.flags;
                userHandleCompat = shortcutInfo.user;
                targetComponent = shortcutInfo.getTargetComponent();
                if (targetComponent == null && AppOptionsFragment.this.mItemInfo.intent.getPackage() != null) {
                    targetComponent = new ComponentName(AppOptionsFragment.this.mItemInfo.intent.getPackage(), "");
                }
            } else {
                AppInfo appInfo = (AppInfo) AppOptionsFragment.this.mItemInfo;
                int i2 = appInfo.flags;
                userHandleCompat = appInfo.user;
                targetComponent = appInfo.componentName;
            }
            if (AppOptionsFragment.this.mFragmentInteractionListener.startApplicationUninstallActivity(targetComponent, userHandleCompat)) {
                AppOptionsFragment appOptionsFragment = AppOptionsFragment.this;
                appOptionsFragment.popStackFragment(appOptionsFragment, true);
            }
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.Adapter
        public int getItemCount() {
            return this.mItems.size();
        }

        @Override // com.voxel.recyclerview.widget.SpringRecyclerView.SpringItemPositioner
        public int getItemViewHeightForPosition(int i) {
            return getItemViewHeightForViewType(getItemViewType(i));
        }

        public int getItemViewHeightForViewType(int i) {
            if (i != 0) {
                return this.mItemHeight;
            }
            int height = AppOptionsFragment.this.mRecyclerView.getHeight();
            int itemViewHeightForViewType = 0;
            for (int i2 = 1; i2 < getItemCount() && (itemViewHeightForViewType = itemViewHeightForViewType + getItemViewHeightForViewType(getItemViewType(i2))) < height; i2++) {
            }
            return Math.max(0, height - itemViewHeightForViewType);
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.Adapter
        public int getItemViewType(int i) {
            return this.mItems.get(i).intValue();
        }

        @Override // com.voxel.recyclerview.widget.SpringRecyclerView.SpringItemPositioner
        public boolean hasUniformViewHeight() {
            return false;
        }

        @Override // com.voxel.simplesearchlauncher.fragment.RecyclerViewCardFragment.Adapter
        public boolean isItemInBackground(int i) {
            return getItemViewType(i) != 0;
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.Adapter
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            int itemViewType = getItemViewType(i);
            if (itemViewType == 0) {
                return;
            }
            AppOptionsViewHolder appOptionsViewHolder = (AppOptionsViewHolder) viewHolder;
            if (i < getItemCount() - 1) {
                appOptionsViewHolder.mDivider.setVisibility(0);
            } else {
                appOptionsViewHolder.mDivider.setVisibility(4);
            }
            if (itemViewType == 1) {
                appOptionsViewHolder.mLabel.setText(R.string.app_options_app_info);
                appOptionsViewHolder.mIcon.setImageResource(R.drawable.options_info_icon);
                appOptionsViewHolder.mView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.appoptions.-$$Lambda$AppOptionsFragment$AppOptionsAdapter$v_WJMX7Eb4-IfYNu0TviIKRlkKM
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        AppOptionsAdapter.this.lambda$onBindViewHolder$0( view);
                    }
                });
                return;
            }
            if (itemViewType == 2) {
                appOptionsViewHolder.mLabel.setText(R.string.app_options_change_icon);
                appOptionsViewHolder.mIcon.setImageResource(R.drawable.options_swap_icon);
                appOptionsViewHolder.mView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.appoptions.-$$Lambda$AppOptionsFragment$AppOptionsAdapter$1xomMLmA-063eEUBsiWzx8AgAUk
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        AppOptionsFragment.AppOptionsAdapter appOptionsAdapter = AppOptionsAdapter.this;
                        AppOptionsFragment.this.pushStackFragment(ChangeIconFragment.newInstance(AppOptionsFragment.this.mItemInfo));
                    }
                });
            } else if (itemViewType == 3) {
                appOptionsViewHolder.mLabel.setText(R.string.app_options_uninstall);
                appOptionsViewHolder.mIcon.setImageResource(R.drawable.options_trash_icon);
                appOptionsViewHolder.mView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.appoptions.-$$Lambda$AppOptionsFragment$AppOptionsAdapter$xYwmy2PObsvMuEohZ9OzBpbJPRg
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        AppOptionsAdapter.this.lambda$onBindViewHolder$2( view);
                    }
                });
            } else {
                throw new IllegalArgumentException("Illegal view type! position = " + i + " viewtype = " + itemViewType);
            }
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.Adapter
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            if (i != 0) {
                return AppOptionsFragment.this.new AppOptionsViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.app_options_item, viewGroup, false));
            }
            View view = new View(viewGroup.getContext());
            RecyclerView.LayoutParams layoutParamsGenerateDefaultLayoutParams = AppOptionsFragment.this.mLayoutManager.generateDefaultLayoutParams();
            ((ViewGroup.LayoutParams) layoutParamsGenerateDefaultLayoutParams).width = -1;
            ((ViewGroup.LayoutParams) layoutParamsGenerateDefaultLayoutParams).height = getItemViewHeightForViewType(i);
            view.setLayoutParams(layoutParamsGenerateDefaultLayoutParams);
            return new OldEmptyViewHolder(view);
        }
    }

    private class AppOptionsViewHolder extends RecyclerView.ViewHolder {
        View mDivider;
        ImageView mIcon;
        TextView mLabel;
        View mView;

        public AppOptionsViewHolder(View view) {
            super(view);
            this.mView = view;
            this.mIcon = (ImageView) view.findViewById(R.id.icon);
            this.mLabel = (TextView) view.findViewById(R.id.label);
            this.mDivider = view.findViewById(R.id.list_divider);
        }
    }

    public interface OnFragmentInteractionListener {
        void hideBlurImage();

        void showBlurImage();

        boolean startApplicationUninstallActivity(ComponentName componentName, UserHandleCompat userHandleCompat);
    }

    private void doneEditingName(boolean z) {
        String string;
        if (getContext() == null) {
            return;
        }
        this.mIconLabel.clearFocus();
        ((InputMethodManager) this.mIconLabel.getContext().getSystemService("input_method")).hideSoftInputFromWindow(this.mIconLabel.getWindowToken(), 0);
        this.mRevertButton.setVisibility(8);
        this.mIconLabel.setHint(R.string.unnamed_hint_text);
        if (z) {
            string = null;
        } else {
            string = this.mIconLabel.getText() == null ? "" : this.mIconLabel.getText().toString();
        }
        if (this.mItemInfo.itemType != 0) {
            ItemInfo itemInfo = this.mItemInfo;
            if (itemInfo instanceof ShortcutInfo) {
                itemInfo.title = string;
                LauncherModel.updateItemInDatabase(getContext(), this.mItemInfo);
                ArrayList<ShortcutInfo> arrayList = new ArrayList<>();
                arrayList.add((ShortcutInfo) this.mItemInfo);
                ((Launcher) getContext()).bindShortcutsChanged(arrayList, new ArrayList<>(), UserHandleCompat.myUserHandle());
            }
        }
        if (this.mItemInfo.itemType != 0 || this.mItemInfo.getTargetComponent() == null) {
            return;
        }
        ComponentName targetComponent = this.mItemInfo.getTargetComponent();
        AppsInfoDataHelper appsInfoDataHelper = new AppsInfoDataHelper(getContext());
        AppsInfoDataHelper.AppInfo app = appsInfoDataHelper.getApp(targetComponent.getPackageName(), targetComponent.getClassName());
        if (app != null) {
            app.labelOverride = string;
            appsInfoDataHelper.update(app);
            if (z) {
                this.mIconLabel.setText(app.appLabel);
            }
        }
        LauncherAppState.getInstance().getModel().forceReload();
    }

    public void lambda$onViewCreated$0(View view, View view2, boolean z) {
        if (view2 == this.mIconLabel && z) {
            if (!SettingsStorage.getInstance().isDesktopLockedCurrently()) {
                this.startEditingName();
                return;
            }
            view2.clearFocus();
            view.requestFocus();
            if (this.getActivity() == null || !(this.getActivity() instanceof Launcher)) {
                return;
            }
            ((Launcher) this.getActivity()).pushStackFragment(new LockOverlayFragment());
        }
    }

    public boolean lambda$onViewCreated$1() {
        this.doneEditingName(false);
        return true;
    }

    public boolean lambda$onViewCreated$2(TextView textView, int i, KeyEvent keyEvent) {
        if (i != 6) {
            return false;
        }
        this.doneEditingName(false);
        return true;
    }

    public static AppOptionsFragment newInstance(ItemInfo itemInfo) {
        AppOptionsFragment appOptionsFragment = new AppOptionsFragment();
        appOptionsFragment.mItemInfo = itemInfo;
        return appOptionsFragment;
    }

    private void startEditingName() {
        this.mIconLabel.setHint("");
        if (this.mItemInfo.itemType == 0) {
            this.mRevertButton.setVisibility(0);
        }
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public boolean canSaveInstanceState() {
        return false;
    }

    @Override // com.voxel.simplesearchlauncher.fragment.SpringCardFragment, com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void focusFragment(boolean z) {
        if (getView() != null) {
            getView().animate().alpha(1.0f).withLayer();
        }
    }

    @Override // com.voxel.simplesearchlauncher.fragment.RecyclerViewCardFragment
    protected RecyclerViewCardFragment.Adapter getAdapter() {
        return this.mAdapter;
    }

    @Override // com.voxel.simplesearchlauncher.fragment.SpringCardFragment
    protected float getBackgroundWashAlphaFactor() {
        return 0.0f;
    }

    @Override // com.voxel.simplesearchlauncher.fragment.SpringCardFragment
    protected float getMaxBackgroundWashAlpha() {
        return 0.0f;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment, androidx.fragment.app.Fragment
    public void onAttach(Activity activity) {
        AndroidSupportInjection.inject(this);
        super.onAttach(activity);
        try {
            this.mFragmentInteractionListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException unused) {
            throw new ClassCastException(activity.toString() + " must implement " + OnFragmentInteractionListener.class.getCanonicalName());
        }
    }

    public void onCloseClick() {
        doneEditingName(true);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.RecyclerViewCardFragment, androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mAdapter = new AppOptionsAdapter();
    }

    @Override // com.voxel.simplesearchlauncher.fragment.RecyclerViewCardFragment, com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    protected void onFragmentWillClose() {
        super.onFragmentWillClose();
        if (getView() != null) {
            getView().animate().cancel();
        }
        this.mIconTextGroup.animate().alpha(0.0f).setDuration(100L);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.voxel.simplesearchlauncher.fragment.SpringCardFragment, com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void onPopLauncherStackedAppearance(FragmentActivity fragmentActivity, boolean z) {
        if (this.hasPopped || !(fragmentActivity instanceof OnFragmentInteractionListener)) {
            return;
        }
        ((OnFragmentInteractionListener) fragmentActivity).hideBlurImage();
        this.hasPopped = true;
    }

    @Override // com.voxel.simplesearchlauncher.fragment.SpringCardFragment, com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void onPushLauncherStackedAppearance(LauncherStackableFragment.OnFragmentInteractionListener onFragmentInteractionListener) {
        if (onFragmentInteractionListener instanceof OnFragmentInteractionListener) {
            ((OnFragmentInteractionListener) onFragmentInteractionListener).showBlurImage();
        }
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void onStackableFragmentResult(int i, int i2, Intent intent) {
        if (i2 == -1) {
            notifyFragmentWillRequestClose();
            notifyFragmentRequestClose();
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(final View view, Bundle bundle) {
        this.mBackground.setVisibility(8);
        this.mIconTextGroup = LayoutInflater.from(view.getContext()).inflate(R.layout.app_options_icon_text, (ViewGroup) view, false);
        ButterKnife.bind(this, this.mIconTextGroup);
        ItemInfo itemInfo = this.mItemInfo;
        if (itemInfo instanceof AppInfo) {
            this.mIconImageView.setImageBitmap(((AppInfo) itemInfo).iconBitmap);
        } else if (itemInfo instanceof ShortcutInfo) {
            this.mIconImageView.setImageBitmap(((ShortcutInfo) itemInfo).iconBitmap);
        }
        this.mIconLabel.setText(this.mItemInfo.title);
        this.mIconLabel.setOnFocusChangeListener(new View.OnFocusChangeListener() { // from class: com.voxel.simplesearchlauncher.appoptions.-$$Lambda$AppOptionsFragment$0w2cghh3PdQoMsFTybABnUpbLWM
            @Override // android.view.View.OnFocusChangeListener
            public final void onFocusChange(View view2, boolean z) {
                AppOptionsFragment.this.lambda$onViewCreated$0( view, view2, z);
            }
        });
        this.mIconLabel.setOnBackKeyListener(new ExtendedEditText.OnBackKeyListener() { // from class: com.voxel.simplesearchlauncher.appoptions.-$$Lambda$AppOptionsFragment$xsfo_tOHzSMPUlDU5Tie5uZ9ueo
            @Override // com.voxel.launcher3.ExtendedEditText.OnBackKeyListener
            public final boolean onBackKey() {
                return AppOptionsFragment.this.lambda$onViewCreated$1();
            }
        });
        this.mIconLabel.setCustomSelectionActionModeCallback(new ActionMode.Callback() { // from class: com.voxel.simplesearchlauncher.appoptions.AppOptionsFragment.1
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
        });
        this.mIconLabel.setOnEditorActionListener(new TextView.OnEditorActionListener() { // from class: com.voxel.simplesearchlauncher.appoptions.-$$Lambda$AppOptionsFragment$y7lRW0EIAHHQNwh7QaozzXKwrbg
            @Override // android.widget.TextView.OnEditorActionListener
            public final boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return AppOptionsFragment.this.lambda$onViewCreated$2( textView, i, keyEvent);
            }
        });
        this.mIconLabel.setSelectAllOnFocus(true);
        ExtendedEditText extendedEditText = this.mIconLabel;
        extendedEditText.setInputType(extendedEditText.getInputType() | 524288 | 8192);
        ((FrameLayout) view).addView(this.mIconTextGroup);
        this.mIconTextGroup.setAlpha(0.0f);
        this.mIconTextGroup.animate().alpha(1.0f);
        this.mBackground.setVisibility(8);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public boolean requiresFullScreen() {
        return true;
    }

    @Override // com.voxel.simplesearchlauncher.fragment.SpringCardFragment, com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void unfocusFragment(boolean z) {
        if (getView() != null) {
            getView().animate().alpha(0.0f).withLayer();
        }
    }

    @Override // com.voxel.simplesearchlauncher.fragment.RecyclerViewCardFragment
    protected boolean usesBackgroundCard() {
        return false;
    }
}
