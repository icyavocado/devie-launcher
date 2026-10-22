package com.voxel.launcher3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import com.android.gallery3d.common.BitmapCropTask;
import com.android.photos.views.TiledImageRenderer;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
public class SavedWallpaperImages extends BaseAdapter implements ListAdapter {
    private static String TAG = "SavedWallpaperImages";
    Context mContext;
    private ImageDb mDb;
    ArrayList<SavedWallpaperTile> mImages;
    LayoutInflater mLayoutInflater;

    static class ImageDb extends SQLiteOpenHelper {
        Context mContext;

        public ImageDb(Context context) {
            super(context, context.getDatabasePath("saved_wallpaper_images.db").getPath(), (SQLiteDatabase.CursorFactory) null, 2);
            this.mContext = context;
        }

        public static void moveFromCacheDirectoryIfNecessary(Context context) {
            File file = new File(context.getCacheDir(), "saved_wallpaper_images.db");
            File databasePath = context.getDatabasePath("saved_wallpaper_images.db");
            if (file.exists()) {
                file.renameTo(databasePath);
            }
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS saved_wallpaper_images (id INTEGER NOT NULL, image_thumbnail TEXT NOT NULL, image TEXT NOT NULL, extras TEXT, PRIMARY KEY (id ASC) );");
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            if (i == 1) {
                sQLiteDatabase.execSQL("ALTER TABLE saved_wallpaper_images ADD COLUMN extras TEXT;");
            } else if (i != i2) {
                sQLiteDatabase.execSQL("DELETE FROM saved_wallpaper_images");
                onCreate(sQLiteDatabase);
            }
        }
    }

    public static class SavedWallpaperTile extends WallpaperPickerActivity.FileWallpaperInfo {
        private int mDbId;
        private Float[] mExtras;

        public SavedWallpaperTile(int i, File file, Drawable drawable, Float[] fArr) {
            super(file, drawable);
            this.mDbId = i;
            this.mExtras = (fArr == null || fArr.length != 3) ? null : fArr;
        }

        @Override // com.voxel.launcher3.WallpaperPickerActivity.FileWallpaperInfo
        protected WallpaperCropActivity.CropViewScaleAndOffsetProvider getCropViewScaleAndOffsetProvider() {
            if (this.mExtras != null) {
                return new WallpaperCropActivity.CropViewScaleAndOffsetProvider() { // from class: com.voxel.launcher3.SavedWallpaperImages.SavedWallpaperTile.1
                    @Override // com.voxel.launcher3.WallpaperCropActivity.CropViewScaleAndOffsetProvider
                    public void updateCropView(WallpaperCropActivity wallpaperCropActivity, TiledImageRenderer.TileSource tileSource) {
                        wallpaperCropActivity.mCropView.setScaleAndCenter(SavedWallpaperTile.this.mExtras[0].floatValue(), SavedWallpaperTile.this.mExtras[1].floatValue(), SavedWallpaperTile.this.mExtras[2].floatValue());
                    }
                };
            }
            return null;
        }

        @Override // com.voxel.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onDelete(WallpaperPickerActivity wallpaperPickerActivity) {
            wallpaperPickerActivity.getSavedImages().deleteImage(this.mDbId);
        }

