package com.android.gallery3d.exif;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
public class ExifTag {
    private static final SimpleDateFormat TIME_FORMAT;
    private int mComponentCountActual;
    private final short mDataType;
    private boolean mHasDefinedDefaultComponentCount;
    private int mIfd;
    private int mOffset;
    private final short mTagId;
    private Object mValue = null;
    private static Charset US_ASCII = Charset.forName("US-ASCII");
    private static final int[] TYPE_TO_SIZE_MAP = new int[11];

    static {
        int[] iArr = TYPE_TO_SIZE_MAP;
        iArr[1] = 1;
        iArr[2] = 1;
        iArr[3] = 2;
        iArr[4] = 4;
        iArr[5] = 8;
        iArr[7] = 1;
        iArr[9] = 4;
        iArr[10] = 8;
        TIME_FORMAT = new SimpleDateFormat("yyyy:MM:dd kk:mm:ss");
    }

    ExifTag(short s, short s2, int i, int i2, boolean z) {
        this.mTagId = s;
        this.mDataType = s2;
        this.mComponentCountActual = i;
        this.mHasDefinedDefaultComponentCount = z;
        this.mIfd = i2;
    }

    private boolean checkBadComponentCount(int i) {
        return this.mHasDefinedDefaultComponentCount && this.mComponentCountActual != i;
    }

