package com.android.gallery3d.glrenderer;

import android.util.Log;

/* JADX INFO: loaded from: classes.dex */
public class RawTexture extends BasicTexture {
    private boolean mIsFlipped;
    private final boolean mOpaque;

    /* REWORK: jadx dropped the only ctor, leaving the final field unassigned;
     * restore the AOSP (width, height, opaque) construction. */
    public RawTexture(int i, int i2, boolean z) {
        this.mOpaque = z;
        setSize(i, i2);
    }

    @Override // com.android.gallery3d.glrenderer.BasicTexture
    protected int getTarget() {
        return 3553;
    }

    @Override // com.android.gallery3d.glrenderer.BasicTexture
    public boolean isFlippedVertically() {
        return this.mIsFlipped;
    }

    @Override // com.android.gallery3d.glrenderer.Texture
    public boolean isOpaque() {
        return this.mOpaque;
    }

    @Override // com.android.gallery3d.glrenderer.BasicTexture
    protected boolean onBind(GLCanvas gLCanvas) {
        if (isLoaded()) {
            return true;
        }
        Log.w("RawTexture", "lost the content due to context change");
        return false;
    }

    @Override // com.android.gallery3d.glrenderer.BasicTexture
    public void yield() {
    }
}
