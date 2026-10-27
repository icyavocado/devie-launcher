package com.voxel.simplesearchlauncher.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.net.Uri;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/* JADX INFO: loaded from: classes2.dex */
public class BitmapUtil {
    public static Bitmap createIconBitmap(Bitmap bitmap, Context context, int i) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width <= i || height <= i) {
            return (width == i && height == i) ? bitmap : createIconBitmap(new BitmapDrawable(context.getResources(), bitmap), context, i);
        }
        return Bitmap.createScaledBitmap(bitmap, i, i, true);
    }

    /* JADX WARN: Code duplicated, block: B:18:0x0049  */
    public static Bitmap createIconBitmap(Drawable drawable, Context context, int i) {
        int i2;
        int i3;
        if (drawable instanceof PaintDrawable) {
            PaintDrawable paintDrawable = (PaintDrawable) drawable;
            paintDrawable.setIntrinsicWidth(i);
            paintDrawable.setIntrinsicHeight(i);
        } else if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap().getDensity() == 0) {
                bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
            }
        }
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            i2 = i;
            i3 = i2;
        } else {
            float f = intrinsicWidth / (float) intrinsicHeight;
            if (intrinsicWidth > intrinsicHeight) {
                i3 = (int) (i / f);
                i2 = i;
            } else if (intrinsicHeight > intrinsicWidth) {
                i2 = (int) (i * f);
                i3 = i;
            } else {
                i2 = i;
                i3 = i2;
            }
        }
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(i, i, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setDrawFilter(new PaintFlagsDrawFilter(4, 2));
        canvas.setBitmap(bitmapCreateBitmap);
        int i4 = (i - i2) / 2;
        int i5 = (i - i3) / 2;
        Rect rect = new Rect();
        rect.set(drawable.getBounds());
        drawable.setBounds(i4, i5, i2 + i4, i3 + i5);
        drawable.draw(canvas);
        drawable.setBounds(rect);
        canvas.setBitmap(null);
        return bitmapCreateBitmap;
    }

    public static Bitmap getBitmap(Drawable drawable, int i) {
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(i, i, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapCreateBitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmapCreateBitmap;
    }

    public static Bitmap getBitmap(Uri uri, ContentResolver contentResolver) {
        /* REWORK: jadx tangled the decode path; restore the dex's simple open/decode/close. */
        InputStream inputStreamOpenInputStream = null;
        try {
            inputStreamOpenInputStream = contentResolver.openInputStream(uri);
            return BitmapFactory.decodeStream(inputStreamOpenInputStream);
        } catch (IOException unused) { /* REWORK: FileNotFoundException is subsumed */
            return null;
        } catch (RuntimeException unused2) {
            return null;
        } finally {
            if (inputStreamOpenInputStream != null) {
                try {
                    inputStreamOpenInputStream.close();
                } catch (IOException unused3) {
                }
            }
        }
    }

    public static String getUrlFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return String.format("data:image/bmp;base64,%s", Base64.encodeToString(byteArrayOutputStream.toByteArray(), 0));
    }
}