    private boolean checkOverflowForRational(Rational[] rationalArr) {
        for (Rational rational : rationalArr) {
            if (rational.getNumerator() < -2147483648L || rational.getDenominator() < -2147483648L || rational.getNumerator() > 2147483647L || rational.getDenominator() > 2147483647L) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOverflowForUnsignedLong(int[] iArr) {
        for (int i : iArr) {
            if (i < 0) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOverflowForUnsignedLong(long[] jArr) {
        for (long j : jArr) {
            if (j < 0 || j > 4294967295L) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOverflowForUnsignedRational(Rational[] rationalArr) {
        for (Rational rational : rationalArr) {
            if (rational.getNumerator() < 0 || rational.getDenominator() < 0 || rational.getNumerator() > 4294967295L || rational.getDenominator() > 4294967295L) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOverflowForUnsignedShort(int[] iArr) {
        for (int i : iArr) {
            if (i > 65535 || i < 0) {
                return true;
            }
        }
        return false;
    }

    private static String convertTypeToString(short s) {
        switch (s) {
            case 1:
                return "UNSIGNED_BYTE";
            case 2:
                return "ASCII";
            case 3:
                return "UNSIGNED_SHORT";
            case 4:
                return "UNSIGNED_LONG";
            case 5:
                return "UNSIGNED_RATIONAL";
            case 6:
            case 8:
            default:
                return "";
            case 7:
                return "UNDEFINED";
            case 9:
                return "LONG";
            case 10:
                return "RATIONAL";
        }
    }

    public static int getElementSize(short s) {
        return TYPE_TO_SIZE_MAP[s];
    }

    public static boolean isValidIfd(int i) {
        return i == 0 || i == 1 || i == 2 || i == 3 || i == 4;
    }

    public static boolean isValidType(short s) {
        return s == 1 || s == 2 || s == 3 || s == 4 || s == 5 || s == 7 || s == 9 || s == 10;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ExifTag)) {
            return false;
        }
        ExifTag exifTag = (ExifTag) obj;
        if (exifTag.mTagId != this.mTagId || exifTag.mComponentCountActual != this.mComponentCountActual || exifTag.mDataType != this.mDataType) {
            return false;
        }
        Object obj2 = this.mValue;
        if (obj2 == null) {
            return exifTag.mValue == null;
        }
        Object obj3 = exifTag.mValue;
        if (obj3 == null) {
            return false;
        }
        if (obj2 instanceof long[]) {
            if (obj3 instanceof long[]) {
                return Arrays.equals((long[]) obj2, (long[]) obj3);
            }
            return false;
        }
        if (obj2 instanceof Rational[]) {
            if (obj3 instanceof Rational[]) {
                return Arrays.equals((Rational[]) obj2, (Rational[]) obj3);
            }
            return false;
        }
        if (!(obj2 instanceof byte[])) {
            return obj2.equals(obj3);
        }
        if (obj3 instanceof byte[]) {
            return Arrays.equals((byte[]) obj2, (byte[]) obj3);
        }
        return false;
    }

    public String forceGetValueAsString() {
        Object obj = this.mValue;
        if (obj == null) {
            return "";
        }
        if (obj instanceof byte[]) {
            return this.mDataType == 2 ? new String((byte[]) obj, US_ASCII) : Arrays.toString((byte[]) obj);
        }
        if (obj instanceof long[]) {
            return ((long[]) obj).length == 1 ? String.valueOf(((long[]) obj)[0]) : Arrays.toString((long[]) obj);
        }
        if (!(obj instanceof Object[])) {
            return obj.toString();
        }
        if (((Object[]) obj).length != 1) {
            return Arrays.toString((Object[]) obj);
        }
        Object obj2 = ((Object[]) obj)[0];
        return obj2 == null ? "" : obj2.toString();
    }

    protected void forceSetComponentCount(int i) {
        this.mComponentCountActual = i;
    }

    public int getComponentCount() {
        return this.mComponentCountActual;
    }

    public int getDataSize() {
        return getComponentCount() * getElementSize(getDataType());
    }

    public short getDataType() {
        return this.mDataType;
    }

    public int getIfd() {
        return this.mIfd;
    }

    protected int getOffset() {
        return this.mOffset;
    }

    public short getTagId() {
        return this.mTagId;
    }

    public int[] getValueAsInts() {
        Object obj = this.mValue;
        if (obj == null || !(obj instanceof long[])) {
            return null;
        }
        long[] jArr = (long[]) obj;
        int[] iArr = new int[jArr.length];
        for (int i = 0; i < jArr.length; i++) {
            iArr[i] = (int) jArr[i];
        }
        return iArr;
    }

    protected long getValueAt(int i) {
        Object obj = this.mValue;
        if (obj instanceof long[]) {
            return ((long[]) obj)[i];
        }
        if (obj instanceof byte[]) {
            return ((byte[]) obj)[i];
        }
        throw new IllegalArgumentException("Cannot get integer value from " + convertTypeToString(this.mDataType));
    }

    protected boolean hasDefinedCount() {
        return this.mHasDefinedDefaultComponentCount;
    }

    public boolean hasValue() {
        return this.mValue != null;
    }

    protected void setHasDefinedCount(boolean z) {
        this.mHasDefinedDefaultComponentCount = z;
    }

    protected void setIfd(int i) {
        this.mIfd = i;
    }

    protected void setOffset(int i) {
        this.mOffset = i;
    }

    public boolean setValue(String str) {
        short s = this.mDataType;
        if (s != 2 && s != 7) {
            return false;
        }
        byte[] bytes = str.getBytes(US_ASCII);
        if (bytes.length > 0) {
            if (bytes[bytes.length - 1] != 0 && this.mDataType != 7) {
                bytes = Arrays.copyOf(bytes, bytes.length + 1);
            }
        } else if (this.mDataType == 2 && this.mComponentCountActual == 1) {
            bytes = new byte[]{0};
        }
        int length = bytes.length;
        if (checkBadComponentCount(length)) {
            return false;
        }
        this.mComponentCountActual = length;
        this.mValue = bytes;
        return true;
    }

    public boolean setValue(byte[] bArr) {
        return setValue(bArr, 0, bArr.length);
    }

    public boolean setValue(byte[] bArr, int i, int i2) {
        if (checkBadComponentCount(i2)) {
            return false;
        }
        short s = this.mDataType;
        if (s != 1 && s != 7) {
            return false;
        }
        this.mValue = new byte[i2];
        System.arraycopy(bArr, i, this.mValue, 0, i2);
        this.mComponentCountActual = i2;
        return true;
    }

    public boolean setValue(int[] iArr) {
        if (checkBadComponentCount(iArr.length)) {
            return false;
        }
        short s = this.mDataType;
        if (s != 3 && s != 9 && s != 4) {
            return false;
        }
        if (this.mDataType == 3 && checkOverflowForUnsignedShort(iArr)) {
            return false;
        }
        if (this.mDataType == 4 && checkOverflowForUnsignedLong(iArr)) {
            return false;
        }
        long[] jArr = new long[iArr.length];
        for (int i = 0; i < iArr.length; i++) {
            jArr[i] = iArr[i];
        }
        this.mValue = jArr;
        this.mComponentCountActual = iArr.length;
        return true;
    }

    public boolean setValue(long[] jArr) {
        if (checkBadComponentCount(jArr.length) || this.mDataType != 4 || checkOverflowForUnsignedLong(jArr)) {
            return false;
        }
        this.mValue = jArr;
        this.mComponentCountActual = jArr.length;
        return true;
    }

    public boolean setValue(Rational[] rationalArr) {
        if (checkBadComponentCount(rationalArr.length)) {
            return false;
        }
        short s = this.mDataType;
        if (s != 5 && s != 10) {
            return false;
        }
        if (this.mDataType == 5 && checkOverflowForUnsignedRational(rationalArr)) {
            return false;
        }
        if (this.mDataType == 10 && checkOverflowForRational(rationalArr)) {
            return false;
        }
        this.mValue = rationalArr;
        this.mComponentCountActual = rationalArr.length;
        return true;
    }

    public String toString() {
        return String.format("tag id: %04X\n", Short.valueOf(this.mTagId)) + "ifd id: " + this.mIfd + "\ntype: " + convertTypeToString(this.mDataType) + "\ncount: " + this.mComponentCountActual + "\noffset: " + this.mOffset + "\nvalue: " + forceGetValueAsString() + "\n";
    }
}
