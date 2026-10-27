package com.voxel.simplesearchlauncher.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.ovacoda.devie.images.IconLoader;
import com.voxel.launcher3.DeviceProfile;
import com.voxel.launcher3.FolderInfo;
import com.voxel.launcher3.ItemInfo;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.LauncherModel;
import com.voxel.launcher3.ShortcutInfo;
import com.voxel.launcher3.Utilities;
import com.voxel.simplesearchlauncher.iconpack.IconPack;
import com.voxel.simplesearchlauncher.iconpack.IconPackManager;
import com.voxel.simplesearchlauncher.model.itemdata.LocalAppData;
import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.voxel.simplesearchlauncher.utils.LaunchUtil;
import com.voxel.simplesearchlauncher.view.MockLauncherController;
import dagger.android.support.AndroidSupportInjection;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes2.dex */
public class IconPackSettingsFragment extends Fragment {
    IconPack mChosenPack;
    LinearLayout mContainer;
    private DeviceProfile mDeviceProfile;
    private MockLauncherController.MockLauncherHolder mFragmentInteractionListener;
    private List<ItemInfo> mHotseatItems;
    private SettingsSwitchViewHolder mIconNormalizationSwitch;
    IconPackManager mIconPackManager;
    List<IconPack> mIconPacks;
    View.OnClickListener mItemClickListener;
    LocalAppsManager mLocalAppsManager;
    private View mMockLauncher;
    private MockLauncherController mMockLauncherController;
    private int mMockLauncherStartTranslationY;
    private NestedScrollView mScrollView;
    Map<IconPack, IconPackViewHolder> mViewHolderMap = new HashMap();
    private List<ItemInfo> mWorkspaceItems;

    public static class ConfirmIconPackDialog extends DialogFragment {
        LocalAppsManager mLocalAppsManager;
        private static final String PREFIX = ConfirmIconPackDialog.class.getCanonicalName();
        private static final String ICON_PACK_LABEL_KEY = PREFIX + ".label";
        private static final String ICON_PACK_PACKAGE_NAME_KEY = PREFIX + ".package_name";
        private static final String ICON_PACK_VERSION_CODE_KEY = PREFIX + ".version_code";

        public void lambda$onCreateDialog$1(DialogInterface dialogInterface, int i) {
            this.mLocalAppsManager.clearAllCustomIcons();
            IconLoader.getInstance().clearMemory();
            LauncherAppState.getInstance().getIconCache().flush();
            LauncherAppState.getInstance().getModel().forceReload();
            dialogInterface.dismiss();
            this.notifySuccess();
        }

        public static ConfirmIconPackDialog newInstance(IconPack iconPack, LocalAppsManager localAppsManager) {
            Bundle bundle = new Bundle();
            bundle.putString(ICON_PACK_LABEL_KEY, iconPack.getLabel());
            bundle.putString(ICON_PACK_PACKAGE_NAME_KEY, iconPack.getPackageName());
            bundle.putInt(ICON_PACK_VERSION_CODE_KEY, iconPack.getVersionCode());
            ConfirmIconPackDialog confirmIconPackDialog = new ConfirmIconPackDialog();
            confirmIconPackDialog.setArguments(bundle);
            confirmIconPackDialog.mLocalAppsManager = localAppsManager;
            return confirmIconPackDialog;
        }

        private void notifySuccess() {
            getParentFragment().onActivityResult(getTargetRequestCode(), -1, null);
        }

