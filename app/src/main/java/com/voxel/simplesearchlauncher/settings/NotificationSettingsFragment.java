package com.voxel.simplesearchlauncher.settings;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.evie.common.HybridNetworkFetcherCommon;
import com.ovacoda.devie.R;
import com.ovacoda.devie.images.IconLoader;
import com.ovacoda.devie.widget.DiscreteSeekBar;
import com.voxel.launcher3.CellParams;
import com.voxel.launcher3.DeviceProfile;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.simplesearchlauncher.activity.SafeActivityStarter;
import com.voxel.simplesearchlauncher.model.AppsInfoDataHelper;
import com.voxel.simplesearchlauncher.notification.InterceptingWidgetHost;
import com.voxel.simplesearchlauncher.notification.NotificationController;
import com.voxel.simplesearchlauncher.notification.Notifier;
import com.voxel.simplesearchlauncher.notification.extractors.Extractor;
import com.voxel.simplesearchlauncher.notification.extractors.GmailExtractor;
import com.voxel.simplesearchlauncher.notification.extractors.WidgetExtractor;
import com.voxel.simplesearchlauncher.notification.service.NotificationListener;
import com.voxel.simplesearchlauncher.view.BadgeDrawable;
import com.voxel.simplesearchlauncher.view.BadgeableAppIcon;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/* JADX INFO: loaded from: classes2.dex */
public class NotificationSettingsFragment extends Fragment {
    private static final int TYPE_CONFIG = 0;
    private static final int TYPE_HEADER_COMMON = 1;
    private static final int TYPE_APP_COMMON = 2;
    private static final int TYPE_HEADER_ALL = 3;
    private static final int TYPE_APP_ALL = 4;

    private List<AppsInfoDataHelper.AppInfo> mAllApps;
    private AllAppsSection mAllSection;
    private AppsInfoDataHelper mAppsInfoDataHelper;
    private RecyclerView mAppsRecyclerView;
    private AppsAdapter mAppsAdapter;
    private List<AppsInfoDataHelper.AppInfo> mCommonApps;
    private AppSwitchSection mCommonSection;
    private ConfigSection mConfigSection;
    private NotificationController mController;
    private SwitchCompat mEnabledSwitch;
    private View mInitContainer;
    private LayoutInflater mLayoutInflater;
    private String mPendingPackageToEnable;
    private WidgetExtractor mPendingWidgetExtractor;
    private boolean mStartedConfiguration;
    private View mSwitchContainer;

    private class AppsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public int getItemViewType(int position) {
            int size = NotificationSettingsFragment.this.mCommonApps.size();
            if (position == 0) {
                return TYPE_CONFIG;
            }
            if (position == 1) {
                return TYPE_HEADER_COMMON;
            }
            if (position <= size + 1) {
                return TYPE_APP_COMMON;
            }
            if (position == size + 2) {
                return TYPE_HEADER_ALL;
            }
            return TYPE_APP_ALL;
        }

