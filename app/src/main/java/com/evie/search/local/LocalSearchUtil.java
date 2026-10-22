package com.evie.search.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.evie.search.model.LocalSearchItem;
import com.evie.search.model.SearchKeywordItem;
import java.lang.reflect.Array;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import timber.log.Timber;

/* JADX INFO: loaded from: classes.dex */
class LocalSearchUtil {
    static void addKeywordWithDeletions(Set<String> set, String str, int i, boolean z) {
        set.add(str);
        int i2 = 1;
        if (i < 1) {
            return;
        }
        int length = z ? str.length() : str.length() - 1;
        while (i2 < length) {
            StringBuilder sb = new StringBuilder();
            sb.append(str.substring(0, i2));
            i2++;
            sb.append(str.substring(i2));
            set.add(sb.toString());
        }
    }

    static void addKeywords(List<SearchKeywordItem> list, String str, String str2) {
        String strNormalizeString = normalizeString(str);
        if (strNormalizeString == null) {
            return;
        }
        list.add(new SearchKeywordItem(strNormalizeString.toLowerCase(), str2));
        String[] strArr = tokenizeString(strNormalizeString);
        if (strArr == null || strArr.length <= 1) {
            return;
        }
        for (String str3 : strArr) {
            list.add(new SearchKeywordItem(str3, null));
        }
    }

    static int distance(String str, String str2, boolean z) {
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        for (int i = 0; i < str.length(); i++) {
            char cCharAt = str.charAt(i);
            if (cCharAt == ' ') {
                arrayList.add(0);
            } else if (cCharAt > '`' && cCharAt < '{') {
                arrayList.add(Integer.valueOf(cCharAt - '`'));
            }
        }
        for (int i2 = 0; i2 < str2.length(); i2++) {
            char cCharAt2 = str2.charAt(i2);
            if (cCharAt2 == ' ') {
                arrayList2.add(0);
            } else if (cCharAt2 > '`' && cCharAt2 < '{') {
                arrayList2.add(Integer.valueOf(cCharAt2 - '`'));
            }
        }
        int[] iArr = new int[27];
        int[][] iArr2 = (int[][]) Array.newInstance((Class<?>) int.class, arrayList.size() + 2, arrayList2.size() + 2);
        int size = arrayList.size() + arrayList2.size();
        iArr2[0][0] = size;
        int i3 = 0;
        while (i3 <= arrayList.size()) {
            int i4 = i3 + 1;
            iArr2[i4][0] = size;
            iArr2[i4][1] = i3;
            i3 = i4;
        }
        int i5 = 0;
        while (i5 <= arrayList2.size()) {
            int i6 = i5 + 1;
            iArr2[0][i6] = size;
            iArr2[1][i6] = i5;
            i5 = i6;
        }
        for (int i7 = 1; i7 <= arrayList.size(); i7++) {
            int i8 = 1;
            int i9 = 0;
            while (i8 <= arrayList2.size()) {
                int i10 = i8 - 1;
                int i11 = iArr[((Integer) arrayList2.get(i10)).intValue()];
                boolean zEquals = ((Integer) arrayList.get(i7 - 1)).equals(arrayList2.get(i10));
                int i12 = zEquals ? i8 : i9;
                int i13 = iArr2[i7][i8] + (!zEquals ? 1 : 0);
                int i14 = i7 + 1;
                int i15 = i8 + 1;
                iArr2[i14][i15] = Math.min(Math.min(i13, iArr2[i14][i8] + 1), Math.min(iArr2[i7][i15] + 1, iArr2[i11][i9] + ((i7 - i11) - 1) + 1 + ((i8 - i9) - 1)));
                i9 = i12;
                i8 = i15;
            }
            iArr[((Integer) arrayList.get(i7 - 1)).intValue()] = i7;
        }
        int iMin = iArr2[arrayList.size() + 1][arrayList2.size() + 1];
        if (z && arrayList.size() <= arrayList2.size()) {
            int iMaxDistance = maxDistance(str);
            int size2 = iMaxDistance == 0 ? arrayList.size() : Math.max(arrayList.size() - iMaxDistance, 4);
            int iMin2 = Math.min(arrayList.size() + iMaxDistance, arrayList2.size());
            while (size2 <= iMin2) {
                size2++;
                iMin = Math.min(iMin, iArr2[arrayList.size() + 1][size2]);
            }
        }
        return iMin;
    }

