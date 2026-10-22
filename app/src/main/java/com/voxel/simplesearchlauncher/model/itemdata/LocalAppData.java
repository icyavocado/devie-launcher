package com.voxel.simplesearchlauncher.model.itemdata;

import timber.log.Timber;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import com.voxel.simplesearchlauncher.activity.SafeActivityStarter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/* JADX INFO: loaded from: classes2.dex */
public class LocalAppData extends BaseAppData {
    private static final String TAG = "LocalAppData";
    private long firstInstallTime;
    private boolean hasOpened;
    private String labelOverride;
    private transient Bitmap mCustomIcon;

    LocalAppData() {
    }

    public LocalAppData(String str, String str2) {
        super(str, str2);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private boolean launchApplication(Activity activity) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setClassName(getPackageName(), getActivityName());
        intent.setFlags(270532608);
        if (intent.resolveActivity(activity.getApplicationContext().getPackageManager()) == null) {
            Log.e(TAG, "Could not launch activity for app: " + getLabel());
            return false;
        }
        try {
            if (activity instanceof SafeActivityStarter) {
                ((SafeActivityStarter) activity).startActivitySafely(intent);
                return true;
            }
            activity.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Activity not found. App probably uninstalled: " + getPackageName() + "/" + getActivityName(), e);
            return false;
        } catch (SecurityException e2) {
            Log.e(TAG, "SecurityException launching app: " + getPackageName() + "/" + getActivityName(), e2);
            Timber.e(e2);
            return false;
        }
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        this.firstInstallTime = objectInputStream.readLong();
        this.hasOpened = objectInputStream.readBoolean();
        this.labelOverride = (String) objectInputStream.readObject();
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.writeLong(this.firstInstallTime);
        objectOutputStream.writeBoolean(this.hasOpened);
        objectOutputStream.writeObject(this.labelOverride);
    }

    public String getAppActivityName() {
        return getActivityName();
    }

    public String getAppPackageName() {
        return getPackageName();
    }

    public int getAppVersionCode() {
        return getVersionCode();
    }

    public Bitmap getCustomIcon() {
        return this.mCustomIcon;
    }

    public long getFirstInstallTime() {
        return this.firstInstallTime;
    }

    public boolean getHasOpened() {
        return this.hasOpened;
    }

    @Override // com.voxel.simplesearchlauncher.model.itemdata.SearchItemData
    public Intent getIntent(Context context) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setClassName(getPackageName(), getActivityName());
        intent.setFlags(270532608);
        return intent;
    }

    @Override // com.voxel.simplesearchlauncher.model.itemdata.SearchItemData
    public String getLabel() {
        String str = this.labelOverride;
        return str != null ? str : super.getLabel();
    }

    public String getLabelOverride() {
        return this.labelOverride;
    }

    @Override // com.voxel.simplesearchlauncher.model.itemdata.SearchItemData
    public void handleAction(SearchItemData.ActionType actionType, Activity activity) {
        if (actionType == SearchItemData.ActionType.CLICK) {
            launchApplication(activity);
        }
    }

    public void setCustomIcon(Bitmap bitmap) {
        this.mCustomIcon = bitmap;
    }

    public void setFirstInstallTime(long j) {
        this.firstInstallTime = j;
    }

    public void setHasOpened(boolean z) {
        this.hasOpened = z;
    }

    public void setLabelOverride(String str) {
        this.labelOverride = str;
    }
}
