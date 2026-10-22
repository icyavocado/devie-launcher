package com.voxel.simplesearchlauncher.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.ArrayDeque;
import java.util.Deque;

/* JADX INFO: loaded from: classes2.dex */
public class Choreographer {
    private static final String TAG = "Choreographer";
    private static Choreographer sInstance;
    private int mAnimationCounter;
    private Runnable mTimeoutRunnable;
    private Deque<Runnable> mAnimationQueue = new ArrayDeque();
    private Deque<Runnable> mNonAnimationQueue = new ArrayDeque();
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Choreographer() {
    }

    public static synchronized Choreographer getInstance() {
        if (sInstance == null) {
            sInstance = new Choreographer();
        }
        return sInstance;
    }

    private Runnable getTimeoutRunnable() {
        return new Runnable() { // from class: com.voxel.simplesearchlauncher.utils.-$$Lambda$Choreographer$nQFhTMeZcvs5uXfxuN3pbjX21Gw
            @Override // java.lang.Runnable
            public final void run() {
                Choreographer.this.lambda$getTimeoutRunnable$3();
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleQueues() {
        Runnable runnableRemoveFirst;
        logState();
        if (this.mAnimationQueue.isEmpty()) {
            runnableRemoveFirst = (this.mAnimationCounter != 0 || this.mNonAnimationQueue.isEmpty()) ? null : this.mNonAnimationQueue.removeFirst();
        } else {
            runnableRemoveFirst = this.mAnimationQueue.removeFirst();
        }
        if (runnableRemoveFirst != null) {
            runnableRemoveFirst.run();
            this.mHandler.post(new Runnable() { // from class: com.voxel.simplesearchlauncher.utils.-$$Lambda$Choreographer$EGbIqHK4dQ6yvhNDFPr8RzMTXMU
                @Override // java.lang.Runnable
                public final void run() {
                    Choreographer.this.handleQueues();
                }
            });
        }
    }

    public void lambda$animationEnded$4() {
        int i = this.mAnimationCounter;
        if (i == 0) {
            return;
        }
        this.mAnimationCounter = i - 1;
        if (this.mAnimationCounter == 0) {
            Runnable runnable = this.mTimeoutRunnable;
            if (runnable != null) {
                this.mHandler.removeCallbacks(runnable);
                this.mTimeoutRunnable = null;
            }
            this.handleQueues();
        }
    }

    public void lambda$getTimeoutRunnable$3() {
        Log.e(TAG, "Animation timeout");
        this.logState();
        this.mAnimationCounter = 0;
        this.mTimeoutRunnable = null;
        this.handleQueues();
    }

    public void lambda$postAnimationStart$2(Runnable runnable) {
        if (this.mTimeoutRunnable != null) {
            this.mHandler.removeCallbacks(runnable);
        } else {
            this.mTimeoutRunnable = this.getTimeoutRunnable();
        }
        this.mHandler.postDelayed(this.mTimeoutRunnable, 2000L);
        this.mAnimationCounter++;
        this.mAnimationQueue.addLast(runnable);
        this.handleQueues();
    }

    private void logState() {
        Log.d(TAG, String.format("mAnimationCounter: %d, mAnimationQueue: %d, mNonAnimationQueue: %d", Integer.valueOf(this.mAnimationCounter), Integer.valueOf(this.mAnimationQueue.size()), Integer.valueOf(this.mNonAnimationQueue.size())));
    }

    public void animationEnded() {
        execute(new Runnable() { // from class: com.voxel.simplesearchlauncher.utils.-$$Lambda$Choreographer$R52HNZnnmm_XX5wPhfwOxETjG_Y
            @Override // java.lang.Runnable
            public final void run() {
                Choreographer.this.lambda$animationEnded$4();
            }
        });
    }

    public void execute(Runnable runnable) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            this.mHandler.post(runnable);
        }
    }

    public synchronized void postAnimationStart(final Runnable runnable) {
        execute(new Runnable() { // from class: com.voxel.simplesearchlauncher.utils.-$$Lambda$Choreographer$wjG3US3ph7dLpMTAcowpdjkwVEk
            @Override // java.lang.Runnable
            public final void run() {
                Choreographer.this.lambda$postAnimationStart$2( runnable);
            }
        });
    }
}
