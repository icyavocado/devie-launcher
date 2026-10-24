package com.evie.common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import com.ovacoda.devie.images.IconLoader;
import com.voxel.simplesearchlauncher.utils.BitmapUtil;

/* JADX INFO: loaded from: classes.dex */
public class BadgedIcons {
    private static final Paint SHORTCUT_BASE_ICON_PAINT = new Paint(7);

    /* REWORK(fresco): the base and badge bitmaps now resolve via the in-process
       IconLoader (callers are background threads, so blocking is fine). */
    public static String getBadgedIconUrl(String str, String str2, boolean z, int i) {
        Bitmap bitmapDecode = IconLoader.getInstance().loadSync(str);
        if (bitmapDecode == null) {
            return null;
        }
        Bitmap bitmapDecode2 = IconLoader.getInstance().loadSync(str2);
        if (bitmapDecode2 == null) {
            return null;
        }
        int iRound = Math.round(i * 0.4f);
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(i, i, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapCreateBitmap);
        Paint paint = new Paint(SHORTCUT_BASE_ICON_PAINT);
        Rect rect = new Rect(0, 0, bitmapDecode.getWidth(), bitmapDecode.getHeight());
        if (z) {
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(-12434878);
            canvas.drawCircle(i / 2, i / 2, i / 2, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        }
        canvas.drawBitmap(bitmapDecode, rect, new Rect(0, 0, i, i), paint);
        int i2 = i - iRound;
        canvas.drawBitmap(bitmapDecode2, new Rect(0, 0, bitmapDecode2.getWidth(), bitmapDecode2.getHeight()), new Rect(i2, i2, i, i), (Paint) null);
        return BitmapUtil.getUrlFromBitmap(bitmapCreateBitmap);
    }
}
