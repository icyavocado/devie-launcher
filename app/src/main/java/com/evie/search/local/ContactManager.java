package com.evie.search.local;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.evie.common.HybridNetworkFetcherCommon;
import com.evie.search.drawable.TextDrawable;
import com.evie.search.model.ActionData;
import com.evie.search.model.AppData;
import com.evie.search.model.AppLink;
import com.evie.search.model.ContactData;
import com.evie.search.model.LocalSearchItem;
import com.voxel.simplesearchlauncher.utils.BitmapUtil;
import com.voxel.simplesearchlauncher.utils.ColorUtil;
import java.util.ArrayList;
import java.util.HashMap;
import timber.log.Timber;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class ContactManager {
    private static ContactManager instance;
    private ContentResolver mContentResolver;
    private LocalAppsManager mLocalAppsManager;
    private PackageManager mPackageManager;
    private Resources mResources;

    private ContactManager(Context context) {
        this.mLocalAppsManager = LocalAppsManager.getInstance(context);
        this.mPackageManager = context.getPackageManager();
        this.mContentResolver = context.getContentResolver();
        this.mResources = context.getResources();
    }

    private String buildInitialsUri(String str, TextDrawable.IShapeBuilder iShapeBuilder, int i) {
        String initials = getInitials(str);
        TypedArray typedArrayObtainTypedArray = this.mResources.obtainTypedArray(R.array.contact_colors);
        int color = typedArrayObtainTypedArray.getColor(Math.abs(str.hashCode()) % typedArrayObtainTypedArray.length(), -16777216);
        typedArrayObtainTypedArray.recycle();
        return BitmapUtil.getUrlFromBitmap(BitmapUtil.getBitmap(iShapeBuilder.buildRound(initials, color), i));
    }

    private String getClearPhoneNumber(String str, int i) {
        if (str == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int length = str.length() - 1; length >= 0; length--) {
            char cCharAt = str.charAt(length);
            if (cCharAt >= '0' && cCharAt <= '9') {
                sb.insert(0, cCharAt);
                if (sb.length() == i) {
                    break;
                }
            }
        }
        return sb.toString();
    }

    private static String getFirstLetter(String str) {
        for (int i = 0; i < str.length(); i++) {
            char cCharAt = str.charAt(i);
            if (Character.isLetter(cCharAt)) {
                return String.valueOf(Character.toUpperCase(cCharAt));
            }
        }
        return null;
    }

    private String getHeaderImageUrl(int i) {
        TypedArray typedArrayObtainTypedArray = this.mResources.obtainTypedArray(R.array.contact_colors);
        int i2 = 0;
        int i3 = -1;
        double d = 0.0d;
        for (int i4 = 0; i4 < typedArrayObtainTypedArray.length(); i4++) {
            double colorDifference = ColorUtil.getColorDifference(i, typedArrayObtainTypedArray.getColor(i4, -16777216));
            if (i3 < 0 || colorDifference < d) {
                i3 = i4;
                d = colorDifference;
            }
        }
        typedArrayObtainTypedArray.recycle();
        String str = this.mResources.getDisplayMetrics().densityDpi <= 240 ? "hdpi" : "xhdpi";
        TypedArray typedArrayObtainTypedArray2 = this.mResources.obtainTypedArray(R.array.contact_headers);
        if (i3 >= 0 && i3 < typedArrayObtainTypedArray2.length()) {
            i2 = i3;
        }
        String str2 = typedArrayObtainTypedArray2.getString(i2) + str;
        typedArrayObtainTypedArray2.recycle();
        return str2;
    }

    private String getIconUrl(AppData appData) {
        try {
            return HybridNetworkFetcherCommon.getMagicResourceUri(appData.getAppPackage(), appData.getAppActivity(), this.mPackageManager.getPackageInfo(appData.getAppPackage(), 0).versionCode).toString();
        } catch (PackageManager.NameNotFoundException unused) {
            Timber.e("Error getting app version code.", new Object[0]);
            return null;
        }
    }

    private static String getInitials(String str) {
        String[] strArrSplit = str.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String str2 : strArrSplit) {
            String firstLetter = getFirstLetter(str2);
            if (firstLetter != null) {
                sb.append(firstLetter);
            }
            if (sb.length() > 1) {
                break;
            }
        }
        return sb.toString();
    }

    public static ContactManager getInstance(Context context) {
        if (instance == null) {
            instance = new ContactManager(context);
        }
        return instance;
    }

    /* JADX WARN: Code duplicated, block: B:13:0x0029  */
    private String getPhoneActionLabel(String str, int i, String str2) {
        String phoneNumberTypeString = getPhoneNumberTypeString(i, false, str2);
        switch (str) {
            case "call":
                return phoneNumberTypeString == null ? this.mResources.getString(R.string.contact_action_call) : this.mResources.getString(R.string.contact_action_call_type, phoneNumberTypeString);
            case "text":
                return phoneNumberTypeString == null ? this.mResources.getString(R.string.contact_action_text) : this.mResources.getString(R.string.contact_action_text_type, phoneNumberTypeString);
            default:
                throw new IllegalArgumentException("Method getPhoneActionLabel should be invoked only for CALL or TEXT actions.");
        }
    }

    private String getPhoneNumberTypeString(int i, boolean z, String str) {
        StringBuilder sb = new StringBuilder();
        if (i != 17) {
            switch (i) {
                case 0:
                    sb.append(str);
                    break;
                case 1:
                    sb.append(this.mResources.getString(R.string.phone_number_home));
                    break;
                case 2:
                    sb.append(this.mResources.getString(R.string.phone_number_mobile));
                    break;
                case 3:
                    sb.append(this.mResources.getString(R.string.phone_number_work));
                    break;
                case 4:
                    sb.append(this.mResources.getString(R.string.phone_number_fax_work));
                    break;
                case 5:
                    sb.append(this.mResources.getString(R.string.phone_number_fax_home));
                    break;
                default:
                    sb = null;
                    break;
            }
        } else {
            sb.append(this.mResources.getString(R.string.phone_number_work_mobile));
        }
        if (sb == null || sb.length() == 0) {
            return null;
        }
        if (z) {
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        } else {
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        }
        return sb.toString();
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    private void updateContactAppLinks(ContactData contactData) throws Throwable {
        /* REWORK: jadx lost an exception-flow local across catch scopes. */
        Throwable thReflow = null;
        Cursor cursorQuery;
        try {
            ActionData[] actionDataArr = new ActionData[7];
            HashMap map = new HashMap();
            cursorQuery = this.mContentResolver.query(Uri.parse(contactData.getLink()), new String[]{"_id", "lookup"}, "in_visible_group=?", new String[]{"1"}, null);
            if (cursorQuery != null) {
                try {
                    if (cursorQuery.moveToFirst()) {
                        String string = cursorQuery.getString(cursorQuery.getColumnIndexOrThrow("lookup"));
                        cursorQuery.close();
                        boolean z = true;
                        cursorQuery = this.mContentResolver.query(ContactsContract.Data.CONTENT_URI, null, "lookup=?", new String[]{string}, null);
                        while (cursorQuery != null && cursorQuery.moveToNext()) {
                            String string2 = cursorQuery.getString(cursorQuery.getColumnIndexOrThrow("_id"));
                            String string3 = cursorQuery.getString(cursorQuery.getColumnIndexOrThrow("mimetype"));
                            String string4 = cursorQuery.getString(cursorQuery.getColumnIndexOrThrow("data1"));
                            String string5 = cursorQuery.getString(cursorQuery.getColumnIndexOrThrow("data4"));
                            if (!TextUtils.isEmpty(string2) && !TextUtils.isEmpty(string3)) {
                                if (string3.endsWith("/phone_v2")) {
                                    if (!TextUtils.isEmpty(string4)) {
                                        int i = cursorQuery.getInt(cursorQuery.getColumnIndex("data2"));
                                        int columnIndex = cursorQuery.getColumnIndex("data3");
                                        if (Build.VERSION.SDK_INT >= 21) {
                                            string4 = PhoneNumberUtils.formatNumber(string4, "US");
                                        }
                                        String str = "phone:" + getClearPhoneNumber(string4, 10);
                                        if (!map.containsKey(str)) {
                                            map.put(str, Boolean.valueOf(z));
                                            String string6 = i == 0 ? cursorQuery.getString(columnIndex) : null;
                                            String phoneNumberTypeString = getPhoneNumberTypeString(i, z, string6);
                                            AppData defaultPhoneApp = this.mLocalAppsManager.getDefaultPhoneApp();
                                            if (defaultPhoneApp != null) {
                                                AppLink appLink = new AppLink(defaultPhoneApp.getName(), defaultPhoneApp.getAppPackage(), "tel:" + string4, phoneNumberTypeString != null ? phoneNumberTypeString : defaultPhoneApp.getName(), string4);
                                                ActionData actionData = actionDataArr[0];
                                                if (actionData == null) {
                                                    ActionData actionData2 = new ActionData(getPhoneActionLabel("call", i, string6), appLink.getSubtext(), getIconUrl(defaultPhoneApp), "CALL");
                                                    actionData2.addLink(appLink);
                                                    actionDataArr[0] = actionData2;
                                                } else {
                                                    actionData.addLink(appLink);
                                                    actionData.setLabel(this.mResources.getString(R.string.contact_action_call));
                                                    actionData.setSubtext("" + actionData.getLinks().size() + " numbers");
                                                }
                                            }
                                            AppData defaultTextApp = this.mLocalAppsManager.getDefaultTextApp();
                                            if (defaultTextApp != null) {
                                                AppLink appLink2 = new AppLink(defaultTextApp.getName(), defaultTextApp.getAppPackage(), "sms:" + string4, phoneNumberTypeString != null ? phoneNumberTypeString : defaultTextApp.getName(), string4);
                                                ActionData actionData3 = actionDataArr[1];
                                                if (actionData3 == null) {
                                                    ActionData actionData4 = new ActionData(getPhoneActionLabel("text", i, string6), appLink2.getSubtext(), getIconUrl(defaultTextApp), "TEXT");
                                                    actionData4.addLink(appLink2);
                                                    actionDataArr[1] = actionData4;
                                                } else {
                                                    actionData3.addLink(appLink2);
                                                    actionData3.setLabel(this.mResources.getString(R.string.contact_action_text));
                                                    actionData3.setSubtext("" + actionData3.getLinks().size() + " numbers");
                                                }
                                            }
                                            z = true;
                                        }
                                    }
                                } else if (string3.endsWith("/email_v2")) {
                                    if (TextUtils.isEmpty(string4)) {
                                        z = true;
                                    } else if (string4.contains("@")) {
                                        String lowerCase = string4.toLowerCase();
                                        String str2 = "email:" + lowerCase;
                                        if (map.containsKey(str2)) {
                                            z = true;
                                        } else {
                                            map.put(str2, true);
                                            AppData defaultEmailApp = this.mLocalAppsManager.getDefaultEmailApp();
                                            if (defaultEmailApp != null) {
                                                AppLink appLink3 = new AppLink(defaultEmailApp.getName(), defaultEmailApp.getAppPackage(), "mailto:" + lowerCase, defaultEmailApp.getName(), lowerCase);
                                                ActionData actionData5 = actionDataArr[2];
                                                if (actionData5 == null) {
                                                    ActionData actionData6 = new ActionData(this.mResources.getString(R.string.contact_action_email), appLink3.getSubtext(), getIconUrl(defaultEmailApp), "EMAIL");
                                                    actionData6.addLink(appLink3);
                                                    actionDataArr[2] = actionData6;
                                                } else {
                                                    actionData5.addLink(appLink3);
                                                    actionData5.setSubtext("" + actionData5.getLinks().size() + " emails");
                                                }
                                            }
                                            z = true;
                                        }
                                    } else {
                                        z = true;
                                    }
                                } else if (string3.endsWith("/vnd.googleplus.profile.comm") && string5 != null && string5.equals("10")) {
                                    String str3 = "hangout:" + string2;
                                    if (map.containsKey(str3)) {
                                        z = true;
                                    } else {
                                        map.put(str3, true);
                                        AppData googleHangoutApp = this.mLocalAppsManager.getGoogleHangoutApp();
                                        if (googleHangoutApp != null) {
                                            AppLink appLink4 = new AppLink(googleHangoutApp.getName(), googleHangoutApp.getAppPackage(), "content://com.android.contacts/data/" + string2, googleHangoutApp.getName(), this.mResources.getString(R.string.contact_entity_chat, contactData.getName()));
                                            ActionData actionData7 = actionDataArr[3];
                                            if (actionData7 == null) {
                                                ActionData actionData8 = new ActionData(appLink4.getLabel(), null, getIconUrl(googleHangoutApp), "HANGOUT");
                                                actionData8.addLink(appLink4);
                                                actionDataArr[3] = actionData8;
                                            } else {
                                                actionData7.addLink(appLink4);
                                                actionData7.setSubtext("" + actionData7.getLinks().size() + " accounts");
                                            }
                                        }
                                        z = true;
                                    }
                                } else if (string3.endsWith("/vnd.com.whatsapp.profile")) {
                                    if (TextUtils.isEmpty(string4)) {
                                        z = true;
                                    } else {
                                        String str4 = "whatsapp:" + string4.toLowerCase();
                                        if (map.containsKey(str4)) {
                                            z = true;
                                        } else {
                                            map.put(str4, true);
                                            AppData whatsAppApp = this.mLocalAppsManager.getWhatsAppApp();
                                            if (whatsAppApp != null) {
                                                AppLink appLink5 = new AppLink(whatsAppApp.getName(), whatsAppApp.getAppPackage(), "content://com.android.contacts/data/" + string2, whatsAppApp.getName(), this.mResources.getString(R.string.contact_entity_message, contactData.getName()));
                                                ActionData actionData9 = actionDataArr[4];
                                                if (actionData9 == null) {
                                                    ActionData actionData10 = new ActionData(appLink5.getLabel(), null, getIconUrl(whatsAppApp), "WHATSAPP");
                                                    actionData10.addLink(appLink5);
                                                    actionDataArr[4] = actionData10;
                                                } else {
                                                    actionData9.addLink(appLink5);
                                                    actionData9.setSubtext("" + actionData9.getLinks().size() + " accounts");
                                                }
                                            }
                                            z = true;
                                        }
                                    }
                                } else if (!string3.endsWith("com.skype.android.chat.action")) {
                                    z = true;
                                } else if (TextUtils.isEmpty(string4)) {
                                    z = true;
                                } else {
                                    String str5 = "skype:" + string4.toLowerCase();
                                    if (map.containsKey(str5)) {
                                        z = true;
                                    } else {
                                        map.put(str5, true);
                                        AppData skypeApp = this.mLocalAppsManager.getSkypeApp();
                                        if (skypeApp != null) {
                                            AppLink appLink6 = new AppLink(skypeApp.getName(), skypeApp.getAppPackage(), "content://com.android.contacts/data/" + string2, skypeApp.getName(), this.mResources.getString(R.string.contact_entity_skype, string4));
                                            ActionData actionData11 = actionDataArr[5];
                                            if (actionData11 == null) {
                                                ActionData actionData12 = new ActionData(appLink6.getLabel(), null, getIconUrl(skypeApp), "SKYPE");
                                                actionData12.addLink(appLink6);
                                                actionDataArr[5] = actionData12;
                                            } else {
                                                actionData11.addLink(appLink6);
                                                actionData11.setSubtext("" + actionData11.getLinks().size() + " accounts");
                                            }
                                        }
                                        z = true;
                                    }
                                }
                            }
                        }
                        AppData defaultContactsApp = this.mLocalAppsManager.getDefaultContactsApp();
                        if (defaultContactsApp != null) {
                            String string7 = this.mResources.getString(R.string.contact_entity_open_in_contacts);
                            AppLink appLink7 = new AppLink(defaultContactsApp.getName(), defaultContactsApp.getAppPackage(), contactData.getLink(), string7, null);
                            ActionData actionData13 = new ActionData(string7, null, getIconUrl(defaultContactsApp), "OPEN_CONTACT");
                            actionData13.addLink(appLink7);
                            actionDataArr[6] = actionData13;
                        }
                        ArrayList arrayList = new ArrayList();
                        for (ActionData actionData14 : actionDataArr) {
                            if (actionData14 != null) {
                                arrayList.add(actionData14);
                            }
                        }
                        contactData.setActions(arrayList);
                        if (cursorQuery != null) {
                            cursorQuery.close();
                            return;
                        }
                        return;
                    }
                } catch (Throwable th) {
                    th = th;
                    if (cursorQuery != null) {
                        cursorQuery.close();
                    }
                    throw th;
                }
            }
            if (cursorQuery != null) {
                cursorQuery.close();
            }
        } catch (Throwable th2) {
            thReflow = th2;
            cursorQuery = null;
        }
    }

    private void updateContactNameAndImage(ContactData contactData) throws Throwable {
        /* REWORK: jadx lost an exception-flow local across catch scopes. */
        Throwable thReflow = null;
        Cursor cursorQuery;
        String strBuildInitialsUri = null;
        try {
            cursorQuery = this.mContentResolver.query(Uri.parse(contactData.getLink()), new String[]{"display_name", "photo_thumb_uri", "photo_uri"}, "in_visible_group=?", new String[]{"1"}, null);
            if (cursorQuery != null) {
                try {
                    if (cursorQuery.moveToFirst()) {
                        String string = cursorQuery.getString(cursorQuery.getColumnIndexOrThrow("display_name"));
                        String string2 = cursorQuery.getString(cursorQuery.getColumnIndexOrThrow("photo_thumb_uri"));
                        String string3 = cursorQuery.getString(cursorQuery.getColumnIndexOrThrow("photo_uri"));
                        contactData.setName(string);
                        if (!TextUtils.isEmpty(string2)) {
                            contactData.setOverlayUri(string2);
                            contactData.setShortcutUri(string2);
                            strBuildInitialsUri = string2;
                        }
                        if (!TextUtils.isEmpty(string3)) {
                            contactData.setOverlayUri(string3);
                            if (strBuildInitialsUri == null) {
                                contactData.setShortcutUri(string3);
                                strBuildInitialsUri = string3;
                            }
                        }
                        if (strBuildInitialsUri == null) {
                            contactData.setOverlayUri(buildInitialsUri(string, TextDrawable.builder().beginConfig().withBorder(this.mResources.getDimensionPixelSize(R.dimen.contact_circle_border_width)).fontSize(this.mResources.getDimensionPixelSize(R.dimen.contact_overlay_initials_size)).bold().endConfig(), this.mResources.getDimensionPixelSize(R.dimen.media_overlay_dimension)));
                            strBuildInitialsUri = buildInitialsUri(string, TextDrawable.builder().beginConfig().fontSize(this.mResources.getDimensionPixelSize(R.dimen.contact_shortcut_initials_size)).bold().endConfig(), this.mResources.getDimensionPixelSize(R.dimen.shortcut_icon_dimension));
                            contactData.setShortcutUri(strBuildInitialsUri);
                        }
                        contactData.setHeaderUri(getHeaderImageUrl(BitmapUtil.getTint(BitmapUtil.getBitmap(Uri.parse(strBuildInitialsUri), this.mContentResolver))));
                    }
                } catch (Throwable th) {
                    th = th;
                    if (cursorQuery != null) {
                        cursorQuery.close();
                    }
                    throw th;
                }
            }
            if (cursorQuery != null) {
                cursorQuery.close();
            }
        } catch (Throwable th2) {
            thReflow = th2;
            cursorQuery = null;
        }
    }

    LocalSearchItem buildContactFromCursor(Cursor cursor) throws Throwable {
        long j = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
        String string = cursor.getString(cursor.getColumnIndexOrThrow("lookup"));
        String string2 = cursor.getString(cursor.getColumnIndexOrThrow("display_name"));
        String string3 = new Uri.Builder().scheme("contact").opaquePart(ContactsContract.Contacts.CONTENT_LOOKUP_URI + "/" + string + "/" + j).build().toString();
        ArrayList arrayList = new ArrayList();
        LocalSearchUtil.addKeywords(arrayList, string2, "name");
        String nickname = getNickname(string);
        if (nickname != null) {
            LocalSearchUtil.addKeywords(arrayList, nickname, "nickname");
        }
        return new LocalSearchItem(string3, string2, nickname, arrayList, "CONTACT");
    }

    /* JADX WARN: Code duplicated, block: B:24:0x006b A[DONT_GENERATE, PHI: r2
      0x006b: PHI (r2v3 android.database.Cursor) = (r2v2 android.database.Cursor), (r2v4 android.database.Cursor) binds: [B:23:0x0069, B:17:0x0056] A[DONT_GENERATE, DONT_INLINE]] */
    public boolean contactRecentlyUsed(LocalSearchItem localSearchItem) {
        Cursor cursorQuery = null;
        try {
            try {
                cursorQuery = this.mContentResolver.query(Uri.parse(Uri.parse(localSearchItem.getLink()).getSchemeSpecificPart()), new String[]{"last_time_contacted"}, "in_visible_group=?", new String[]{"1"}, null);
                if (cursorQuery != null && cursorQuery.moveToFirst()) {
                    long j = cursorQuery.getLong(cursorQuery.getColumnIndexOrThrow("last_time_contacted"));
                    return j > 0 && System.currentTimeMillis() - 31536000000L < j;
                }
            } catch (Exception e) {
                Timber.e("Error retrieving contact data: %s", e.getMessage());
            }
            return false;
        } finally {
            if (cursorQuery != null) {
                cursorQuery.close();
            }
        }
    }

    Cursor getAllContactsCursor() {
        try {
            return this.mContentResolver.query(ContactsContract.Contacts.CONTENT_URI, new String[]{"_id", "lookup", "display_name", "last_time_contacted"}, "in_visible_group=?", new String[]{"1"}, null);
        } catch (SQLException | SecurityException e) {
            Timber.e(e);
            return null;
        }
    }

    public ContactData getContactData(String str) throws Throwable {
        ContactData contactData = new ContactData(str);
        updateContactNameAndImage(contactData);
        updateContactAppLinks(contactData);
        return contactData;
    }

    /* JADX WARN: Code duplicated, block: B:18:0x0038 A[PHI: r9
      0x0038: PHI (r9v7 android.database.Cursor) = (r9v6 android.database.Cursor), (r9v9 android.database.Cursor) binds: [B:17:0x0036, B:11:0x0028] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Code duplicated, block: B:23:0x0042  */
    String getContactLink(String str) throws Throwable {
        /* REWORK: jadx lost an exception-flow local across catch scopes. */
        Throwable thReflow = null;
        Cursor cursorQuery;
        Cursor cursor = null;
        try {
            cursorQuery = this.mContentResolver.query(Uri.parse(str), new String[]{"_id", "lookup"}, null, null, null);
            if (cursorQuery != null) {
                try {
                    try {
                        if (cursorQuery.moveToFirst()) {
                            String linkFromCursor = getLinkFromCursor(cursorQuery);
                            if (cursorQuery != null) {
                                cursorQuery.close();
                            }
                            return linkFromCursor;
                        }
                    } catch (Exception unused) {
                        Timber.e("Error looking up contact by lookupKey.", new Object[0]);
                        if (cursorQuery != null) {
                            cursorQuery.close();
                        }
                    }
                } catch (Throwable th) {
                    cursor = cursorQuery;
                    th = th;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
            if (cursorQuery != null) {
                cursorQuery.close();
            }
        } catch (Exception unused2) {
            cursorQuery = null;
        } catch (Throwable th2) {
            thReflow = th2;
            if (cursor != null) {
                cursor.close();
            }
            throw thReflow;
        }
        return null;
    }

    /* JADX WARN: Code duplicated, block: B:28:0x0092 A[DONT_GENERATE, PHI: r2
      0x0092: PHI (r2v3 android.database.Cursor) = (r2v2 android.database.Cursor), (r2v6 android.database.Cursor) binds: [B:27:0x0090, B:21:0x007c] A[DONT_GENERATE, DONT_INLINE]] */
    public String getImageUrl(LocalSearchItem localSearchItem) {
        Cursor cursorQuery = null;
        if (!localSearchItem.getType().equals("CONTACT")) {
            Timber.e("Type mismatch; expected contact, found " + localSearchItem.getType(), new Object[0]);
            return null;
        }
        try {
            try {
                cursorQuery = this.mContentResolver.query(Uri.parse(Uri.parse(localSearchItem.getLink()).getSchemeSpecificPart()), new String[]{"photo_thumb_uri", "photo_uri"}, "in_visible_group=?", new String[]{"1"}, null);
                if (cursorQuery != null && cursorQuery.moveToFirst()) {
                    String string = cursorQuery.getString(cursorQuery.getColumnIndexOrThrow("photo_thumb_uri"));
                    if (string != null) {
                        return string;
                    }
                    String string2 = cursorQuery.getString(cursorQuery.getColumnIndexOrThrow("photo_uri"));
                    if (string2 != null) {
                        if (cursorQuery != null) {
                            cursorQuery.close();
                        }
                        return string2;
                    }
                }
            } catch (Exception e) {
                Timber.e("Error retrieving contact imageUrl: %s", e.getMessage());
            }
            return buildInitialsUri(localSearchItem.getLabel(), TextDrawable.builder().beginConfig().fontSize(this.mResources.getDimensionPixelSize(R.dimen.contact_shortcut_initials_size)).bold().endConfig(), this.mResources.getDimensionPixelSize(R.dimen.search_item_image_dimension));
        } finally {
            if (cursorQuery != null) {
                cursorQuery.close();
            }
        }
    }

    String getLinkFromCursor(Cursor cursor) {
        long j = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
        String string = cursor.getString(cursor.getColumnIndexOrThrow("lookup"));
        return new Uri.Builder().scheme("contact").opaquePart(ContactsContract.Contacts.CONTENT_LOOKUP_URI + "/" + string + "/" + j).build().toString();
    }

    /* JADX WARN: Code duplicated, block: B:21:0x005b A[PHI: r9
      0x005b: PHI (r9v7 android.database.Cursor) = (r9v6 android.database.Cursor), (r9v10 android.database.Cursor) binds: [B:20:0x0059, B:13:0x0034] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Code duplicated, block: B:25:0x0062  */
    String getName(String str) throws Throwable {
        Throwable th;
        Cursor cursorQuery;
        try {
            cursorQuery = this.mContentResolver.query(Uri.parse(str), new String[]{"display_name"}, "in_visible_group=?", new String[]{"1"}, null);
            if (cursorQuery != null) {
                try {
                    try {
                        if (cursorQuery.moveToFirst()) {
                            String string = cursorQuery.getString(cursorQuery.getColumnIndexOrThrow("display_name"));
                            if (cursorQuery != null) {
                                cursorQuery.close();
                            }
                            return string;
                        }
                    } catch (Exception e) {
                        e = e;
                        Timber.e("Error retrieving name: " + e.getMessage(), new Object[0]);
                        if (cursorQuery != null) {
                            cursorQuery.close();
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (cursorQuery != null) {
                        cursorQuery.close();
                    }
                    throw th;
                }
            }
            if (cursorQuery != null) {
                cursorQuery.close();
            }
        } catch (Exception e2) {
            cursorQuery = null; /* REWORK: jadx dead reflow store */
        } catch (Throwable th3) {
            th = th3;
            cursorQuery = null;
            if (cursorQuery != null) {
                cursorQuery.close();
            }
            throw th;
        }
        return null;
    }

    /* JADX WARN: Code duplicated, block: B:21:0x005b A[PHI: r9
      0x005b: PHI (r9v6 android.database.Cursor) = (r9v5 android.database.Cursor), (r9v8 android.database.Cursor) binds: [B:20:0x0059, B:13:0x0037] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Code duplicated, block: B:25:0x0062  */
    String getNickname(String str) throws Throwable {
        Cursor cursorQuery;
        try {
            cursorQuery = this.mContentResolver.query(ContactsContract.Data.CONTENT_URI, new String[]{"data1"}, "lookup=? AND mimetype=?", new String[]{str, "vnd.android.cursor.item/nickname"}, null);
            if (cursorQuery != null) {
                try {
                    try {
                        if (cursorQuery.moveToFirst()) {
                            String string = cursorQuery.getString(cursorQuery.getColumnIndexOrThrow("data1"));
                            if (cursorQuery != null) {
                                cursorQuery.close();
                            }
                            return string;
                        }
                    } catch (Exception e) {
                        e = e;
                        Timber.e("Error retrieving nickname: " + e.getMessage(), new Object[0]);
                        if (cursorQuery != null) {
                            cursorQuery.close();
                        }
                    }
                } catch (Throwable th) {
                    th = th;
                    if (cursorQuery != null) {
                        cursorQuery.close();
                    }
                    throw th;
                }
            }
            if (cursorQuery != null) {
                cursorQuery.close();
            }
        } catch (Exception e2) {
            cursorQuery = null; /* REWORK: jadx dead reflow store */
        } catch (Throwable th2) {
            /* REWORK: jadx reflowed through a local the decompile dropped; rethrow
             * the caught throwable directly after cleanup. */
            cursorQuery = null;
            if (cursorQuery != null) {
                cursorQuery.close();
            }
            throw th2;
        }
        return null;
    }
}
