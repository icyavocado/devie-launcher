package com.voxel.launcher3;

import android.app.WallpaperInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.ovacoda.devie.R;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

/* JADX INFO: loaded from: classes.dex */
public class LiveWallpaperListAdapter extends BaseAdapter implements ListAdapter {
    private final LayoutInflater mInflater;
    private final PackageManager mPackageManager;
    List<LiveWallpaperTile> mWallpapers;

    private class LiveWallpaperEnumerator extends AsyncTask<List<ResolveInfo>, LiveWallpaperTile, Void> {
        private Context mContext;
        private int mWallpaperPosition = 0;

        public LiveWallpaperEnumerator(Context context) {
            this.mContext = context;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Void doInBackground(List<ResolveInfo>... listArr) {
            final PackageManager packageManager = this.mContext.getPackageManager();
            List<ResolveInfo> list = listArr[0];
            Collections.sort(list, new Comparator<ResolveInfo>() { // from class: com.voxel.launcher3.LiveWallpaperListAdapter.LiveWallpaperEnumerator.1
                final Collator mCollator = Collator.getInstance();

                @Override // java.util.Comparator
                public int compare(ResolveInfo resolveInfo, ResolveInfo resolveInfo2) {
                    return this.mCollator.compare(resolveInfo.loadLabel(packageManager), resolveInfo2.loadLabel(packageManager));
                }
            });
            for (ResolveInfo resolveInfo : list) {
                try {
                    WallpaperInfo wallpaperInfo = new WallpaperInfo(this.mContext, resolveInfo);
                    Drawable drawableLoadThumbnail = wallpaperInfo.loadThumbnail(packageManager);
                    Intent intent = new Intent("android.service.wallpaper.WallpaperService");
                    intent.setClassName(wallpaperInfo.getPackageName(), wallpaperInfo.getServiceName());
                    publishProgress(new LiveWallpaperTile(drawableLoadThumbnail, wallpaperInfo, intent));
                } catch (IOException e) {
                    Log.w("LiveWallpaperListAdapter", "Skipping wallpaper " + resolveInfo.serviceInfo, e);
                } catch (XmlPullParserException e2) {
                    Log.w("LiveWallpaperListAdapter", "Skipping wallpaper " + resolveInfo.serviceInfo, e2);
                }
            }
            publishProgress((LiveWallpaperTile) null);
            return null;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onProgressUpdate(LiveWallpaperTile... liveWallpaperTileArr) {
            for (LiveWallpaperTile liveWallpaperTile : liveWallpaperTileArr) {
                if (liveWallpaperTile == null) {
                    LiveWallpaperListAdapter.this.notifyDataSetChanged();
                    return;
                }
                if (liveWallpaperTile.mThumbnail != null) {
                    liveWallpaperTile.mThumbnail.setDither(true);
                }
                if (this.mWallpaperPosition < LiveWallpaperListAdapter.this.mWallpapers.size()) {
                    LiveWallpaperListAdapter.this.mWallpapers.set(this.mWallpaperPosition, liveWallpaperTile);
                } else {
                    LiveWallpaperListAdapter.this.mWallpapers.add(liveWallpaperTile);
                }
                this.mWallpaperPosition++;
            }
        }
    }

    public static class LiveWallpaperTile extends WallpaperPickerActivity.WallpaperTileInfo {
        WallpaperInfo mInfo;
        Drawable mThumbnail;

        public LiveWallpaperTile(Drawable drawable, WallpaperInfo wallpaperInfo, Intent intent) {
            this.mThumbnail = drawable;
            this.mInfo = wallpaperInfo;
        }

        @Override // com.voxel.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onClick(WallpaperPickerActivity wallpaperPickerActivity) {
            Intent intent = new Intent("android.service.wallpaper.CHANGE_LIVE_WALLPAPER");
            intent.putExtra("android.service.wallpaper.extra.LIVE_WALLPAPER_COMPONENT", this.mInfo.getComponent());
            wallpaperPickerActivity.startActivityForResultSafely(intent, 6);
        }
    }

    public LiveWallpaperListAdapter(Context context) {
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.mPackageManager = context.getPackageManager();
        List<ResolveInfo> listQueryIntentServices = this.mPackageManager.queryIntentServices(new Intent("android.service.wallpaper.WallpaperService"), 128);
        this.mWallpapers = new ArrayList();
        new LiveWallpaperEnumerator(context).execute(listQueryIntentServices);
    }

    @Override // android.widget.Adapter
    public int getCount() {
        List<LiveWallpaperTile> list = this.mWallpapers;
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    @Override // android.widget.Adapter
    public LiveWallpaperTile getItem(int i) {
        return this.mWallpapers.get(i);
    }

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        return i;
    }

    @Override // android.widget.Adapter
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = this.mInflater.inflate(R.layout.wallpaper_picker_live_wallpaper_item, viewGroup, false);
        }
        LiveWallpaperTile liveWallpaperTile = this.mWallpapers.get(i);
        liveWallpaperTile.setView(view);
        ImageView imageView = (ImageView) view.findViewById(R.id.wallpaper_image);
        ImageView imageView2 = (ImageView) view.findViewById(R.id.wallpaper_icon);
        if (liveWallpaperTile.mThumbnail != null) {
            imageView.setImageDrawable(liveWallpaperTile.mThumbnail);
            imageView2.setVisibility(8);
        } else {
            imageView2.setImageDrawable(liveWallpaperTile.mInfo.loadIcon(this.mPackageManager));
            imageView2.setVisibility(0);
        }
        ((TextView) view.findViewById(R.id.wallpaper_item_label)).setText(liveWallpaperTile.mInfo.loadLabel(this.mPackageManager));
        return view;
    }
}
