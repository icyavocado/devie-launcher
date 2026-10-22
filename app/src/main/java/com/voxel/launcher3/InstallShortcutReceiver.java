package com.voxel.launcher3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.simplesearchlauncher.lock.LockOverlayActivity;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

/* JADX INFO: loaded from: classes.dex */
public class InstallShortcutReceiver extends BroadcastReceiver {
    private static Object sLock = new Object();
    private static boolean mUseInstallQueue = false;

    public static class PendingInstallShortcutInfo {
        Intent data;
        Bitmap icon;
        Intent.ShortcutIconResource iconResource;
        Intent launchIntent;
        String name;

        public PendingInstallShortcutInfo(Intent intent, String str, Intent intent2) {
            this.data = intent;
            this.name = str;
            this.launchIntent = intent2;
            Parcelable parcelableExtra = this.data.getParcelableExtra("android.intent.extra.shortcut.ICON");
            if (parcelableExtra instanceof Bitmap) {
                this.icon = (Bitmap) parcelableExtra;
            }
            Parcelable parcelableExtra2 = this.data.getParcelableExtra("android.intent.extra.shortcut.ICON_RESOURCE");
            if (parcelableExtra2 instanceof Intent.ShortcutIconResource) {
                this.iconResource = (Intent.ShortcutIconResource) parcelableExtra2;
            }
        }
    }

    public static void addToInstallQueue(SharedPreferences sharedPreferences, PendingInstallShortcutInfo pendingInstallShortcutInfo) {
        synchronized (sLock) {
            try {
                JSONStringer jSONStringerValue = new JSONStringer().object().key("intent.data").value(pendingInstallShortcutInfo.data.toUri(0)).key("intent.launch").value(pendingInstallShortcutInfo.launchIntent.toUri(0)).key("name").value(pendingInstallShortcutInfo.name);
                if (pendingInstallShortcutInfo.icon != null) {
                    byte[] bArrFlattenBitmap = Utilities.flattenBitmap(pendingInstallShortcutInfo.icon);
                    jSONStringerValue = jSONStringerValue.key("icon").value(Base64.encodeToString(bArrFlattenBitmap, 0, bArrFlattenBitmap.length, 0));
                }
                if (pendingInstallShortcutInfo.iconResource != null) {
                    jSONStringerValue = jSONStringerValue.key("iconResource").value(pendingInstallShortcutInfo.iconResource.resourceName).key("iconResourcePackage").value(pendingInstallShortcutInfo.iconResource.packageName);
                }
                JSONStringer jSONStringerEndObject = jSONStringerValue.endObject();
                SharedPreferences.Editor editorEdit = sharedPreferences.edit();
                addToStringSet(sharedPreferences, editorEdit, "apps_to_install", jSONStringerEndObject.toString());
                editorEdit.commit();
            } catch (JSONException e) {
                Log.d("InstallShortcutReceiver", "Exception when adding shortcut: " + e);
            }
        }
    }

    private static void addToStringSet(SharedPreferences sharedPreferences, SharedPreferences.Editor editor, String str, String str2) {
        Set<String> stringSet = sharedPreferences.getStringSet(str, null);
        HashSet hashSet = stringSet == null ? new HashSet(1) : new HashSet(stringSet);
        hashSet.add(str2);
        editor.putStringSet(str, hashSet);
    }

    static int disableAndFlushInstallQueue(Context context) {
        mUseInstallQueue = false;
        return flushInstallQueue(context);
    }

    static void enableInstallQueue() {
        mUseInstallQueue = true;
    }

    private static CharSequence ensureValidName(Context context, Intent intent, CharSequence charSequence) {
        if (charSequence != null) {
            return charSequence;
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            return packageManager.getActivityInfo(intent.getComponent(), 0).loadLabel(packageManager).toString();
        } catch (PackageManager.NameNotFoundException unused) {
            return "";
        }
    }

