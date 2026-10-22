package com.voxel.simplesearchlauncher.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import butterknife.ButterKnife;
import com.voxel.launcher3.CellParams;
import com.voxel.launcher3.DeviceProfile;
import com.voxel.launcher3.Folder;
import com.voxel.launcher3.FolderInfo;
import com.voxel.launcher3.IconCache;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.ShortcutInfo;
import com.voxel.launcher3.Utilities;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.simplesearchlauncher.model.itemdata.LocalAppData;
import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.voxel.simplesearchlauncher.settings.gridsettings.GridSettingsView;
import com.voxel.simplesearchlauncher.view.MockLauncherController;
import dagger.android.support.AndroidSupportInjection;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

/* JADX INFO: loaded from: classes2.dex */
public class FolderSettingsFragment extends Fragment {
    private CellParams mCellParams;
    private DeviceProfile mDeviceProfile;
    private Folder mFolder;
    private SettingsSwitchViewHolder mFolderAlphaSort;
    private boolean mFolderAlphaSortOriginalValue;
    private FolderInfo mFolderInfo;
    private SettingsTextViewHolder mFolderShape;
    private MockLauncherController.MockLauncherHolder mFragmentInteractionListener;
    private GridSettingsView mGridSettingsView;
    LayoutSettingsManager mLayoutSettingsManager;
    LocalAppsManager mLocalAppsManager;
    private float mMockStartTranslationY;

    LinearLayout mMockTarget;

    ViewGroup mMockView;

    NestedScrollView mScrollView;
    private SettingsSwitchViewHolder mShowLabelsViewHolder;
    private ArrayList<Integer> mShuffledRankList;

    private void applyShuffledRanksToFolderInfo() {
        for (int i = 0; i < this.mShuffledRankList.size(); i++) {
            this.mFolderInfo.contents.get(i).rank = this.mShuffledRankList.get(i).intValue();
        }
    }

    private static int getResourceIdForFolderShape(int i) {
        switch (i) {
            case 0:
                return R.string.app_settings_item_folders_shape_circle;
            case 1:
                return R.string.app_settings_item_folders_shape_squircle;
            default:
                return R.string.app_settings_item_folders_shape_square;
        }
    }

    public void lambda$onViewCreated$0(CompoundButton compoundButton, boolean z) {
        this.mCellParams.showLabels = z;
        this.mDeviceProfile.updateCellParams(this.getContext(), DeviceProfile.ParamType.FOLDER, this.mCellParams);
        this.refreshLayout();
    }