        @Override // com.voxel.launcher3.WallpaperPickerActivity.FileWallpaperInfo, com.voxel.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onSave(WallpaperPickerActivity wallpaperPickerActivity) {
            if (this.mExtras == null) {
                super.onSave(wallpaperPickerActivity);
            } else {
                wallpaperPickerActivity.cropImageAndSetWallpaper(Uri.fromFile(this.mFile), (BitmapCropTask.OnBitmapCroppedHandler) null, true, wallpaperPickerActivity.getWallpaperParallaxOffset() == 0.0f);
            }
        }
    }

    public SavedWallpaperImages(Context context) {
        ImageDb.moveFromCacheDirectoryIfNecessary(context);
        this.mDb = new ImageDb(context);
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    private Pair<String, String> getImageFilenames(int i) {
        Cursor cursorQuery = this.mDb.getReadableDatabase().query("saved_wallpaper_images", new String[]{"image_thumbnail", "image"}, "id = ?", new String[]{Integer.toString(i)}, null, null, null, null);
        if (cursorQuery.getCount() <= 0) {
            return null;
        }
        cursorQuery.moveToFirst();
        String string = cursorQuery.getString(0);
        String string2 = cursorQuery.getString(1);
        cursorQuery.close();
        return new Pair<>(string, string2);
    }

    private void writeImage(Bitmap bitmap, InputStream inputStream, Float[] fArr) throws IOException {
        File fileCreateTempFile = File.createTempFile("wallpaper", "", this.mContext.getFilesDir());
        FileOutputStream fileOutputStreamOpenFileOutput = this.mContext.openFileOutput(fileCreateTempFile.getName(), 0);
        byte[] bArr = new byte[4096];
        while (true) {
            int i = inputStream.read(bArr);
            if (i <= 0) {
                break;
            } else {
                fileOutputStreamOpenFileOutput.write(bArr, 0, i);
            }
        }
        fileOutputStreamOpenFileOutput.close();
        inputStream.close();
        File fileCreateTempFile2 = File.createTempFile("wallpaperthumb", "", this.mContext.getFilesDir());
        FileOutputStream fileOutputStreamOpenFileOutput2 = this.mContext.openFileOutput(fileCreateTempFile2.getName(), 0);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fileOutputStreamOpenFileOutput2);
        fileOutputStreamOpenFileOutput2.close();
        SQLiteDatabase writableDatabase = this.mDb.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("image_thumbnail", fileCreateTempFile2.getName());
        contentValues.put("image", fileCreateTempFile.getName());
        if (fArr != null) {
            contentValues.put("extras", TextUtils.join(",", fArr));
        }
        writableDatabase.insert("saved_wallpaper_images", null, contentValues);
    }

    public void deleteImage(int i) {
        Pair<String, String> imageFilenames = getImageFilenames(i);
        new File(this.mContext.getFilesDir(), (String) imageFilenames.first).delete();
        new File(this.mContext.getFilesDir(), (String) imageFilenames.second).delete();
        this.mDb.getWritableDatabase().delete("saved_wallpaper_images", "id = ?", new String[]{Integer.toString(i)});
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.mImages.size();
    }

    @Override // android.widget.Adapter
    public SavedWallpaperTile getItem(int i) {
        return this.mImages.get(i);
    }

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        return i;
    }

    @Override // android.widget.Adapter
    public View getView(int i, View view, ViewGroup viewGroup) {
        Drawable drawable = this.mImages.get(i).mThumb;
        if (drawable == null) {
            Log.e(TAG, "Error decoding thumbnail for wallpaper #" + i);
        }
        return WallpaperPickerActivity.createImageTileView(this.mLayoutInflater, view, viewGroup, drawable);
    }

    public void loadThumbnailsAndImageIdList() {
        this.mImages = new ArrayList<>();
        Cursor cursorQuery = this.mDb.getReadableDatabase().query("saved_wallpaper_images", new String[]{"id", "image_thumbnail", "image", "extras"}, null, null, null, null, "id DESC", null);
        while (cursorQuery.moveToNext()) {
            Bitmap bitmapDecodeFile = BitmapFactory.decodeFile(new File(this.mContext.getFilesDir(), cursorQuery.getString(1)).getAbsolutePath());
            if (bitmapDecodeFile != null) {
                String string = cursorQuery.getString(3);
                Float[] fArr = null;
                if (string != null) {
                    String[] strArrSplit = string.split(",");
                    Float[] fArr2 = new Float[strArrSplit.length];
                    for (int i = 0; i < strArrSplit.length; i++) {
                        try {
                            fArr2[i] = Float.valueOf(Float.parseFloat(strArrSplit[i]));
                        } catch (Exception unused) {
                        }
                    }
                    fArr = fArr2;
                }
                this.mImages.add(new SavedWallpaperTile(cursorQuery.getInt(0), new File(this.mContext.getFilesDir(), cursorQuery.getString(2)), new BitmapDrawable(bitmapDecodeFile), fArr));
            }
        }
        cursorQuery.close();
    }

    public void writeImage(Bitmap bitmap, Uri uri, Float[] fArr) {
        try {
            writeImage(bitmap, this.mContext.getContentResolver().openInputStream(uri), fArr);
        } catch (IOException e) {
            Log.e(TAG, "Failed writing images to storage " + e);
        }
    }

    public void writeImage(Bitmap bitmap, byte[] bArr) {
        try {
            writeImage(bitmap, new ByteArrayInputStream(bArr), (Float[]) null);
        } catch (IOException e) {
            Log.e(TAG, "Failed writing images to storage " + e);
        }
    }
}
