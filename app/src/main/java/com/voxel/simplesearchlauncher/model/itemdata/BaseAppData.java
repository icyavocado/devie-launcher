package com.voxel.simplesearchlauncher.model.itemdata;

import android.text.TextUtils;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/* JADX INFO: loaded from: classes2.dex */
public abstract class BaseAppData extends SearchItemData {
    protected String activityName;
    private String itemId;
    protected String packageName;
    private int versionCode;

    BaseAppData() {
    }

    public BaseAppData(String str, String str2) {
        this.packageName = str;
        this.activityName = TextUtils.isEmpty(str2) ? str : str2;
    }

    private synchronized void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        this.activityName = (String) objectInputStream.readObject();
        this.packageName = (String) objectInputStream.readObject();
        this.itemId = (String) objectInputStream.readObject();
        this.versionCode = objectInputStream.readInt();
    }

    private synchronized void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.writeObject(this.activityName);
        objectOutputStream.writeObject(this.packageName);
        objectOutputStream.writeObject(this.itemId);
        objectOutputStream.writeInt(this.versionCode);
    }

    public String getActivityName() {
        return this.activityName;
    }

    @Override // com.voxel.simplesearchlauncher.model.itemdata.SearchItemData
    public String getItemId() {
        if (this.itemId == null) {
            this.itemId = "" + Math.abs(this.packageName.hashCode()) + "-" /* REWORK: was crittercism EventsFilesManager constant */ + Math.abs(this.activityName.hashCode());
        }
        return this.itemId;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public int getVersionCode() {
        return this.versionCode;
    }

    public synchronized void setVersionCode(int i) {
        this.versionCode = i;
    }
}
