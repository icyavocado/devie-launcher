package com.voxel.launcher3;

import android.content.Intent;
import android.util.Log;
import com.ovacoda.devie.R;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
public class Stats {
    ArrayList<Integer> mHistogram;
    ArrayList<String> mIntents;
    private final String mLaunchBroadcastPermission;
    private final Launcher mLauncher;
    DataOutputStream mLog;

    public Stats(Launcher launcher) {
        /* REWORK: the dex version was best-effort; the decompile's throws-tangle does
         * not survive javac. All IO here swallows after logging. */
        this.mLauncher = launcher;
        this.mLaunchBroadcastPermission = launcher.getResources().getString(R.string.receive_launch_broadcasts_permission);
        loadStats();
        try {
            this.mLog = new DataOutputStream(this.mLauncher.openFileOutput("launches.log", 32768));
            this.mLog.writeInt(1);
            this.mLog.writeInt(1);
        } catch (FileNotFoundException e) {
            Log.e("Launcher3/Stats", "unable to create stats log: " + e);
            this.mLog = null;
        } catch (IOException e2) {
            Log.e("Launcher3/Stats", "unable to write to stats log: " + e2);
            this.mLog = null;
        }
    }

    private void loadStats() {
        /* REWORK: see the ctor note; best-effort read. */
        this.mIntents = new ArrayList<>(100);
        this.mHistogram = new ArrayList<>(100);
        DataInputStream dataInputStream = null;
        try {
            dataInputStream = new DataInputStream(this.mLauncher.openFileInput("stats.log"));
            if (dataInputStream.readInt() == 1) {
                int i = dataInputStream.readInt();
                for (int i2 = 0; i2 < i; i2++) {
                    String utf = dataInputStream.readUTF();
                    int i3 = dataInputStream.readInt();
                    this.mIntents.add(utf);
                    this.mHistogram.add(Integer.valueOf(i3));
                }
            }
        } catch (FileNotFoundException unused) {
        } catch (IOException unused2) {
        } catch (RuntimeException unused3) {
        } finally {
            try {
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
            } catch (IOException unused4) {
            }
        }
    }

    /* JADX WARN: Code duplicated, block: B:39:0x009c A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    private void saveStats() {
        /* REWORK: see the ctor note; write tmp then atomically rename. */
        DataOutputStream dataOutputStream = null;
        try {
            dataOutputStream = new DataOutputStream(this.mLauncher.openFileOutput("stats.log.tmp", 0));
            dataOutputStream.writeInt(1);
            int size = this.mHistogram.size();
            dataOutputStream.writeInt(size);
            for (int i = 0; i < size; i++) {
                dataOutputStream.writeUTF(this.mIntents.get(i));
                dataOutputStream.writeInt(this.mHistogram.get(i).intValue());
            }
            this.mLauncher.getFileStreamPath("stats.log.tmp").renameTo(this.mLauncher.getFileStreamPath("stats.log"));
        } catch (FileNotFoundException e3) {
            Log.e("Launcher3/Stats", "unable to create stats data: " + e3);
        } catch (IOException e4) {
            Log.e("Launcher3/Stats", "unable to write to stats data: " + e4);
        } catch (RuntimeException e5) {
            Log.e("Launcher3/Stats", "unable to write to stats data", e5);
        } finally {
            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
                } catch (IOException unused2) {
                }
            }
        }
    }

    public void incrementLaunch(String str) {
        int iIndexOf = this.mIntents.indexOf(str);
        if (iIndexOf < 0) {
            this.mIntents.add(str);
            this.mHistogram.add(1);
        } else {
            ArrayList<Integer> arrayList = this.mHistogram;
            arrayList.set(iIndexOf, Integer.valueOf(arrayList.get(iIndexOf).intValue() + 1));
        }
    }

    public void recordLaunch(Intent intent, ShortcutInfo shortcutInfo) {
        Intent intent2 = new Intent(intent);
        intent2.setSourceBounds(null);
        String uri = intent2.toUri(0);
        Intent intentPutExtra = new Intent("com.voxel.launcher3.action.LAUNCH").putExtra("intent", uri);
        if (shortcutInfo != null) {
            intentPutExtra.putExtra("container", shortcutInfo.container).putExtra("screen", shortcutInfo.screenId).putExtra("cellX", shortcutInfo.cellX).putExtra("cellY", shortcutInfo.cellY);
        }
        this.mLauncher.sendBroadcast(intentPutExtra, this.mLaunchBroadcastPermission);
        incrementLaunch(uri);
        saveStats();
        DataOutputStream dataOutputStream = this.mLog;
        if (dataOutputStream != null) {
            try {
                dataOutputStream.writeInt(4096);
                this.mLog.writeLong(System.currentTimeMillis());
                if (shortcutInfo == null) {
                    this.mLog.writeShort(0);
                    this.mLog.writeShort(0);
                    this.mLog.writeShort(0);
                    this.mLog.writeShort(0);
                } else {
                    this.mLog.writeShort((short) shortcutInfo.container);
                    this.mLog.writeShort((short) shortcutInfo.screenId);
                    this.mLog.writeShort((short) shortcutInfo.cellX);
                    this.mLog.writeShort((short) shortcutInfo.cellY);
                }
                this.mLog.writeUTF(uri);
                this.mLog.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}
}
