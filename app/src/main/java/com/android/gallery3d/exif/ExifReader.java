package com.android.gallery3d.exif;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;

/* JADX INFO: loaded from: classes.dex */
class ExifReader {
    private final ExifInterface mInterface;

    ExifReader(ExifInterface exifInterface) {
        this.mInterface = exifInterface;
    }

    protected ExifData read(InputStream inputStream) throws ExifInvalidFormatException, IOException {
        ExifParser exifParser = ExifParser.parse(inputStream, this.mInterface);
        ExifData exifData = new ExifData(exifParser.getByteOrder());
        for (int next = exifParser.next(); next != 5; next = exifParser.next()) {
            switch (next) {
                case 0:
                    exifData.addIfdData(new IfdData(exifParser.getCurrentIfd()));
                    break;
                case 1:
                    ExifTag tag = exifParser.getTag();
                    if (tag.hasValue()) {
                        exifData.getIfdData(tag.getIfd()).setTag(tag);
                    } else {
                        exifParser.registerForTagValue(tag);
                    }
                    break;
                case 2:
                    ExifTag tag2 = exifParser.getTag();
                    if (tag2.getDataType() == 7) {
                        exifParser.readFullTagValue(tag2);
                    }
                    exifData.getIfdData(tag2.getIfd()).setTag(tag2);
                    break;
                case 3:
                    byte[] bArr = new byte[exifParser.getCompressedImageSize()];
                    if (bArr.length == exifParser.read(bArr)) {
                        exifData.setCompressedThumbnail(bArr);
                    } else {
                        Log.w("ExifReader", "Failed to read the compressed thumbnail");
                    }
                    break;
                case 4:
                    byte[] bArr2 = new byte[exifParser.getStripSize()];
                    if (bArr2.length == exifParser.read(bArr2)) {
                        exifData.setStripBytes(exifParser.getStripIndex(), bArr2);
                    } else {
                        Log.w("ExifReader", "Failed to read the strip bytes");
                    }
                    break;
            }
        }
        return exifData;
    }
}
