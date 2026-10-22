package com.voxel.simplesearchlauncher.settings;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import com.voxel.launcher3.CellParams;
import com.voxel.launcher3.DeviceProfile;
import com.voxel.launcher3.ItemInfo;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.LauncherModel;
import com.voxel.launcher3.Utilities;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.voxel.simplesearchlauncher.settings.gridsettings.GridSettingsView;
import com.voxel.simplesearchlauncher.view.GridLineController;
import com.voxel.simplesearchlauncher.view.MockLauncherController;
import dagger.android.support.AndroidSupportInjection;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

/* JADX INFO: loaded from: classes2.dex */
public class DockSettingsFragment extends Fragment {
    private SettingsSeekBarViewHolder mBackgroundAlphaViewHolder;
    private SettingsSwitchViewHolder mBackgroundBlurViewHolder;
    private CellParams mCellParams;
    private DeviceProfile mDeviceProfile;
    private SwitchCompat mEnabledSwitch;
    private MockLauncherController.MockLauncherHolder mFragmentInteractionListener;
    private GridSettingsView mGridSettingsView;
    LayoutSettingsManager mLayoutSettingsManager;
    private View mMockLauncher;
    private MockLauncherController mMockLauncherController;
    private float mMockLauncherStartTranslationY;
    private NestedScrollView mScrollView;
    private SettingsSwitchViewHolder mShowLabelsViewHolder;
    private View mSwitchContainer;
    private List<ItemInfo> mWorkspaceItems = Collections.emptyList();
    private List<ItemInfo> mHotseatItems = Collections.emptyList();
    private Map<ItemInfo, Drawable> mWidgetImageMap = new HashMap();

    public void lambda$setupControls$2(CompoundButton compoundButton, boolean z) {
        this.mCellParams.enabled = z;
        this.mDeviceProfile.updateCellParams(this.getContext(), DeviceProfile.ParamType.HOTSEAT, this.mCellParams);
        this.mMockLauncherController.layout();
        this.setControlsEnabled(z);
    }

    public void lambda$setupControls$3(CompoundButton compoundButton, boolean z) {
        this.mCellParams.backgroundBlur = z;
        this.mDeviceProfile.updateCellParams(this.getContext(), DeviceProfile.ParamType.HOTSEAT, this.mCellParams);
    }

    public void lambda$setupControls$4(CompoundButton compoundButton, boolean z) {
        this.mCellParams.showLabels = z;
        this.mDeviceProfile.updateCellParams(this.getContext(), DeviceProfile.ParamType.HOTSEAT, this.mCellParams);
        MockLauncherController mockLauncherController = this.mMockLauncherController;
        mockLauncherController.addItemsToCellLayout(mockLauncherController.getHotseatCellLayout(), this.getActivity(), this.mHotseatItems);
        this.mMockLauncherController.layout();
    }

    private void saveOptions() {
        this.mCellParams.numColumns = this.mGridSettingsView.getCellParams().numColumns;
        this.mCellParams.iconScale = this.mGridSettingsView.getCellParams().iconScale;
        this.mCellParams.showLabels = this.mShowLabelsViewHolder.mSwitch.isChecked();
        this.mCellParams.backgroundBlur = this.mBackgroundBlurViewHolder.mSwitch.isChecked();
        this.mCellParams.enabled = this.mEnabledSwitch.isChecked();
        this.mCellParams.inheritWorkspace = this.mGridSettingsView.getCellParams().inheritWorkspace;
        this.mLayoutSettingsManager.setLayout(getContext(), DeviceProfile.ParamType.HOTSEAT, this.mCellParams);
    }

    private void setControlsEnabled(boolean z) {
        this.mGridSettingsView.setControlsEnabled(z);
        this.mShowLabelsViewHolder.setEnabled(z);
        this.mBackgroundBlurViewHolder.setEnabled(z);
        this.mBackgroundAlphaViewHolder.setEnabled(z);
    }

