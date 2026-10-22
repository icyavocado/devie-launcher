package com.evie.search.local;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import com.evie.search.model.LocalSearchItem;
import com.ovacoda.devie.BuildConfig;
import java.util.ArrayList;
import java.util.List;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class SettingsDeeplinkManager {

    private enum SettingsConfig {
        Account("android.settings.SYNC_SETTINGS", R.string.settings_item_account),
        AirplaneMode("android.settings.AIRPLANE_MODE_SETTINGS", R.string.settings_item_airplane_mode),
        Apps("android.settings.APPLICATION_SETTINGS", R.string.settings_item_apps, R.string.settings_item_applications),
        BackupReset("android.settings.PRIVACY_SETTINGS", R.string.settings_item_backup_reset),
        Battery("android.intent.action.POWER_USAGE_SUMMARY", R.string.settings_item_battery),
        Bluetooth("android.settings.BLUETOOTH_SETTINGS", R.string.settings_item_bluetooth),
        DateTime("android.settings.DATE_SETTINGS", R.string.settings_item_date_time),
        DefaultApps("android.settings.MANAGE_DEFAULT_APPS_SETTINGS", R.string.settings_item_default_apps),
        Developer("android.settings.APPLICATION_DEVELOPMENT_SETTINGS", R.string.settings_item_developer),
        DeviceInfo("android.settings.DEVICE_INFO_SETTINGS", R.string.settings_item_device_info, R.string.settings_item_about_device),
        Display("android.settings.DISPLAY_SETTINGS", R.string.settings_item_display),
        Evie(BuildConfig.APPLICATION_ID, "com.voxel.simplesearchlauncher.settings.SettingsActivity", R.string.settings_item_evie),
        Home("android.settings.HOME_SETTINGS", R.string.settings_item_home, R.string.settings_item_launcher),
        Hotspot("com.android.settings", "com.android.settings.TetherSettings", R.string.settings_item_hotspot),
        Keyboard("android.settings.INPUT_METHOD_SETTINGS", R.string.settings_item_keyboard, R.string.settings_item_input_method),
        Language("android.settings.LOCALE_SETTINGS", R.string.settings_item_language),
        Location("android.settings.LOCATION_SOURCE_SETTINGS", R.string.settings_item_location),
        Nfc("android.settings.NFC_SETTINGS", R.string.settings_item_nfc),
        NetworkOperator("android.settings.NETWORK_OPERATOR_SETTINGS", R.string.settings_item_network_operator),
        NotificationAccess("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS", R.string.settings_item_notification_access),
        OverlayPermission("android.settings.action.MANAGE_OVERLAY_PERMISSION", R.string.settings_item_overlay_permission),
        Screensaver("android.settings.DREAM_SETTINGS", R.string.settings_item_screensaver, R.string.settings_item_daydream),
        Security("android.settings.SECURITY_SETTINGS", R.string.settings_item_security),
        Sound("android.settings.SOUND_SETTINGS", R.string.settings_item_sound),
        Storage("android.settings.INTERNAL_STORAGE_SETTINGS", R.string.settings_item_storage),
        Wallpaper(BuildConfig.APPLICATION_ID, "com.voxel.launcher3.LauncherWallpaperPickerActivity", R.string.settings_item_wallpaper),
        Wifi("android.settings.WIFI_SETTINGS", R.string.settings_item_wifi),
        Wireless("android.settings.WIRELESS_SETTINGS", R.string.settings_item_wireless);

        int altNameRes;
        Intent intent;
        String link;
        int nameRes;

        SettingsConfig(String str, int i) {
            this(str, i, -1);
        }

        SettingsConfig(String str, int i, int i2) {
            this.nameRes = i;
            this.altNameRes = i2;
            this.link = "settingsdeeplink:" + str;
            this.intent = new Intent(str);
        }

        SettingsConfig(String str, String str2, int i) {
            this.nameRes = i;
            this.altNameRes = -1;
            this.link = "settingsdeeplink:" + str + "|" + str2;
            this.intent = new Intent();
            this.intent.setClassName(str, str2);
        }

        public String getAlternativeName(Resources resources) {
            int i = this.altNameRes;
            if (i < 0) {
                return null;
            }
            return resources.getString(i);
        }

        public String getName(Resources resources) {
            return resources.getString(this.nameRes);
        }
    }

    static List<LocalSearchItem> getAvailableItems(Context context) {
        ArrayList arrayList = new ArrayList();
        for (SettingsConfig settingsConfig : SettingsConfig.values()) {
            if (!context.getPackageManager().queryIntentActivities(settingsConfig.intent, 0).isEmpty()) {
                String name = settingsConfig.getName(context.getResources());
                ArrayList arrayList2 = new ArrayList();
                LocalSearchUtil.addKeywords(arrayList2, name, "name");
                String alternativeName = settingsConfig.getAlternativeName(context.getResources());
                if (alternativeName != null) {
                    LocalSearchUtil.addKeywords(arrayList2, alternativeName, "altname");
                }
                arrayList.add(new LocalSearchItem(settingsConfig.link, name, arrayList2, "SETTINGS_DEEPLINK"));
            }
        }
        return arrayList;
    }
}
