package com.voxel.simplesearchlauncher.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

/* JADX INFO: loaded from: classes2.dex */
public class CombinedFactorHelper extends Observable {
    public static final Combiner ADD_COMBINER = new Combiner() { // from class: com.voxel.simplesearchlauncher.utils.-$$Lambda$CombinedFactorHelper$6WjGuw15km6sO_6UUnaniAt2-5c
        @Override // com.voxel.simplesearchlauncher.utils.CombinedFactorHelper.Combiner
        public final float combine(float f, float f2) {
            return CombinedFactorHelper.lambda$static$0(f, f2);
        }
    };
    public static final Combiner MULTIPLY_COMBINER = new Combiner() { // from class: com.voxel.simplesearchlauncher.utils.-$$Lambda$CombinedFactorHelper$SVGLyEZvI3FcYOTu0eislVhG248
        @Override // com.voxel.simplesearchlauncher.utils.CombinedFactorHelper.Combiner
        public final float combine(float f, float f2) {
            return CombinedFactorHelper.lambda$static$1(f, f2);
        }
    };
    private Map<Object, FactorHolder> mFactorMap = new HashMap();
    private Map<Object, FactorHolder> mModifierMap = new HashMap();

    public interface Combiner {
        float combine(float f, float f2);
    }

    private static class FactorHolder {
        float factor;

        private FactorHolder() {
        }

        public String toString() {
            return "CombinedFactorHelper.FactorHolder(factor=" + this.factor + ")";
        }
    }

    static /* synthetic */ float lambda$static$0(float f, float f2) {
        return f + f2;
    }

    static /* synthetic */ float lambda$static$1(float f, float f2) {
        return f * f2;
    }

    public float getCombinedFactor(Combiner combiner) {
        boolean z = false;
        float fCombine = 0.0f;
        for (Object obj : this.mFactorMap.keySet()) {
            FactorHolder factorHolder = this.mFactorMap.get(obj);
            FactorHolder factorHolder2 = this.mModifierMap.get(obj);
            if (z) {
                float f = factorHolder.factor;
                if (factorHolder2 != null) {
                    f *= factorHolder2.factor;
                }
                fCombine = combiner.combine(fCombine, f);
            } else {
                float f2 = factorHolder.factor;
                if (factorHolder2 != null) {
                    f2 *= factorHolder2.factor;
                }
                fCombine = f2;
                z = true;
            }
        }
        return fCombine;
    }

    public void put(Object obj, float f) {
        FactorHolder factorHolder = this.mFactorMap.get(obj);
        if (factorHolder == null) {
            factorHolder = new FactorHolder();
            this.mFactorMap.put(obj, factorHolder);
        }
        if (f != factorHolder.factor) {
            factorHolder.factor = f;
            setChanged();
            notifyObservers();
        }
    }

    public void putModifier(Object obj, float f) {
        FactorHolder factorHolder = this.mModifierMap.get(obj);
        if (factorHolder == null) {
            factorHolder = new FactorHolder();
            this.mModifierMap.put(obj, factorHolder);
        }
        if (f != factorHolder.factor) {
            factorHolder.factor = f;
            setChanged();
            notifyObservers();
        }
    }
}
