package com.voxel.recyclerview.widget;

import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
class ChildHelper {
    final Callback mCallback;
    final Bucket mBucket = new Bucket();
    final List<View> mHiddenViews = new ArrayList();

    static class Bucket {
        long mData = 0;
        Bucket next;

        Bucket() {
        }

        private void ensureNext() {
            if (this.next == null) {
                this.next = new Bucket();
            }
        }

        void clear(int i) {
            if (i < 64) {
                this.mData &= (1 << i) ^ (-1);
                return;
            }
            Bucket bucket = this.next;
            if (bucket != null) {
                bucket.clear(i - 64);
            }
        }

        int countOnesBefore(int i) {
            Bucket bucket = this.next;
            if (bucket == null) {
                return i >= 64 ? Long.bitCount(this.mData) : Long.bitCount(this.mData & ((1 << i) - 1));
            }
            return i < 64 ? Long.bitCount(this.mData & ((1 << i) - 1)) : bucket.countOnesBefore(i - 64) + Long.bitCount(this.mData);
        }

        boolean get(int i) {
            if (i < 64) {
                return (this.mData & (1 << i)) != 0;
            }
            ensureNext();
            return this.next.get(i - 64);
        }

        void insert(int i, boolean z) {
            if (i >= 64) {
                ensureNext();
                this.next.insert(i - 64, z);
                return;
            }
            boolean z2 = (this.mData & Long.MIN_VALUE) != 0;
            long j = (1 << i) - 1;
            long j2 = this.mData;
            this.mData = ((j2 & (j ^ (-1))) << 1) | (j2 & j);
            if (z) {
                set(i);
            } else {
                clear(i);
            }
            if (z2 || this.next != null) {
                ensureNext();
                this.next.insert(0, z2);
            }
        }

        boolean remove(int i) {
            if (i >= 64) {
                ensureNext();
                return this.next.remove(i - 64);
            }
            long j = 1 << i;
            boolean z = (this.mData & j) != 0;
            this.mData &= j ^ (-1);
            long j2 = j - 1;
            long j3 = this.mData;
            this.mData = Long.rotateRight(j3 & (j2 ^ (-1)), 1) | (j3 & j2);
            Bucket bucket = this.next;
            if (bucket != null) {
                if (bucket.get(0)) {
                    set(63);
                }
                this.next.remove(0);
            }
            return z;
        }

        void reset() {
            this.mData = 0L;
            Bucket bucket = this.next;
            if (bucket != null) {
                bucket.reset();
            }
        }

        void set(int i) {
            if (i < 64) {
                this.mData |= 1 << i;
            } else {
                ensureNext();
                this.next.set(i - 64);
            }
        }

        public String toString() {
            if (this.next == null) {
                return Long.toBinaryString(this.mData);
            }
            return this.next.toString() + "xx" + Long.toBinaryString(this.mData);
        }
    }

    interface Callback {
        void addView(View view, int i);

        void attachViewToParent(View view, int i, ViewGroup.LayoutParams layoutParams);

        void detachViewFromParent(int i);

        View getChildAt(int i);

        int getChildCount();

        RecyclerView.ViewHolder getChildViewHolder(View view);

        int indexOfChild(View view);

        void removeAllViews();

        void removeViewAt(int i);
    }

    ChildHelper(Callback callback) {
        this.mCallback = callback;
    }

    private int getOffset(int i) {
        if (i < 0) {
            return -1;
        }
        int childCount = this.mCallback.getChildCount();
        int i2 = i;
        while (i2 < childCount) {
            int iCountOnesBefore = i - (i2 - this.mBucket.countOnesBefore(i2));
            if (iCountOnesBefore == 0) {
                while (this.mBucket.get(i2)) {
                    i2++;
                }
                return i2;
            }
            i2 += iCountOnesBefore;
        }
        return -1;
    }

