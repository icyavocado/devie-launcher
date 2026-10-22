package com.voxel.simplesearchlauncher.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
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
import android.text.TextUtils;
import android.util.Base64;
import androidx.palette.graphics.Palette;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
            float f = intrinsicWidth / intrinsicHeight;
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

    public static Bitmap getBitmapFromURL(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();
            return getCircleBitmap(BitmapFactory.decodeStream(httpURLConnection.getInputStream()));
        } catch (IOException unused) {
            return null;
        }
    }

    private static Bitmap getCircleBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapCreateBitmap);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(-65536);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        bitmap.recycle();
        return bitmapCreateBitmap;
    }

    public static int getTint(Bitmap bitmap) {
        int iArgb = Color.argb(127, 0, 0, 0);
        try {
            Palette.Builder builder = new Palette.Builder(bitmap);
            builder.maximumColorCount(24);
            Palette paletteGenerate = builder.generate();
            Palette.Swatch lightVibrantSwatch = paletteGenerate.getLightVibrantSwatch();
            if (lightVibrantSwatch == null) {
                lightVibrantSwatch = paletteGenerate.getVibrantSwatch();
            }
            if (lightVibrantSwatch == null) {
                lightVibrantSwatch = paletteGenerate.getDarkVibrantSwatch();
            }
            if (lightVibrantSwatch == null) {
                return iArgb;
            }
            int i = MaterialColorMapUtils.calculatePrimaryAndSecondaryColor(lightVibrantSwatch.getRgb(), false).mPrimaryColor;
            return Color.argb(127, Color.red(i), Color.green(i), Color.blue(i));
        } catch (IllegalArgumentException unused) {
            return iArgb;
        }
    }

    public static String getUrlFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return String.format("data:image/bmp;base64,%s", Base64.encodeToString(byteArrayOutputStream.toByteArray(), 0));
    }
}
