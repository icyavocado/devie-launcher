package com.voxel.launcher3;

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.AsyncTask;
import com.ovacoda.devie.R;
import java.io.IOException;
import java.io.InputStream;

/* JADX INFO: loaded from: classes.dex */
public class NycWallpaperUtils {
    public static void clear(Context context, int i) throws IOException {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        if (Utilities.ATLEAST_N) {
            wallpaperManager.clear(i);
        } else {
            wallpaperManager.clear();
        }
    }

    public static void executeCropTaskAfterPrompt(Context context, final AsyncTask<Integer, ?, ?> asyncTask, DialogInterface.OnCancelListener onCancelListener) {
        if (Utilities.ATLEAST_N) {
            new AlertDialog.Builder(context).setTitle(R.string.wallpaper_instructions).setItems(R.array.which_wallpaper_options, new DialogInterface.OnClickListener() { // from class: com.voxel.launcher3.-$$Lambda$NycWallpaperUtils$nbV3b3PRoQrw_7bT4CCzHiWZgcE
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    NycWallpaperUtils.lambda$executeCropTaskAfterPrompt$0(asyncTask, dialogInterface, i);
                }
            }).setOnCancelListener(onCancelListener).show();
        } else {
            asyncTask.execute(1);
        }
    }

    static /* synthetic */ void lambda$executeCropTaskAfterPrompt$0(AsyncTask<Integer, ?, ?> asyncTask, DialogInterface dialogInterface, int i) {
        int i2;
        if (i == 0) {
            i2 = 1;
        } else {
            i2 = i == 1 ? 2 : 3;
        }
        /* REWORK: the param must stay generic — donor dex builds an Integer[] here
           (new-array [Ljava/lang/Integer;); a raw AsyncTask receiver made javac
           pass an Object[] and doInBackground(Integer...) threw ClassCastException
           in the wallpaper_chooser process (crash on "Set wallpaper"/"Crop"). */
        asyncTask.execute(Integer.valueOf(i2));
    }

    public static void setStream(Context context, InputStream inputStream, Rect rect, boolean z, int i) throws IOException {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        if (Utilities.ATLEAST_N) {
            wallpaperManager.setStream(inputStream, rect, z, i);
        } else {
            wallpaperManager.setStream(inputStream);
        }
    }
}