    static void insert(SQLiteDatabase sQLiteDatabase, LocalSearchItem localSearchItem, float f) {
        if (localSearchItem == null || TextUtils.isEmpty(localSearchItem.getLink()) || localSearchItem.getKeywords().size() == 0) {
            Timber.e("Insert called with invalid LocalLocalSearchItem.", new Object[0]);
            return;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("link", localSearchItem.getLink());
        contentValues.put("label", localSearchItem.getLabel());
        contentValues.put("note", localSearchItem.getNote());
        contentValues.put("type", localSearchItem.getType());
        contentValues.put("usage", Float.valueOf(f));
        sQLiteDatabase.beginTransaction();
        long jInsert = sQLiteDatabase.insert("item", null, contentValues);
        for (SearchKeywordItem searchKeywordItem : localSearchItem.getKeywords()) {
            String keyword = searchKeywordItem.getKeyword();
            HashSet<String> hashSet = new HashSet();
            int iMin = Math.min(6, keyword.length());
            for (int i = 1; i <= iMin; i++) {
                String strSubstring = keyword.substring(0, i);
                addKeywordWithDeletions(hashSet, strSubstring, maxDistance(strSubstring), false);
            }
            for (String str : hashSet) {
                ContentValues contentValues2 = new ContentValues();
                contentValues2.put("search_keyword", str);
                contentValues2.put("match_keyword", keyword);
                contentValues2.put("keyword_type", searchKeywordItem.getKeywordType());
                contentValues2.put("item_id", Long.valueOf(jInsert));
                sQLiteDatabase.insert("search", null, contentValues2);
            }
        }
        sQLiteDatabase.setTransactionSuccessful();
        sQLiteDatabase.endTransaction();
    }

    static boolean keywordMatches(SQLiteDatabase sQLiteDatabase, long j, String str, String str2) {
        String strNormalizeString = normalizeString(str2);
        if (strNormalizeString != null) {
            strNormalizeString = strNormalizeString.toLowerCase();
        }
        Cursor cursorQuery = null;
        try {
            boolean zEquals = false;
            cursorQuery = sQLiteDatabase.query("search", new String[]{"match_keyword"}, "item_id=? AND keyword_type=?", new String[]{String.valueOf(j), str}, null, null, null, "1");
            if (cursorQuery.moveToFirst()) {
                zEquals = cursorQuery.getString(cursorQuery.getColumnIndexOrThrow("match_keyword")).equals(strNormalizeString);
            } else if (strNormalizeString == null) {
                zEquals = true;
            }
            return zEquals;
        } finally {
            if (cursorQuery != null) {
                cursorQuery.close();
            }
        }
    }

    static int maxDistance(String str) {
        return str.length() < 4 ? 0 : 1;
    }

    static String normalizeString(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replaceAll("'", "");
    }

    static String[] tokenizeString(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        String[] strArrSplit = str.split("\\s+|(?<!\\p{Upper})(?=\\p{Upper})");
        for (int i = 0; i < strArrSplit.length; i++) {
            strArrSplit[i] = strArrSplit[i].toLowerCase();
        }
        return strArrSplit;
    }

    static void updateRank(LocalSearchItem localSearchItem, float f, float f2) {
        localSearchItem.setRank(localSearchItem.getRank() + ((f * 0.2f) / f2));
    }

    static void updateRank(LocalSearchItem localSearchItem, String str, int i) {
        if (localSearchItem == null) {
            return;
        }
        float length = (str.length() - i) / str.length();
        HashSet hashSet = new HashSet();
        HashSet hashSet2 = new HashSet();
        for (char c : normalizeString(localSearchItem.getLabel()).toLowerCase().toCharArray()) {
            hashSet.add(Character.valueOf(c));
            hashSet2.add(Character.valueOf(c));
        }
        float f = 0.0f;
        for (char c2 : str.toCharArray()) {
            if (hashSet.remove(Character.valueOf(c2))) {
                f += 1.0f;
            }
            hashSet2.add(Character.valueOf(c2));
        }
        float size = (length * 0.6f) + ((f / hashSet2.size()) * 0.2f);
        if (localSearchItem.getRank() < size) {
            localSearchItem.setRank(size);
        }
    }
}