    void addView(View view, int i, boolean z) {
        int childCount = i < 0 ? this.mCallback.getChildCount() : getOffset(i);
        this.mBucket.insert(childCount, z);
        if (z) {
            this.mHiddenViews.add(view);
        }
        this.mCallback.addView(view, childCount);
    }

    void addView(View view, boolean z) {
        addView(view, -1, z);
    }

    void attachViewToParent(View view, int i, ViewGroup.LayoutParams layoutParams, boolean z) {
        int childCount = i < 0 ? this.mCallback.getChildCount() : getOffset(i);
        this.mBucket.insert(childCount, z);
        if (z) {
            this.mHiddenViews.add(view);
        }
        this.mCallback.attachViewToParent(view, childCount, layoutParams);
    }

    void detachViewFromParent(int i) {
        int offset = getOffset(i);
        this.mBucket.remove(offset);
        this.mCallback.detachViewFromParent(offset);
    }

    View findHiddenNonRemovedView(int i, int i2) {
        int size = this.mHiddenViews.size();
        for (int i3 = 0; i3 < size; i3++) {
            View view = this.mHiddenViews.get(i3);
            RecyclerView.ViewHolder childViewHolder = this.mCallback.getChildViewHolder(view);
            if (childViewHolder.getLayoutPosition() == i && !childViewHolder.isInvalid() && (i2 == -1 || childViewHolder.getItemViewType() == i2)) {
                return view;
            }
        }
        return null;
    }

    View getChildAt(int i) {
        return this.mCallback.getChildAt(getOffset(i));
    }

    int getChildCount() {
        return this.mCallback.getChildCount() - this.mHiddenViews.size();
    }

    View getUnfilteredChildAt(int i) {
        return this.mCallback.getChildAt(i);
    }

    int getUnfilteredChildCount() {
        return this.mCallback.getChildCount();
    }

    void hide(View view) {
        int iIndexOfChild = this.mCallback.indexOfChild(view);
        if (iIndexOfChild >= 0) {
            this.mBucket.set(iIndexOfChild);
            this.mHiddenViews.add(view);
        } else {
            throw new IllegalArgumentException("view is not a child, cannot hide " + view);
        }
    }

    int indexOfChild(View view) {
        int iIndexOfChild = this.mCallback.indexOfChild(view);
        if (iIndexOfChild == -1 || this.mBucket.get(iIndexOfChild)) {
            return -1;
        }
        return iIndexOfChild - this.mBucket.countOnesBefore(iIndexOfChild);
    }

    boolean isHidden(View view) {
        return this.mHiddenViews.contains(view);
    }

    void removeAllViewsUnfiltered() {
        this.mBucket.reset();
        this.mHiddenViews.clear();
        this.mCallback.removeAllViews();
    }

    void removeView(View view) {
        int iIndexOfChild = this.mCallback.indexOfChild(view);
        if (iIndexOfChild < 0) {
            return;
        }
        if (this.mBucket.remove(iIndexOfChild)) {
            this.mHiddenViews.remove(view);
        }
        this.mCallback.removeViewAt(iIndexOfChild);
    }

    void removeViewAt(int i) {
        int offset = getOffset(i);
        View childAt = this.mCallback.getChildAt(offset);
        if (childAt == null) {
            return;
        }
        if (this.mBucket.remove(offset)) {
            this.mHiddenViews.remove(childAt);
        }
        this.mCallback.removeViewAt(offset);
    }

    boolean removeViewIfHidden(View view) {
        int iIndexOfChild = this.mCallback.indexOfChild(view);
        if (iIndexOfChild == -1) {
            this.mHiddenViews.remove(view);
            return true;
        }
        if (!this.mBucket.get(iIndexOfChild)) {
            return false;
        }
        this.mBucket.remove(iIndexOfChild);
        this.mHiddenViews.remove(view);
        this.mCallback.removeViewAt(iIndexOfChild);
        return true;
    }

    public String toString() {
        return this.mBucket.toString() + ", hidden list:" + this.mHiddenViews.size();
    }
}
