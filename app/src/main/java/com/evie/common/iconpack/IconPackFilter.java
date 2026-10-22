package com.evie.common.iconpack;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import com.evie.common.iconpack.shader.IconPackShader;
import com.voxel.simplesearchlauncher.utils.BitmapUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/* JADX INFO: loaded from: classes.dex */
public class IconPackFilter {
    private static final String TAG = "IconPackFilter";
    private final Context mContext;
    private final String mPackageName;
    private final int mVersionCode;
    private int totalIcons;
    private boolean mAppFilterLoaded = false;
    private HashMap<String, String> mAppFilterDrawables = new HashMap<>();
    private HashMap<String, String> mCalendarPrefixes = new HashMap<>();
    private boolean mDrawableListLoaded = false;
    private boolean mDrawableListVerified = false;
    private final List<String> mDrawableList = new ArrayList();
    private final List<Bitmap> mBackImages = new ArrayList();
    private Bitmap mMaskImage = null;
    private Bitmap mFrontImage = null;
    private float mFactor = 1.0f;
    private IconPackShader mShader = null;
    Resources iconPackres = null;

    public IconPackFilter(Context context, String str, int i) {
        this.mContext = context;
        this.mPackageName = str;
        this.mVersionCode = i;
        load();
    }

    private Bitmap generateBitmap(String str, Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        if (this.mBackImages.size() == 0) {
            return bitmap;
        }
        Bitmap bitmap2 = this.mBackImages.get(Math.abs(str.hashCode()) % this.mBackImages.size());
        int width = bitmap2.getWidth();
        int height = bitmap2.getHeight();
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapCreateBitmap);
        canvas.drawBitmap(bitmap2, 0.0f, 0.0f, (Paint) null);
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Rect rect2 = new Rect(0, 0, Math.round(width * this.mFactor), Math.round(height * this.mFactor));
        rect2.offset((width - rect2.width()) / 2, (height - rect2.height()) / 2);
        if (this.mMaskImage != null) {
            Bitmap bitmapCreateBitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas2 = new Canvas(bitmapCreateBitmap2);
            Paint paint = new Paint(1);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            Rect rect3 = new Rect(0, 0, this.mMaskImage.getWidth(), this.mMaskImage.getHeight());
            Rect rect4 = new Rect(0, 0, width, height);
            canvas2.drawBitmap(bitmap, rect, rect2, (Paint) null);
            canvas2.drawBitmap(this.mMaskImage, rect3, rect4, paint);
            canvas.drawBitmap(bitmapCreateBitmap2, 0.0f, 0.0f, (Paint) null);
            paint.setXfermode(null);
        } else {
            canvas.drawBitmap(bitmap, rect, rect2, (Paint) null);
        }
        Bitmap bitmap3 = this.mFrontImage;
        if (bitmap3 != null) {
            if (bitmap3.getWidth() > rect2.width() || this.mFrontImage.getHeight() > rect2.height()) {
                canvas.drawBitmap(this.mFrontImage, new Rect(0, 0, this.mFrontImage.getWidth(), this.mFrontImage.getHeight()), rect2, (Paint) null);
            } else {
                canvas.drawBitmap(this.mFrontImage, 0.0f, 0.0f, (Paint) null);
            }
        }
        IconPackShader iconPackShader = this.mShader;
        return iconPackShader != null ? iconPackShader.processBitmap(bitmapCreateBitmap) : bitmapCreateBitmap;
    }

    private Bitmap loadBitmap(String str) {
        int identifier = this.iconPackres.getIdentifier(str, "drawable", this.mPackageName);
        if (identifier <= 0) {
            return null;
        }
        Drawable drawable = this.iconPackres.getDrawable(identifier);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        return null;
    }

    public List<String> getDrawableList() {
        if (!this.mDrawableListLoaded) {
            synchronized (this.mDrawableList) {
                loadDrawableList();
            }
        }
        return Collections.unmodifiableList(this.mDrawableList);
    }

    public Bitmap getIconForPackage(String str, String str2, Bitmap bitmap, int i) {
        int iIndexOf;
        int iIndexOf2;
        if (!this.mAppFilterLoaded) {
            load();
        }
        if (!this.mAppFilterLoaded) {
            return bitmap;
        }
        String string = "";
        if (str != null) {
            if (str2 == null) {
                PackageManager packageManager = this.mContext.getPackageManager();
                if (packageManager.getLaunchIntentForPackage(str) != null) {
                    string = packageManager.getLaunchIntentForPackage(str).getComponent().toString();
                }
            } else {
                string = new ComponentName(str, str2).toString();
            }
        }
        String str3 = this.mAppFilterDrawables.get(string);
        if (this.mCalendarPrefixes.containsKey(string)) {
            str3 = String.format("%s%d", this.mCalendarPrefixes.get(string), Integer.valueOf(Calendar.getInstance().get(5)));
        }
        Bitmap bitmapLoadBitmap = null;
        if (str3 != null) {
            bitmapLoadBitmap = loadBitmap(str3);
        } else if (string != null && (iIndexOf2 = string.indexOf("}", (iIndexOf = string.indexOf("{") + 1))) > iIndexOf) {
            String strReplace = string.substring(iIndexOf, iIndexOf2).toLowerCase(Locale.getDefault()).replace(".", "_").replace("/", "_");
            if (this.iconPackres.getIdentifier(strReplace, "drawable", this.mPackageName) > 0) {
                bitmapLoadBitmap = loadBitmap(strReplace);
            }
        }
        if (bitmapLoadBitmap == null) {
            bitmapLoadBitmap = generateBitmap(string, bitmap);
        }
        return BitmapUtil.createIconBitmap(bitmapLoadBitmap, this.mContext, i);
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public int getVersionCode() {
        return this.mVersionCode;
    }

    public boolean isCalendarApp(String str, String str2) {
        if (str == null || str2 == null) {
            return false;
        }
        return this.mCalendarPrefixes.containsKey(new ComponentName(str, str2).toString());
    }

    public boolean isDrawableListVerified() {
        return this.mDrawableListVerified;
    }

    public void load() {
        /* REWORK: jadx lost an exception-flow local across catch scopes. */
        Throwable eReflow = null;
        XmlPullParser xmlPullParserNewPullParser;
        Bitmap bitmapLoadBitmap;
        if (this.mAppFilterLoaded) {
            return;
        }
        try {
            try {
                this.iconPackres = this.mContext.getPackageManager().getResourcesForApplication(this.mPackageName);
                int identifier = this.iconPackres.getIdentifier("appfilter", "xml", this.mPackageName);
                if (identifier > 0) {
                    xmlPullParserNewPullParser = this.iconPackres.getXml(identifier);
                } else {
                    try {
                        InputStream inputStreamOpen = this.iconPackres.getAssets().open("appfilter.xml");
                        XmlPullParserFactory xmlPullParserFactoryNewInstance = XmlPullParserFactory.newInstance();
                        xmlPullParserFactoryNewInstance.setNamespaceAware(true);
                        xmlPullParserNewPullParser = xmlPullParserFactoryNewInstance.newPullParser();
                        try {
                            xmlPullParserNewPullParser.setInput(inputStreamOpen, "utf-8");
                        } catch (Exception e) {
                            /* REWORK: setInput throws XmlPullParserException, not IOException */
                            e = e;
                            Log.d(TAG, "No appfilter.xml file", e);
                        }
                    } catch (IOException e2) {
                        eReflow = e2;
                        xmlPullParserNewPullParser = null;
                    }
                }
                if (xmlPullParserNewPullParser != null) {
                    for (int eventType = xmlPullParserNewPullParser.getEventType(); eventType != 1; eventType = xmlPullParserNewPullParser.next()) {
                        if (eventType == 2) {
                            int i = 0;
                            if (xmlPullParserNewPullParser.getName().equals("iconback")) {
                                while (i < xmlPullParserNewPullParser.getAttributeCount()) {
                                    if (xmlPullParserNewPullParser.getAttributeName(i).startsWith("img") && (bitmapLoadBitmap = loadBitmap(xmlPullParserNewPullParser.getAttributeValue(i))) != null) {
                                        this.mBackImages.add(bitmapLoadBitmap);
                                    }
                                    i++;
                                }
                            } else if (xmlPullParserNewPullParser.getName().equals("iconmask")) {
                                if (xmlPullParserNewPullParser.getAttributeCount() > 0 && xmlPullParserNewPullParser.getAttributeName(0).startsWith("img")) {
                                    this.mMaskImage = loadBitmap(xmlPullParserNewPullParser.getAttributeValue(0));
                                }
                            } else if (xmlPullParserNewPullParser.getName().equals("iconupon")) {
                                if (xmlPullParserNewPullParser.getAttributeCount() > 0 && xmlPullParserNewPullParser.getAttributeName(0).startsWith("img")) {
                                    this.mFrontImage = loadBitmap(xmlPullParserNewPullParser.getAttributeValue(0));
                                }
                            } else if (xmlPullParserNewPullParser.getName().equals("scale")) {
                                if (xmlPullParserNewPullParser.getAttributeCount() > 0 && xmlPullParserNewPullParser.getAttributeName(0).equals("factor")) {
                                    try {
                                        this.mFactor = Float.valueOf(xmlPullParserNewPullParser.getAttributeValue(null, "factor")).floatValue();
                                    } catch (NumberFormatException unused) {
                                    }
                                }
                            } else if (xmlPullParserNewPullParser.getName().equals("item")) {
                                String attributeValue = null;
                                String attributeValue2 = null;
                                while (i < xmlPullParserNewPullParser.getAttributeCount()) {
                                    if (xmlPullParserNewPullParser.getAttributeName(i).equals("component")) {
                                        attributeValue = xmlPullParserNewPullParser.getAttributeValue(i);
                                    } else if (xmlPullParserNewPullParser.getAttributeName(i).equals("drawable")) {
                                        attributeValue2 = xmlPullParserNewPullParser.getAttributeValue(i);
                                    }
                                    i++;
                                }
                                if (!this.mAppFilterDrawables.containsKey(attributeValue)) {
                                    this.mAppFilterDrawables.put(attributeValue, attributeValue2);
                                    this.totalIcons++;
                                }
                            } else if (xmlPullParserNewPullParser.getName().equals("calendar")) {
                                String attributeValue3 = null;
                                String attributeValue4 = null;
                                while (i < xmlPullParserNewPullParser.getAttributeCount()) {
                                    if (xmlPullParserNewPullParser.getAttributeName(i).equals("component")) {
                                        attributeValue3 = xmlPullParserNewPullParser.getAttributeValue(i);
                                    } else if (xmlPullParserNewPullParser.getAttributeName(i).equals("prefix")) {
                                        attributeValue4 = xmlPullParserNewPullParser.getAttributeValue(i);
                                    }
                                    if (attributeValue3 != null && attributeValue4 != null) {
                                        this.mCalendarPrefixes.put(attributeValue3, attributeValue4);
                                    }
                                    i++;
                                }
                            }
                        }
                    }
                }
                this.mShader = new IconPackShader(this.mContext, this.mPackageName, this.mVersionCode);
                this.mAppFilterLoaded = true;
            } catch (IOException e3) {
                Log.e(TAG, "IOException during loading of appfilter.xml", e3);
            }
        } catch (PackageManager.NameNotFoundException e4) {
            Log.d(TAG, "Cannot load icon pack", e4);
        } catch (XmlPullParserException e5) {
            Log.d(TAG, "Cannot parse icon pack appfilter.xml", e5);
        }
    }

    public Drawable loadDrawable(String str) {
        int identifier = this.iconPackres.getIdentifier(str, "drawable", this.mPackageName);
        if (identifier > 0) {
            return this.iconPackres.getDrawable(identifier);
        }
        return null;
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public void loadDrawableList() {
        XmlPullParser xmlPullParserNewPullParser;
        synchronized (this.mDrawableList) {
            /* REWORK: jadx lost an exception-flow local across catch scopes. */
            Throwable eReflow = null;
            if (this.mDrawableListLoaded) {
                return;
            }
            try {
                try {
                    this.iconPackres = this.mContext.getPackageManager().getResourcesForApplication(this.mPackageName);
                    int identifier = this.iconPackres.getIdentifier("drawable", "xml", this.mPackageName);
                    if (identifier > 0) {
                        xmlPullParserNewPullParser = this.iconPackres.getXml(identifier);
                    } else {
                        try {
                            InputStream inputStreamOpen = this.iconPackres.getAssets().open("drawable.xml");
                            XmlPullParserFactory xmlPullParserFactoryNewInstance = XmlPullParserFactory.newInstance();
                            xmlPullParserFactoryNewInstance.setNamespaceAware(true);
                            xmlPullParserNewPullParser = xmlPullParserFactoryNewInstance.newPullParser();
                            try {
                                xmlPullParserNewPullParser.setInput(inputStreamOpen, "utf-8");
                            } catch (Exception e) {
                                /* REWORK: setInput throws XmlPullParserException, not IOException */
                                e = e;
                                Log.d(TAG, "No drawable.xml file", e);
                            }
                        } catch (IOException e2) {
                            eReflow = e2;
                            xmlPullParserNewPullParser = null;
                        }
                    }
                    if (xmlPullParserNewPullParser != null) {
                        for (int eventType = xmlPullParserNewPullParser.getEventType(); eventType != 1; eventType = xmlPullParserNewPullParser.next()) {
                            if (eventType == 2 && xmlPullParserNewPullParser.getName().equals("item")) {
                                String attributeValue = null;
                                for (int i = 0; i < xmlPullParserNewPullParser.getAttributeCount(); i++) {
                                    if (xmlPullParserNewPullParser.getAttributeName(i).equals("drawable")) {
                                        attributeValue = xmlPullParserNewPullParser.getAttributeValue(i);
                                    }
                                }
                                this.mDrawableList.add(attributeValue);
                            }
                        }
                    }
                    this.mDrawableListLoaded = true;
                } catch (IOException e3) {
                    Log.e(TAG, "IOException during loading of drawable.xml", e3);
                }
            } catch (PackageManager.NameNotFoundException e4) {
                Log.d(TAG, "Cannot load icon pack", e4);
            } catch (XmlPullParserException e5) {
                Log.d(TAG, "Cannot parse icon pack drawable.xml", e5);
            }
        }
    }

    public void releaseMemory() {
        IconPackShader iconPackShader = this.mShader;
        if (iconPackShader != null) {
            iconPackShader.releaseRegisters();
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public void verifyDrawableList() {
        synchronized (this.mDrawableList) {
            if (this.mDrawableListLoaded) {
                if (this.mDrawableListVerified) {
                    return;
                }
                Iterator<String> it = this.mDrawableList.iterator();
                while (it.hasNext()) {
                    String next = it.next();
                    if (TextUtils.isEmpty(next) || this.iconPackres.getIdentifier(next, "drawable", this.mPackageName) == 0) {
                        it.remove();
                    }
                }
                this.mDrawableListVerified = true;
            }
        }
    }
}