        @Override // androidx.fragment.app.DialogFragment
        public Dialog onCreateDialog(Bundle bundle) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), 5);
            builder.setMessage(getResources().getString(R.string.app_settings_item_change_icon_pack_dialog_custom_icon_overwrite, getArguments().getString(ICON_PACK_LABEL_KEY)));
            builder.setPositiveButton(R.string.app_settings_item_change_icon_pack_dialog_custom_icon_overwrite_keep, new DialogInterface.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$IconPackSettingsFragment$ConfirmIconPackDialog$521Zuv1liKk6SQ8r3p69qpfajAQ
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton(R.string.app_settings_item_change_icon_pack_dialog_custom_icon_overwrite_replace, new DialogInterface.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$IconPackSettingsFragment$ConfirmIconPackDialog$KFjk0LiRhCmd5Ur2P8CJszvQza4
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    ConfirmIconPackDialog.this.lambda$onCreateDialog$1( dialogInterface, i);
                }
            });
            builder.setCancelable(true);
            return builder.create();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    class IconPackViewHolder extends RecyclerView.ViewHolder {
        private ImageView mIcon;
        private TextView mLabel;
        private RadioButton mRadioButton;

        public IconPackViewHolder(View view) {
            super(view);
            this.mIcon = (ImageView) view.findViewById(R.id.list_item_icon);
            this.mLabel = (TextView) view.findViewById(R.id.list_item_label);
            this.mRadioButton = (RadioButton) view.findViewById(R.id.radio_btn);
        }
    }

    private void clearRadioButtons() {
        Iterator<IconPackViewHolder> it = this.mViewHolderMap.values().iterator();
        while (it.hasNext()) {
            it.next().mRadioButton.setChecked(false);
        }
    }

    private List<ItemInfo> cloneItems(List<ItemInfo> list) {
        ArrayList arrayList = new ArrayList(list.size());
        for (ItemInfo itemInfo : list) {
            switch (itemInfo.itemType) {
                case 0:
                case 1:
                    arrayList.add(new ShortcutInfo((ShortcutInfo) itemInfo));
                    break;
                case 2:
                    FolderInfo folderInfo = new FolderInfo();
                    folderInfo.copyFrom(itemInfo);
                    Iterator<ShortcutInfo> it = ((FolderInfo) itemInfo).contents.iterator();
                    while (it.hasNext()) {
                        folderInfo.add(new ShortcutInfo(it.next()));
                    }
                    arrayList.add(folderInfo);
                    break;
            }
        }
        return arrayList;
    }

    public void lambda$onViewCreated$0(CompoundButton compoundButton, boolean z) {
        SettingsStorage.getInstance().setIconNormalizationSetting(z);
        IconLoader.getInstance().clearMemory();
        LauncherAppState.getInstance().getIconCache().flush();
        LauncherAppState.getInstance().getModel().forceReload();
        this.refreshMockLauncher();
    }

    public void lambda$setupIconPackRows$2(View view) {
        IconPack iconPack = (IconPack) view.getTag();
        if (iconPack == this.mChosenPack) {
            return;
        }
        this.clearRadioButtons();
        this.mViewHolderMap.get(iconPack).mRadioButton.setChecked(true);
        this.mChosenPack = iconPack;
        this.mIconPackManager.setCurrentIconPack(this.mChosenPack);
        this.refreshMockLauncher();
        boolean z = false;
        Iterator<LocalAppData> it = this.mLocalAppsManager.getAllLocalLaunchApps().iterator();
        while (it.hasNext()) {
            if (it.next().getCustomIcon() != null) {
                z = true;
                break;
            }
        }
        if (z) {
            ConfirmIconPackDialog confirmIconPackDialogNewInstance = ConfirmIconPackDialog.newInstance(iconPack, this.mLocalAppsManager);
            confirmIconPackDialogNewInstance.setTargetFragment(null, 1);
            confirmIconPackDialogNewInstance.show(this.getChildFragmentManager(), null);
        }
    }

    private void refreshMockLauncher() {
        wipeIcons(this.mWorkspaceItems);
        wipeIcons(this.mHotseatItems);
        this.mMockLauncherController.layout();
        MockLauncherController mockLauncherController = this.mMockLauncherController;
        mockLauncherController.addItemsToCellLayout(mockLauncherController.getWorkspaceCellLayout(), getActivity(), this.mWorkspaceItems);
        MockLauncherController mockLauncherController2 = this.mMockLauncherController;
        mockLauncherController2.addItemsToCellLayout(mockLauncherController2.getHotseatCellLayout(), getActivity(), this.mHotseatItems);
    }

    private void setupIconPackRows(View view) {
        this.mContainer = (LinearLayout) view.findViewById(R.id.container);
        this.mItemClickListener = new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$IconPackSettingsFragment$csIC6qfQucK44l2UbWk15iYXn3Q
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                IconPackSettingsFragment.this.lambda$setupIconPackRows$2( view2);
            }
        };
        for (IconPack iconPack : this.mIconPacks) {
            View viewInflate = LayoutInflater.from(view.getContext()).inflate(R.layout.settings_activity_icon_pack_item, (ViewGroup) this.mContainer, false);
            IconPackViewHolder iconPackViewHolder = new IconPackViewHolder(viewInflate);
            iconPackViewHolder.itemView.setTag(iconPack);
            iconPackViewHolder.itemView.setOnClickListener(this.mItemClickListener);
            iconPackViewHolder.mRadioButton.setTag(iconPack);
            iconPackViewHolder.mRadioButton.setOnClickListener(this.mItemClickListener);
            iconPackViewHolder.mLabel.setText(iconPack.getLabel());
            iconPackViewHolder.mIcon.setImageDrawable(iconPack.getIcon());
            iconPackViewHolder.mRadioButton.setChecked(TextUtils.equals(this.mChosenPack.getPackageName(), iconPack.getPackageName()));
            this.mContainer.addView(viewInflate);
            this.mViewHolderMap.put(iconPack, iconPackViewHolder);
        }
        View viewInflate2 = LayoutInflater.from(view.getContext()).inflate(R.layout.settings_activity_icon_pack_item, (ViewGroup) this.mContainer, false);
        IconPackViewHolder iconPackViewHolder2 = new IconPackViewHolder(viewInflate2);
        iconPackViewHolder2.mLabel.setText(R.string.icon_pack_market_search);
        iconPackViewHolder2.mIcon.setImageResource(R.drawable.rollup_play_store);
        iconPackViewHolder2.mRadioButton.setVisibility(8);
        this.mContainer.addView(viewInflate2);
        viewInflate2.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$IconPackSettingsFragment$1sGujFKf899Sf789EdQICoehR9M
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                IconPackSettingsFragment iconPackSettingsFragment = IconPackSettingsFragment.this;
                LaunchUtil.startActivitySafely(iconPackSettingsFragment.getActivity(), iconPackSettingsFragment.mIconPackManager.getMarketSearchIntent());
            }
        });
    }

    private void setupMockLauncher(ViewGroup viewGroup) {
        this.mMockLauncher = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.settings_mock_launcher, viewGroup, false);
        this.mMockLauncherController = new MockLauncherController(this.mMockLauncher, this.mDeviceProfile);
        this.mMockLauncherController.layout();
        MockLauncherController mockLauncherController = this.mMockLauncherController;
        mockLauncherController.addItemsToCellLayout(mockLauncherController.getWorkspaceCellLayout(), getActivity(), this.mWorkspaceItems);
        MockLauncherController mockLauncherController2 = this.mMockLauncherController;
        mockLauncherController2.addItemsToCellLayout(mockLauncherController2.getHotseatCellLayout(), getActivity(), this.mHotseatItems);
        this.mFragmentInteractionListener.getRootView().addView(this.mMockLauncher, 0);
        final View viewFindViewById = viewGroup.findViewById(R.id.mock_launcher_target);
        viewFindViewById.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() { // from class: com.voxel.simplesearchlauncher.settings.IconPackSettingsFragment.1
            @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
            public void onGlobalLayout() {
                viewFindViewById.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int dimensionPixelSize = IconPackSettingsFragment.this.getResources().getDimensionPixelSize(R.dimen.spacer);
                float height = (viewFindViewById.getHeight() - (dimensionPixelSize * 2)) / (float) IconPackSettingsFragment.this.mMockLauncher.getHeight();
                IconPackSettingsFragment.this.mMockLauncher.setPivotY(0.0f);
                IconPackSettingsFragment.this.mMockLauncher.setScaleX(height);
                IconPackSettingsFragment.this.mMockLauncher.setScaleY(height);
                int[] iArr = {0, dimensionPixelSize};
                Utilities.getDescendantCoordRelativeToParent((View) viewFindViewById.getParent(), IconPackSettingsFragment.this.mFragmentInteractionListener.getRootView(), iArr, false);
                IconPackSettingsFragment iconPackSettingsFragment = IconPackSettingsFragment.this;
                iconPackSettingsFragment.mMockLauncherStartTranslationY = iArr[1] - iconPackSettingsFragment.mMockLauncher.getTop();
                IconPackSettingsFragment.this.mMockLauncher.setTranslationY(IconPackSettingsFragment.this.mMockLauncherStartTranslationY);
            }
        });
    }

    private void wipeIcons(List<ItemInfo> list) {
        for (ItemInfo itemInfo : list) {
            switch (itemInfo.itemType) {
                case 0:
                case 1:
                    ((ShortcutInfo) itemInfo).iconBitmap = null;
                    break;
                case 2:
                    Iterator<ShortcutInfo> it = ((FolderInfo) itemInfo).contents.iterator();
                    while (it.hasNext()) {
                        it.next().iconBitmap = null;
                    }
                    break;
            }
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == 1 && i2 == -1) {
            refreshMockLauncher();
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // androidx.fragment.app.Fragment
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
        try {
            this.mFragmentInteractionListener = (MockLauncherController.MockLauncherHolder) context;
        } catch (ClassCastException unused) {
            throw new ClassCastException(context.toString() + " must implement " + MockLauncherController.MockLauncherHolder.class.getCanonicalName());
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mWorkspaceItems = cloneItems(LauncherModel.getItemsForScreenPosition(0));
        this.mHotseatItems = cloneItems(LauncherModel.getHotseatItems());
        this.mIconPacks = this.mIconPackManager.getAllIconPacks();
        String currentIconPackPackageName = this.mIconPackManager.getCurrentIconPackPackageName();
        for (IconPack iconPack : this.mIconPacks) {
            if (TextUtils.equals(currentIconPackPackageName, iconPack.getPackageName()) || (this.mChosenPack == null && TextUtils.equals(null, iconPack.getPackageName()))) {
                this.mChosenPack = iconPack;
            }
        }
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.settings_activity_icon_pack_fragment, viewGroup, false);
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        DeviceProfile deviceProfile = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
        ArrayList arrayList = new ArrayList();
        arrayList.add(deviceProfile);
        this.mDeviceProfile = new DeviceProfile(getActivity(), (ArrayList<DeviceProfile>) arrayList, deviceProfile.minWidthDps, deviceProfile.minHeightDps, deviceProfile.widthPx, deviceProfile.heightPx, deviceProfile.availableWidthPx, deviceProfile.availableHeightPx, deviceProfile.getCellParamsMap(), getActivity().getResources(), SettingsStorage.getInstance().getSearchBarDesktopEnabled());
        setupMockLauncher((ViewGroup) view);
        setupIconPackRows(view);
        this.mIconNormalizationSwitch = new SettingsSwitchViewHolder(view.findViewById(R.id.icon_normalization));
        this.mIconNormalizationSwitch.getLabel().setText(R.string.app_settings_item_icon_normalization_text);
        this.mIconNormalizationSwitch.getSubLabel().setText(R.string.app_settings_item_icon_normalization_subtext);
        this.mIconNormalizationSwitch.getSubLabel().setVisibility(0);
        this.mIconNormalizationSwitch.getSwitch().setChecked(SettingsStorage.getInstance().getIconNormalizationSetting());
        this.mIconNormalizationSwitch.getSwitch().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$IconPackSettingsFragment$hxYsl30tn3gmwPiEc3ZnZ7MO2mA
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                IconPackSettingsFragment.this.lambda$onViewCreated$0( compoundButton, z);
            }
        });
        this.mScrollView = (NestedScrollView) view.findViewById(R.id.scroll_view);
    }
}
