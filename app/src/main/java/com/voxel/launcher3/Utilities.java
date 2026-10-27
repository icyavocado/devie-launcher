package com.voxel.launcher3;

import timber.log.Timber;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.os.Process;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.voxel.launcher3.graphics.ShadowGenerator;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.voxel.simplesearchlauncher.utils.IconNormalizer;
import com.voxel.simplesearchlauncher.utils.NamedThreadFactory;
import com.ovacoda.devie.R;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/* JADX INFO: loaded from: classes.dex */
public final class Utilities {
    public static final boolean ATLEAST_JB_MR1;
    public static final boolean ATLEAST_JB_MR2;
    public static final boolean ATLEAST_KITKAT;
    public static final boolean ATLEAST_LOLLIPOP;
    public static final boolean ATLEAST_LOLLIPOP_MR1;
    public static final boolean ATLEAST_MARSHMALLOW;
    public static final boolean ATLEAST_N;
    private static final int CORE_POOL_SIZE;
    private static final int CPU_COUNT;
    private static final int MAXIMUM_POOL_SIZE;
    public static final Executor THREAD_POOL_EXECUTOR;
    static int sColorIndex = 0;
    static int[] sColors = null;
    public static boolean sForceEnableRotation = false;
    private static int sIconHeight = -1;
    public static int sIconTextureHeight = -1;
    public static int sIconTextureWidth = -1;
    private static int sIconWidth = -1;
    static int[] sLoc0;
    static int[] sLoc1;
    private static final Rect sOldBounds = new Rect();
    private static final Canvas sCanvas = new Canvas();
    private static final Pattern sTrimPattern = Pattern.compile("^[\\s|\\p{javaSpaceChar}]*(.*)[\\s|\\p{javaSpaceChar}]*$");

    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(4, 2));
        sColors = new int[]{-65536, -16711936, -16776961};
        sColorIndex = 0;
        sLoc0 = new int[2];
        sLoc1 = new int[2];
        ATLEAST_N = Build.VERSION.SDK_INT >= 24;
        ATLEAST_MARSHMALLOW = Build.VERSION.SDK_INT >= 23;
        ATLEAST_LOLLIPOP_MR1 = Build.VERSION.SDK_INT >= 22;
        ATLEAST_LOLLIPOP = Build.VERSION.SDK_INT >= 21;
        ATLEAST_KITKAT = Build.VERSION.SDK_INT >= 19;
        ATLEAST_JB_MR1 = Build.VERSION.SDK_INT >= 17;
        ATLEAST_JB_MR2 = Build.VERSION.SDK_INT >= 18;
        CPU_COUNT = Runtime.getRuntime().availableProcessors();
        int i = CPU_COUNT;
        CORE_POOL_SIZE = i + 1;
        MAXIMUM_POOL_SIZE = (i * 2) + 1;
        THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue(), new NamedThreadFactory(Utilities.class.getSimpleName()));
        sForceEnableRotation = isPropertyEnabled("launcher_force_rotate");
    }

    public static Bitmap addShadowToIcon(Bitmap bitmap) {
        return ShadowGenerator.getInstance().recreateIcon(bitmap);
    }

    public static void assertWorkerThread() {
        if (LauncherAppState.isDogfoodBuild() && LauncherModel.sWorkerThread.getThreadId() != Process.myTid()) {
            throw new IllegalStateException();
        }
    }

    @TargetApi(21)
    public static Bitmap badgeWithBitmap(Bitmap bitmap, Bitmap bitmap2, Context context) {
        if (bitmap2 == null) {
            return bitmap;
        }
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(R.dimen.profile_badge_size);
        synchronized (sCanvas) {
            sCanvas.setBitmap(bitmap);
            sCanvas.drawBitmap(bitmap2, new Rect(0, 0, bitmap2.getWidth(), bitmap2.getHeight()), new Rect(bitmap.getWidth() - dimensionPixelSize, bitmap.getHeight() - dimensionPixelSize, bitmap.getWidth(), bitmap.getHeight()), new Paint(2));
            sCanvas.setBitmap(null);
        }
        return bitmap;
    }

    public static Bitmap createIconBitmap(Bitmap bitmap, Context context) {
        synchronized (sCanvas) {
            if (sIconWidth == -1) {
                initStatics(context);
            }
        }
        int i = sIconTextureWidth;
        int i2 = sIconTextureHeight;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width <= i || height <= i2) {
            return (width == i && height == i2) ? bitmap : createIconBitmap(new BitmapDrawable(context.getResources(), bitmap), context);
        }
        return Bitmap.createScaledBitmap(bitmap, i, i2, true);
    }

    public static Bitmap createIconBitmap(Drawable drawable, Context context) {
        return createIconBitmap(drawable, context, SettingsStorage.getInstance().getIconNormalizationSetting() ? IconNormalizer.getInstance().getScale(drawable, null) : 1.0f);
    }

    public static Bitmap createIconBitmap(Drawable drawable, Context context, float f) {
        Bitmap bitmapCreateBitmap;
        synchronized (sCanvas) {
            if (sIconWidth == -1) {
                initStatics(context);
            }
            int i = sIconWidth;
            int i2 = sIconHeight;
            if (drawable instanceof PaintDrawable) {
                PaintDrawable paintDrawable = (PaintDrawable) drawable;
                paintDrawable.setIntrinsicWidth(i);
                paintDrawable.setIntrinsicHeight(i2);
            } else if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                if (bitmapDrawable.getBitmap().getDensity() == 0) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int intrinsicWidth = drawable.getIntrinsicWidth();
            int intrinsicHeight = drawable.getIntrinsicHeight();
            if (intrinsicWidth > 0 && intrinsicHeight > 0) {
                float f2 = (float) intrinsicWidth / intrinsicHeight;
                if (intrinsicWidth > intrinsicHeight) {
                    i2 = (int) (i / f2);
                } else if (intrinsicHeight > intrinsicWidth) {
                    i = (int) (i2 * f2);
                }
            }
            int i3 = sIconTextureWidth;
            int i4 = sIconTextureHeight;
            bitmapCreateBitmap = Bitmap.createBitmap(i3, i4, Bitmap.Config.ARGB_8888);
            Canvas canvas = sCanvas;
            canvas.setBitmap(bitmapCreateBitmap);
            int i5 = (i3 - i) / 2;
            int i6 = (i4 - i2) / 2;
            sOldBounds.set(drawable.getBounds());
            drawable.setBounds(i5, i6, i + i5, i2 + i6);
            canvas.save();
            canvas.scale(f, f, i3 / 2, i4 / 2);
            drawable.draw(canvas);
            canvas.restore();
            drawable.setBounds(sOldBounds);
            canvas.setBitmap(null);
        }
        return bitmapCreateBitmap;
    }

    public static Bitmap createIconBitmap(String str, String str2, Context context) {
        try {
            Resources resourcesForApplication = context.getPackageManager().getResourcesForApplication(str);
            if (resourcesForApplication != null) {
                return createIconBitmap(resourcesForApplication.getDrawableForDensity(resourcesForApplication.getIdentifier(str2, null, null), LauncherAppState.getInstance().getIconCache().getFullResIconDpi()), context);
            }
        } catch (Exception unused) {
        }
        return null;
    }

    public static FastBitmapDrawable createIconDrawable(Bitmap bitmap) {
        FastBitmapDrawable fastBitmapDrawable = new FastBitmapDrawable(bitmap);
        fastBitmapDrawable.setFilterBitmap(true);
        resizeIconDrawable(fastBitmapDrawable);
        return fastBitmapDrawable;
    }

    public static int findDominantColorByHue(Bitmap bitmap, int i) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int iSqrt = (int) Math.sqrt((height * width) / i);
        char c = 1;
        if (iSqrt < 1) {
            iSqrt = 1;
        }
        float[] fArr = new float[3];
        float[] fArr2 = new float[360];
        char c2 = 0;
        int i2 = 0;
        int i3 = -1;
        float f = -1.0f;
        while (i2 < height) {
            float f2 = f;
            int i4 = i3;
            for (int i5 = 0; i5 < width; i5 += iSqrt) {
                int pixel = bitmap.getPixel(i5, i2);
                if (((pixel >> 24) & 255) >= 128) {
                    Color.colorToHSV(pixel | (-16777216), fArr);
                    int i6 = (int) fArr[0];
                    if (i6 >= 0 && i6 < fArr2.length) {
                        fArr2[i6] = fArr2[i6] + (fArr[1] * fArr[2]);
                        if (fArr2[i6] > f2) {
                            f2 = fArr2[i6];
                            i4 = i6;
                        }
                    }
                }
            }
            i2 += iSqrt;
            i3 = i4;
            f = f2;
        }
        SparseArray sparseArray = new SparseArray();
        int i7 = 0;
        int i8 = -16777216;
        float f3 = -1.0f;
        while (i7 < height) {
            float f4 = f3;
            int i9 = i8;
            int i10 = 0;
            while (i10 < width) {
                int pixel2 = bitmap.getPixel(i10, i7) | (-16777216);
                Color.colorToHSV(pixel2, fArr);
                if (((int) fArr[c2]) == i3) {
                    float f5 = fArr[c];
                    float f6 = fArr[2];
                    int i11 = ((int) (f5 * 100.0f)) + ((int) (f6 * 10000.0f));
                    float fFloatValue = f5 * f6;
                    Float f7 = (Float) sparseArray.get(i11);
                    if (f7 != null) {
                        fFloatValue = f7.floatValue() + fFloatValue;
                    }
                    sparseArray.put(i11, Float.valueOf(fFloatValue));
                    if (fFloatValue > f4) {
                        i9 = pixel2;
                        f4 = fFloatValue;
                    }
                }
                i10 += iSqrt;
                c = 1;
                c2 = 0;
            }
            i7 += iSqrt;
            i8 = i9;
            f3 = f4;
            c = 1;
            c2 = 0;
        }
        return i8;
    }

    static Pair<String, Resources> findSystemApk(String str, PackageManager packageManager) {
        Iterator<ResolveInfo> it = packageManager.queryBroadcastReceivers(new Intent(str), 0).iterator();
        while (it.hasNext()) {
            ResolveInfo next = it.next();
            if (next.activityInfo != null && (next.activityInfo.applicationInfo.flags & 1) != 0) {
                String str2 = next.activityInfo.packageName;
                try {
                    return Pair.create(str2, packageManager.getResourcesForApplication(str2));
                } catch (PackageManager.NameNotFoundException unused) {
                    Log.w("Launcher.Utilities", "Failed to find resources for " + str2);
                }
            }
        }
        return null;
    }

    public static byte[] flattenBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(bitmap.getWidth() * bitmap.getHeight() * 4);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException unused) {
            Log.w("Favorite", "Could not write icon");
            return null;
        }
    }

    public static float getDescendantCoordRelativeToParent(View view, View view2, int[] iArr, boolean z) {
        ArrayList arrayList = new ArrayList();
        float[] fArr = {iArr[0], iArr[1]};
        for (View view3 = view; view3 != view2 && view3 != null; view3 = (View) view3.getParent()) {
            try {
                arrayList.add(view3);
            } catch (ClassCastException e) {
                StringBuilder sb = new StringBuilder();
                sb.append("Descendant: ");
                sb.append(view);
                sb.append("\n");
                sb.append("AncestorChain:\n");
                Iterator it = arrayList.iterator();
                while (it.hasNext()) {
                    sb.append(((View) it.next()).toString());
                    sb.append("\n");
                }
                sb.append("root:");
                sb.append(view2);
                Log.e("Launcher.Utilities", sb.toString(), e);
                Timber.e(new RuntimeException(sb.toString(), e));
            }
        }
        arrayList.add(view2);
        int size = arrayList.size();
        float scaleX = 1.0f;
        for (int i = 0; i < size; i++) {
            View view4 = (View) arrayList.get(i);
            if (view4 != view || z) {
                fArr[0] = fArr[0] - view4.getScrollX();
                fArr[1] = fArr[1] - view4.getScrollY();
            }
            view4.getMatrix().mapPoints(fArr);
            fArr[0] = fArr[0] + view4.getLeft();
            fArr[1] = fArr[1] + view4.getTop();
            scaleX *= view4.getScaleX();
        }
        iArr[0] = Math.round(fArr[0]);
        iArr[1] = Math.round(fArr[1]);
        return scaleX;
    }

    public static int getIconSize(Context context) {
        int i;
        synchronized (sCanvas) {
            if (sIconWidth == -1) {
                initStatics(context);
            }
            i = sIconWidth;
        }
        return i;
    }

    private static void initStatics(Context context) {
        int dimension = (int) context.getResources().getDimension(R.dimen.app_icon_size);
        sIconHeight = dimension;
        sIconWidth = dimension;
        int i = sIconWidth;
        sIconTextureHeight = i;
        sIconTextureWidth = i;
    }

    public static boolean isAtLeast81() {
        return Build.VERSION.SDK_INT >= 27;
    }

    /* REWORK: API 33 (T) — legacy storage permission stops granting (scoped storage). */
    public static boolean isAtLeastT() {
        return Build.VERSION.SDK_INT >= 33;
    }

    public static boolean isAtLeastO() {
        return Build.VERSION.SDK_INT >= 26;
    }

    public static boolean isLmpOrAbove() {
        return Build.VERSION.SDK_INT >= 21;
    }

    public static boolean isNycMR1OrAbove() {
        return Build.VERSION.SDK_INT >= 25;
    }

    public static boolean isNycOrAbove() {
        return Build.VERSION.SDK_INT >= 24;
    }

    private static boolean isPropertyEnabled(String str) {
        return Log.isLoggable(str, 2);
    }

    @TargetApi(17)
    public static boolean isRtl(Resources resources) {
        return ATLEAST_JB_MR1 && resources.getConfiguration().getLayoutDirection() == 1;
    }

    static boolean isSystemApp(Context context, Intent intent) {
        String packageName;
        PackageManager packageManager = context.getPackageManager();
        ComponentName component = intent.getComponent();
        if (component == null) {
            ResolveInfo resolveInfoResolveActivity = packageManager.resolveActivity(intent, 65536);
            packageName = (resolveInfoResolveActivity == null || resolveInfoResolveActivity.activityInfo == null) ? null : resolveInfoResolveActivity.activityInfo.packageName;
        } else {
            packageName = component.getPackageName();
        }
        if (packageName == null) {
            return false;
        }
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return (packageInfo == null || packageInfo.applicationInfo == null || (packageInfo.applicationInfo.flags & 1) == 0) ? false : true;
        } catch (PackageManager.NameNotFoundException unused) {
            return false;
        }
    }

    public static float mapCoordInSelfToDescendent(View view, View view2, int[] iArr) {
        ArrayList arrayList = new ArrayList();
        float[] fArr = {iArr[0], iArr[1]};
        while (view != view2) {
            arrayList.add(view);
            view = (View) view.getParent();
        }
        arrayList.add(view2);
        float scaleX = 1.0f;
        Matrix matrix = new Matrix();
        int size = arrayList.size() - 1;
        while (size >= 0) {
            View view3 = (View) arrayList.get(size);
            View view4 = size > 0 ? (View) arrayList.get(size - 1) : null;
            fArr[0] = fArr[0] + view3.getScrollX();
            fArr[1] = fArr[1] + view3.getScrollY();
            if (view4 != null) {
                fArr[0] = fArr[0] - view4.getLeft();
                fArr[1] = fArr[1] - view4.getTop();
                view4.getMatrix().invert(matrix);
                matrix.mapPoints(fArr);
                scaleX *= view4.getScaleX();
            }
            size--;
        }
        iArr[0] = Math.round(fArr[0]);
        iArr[1] = Math.round(fArr[1]);
        return scaleX;
    }

    public static boolean pointInView(View view, float f, float f2, float f3) {
        float f4 = -f3;
        return f >= f4 && f2 >= f4 && f < ((float) view.getWidth()) + f3 && f2 < ((float) view.getHeight()) + f3;
    }

    static Bitmap resampleIconBitmap(Bitmap bitmap, Context context) {
        synchronized (sCanvas) {
            if (sIconWidth == -1) {
                initStatics(context);
            }
            if (bitmap.getWidth() == sIconWidth && bitmap.getHeight() == sIconHeight) {
                return bitmap;
            }
            return createIconBitmap(new BitmapDrawable(context.getResources(), bitmap), context);
        }
    }

    static void resizeIconDrawable(Drawable drawable) {
        drawable.setBounds(0, 0, sIconTextureWidth, sIconTextureHeight);
    }

    public static void scaleRect(Rect rect, float f) {
        if (f != 1.0f) {
            rect.left = (int) ((rect.left * f) + 0.5f);
            rect.top = (int) ((rect.top * f) + 0.5f);
            rect.right = (int) ((rect.right * f) + 0.5f);
            rect.bottom = (int) ((rect.bottom * f) + 0.5f);
        }
    }

    public static void scaleRectAboutCenter(Rect rect, float f) {
        int iCenterX = rect.centerX();
        int iCenterY = rect.centerY();
        rect.offset(-iCenterX, -iCenterY);
        scaleRect(rect, f);
        rect.offset(iCenterX, iCenterY);
    }

    public static void setIconSize(int i) {
        sIconHeight = i;
        sIconWidth = i;
        sIconTextureHeight = i;
        sIconTextureWidth = i;
    }

    @TargetApi(19)
    public static void setupTransparentSystemBarsForLmp(Activity activity) {
        if (isLmpOrAbove()) {
            try {
                try {
                    activity.getWindow().getAttributes().systemUiVisibility |= 1792;
                    activity.getWindow().clearFlags(201326592);
                    activity.getWindow().addFlags(WindowManager.LayoutParams.class.getField("FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS").getInt(null));
                    Method declaredMethod = Window.class.getDeclaredMethod("setStatusBarColor", Integer.TYPE);
                    Method declaredMethod2 = Window.class.getDeclaredMethod("setNavigationBarColor", Integer.TYPE);
                    declaredMethod.invoke(activity.getWindow(), 0);
                    declaredMethod2.invoke(activity.getWindow(), 0);
                } catch (NoSuchFieldException unused) {
                    Log.w("Launcher.Utilities", "NoSuchFieldException while setting up transparent bars");
                } catch (InvocationTargetException unused2) {
                    Log.w("Launcher.Utilities", "InvocationTargetException while setting up transparent bars");
                }
            } catch (IllegalAccessException unused3) {
                Log.w("Launcher.Utilities", "IllegalAccessException while setting up transparent bars");
            } catch (IllegalArgumentException unused4) {
                Log.w("Launcher.Utilities", "IllegalArgumentException while setting up transparent bars");
            } catch (NoSuchMethodException unused5) {
                Log.w("Launcher.Utilities", "NoSuchMethodException while setting up transparent bars");
            }
        }
    }

    public static void startActivityForResultSafely(Activity activity, Intent intent, int i) {
        try {
            activity.startActivityForResult(intent, i);
        } catch (ActivityNotFoundException unused) {
            Toast.makeText(activity, R.string.activity_not_found, 0).show();
        } catch (SecurityException e) {
            Toast.makeText(activity, R.string.activity_not_found, 0).show();
            Log.e("Launcher.Utilities", "Launcher does not have the permission to launch " + intent + ". Make sure to create a MAIN intent-filter for the corresponding activity or use the exported attribute for this activity.", e);
        }
    }

    public static String trim(CharSequence charSequence) {
        if (charSequence == null) {
            return null;
        }
        return sTrimPattern.matcher(charSequence).replaceAll("$1");
    }
}
