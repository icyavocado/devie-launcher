package com.android.gallery3d.glrenderer;

/* JADX INFO: loaded from: classes.dex */
public class IntArray {
    private int[] mData = new int[8];
    private int mSize = 0;

    public void add(int i) {
        int[] iArr = this.mData;
        int length = iArr.length;
        int i2 = this.mSize;
        if (length == i2) {
            int[] iArr2 = new int[i2 + i2];
            System.arraycopy(iArr, 0, iArr2, 0, i2);
            this.mData = iArr2;
        }
        int[] iArr3 = this.mData;
        int i3 = this.mSize;
        this.mSize = i3 + 1;
        iArr3[i3] = i;
    }

    public void clear() {
        this.mSize = 0;
        if (this.mData.length != 8) {
            this.mData = new int[8];
        }
    }

    public int[] getInternalArray() {
        return this.mData;
    }

    public int removeLast() {
        this.mSize--;
        return this.mData[this.mSize];
    }

    public int size() {
        return this.mSize;
    }
}
