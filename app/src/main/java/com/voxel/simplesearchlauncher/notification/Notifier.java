package com.voxel.simplesearchlauncher.notification;

/* REWORK: minSdk-safe receiver registration (API 34+ flag rule) */
import androidx.core.content.ContextCompat;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Telephony;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import com.voxel.launcher3.ShortcutInfo;
import com.voxel.launcher3.Utilities;
import com.voxel.simplesearchlauncher.utils.UpgradeUtil;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/* JADX INFO: loaded from: classes2.dex */
public class Notifier implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static Notifier sInstance;
    BadgeAppsInfo mBadgeAppsInfo;
    INotificationController mController;
    boolean mEnabled;
    Handler mHandler;
    Map<OnNotificationChangeListener, Set<String>> mListenerMap;
    SharedPreferences mPreferences;
    private boolean mReceiverRegistered;
    private BroadcastReceiver mAppRemoveReceiver = new BroadcastReceiver() { // from class: com.voxel.simplesearchlauncher.notification.Notifier.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (!"android.intent.action.PACKAGE_REMOVED".equals(intent.getAction()) || intent.getBooleanExtra("android.intent.extra.REPLACING", false) || intent.getData() == null) {
                return;
            }
            Notifier.this.clearCount(intent.getData().getEncodedSchemeSpecificPart());
        }
    };
    Map<String, List<WeakReference<OnNotificationChangeListener>>> mListeners = new HashMap();
    Map<String, Integer> mTransientNotifications = Collections.synchronizedMap(new HashMap());
    Map<String, StatusBarNotification> mNotifications = Collections.synchronizedMap(new HashMap());

    public interface OnNotificationChangeListener {
        void onNotificationChanged(NotificationCount notificationCount);
    }

    private Notifier(final Context context) {
        this.mPreferences = context.getSharedPreferences("notifications", 0);
        this.mPreferences.registerOnSharedPreferenceChangeListener(this);
        this.mListenerMap = new WeakHashMap();
        this.mBadgeAppsInfo = new BadgeAppsInfo();
        this.mHandler = new Handler(Looper.getMainLooper());
        UpgradeUtil.processUpgrade(this.mPreferences, new UpgradeUtil.UpgradableComponent() { // from class: com.voxel.simplesearchlauncher.notification.-$$Lambda$Notifier$JDm1OXrSbRxkOenEBxHKf4U7Zjk
            @Override // com.voxel.simplesearchlauncher.utils.UpgradeUtil.UpgradableComponent
            public final void onUpgrade(int i, int i2) {
                Notifier.this.lambda$new$0( context, i, i2);
            }
        }, 2);
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    private synchronized void dispatchListeners(final NotificationCount notificationCount) {
        if (shouldNotifyForApp(notificationCount.packageName) || notificationCount.count == 0) {
            final List<WeakReference<OnNotificationChangeListener>> list = this.mListeners.get(notificationCount.packageName);
            if (list == null) {
                return;
            }
            ArrayList arrayList = new ArrayList();
            for (WeakReference<OnNotificationChangeListener> weakReference : list) {
                if (weakReference.get() == null) {
                    arrayList.add(weakReference);
                }
            }
            list.removeAll(arrayList);
            this.mHandler.post(new Runnable() { // from class: com.voxel.simplesearchlauncher.notification.-$$Lambda$Notifier$x_0iGkzJrSFCE2sMjBjaWWgHevM
                @Override // java.lang.Runnable
                public final void run() {
                    Notifier.lambda$dispatchListeners$2(list, notificationCount);
                }
            });
        }
    }

    private int getCountReal(String str, String str2) {
        int i;
        int i2 = this.mPreferences.getInt(getComponentName(str, str2), 0);
        if (i2 > 0) {
            return i2;
        }
        if (str2 != null && (i = this.mPreferences.getInt(str, 0)) > 0) {
            return i;
        }
        Integer num = this.mTransientNotifications.get(str);
        if (num != null) {
            return num.intValue();
        }
        return 0;
    }

    public static synchronized Notifier getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new Notifier(context.getApplicationContext());
        }
        synchronized (sInstance) {
            if (!sInstance.mReceiverRegistered) {
                IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_REMOVED");
                intentFilter.addDataScheme("package");
                ContextCompat.registerReceiver(context, sInstance.mAppRemoveReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
                sInstance.mReceiverRegistered = true;
            }
        }
        return sInstance;
    }

    private NotificationCount getNotificationForKey(String str) {
        if (str.endsWith("/source") || str.endsWith("/updated")) {
            return null;
        }
        try {
            NotificationCount notificationCount = new NotificationCount(str);
            notificationCount.count = getCount(notificationCount.packageName, notificationCount.activityName);
            return notificationCount;
        } catch (IllegalArgumentException unused) {
            return null;
        }
    }

    static /* synthetic */ void lambda$dispatchListeners$2(List list, NotificationCount notificationCount) {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            OnNotificationChangeListener onNotificationChangeListener = (OnNotificationChangeListener) ((WeakReference) it.next()).get();
            if (onNotificationChangeListener != null) {
                onNotificationChangeListener.onNotificationChanged(notificationCount);
            }
        }
    }

    public int lambda$getNotificationKeys$1(NotificationKeyData notificationKeyData, NotificationKeyData notificationKeyData2) {
        StatusBarNotification statusBarNotification = this.mNotifications.get(notificationKeyData.notificationKey);
        StatusBarNotification statusBarNotification2 = this.mNotifications.get(notificationKeyData2.notificationKey);
        if (statusBarNotification == null) {
            return statusBarNotification2 == null ? 0 : -1;
        }
        if (statusBarNotification2 == null) {
            return 1;
        }
        if (statusBarNotification.getPostTime() > statusBarNotification2.getPostTime()) {
            return -1;
        }
        return statusBarNotification.getPostTime() < statusBarNotification2.getPostTime() ? 1 : 0;
    }

    public void lambda$new$0(Context context, int i, int i2) {
        String defaultSmsPackage;
        if (i < 2) {
            String str = null;
            try {
                defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(context);
            } catch (Exception unused) {
                defaultSmsPackage = null;
            }
            if (defaultSmsPackage != null) {
                this.clearCount(defaultSmsPackage);
            }
            Intent intent = new Intent("android.intent.action.DIAL");
            intent.setData(Uri.parse("tel:123456789"));
            ResolveInfo resolveInfoResolveActivity = context.getPackageManager().resolveActivity(intent, 65536);
            if (resolveInfoResolveActivity != null && resolveInfoResolveActivity.activityInfo != null && !resolveInfoResolveActivity.activityInfo.packageName.equals("android" /* REWORK: was facebook SDK AbstractSpiCall constant */)) {
                str = resolveInfoResolveActivity.activityInfo.packageName;
            }
            this.clearCount(str);
        }
    }

    private boolean shouldNotifyForApp(String str) {
        INotificationController iNotificationController;
        if (!this.mEnabled || (iNotificationController = this.mController) == null) {
            return false;
        }
        return iNotificationController.isAppWorking(str);
    }

    public static boolean supportsNotifications(ShortcutInfo shortcutInfo) {
        ComponentName component;
        Intent intent = shortcutInfo.getIntent();
        return (intent == null || (component = intent.getComponent()) == null || shortcutInfo.itemType != 0 || component.getPackageName() == null) ? false : true;
    }

    @TargetApi(20)
    public void addNotification(StatusBarNotification statusBarNotification) {
        this.mNotifications.put(statusBarNotification.getKey(), statusBarNotification);
    }

    public void clearCount(String str) {
        if (str == null) {
            return;
        }
        ArrayList arrayList = new ArrayList();
        for (String str2 : this.mPreferences.getAll().keySet()) {
            NotificationCount notificationForKey = getNotificationForKey(str2);
            if (notificationForKey != null && str.equals(notificationForKey.packageName)) {
                arrayList.add(str2);
            }
        }
        if (arrayList.size() > 0) {
            SharedPreferences.Editor editorEdit = this.mPreferences.edit();
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                editorEdit.remove((String) it.next());
            }
            editorEdit.apply();
        }
        this.mTransientNotifications.remove(str);
        dispatchForPackage(str);
    }

    public void clearNotifications() {
        this.mNotifications.clear();
    }

    public void dispatchForPackage(String str) {
        if (str == null) {
            return;
        }
        NotificationCount notificationCount = new NotificationCount(str);
        notificationCount.count = getCountReal(str, null);
        boolean zShouldNotifyForApp = shouldNotifyForApp(str);
        if (notificationCount.count > 0) {
            if (!zShouldNotifyForApp) {
                notificationCount.count = 0;
            }
            dispatchListeners(notificationCount);
        }
    }

    public List<NotificationCount> getAllCounts() {
        ArrayList arrayList = new ArrayList();
        Iterator<String> it = this.mPreferences.getAll().keySet().iterator();
        while (it.hasNext()) {
            NotificationCount notificationForKey = getNotificationForKey(it.next());
            if (notificationForKey != null) {
                arrayList.add(notificationForKey);
            }
        }
        return arrayList;
    }

    public BadgeAppsInfo getBadgeAppsInfo() {
        return this.mBadgeAppsInfo;
    }

    protected String getComponentName(String str, String str2) {
        if (str == null) {
            return null;
        }
        return (str2 == null || str2.length() == 0) ? str : String.format("%s/%s", str, str2);
    }

    public int getCount(String str) {
        return getCount(str, null);
    }

    public int getCount(String str, String str2) {
        if (shouldNotifyForApp(str)) {
            return getCountReal(str, str2);
        }
        return 0;
    }

    public List<NotificationKeyData> getNotificationKeys(String str) {
        ArrayList arrayList = new ArrayList();
        if (!Utilities.ATLEAST_LOLLIPOP) {
            return arrayList;
        }
        for (StatusBarNotification statusBarNotification : this.mNotifications.values()) {
            if (statusBarNotification.getPackageName().equals(str)) {
                arrayList.add(NotificationKeyData.fromNotification(statusBarNotification));
            }
        }
        Collections.sort(arrayList, new Comparator() { // from class: com.voxel.simplesearchlauncher.notification.-$$Lambda$Notifier$wq73PpHRLkMqvdvfXaRUGU3i6pQ
            @Override // java.util.Comparator
            public final int compare(Object obj, Object obj2) {
                return Notifier.this.lambda$getNotificationKeys$1( (NotificationKeyData) obj, (NotificationKeyData) obj2);
            }
        });
        return arrayList;
    }

    public List<StatusBarNotification> getStatusBarNotificationsForKeys(List<NotificationKeyData> list) {
        ArrayList arrayList = new ArrayList();
        Iterator<NotificationKeyData> it = list.iterator();
        while (it.hasNext()) {
            StatusBarNotification statusBarNotification = this.mNotifications.get(it.next().notificationKey);
            if (statusBarNotification != null) {
                arrayList.add(statusBarNotification);
            }
        }
        return arrayList;
    }

    public boolean hasHandlerData(String str) {
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append("/source");
        return NotificationCount.Source.HANDLER.ordinal() == this.mPreferences.getInt(sb.toString(), -1);
    }

    public boolean isHighAccuracy(String str) {
        return this.mBadgeAppsInfo.isHighAccuracyApp(str);
    }

    @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
        NotificationCount notificationForKey = getNotificationForKey(str);
        if (notificationForKey == null) {
            return;
        }
        dispatchListeners(notificationForKey);
    }

    public void readCount(NotificationCount notificationCount) {
        notificationCount.count = getCount(notificationCount.packageName, notificationCount.activityName);
    }

    public synchronized void registerListener(String str, OnNotificationChangeListener onNotificationChangeListener) {
        WeakReference<OnNotificationChangeListener> weakReference = new WeakReference<>(onNotificationChangeListener);
        List<WeakReference<OnNotificationChangeListener>> listSynchronizedList = this.mListeners.get(str);
        if (listSynchronizedList == null) {
            listSynchronizedList = Collections.synchronizedList(new LinkedList());
            this.mListeners.put(str, listSynchronizedList);
        }
        listSynchronizedList.add(weakReference);
        Set<String> hashSet = this.mListenerMap.get(onNotificationChangeListener);
        if (hashSet == null) {
            hashSet = new HashSet<>();
            this.mListenerMap.put(onNotificationChangeListener, hashSet);
        }
        hashSet.add(str);
    }

    public void setController(INotificationController iNotificationController) {
        this.mController = iNotificationController;
    }

    public void setCount(NotificationCount notificationCount) {
        String componentName = getComponentName(notificationCount.packageName, notificationCount.activityName);
        if (componentName == null) {
            return;
        }
        if (notificationCount.source == NotificationCount.Source.EXTRACTOR && this.mBadgeAppsInfo.isTrustedHandler(notificationCount.packageName) && hasHandlerData(notificationCount.packageName)) {
            return;
        }
        if (notificationCount.source == NotificationCount.Source.HANDLER && this.mBadgeAppsInfo.isUnreliableHandler(notificationCount.packageName)) {
            return;
        }
        Log.i("Notifier", String.format("Setting count of %s to %d", componentName, Integer.valueOf(notificationCount.count)));
        SharedPreferences.Editor editorEdit = this.mPreferences.edit();
        if (notificationCount.count == 0) {
            editorEdit.remove(componentName);
            editorEdit.remove(notificationCount.packageName);
        } else {
            editorEdit.putInt(componentName, notificationCount.count);
            editorEdit.putInt(notificationCount.packageName, notificationCount.count);
        }
        editorEdit.putInt(notificationCount.packageName + "/source", notificationCount.source.ordinal());
        editorEdit.apply();
        INotificationController iNotificationController = this.mController;
        if (iNotificationController != null) {
            iNotificationController.hasNotificationUpdate(notificationCount);
        }
        dispatchListeners(notificationCount);
    }

    public void setEnabled(boolean z) {
        boolean z2 = z != this.mEnabled;
        this.mEnabled = z;
        if (z2) {
            for (NotificationCount notificationCount : getAllCounts()) {
                if (!z) {
                    notificationCount.count = 0;
                }
                dispatchListeners(notificationCount);
            }
        }
    }

    public void setTransientCount(String str, int i) {
        if (i == 0) {
            this.mTransientNotifications.remove(str);
        } else {
            this.mTransientNotifications.put(str, Integer.valueOf(i));
        }
        if (this.mEnabled) {
            NotificationCount notificationCount = new NotificationCount(str);
            readCount(notificationCount);
            dispatchListeners(notificationCount);
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public synchronized void unregisterListener(String str, OnNotificationChangeListener onNotificationChangeListener) {
        List<WeakReference<OnNotificationChangeListener>> list = this.mListeners.get(str);
        if (list != null) {
            WeakReference<OnNotificationChangeListener> weakReference = null;
            for (WeakReference<OnNotificationChangeListener> weakReference2 : list) {
                if (weakReference2.get() == onNotificationChangeListener) {
                    weakReference = weakReference2;
                    break;
                }
            }
            if (weakReference != null) {
                list.remove(weakReference);
            }
        }
        Set<String> set = this.mListenerMap.get(onNotificationChangeListener);
        if (set != null) {
            set.remove(str);
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public synchronized void unregisterListenerForAll(OnNotificationChangeListener onNotificationChangeListener) {
        Set<String> set = this.mListenerMap.get(onNotificationChangeListener);
        if (set == null) {
            return;
        }
        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            unregisterListener(it.next(), onNotificationChangeListener);
        }
    }
}
