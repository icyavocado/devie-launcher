package com.voxel.launcher3;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class ThirdPartyWallpaperPickerListAdapter extends BaseAdapter implements ListAdapter {
    private final int mIconSize;
    private final LayoutInflater mInflater;
    private final PackageManager mPackageManager;
    private List<ThirdPartyWallpaperTile> mThirdPartyWallpaperPickers = new ArrayList();

    public static class ThirdPartyWallpaperTile extends WallpaperPickerActivity.WallpaperTileInfo {
        ResolveInfo mResolveInfo;

        public ThirdPartyWallpaperTile(ResolveInfo resolveInfo) {
            this.mResolveInfo = resolveInfo;
        }

        @Override // com.voxel.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onClick(WallpaperPickerActivity wallpaperPickerActivity) {
            ComponentName componentName = new ComponentName(this.mResolveInfo.activityInfo.packageName, this.mResolveInfo.activityInfo.name);
            Intent intent = new Intent("android.intent.action.SET_WALLPAPER");
            intent.setComponent(componentName).putExtra("com.voxel.launcher3.WALLPAPER_OFFSET", wallpaperPickerActivity.getWallpaperParallaxOffset());
            wallpaperPickerActivity.startActivityForResultSafely(intent, 6);
        }
    }

    public ThirdPartyWallpaperPickerListAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        this.mPackageManager = context.getPackageManager();
        this.mIconSize = context.getResources().getDimensionPixelSize(R.dimen.wallpaperItemIconSize);
        PackageManager packageManager = this.mPackageManager;
        List<ResolveInfo> listQueryIntentActivities = packageManager.queryIntentActivities(new Intent("android.intent.action.SET_WALLPAPER"), 0);
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        List<ResolveInfo> listQueryIntentActivities2 = packageManager.queryIntentActivities(intent, 0);
        ComponentName[] componentNameArr = new ComponentName[listQueryIntentActivities2.size()];
        for (int i = 0; i < listQueryIntentActivities2.size(); i++) {
            ActivityInfo activityInfo = listQueryIntentActivities2.get(i).activityInfo;
            componentNameArr[i] = new ComponentName(activityInfo.packageName, activityInfo.name);
        }
        for (ResolveInfo resolveInfo : listQueryIntentActivities) {
            String packageName = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name).getPackageName();
            if (!packageName.equals(context.getPackageName()) && !packageName.equals("com.android.launcher") && !packageName.equals("com.android.wallpaper.livepicker")) {
                Iterator<ResolveInfo> it = listQueryIntentActivities2.iterator();
                do {
                    if (!it.hasNext()) {
                        this.mThirdPartyWallpaperPickers.add(new ThirdPartyWallpaperTile(resolveInfo));
                        break;
                    }
                } while (!packageName.equals(it.next().activityInfo.packageName));
            }
        }
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.mThirdPartyWallpaperPickers.size();
    }

    @Override // android.widget.Adapter
    public ThirdPartyWallpaperTile getItem(int i) {
        return this.mThirdPartyWallpaperPickers.get(i);
    }

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        return i;
    }

    @Override // android.widget.Adapter
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = this.mInflater.inflate(R.layout.wallpaper_picker_third_party_item, viewGroup, false);
        }
        ResolveInfo resolveInfo = this.mThirdPartyWallpaperPickers.get(i).mResolveInfo;
        TextView textView = (TextView) view.findViewById(R.id.wallpaper_item_label);
        textView.setText(resolveInfo.loadLabel(this.mPackageManager));
        Drawable drawableLoadIcon = resolveInfo.loadIcon(this.mPackageManager);
        int i2 = this.mIconSize;
        drawableLoadIcon.setBounds(new Rect(0, 0, i2, i2));
        textView.setCompoundDrawables(null, drawableLoadIcon, null, null);
        return view;
    }
}
