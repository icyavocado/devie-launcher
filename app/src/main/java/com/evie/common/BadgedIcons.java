package com.evie.common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSources;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.voxel.simplesearchlauncher.utils.BitmapUtil;

/* JADX INFO: loaded from: classes.dex */
public class BadgedIcons {
    private static final Paint SHORTCUT_BASE_ICON_PAINT = new Paint(7);

    public static String getBadgedIconUrl(String str, String str2, boolean z, int i) {
        int iRound = Math.round(i * 0.4f);
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(i, i, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapCreateBitmap);
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>> dataSourceFetchDecodedImage = imagePipeline.fetchDecodedImage(ImageRequest.fromUri(str), null);
        try {
            try {
                CloseableReference closeableReference = (CloseableReference) DataSources.waitForFinalResult(dataSourceFetchDecodedImage);
                if (closeableReference != null) {
                    CloseableImage closeableImage = (CloseableImage) closeableReference.get();
                    if (!(closeableImage instanceof CloseableBitmap)) {
                        dataSourceFetchDecodedImage.close();
                        return null;
                    }
                    Bitmap underlyingBitmap = ((CloseableBitmap) closeableImage).getUnderlyingBitmap();
                    Paint paint = new Paint(SHORTCUT_BASE_ICON_PAINT);
                    Rect rect = new Rect(0, 0, underlyingBitmap.getWidth(), underlyingBitmap.getHeight());
                    if (z) {
                        canvas.drawARGB(0, 0, 0, 0);
                        paint.setColor(-12434878);
                        canvas.drawCircle(i / 2, i / 2, i / 2, paint);
                        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                    }
                    canvas.drawBitmap(underlyingBitmap, rect, new Rect(0, 0, i, i), paint);
                }
                dataSourceFetchDecodedImage.close();
                DataSource<CloseableReference<CloseableImage>> dataSourceFetchDecodedImage2 = imagePipeline.fetchDecodedImage(ImageRequest.fromUri(str2), null);
                try {
                    CloseableReference closeableReference2 = (CloseableReference) DataSources.waitForFinalResult(dataSourceFetchDecodedImage2);
                    if (closeableReference2 == null) {
                        return null;
                    }
                    CloseableImage closeableImage2 = (CloseableImage) closeableReference2.get();
                    if (closeableImage2 instanceof CloseableBitmap) {
                        Bitmap underlyingBitmap2 = ((CloseableBitmap) closeableImage2).getUnderlyingBitmap();
                        int i2 = i - iRound;
                        canvas.drawBitmap(underlyingBitmap2, new Rect(0, 0, underlyingBitmap2.getWidth(), underlyingBitmap2.getHeight()), new Rect(i2, i2, i, i), (Paint) null);
                    }
                    return BitmapUtil.getUrlFromBitmap(bitmapCreateBitmap);
                } catch (Throwable th) {
                    th.printStackTrace();
                    return null;
                } finally {
                    dataSourceFetchDecodedImage2.close();
                }
            } catch (Throwable th2) {
                dataSourceFetchDecodedImage.close();
                throw th2;
            }
        } catch (Throwable th3) {
            th3.printStackTrace();
            dataSourceFetchDecodedImage.close();
            return null;
        }
    }
}