        @Override
        public int getItemCount() {
            return NotificationSettingsFragment.this.mCommonApps.size() + NotificationSettingsFragment.this.mAllApps.size() + 3;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_CONFIG:
                    return NotificationSettingsFragment.this.mConfigSection.createViewHolder(NotificationSettingsFragment.this.mLayoutInflater, parent);
                case TYPE_HEADER_COMMON:
                case TYPE_HEADER_ALL:
                    return new SectionHeaderViewHolder(NotificationSettingsFragment.this.mLayoutInflater.inflate(R.layout.settings_activity_notification_section_header, parent, false));
                default:
                    return new SwitchViewHolder(NotificationSettingsFragment.this.mLayoutInflater.inflate(R.layout.settings_activity_icon_switch_item, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            int itemViewType = getItemViewType(position);
            if (itemViewType == TYPE_CONFIG) {
                NotificationSettingsFragment.this.mConfigSection.bind();
                return;
            }
            int size = NotificationSettingsFragment.this.mCommonApps.size();
            if (itemViewType == TYPE_HEADER_COMMON) {
                NotificationSettingsFragment.this.mCommonSection.bindHeader(viewHolder);
            } else if (itemViewType == TYPE_APP_COMMON) {
                NotificationSettingsFragment.this.mCommonSection.bindItem((SwitchViewHolder) viewHolder, position - 2);
            } else if (itemViewType == TYPE_HEADER_ALL) {
                NotificationSettingsFragment.this.mAllSection.bindHeader(viewHolder);
            } else {
                NotificationSettingsFragment.this.mAllSection.bindItem((SwitchViewHolder) viewHolder, position - size - 3);
            }
        }
    }

    class AllAppsSection extends AppSwitchSection {
        AllAppsSection(int i, int i2, List<AppsInfoDataHelper.AppInfo> list) {
            super(i, i2, list);
        }

        public static /* synthetic */ void lambda$bindHeader$0(AllAppsSection allAppsSection, NotificationSettingsFragment notificationSettingsFragment, CompoundButton compoundButton, boolean z) {
            notificationSettingsFragment.mController.setAppDefaultEnabled(z);
            Iterator<AppsInfoDataHelper.AppInfo> it = allAppsSection.mAppInfos.iterator();
            while (it.hasNext()) {
                notificationSettingsFragment.mController.clearAppEnabled(it.next().packageName);
            }
            notificationSettingsFragment.mAppsAdapter.notifyDataSetChanged();
        }

        @Override
        public void bindHeader(RecyclerView.ViewHolder viewHolder) {
            super.bindHeader(viewHolder);
            SectionHeaderViewHolder sectionHeaderViewHolder = (SectionHeaderViewHolder) viewHolder;
            sectionHeaderViewHolder.mSwitch.setEnabled(NotificationSettingsFragment.this.mController.isEnabled());
            sectionHeaderViewHolder.mSwitch.setVisibility(0);
            sectionHeaderViewHolder.mSwitch.setChecked(NotificationSettingsFragment.this.mController.isAppDefaultEnabled());
            sectionHeaderViewHolder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                    NotificationSettingsFragment.AllAppsSection.lambda$bindHeader$0(AllAppsSection.this, NotificationSettingsFragment.this, compoundButton, z);
                }
            });
        }
    }

    class AppSwitchSection {
        List<AppsInfoDataHelper.AppInfo> mAppInfos;
        int mSectionDescription;
        int mSectionTitle;

        AppSwitchSection(int i, int i2, List<AppsInfoDataHelper.AppInfo> list) {
            this.mSectionTitle = i;
            this.mSectionDescription = i2;
            this.mAppInfos = list;
        }

        public void bindHeader(RecyclerView.ViewHolder viewHolder) {
            SectionHeaderViewHolder sectionHeaderViewHolder = (SectionHeaderViewHolder) viewHolder;
            sectionHeaderViewHolder.mName.setText(this.mSectionTitle);
            sectionHeaderViewHolder.mSubtext.setText(this.mSectionDescription);
            sectionHeaderViewHolder.mSwitch.setVisibility(8);
        }

        public void bindItem(SwitchViewHolder switchViewHolder, int i) {
            bindItem(switchViewHolder, (AppsInfoDataHelper.AppInfo) this.mAppInfos.get(i));
        }

        void bindItem(SwitchViewHolder switchViewHolder, AppsInfoDataHelper.AppInfo appInfo) {
            final String str = appInfo.packageName;
            boolean zIsAppWorking = NotificationSettingsFragment.this.mController.isAppWorking(str);
            switchViewHolder.mSwitch.setOnCheckedChangeListener(null);
            switchViewHolder.itemView.setOnClickListener(null);
            switchViewHolder.mSwitch.setChecked(zIsAppWorking);
            switchViewHolder.mLabel.setText(appInfo.appLabel);
            switchViewHolder.mImage.setImageDrawable(null);
            IconLoader.getInstance().loadInto(switchViewHolder.mImage, HybridNetworkFetcherCommon.getMagicResourceUri(appInfo.packageName, appInfo.activityName, appInfo.appVersion));
            switchViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public final void onClick(View view) {
                    NotificationSettingsFragment.this.setEnabled(str, !switchViewHolder.mSwitch.isChecked());
                }
            });
            switchViewHolder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                    NotificationSettingsFragment.this.setEnabled(str, z);
                }
            });
            switchViewHolder.mSwitch.setEnabled(NotificationSettingsFragment.this.mController.isEnabled());
        }
    }

    class ConfigSection {
        ConfigViewHolder mConfigViewHolder;

        public RecyclerView.ViewHolder createViewHolder(LayoutInflater layoutInflater, ViewGroup parent) {
            this.mConfigViewHolder = new ConfigViewHolder(layoutInflater.inflate(R.layout.settings_activity_notification_config, parent, false));
            return this.mConfigViewHolder;
        }

        public void lambda$bind$0(NotificationController.Style style, View view) {
            ArrayList arrayList = new ArrayList();
            for (NotificationController.Style style2 : NotificationController.Style.values()) {
                arrayList.add(new MultipleChoiceDialogFragment.ListItem(style2.hashCode(), style2.getIcon(), style2.getLabel(), 0));
            }
            MultipleChoiceDialogFragment multipleChoiceDialogFragmentNewInstance = MultipleChoiceDialogFragment.newInstance(R.string.app_settings_badge_style, !style.equals(NotificationController.Style.COUNT) ? 1 : 0, arrayList, R.layout.dialog_icon_radiobutton_item);
            multipleChoiceDialogFragmentNewInstance.setTargetFragment(null, 103);
            multipleChoiceDialogFragmentNewInstance.show(NotificationSettingsFragment.this.getChildFragmentManager(), null);
        }

        public void bind() {
            BadgeableAppIcon badgeableAppIcon;
            final ConfigViewHolder configViewHolder = this.mConfigViewHolder;
            for (int i = 0; i < configViewHolder.mIcons.size() && i < NotificationSettingsFragment.this.mCommonApps.size() && (badgeableAppIcon = configViewHolder.mIcons.get(i)) != null; i++) {
                badgeableAppIcon.setAppInfo((AppsInfoDataHelper.AppInfo) NotificationSettingsFragment.this.mCommonApps.get(i));
                if (NotificationSettingsFragment.this.mController.isEnabled()) {
                    badgeableAppIcon.setBadgeCount(5);
                } else {
                    badgeableAppIcon.setBadgeCount(0);
                }
            }
            DiscreteSeekBar.OnProgressChangeListener onProgressChangeListener = new DiscreteSeekBar.OnProgressChangeListener() {
                @Override
                public void onProgressChanged(DiscreteSeekBar discreteSeekBar, int i2, boolean z) {
                    if (discreteSeekBar == configViewHolder.mSizeSeek.mDiscreteSeekBar) {
                        float f = i2 / 100.0f;
                        NotificationSettingsFragment.this.mController.setBadgeScale(f);
                        BadgeDrawable.setScaleFactor(f);
                    }
                    for (BadgeableAppIcon badgeableAppIcon2 : configViewHolder.mIcons) {
                        badgeableAppIcon2.requestLayout();
                        badgeableAppIcon2.invalidate();
                    }
                }

                @Override
                public void onStartTrackingTouch(DiscreteSeekBar discreteSeekBar) {
                }

                @Override
                public void onStopTrackingTouch(DiscreteSeekBar discreteSeekBar) {
                }
            };
            configViewHolder.mSizeSeek.setEnabled(NotificationSettingsFragment.this.mController.isEnabled());
            configViewHolder.mSizeSeek.mLabel.setText(R.string.app_settings_size_label);
            configViewHolder.mSizeSeek.mDiscreteSeekBar.setMin(60);
            configViewHolder.mSizeSeek.mDiscreteSeekBar.setMax(100);
            configViewHolder.mSizeSeek.mDiscreteSeekBar.setValue((int) (NotificationSettingsFragment.this.mController.getBadgeScale() * 100.0f));
            configViewHolder.mSizeSeek.mDiscreteSeekBar.setOnProgressChangeListener(onProgressChangeListener);
            configViewHolder.mBadgeStyle.mLabel.setText(R.string.app_settings_badge_style);
            final NotificationController.Style badgeStyle = NotificationSettingsFragment.this.mController.getBadgeStyle();
            configViewHolder.mBadgeStyle.mSubLabel.setText(badgeStyle.getLabel());
            configViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public final void onClick(View view) {
                    ConfigSection.this.lambda$bind$0( badgeStyle, view);
                }
            });
        }
    }

    static class ConfigViewHolder extends RecyclerView.ViewHolder {
        SettingsTextViewHolder mBadgeStyle;
        List<BadgeableAppIcon> mIcons;
        SettingsSeekBarViewHolder mSizeSeek;

        public ConfigViewHolder(View view) {
            super(view);
            this.mIcons = new ArrayList();
            int[] iArr = {R.id.icon1, R.id.icon2, R.id.icon3, R.id.icon4};
            CellParams cellParamsForType = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getCellParamsForType(DeviceProfile.ParamType.ALLAPPS);
            for (int i : iArr) {
                BadgeableAppIcon badgeableAppIcon = (BadgeableAppIcon) view.findViewById(i);
                badgeableAppIcon.setCellParams(cellParamsForType);
                this.mIcons.add(badgeableAppIcon);
            }
            this.mSizeSeek = new SettingsSeekBarViewHolder(view.findViewById(R.id.badge_size_seek));
            this.mBadgeStyle = new SettingsTextViewHolder(view.findViewById(R.id.badge_style));
        }
    }

    static class SectionHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView mName;
        TextView mSubtext;
        SwitchCompat mSwitch;

        public SectionHeaderViewHolder(View view) {
            super(view);
            this.mName = (TextView) view.findViewById(R.id.name);
            this.mSubtext = (TextView) view.findViewById(R.id.subtext);
            this.mSwitch = (SwitchCompat) view.findViewById(R.id.item_switch);
        }
    }

    static class SwitchViewHolder extends RecyclerView.ViewHolder {
        ImageView mImage;
        TextView mLabel;
        TextView mSubLabel;
        SwitchCompat mSwitch;

        public SwitchViewHolder(View view) {
            super(view);
            this.mImage = (ImageView) view.findViewById(R.id.list_item_icon);
            this.mLabel = (TextView) view.findViewById(R.id.line1);
            this.mSubLabel = (TextView) view.findViewById(R.id.line2);
            this.mSwitch = (SwitchCompat) view.findViewById(R.id.item_switch);
        }
    }

    private void cleanupPendingData() {
        this.mPendingWidgetExtractor = null;
        this.mPendingPackageToEnable = null;
        this.mAppsAdapter.notifyDataSetChanged();
    }

    private void configureWidget(int i) {
        AppWidgetProviderInfo appWidgetInfo = AppWidgetManager.getInstance(getActivity()).getAppWidgetInfo(i);
        if (appWidgetInfo == null) {
            return;
        }
        if (appWidgetInfo.configure == null) {
            createWidget(i);
            return;
        }
        Intent intent = new Intent("android.appwidget.action.APPWIDGET_CONFIGURE");
        intent.setComponent(appWidgetInfo.configure);
        intent.putExtra("appWidgetId", i);
        startActivityForResult(intent, 101);
    }

    private void createWidget(int i) {
        WidgetExtractor widgetExtractor = this.mPendingWidgetExtractor;
        if (widgetExtractor == null) {
            return;
        }
        widgetExtractor.createWidget(i);
        this.mPendingWidgetExtractor = null;
        setEnabled(this.mPendingPackageToEnable, true);
    }

    public void lambda$onViewCreated$0(CompoundButton compoundButton, boolean z) {
        this.mController.setEnabled(z);
        this.mAppsAdapter.notifyDataSetChanged();
    }

    public void lambda$setupPermissionsView$1(View view) {
        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        FragmentActivity activity = this.getActivity();
        if (activity instanceof SafeActivityStarter) {
            ((SafeActivityStarter) activity).startActivitySafely(intent);
        } else {
            activity.startActivity(intent);
        }
    }

    private void setupMainView() {
        this.mAppsRecyclerView.setVisibility(0);
        this.mSwitchContainer.setVisibility(0);
        this.mInitContainer.setVisibility(8);
    }

    private void setupPermissionsView() {
        this.mAppsRecyclerView.setVisibility(8);
        this.mSwitchContainer.setVisibility(8);
        this.mInitContainer.setVisibility(0);
        ((TextView) this.mInitContainer.findViewById(R.id.description)).setText(R.string.app_settings_badge_notification_permission_description);
        Button button = (Button) this.mInitContainer.findViewById(R.id.button);
        button.setText(R.string.app_settings_badge_notification_permission_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                NotificationSettingsFragment.this.lambda$setupPermissionsView$1( view);
            }
        });
    }

    @Override
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i2 != -1) {
            cleanupPendingData();
            return;
        }
        if (i == 100) {
            if (intent == null) {
                return;
            }
            configureWidget(intent.getIntExtra("appWidgetId", 0));
            return;
        }
        if (i == 101) {
            if (intent == null) {
                return;
            }
            /* REWORK: jadx dropped this exception-flow local. */
            int intExtra = 0;
            intExtra = intent != null ? intent.getIntExtra("appWidgetId", 0) : 0;
            if (intExtra != 0) {
                createWidget(intExtra);
                return;
            } else {
                cleanupPendingData();
                return;
            }
        }
        if (i == 102) {
            setEnabled(this.mPendingPackageToEnable, true);
            return;
        }
        if (i == 103) {
            int longExtra = (int) intent.getLongExtra("ID", -1L);
            for (NotificationController.Style style : NotificationController.Style.values()) {
                if (longExtra == style.hashCode()) {
                    this.mController.setBadgeStyle(style);
                    BadgeDrawable.setStyle(style);
                    break;
                }
            }
            this.mAppsAdapter.notifyDataSetChanged();
            for (BadgeableAppIcon badgeableAppIcon : this.mConfigSection.mConfigViewHolder.mIcons) {
                badgeableAppIcon.requestLayout();
                badgeableAppIcon.invalidate();
            }
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mController = NotificationController.getInstance(getActivity());
        this.mCommonApps = new ArrayList();
        this.mAllApps = new ArrayList();
        this.mStartedConfiguration = false;
        this.mAppsInfoDataHelper = new AppsInfoDataHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.mLayoutInflater = layoutInflater;
        this.mSwitchContainer = layoutInflater.inflate(R.layout.toolbar_switch, (ViewGroup) null);
        this.mEnabledSwitch = (SwitchCompat) this.mSwitchContainer.findViewById(R.id.toolbar_switch);
        return layoutInflater.inflate(R.layout.settings_activity_notification_fragment, viewGroup, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.mController.start();
    }

    @Override
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        boolean z = false;
        for (int i2 = 0; i2 < strArr.length; i2++) {
            if (iArr[i2] != 0) {
                z = true;
            }
        }
        if (!z) {
            this.mStartedConfiguration = true;
        } else {
            this.mAppsAdapter.notifyDataSetChanged();
            this.mPendingPackageToEnable = null;
        }
    }

    @Override
    public void onResume() {
        String str;
        super.onResume();
        if (this.mSwitchContainer.getParent() == null) {
            ((TransparentSettingsActivity) getActivity()).getToolbarContainer().addView(this.mSwitchContainer);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowCustomEnabled(true);
        }
        if (!this.mStartedConfiguration || (str = this.mPendingPackageToEnable) == null) {
            this.mAppsAdapter.notifyDataSetChanged();
        } else {
            setEnabled(str, true);
            this.mStartedConfiguration = false;
        }
    }

    @Override
    public void onStart() {
        String str;
        super.onStart();
        this.mController.stop();
        this.mCommonApps.clear();
        this.mAllApps.clear();
        HashMap map = new HashMap();
        for (Extractor extractor : Extractor.getValidExtractors(getActivity())) {
            map.put(extractor.getPackageId(), extractor.getActivityName());
        }
        Notifier notifier = Notifier.getInstance(getActivity());
        Iterator<AppsInfoDataHelper.AppInfo> it = this.mAppsInfoDataHelper.getAllApps().iterator();
        while (true) {
            boolean z = true;
            if (!it.hasNext()) {
                break;
            }
            AppsInfoDataHelper.AppInfo next = it.next();
            boolean zIsHighAccuracy = notifier.isHighAccuracy(next.packageName);
            if (!map.containsKey(next.packageName) || ((str = (String) map.get(next.packageName)) != null && !str.equals(next.activityName))) {
                z = zIsHighAccuracy;
            }
            if (z) {
                this.mCommonApps.add(next);
            } else {
                this.mAllApps.add(next);
            }
        }
        Comparator<AppsInfoDataHelper.AppInfo> comparator = new Comparator<AppsInfoDataHelper.AppInfo>() {
            final Collator mCollator = Collator.getInstance(Locale.getDefault());

            {
                this.mCollator.setStrength(0);
            }

            @Override
            public int compare(AppsInfoDataHelper.AppInfo appInfo, AppsInfoDataHelper.AppInfo appInfo2) {
                return this.mCollator.compare(appInfo.appLabel, appInfo2.appLabel);
            }
        };
        Collections.sort(this.mCommonApps, comparator);
        Collections.sort(this.mAllApps, comparator);
        ComponentName componentName = new ComponentName(getActivity(), (Class<?>) NotificationListener.class);
        String string = Settings.Secure.getString(getActivity().getContentResolver(), "enabled_notification_listeners");
        if (!(string != null && string.contains(componentName.flattenToString()))) {
            setupPermissionsView();
        } else {
            setupMainView();
            this.mAppsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        this.mController.save();
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.mInitContainer = view.findViewById(R.id.init_container);
        this.mEnabledSwitch.setChecked(this.mController.isEnabled());
        this.mEnabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                NotificationSettingsFragment.this.lambda$onViewCreated$0( compoundButton, z);
            }
        });
        this.mAppsRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        this.mAppsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.mCommonSection = new AppSwitchSection(R.string.app_settings_badge_section_unread, R.string.app_settings_badge_section_unread_description, this.mCommonApps);
        this.mAllSection = new AllAppsSection(R.string.app_settings_badge_section_notification, R.string.app_settings_badge_section_notification_description, this.mAllApps);
        this.mConfigSection = new ConfigSection();
        this.mAppsAdapter = new AppsAdapter();
        this.mAppsRecyclerView.setAdapter(this.mAppsAdapter);
    }

    public void setEnabled(String str, boolean z) {
        Extractor extractor = this.mController.getExtractor(str);
        boolean z2 = false;
        if (!z) {
            this.mController.setAppEnabled(str, false);
            this.mAppsAdapter.notifyDataSetChanged();
            return;
        }
        if (!this.mController.isExtractorRequired(extractor)) {
            z2 = true;
        } else if (extractor.hasPermission() && extractor.isConfigured()) {
            z2 = true;
        }
        if (z2) {
            this.mController.setAppEnabled(str, true);
            if (this.mPendingPackageToEnable != null) {
                this.mPendingPackageToEnable = null;
            }
            this.mAppsAdapter.notifyDataSetChanged();
            return;
        }
        this.mPendingPackageToEnable = str;
        if (!extractor.hasPermission()) {
            requestPermissions(extractor.getPermissions(), 1);
            return;
        }
        if (extractor.isConfigured()) {
            return;
        }
        if (!(extractor instanceof WidgetExtractor)) {
            if (extractor instanceof GmailExtractor) {
                Fragment notificationSettingsGmailConfigFragment = new NotificationSettingsGmailConfigFragment();
                this.mStartedConfiguration = true;
                FragmentTransaction fragmentTransactionBeginTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransactionBeginTransaction.setCustomAnimations(R.anim.fade_in_short, R.anim.fade_out_short, R.anim.fade_in_short, R.anim.fade_out_short);
                fragmentTransactionBeginTransaction.replace(R.id.container, notificationSettingsGmailConfigFragment);
                fragmentTransactionBeginTransaction.setBreadCrumbTitle(R.string.app_settings_badge_configure_gmail);
                fragmentTransactionBeginTransaction.addToBackStack(null);
                fragmentTransactionBeginTransaction.commit();
                return;
            }
            return;
        }
        this.mPendingWidgetExtractor = (WidgetExtractor) extractor;
        int iAllocateAppWidgetId = InterceptingWidgetHost.getInstance(getActivity()).allocateAppWidgetId();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getActivity());
        ComponentName widgetComponent = this.mPendingWidgetExtractor.getWidgetComponent();
        try {
            if (appWidgetManager.bindAppWidgetIdIfAllowed(iAllocateAppWidgetId, widgetComponent)) {
                configureWidget(iAllocateAppWidgetId);
            } else {
                Intent intent = new Intent("android.appwidget.action.APPWIDGET_BIND");
                intent.putExtra("appWidgetId", iAllocateAppWidgetId);
                intent.putExtra("appWidgetProvider", widgetComponent);
                startActivityForResult(intent, 100);
            }
        } catch (IllegalArgumentException unused) {
        }
    }
}