package com.voxel.launcher3.compat;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import com.voxel.launcher3.Utilities;
import java.lang.reflect.Method;
import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
public class AlphabeticIndexCompat {
    private final BaseIndex mBaseIndex;
    private final String mDefaultMiscLabel;

    private static class AlphabeticIndexV16 extends BaseIndex {
        private Object mAlphabeticIndex;
        private Method mGetBucketIndexMethod;
        private Method mGetBucketLabelMethod;

        public AlphabeticIndexV16(Context context) throws Exception {
            Locale locale = context.getResources().getConfiguration().locale;
            Class<?> cls = Class.forName("libcore.icu.AlphabeticIndex");
            this.mGetBucketIndexMethod = cls.getDeclaredMethod("getBucketIndex", String.class);
            this.mGetBucketLabelMethod = cls.getDeclaredMethod("getBucketLabel", Integer.TYPE);
            this.mAlphabeticIndex = cls.getConstructor(Locale.class).newInstance(locale);
            if (locale.getLanguage().equals(Locale.ENGLISH.getLanguage())) {
                return;
            }
            cls.getDeclaredMethod("addLabels", Locale.class).invoke(this.mAlphabeticIndex, Locale.ENGLISH);
        }

        @Override // com.voxel.launcher3.compat.AlphabeticIndexCompat.BaseIndex
        protected int getBucketIndex(String str) {
            try {
                return ((Integer) this.mGetBucketIndexMethod.invoke(this.mAlphabeticIndex, str)).intValue();
            } catch (Exception e) {
                e.printStackTrace();
                return super.getBucketIndex(str);
            }
        }

        @Override // com.voxel.launcher3.compat.AlphabeticIndexCompat.BaseIndex
        protected String getBucketLabel(int i) {
            try {
                return (String) this.mGetBucketLabelMethod.invoke(this.mAlphabeticIndex, Integer.valueOf(i));
            } catch (Exception e) {
                e.printStackTrace();
                return super.getBucketLabel(i);
            }
        }
    }

    private static class AlphabeticIndexVN extends BaseIndex {
        private Object mAlphabeticIndex;
        private Method mGetBucketIndexMethod;
        private Method mGetBucketMethod;
        private Method mGetLabelMethod;

        public AlphabeticIndexVN(Context context) throws Exception {
            Object objInvoke = Configuration.class.getDeclaredMethod("getLocales", new Class[0]).invoke(context.getResources().getConfiguration(), new Object[0]);
            int iIntValue = ((Integer) objInvoke.getClass().getDeclaredMethod("size", new Class[0]).invoke(objInvoke, new Object[0])).intValue();
            Method declaredMethod = objInvoke.getClass().getDeclaredMethod("get", Integer.TYPE);
            Locale locale = iIntValue == 0 ? Locale.ENGLISH : (Locale) declaredMethod.invoke(objInvoke, 0);
            Class<?> cls = Class.forName("android.icu.text.AlphabeticIndex");
            this.mAlphabeticIndex = cls.getConstructor(Locale.class).newInstance(locale);
            Method declaredMethod2 = cls.getDeclaredMethod("addLabels", Locale[].class);
            for (int i = 1; i < iIntValue; i++) {
                declaredMethod2.invoke(this.mAlphabeticIndex, new Locale[]{(Locale) declaredMethod.invoke(objInvoke, Integer.valueOf(i))});
            }
            declaredMethod2.invoke(this.mAlphabeticIndex, new Locale[]{Locale.ENGLISH});
            this.mAlphabeticIndex = this.mAlphabeticIndex.getClass().getDeclaredMethod("buildImmutableIndex", new Class[0]).invoke(this.mAlphabeticIndex, new Object[0]);
            this.mGetBucketIndexMethod = this.mAlphabeticIndex.getClass().getDeclaredMethod("getBucketIndex", CharSequence.class);
            this.mGetBucketMethod = this.mAlphabeticIndex.getClass().getDeclaredMethod("getBucket", Integer.TYPE);
            this.mGetLabelMethod = this.mGetBucketMethod.getReturnType().getDeclaredMethod("getLabel", new Class[0]);
        }

        @Override // com.voxel.launcher3.compat.AlphabeticIndexCompat.BaseIndex
        protected int getBucketIndex(String str) {
            try {
                return ((Integer) this.mGetBucketIndexMethod.invoke(this.mAlphabeticIndex, str)).intValue();
            } catch (Exception e) {
                e.printStackTrace();
                return super.getBucketIndex(str);
            }
        }

        @Override // com.voxel.launcher3.compat.AlphabeticIndexCompat.BaseIndex
        protected String getBucketLabel(int i) {
            try {
                return (String) this.mGetLabelMethod.invoke(this.mGetBucketMethod.invoke(this.mAlphabeticIndex, Integer.valueOf(i)), new Object[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return super.getBucketLabel(i);
            }
        }
    }

    private static class BaseIndex {
        private static final int UNKNOWN_BUCKET_INDEX = 36;

        private BaseIndex() {
        }

        protected int getBucketIndex(String str) {
            int iIndexOf;
            return (str.isEmpty() || (iIndexOf = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-".indexOf(str.substring(0, 1).toUpperCase())) == -1) ? UNKNOWN_BUCKET_INDEX : iIndexOf;
        }

        protected String getBucketLabel(int i) {
            return "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-".substring(i, i + 1);
        }
    }

    public AlphabeticIndexCompat(Context context) {
        BaseIndex alphabeticIndexV16;
        try {
            alphabeticIndexV16 = Utilities.ATLEAST_N ? new AlphabeticIndexVN(context) : null;
        } catch (Exception e) {
            Log.d("AlphabeticIndexCompat", "Unable to load the system index", e);
            alphabeticIndexV16 = null;
        }
        if (alphabeticIndexV16 == null) {
            try {
                alphabeticIndexV16 = new AlphabeticIndexV16(context);
            } catch (Exception e2) {
                Log.d("AlphabeticIndexCompat", "Unable to load the system index", e2);
            }
        }
        this.mBaseIndex = alphabeticIndexV16 == null ? new BaseIndex() : alphabeticIndexV16;
        if (context.getResources().getConfiguration().locale.getLanguage().equals(Locale.JAPANESE.getLanguage())) {
            this.mDefaultMiscLabel = "他";
        } else {
            this.mDefaultMiscLabel = "∙";
        }
    }

    public String computeSectionName(CharSequence charSequence) {
        String strTrim = Utilities.trim(charSequence);
        BaseIndex baseIndex = this.mBaseIndex;
        String bucketLabel = baseIndex.getBucketLabel(baseIndex.getBucketIndex(strTrim));
        if (!Utilities.trim(bucketLabel).isEmpty() || strTrim.length() <= 0) {
            return bucketLabel;
        }
        int iCodePointAt = strTrim.codePointAt(0);
        if (Character.isDigit(iCodePointAt)) {
            return "#";
        }
        return Character.isLetter(iCodePointAt) ? this.mDefaultMiscLabel : "∙";
    }
}
