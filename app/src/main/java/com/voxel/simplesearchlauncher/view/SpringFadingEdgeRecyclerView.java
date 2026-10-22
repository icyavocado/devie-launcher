package com.voxel.simplesearchlauncher.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import com.voxel.recyclerview.widget.SpringRecyclerView;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class SpringFadingEdgeRecyclerView extends SpringRecyclerView {
    private boolean fadeBottom;
    private boolean fadeTop;

    public SpringFadingEdgeRecyclerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.fadeTop = true;
        this.fadeBottom = true;
        initializeFromAttributes(context, attributeSet);
    }

    private void initializeFromAttributes(Context context, AttributeSet attributeSet) {
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.SpringFadingEdgeRecyclerView);
        this.fadeTop = typedArrayObtainStyledAttributes.getBoolean(1, this.fadeTop);
        this.fadeBottom = typedArrayObtainStyledAttributes.getBoolean(0, this.fadeBottom);
        typedArrayObtainStyledAttributes.recycle();
    }

    @Override // android.view.View
    protected float getBottomFadingEdgeStrength() {
        if (!this.fadeBottom) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min((getScrollBottom() - getCurrentScrollPositionY()) / getVerticalFadingEdgeLength(), 1.0f));
    }

    @Override // android.view.View
    protected float getTopFadingEdgeStrength() {
        if (!this.fadeTop) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min((getCurrentScrollPositionY() - getScrollTop()) / getVerticalFadingEdgeLength(), 1.0f));
    }
}
