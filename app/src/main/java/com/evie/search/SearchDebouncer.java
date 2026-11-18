package com.evie.search;

import android.os.Handler;
import android.os.Looper;

/* Coalesces rapid search keystrokes: only the latest scheduled query runs. */
class SearchDebouncer {
    private final Handler mHandler;
    private final long mDelayMs;
    private Runnable mPending;

    SearchDebouncer(long delayMs) {
        this(new Handler(Looper.getMainLooper()), delayMs);
    }

    SearchDebouncer(Handler handler, long delayMs) {
        this.mHandler = handler;
        this.mDelayMs = delayMs;
    }

    void schedule(Runnable runnable) {
        cancel();
        this.mPending = runnable;
        this.mHandler.postDelayed(runnable, this.mDelayMs);
    }

    void runNow(Runnable runnable) {
        cancel();
        runnable.run();
    }

    void cancel() {
        if (this.mPending != null) {
            this.mHandler.removeCallbacks(this.mPending);
            this.mPending = null;
        }
    }
}
