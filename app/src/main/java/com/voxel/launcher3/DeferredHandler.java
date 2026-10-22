package com.voxel.launcher3;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.util.Pair;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/* JADX INFO: loaded from: classes.dex */
public class DeferredHandler {
    private LinkedList<Pair<Runnable, Integer>> mQueue = new LinkedList<>();
    private MessageQueue mMessageQueue = Looper.myQueue();
    private Impl mHandler = new Impl();

    private class IdleRunnable implements Runnable {
        Runnable mRunnable;

        IdleRunnable(Runnable runnable) {
            this.mRunnable = runnable;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.mRunnable.run();
        }
    }

    private class Impl extends Handler implements MessageQueue.IdleHandler {
        private Impl() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            synchronized (DeferredHandler.this.mQueue) {
                if (DeferredHandler.this.mQueue.size() == 0) {
                    return;
                }
                ((Runnable) ((Pair) DeferredHandler.this.mQueue.removeFirst()).first).run();
                synchronized (DeferredHandler.this.mQueue) {
                    DeferredHandler.this.scheduleNextLocked();
                }
            }
        }

        @Override // android.os.MessageQueue.IdleHandler
        public boolean queueIdle() {
            handleMessage(null);
            return false;
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public void cancelAllRunnablesOfType(int i) {
        synchronized (this.mQueue) {
            ListIterator<Pair<Runnable, Integer>> listIterator = this.mQueue.listIterator();
            while (listIterator.hasNext()) {
                if (((Integer) listIterator.next().second).intValue() == i) {
                    listIterator.remove();
                }
            }
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public void flush() {
        LinkedList linkedList = new LinkedList();
        synchronized (this.mQueue) {
            linkedList.addAll(this.mQueue);
            this.mQueue.clear();
        }
        Iterator it = linkedList.iterator();
        while (it.hasNext()) {
            ((Runnable) ((Pair) it.next()).first).run();
        }
    }

    public void post(Runnable runnable) {
        post(runnable, 0);
    }

    public void post(Runnable runnable, int i) {
        synchronized (this.mQueue) {
            this.mQueue.add(new Pair<>(runnable, Integer.valueOf(i)));
            if (this.mQueue.size() == 1) {
                scheduleNextLocked();
            }
        }
    }

    public void postIdle(Runnable runnable) {
        postIdle(runnable, 0);
    }

    public void postIdle(Runnable runnable, int i) {
        post(new IdleRunnable(runnable), i);
    }

    void scheduleNextLocked() {
        if (this.mQueue.size() > 0) {
            if (((Runnable) this.mQueue.getFirst().first) instanceof IdleRunnable) {
                this.mMessageQueue.addIdleHandler(this.mHandler);
            } else {
                this.mHandler.sendEmptyMessage(1);
            }
        }
    }
}
