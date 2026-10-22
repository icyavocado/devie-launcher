package com.android.gallery3d.glrenderer;

import android.util.Log;
import com.android.gallery3d.common.Utils;
import java.util.WeakHashMap;

/* JADX INFO: loaded from: classes.dex */
public abstract class BasicTexture implements Texture {
    private static WeakHashMap<BasicTexture, Object> sAllTextures = new WeakHashMap<>();
    private static ThreadLocal sInFinalizer = new ThreadLocal();
    protected GLCanvas mCanvasRef;
    private boolean mHasBorder;
    protected int mHeight;
    protected int mId;
    protected int mState;
    protected int mTextureHeight;
    protected int mTextureWidth;
    protected int mWidth;

    protected BasicTexture() {
        this(null, 0, 0);
    }

    protected BasicTexture(GLCanvas gLCanvas, int i, int i2) {
        this.mId = -1;
        this.mWidth = -1;
        this.mHeight = -1;
        this.mCanvasRef = null;
        setAssociatedCanvas(gLCanvas);
        this.mId = i;
        this.mState = i2;
        synchronized (sAllTextures) {
            sAllTextures.put(this, null);
        }
    }

    private void freeResource() {
        GLCanvas gLCanvas = this.mCanvasRef;
        if (gLCanvas != null && this.mId != -1) {
            gLCanvas.unloadTexture(this);
            this.mId = -1;
        }
        this.mState = 0;
        setAssociatedCanvas(null);
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public static void invalidateAllTextures() {
        synchronized (sAllTextures) {
            for (BasicTexture basicTexture : sAllTextures.keySet()) {
                basicTexture.mState = 0;
                basicTexture.setAssociatedCanvas(null);
            }
        }
    }

    public void draw(GLCanvas gLCanvas, int i, int i2, int i3, int i4) {
        gLCanvas.drawTexture(this, i, i2, i3, i4);
    }

    protected void finalize() {
        sInFinalizer.set(BasicTexture.class);
        recycle();
        sInFinalizer.set(null);
    }

    public int getHeight() {
        return this.mHeight;
    }

    public int getId() {
        return this.mId;
    }

    protected abstract int getTarget();

    public int getTextureHeight() {
        return this.mTextureHeight;
    }

    public int getTextureWidth() {
        return this.mTextureWidth;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public boolean hasBorder() {
        return this.mHasBorder;
    }

    public boolean isFlippedVertically() {
        return false;
    }

    public boolean isLoaded() {
        return this.mState == 1;
    }

    protected abstract boolean onBind(GLCanvas gLCanvas);

    public void recycle() {
        freeResource();
    }

    protected void setAssociatedCanvas(GLCanvas gLCanvas) {
        this.mCanvasRef = gLCanvas;
    }

    protected void setBorder(boolean z) {
        this.mHasBorder = z;
    }

    public void setSize(int i, int i2) {
        this.mWidth = i;
        this.mHeight = i2;
        this.mTextureWidth = i > 0 ? Utils.nextPowerOf2(i) : 0;
        this.mTextureHeight = i2 > 0 ? Utils.nextPowerOf2(i2) : 0;
        if (this.mTextureWidth > 4096 || this.mTextureHeight > 4096) {
            Log.w("BasicTexture", String.format("texture is too large: %d x %d", Integer.valueOf(this.mTextureWidth), Integer.valueOf(this.mTextureHeight)), new Exception());
        }
    }

    public void yield() {
        freeResource();
    }
}
