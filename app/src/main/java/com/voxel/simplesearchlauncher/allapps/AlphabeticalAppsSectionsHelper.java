package com.voxel.simplesearchlauncher.allapps;

import android.content.Context;
import com.voxel.launcher3.compat.AlphabeticIndexCompat;
import java.util.HashMap;

/* JADX INFO: loaded from: classes.dex */
public class AlphabeticalAppsSectionsHelper {
    private HashMap<CharSequence, String> mCachedSectionNames = new HashMap<>();
    private AlphabeticIndexCompat mIndexer;

    public AlphabeticalAppsSectionsHelper(Context context) {
        this.mIndexer = new AlphabeticIndexCompat(context);
    }

    public String getSectionName(CharSequence charSequence) {
        String str = this.mCachedSectionNames.get(charSequence);
        if (str != null) {
            return str;
        }
        String strComputeSectionName = this.mIndexer.computeSectionName(charSequence);
        this.mCachedSectionNames.put(charSequence, strComputeSectionName);
        return strComputeSectionName;
    }
}
