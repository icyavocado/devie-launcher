package com.voxel.simplesearchlauncher.utils.alpha;

import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;
import com.voxel.simplesearchlauncher.utils.AlphaUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/* JADX INFO: loaded from: classes2.dex */
public class AlphaSet extends Observable {
    private Map<Object, Float> mAlphaMap = new HashMap();
    private float mCurrentValue = 1.0f;
    final ArrayList<View> mNormalViews = new ArrayList<>();
    final ArrayList<ImageView> mImageViews = new ArrayList<>();
    final Map<View, Modifier> mModifiers = new HashMap();

    public static class AnimatableProperty {
        AlphaSet mAlphaSet;
        Object mKey;

        public AnimatableProperty(AlphaSet alphaSet, Object obj) {
            this.mAlphaSet = alphaSet;
            this.mKey = obj;
        }

        public void setAlpha(float f) {
            this.mAlphaSet.putValue(this.mKey, f);
        }
    }

    public interface Modifier {
        float modify(float f);
    }

    public AlphaSet() {
    }

    public AlphaSet(View... viewArr) {
        for (View view : viewArr) {
            if (view != null) {
                if (view instanceof ImageView) {
                    this.mImageViews.add((ImageView) view);
                } else {
                    this.mNormalViews.add(view);
                }
            }
        }
        initBaseObserver();
    }

    public AlphaSet(ImageView... imageViewArr) {
        for (ImageView imageView : imageViewArr) {
            if (imageView != null) {
                this.mImageViews.add(imageView);
            }
        }
        initBaseObserver();
    }

    private void initBaseObserver() {
        addObserver(new Observer() { // from class: com.voxel.simplesearchlauncher.utils.alpha.AlphaSet.1
            @Override // java.util.Observer
            public void update(Observable observable, Object obj) {
                float fFloatValue = ((Float) obj).floatValue();
                for (View view : AlphaSet.this.mNormalViews) {
                    Modifier modifier = AlphaSet.this.mModifiers.get(view);
                    view.setAlpha(modifier != null ? AlphaUtil.limitAlpha(modifier.modify(fFloatValue)) : fFloatValue);
                    AlphaSet.updateVisibility(view);
                }
                for (ImageView imageView : AlphaSet.this.mImageViews) {
                    Modifier modifier2 = AlphaSet.this.mModifiers.get(imageView);
                    imageView.setImageAlpha(AlphaUtil.floatToIntAlpha(modifier2 != null ? AlphaUtil.limitAlpha(modifier2.modify(fFloatValue)) : fFloatValue));
                    AlphaSet.updateVisibility(imageView);
                }
            }
        });
    }

    private void recalculateValue() {
        float f = this.mCurrentValue;
        Iterator<Float> it = this.mAlphaMap.values().iterator();
        float fFloatValue = 1.0f;
        while (it.hasNext()) {
            fFloatValue *= it.next().floatValue();
        }
        this.mCurrentValue = AlphaUtil.limitAlpha(fFloatValue);
        if (f != this.mCurrentValue) {
            setChanged();
            notifyObservers(Float.valueOf(this.mCurrentValue));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void updateVisibility(View view) {
        if (view.getAlpha() < 0.01f && view.getVisibility() != 8) {
            view.setVisibility(8);
        } else {
            if (view.getAlpha() <= 0.01f || view.getVisibility() == 0) {
                return;
            }
            view.setVisibility(0);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void updateVisibility(ImageView imageView) {
        if (imageView.getImageAlpha() == 0 && imageView.getVisibility() != 8) {
            imageView.setVisibility(8);
        } else {
            if (imageView.getAlpha() <= 0.0f || imageView.getVisibility() == 0) {
                return;
            }
            imageView.setVisibility(0);
        }
    }

    public void addToSet(View view) {
        if (this.mImageViews.contains(view) || this.mNormalViews.contains(view) || view == null) {
            return;
        }
        if (view instanceof ImageView) {
            this.mImageViews.add((ImageView) view);
        } else {
            this.mNormalViews.add(view);
        }
        notifyObservers(Float.valueOf(this.mCurrentValue));
    }

    public Float getValueForKey(Object obj, float f) {
        Float f2 = this.mAlphaMap.get(obj);
        return f2 != null ? f2 : Float.valueOf(f);
    }

    public void putModifier(View view, Modifier modifier) {
        this.mModifiers.put(view, modifier);
        recalculateValue();
    }

    public void putValue(Object obj, float f) {
        this.mAlphaMap.put(obj, Float.valueOf(f));
        recalculateValue();
    }

    public void removeFromSet(View view) {
        if (view == null) {
            return;
        }
        if (view instanceof ImageView) {
            this.mImageViews.remove(view);
        } else {
            this.mNormalViews.remove(view);
        }
    }

    public void setLayerType(int i, Paint paint) {
        Iterator<View> it = this.mNormalViews.iterator();
        while (it.hasNext()) {
            it.next().setLayerType(i, paint);
        }
    }
}
