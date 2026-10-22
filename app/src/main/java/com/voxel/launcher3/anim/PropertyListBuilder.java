package com.voxel.launcher3.anim;

import android.animation.PropertyValuesHolder;
import android.util.Property;
import android.view.View;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
public class PropertyListBuilder {
    private final ArrayList<PropertyValuesHolder> mProperties = new ArrayList<>();

    public PropertyValuesHolder[] build() {
        ArrayList<PropertyValuesHolder> arrayList = this.mProperties;
        return (PropertyValuesHolder[]) arrayList.toArray(new PropertyValuesHolder[arrayList.size()]);
    }

    public PropertyListBuilder scale(float f) {
        return scaleX(f).scaleY(f);
    }

    public PropertyListBuilder scaleX(float f) {
        this.mProperties.add(PropertyValuesHolder.ofFloat((Property<?, Float>) View.SCALE_X, f));
        return this;
    }

    public PropertyListBuilder scaleY(float f) {
        this.mProperties.add(PropertyValuesHolder.ofFloat((Property<?, Float>) View.SCALE_Y, f));
        return this;
    }

    public PropertyListBuilder translationY(float f) {
        this.mProperties.add(PropertyValuesHolder.ofFloat((Property<?, Float>) View.TRANSLATION_Y, f));
        return this;
    }
}
