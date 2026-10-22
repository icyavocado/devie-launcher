package com.android.gallery3d.exif;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
class ExifData {
    private static final byte[] USER_COMMENT_ASCII = {65, 83, 67, 73, 73, 0, 0, 0};
    private static final byte[] USER_COMMENT_JIS = {74, 73, 83, 0, 0, 0, 0, 0};
    private static final byte[] USER_COMMENT_UNICODE = {85, 78, 73, 67, 79, 68, 69, 0};
    private final ByteOrder mByteOrder;
    private final IfdData[] mIfdDatas = new IfdData[5];
    private ArrayList<byte[]> mStripBytes = new ArrayList<>();
    private byte[] mThumbnail;

    ExifData(ByteOrder byteOrder) {
        this.mByteOrder = byteOrder;
    }

    protected void addIfdData(IfdData ifdData) {
        this.mIfdDatas[ifdData.getId()] = ifdData;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof ExifData)) {
            return false;
        }
        ExifData exifData = (ExifData) obj;
        if (exifData.mByteOrder != this.mByteOrder || exifData.mStripBytes.size() != this.mStripBytes.size() || !Arrays.equals(exifData.mThumbnail, this.mThumbnail)) {
            return false;
        }
        for (int i = 0; i < this.mStripBytes.size(); i++) {
            if (!Arrays.equals(exifData.mStripBytes.get(i), this.mStripBytes.get(i))) {
                return false;
            }
        }
        for (int i2 = 0; i2 < 5; i2++) {
            IfdData ifdData = exifData.getIfdData(i2);
            IfdData ifdData2 = getIfdData(i2);
            if (ifdData != ifdData2 && ifdData != null && !ifdData.equals(ifdData2)) {
                return false;
            }
        }
        return true;
    }

    protected IfdData getIfdData(int i) {
        if (ExifTag.isValidIfd(i)) {
            return this.mIfdDatas[i];
        }
        return null;
    }

    protected ExifTag getTag(short s, int i) {
        IfdData ifdData = this.mIfdDatas[i];
        if (ifdData == null) {
            return null;
        }
        return ifdData.getTag(s);
    }

    protected void setCompressedThumbnail(byte[] bArr) {
        this.mThumbnail = bArr;
    }

    protected void setStripBytes(int i, byte[] bArr) {
        if (i < this.mStripBytes.size()) {
            this.mStripBytes.set(i, bArr);
            return;
        }
        for (int size = this.mStripBytes.size(); size < i; size++) {
            this.mStripBytes.add(null);
        }
        this.mStripBytes.add(bArr);
    }
}