    static int flushInstallQueue(Context context) {
        ArrayList<PendingInstallShortcutInfo> andClearInstallQueue = getAndClearInstallQueue(context.getSharedPreferences(LauncherAppState.getSharedPreferencesKey(), 0));
        if (andClearInstallQueue.isEmpty()) {
            return 0;
        }
        SettingsStorage.init(context);
        if (SettingsStorage.getInstance().isDesktopLockedCurrently()) {
            andClearInstallQueue.clear();
            Intent intent = new Intent(context, (Class<?>) LockOverlayActivity.class);
            intent.addFlags(268435456);
            context.startActivity(intent);
            return 0;
        }
        ArrayList<ItemInfo> arrayList = new ArrayList<>();
        for (PendingInstallShortcutInfo pendingInstallShortcutInfo : andClearInstallQueue) {
            Intent intent2 = pendingInstallShortcutInfo.launchIntent;
            String str = pendingInstallShortcutInfo.name;
            if (!LauncherAppState.isDisableAllApps() || isValidShortcutLaunchIntent(intent2)) {
                LauncherModel.shortcutExists(context, str, intent2);
                String packageName = intent2.getPackage();
                if (packageName == null) {
                    packageName = intent2.getComponent() == null ? null : intent2.getComponent().getPackageName();
                }
                if (packageName == null || packageName.isEmpty() || LauncherModel.isValidPackage(context, packageName, UserHandleCompat.myUserHandle())) {
                    arrayList.add(getShortcutInfo(context, pendingInstallShortcutInfo.data, pendingInstallShortcutInfo.launchIntent));
                }
            }
        }
        if (arrayList.isEmpty()) {
            return 0;
        }
        int size = arrayList.size();
        LauncherAppState.getInstance().getModel().addAndBindAddedWorkspaceApps(context, arrayList);
        return size;
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    private static ArrayList<PendingInstallShortcutInfo> getAndClearInstallQueue(SharedPreferences sharedPreferences) {
        synchronized (sLock) {
            try {
                Set<String> stringSet = sharedPreferences.getStringSet("apps_to_install", null);
                if (stringSet == null) {
                    return new ArrayList<>();
                }
                ArrayList<PendingInstallShortcutInfo> arrayList = new ArrayList<>();
                Iterator<String> it = stringSet.iterator();
                while (it.hasNext()) {
                    try {
                        try {
                            JSONObject jSONObject = (JSONObject) new JSONTokener(it.next()).nextValue();
                            Intent uri = Intent.parseUri(jSONObject.getString("intent.data"), 0);
                            Intent uri2 = Intent.parseUri(jSONObject.getString("intent.launch"), 0);
                            String string = jSONObject.getString("name");
                            String strOptString = jSONObject.optString("icon");
                            String strOptString2 = jSONObject.optString("iconResource");
                            String strOptString3 = jSONObject.optString("iconResourcePackage");
                            if (strOptString != null && !strOptString.isEmpty()) {
                                byte[] bArrDecode = Base64.decode(strOptString, 0);
                                uri.putExtra("android.intent.extra.shortcut.ICON", BitmapFactory.decodeByteArray(bArrDecode, 0, bArrDecode.length));
                            } else if (strOptString2 != null && !strOptString2.isEmpty()) {
                                Intent.ShortcutIconResource shortcutIconResource = new Intent.ShortcutIconResource();
                                shortcutIconResource.resourceName = strOptString2;
                                shortcutIconResource.packageName = strOptString3;
                                uri.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", shortcutIconResource);
                            }
                            uri.putExtra("android.intent.extra.shortcut.INTENT", uri2);
                            arrayList.add(new PendingInstallShortcutInfo(uri, string, uri2));
                        } catch (JSONException e) {
                            Log.d("InstallShortcutReceiver", "Exception reading shortcut to add: " + e);
                        }
                    } catch (URISyntaxException e2) {
                        Log.d("InstallShortcutReceiver", "Exception reading shortcut to add: " + e2);
                    }
                }
                sharedPreferences.edit().putStringSet("apps_to_install", new HashSet()).commit();
                return arrayList;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private static ShortcutInfo getShortcutInfo(Context context, Intent intent, Intent intent2) {
        if (intent2.getAction() == null) {
            intent2.setAction("android.intent.action.VIEW");
        } else if (intent2.getAction().equals("android.intent.action.MAIN") && intent2.getCategories() != null && intent2.getCategories().contains("android.intent.category.LAUNCHER")) {
            intent2.addFlags(270532608);
        }
        ShortcutInfo shortcutInfoInfoFromShortcutIntent = LauncherAppState.getInstance().getModel().infoFromShortcutIntent(context, intent);
        shortcutInfoInfoFromShortcutIntent.title = ensureValidName(context, intent2, shortcutInfoInfoFromShortcutIntent.title);
        return shortcutInfoInfoFromShortcutIntent;
    }

    static boolean isValidShortcutLaunchIntent(Intent intent) {
        return intent == null || !"android.intent.action.MAIN".equals(intent.getAction()) || intent.getComponent() == null || intent.getCategories() == null || intent.getCategories().size() != 1 || !intent.hasCategory("android.intent.category.LAUNCHER") || (intent.getExtras() != null && (intent.getExtras().size() != 1 || intent.getLongExtra("profile", Long.MIN_VALUE) == Long.MIN_VALUE)) || !TextUtils.isEmpty(intent.getDataString());
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public static void removeFromInstallQueue(SharedPreferences sharedPreferences, HashSet<String> hashSet) {
        if (hashSet.isEmpty()) {
            return;
        }
        synchronized (sLock) {
            try {
                Set<String> stringSet = sharedPreferences.getStringSet("apps_to_install", null);
                if (stringSet != null) {
                    HashSet hashSet2 = new HashSet(stringSet);
                    Iterator it = hashSet2.iterator();
                    while (it.hasNext()) {
                        try {
                            try {
                                Intent uri = Intent.parseUri(((JSONObject) new JSONTokener((String) it.next()).nextValue()).getString("intent.launch"), 0);
                                String packageName = uri.getPackage();
                                if (packageName == null) {
                                    if (uri.getComponent() != null) {
                                        packageName = uri.getComponent().getPackageName();
                                    }
                                }
                                if (hashSet.contains(packageName)) {
                                    it.remove();
                                }
                            } catch (JSONException e) {
                                Log.d("InstallShortcutReceiver", "Exception reading shortcut to remove: " + e);
                            }
                        } catch (URISyntaxException e2) {
                            Log.d("InstallShortcutReceiver", "Exception reading shortcut to remove: " + e2);
                        }
                    }
                    sharedPreferences.edit().putStringSet("apps_to_install", new HashSet(hashSet2)).commit();
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        Intent intent2;
        if ("com.android.launcher.action.INSTALL_SHORTCUT".equals(intent.getAction()) && (intent2 = (Intent) intent.getParcelableExtra("android.intent.extra.shortcut.INTENT")) != null) {
            String packageName = intent2.getPackage();
            if (packageName == null) {
                packageName = intent2.getComponent() == null ? null : intent2.getComponent().getPackageName();
            }
            if (packageName != null) {
                try {
                    if (System.currentTimeMillis() - context.getPackageManager().getPackageInfo(packageName, 0).firstInstallTime < 30000) {
                        return;
                    }
                } catch (PackageManager.NameNotFoundException unused) {
                }
            }
            String string = ensureValidName(context, intent2, intent.getStringExtra("android.intent.extra.shortcut.NAME")).toString();
            LauncherAppState.setApplicationContext(context.getApplicationContext());
            boolean z = LauncherAppState.getInstance().getDynamicGrid() == null;
            addToInstallQueue(context.getSharedPreferences(LauncherAppState.getSharedPreferencesKey(), 0), new PendingInstallShortcutInfo(intent, string, intent2));
            if (mUseInstallQueue || z) {
                return;
            }
            flushInstallQueue(context);
        }
    }
}
