package com.voxel.simplesearchlauncher.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import com.voxel.launcher3.CellParams;
import com.voxel.launcher3.DeviceProfile;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.Utilities;
import com.voxel.simplesearchlauncher.allapps.AllAppsFragment;
import com.voxel.simplesearchlauncher.model.itemdata.LocalAppData;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.voxel.simplesearchlauncher.settings.gridsettings.GridSettingsView;
import com.voxel.simplesearchlauncher.view.MockLauncherController;
import dagger.android.support.AndroidSupportInjection;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import com.ovacoda.devie.widget.DiscreteSeekBar;

/* JADX INFO: loaded from: classes2.dex */
public class AppDrawerSettingsFragment extends Fragment implements AllAppsFragment.OnFragmentInteractionListener {
    private AllAppsFragment mAllAppsFragment;
    private DeviceProfile mDeviceProfile;
    private SettingsTextViewHolder mDisplayStyleViewHolder;
    private MockLauncherController.MockLauncherHolder mFragmentInteractionListener;

    GridSettingsView mGridSettingsView;
    private SettingsTextViewHolder mIndexStyleViewHolder;
    LayoutSettingsManager mLayoutSettingsManager;
    private float mMockStartTranslationY;

    ViewGroup mMockTarget;

    View mMockView;

    NestedScrollView mScrollView;
    private SettingsSwitchViewHolder mShowLabelsViewHolder;
    private SettingsSwitchViewHolder mShowNewAppsViewHolder;

