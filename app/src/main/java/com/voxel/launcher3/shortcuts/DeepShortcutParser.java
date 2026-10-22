package com.voxel.launcher3.shortcuts;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.voxel.launcher3.compat.UserHandleCompat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* JADX INFO: loaded from: classes.dex */
public class DeepShortcutParser {
    private static final String TAG = "DeepShortcutParser";

    private static DeepShortcutInfoCompatV16 createShortcutFromManifest(Resources resources, int i, String str, String str2, ComponentName componentName, int i2, int i3, int i4, int i5, int i6, boolean z) {
        DeepShortcutInfoCompatV16 deepShortcutInfoCompatV16 = new DeepShortcutInfoCompatV16();
        deepShortcutInfoCompatV16.mUserHandle = UserHandleCompat.myUserHandle();
        deepShortcutInfoCompatV16.mId = str;
        deepShortcutInfoCompatV16.mPackage = str2;
        deepShortcutInfoCompatV16.mActivity = componentName;
        deepShortcutInfoCompatV16.mRank = i5;
        deepShortcutInfoCompatV16.mEnabled = z;
        if (i2 != 0) {
            deepShortcutInfoCompatV16.mShortLabel = resources.getString(i2);
        }
        if (i3 != 0) {
            deepShortcutInfoCompatV16.mLongLabel = resources.getString(i3);
        }
        if (i4 != 0) {
            deepShortcutInfoCompatV16.mDisabledMessage = resources.getString(i4);
        }
        deepShortcutInfoCompatV16.mIconResId = i6;
        return deepShortcutInfoCompatV16;
    }

