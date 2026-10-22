package com.voxel.simplesearchlauncher.notification;

import timber.log.Timber;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import com.voxel.simplesearchlauncher.notification.extractors.Extractor;
import com.voxel.simplesearchlauncher.notification.extractors.WidgetExtractor;
import com.voxel.simplesearchlauncher.utils.UpgradeUtil;
import com.ovacoda.devie.R;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/* JADX INFO: loaded from: classes2.dex */
public class NotificationController implements INotificationController, Extractor.OnExtractorUpdateListener, UpgradeUtil.UpgradableComponent {
    public static final String TAG = "NotificationController";
    private static NotificationController sInstance;
    private Context mContext;
    private SharedPreferences mPrefs;
    private Set<Extractor> mUpdatedExtractors = Collections.synchronizedSet(new HashSet());
    private Map<String, Extractor> mExtractorMap = Collections.synchronizedMap(new HashMap());
    private State mState = State.STOPPED;
    private boolean mEnabled = false;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mAppDefaultEnabled = false;
    private Map<String, Boolean> mAppsEnabled = Collections.synchronizedMap(new HashMap());

    public enum State {
        STOPPED,
        STARTED,
        RUNNING
    }

    public enum Style {
        COUNT(R.string.app_settings_badge_style_count, R.drawable.ic_badge_style_count),
        DOTS(R.string.app_settings_badge_style_dots, R.drawable.ic_badge_style_dots);

        int mIcon;
        int mLabel;

        Style(int i, int i2) {
            this.mLabel = i;
            this.mIcon = i2;
        }

        public int getIcon() {
            return this.mIcon;
        }

        public int getLabel() {
            return this.mLabel;
        }
    }

    public NotificationController(Context context) {
        this.mContext = context;
        this.mPrefs = context.getSharedPreferences("notification_controller", 0);
        refreshApps();
        initFromPrefs();
        Notifier notifier = Notifier.getInstance(this.mContext);
        notifier.setController(this);
        notifier.setEnabled(isEnabled());
        UpgradeUtil.processUpgrade(this.mPrefs, this, 3);
    }