    public void lambda$setupControls$0(View view) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new MultipleChoiceDialogFragment.ListItem("list".hashCode(), R.drawable.settings_list, R.string.app_settings_item_app_drawer_display_style_list, 0));
        arrayList.add(new MultipleChoiceDialogFragment.ListItem("grid".hashCode(), R.drawable.settings_grid, R.string.app_settings_item_app_drawer_display_style_grid, 0));
        MultipleChoiceDialogFragment multipleChoiceDialogFragmentNewInstance = MultipleChoiceDialogFragment.newInstance(R.string.app_settings_item_app_drawer_display_style_dialog_title, SettingsStorage.getInstance().getAllAppsDisplayType().equals("grid") ? 1 : 0, arrayList, R.layout.dialog_icon_radiobutton_item);
        multipleChoiceDialogFragmentNewInstance.setTargetFragment(null, 1);
        multipleChoiceDialogFragmentNewInstance.show(this.getChildFragmentManager(), null);
    }

    public void lambda$setupControls$2(View view) {
        ArrayList arrayList = new ArrayList();
        int i = 0;
        arrayList.add(new MultipleChoiceDialogFragment.ListItem(0, R.drawable.settings_vertical_letters, R.string.app_settings_item_app_drawer_index_style_vertical_letters, 0));
        arrayList.add(new MultipleChoiceDialogFragment.ListItem(1, R.drawable.settings_classic_scrollbar, R.string.app_settings_item_app_drawer_index_style_classic_scrollbar, 0));
        int allAppsIndexType = SettingsStorage.getInstance().getAllAppsIndexType();
        for (int i2 = 0; i2 < arrayList.size(); i2++) {
            if (allAppsIndexType == ((MultipleChoiceDialogFragment.ListItem) arrayList.get(i2)).id) {
                i = i2;
                break;
            }
        }
        MultipleChoiceDialogFragment multipleChoiceDialogFragmentNewInstance = MultipleChoiceDialogFragment.newInstance(R.string.app_settings_item_app_drawer_index_style_dialog_title, i, arrayList, R.layout.dialog_icon_radiobutton_item);
        multipleChoiceDialogFragmentNewInstance.setTargetFragment(null, 2);
        multipleChoiceDialogFragmentNewInstance.show(this.getChildFragmentManager(), null);
    }

    public void lambda$setupControls$4(CompoundButton compoundButton, boolean z) {
        CellParams cellParamsForType = this.mDeviceProfile.getCellParamsForType(DeviceProfile.ParamType.ALLAPPS);
        cellParamsForType.showLabels = z;
        this.mDeviceProfile.updateCellParams(this.getContext(), DeviceProfile.ParamType.ALLAPPS, cellParamsForType);
        this.refreshLayout();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refreshLayout() {
        this.mAllAppsFragment.setCellParams(this.mGridSettingsView.getCellParams());
    }

    private void saveOptions() {
        this.mLayoutSettingsManager.setLayout(getContext(), DeviceProfile.ParamType.ALLAPPS, this.mGridSettingsView.getCellParams());
    }

    private void setupControls(View view) {
        this.mShowLabelsViewHolder = new SettingsSwitchViewHolder(view.findViewById(R.id.icon_label_switch));
        this.mDisplayStyleViewHolder = new SettingsTextViewHolder(view.findViewById(R.id.display_style));
        this.mShowNewAppsViewHolder = new SettingsSwitchViewHolder(view.findViewById(R.id.show_new_apps));
        this.mIndexStyleViewHolder = new SettingsTextViewHolder(view.findViewById(R.id.index_style));
        this.mDisplayStyleViewHolder.mLabel.setText(R.string.app_settings_item_app_drawer_display_style);
        updateDisplayStyle();
        this.mDisplayStyleViewHolder.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$AppDrawerSettingsFragment$UDFrYJ4644xtNbMvcm-0NjsD9ik
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                AppDrawerSettingsFragment.this.lambda$setupControls$0( view2);
            }
        });
        this.mShowNewAppsViewHolder.mLabel.setText(R.string.app_settings_item_app_drawer_show_new_apps);
        this.mShowNewAppsViewHolder.mSwitch.setChecked(SettingsStorage.getInstance().getAllAppsShowNewApps());
        this.mShowNewAppsViewHolder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$AppDrawerSettingsFragment$zaN46mt9GmIp3S4xG83_-Prm5Sg
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                SettingsStorage.getInstance().setAllAppsShowNewApps(z);
            }
        });
        this.mIndexStyleViewHolder.mLabel.setText(R.string.app_settings_item_app_drawer_index_style);
        updateIndexStyle();
        this.mIndexStyleViewHolder.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$AppDrawerSettingsFragment$YFgT_uXUHI9F3oLZ2Eo3266j4T4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                AppDrawerSettingsFragment.this.lambda$setupControls$2( view2);
            }
        });
        this.mGridSettingsView.getRowsViewHolder().itemView.setVisibility(8);
        this.mGridSettingsView.setParameters(this.mDeviceProfile, DeviceProfile.ParamType.ALLAPPS);
        this.mGridSettingsView.setOnInheritChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$AppDrawerSettingsFragment$oaThjBO9gMCgWNP-uff1nkHAzpA
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                AppDrawerSettingsFragment.this.refreshLayout();
            }
        });
        this.mGridSettingsView.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.AppDrawerSettingsFragment.1
            @Override // com.ovacoda.devie.widget.DiscreteSeekBar.OnProgressChangeListener
            public void onProgressChanged(DiscreteSeekBar discreteSeekBar, int i, boolean z) {
                if (z) {
                    AppDrawerSettingsFragment.this.refreshLayout();
                }
            }

            @Override // com.ovacoda.devie.widget.DiscreteSeekBar.OnProgressChangeListener
            public void onStartTrackingTouch(DiscreteSeekBar discreteSeekBar) {
            }

            @Override // com.ovacoda.devie.widget.DiscreteSeekBar.OnProgressChangeListener
            public void onStopTrackingTouch(DiscreteSeekBar discreteSeekBar) {
            }
        });
        this.mShowLabelsViewHolder.mLabel.setText(R.string.settings_show_icon_labels);
        this.mShowLabelsViewHolder.mSwitch.setChecked(this.mGridSettingsView.getCellParams().showLabels);
        this.mShowLabelsViewHolder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$AppDrawerSettingsFragment$OekfTKd9bgbVSV5_TT2xQdANqQE
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                AppDrawerSettingsFragment.this.lambda$setupControls$4( compoundButton, z);
            }
        });
    }

    private void setupMock() {
        /* REWORK: skip the donor's 37.5% black wash — it dimmed the preview band
           darker than the card rows (same as FolderSettingsFragment). */
        this.mFragmentInteractionListener.getRootView().setBackgroundColor(getContext().getResources().getColor(R.color.settings_activity_background));
        ((ViewGroup) this.mMockView.getParent()).removeView(this.mMockView);
        this.mFragmentInteractionListener.getRootView().addView(this.mMockView, 0);
        this.mMockTarget.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() { // from class: com.voxel.simplesearchlauncher.settings.AppDrawerSettingsFragment.2
            @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
            public void onGlobalLayout() {
                AppDrawerSettingsFragment.this.mMockTarget.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int dimensionPixelSize = AppDrawerSettingsFragment.this.getResources().getDimensionPixelSize(R.dimen.spacer);
                float height = (AppDrawerSettingsFragment.this.mMockTarget.getHeight() - (dimensionPixelSize * 2)) / (float) AppDrawerSettingsFragment.this.mMockView.getHeight();
                AppDrawerSettingsFragment.this.mMockView.setPivotX(AppDrawerSettingsFragment.this.mMockView.getWidth() / 2);
                AppDrawerSettingsFragment.this.mMockView.setPivotY(0.0f);
                AppDrawerSettingsFragment.this.mMockView.setScaleX(height);
                AppDrawerSettingsFragment.this.mMockView.setScaleY(height);
                int[] iArr = {0, dimensionPixelSize};
                Utilities.getDescendantCoordRelativeToParent((View) AppDrawerSettingsFragment.this.mMockTarget.getParent(), AppDrawerSettingsFragment.this.mFragmentInteractionListener.getRootView(), iArr, false);
                AppDrawerSettingsFragment appDrawerSettingsFragment = AppDrawerSettingsFragment.this;
                appDrawerSettingsFragment.mMockStartTranslationY = iArr[1] - appDrawerSettingsFragment.mMockView.getTop();
                AppDrawerSettingsFragment.this.mMockView.setTranslationY(AppDrawerSettingsFragment.this.mMockStartTranslationY);
            }
        });
    }

    private void updateDisplayStyle() {
        String allAppsDisplayType = SettingsStorage.getInstance().getAllAppsDisplayType();
        int i = 0;
        if ("list".equals(allAppsDisplayType)) {
            this.mGridSettingsView.showErrorText(R.string.app_settings_item_app_drawer_error_grid_only);
            this.mGridSettingsView.setControlsEnabled(false);
            this.mShowLabelsViewHolder.setEnabled(false);
            i = R.string.app_settings_item_app_drawer_display_style_list;
        } else if ("grid".equals(allAppsDisplayType)) {
            i = R.string.app_settings_item_app_drawer_display_style_grid;
            this.mGridSettingsView.hideErrorText();
            this.mGridSettingsView.setControlsEnabled(true);
            this.mShowLabelsViewHolder.setEnabled(true);
        }
        this.mDisplayStyleViewHolder.mSubLabel.setText(i);
        AllAppsFragment allAppsFragment = this.mAllAppsFragment;
        if (allAppsFragment != null) {
            allAppsFragment.updateStyle();
        }
    }

    private void updateIndexStyle() {
        this.mIndexStyleViewHolder.mSubLabel.setText(SettingsStorage.getInstance().getAllAppsIndexType() != 1 ? R.string.app_settings_item_app_drawer_index_style_vertical_letters : R.string.app_settings_item_app_drawer_index_style_classic_scrollbar);
        AllAppsFragment allAppsFragment = this.mAllAppsFragment;
        if (allAppsFragment != null) {
            allAppsFragment.updateStyle();
        }
    }

    @Override // com.voxel.simplesearchlauncher.allapps.AllAppsFragment.OnFragmentInteractionListener
    public boolean canShowAllAppsSetDefaultPrompt() {
        return false;
    }

    @Override // androidx.fragment.app.Fragment
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i != 1) {
            if (i == 2) {
                SettingsStorage.getInstance().setAllAppsIndexType((int) intent.getLongExtra("ID", -1L));
                updateIndexStyle();
                return;
            }
            return;
        }
        int longExtra = (int) intent.getLongExtra("ID", -1L);
        if (longExtra == "list".hashCode()) {
            SettingsStorage.getInstance().setAllAppsDisplayType("list");
        }
        if (longExtra == "grid".hashCode()) {
            SettingsStorage.getInstance().setAllAppsDisplayType("grid");
        }
        updateDisplayStyle();
    }

    @Override // com.voxel.simplesearchlauncher.allapps.AllAppsFragment.OnFragmentInteractionListener
    public boolean onAllAppsDropCompleted(AllAppsFragment allAppsFragment, boolean z) {
        return false;
    }

    @Override // com.voxel.simplesearchlauncher.allapps.AllAppsFragment.OnFragmentInteractionListener
    public boolean onAllAppsLongPress(AllAppsFragment allAppsFragment, View view, LocalAppData localAppData) {
        return false;
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
        return layoutInflater.inflate(R.layout.settings_activity_app_drawer_fragment, viewGroup, false);
    }

    @Override // androidx.fragment.app.Fragment
    public void onStop() {
        super.onStop();
        saveOptions();
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        this.mScrollView = (NestedScrollView) view.findViewById(R.id.scroll_view);
        this.mMockView = view.findViewById(R.id.mock);
        this.mMockTarget = (ViewGroup) view.findViewById(R.id.mock_target);
        this.mGridSettingsView = (GridSettingsView) view.findViewById(R.id.grid_settings);
        DeviceProfile deviceProfile = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
        ArrayList arrayList = new ArrayList();
        arrayList.add(deviceProfile);
        this.mDeviceProfile = new DeviceProfile(getActivity(), (ArrayList<DeviceProfile>) arrayList, deviceProfile.minWidthDps, deviceProfile.minHeightDps, deviceProfile.widthPx, deviceProfile.heightPx, deviceProfile.availableWidthPx, deviceProfile.availableHeightPx, deviceProfile.getCellParamsMap(), getActivity().getResources(), SettingsStorage.getInstance().getSearchBarDesktopEnabled());
        setupControls(view);
        setupMock();
        this.mAllAppsFragment = (AllAppsFragment) getChildFragmentManager().findFragmentById(R.id.all_apps_fragment);
    }
}
