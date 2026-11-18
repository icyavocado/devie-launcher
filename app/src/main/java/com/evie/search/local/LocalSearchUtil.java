package com.evie.search.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.evie.search.model.LocalSearchItem;
import com.evie.search.model.SearchKeywordItem;
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

    private static final int NON_ASCII_BUCKETS = 200;

    static int charToBucket(char c) {
        if (c == ' ') {
            return 0;
        }
        if (c > '`' && c < '{') {
            return c - '`';
        }
        return 26 + (Character.toLowerCase(c) % NON_ASCII_BUCKETS) + 1;
    }

    static int distance(String str, String str2, boolean z) {
        int len1 = str.length();
        int len2 = str2.length();
        int[] buckets1 = new int[len1];
        int[] buckets2 = new int[len2];
        for (int i = 0; i < len1; i++) {
            buckets1[i] = charToBucket(str.charAt(i));
        }
        for (int i2 = 0; i2 < len2; i2++) {
            buckets2[i2] = charToBucket(str2.charAt(i2));
        }
        int[] lastRowByBucket = new int[27 + NON_ASCII_BUCKETS];
        int[][] matrix = new int[len1 + 2][len2 + 2];
        int size = len1 + len2;
        matrix[0][0] = size;
        int i3 = 0;
        while (i3 <= len1) {
            int i4 = i3 + 1;
            matrix[i4][0] = size;
            matrix[i4][1] = i3;
            i3 = i4;
        }
        int i5 = 0;
        while (i5 <= len2) {
            int i6 = i5 + 1;
            matrix[0][i6] = size;
            matrix[1][i6] = i5;
            i5 = i6;
        }
        for (int i7 = 1; i7 <= len1; i7++) {
            int i8 = 1;
            int i9 = 0;
            while (i8 <= len2) {
                int i10 = i8 - 1;
                int i11 = lastRowByBucket[buckets2[i10]];
                boolean zEquals = buckets1[i7 - 1] == buckets2[i10];
                int i12 = zEquals ? i8 : i9;
                int i13 = matrix[i7][i8] + (!zEquals ? 1 : 0);
                int i14 = i7 + 1;
                int i15 = i8 + 1;
                matrix[i14][i15] = Math.min(Math.min(i13, matrix[i14][i8] + 1), Math.min(matrix[i7][i15] + 1, matrix[i11][i9] + ((i7 - i11) - 1) + 1 + ((i8 - i9) - 1)));
                i9 = i12;
                i8 = i15;
            }
            lastRowByBucket[buckets1[i7 - 1]] = i7;
        }
        int iMin = matrix[len1 + 1][len2 + 1];
        if (z && len1 <= len2) {
            int iMaxDistance = maxDistance(str);
            int size2 = iMaxDistance == 0 ? len1 : Math.max(len1 - iMaxDistance, 4);
            int iMin2 = Math.min(len1 + iMaxDistance, len2);
            while (size2 <= iMin2) {
                size2++;
                iMin = Math.min(iMin, matrix[len1 + 1][size2]);
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
        String norm = normalizeString(localSearchItem.getLabel());
        if (norm != null) {
            norm = norm.toLowerCase();
        }
        contentValues.put("label_norm", norm);
        contentValues.put("note", localSearchItem.getNote());
        contentValues.put("type", localSearchItem.getType());
        contentValues.put("usage", Float.valueOf(f));
        sQLiteDatabase.beginTransaction();
        try {
            long jInsert = sQLiteDatabase.insert("item", null, contentValues);
            if (jInsert == -1) {
                return;
            }
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
        } finally {
            sQLiteDatabase.endTransaction();
        }
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
        updateRank(localSearchItem, str, i, null);
    }

    static void updateRank(LocalSearchItem localSearchItem, String str, int i, String labelNorm) {
        if (localSearchItem == null) {
            return;
        }
        float length = (str.length() - i) / (float) str.length();
        String normalized = labelNorm != null ? labelNorm : normalizeString(localSearchItem.getLabel()).toLowerCase();
        HashSet<Character> hashSet = new HashSet<>();
        HashSet<Character> hashSet2 = new HashSet<>();
        for (char c : normalized.toCharArray()) {
            hashSet.add(c);
            hashSet2.add(c);
        }
        float f = 0.0f;
        for (char c2 : str.toCharArray()) {
            if (hashSet.remove(c2)) {
                f += 1.0f;
            }
            hashSet2.add(c2);
        }
        float size = (length * 0.6f) + ((f / hashSet2.size()) * 0.2f);
        if (localSearchItem.getRank() < size) {
            localSearchItem.setRank(size);
        }
    }
}