    public static NotificationController getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new NotificationController(context.getApplicationContext());
        }
        return sInstance;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public synchronized void updateExtractors() {
        Iterator<Extractor> it = this.mUpdatedExtractors.iterator();
        while (it.hasNext()) {
            it.next().update();
        }
        this.mUpdatedExtractors.clear();
    }

    /* JADX WARN: Code duplicated, block: B:41:0x0054 A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:47:0x0011 A[SYNTHETIC] */
    public void activateWidgetExtractors() {
        if (isEnabled()) {
            for (Extractor extractor : Extractor.getValidExtractors(this.mContext)) {
                if (extractor instanceof WidgetExtractor) {
                    WidgetExtractor widgetExtractor = (WidgetExtractor) extractor;
                    if (!widgetExtractor.isConfigured() && !widgetExtractor.requiresConfig()) {
                        InterceptingWidgetHost interceptingWidgetHost = InterceptingWidgetHost.getInstance(this.mContext);
                        int iAllocateAppWidgetId = interceptingWidgetHost.allocateAppWidgetId();
                        boolean z = false;
                        try {
                            try {
                                ComponentName widgetComponent = widgetExtractor.getWidgetComponent();
                                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.mContext);
                                if (appWidgetManager.bindAppWidgetIdIfAllowed(iAllocateAppWidgetId, widgetComponent)) {
                                    if (appWidgetManager.getAppWidgetInfo(iAllocateAppWidgetId).configure == null) {
                                        widgetExtractor.createWidget(iAllocateAppWidgetId);
                                        setAppEnabled(widgetExtractor.getPackageId(), true);
                                        z = true;
                                        if (!z) {
                                        }
                                    }
                                    interceptingWidgetHost.deleteAppWidgetId(iAllocateAppWidgetId);
                                } else if (!z) {
                                    interceptingWidgetHost.deleteAppWidgetId(iAllocateAppWidgetId);
                                }
                            } catch (Exception e) {
                                Timber.e(e);
                            }
                        } catch (Throwable th) {
                            interceptingWidgetHost.deleteAppWidgetId(iAllocateAppWidgetId);
                            throw th;
                        }
                    }
                }
            }
        }
    }

    public void clearAppEnabled(String str) {
        this.mAppsEnabled.remove(str);
        Notifier.getInstance(this.mContext).dispatchForPackage(str);
    }

    public void dismissNotification(String str) {
        Intent intent = new Intent("com.evie.dismiss_notification");
        intent.putExtra("com.evie.notification.key", str);
        LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(intent);
    }

    public float getBadgeScale() {
        return this.mPrefs.getFloat("scale_factor", 0.85f);
    }

    public Style getBadgeStyle() {
        return Style.values()[this.mPrefs.getInt("badge_style", Style.DOTS.ordinal())];
    }

    public Extractor getExtractor(String str) {
        return this.mExtractorMap.get(str);
    }

    public Extractor getExtractorByKey(String str) {
        for (Extractor extractor : this.mExtractorMap.values()) {
            if (str.equals(extractor.getConfigKey())) {
                return extractor;
            }
        }
        return null;
    }

    public State getState() {
        return this.mState;
    }

    @Override // com.voxel.simplesearchlauncher.notification.INotificationController
    public void hasNotificationUpdate(NotificationCount notificationCount) {
        if (notificationCount.source == NotificationCount.Source.HANDLER) {
            Notifier notifier = Notifier.getInstance(this.mContext);
            if (notifier.mBadgeAppsInfo.isHandlerPackage(notificationCount.packageName)) {
                return;
            }
            notifier.mBadgeAppsInfo.addHandlerPackage(notificationCount.packageName);
            AnalyticsHandler.getInstance().logEvent("ev_badge_new_handler", new AnalyticsHandler.EventProp().add("package_id", notificationCount.packageName));
            savePrefs();
        }
    }

    protected void initFromPrefs() {
        String configKey;
        String string;
        this.mEnabled = this.mPrefs.getBoolean("enabled", false);
        this.mAppDefaultEnabled = this.mPrefs.getBoolean("app_default_enabled", false);
        this.mAppsEnabled.clear();
        Set<String> stringSet = this.mPrefs.getStringSet("disabled_apps", new HashSet());
        Set<String> stringSet2 = this.mPrefs.getStringSet("enabled_apps", new HashSet());
        Iterator<String> it = stringSet.iterator();
        while (it.hasNext()) {
            setAppEnabled(it.next(), false);
        }
        Iterator<String> it2 = stringSet2.iterator();
        while (it2.hasNext()) {
            setAppEnabled(it2.next(), true);
        }
        if (this.mEnabled) {
            for (Extractor extractor : Extractor.getValidExtractors(this.mContext)) {
                if (isAppEnabled(extractor.getPackageId()) && (configKey = extractor.getConfigKey()) != null && (string = this.mPrefs.getString(configKey, null)) != null) {
                    extractor.setConfig(string);
                }
            }
        }
    }

    public boolean isAppDefaultEnabled() {
        return this.mAppDefaultEnabled;
    }

    public boolean isAppEnabled(String str) {
        Boolean bool = this.mAppsEnabled.get(str);
        Extractor extractor = this.mExtractorMap.get(str);
        if (extractor != null && !extractor.hasPermission()) {
            return false;
        }
        if (bool != null) {
            return bool.booleanValue();
        }
        if (Notifier.getInstance(this.mContext).isHighAccuracy(str)) {
            return true;
        }
        return this.mAppDefaultEnabled;
    }

    @Override // com.voxel.simplesearchlauncher.notification.INotificationController
    public boolean isAppWorking(String str) {
        if (!isEnabled() || !isAppEnabled(str)) {
            return false;
        }
        Extractor extractor = this.mExtractorMap.get(str);
        if (isExtractorRequired(extractor)) {
            return extractor.hasPermission() && extractor.isConfigured();
        }
        return true;
    }

    public boolean isEnabled() {
        return this.mEnabled;
    }

    public boolean isExtractorRequired(Extractor extractor) {
        if (extractor == null) {
            return false;
        }
        return !Notifier.getInstance(this.mContext).getBadgeAppsInfo().isTrustedHandler(extractor.getPackageId());
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor.OnExtractorUpdateListener
    public synchronized void onExtractorUpdated(Extractor extractor) {
        this.mUpdatedExtractors.add(extractor);
        if (this.mState == State.RUNNING) {
            this.mHandler.postDelayed(new Runnable() { // from class: com.voxel.simplesearchlauncher.notification.-$$Lambda$NotificationController$fi_09sT6kC5x8N-XD0CJ_vkPG-I
                @Override // java.lang.Runnable
                public final void run() {
                    NotificationController.this.updateExtractors();
                }
            }, 500L);
        }
    }

    @Override // com.voxel.simplesearchlauncher.utils.UpgradeUtil.UpgradableComponent
    public void onUpgrade(int i, int i2) {
        if (i < 2) {
            activateWidgetExtractors();
        } else if (i < 3) {
            setBadgeStyle(Style.COUNT);
        }
    }

    public synchronized void pause() {
        if (this.mState != State.RUNNING) {
            return;
        }
        Log.i(TAG, "paused");
        this.mState = State.STARTED;
    }

    protected void refreshApps() {
        for (Extractor extractor : this.mExtractorMap.values()) {
            extractor.unregisterObserver();
            extractor.setListener(null);
        }
        this.mExtractorMap.clear();
        for (Extractor extractor2 : Extractor.getValidExtractors(this.mContext)) {
            extractor2.refresh();
            this.mExtractorMap.put(extractor2.getPackageId(), extractor2);
        }
        Notifier.getInstance(this.mContext).mBadgeAppsInfo.resetExtractors();
        Notifier.getInstance(this.mContext).mBadgeAppsInfo.addExtractors(this.mExtractorMap.values());
    }

    public synchronized void resume() {
        if (this.mState != State.STARTED) {
            return;
        }
        Log.i(TAG, "resumed");
        this.mState = State.RUNNING;
        updateExtractors();
        LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(new Intent("com.evie.refresh_notifications"));
    }

    public void save() {
        savePrefs();
    }

    protected void savePrefs() {
        SharedPreferences.Editor editorEdit = this.mPrefs.edit();
        editorEdit.putBoolean("enabled", isEnabled());
        editorEdit.putBoolean("app_default_enabled", isAppDefaultEnabled());
        editorEdit.putStringSet("known_handlers", Notifier.getInstance(this.mContext).mBadgeAppsInfo.getHandlerPackages());
        HashSet hashSet = new HashSet();
        HashSet hashSet2 = new HashSet();
        for (String str : this.mAppsEnabled.keySet()) {
            Boolean bool = this.mAppsEnabled.get(str);
            if (bool != null) {
                if (bool.booleanValue()) {
                    hashSet2.add(str);
                } else {
                    hashSet.add(str);
                }
            }
        }
        editorEdit.putStringSet("enabled_apps", hashSet2);
        editorEdit.putStringSet("disabled_apps", hashSet);
        for (Extractor extractor : Extractor.getValidExtractors(this.mContext)) {
            if (isAppEnabled(extractor.getPackageId())) {
                String configKey = extractor.getConfigKey();
                String config = extractor.getConfig();
                if (configKey != null && config != null) {
                    editorEdit.putString(configKey, config);
                }
            }
        }
        editorEdit.apply();
    }

    public void setAppDefaultEnabled(boolean z) {
        this.mAppDefaultEnabled = z;
    }

    public void setAppEnabled(String str, boolean z) {
        if (str == null) {
            return;
        }
        if (this.mState != State.STOPPED) {
            Log.w(TAG, "Could not change settings as state isn't stopped. State: " + this.mState);
            return;
        }
        if (z) {
            Extractor extractor = this.mExtractorMap.get(str);
            if (isExtractorRequired(extractor) && (!extractor.hasPermission() || !extractor.isConfigured())) {
                return;
            } else {
                this.mAppsEnabled.put(str, true);
            }
        } else {
            this.mAppsEnabled.put(str, false);
            Extractor extractor2 = this.mExtractorMap.get(str);
            if (extractor2 != null) {
                extractor2.setConfig(null);
            }
        }
        Notifier.getInstance(this.mContext).dispatchForPackage(str);
    }

    public void setBadgeScale(float f) {
        SharedPreferences.Editor editorEdit = this.mPrefs.edit();
        editorEdit.putFloat("scale_factor", f);
        editorEdit.commit();
    }

    public void setBadgeStyle(Style style) {
        SharedPreferences.Editor editorEdit = this.mPrefs.edit();
        editorEdit.putInt("badge_style", style.ordinal());
        editorEdit.commit();
    }

    public void setEnabled(boolean z) {
        if (this.mState == State.STOPPED && this.mEnabled != z) {
            this.mEnabled = z;
            if (this.mEnabled) {
                AnalyticsHandler.getInstance().logEvent("ev_badge_enabled", new AnalyticsHandler.EventProp());
                activateWidgetExtractors();
            }
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public synchronized void start() {
        if (this.mState != State.STOPPED) {
            return;
        }
        this.mState = State.STARTED;
        savePrefs();
        Notifier.getInstance(this.mContext).setEnabled(isEnabled());
        if (!isEnabled()) {
            InterceptingWidgetHost.deleteInstanceHost();
        }
        for (Extractor extractor : this.mExtractorMap.values()) {
            if (isAppEnabled(extractor.getPackageId())) {
                extractor.setListener(this);
                extractor.registerObserver();
                extractor.update();
            }
        }
    }

    public synchronized void stop() {
        if (this.mState == State.RUNNING) {
            pause();
        }
        if (this.mState != State.STARTED) {
            return;
        }
        Log.i(TAG, "stopped");
        this.mState = State.STOPPED;
        refreshApps();
    }
}
