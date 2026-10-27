package com.voxel.simplesearchlauncher.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import com.voxel.simplesearchlauncher.activity.OnboardingActivity;
import com.voxel.simplesearchlauncher.activity.ResetLauncherActivity;
import com.voxel.simplesearchlauncher.activity.SafeActivityStarter;
import com.voxel.simplesearchlauncher.activity.onboarding.PermissionsSlide;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import com.ovacoda.devie.BuildConfig;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.List;

/* Brave-style grouped settings list: small muted section header above one
   rounded card per group, text-only rows, thin inset dividers. Replaces the
   deprecated per-row cards and the vendored SectionedRecyclerViewAdapter. */
public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";

    private enum SettingsGroup {
        ALERT(0, false),
        CUSTOMIZATION(R.string.app_settings_item_customization_header, true),
        GENERAL(R.string.app_settings_item_general_header, true),
        SWITCH(0, true);

        int headerTextResId;
        boolean showHeader;

        SettingsGroup(int i, boolean z) {
            this.headerTextResId = i;
            this.showHeader = z;
        }
    }

    private enum SettingsItemType {
        PERMISSIONS_REQUIRED(SettingsGroup.ALERT, R.string.app_settings_item_permission_required),
        DESKTOP_SETTINGS(SettingsGroup.CUSTOMIZATION, R.string.app_settings_item_desktop_title),
        DOCK_SETTINGS(SettingsGroup.CUSTOMIZATION, R.string.app_settings_item_dock_title),
        CHANGE_ICON_PACK(SettingsGroup.CUSTOMIZATION, R.string.app_settings_item_icon_pack_title),
        FOLDER_SETTINGS(SettingsGroup.CUSTOMIZATION, R.string.app_settings_item_folder_title),
        BADGES(SettingsGroup.CUSTOMIZATION, R.string.app_settings_item_badges_title),
        GESTURES(SettingsGroup.CUSTOMIZATION, R.string.app_settings_item_gestures_title),
        APP_DRAWER(SettingsGroup.CUSTOMIZATION, R.string.app_settings_item_app_drawer_title),
        SEARCH(SettingsGroup.CUSTOMIZATION, R.string.app_settings_item_search_title),
        HIDE_APPS(SettingsGroup.CUSTOMIZATION, R.string.app_settings_hide_apps),
        BACKUP_AND_RESTORE(SettingsGroup.GENERAL, R.string.app_settings_item_backup_restore_title),
        FEEDBACK(SettingsGroup.GENERAL, R.string.app_settings_item_feedback_title),
        /* REWORK: privacy-policy web page cut; the row now shows the no-tracking copy. */
        PRIVACY_POLICY(SettingsGroup.GENERAL, R.string.app_settings_item_no_tracking_title),
        SWITCH_LAUNCHER(SettingsGroup.SWITCH, R.string.app_settings_item_switch_launcher);

        SettingsGroup group;
        int mainTextResId;

        SettingsItemType(SettingsGroup settingsGroup, int i2) {
            this.group = settingsGroup;
            this.mainTextResId = i2;
        }
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.settings_activity_top_level_fragment, viewGroup, false);
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        rebuild(view);
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        rebuild(getView());
    }

    private void rebuild(View view) {
        LinearLayout container = (LinearLayout) view.findViewById(R.id.list_container);
        container.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();
        boolean permissionsRequired = PermissionsSlide.needsPermissions(getActivity());
        for (SettingsGroup group : SettingsGroup.values()) {
            List<SettingsItemType> items = itemsFor(group);
            if (group == SettingsGroup.ALERT && !permissionsRequired) {
                continue;
            }
            if (group.showHeader && group.headerTextResId > 0) {
                View header = inflater.inflate(R.layout.settings_activity_top_level_header, container, false);
                ((TextView) header.findViewById(R.id.text)).setText(group.headerTextResId);
                container.addView(header);
            }
            LinearLayout card = new LinearLayout(getContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.settings_list_item_background);
            card.setDividerDrawable(new android.graphics.drawable.InsetDrawable(
                getResources().getDrawable(R.drawable.settings_group_divider), dp(25), 0, dp(15), 0));
            card.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            int margin = dp(10);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
            cardParams.setMargins(dp(12), 0, dp(12), margin);
            card.setLayoutParams(cardParams);
            for (SettingsItemType item : items) {
                TextView row = (TextView) inflater.inflate(R.layout.settings_activity_top_level_row, card, false);
                row.setText(item.mainTextResId);
                row.setOnClickListener(createClickListener(item));
                card.addView(row);
            }
            container.addView(card);
        }
        TextView footer = (TextView) inflater.inflate(R.layout.settings_activity_version_footer, container, false);
        footer.setText(getString(R.string.app_settings_version_footer, BuildConfig.VERSION_NAME, BuildConfig.GIT_SHA));
        container.addView(footer);
    }

    private List<SettingsItemType> itemsFor(SettingsGroup group) {
        List<SettingsItemType> items = new ArrayList<>();
        for (SettingsItemType item : SettingsItemType.values()) {
            if (item.group == group) {
                items.add(item);
            }
        }
        return items;
    }

    private View.OnClickListener createClickListener(final SettingsItemType item) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openItem(item);
            }
        };
    }

    private void openItem(SettingsItemType item) {
        switch (item) {
            case PERMISSIONS_REQUIRED:
                try {
                    Intent onboarding = new Intent(getActivity(), OnboardingActivity.class);
                    onboarding.putExtra("permission", true);
                    startActivity(onboarding);
                } catch (Exception unused) {
                }
                break;
            case DESKTOP_SETTINGS:
                startWallpaperFragmentActivity(DesktopSettingsFragment.class.getName(), item.mainTextResId);
                break;
            case DOCK_SETTINGS:
                startWallpaperFragmentActivity(DockSettingsFragment.class.getName(), item.mainTextResId);
                break;
            case FOLDER_SETTINGS:
                startWallpaperFragmentActivity(FolderSettingsFragment.class.getName(), item.mainTextResId);
                break;
            case APP_DRAWER:
                startWallpaperFragmentActivity(AppDrawerSettingsFragment.class.getName(), item.mainTextResId);
                break;
            case GESTURES:
                pushFragment(new CustomGestureSettingsFragment(), item.mainTextResId);
                break;
            case CHANGE_ICON_PACK:
                startWallpaperFragmentActivity(IconPackSettingsFragment.class.getName(), item.mainTextResId);
                break;
            case BADGES:
                startWallpaperFragmentActivity(NotificationSettingsFragment.class.getName(), item.mainTextResId);
                break;
            case SEARCH:
                pushFragment(new SearchSettingsFragment(), item.mainTextResId);
                break;
            case HIDE_APPS:
                pushFragment(new HideAppsSettingsFragment(), item.mainTextResId);
                break;
            case BACKUP_AND_RESTORE:
                pushFragment(new BackupRestoreSettingsFragment(), item.mainTextResId);
                break;
            case FEEDBACK:
                pushFragment(new FeedbackFragment(), item.mainTextResId);
                break;
            case PRIVACY_POLICY:
                new androidx.appcompat.app.AlertDialog.Builder(getActivity())
                        .setTitle(R.string.app_settings_item_no_tracking_title)
                        .setMessage(R.string.app_settings_no_tracking_blurb)
                        .setPositiveButton(R.string.app_settings_item_backup_restore_backup_success_dialog_positive, null)
                        .show();
                break;
            case SWITCH_LAUNCHER:
                ResetLauncherActivity.resetHome(getActivity(), true, false);
                AnalyticsHandler.getInstance().logEvent("ev_settings_switch_launcher", null);
                break;
        }
    }

    private void pushFragment(Fragment fragment, int i) {
        FragmentTransaction fragmentTransactionBeginTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransactionBeginTransaction.setCustomAnimations(R.anim.fade_in_short, R.anim.fade_out_short, R.anim.fade_in_short, R.anim.fade_out_short);
        fragmentTransactionBeginTransaction.replace(R.id.container, fragment);
        fragmentTransactionBeginTransaction.setBreadCrumbTitle(i);
        fragmentTransactionBeginTransaction.addToBackStack(null);
        fragmentTransactionBeginTransaction.commit();
    }

    private void startWallpaperFragmentActivity(String str, int i) {
        Intent intent = new Intent(getActivity(), TransparentSettingsActivity.class);
        intent.putExtra("show_title", i);
        intent.putExtra("show_fragment", str);
        FragmentActivity activity = getActivity();
        if (activity instanceof SafeActivityStarter) {
            ((SafeActivityStarter) activity).startActivitySafely(intent);
        } else {
            activity.startActivity(intent);
        }
    }

    private int dp(int dip) {
        return (int) (dip * getResources().getDisplayMetrics().density + 0.5f);
    }
}
