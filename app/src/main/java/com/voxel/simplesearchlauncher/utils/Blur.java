package com.voxel.simplesearchlauncher.utils;

import android.content.Context;
import android.graphics.Bitmap;
import java.lang.reflect.Array;

/* JADX INFO: loaded from: classes2.dex */
public class Blur {
    public static Bitmap fastBlur(Context context, Bitmap bitmap, int i, float f) {
        int i2 = i;
        Bitmap bitmapCopy = bitmap.copy(bitmap.getConfig(), true);
        if (i2 < 1) {
            return null;
        }
        int width = bitmapCopy.getWidth();
        int height = bitmapCopy.getHeight();
        int i3 = width * height;
        int[] iArr = new int[i3];
        bitmapCopy.getPixels(iArr, 0, width, 0, 0, width, height);
        int i4 = width - 1;
        int i5 = height - 1;
        int i6 = i2 + i2 + 1;
        int[] iArr2 = new int[i3];
        int[] iArr3 = new int[i3];
        int[] iArr4 = new int[i3];
        int[] iArr5 = new int[Math.max(width, height)];
        int i7 = (i6 + 1) >> 1;
        int i8 = i7 * i7;
        int i9 = i8 * 256;
        int[] iArr6 = new int[i9];
        for (int i10 = 0; i10 < i9; i10++) {
            iArr6[i10] = i10 / i8;
        }
        int[][] iArr7 = (int[][]) Array.newInstance((Class<?>) int.class, i6, 3);
        int i11 = i2 + 1;
        int i12 = 0;
        int i13 = 0;
        int i14 = 0;
        while (i12 < height) {
            Bitmap bitmap2 = bitmapCopy;
            int i15 = -i2;
            int i16 = 0;
            int i17 = 0;
            int i18 = 0;
            int i19 = 0;
            int i20 = 0;
            int i21 = 0;
            int i22 = 0;
            int i23 = 0;
            int i24 = 0;
            while (i15 <= i2) {
                int i25 = i5;
                int i26 = height;
                int i27 = iArr[i13 + Math.min(i4, Math.max(i15, 0))];
                int[] iArr8 = iArr7[i15 + i2];
                iArr8[0] = (i27 & 16711680) >> 16;
                iArr8[1] = (i27 & 65280) >> 8;
                iArr8[2] = i27 & 255;
                int iAbs = i11 - Math.abs(i15);
                i16 += iArr8[0] * iAbs;
                i17 += iArr8[1] * iAbs;
                i18 += iArr8[2] * iAbs;
                if (i15 > 0) {
                    i22 += iArr8[0];
                    i23 += iArr8[1];
                    i24 += iArr8[2];
                } else {
                    i19 += iArr8[0];
                    i20 += iArr8[1];
                    i21 += iArr8[2];
                }
                i15++;
                height = i26;
                i5 = i25;
            }
            int i28 = i5;
            int i29 = height;
            int i30 = i2;
            int i31 = 0;
            while (i31 < width) {
                iArr2[i13] = iArr6[i16];
                iArr3[i13] = iArr6[i17];
                iArr4[i13] = iArr6[i18];
                int i32 = i16 - i19;
                int i33 = i17 - i20;
                int i34 = i18 - i21;
                int[] iArr9 = iArr7[((i30 - i2) + i6) % i6];
                int i35 = i19 - iArr9[0];
                int i36 = i20 - iArr9[1];
                int i37 = i21 - iArr9[2];
                if (i12 == 0) {
                    iArr5[i31] = Math.min(i31 + i2 + 1, i4);
                }
                int i38 = iArr[i14 + iArr5[i31]];
                iArr9[0] = (i38 & 16711680) >> 16;
                iArr9[1] = (i38 & 65280) >> 8;
                iArr9[2] = i38 & 255;
                int i39 = i22 + iArr9[0];
                int i40 = i23 + iArr9[1];
                int i41 = i24 + iArr9[2];
                i16 = i32 + i39;
                i17 = i33 + i40;
                i18 = i34 + i41;
                i30 = (i30 + 1) % i6;
                int[] iArr10 = iArr7[i30 % i6];
                i19 = i35 + iArr10[0];
                i20 = i36 + iArr10[1];
                i21 = i37 + iArr10[2];
                i22 = i39 - iArr10[0];
                i23 = i40 - iArr10[1];
                i24 = i41 - iArr10[2];
                i13++;
                i31++;
                iArr6 = iArr6;
            }
            i14 += width;
            i12++;
            bitmapCopy = bitmap2;
            height = i29;
            i5 = i28;
        }
        Bitmap bitmap3 = bitmapCopy;
        int i42 = i5;
        int i43 = height;
        int[] iArr11 = iArr6;
        int i44 = 0;
        while (i44 < width) {
            int i45 = -i2;
            int i46 = i45 * width;
            int i47 = 0;
            int i48 = 0;
            int i49 = 0;
            int i50 = 0;
            int i51 = 0;
            int i52 = 0;
            int i53 = 0;
            int i54 = 0;
            int i55 = 0;
            while (i45 <= i2) {
                int[] iArr12 = iArr5;
                int iMax = Math.max(0, i46) + i44;
                int[] iArr13 = iArr7[i45 + i2];
                iArr13[0] = iArr2[iMax];
                iArr13[1] = iArr3[iMax];
                iArr13[2] = iArr4[iMax];
                int iAbs2 = i11 - Math.abs(i45);
                i47 += iArr2[iMax] * iAbs2;
                i48 += iArr3[iMax] * iAbs2;
                i49 += iArr4[iMax] * iAbs2;
                if (i45 > 0) {
                    i53 += iArr13[0];
                    i54 += iArr13[1];
                    i55 += iArr13[2];
                } else {
                    i50 += iArr13[0];
                    i51 += iArr13[1];
                    i52 += iArr13[2];
                }
                if (i45 < i42) {
                    i46 += width;
                }
                i45++;
                i42 = i42;
                iArr5 = iArr12;
            }
            int[] iArr14 = iArr5;
            int i56 = i42;
            int i57 = i44;
            int i58 = i54;
            int i59 = i55;
            int i60 = 0;
            int i61 = i2;
            int i62 = i53;
            int i63 = i52;
            int i64 = i51;
            int i65 = i50;
            int i66 = i49;
            int i67 = i48;
            int i68 = i47;
            int i69 = i43;
            while (i60 < i69) {
                iArr[i57] = (iArr[i57] & (-16777216)) | (iArr11[i68] << 16) | (iArr11[i67] << 8) | iArr11[i66];
                int i70 = i68 - i65;
                int i71 = i67 - i64;
                int i72 = i66 - i63;
                int[] iArr15 = iArr7[((i61 - i2) + i6) % i6];
                int i73 = i65 - iArr15[0];
                int i74 = i64 - iArr15[1];
                int i75 = i63 - iArr15[2];
                if (i44 == 0) {
                    iArr14[i60] = Math.min(i60 + i11, i56) * width;
                }
                int i76 = iArr14[i60] + i44;
                iArr15[0] = iArr2[i76];
                iArr15[1] = iArr3[i76];
                iArr15[2] = iArr4[i76];
                int i77 = i62 + iArr15[0];
                int i78 = i58 + iArr15[1];
                int i79 = i59 + iArr15[2];
                i68 = i70 + i77;
                i67 = i71 + i78;
                i66 = i72 + i79;
                i61 = (i61 + 1) % i6;
                int[] iArr16 = iArr7[i61];
                i65 = i73 + iArr16[0];
                i64 = i74 + iArr16[1];
                i63 = i75 + iArr16[2];
                i62 = i77 - iArr16[0];
                i58 = i78 - iArr16[1];
                i59 = i79 - iArr16[2];
                i57 += width;
                i60++;
                i2 = i;
            }
            i44++;
            i42 = i56;
            i43 = i69;
            iArr5 = iArr14;
            i2 = i;
        }
        bitmap3.setPixels(iArr, 0, width, 0, 0, width, i43);
        return bitmap3;
    }
}
