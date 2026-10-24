package com.voxel.simplesearchlauncher.analytics;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;

/* REWORK: cloud analytics removed (events.evie.com upload, Fabric/Crashlytics, GMS AdvertisingIdClient,
 * device fingerprint payload, location/geohash, local log upload DB). Events still flow through the
 * AnalyticsFilter chain locally: FrequentlyUsedAnalyticsFilter uses app-launch events to keep the
 * frequently-used list populated. Upload side is a no-op, and no device
 * identifiers are generated or persisted (the old anonym_user_id UUID prefs
 * writer is gone — event JSON carries no user_id). */
public class AnalyticsHandler {
    private static final String TAG = "AnalyticsHandler";
    private static AnalyticsHandler instance;
    private Context context;
    private final AnalyticsFilter mAnalyticsFilter;
    private final boolean mAllowedUser;

    public static class EventProp {
        private Object action;
        private Object label;
        JSONObject props = new JSONObject();

        private EventProp addProp(String str, Object obj) {
            try {
                this.props.put(str, obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (this.action == null) {
                this.action = obj;
            } else if (this.label == null) {
                this.label = obj;
            }
            return this;
        }

        public EventProp add(String str, double d) {
            return addProp(str, Double.valueOf(d));
        }

        public EventProp add(String str, int i) {
            return addProp(str, Integer.valueOf(i));
        }

        public EventProp add(String str, String str2) {
            return addProp(str, str2);
        }

        public EventProp add(String str, boolean z) {
            return addProp(str, Boolean.valueOf(z));
        }

        public JSONObject getProps() {
            return this.props;
        }

        public String toString() {
            return this.props.toString();
        }
    }

    private AnalyticsHandler(Context context, AnalyticsFilter analyticsFilter) {
        this.context = context;
        this.mAnalyticsFilter = analyticsFilter;
        this.mAllowedUser = !ActivityManager.isUserAMonkey() && !ActivityManager.isRunningInTestHarness();
    }

    public static EventProp generateAppLaunchProperties(Context context, Intent intent, String str) {
        /* REWORK: the (Context, ComponentName, String) overload jadx emitted was a
         * register-misbind of the (ComponentName, String) one; route through it. */
        return generateAppLaunchProperties(getComponent(context, intent), str);
    }

    @SuppressLint({"NewApi"})
    private static EventProp generateAppLaunchProperties(ComponentName componentName, String str) {
        return generateAppLaunchProperties(componentName != null ? componentName.getPackageName() : null, componentName != null ? componentName.getClassName() : null, str);
    }

    private static EventProp generateAppLaunchProperties(String str, String str2, String str3) {
        EventProp eventProp = new EventProp();
        if (!TextUtils.isEmpty(str3)) {
            eventProp.add("launch_source", str3);
        }
        if (!TextUtils.isEmpty(str)) {
            eventProp.add("launch_package_name", str);
        }
        if (!TextUtils.isEmpty(str2)) {
            eventProp.add("launch_class_name", str2);
        }
        return eventProp;
    }

    private JSONObject generateEventLog(String str, EventProp eventProp) {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("event", str);
            jSONObject.put("time", System.currentTimeMillis());
            if (eventProp != null) {
                jSONObject.put("properties", eventProp.getProps());
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
        return this.mAnalyticsFilter.filter(jSONObject);
    }

    private static ComponentName getComponent(Context context, Intent intent) {
        try {
            PackageManager packageManager = context.getPackageManager();
            if (intent == null) {
                return null;
            }
            if (intent.getComponent() != null) {
                return intent.getComponent();
            }
            ResolveInfo resolveInfoResolveActivity = packageManager.resolveActivity(intent, 0);
            if (resolveInfoResolveActivity != null && resolveInfoResolveActivity.activityInfo != null && resolveInfoResolveActivity.activityInfo.packageName != null) {
                return new ComponentName(resolveInfoResolveActivity.activityInfo.packageName, resolveInfoResolveActivity.activityInfo.name);
            }
            return null;
        } catch (Exception unused) {
            return null;
        }
    }

    @Deprecated
    public static synchronized AnalyticsHandler getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Instance must be initialized before trying to use.");
        }
        return instance;
    }

    public static synchronized void init(Context context, AnalyticsFilter analyticsFilter) {
        if (instance != null) {
            return;
        }
        if (context == null) {
            throw new IllegalArgumentException("Context must be provided.");
        }
        instance = new AnalyticsHandler(context, analyticsFilter);
    }

    public void logEvent(String str) {
        logEvent(str, null);
    }

    public void logEvent(String str, EventProp eventProp) {
        if (this.mAllowedUser && !TextUtils.isEmpty(str)) {
            Timber.d(TAG + " " + str + ": %s", generateEventLog(str, eventProp == null ? new EventProp() : eventProp).toString());
        }
    }

    public void recordAppLaunch(Context context, Intent intent, String str) {
        logEvent("ev_app_launch", generateAppLaunchProperties(context, intent, str));
    }

    public void recordAppLaunch(String str, String str2, String str3) {
        logEvent("ev_app_launch", generateAppLaunchProperties(str, str2, str3));
    }

    /* REWORK: sync was the upload path; no-op now that upload is removed. */
    public void requestLogsSync() {
    }

    public void requestLogsSync(boolean z) {
    }

    public void stopLogsSync() {
    }
}