    private static DeepShortcutInfoCompatV16 parseShortcutAttributes(Resources resources, AttributeSet attributeSet, String str, ComponentName componentName, int i, int i2) {
        String attributeValue = attributeSet.getAttributeValue("http://schemas.android.com/apk/res/android", "shortcutId");
        boolean attributeBooleanValue = attributeSet.getAttributeBooleanValue("http://schemas.android.com/apk/res/android", "enabled", true);
        int attributeResourceValue = attributeSet.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "icon", 0);
        int attributeResourceValue2 = attributeSet.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "shortcutShortLabel", 0);
        int attributeResourceValue3 = attributeSet.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "shortcutLongLabel", 0);
        int attributeResourceValue4 = attributeSet.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "shortcutDisabledMessage", 0);
        if (TextUtils.isEmpty(attributeValue)) {
            Log.w(TAG, "android:shortcutId must be provided. activity=" + componentName);
            return null;
        }
        if (attributeResourceValue2 != 0) {
            return createShortcutFromManifest(resources, i, attributeValue, str, componentName, attributeResourceValue2, attributeResourceValue3, attributeResourceValue4, i2, attributeResourceValue, attributeBooleanValue);
        }
        Log.w(TAG, "android:shortcutShortLabel must be provided. activity=" + componentName);
        return null;
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public static List<DeepShortcutInfoCompatV16> parseShortcutsFile(Resources resources, ActivityInfo activityInfo, String str, XmlPullParser xmlPullParser) {
        int i;
        ArrayList arrayList = new ArrayList();
        try {
            if (xmlPullParser == null) {
                List<DeepShortcutInfoCompatV16> listEmptyList = Collections.emptyList();
                if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                    ((XmlResourceParser) xmlPullParser).close();
                }
                return listEmptyList;
            }
            ComponentName componentName = new ComponentName(str, activityInfo.name);
            AttributeSet attributeSetAsAttributeSet = Xml.asAttributeSet(xmlPullParser);
            ArrayList arrayList2 = new ArrayList();
            int i2 = 0;
            DeepShortcutInfoCompatV16 deepShortcutInfoCompatV16 = null;
            int i3 = 0;
            while (true) {
                int next = xmlPullParser.next();
                if (next == 1 || (next == 3 && xmlPullParser.getDepth() <= 0)) {
                    break;
                }
                int depth = xmlPullParser.getDepth();
                String name = xmlPullParser.getName();
                if (next != 3 || depth != 2 || !"shortcut".equals(name)) {
                    if (next != 2) {
                        i = i2;
                    } else if (depth != 1 || !"shortcuts".equals(name)) {
                        if (depth == 2 && "shortcut".equals(name)) {
                            i = i2;
                            DeepShortcutInfoCompatV16 shortcutAttributes = parseShortcutAttributes(resources, attributeSetAsAttributeSet, str, componentName, 0, i3);
                            if (shortcutAttributes != null) {
                                int size = arrayList.size() - 1;
                                while (true) {
                                    if (size < 0) {
                                        deepShortcutInfoCompatV16 = shortcutAttributes;
                                        i2 = i;
                                        break;
                                    }
                                    if (shortcutAttributes.getId().equals(((DeepShortcutInfoCompatV16) arrayList.get(size)).getId())) {
                                        Log.e(TAG, "Duplicate shortcut ID detected. Skipping it.");
                                    } else {
                                        size--;
                                    }
                                }
                            }
                        } else {
                            i = i2;
                            if (depth == 3 && "intent".equals(name)) {
                                if (deepShortcutInfoCompatV16 == null || !deepShortcutInfoCompatV16.isEnabled()) {
                                    Log.e(TAG, "Ignoring excessive intent tag.");
                                } else {
                                    Intent intent = Intent.parseIntent(resources, xmlPullParser, attributeSetAsAttributeSet);
                                    if (TextUtils.isEmpty(intent.getAction())) {
                                        Log.e(TAG, "Shortcut intent action must be provided. activity=" + componentName);
                                        i2 = i;
                                        deepShortcutInfoCompatV16 = null;
                                    } else {
                                        arrayList2.add(intent);
                                    }
                                }
                            } else if (depth != 3 || !"categories".equals(name)) {
                                Log.w(TAG, String.format("Invalid tag '%s' found at depth %d", name, Integer.valueOf(depth)));
                            }
                        }
                    } else {
                        i = i2;
                    }
                    i2 = i;
                    break;
                } else {
                    if (deepShortcutInfoCompatV16 == null) {
                        i = i2;
                        i2 = i;
                        break;
                    }
                    if (deepShortcutInfoCompatV16.isEnabled()) {
                        if (arrayList2.size() == 0) {
                            Log.e(TAG, "Shortcut " + deepShortcutInfoCompatV16.getId() + " has no intent. Skipping it.");
                        }
                        deepShortcutInfoCompatV16 = null;
                    } else {
                        arrayList2.clear();
                        arrayList2.add(new Intent("android.intent.action.VIEW"));
                    }
                    if (i2 >= 4) {
                        Log.e(TAG, "More than 4 shortcuts found for " + activityInfo + ". Skipping the rest.");
                        if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                            ((XmlResourceParser) xmlPullParser).close();
                        }
                        return arrayList;
                    }
                    ((Intent) arrayList2.get(arrayList2.size() - 1)).addFlags(268484608);
                    try {
                        deepShortcutInfoCompatV16.mIntent = (Intent) arrayList2.get(arrayList2.size() - 1);
                        arrayList2.clear();
                        arrayList.add(deepShortcutInfoCompatV16);
                        i2++;
                        i3++;
                        deepShortcutInfoCompatV16 = null;
                    } catch (RuntimeException unused) {
                        Log.e(TAG, "Shortcut's extras contain un-persistable values. Skipping it.");
                        deepShortcutInfoCompatV16 = null;
                    }
                }
            }
            if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
            }
            return arrayList;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e2) {
            e2.printStackTrace();
        } finally {
            if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                ((XmlResourceParser) xmlPullParser).close();
            }
        }
        /* REWORK: the parser fell through its catch blocks; report what we got. */
        return arrayList;
    }
}