    private void setupControls(View view) {
        this.mGridSettingsView = (GridSettingsView) view.findViewById(R.id.grid_settings);
        this.mShowLabelsViewHolder = new SettingsSwitchViewHolder(view.findViewById(R.id.icon_label_switch));
        this.mBackgroundAlphaViewHolder = new SettingsSeekBarViewHolder(view.findViewById(R.id.background_alpha));
        this.mBackgroundBlurViewHolder = new SettingsSwitchViewHolder(view.findViewById(R.id.background_blur));
        this.mGridSettingsView.getRowsViewHolder().itemView.setVisibility(8);
        this.mGridSettingsView.setParameters(this.mDeviceProfile, DeviceProfile.ParamType.HOTSEAT);
        this.mEnabledSwitch.setChecked(this.mCellParams.enabled);
        setControlsEnabled(this.mCellParams.enabled);
        this.mShowLabelsViewHolder.mLabel.setText(R.string.settings_show_icon_labels);
        this.mShowLabelsViewHolder.mSwitch.setChecked(this.mCellParams.showLabels);
        int iRound = Math.round(Math.max(0.0f, Math.min(this.mCellParams.backgroundAlpha * 100.0f, 100.0f)));
        this.mBackgroundAlphaViewHolder.mLabel.setText(R.string.app_settings_dock_background_alpha);
        this.mBackgroundAlphaViewHolder.mDiscreteSeekBar.setMin(0);
        this.mBackgroundAlphaViewHolder.mDiscreteSeekBar.setMax(100);
        this.mBackgroundAlphaViewHolder.mDiscreteSeekBar.setProgress(iRound);
        this.mBackgroundBlurViewHolder.mLabel.setText(R.string.app_settings_dock_background_blur);
        this.mBackgroundBlurViewHolder.mSwitch.setChecked(this.mCellParams.backgroundBlur);
        this.mGridSettingsView.setOnInheritChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$DockSettingsFragment$FtTN6y1eIfMbIXCDsEEUdyBn-s8
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                DockSettingsFragment.this.updateGrid();
            }
        });
        this.mGridSettingsView.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.DockSettingsFragment.1
            @Override // org.adw.library.widgets.discreteseekbar.DiscreteSeekBar.OnProgressChangeListener
            public void onProgressChanged(DiscreteSeekBar discreteSeekBar, int i, boolean z) {
                if (z) {
                    if (discreteSeekBar == DockSettingsFragment.this.mGridSettingsView.getIconSizeViewHolder().mDiscreteSeekBar) {
                        DockSettingsFragment.this.updateIconSize();
                    } else {
                        DockSettingsFragment.this.updateGrid();
                    }
                }
            }

            @Override // org.adw.library.widgets.discreteseekbar.DiscreteSeekBar.OnProgressChangeListener
            public void onStartTrackingTouch(DiscreteSeekBar discreteSeekBar) {
                GridLineController hotseatGridLineController = DockSettingsFragment.this.mMockLauncherController.getHotseatGridLineController();
                hotseatGridLineController.updateGridLines(1, Math.round(DockSettingsFragment.this.mCellParams.numColumns));
                hotseatGridLineController.showGridLines();
            }

            @Override // org.adw.library.widgets.discreteseekbar.DiscreteSeekBar.OnProgressChangeListener
            public void onStopTrackingTouch(DiscreteSeekBar discreteSeekBar) {
                DockSettingsFragment.this.mMockLauncherController.getHotseatGridLineController().hideGridLines();
            }
        });
        this.mEnabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$DockSettingsFragment$bNpL9aFOodcPgvX8_idYMHbZ3NA
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                DockSettingsFragment.this.lambda$setupControls$2( compoundButton, z);
            }
        });
        this.mBackgroundBlurViewHolder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$DockSettingsFragment$YhQ0nd5y4DRtycmvMY8ibLyVdi0
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                DockSettingsFragment.this.lambda$setupControls$3( compoundButton, z);
            }
        });
        this.mShowLabelsViewHolder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$DockSettingsFragment$0AiG2dvzl8cwxz9h-SCz0B4AlPA
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                DockSettingsFragment.this.lambda$setupControls$4( compoundButton, z);
            }
        });
        this.mBackgroundAlphaViewHolder.mDiscreteSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.DockSettingsFragment.2
            @Override // org.adw.library.widgets.discreteseekbar.DiscreteSeekBar.OnProgressChangeListener
            public void onProgressChanged(DiscreteSeekBar discreteSeekBar, int i, boolean z) {
                DockSettingsFragment.this.updateDockAlpha();
            }

            @Override // org.adw.library.widgets.discreteseekbar.DiscreteSeekBar.OnProgressChangeListener
            public void onStartTrackingTouch(DiscreteSeekBar discreteSeekBar) {
            }

            @Override // org.adw.library.widgets.discreteseekbar.DiscreteSeekBar.OnProgressChangeListener
            public void onStopTrackingTouch(DiscreteSeekBar discreteSeekBar) {
            }
        });
    }

    private void setupMockLauncher(FrameLayout frameLayout) {
        this.mMockLauncher = LayoutInflater.from(frameLayout.getContext()).inflate(R.layout.settings_mock_launcher, (ViewGroup) frameLayout, false);
        this.mMockLauncherController = new MockLauncherController(this.mMockLauncher, this.mDeviceProfile);
        this.mMockLauncherController.layout();
        MockLauncherController mockLauncherController = this.mMockLauncherController;
        mockLauncherController.addItemsToCellLayout(mockLauncherController.getWorkspaceCellLayout(), getActivity(), this.mWorkspaceItems);
        MockLauncherController mockLauncherController2 = this.mMockLauncherController;
        mockLauncherController2.addItemsToCellLayout(mockLauncherController2.getHotseatCellLayout(), getActivity(), this.mHotseatItems);
        this.mFragmentInteractionListener.getRootView().addView(this.mMockLauncher, 0);
        final View viewFindViewById = frameLayout.findViewById(R.id.mock_launcher_target);
        viewFindViewById.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() { // from class: com.voxel.simplesearchlauncher.settings.DockSettingsFragment.3
            @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
            public void onGlobalLayout() {
                viewFindViewById.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int dimensionPixelSize = DockSettingsFragment.this.getResources().getDimensionPixelSize(R.dimen.spacer);
                float height = (viewFindViewById.getHeight() - (dimensionPixelSize * 2)) / (float) DockSettingsFragment.this.mMockLauncher.getHeight();
                DockSettingsFragment.this.mMockLauncher.setPivotY(0.0f);
                DockSettingsFragment.this.mMockLauncher.setScaleX(height);
                DockSettingsFragment.this.mMockLauncher.setScaleY(height);
                int[] iArr = {0, dimensionPixelSize};
                Utilities.getDescendantCoordRelativeToParent((View) viewFindViewById.getParent(), DockSettingsFragment.this.mFragmentInteractionListener.getRootView(), iArr, false);
                DockSettingsFragment dockSettingsFragment = DockSettingsFragment.this;
                dockSettingsFragment.mMockLauncherStartTranslationY = iArr[1] - dockSettingsFragment.mMockLauncher.getTop();
                DockSettingsFragment.this.mMockLauncher.setTranslationY(DockSettingsFragment.this.mMockLauncherStartTranslationY);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDockAlpha() {
        this.mCellParams.backgroundAlpha = Math.max(0.0f, Math.min(this.mBackgroundAlphaViewHolder.mDiscreteSeekBar.getProgress() / 100.0f, 1.0f));
        this.mMockLauncherController.layout();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateGrid() {
        int i = (int) this.mGridSettingsView.getCellParams().numColumns;
        this.mDeviceProfile.updateAvailableDimensions(getContext());
        this.mMockLauncherController.layout();
        this.mMockLauncherController.getHotseatCellLayout().setGridSize(i, 1);
        this.mMockLauncherController.getHotseatGridLineController().updateGridLines(1, i);
        updateIconSize();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateIconSize() {
        this.mDeviceProfile.updateAvailableDimensions(getContext());
        MockLauncherController mockLauncherController = this.mMockLauncherController;
        mockLauncherController.addItemsToCellLayout(mockLauncherController.getWorkspaceCellLayout(), getActivity(), this.mWorkspaceItems);
        MockLauncherController mockLauncherController2 = this.mMockLauncherController;
        mockLauncherController2.addItemsToCellLayout(mockLauncherController2.getHotseatCellLayout(), getActivity(), this.mHotseatItems);
        this.mMockLauncherController.layout();
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
        this.mWorkspaceItems = LauncherModel.getItemsForScreenPosition(0);
        this.mHotseatItems = LauncherModel.getHotseatItems();
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.mSwitchContainer = layoutInflater.inflate(R.layout.toolbar_switch, (ViewGroup) null);
        this.mEnabledSwitch = (SwitchCompat) this.mSwitchContainer.findViewById(R.id.toolbar_switch);
        return layoutInflater.inflate(R.layout.settings_activity_dock_fragment, viewGroup, false);
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        if (this.mSwitchContainer.getParent() == null) {
            ((TransparentSettingsActivity) getActivity()).getToolbarContainer().addView(this.mSwitchContainer);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowCustomEnabled(true);
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onStop() {
        super.onStop();
        saveOptions();
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        DeviceProfile deviceProfile = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
        ArrayList arrayList = new ArrayList();
        arrayList.add(deviceProfile);
        this.mDeviceProfile = new DeviceProfile(getActivity(), (ArrayList<DeviceProfile>) arrayList, deviceProfile.minWidthDps, deviceProfile.minHeightDps, deviceProfile.widthPx, deviceProfile.heightPx, deviceProfile.availableWidthPx, deviceProfile.availableHeightPx, deviceProfile.getCellParamsMap(), getActivity().getResources(), SettingsStorage.getInstance().getSearchBarDesktopEnabled());
        this.mCellParams = this.mDeviceProfile.getCellParamsForType(DeviceProfile.ParamType.HOTSEAT);
        setupControls(view);
        setupMockLauncher((FrameLayout) view);
        this.mScrollView = (NestedScrollView) view.findViewById(R.id.scroll_view);
        this.mScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$DockSettingsFragment$gGL5dkPwo-h5JbquBbgSIyAnvS4
            @Override // androidx.core.widget.NestedScrollView.OnScrollChangeListener
            public final void onScrollChange(NestedScrollView nestedScrollView, int i, int i2, int i3, int i4) {
                DockSettingsFragment dockSettingsFragment = DockSettingsFragment.this;
                dockSettingsFragment.mMockLauncher.setTranslationY(dockSettingsFragment.mMockLauncherStartTranslationY - (i2 / 2.0f));
            }
        });
    }
}
