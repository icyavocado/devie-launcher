package com.voxel.launcher3.anim;

import android.graphics.Rect;
import com.voxel.launcher3.util.PillRevealOutlineProvider;

/* JADX INFO: loaded from: classes.dex */
public class PillHeightRevealOutlineProvider extends PillRevealOutlineProvider {
    private final int mNewHeight;

    public PillHeightRevealOutlineProvider(Rect rect, float f, int i) {
        super(0, 0, rect, f);
        this.mOutline.set(rect);
        this.mNewHeight = i;
    }

    @Override // com.voxel.launcher3.util.PillRevealOutlineProvider, com.voxel.launcher3.util.RevealOutlineAnimation
    public void setProgress(float f) {
        this.mOutline.top = 0;
        this.mOutline.bottom = (int) (this.mPillRect.bottom - ((this.mPillRect.height() - this.mNewHeight) * (1.0f - f)));
    }
}
