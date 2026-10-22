package com.android.gallery3d.exif;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.TreeMap;

/* JADX INFO: loaded from: classes.dex */
class ExifParser {
    private int mApp1End;
    private boolean mContainExifData;
    private byte[] mDataAboveIfd0;
    private int mIfd0Position;
    private int mIfdType;
    private ImageEvent mImageEvent;
    private final ExifInterface mInterface;
    private ExifTag mJpegSizeTag;
    private boolean mNeedToParseOffsetsInCurrentIfd;
    private final int mOptions;
    private ExifTag mStripSizeTag;
    private ExifTag mTag;
    private int mTiffStartPosition;
    private final CountedDataInputStream mTiffStream;
    private static final Charset US_ASCII = Charset.forName("US-ASCII");
    private static final short TAG_EXIF_IFD = ExifInterface.getTrueTagKey(ExifInterface.TAG_EXIF_IFD);
    private static final short TAG_GPS_IFD = ExifInterface.getTrueTagKey(ExifInterface.TAG_GPS_IFD);
    private static final short TAG_INTEROPERABILITY_IFD = ExifInterface.getTrueTagKey(ExifInterface.TAG_INTEROPERABILITY_IFD);
    private static final short TAG_JPEG_INTERCHANGE_FORMAT = ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT);
    private static final short TAG_JPEG_INTERCHANGE_FORMAT_LENGTH = ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH);
    private static final short TAG_STRIP_OFFSETS = ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_OFFSETS);
    private static final short TAG_STRIP_BYTE_COUNTS = ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_BYTE_COUNTS);
    private int mIfdStartOffset = 0;
    private int mNumOfTagInIfd = 0;
    private int mOffsetToApp1EndFromSOF = 0;
    private final TreeMap<Integer, Object> mCorrespondingEvent = new TreeMap<>();

    private static class ExifTagEvent {
        boolean isRequested;
        ExifTag tag;

        ExifTagEvent(ExifTag exifTag, boolean z) {
            this.tag = exifTag;
            this.isRequested = z;
        }
    }

    private static class IfdEvent {
        int ifd;
        boolean isRequested;

        IfdEvent(int i, boolean z) {
            this.ifd = i;
            this.isRequested = z;
        }
    }

    private static class ImageEvent {
        int stripIndex;
        int type;

        ImageEvent(int i) {
            this.stripIndex = 0;
            this.type = i;
        }

        ImageEvent(int i, int i2) {
            this.type = i;
            this.stripIndex = i2;
        }
    }

    private ExifParser(InputStream inputStream, int i, ExifInterface exifInterface) throws ExifInvalidFormatException, IOException {
        this.mContainExifData = false;
        if (inputStream == null) {
            throw new IOException("Null argument inputStream to ExifParser");
        }
        this.mInterface = exifInterface;
        this.mContainExifData = seekTiffData(inputStream);
        this.mTiffStream = new CountedDataInputStream(inputStream);
        this.mOptions = i;
        if (this.mContainExifData) {
            parseTiffHeader();
            long unsignedInt = this.mTiffStream.readUnsignedInt();
            if (unsignedInt > 2147483647L) {
                throw new ExifInvalidFormatException("Invalid offset " + unsignedInt);
            }
            int i2 = (int) unsignedInt;
            this.mIfd0Position = i2;
            this.mIfdType = 0;
            if (isIfdRequested(0) || needToParseOffsetsInCurrentIfd()) {
                registerIfd(0, unsignedInt);
                if (unsignedInt != 8) {
                    this.mDataAboveIfd0 = new byte[i2 - 8];
                    read(this.mDataAboveIfd0);
                }
            }
        }
    }

    private boolean checkAllowed(int i, int i2) {
        int i3 = this.mInterface.getTagInfo().get(i2);
        if (i3 == 0) {
            return false;
        }
        return ExifInterface.isIfdAllowed(i3, i);
    }

    private void checkOffsetOrImageTag(ExifTag exifTag) {
        if (exifTag.getComponentCount() == 0) {
            return;
        }
        short tagId = exifTag.getTagId();
        int ifd = exifTag.getIfd();
        if (tagId == TAG_EXIF_IFD && checkAllowed(ifd, ExifInterface.TAG_EXIF_IFD)) {
            if (isIfdRequested(2) || isIfdRequested(3)) {
                registerIfd(2, exifTag.getValueAt(0));
                return;
            }
            return;
        }
        if (tagId == TAG_GPS_IFD && checkAllowed(ifd, ExifInterface.TAG_GPS_IFD)) {
            if (isIfdRequested(4)) {
                registerIfd(4, exifTag.getValueAt(0));
                return;
            }
            return;
        }
        if (tagId == TAG_INTEROPERABILITY_IFD && checkAllowed(ifd, ExifInterface.TAG_INTEROPERABILITY_IFD)) {
            if (isIfdRequested(3)) {
                registerIfd(3, exifTag.getValueAt(0));
                return;
            }
            return;
        }
        if (tagId == TAG_JPEG_INTERCHANGE_FORMAT && checkAllowed(ifd, ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT)) {
            if (isThumbnailRequested()) {
                registerCompressedImage(exifTag.getValueAt(0));
                return;
            }
            return;
        }
        if (tagId == TAG_JPEG_INTERCHANGE_FORMAT_LENGTH && checkAllowed(ifd, ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH)) {
            if (isThumbnailRequested()) {
                this.mJpegSizeTag = exifTag;
                return;
            }
            return;
        }
        if (tagId != TAG_STRIP_OFFSETS || !checkAllowed(ifd, ExifInterface.TAG_STRIP_OFFSETS)) {
            if (tagId == TAG_STRIP_BYTE_COUNTS && checkAllowed(ifd, ExifInterface.TAG_STRIP_BYTE_COUNTS) && isThumbnailRequested() && exifTag.hasValue()) {
                this.mStripSizeTag = exifTag;
                return;
            }
            return;
        }
        if (isThumbnailRequested()) {
            if (!exifTag.hasValue()) {
                this.mCorrespondingEvent.put(Integer.valueOf(exifTag.getOffset()), new ExifTagEvent(exifTag, false));
                return;
            }
            for (int i = 0; i < exifTag.getComponentCount(); i++) {
                if (exifTag.getDataType() == 3) {
                    registerUncompressedStrip(i, exifTag.getValueAt(i));
                } else {
                    registerUncompressedStrip(i, exifTag.getValueAt(i));
                }
            }
        }
    }

    private boolean isIfdRequested(int i) {
        switch (i) {
            case 0:
                return (this.mOptions & 1) != 0;
            case 1:
                return (this.mOptions & 2) != 0;
            case 2:
                return (this.mOptions & 4) != 0;
            case 3:
                return (this.mOptions & 16) != 0;
            case 4:
                return (this.mOptions & 8) != 0;
            default:
                return false;
        }
    }

    private boolean isThumbnailRequested() {
        return (this.mOptions & 32) != 0;
    }

    private boolean needToParseOffsetsInCurrentIfd() {
        switch (this.mIfdType) {
            case 0:
                return isIfdRequested(2) || isIfdRequested(4) || isIfdRequested(3) || isIfdRequested(1);
            case 1:
                return isThumbnailRequested();
            case 2:
                return isIfdRequested(3);
            default:
                return false;
        }
    }

    protected static ExifParser parse(InputStream inputStream, ExifInterface exifInterface) throws ExifInvalidFormatException, IOException {
        return new ExifParser(inputStream, 63, exifInterface);
    }

    private void parseTiffHeader() throws ExifInvalidFormatException, IOException {
        short s = this.mTiffStream.readShort();
        if (18761 == s) {
            this.mTiffStream.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        } else {
            if (19789 != s) {
                throw new ExifInvalidFormatException("Invalid TIFF header");
            }
            this.mTiffStream.setByteOrder(ByteOrder.BIG_ENDIAN);
        }
        if (this.mTiffStream.readShort() != 42) {
            throw new ExifInvalidFormatException("Invalid TIFF header");
        }
    }

    private ExifTag readTag() throws ExifInvalidFormatException, IOException {
        short s = this.mTiffStream.readShort();
        short s2 = this.mTiffStream.readShort();
        long unsignedInt = this.mTiffStream.readUnsignedInt();
        if (unsignedInt > 2147483647L) {
            throw new ExifInvalidFormatException("Number of component is larger then Integer.MAX_VALUE");
        }
        if (!ExifTag.isValidType(s2)) {
            Log.w("ExifParser", String.format("Tag %04x: Invalid data type %d", Short.valueOf(s), Short.valueOf(s2)));
            this.mTiffStream.skip(4L);
            return null;
        }
        int i = (int) unsignedInt;
        ExifTag exifTag = new ExifTag(s, s2, i, this.mIfdType, i != 0);
        int dataSize = exifTag.getDataSize();
        if (dataSize > 4) {
            long unsignedInt2 = this.mTiffStream.readUnsignedInt();
            if (unsignedInt2 > 2147483647L) {
                throw new ExifInvalidFormatException("offset is larger then Integer.MAX_VALUE");
            }
            if (unsignedInt2 >= this.mIfd0Position || s2 != 7) {
                exifTag.setOffset((int) unsignedInt2);
            } else {
                byte[] bArr = new byte[i];
                System.arraycopy(this.mDataAboveIfd0, ((int) unsignedInt2) - 8, bArr, 0, i);
                exifTag.setValue(bArr);
            }
        } else {
            boolean zHasDefinedCount = exifTag.hasDefinedCount();
            exifTag.setHasDefinedCount(false);
            readFullTagValue(exifTag);
            exifTag.setHasDefinedCount(zHasDefinedCount);
            this.mTiffStream.skip(4 - dataSize);
            exifTag.setOffset(this.mTiffStream.getReadByteCount() - 4);
        }
        return exifTag;
    }

    private void registerCompressedImage(long j) {
        this.mCorrespondingEvent.put(Integer.valueOf((int) j), new ImageEvent(3));
    }

    private void registerIfd(int i, long j) {
        this.mCorrespondingEvent.put(Integer.valueOf((int) j), new IfdEvent(i, isIfdRequested(i)));
    }

    private void registerUncompressedStrip(int i, long j) {
        this.mCorrespondingEvent.put(Integer.valueOf((int) j), new ImageEvent(4, i));
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    private boolean seekTiffData(InputStream inputStream) throws ExifInvalidFormatException, IOException {
        CountedDataInputStream countedDataInputStream = new CountedDataInputStream(inputStream);
        if (countedDataInputStream.readShort() != -40) {
            throw new ExifInvalidFormatException("Invalid JPEG format");
        }
        for (short s = countedDataInputStream.readShort(); s != -39 && !JpegHeader.isSofMarker(s); s = countedDataInputStream.readShort()) {
            int unsignedShort = countedDataInputStream.readUnsignedShort();
            if (s == -31 && unsignedShort >= 8) {
                int i = countedDataInputStream.readInt();
                short s2 = countedDataInputStream.readShort();
                unsignedShort -= 6;
                if (i == 1165519206 && s2 == 0) {
                    this.mTiffStartPosition = countedDataInputStream.getReadByteCount();
                    this.mApp1End = unsignedShort;
                    this.mOffsetToApp1EndFromSOF = this.mTiffStartPosition + this.mApp1End;
                    return true;
                }
            }
            if (unsignedShort >= 2) {
                long j = unsignedShort - 2;
                if (j == countedDataInputStream.skip(j)) {
                }
            }
            Log.w("ExifParser", "Invalid JPEG format.");
            return false;
        }
        return false;
    }

    private void skipTo(int i) throws IOException {
        this.mTiffStream.skipTo(i);
        while (!this.mCorrespondingEvent.isEmpty() && this.mCorrespondingEvent.firstKey().intValue() < i) {
            this.mCorrespondingEvent.pollFirstEntry();
        }
    }

    protected ByteOrder getByteOrder() {
        return this.mTiffStream.getByteOrder();
    }

    protected int getCompressedImageSize() {
        ExifTag exifTag = this.mJpegSizeTag;
        if (exifTag == null) {
            return 0;
        }
        return (int) exifTag.getValueAt(0);
    }

    protected int getCurrentIfd() {
        return this.mIfdType;
    }

    protected int getStripIndex() {
        return this.mImageEvent.stripIndex;
    }

    protected int getStripSize() {
        ExifTag exifTag = this.mStripSizeTag;
        if (exifTag == null) {
            return 0;
        }
        return (int) exifTag.getValueAt(0);
    }

    protected ExifTag getTag() {
        return this.mTag;
    }

    protected int next() throws ExifInvalidFormatException, IOException {
        if (!this.mContainExifData) {
            return 5;
        }
        int readByteCount = this.mTiffStream.getReadByteCount();
        int i = this.mIfdStartOffset + 2 + (this.mNumOfTagInIfd * 12);
        if (readByteCount < i) {
            this.mTag = readTag();
            ExifTag exifTag = this.mTag;
            if (exifTag == null) {
                return next();
            }
            if (this.mNeedToParseOffsetsInCurrentIfd) {
                checkOffsetOrImageTag(exifTag);
            }
            return 1;
        }
        if (readByteCount == i) {
            if (this.mIfdType == 0) {
                long unsignedLong = readUnsignedLong();
                if ((isIfdRequested(1) || isThumbnailRequested()) && unsignedLong != 0) {
                    registerIfd(1, unsignedLong);
                }
            } else {
                int iIntValue = this.mCorrespondingEvent.size() > 0 ? this.mCorrespondingEvent.firstEntry().getKey().intValue() - this.mTiffStream.getReadByteCount() : 4;
                if (iIntValue < 4) {
                    Log.w("ExifParser", "Invalid size of link to next IFD: " + iIntValue);
                } else {
                    long unsignedLong2 = readUnsignedLong();
                    if (unsignedLong2 != 0) {
                        Log.w("ExifParser", "Invalid link to next IFD: " + unsignedLong2);
                    }
                }
            }
        }
        while (this.mCorrespondingEvent.size() != 0) {
            Map.Entry<Integer, Object> entryPollFirstEntry = this.mCorrespondingEvent.pollFirstEntry();
            Object value = entryPollFirstEntry.getValue();
            try {
                skipTo(entryPollFirstEntry.getKey().intValue());
                if (value instanceof IfdEvent) {
                    IfdEvent ifdEvent = (IfdEvent) value;
                    this.mIfdType = ifdEvent.ifd;
                    this.mNumOfTagInIfd = this.mTiffStream.readUnsignedShort();
                    this.mIfdStartOffset = entryPollFirstEntry.getKey().intValue();
                    if ((this.mNumOfTagInIfd * 12) + this.mIfdStartOffset + 2 > this.mApp1End) {
                        Log.w("ExifParser", "Invalid size of IFD " + this.mIfdType);
                        return 5;
                    }
                    this.mNeedToParseOffsetsInCurrentIfd = needToParseOffsetsInCurrentIfd();
                    if (ifdEvent.isRequested) {
                        return 0;
                    }
                    skipRemainingTagsInCurrentIfd();
                } else {
                    if (value instanceof ImageEvent) {
                        this.mImageEvent = (ImageEvent) value;
                        return this.mImageEvent.type;
                    }
                    ExifTagEvent exifTagEvent = (ExifTagEvent) value;
                    this.mTag = exifTagEvent.tag;
                    if (this.mTag.getDataType() != 7) {
                        readFullTagValue(this.mTag);
                        checkOffsetOrImageTag(this.mTag);
                    }
                    if (exifTagEvent.isRequested) {
                        return 2;
                    }
                }
            } catch (IOException unused) {
                Log.w("ExifParser", "Failed to skip to data at: " + entryPollFirstEntry.getKey() + " for " + value.getClass().getName() + ", the file may be broken.");
            }
        }
        return 5;
    }

    protected int read(byte[] bArr) throws IOException {
        return this.mTiffStream.read(bArr);
    }

    protected void readFullTagValue(ExifTag exifTag) throws IOException {
        short dataType = exifTag.getDataType();
        if (dataType == 2 || dataType == 7 || dataType == 1) {
            int componentCount = exifTag.getComponentCount();
            if (this.mCorrespondingEvent.size() > 0 && this.mCorrespondingEvent.firstEntry().getKey().intValue() < this.mTiffStream.getReadByteCount() + componentCount) {
                Object value = this.mCorrespondingEvent.firstEntry().getValue();
                if (value instanceof ImageEvent) {
                    Log.w("ExifParser", "Thumbnail overlaps value for tag: \n" + exifTag.toString());
                    Log.w("ExifParser", "Invalid thumbnail offset: " + this.mCorrespondingEvent.pollFirstEntry().getKey());
                } else {
                    if (value instanceof IfdEvent) {
                        Log.w("ExifParser", "Ifd " + ((IfdEvent) value).ifd + " overlaps value for tag: \n" + exifTag.toString());
                    } else if (value instanceof ExifTagEvent) {
                        Log.w("ExifParser", "Tag value for tag: \n" + ((ExifTagEvent) value).tag.toString() + " overlaps value for tag: \n" + exifTag.toString());
                    }
                    int iIntValue = this.mCorrespondingEvent.firstEntry().getKey().intValue() - this.mTiffStream.getReadByteCount();
                    Log.w("ExifParser", "Invalid size of tag: \n" + exifTag.toString() + " setting count to: " + iIntValue);
                    exifTag.forceSetComponentCount(iIntValue);
                }
            }
        }
        int i = 0;
        switch (exifTag.getDataType()) {
            case 1:
            case 7:
                byte[] bArr = new byte[exifTag.getComponentCount()];
                read(bArr);
                exifTag.setValue(bArr);
                break;
            case 2:
                exifTag.setValue(readString(exifTag.getComponentCount()));
                break;
            case 3:
                int[] iArr = new int[exifTag.getComponentCount()];
                int length = iArr.length;
                while (i < length) {
                    iArr[i] = readUnsignedShort();
                    i++;
                }
                exifTag.setValue(iArr);
                break;
            case 4:
                long[] jArr = new long[exifTag.getComponentCount()];
                int length2 = jArr.length;
                while (i < length2) {
                    jArr[i] = readUnsignedLong();
                    i++;
                }
                exifTag.setValue(jArr);
                break;
            case 5:
                Rational[] rationalArr = new Rational[exifTag.getComponentCount()];
                int length3 = rationalArr.length;
                while (i < length3) {
                    rationalArr[i] = readUnsignedRational();
                    i++;
                }
                exifTag.setValue(rationalArr);
                break;
            case 9:
                int[] iArr2 = new int[exifTag.getComponentCount()];
                int length4 = iArr2.length;
                while (i < length4) {
                    iArr2[i] = readLong();
                    i++;
                }
                exifTag.setValue(iArr2);
                break;
            case 10:
                Rational[] rationalArr2 = new Rational[exifTag.getComponentCount()];
                int length5 = rationalArr2.length;
                while (i < length5) {
                    rationalArr2[i] = readRational();
                    i++;
                }
                exifTag.setValue(rationalArr2);
                break;
        }
    }

    protected int readLong() throws IOException {
        return this.mTiffStream.readInt();
    }

    protected Rational readRational() throws IOException {
        return new Rational(readLong(), readLong());
    }

    protected String readString(int i) throws IOException {
        return readString(i, US_ASCII);
    }

    protected String readString(int i, Charset charset) throws IOException {
        return i > 0 ? this.mTiffStream.readString(i, charset) : "";
    }

    protected long readUnsignedLong() throws IOException {
        return ((long) readLong()) & 4294967295L;
    }

    protected Rational readUnsignedRational() throws IOException {
        return new Rational(readUnsignedLong(), readUnsignedLong());
    }

    protected int readUnsignedShort() throws IOException {
        return this.mTiffStream.readShort() & 65535;
    }

    protected void registerForTagValue(ExifTag exifTag) {
        if (exifTag.getOffset() >= this.mTiffStream.getReadByteCount()) {
            this.mCorrespondingEvent.put(Integer.valueOf(exifTag.getOffset()), new ExifTagEvent(exifTag, true));
        }
    }

    protected void skipRemainingTagsInCurrentIfd() throws ExifInvalidFormatException, IOException {
        int i = this.mIfdStartOffset + 2 + (this.mNumOfTagInIfd * 12);
        int readByteCount = this.mTiffStream.getReadByteCount();
        if (readByteCount > i) {
            return;
        }
        if (this.mNeedToParseOffsetsInCurrentIfd) {
            while (readByteCount < i) {
                this.mTag = readTag();
                readByteCount += 12;
                ExifTag exifTag = this.mTag;
                if (exifTag != null) {
                    checkOffsetOrImageTag(exifTag);
                }
            }
        } else {
            skipTo(i);
        }
        long unsignedLong = readUnsignedLong();
        if (this.mIfdType == 0) {
            if ((isIfdRequested(1) || isThumbnailRequested()) && unsignedLong > 0) {
                registerIfd(1, unsignedLong);
            }
        }
    }
}
