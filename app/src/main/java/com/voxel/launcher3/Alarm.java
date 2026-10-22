package com.voxel.launcher3;

import android.os.Handler;

/* JADX INFO: loaded from: classes.dex */
public class Alarm implements Runnable {
    private OnAlarmListener mAlarmListener;
    private long mAlarmTriggerTime;
    private boolean mWaitingForCallback;
    private boolean mAlarmPending = false;
    private Handler mHandler = new Handler();

    public boolean alarmPending() {
        return this.mAlarmPending;
    }

    public void cancelAlarm() {
        this.mAlarmTriggerTime = 0L;
        this.mAlarmPending = false;
    }

    @Override // java.lang.Runnable
    public void run() {
        this.mWaitingForCallback = false;
        if (this.mAlarmTriggerTime != 0) {
            long jCurrentTimeMillis = System.currentTimeMillis();
            long j = this.mAlarmTriggerTime;
            if (j > jCurrentTimeMillis) {
                this.mHandler.postDelayed(this, Math.max(0L, j - jCurrentTimeMillis));
                this.mWaitingForCallback = true;
                return;
            }
            this.mAlarmPending = false;
            OnAlarmListener onAlarmListener = this.mAlarmListener;
            if (onAlarmListener != null) {
                onAlarmListener.onAlarm(this);
            }
        }
    }

    public void setAlarm(long j) {
        long jCurrentTimeMillis = System.currentTimeMillis();
        this.mAlarmPending = true;
        this.mAlarmTriggerTime = j + jCurrentTimeMillis;
        if (this.mWaitingForCallback) {
            return;
        }
        this.mHandler.postDelayed(this, this.mAlarmTriggerTime - jCurrentTimeMillis);
        this.mWaitingForCallback = true;
    }

    public void setOnAlarmListener(OnAlarmListener onAlarmListener) {
        this.mAlarmListener = onAlarmListener;
    }
}