    public void lambda$onViewCreated$2(View view) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new MultipleChoiceDialogFragment.ListItem(0, R.drawable.folder_icon_background_circle, R.string.app_settings_item_folders_shape_circle, 0));
        arrayList.add(new MultipleChoiceDialogFragment.ListItem(1, R.drawable.folder_icon_background_squircle, R.string.app_settings_item_folders_shape_squircle, 0));
        arrayList.add(new MultipleChoiceDialogFragment.ListItem(2, R.drawable.folder_icon_background_square, R.string.app_settings_item_folders_shape_square, 0));
        MultipleChoiceDialogFragment multipleChoiceDialogFragmentNewInstance = MultipleChoiceDialogFragment.newInstance(R.string.app_settings_item_folders_shape_dialog_title, SettingsStorage.getInstance().getFolderShapeStyle(), arrayList, R.layout.dialog_icon_radiobutton_item);
        multipleChoiceDialogFragmentNewInstance.setTargetFragment(null, 1);
        multipleChoiceDialogFragmentNewInstance.show(this.getChildFragmentManager(), null);
    }

    public void lambda$onViewCreated$3(CompoundButton compoundButton, boolean z) {
        SettingsStorage.getInstance().setFolderAlphabetSortEnabled(z);
        this.refreshLayout();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refreshLayout() {
        this.mCellParams.numColumns = this.mGridSettingsView.getCellParams().numColumns;
        this.mCellParams.numRows = this.mGridSettingsView.getCellParams().numColumns;
        this.mCellParams.iconScale = this.mGridSettingsView.getCellParams().iconScale;
        this.mFolder.setCellParams(this.mCellParams);
        this.mFolder.getContent().setCellParams(this.mCellParams);
        applyShuffledRanksToFolderInfo();
        this.mFolder.bind(this.mFolderInfo, false, SettingsStorage.getInstance().getFolderAlphabetSortEnabled());
    }

    private void saveOptions() {
        this.mLayoutSettingsManager.setLayout(getContext(), DeviceProfile.ParamType.FOLDER, this.mGridSettingsView.getCellParams());
        if (this.mFolderAlphaSortOriginalValue != SettingsStorage.getInstance().getFolderAlphabetSortEnabled()) {
            LauncherAppState.getInstance().getModel().forceReload();
            this.mFolderAlphaSortOriginalValue = SettingsStorage.getInstance().getFolderAlphabetSortEnabled();
        }
    }

    private void setupMockTarget() {
        this.mFragmentInteractionListener.getRootView().setBackgroundColor(1610612736);
        this.mFolderInfo = new FolderInfo();
        this.mFolderInfo.setTitle(getResources().getString(R.string.app_settings_item_folders_name));
        IconCache iconCache = LauncherAppState.getInstance().getIconCache();
        List<LocalAppData> allLocalLaunchApps = this.mLocalAppsManager.getAllLocalLaunchApps();
        this.mShuffledRankList = new ArrayList<>(16);
        for (int i = 0; i < 16; i++) {
            ShortcutInfo shortcutInfo = new ShortcutInfo();
            shortcutInfo.intent = new Intent();
            shortcutInfo.setIcon(iconCache.getDefaultIcon(UserHandleCompat.myUserHandle()));
            if (allLocalLaunchApps.size() > 0) {
                shortcutInfo.title = allLocalLaunchApps.get(i % allLocalLaunchApps.size()).getLabel();
            }
            this.mFolderInfo.add(shortcutInfo);
            this.mShuffledRankList.add(Integer.valueOf(i));
        }
        Collections.shuffle(this.mShuffledRankList);
        applyShuffledRanksToFolderInfo();
        this.mFolder = Folder.fromXml(getContext());
        this.mFolder.bind(this.mFolderInfo, false, SettingsStorage.getInstance().getFolderAlphabetSortEnabled());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2);
        layoutParams.gravity = 17;
        this.mFolder.setLayoutParams(layoutParams);
        this.mMockView.addView(this.mFolder, 0);
        ((ViewGroup) this.mMockView.getParent()).removeView(this.mMockView);
        this.mFragmentInteractionListener.getRootView().addView(this.mMockView, 0);
        this.mMockTarget.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() { // from class: com.voxel.simplesearchlauncher.settings.FolderSettingsFragment.2
            @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
            public void onGlobalLayout() {
                FolderSettingsFragment.this.mMockTarget.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int dimensionPixelSize = FolderSettingsFragment.this.getResources().getDimensionPixelSize(R.dimen.spacer);
                float height = (FolderSettingsFragment.this.mMockTarget.getHeight() - (dimensionPixelSize * 2)) / (float) FolderSettingsFragment.this.mMockView.getHeight();
                FolderSettingsFragment.this.mMockView.setPivotX(FolderSettingsFragment.this.mMockView.getWidth() / 2);
                FolderSettingsFragment.this.mMockView.setPivotY(0.0f);
                FolderSettingsFragment.this.mMockView.setScaleX(height);
                FolderSettingsFragment.this.mMockView.setScaleY(height);
                int[] iArr = {0, dimensionPixelSize};
                Utilities.getDescendantCoordRelativeToParent((View) FolderSettingsFragment.this.mMockTarget.getParent(), FolderSettingsFragment.this.mFragmentInteractionListener.getRootView(), iArr, false);
                FolderSettingsFragment folderSettingsFragment = FolderSettingsFragment.this;
                folderSettingsFragment.mMockStartTranslationY = iArr[1] - folderSettingsFragment.mMockView.getTop();
                FolderSettingsFragment.this.mMockView.setTranslationY(FolderSettingsFragment.this.mMockStartTranslationY);
            }
        });
        this.mScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$FolderSettingsFragment$hT55fNRufRPTlE0OFNmHvGOiaoU
            @Override // androidx.core.widget.NestedScrollView.OnScrollChangeListener
            public final void onScrollChange(NestedScrollView nestedScrollView, int i2, int i3, int i4, int i5) {
                FolderSettingsFragment folderSettingsFragment = FolderSettingsFragment.this;
                folderSettingsFragment.mMockView.setTranslationY(folderSettingsFragment.mMockStartTranslationY - (i3 / 2.0f));
            }
        });
    }

    private void updateFolderShapeText() {
        this.mFolderShape.mSubLabel.setText(getResourceIdForFolderShape(SettingsStorage.getInstance().getFolderShapeStyle()));
    }

    @Override // androidx.fragment.app.Fragment
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == 1) {
            int longExtra = (int) intent.getLongExtra("ID", -1L);
            if (longExtra != -1) {
                SettingsStorage.getInstance().setFolderShapeStyle(longExtra);
            }
            updateFolderShapeText();
            LauncherAppState.getInstance().getModel().forceReload();
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
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.settings_activity_folder_fragment, viewGroup, false);
    }

    @Override // androidx.fragment.app.Fragment
    public void onStop() {
        super.onStop();
        saveOptions();
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        ButterKnife.bind(this, view);
        DeviceProfile deviceProfile = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
        ArrayList arrayList = new ArrayList();
        arrayList.add(deviceProfile);
        this.mDeviceProfile = new DeviceProfile(getActivity(), (ArrayList<DeviceProfile>) arrayList, deviceProfile.minWidthDps, deviceProfile.minHeightDps, deviceProfile.widthPx, deviceProfile.heightPx, deviceProfile.availableWidthPx, deviceProfile.availableHeightPx, deviceProfile.getCellParamsMap(), getActivity().getResources(), SettingsStorage.getInstance().getSearchBarDesktopEnabled());
        this.mCellParams = this.mDeviceProfile.getCellParamsForType(DeviceProfile.ParamType.FOLDER);
        this.mShowLabelsViewHolder = new SettingsSwitchViewHolder(view.findViewById(R.id.icon_label_switch));
        this.mShowLabelsViewHolder.mLabel.setText(R.string.settings_show_icon_labels);
        this.mShowLabelsViewHolder.mSwitch.setChecked(this.mCellParams.showLabels);
        this.mShowLabelsViewHolder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$FolderSettingsFragment$1_XWSeOuWnzBddLV3Xx5aV8F1QM
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                FolderSettingsFragment.this.lambda$onViewCreated$0( compoundButton, z);
            }
        });
        setupMockTarget();
        this.mGridSettingsView = (GridSettingsView) view.findViewById(R.id.grid_settings);
        this.mGridSettingsView.getRowsViewHolder().itemView.setVisibility(8);
        this.mGridSettingsView.setParameters(this.mDeviceProfile, DeviceProfile.ParamType.FOLDER);
        this.mGridSettingsView.setOnInheritChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$FolderSettingsFragment$fd6cZN-yz0Zei2cHRPBEgsIUs4E
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                FolderSettingsFragment.this.refreshLayout();
            }
        });
        this.mGridSettingsView.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.FolderSettingsFragment.1
            @Override // org.adw.library.widgets.discreteseekbar.DiscreteSeekBar.OnProgressChangeListener
            public void onProgressChanged(DiscreteSeekBar discreteSeekBar, int i, boolean z) {
                FolderSettingsFragment.this.refreshLayout();
            }

            @Override // org.adw.library.widgets.discreteseekbar.DiscreteSeekBar.OnProgressChangeListener
            public void onStartTrackingTouch(DiscreteSeekBar discreteSeekBar) {
            }

            @Override // org.adw.library.widgets.discreteseekbar.DiscreteSeekBar.OnProgressChangeListener
            public void onStopTrackingTouch(DiscreteSeekBar discreteSeekBar) {
            }
        });
        this.mFolderShape = new SettingsTextViewHolder(view.findViewById(R.id.folder_shape));
        this.mFolderShape.mLabel.setText(R.string.app_settings_item_folders_shape);
        updateFolderShapeText();
        this.mFolderShape.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$FolderSettingsFragment$L7AD5iPw6pmRIwXsBtKn5teIbv4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                FolderSettingsFragment.this.lambda$onViewCreated$2( view2);
            }
        });
        this.mFolderAlphaSortOriginalValue = SettingsStorage.getInstance().getFolderAlphabetSortEnabled();
        this.mFolderAlphaSort = new SettingsSwitchViewHolder(view.findViewById(R.id.folder_alpha_sort));
        this.mFolderAlphaSort.mLabel.setText(R.string.app_settings_item_folders_alpha_sort);
        this.mFolderAlphaSort.mSwitch.setChecked(this.mFolderAlphaSortOriginalValue);
        this.mFolderAlphaSort.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$FolderSettingsFragment$vdDmDg9B6sU2FpLnqvwB2Svz0eg
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                FolderSettingsFragment.this.lambda$onViewCreated$3( compoundButton, z);
            }
        });
    }
}
