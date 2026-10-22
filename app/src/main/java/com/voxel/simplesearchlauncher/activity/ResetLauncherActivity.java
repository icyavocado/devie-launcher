package com.voxel.simplesearchlauncher.activity;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import com.voxel.launcher3.Launcher;
import com.voxel.simplesearchlauncher.utils.LaunchUtil;
import com.ovacoda.devie.BuildConfig;

/* JADX INFO: loaded from: classes.dex */
public class ResetLauncherActivity extends Activity {
    public static final String RESET_EXTRA_KEY = ResetLauncherActivity.class.getCanonicalName() + ".reset_extra";
    public static final String VERIFY_EXTRA_KEY = ResetLauncherActivity.class.getCanonicalName() + ".verify_extra";
    private static final ComponentName sResetComponentName = new ComponentName(BuildConfig.APPLICATION_ID, ResetLauncherActivity.class.getName());

    public static void clearHome(Context context) {
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.MAIN");
            intentFilter.addCategory("android.intent.category.HOME");
            intentFilter.addCategory("android.intent.category.DEFAULT");
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.HOME");
            intent.setComponent(null);
            Class<?> cls = Class.forName("android.app.AppGlobals");
            Class.forName("android.content.pm.IPackageManager").getMethod("setLastChosenActivity", Intent.class, String.class, Integer.TYPE, IntentFilter.class, Integer.TYPE, ComponentName.class).invoke(cls.getMethod("getPackageManager", new Class[0]).invoke(cls, (Object[]) null), intent, intent.resolveTypeIfNeeded(context.getContentResolver()), 65536, intentFilter, 1081344, intent.getComponent());
        } catch (Exception unused) {
        }
    }

    public static boolean isDefaultHomeSet(Activity activity) {
        PackageManager packageManager = activity.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        ResolveInfo resolveInfoResolveActivity = packageManager.resolveActivity(intent, 65536);
        if (resolveInfoResolveActivity == null) {
            return false;
        }
        return !resolveInfoResolveActivity.activityInfo.packageName.equals("android" /* REWORK: was facebook SDK AbstractSpiCall constant */);
    }

    public static boolean isLauncherDefault(Context context) {
        /* REWORK: Samsung/modern Android holds the home default as a ROLE, not a
           PackageManager preferred-activity; resolveActivity-based checks see the
           resolver or the rival launcher. Ask the role system instead. */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = (RoleManager) context.getSystemService(RoleManager.class);
            return roleManager != null && roleManager.isRoleHeld(RoleManager.ROLE_HOME);
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        ResolveInfo resolveInfoResolveActivity = context.getPackageManager().resolveActivity(intent, 65536);
        if (resolveInfoResolveActivity != null) {
            return resolveInfoResolveActivity.activityInfo.name.equals(Launcher.class.getCanonicalName());
        }
        return false;
    }

    public static void resetHome(Activity activity, boolean z, boolean z2) {
        /* REWORK: the legacy dance (trampoline enable, resolveActivity, clearHome
           reflection, raw HOME startActivity) is all dead on modern Android — the
           launcher-loader/loader saw no role change and the raw HOME intent just
           foregrounded the currently-set home (the user reads this as "dEvie
           crashed"). ROLE_HOME request gives the one-tap system dialog. */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = (RoleManager) activity.getSystemService(RoleManager.class);
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)
                    && !roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                try {
                    activity.startActivityForResult(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME), 47161);
                } catch (Exception unused) {
                    LaunchUtil.startActivitySafely(activity, roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME));
                }
            }
            return;
        }
        PackageManager packageManager = activity.getPackageManager();
        packageManager.setComponentEnabledSetting(sResetComponentName, 1, 1);
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.putExtra(RESET_EXTRA_KEY, true);
        intent.putExtra(VERIFY_EXTRA_KEY, z);
        packageManager.resolveActivity(intent, 65536);
        packageManager.setComponentEnabledSetting(sResetComponentName, 2, 1);
        clearHome(activity);
        if (!isDefaultHomeSet(activity)) {
            activity.startActivity(intent);
            return;
        }
        Intent intent2 = new Intent(Build.MANUFACTURER.toLowerCase().equals("huawei") ? "android.settings.MANAGE_APPLICATIONS_SETTINGS" : "android.settings.HOME_SETTINGS");
        intent2.addFlags(268468224);
        LaunchUtil.startActivitySafely(activity.getApplicationContext(), intent2);
    }
}
