package com.voxel.simplesearchlauncher.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class LaunchUtil {
    public static void startActivitySafely(Context context, Intent intent) {
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException unused) {
            Toast.makeText(context, R.string.activity_not_found, 0).show();
        } catch (SecurityException unused2) {
            Toast.makeText(context, R.string.msg_no_generic_permission, 0).show();
        } catch (Exception unused3) {
        }
    }
}
