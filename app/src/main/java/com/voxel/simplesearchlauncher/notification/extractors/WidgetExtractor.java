package com.voxel.simplesearchlauncher.notification.extractors;

import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.voxel.simplesearchlauncher.notification.InterceptingWidgetHost;
import com.voxel.simplesearchlauncher.notification.NotificationCount;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes2.dex */
public abstract class WidgetExtractor extends Extractor {
    public static final String TAG = "WidgetExtractor";
    protected String mActivityName;
    protected int mFieldId;
    protected AppWidgetHostView mHostView;
    protected int mWidgetId;

    public WidgetExtractor(Context context) {
        super(context);
    }

    public void createWidget(int i) {
        this.mWidgetId = i;
        restoreWidget();
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public NotificationCount extractNotificationInfo() {
        View viewFindViewById;
        AppWidgetHostView appWidgetHostView = this.mHostView;
        if (appWidgetHostView == null || (viewFindViewById = appWidgetHostView.findViewById(getFieldId())) == null) {
            return null;
        }
        NotificationCount notificationCount = new NotificationCount(getPackageId(), getActivityName());
        if ((viewFindViewById instanceof TextView) && viewFindViewById.getVisibility() == 0) {
            TextView textView = (TextView) viewFindViewById;
            try {
                notificationCount.count = parseCount(textView.getText());
            } catch (Exception unused) {
                Log.w(TAG, "could not parse number: " + ((Object) textView.getText()));
            }
        }
        return notificationCount;
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public String getActivityName() {
        Intent launchIntentForPackage;
        ComponentName component;
        if (this.mActivityName == null && (launchIntentForPackage = this.mContext.getPackageManager().getLaunchIntentForPackage(getPackageId())) != null && (component = launchIntentForPackage.getComponent()) != null) {
            this.mActivityName = component.getClassName();
        }
        return this.mActivityName;
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public String getConfig() {
        if (this.mWidgetId <= 0) {
            return null;
        }
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("id", this.mWidgetId);
            return jSONObject.toString();
        } catch (JSONException unused) {
            return null;
        }
    }

    public int getFieldId() {
        if (this.mFieldId == 0) {
            this.mFieldId = getPackageResourceId(getFieldName());
        }
        return this.mFieldId;
    }

    protected abstract String getFieldName();

    protected int getPackageResourceId(String str) {
        try {
            String packageName = getWidgetComponent().getPackageName();
            return this.mContext.getPackageManager().getResourcesForApplication(packageName).getIdentifier(str, "id", packageName);
        } catch (PackageManager.NameNotFoundException unused) {
            return -1;
        }
    }

    public abstract ComponentName getWidgetComponent();

    public int getWidgetId() {
        return this.mWidgetId;
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public boolean isConfigured() {
        return this.mWidgetId > 0;
    }

    protected void onWidgetRestored() {
    }

    protected int parseCount(CharSequence charSequence) {
        return Integer.parseInt(String.valueOf(charSequence));
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public void registerObserver() {
        InterceptingWidgetHost.getInstance(this.mContext).startListeningIfNeeded();
    }

    public boolean requiresConfig() {
        return false;
    }

    public void restoreWidget() {
        this.mHostView = InterceptingWidgetHost.getInstance(this.mContext).createViewForExtractor(this, AppWidgetManager.getInstance(this.mContext).getAppWidgetInfo(this.mWidgetId));
        onWidgetRestored();
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public void setConfig(String str) {
        if (str == null) {
            if (this.mWidgetId > 0) {
                this.mHostView = null;
                InterceptingWidgetHost.getInstance(this.mContext).deleteAppWidgetId(this.mWidgetId);
            }
            this.mWidgetId = 0;
            return;
        }
        try {
            this.mWidgetId = new JSONObject(str).getInt("id");
            restoreWidget();
            Log.i(TAG, "restored widget " + this.mWidgetId + " for " + this);
        } catch (Exception unused) {
        }
    }
}
