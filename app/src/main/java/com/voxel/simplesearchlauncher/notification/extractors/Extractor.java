package com.voxel.simplesearchlauncher.notification.extractors;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.core.content.ContextCompat;
import com.voxel.simplesearchlauncher.notification.NotificationCount;
import com.voxel.simplesearchlauncher.notification.Notifier;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
public abstract class Extractor {
    private static List<Extractor> sRegisteredExtractors;
    protected Context mContext;
    protected Handler mHandler = new Handler(Looper.getMainLooper());
    protected OnExtractorUpdateListener mListener;

    public interface OnExtractorUpdateListener {
        void onExtractorUpdated(Extractor extractor);
    }

    public Extractor(Context context) {
        this.mContext = context;
    }

    public static synchronized List<Extractor> getRegisteredExtractors(Context context) {
        if (sRegisteredExtractors != null) {
            return sRegisteredExtractors;
        }
        sRegisteredExtractors = new ArrayList();
        sRegisteredExtractors.add(new GmailExtractor(context));
        sRegisteredExtractors.add(new OutlookWidgetExtractor(context));
        sRegisteredExtractors.add(new WhatsappExtractor(context));
        sRegisteredExtractors.add(new YahooMailExtractor(context));
        sRegisteredExtractors.add(new EasilyDoWidgetExtractor(context));
        return sRegisteredExtractors;
    }

    public static List<Extractor> getValidExtractors(Context context) {
        ArrayList arrayList = new ArrayList();
        PackageManager packageManager = context.getPackageManager();
        for (Extractor extractor : getRegisteredExtractors(context)) {
            try {
                packageManager.getPackageInfo(extractor.getPackageId(), 0);
                arrayList.add(extractor);
            } catch (PackageManager.NameNotFoundException unused) {
            }
        }
        return arrayList;
    }

    public abstract NotificationCount extractNotificationInfo();

    public String getActivityName() {
        return null;
    }

    public String getConfig() {
        return null;
    }

    public String getConfigKey() {
        return null;
    }

    public Context getContext() {
        return this.mContext;
    }

    public abstract String getPackageId();

    public String[] getPermissions() {
        return null;
    }

    public boolean hasPermission() {
        String[] permissions = getPermissions();
        if (permissions == null) {
            return true;
        }
        for (String str : permissions) {
            if (ContextCompat.checkSelfPermission(this.mContext, str) != 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isConfigured() {
        return true;
    }

    public void notifyListener() {
        OnExtractorUpdateListener onExtractorUpdateListener = this.mListener;
        if (onExtractorUpdateListener != null) {
            onExtractorUpdateListener.onExtractorUpdated(this);
        }
    }

    public void refresh() {
    }

    public void registerObserver() {
    }

    public void setConfig(String str) {
    }

    public void setListener(OnExtractorUpdateListener onExtractorUpdateListener) {
        this.mListener = onExtractorUpdateListener;
    }

    public void unregisterObserver() {
    }

    public void update() {
        if (hasPermission()) {
            try {
                NotificationCount notificationCountExtractNotificationInfo = extractNotificationInfo();
                if (notificationCountExtractNotificationInfo == null) {
                    return;
                }
                notificationCountExtractNotificationInfo.source = NotificationCount.Source.EXTRACTOR;
                Notifier.getInstance(this.mContext).setCount(notificationCountExtractNotificationInfo);
            } catch (Exception e) {
                Log.w("Extractor", "Could not extract data", e);
            }
        }
    }
}
