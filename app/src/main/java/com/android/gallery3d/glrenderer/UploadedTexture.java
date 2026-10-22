package com.android.gallery3d.glrenderer;

import android.graphics.Bitmap;
import android.opengl.GLUtils;
import com.android.gallery3d.common.Utils;
import java.util.HashMap;

/* JADX INFO: loaded from: classes.dex */
public abstract class UploadedTexture extends BasicTexture {
    private static int sUploadedCount;
    protected Bitmap mBitmap;
    private int mBorder;
    private boolean mContentValid;
    private boolean mIsUploading;
    private boolean mOpaque;
    private boolean mThrottled;
    private static HashMap<BorderKey, Bitmap> sBorderLines = new HashMap<>();
    private static BorderKey sBorderKey = new BorderKey();

    static class BorderKey implements Cloneable {
        public Bitmap.Config config;
        public int length;
        public boolean vertical;

        BorderKey() {
        }

        /* JADX INFO: renamed from: clone, reason: merged with bridge method [inline-methods] */
        public BorderKey m4clone() {
            try {
                return (BorderKey) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(e);
            }
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof BorderKey)) {
                return false;
            }
            BorderKey borderKey = (BorderKey) obj;
            return this.vertical == borderKey.vertical && this.config == borderKey.config && this.length == borderKey.length;
        }

        public int hashCode() {
            int iHashCode = this.config.hashCode() ^ this.length;
            return this.vertical ? iHashCode : -iHashCode;
        }
    }

    protected UploadedTexture() {
        this(false);
    }

    protected UploadedTexture(boolean z) {
        super(null, 0, 0);
        this.mContentValid = true;
        this.mIsUploading = false;
        this.mOpaque = true;
        this.mThrottled = false;
        if (z) {
            setBorder(true);
            this.mBorder = 1;
        }
    }

    private void freeBitmap() {
        Utils.assertTrue(this.mBitmap != null);
        onFreeBitmap(this.mBitmap);
        this.mBitmap = null;
    }

    private Bitmap getBitmap() {
        if (this.mBitmap == null) {
            this.mBitmap = onGetBitmap();
            int width = this.mBitmap.getWidth() + (this.mBorder * 2);
            int height = this.mBitmap.getHeight() + (this.mBorder * 2);
            if (this.mWidth == -1) {
                setSize(width, height);
            }
        }
        return this.mBitmap;
    }

    private static Bitmap getBorderLine(boolean z, Bitmap.Config config, int i) {
        BorderKey borderKey = sBorderKey;
        borderKey.vertical = z;
        borderKey.config = config;
        borderKey.length = i;
        Bitmap bitmapCreateBitmap = sBorderLines.get(borderKey);
        if (bitmapCreateBitmap == null) {
            bitmapCreateBitmap = z ? Bitmap.createBitmap(1, i, config) : Bitmap.createBitmap(i, 1, config);
            sBorderLines.put(borderKey.m4clone(), bitmapCreateBitmap);
        }
        return bitmapCreateBitmap;
    }

    private void uploadToCanvas(GLCanvas gLCanvas) {
        Bitmap bitmap = getBitmap();
        if (bitmap == null) {
            this.mState = -1;
            throw new RuntimeException("Texture load fail, no bitmap");
        }
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int i = this.mBorder;
            int i2 = this.mBorder;
            int textureWidth = getTextureWidth();
            int textureHeight = getTextureHeight();
            Utils.assertTrue(width <= textureWidth && height <= textureHeight);
            this.mId = gLCanvas.getGLId().generateTexture();
            gLCanvas.setTextureParameters(this);
            if (width == textureWidth && height == textureHeight) {
                gLCanvas.initializeTexture(this, bitmap);
            } else {
                int internalFormat = GLUtils.getInternalFormat(bitmap);
                int type = GLUtils.getType(bitmap);
                Bitmap.Config config = bitmap.getConfig();
                gLCanvas.initializeTextureSize(this, internalFormat, type);
                gLCanvas.texSubImage2D(this, this.mBorder, this.mBorder, bitmap, internalFormat, type);
                if (this.mBorder > 0) {
                    gLCanvas.texSubImage2D(this, 0, 0, getBorderLine(true, config, textureHeight), internalFormat, type);
                    gLCanvas.texSubImage2D(this, 0, 0, getBorderLine(false, config, textureWidth), internalFormat, type);
                }
                if (this.mBorder + width < textureWidth) {
                    gLCanvas.texSubImage2D(this, this.mBorder + width, 0, getBorderLine(true, config, textureHeight), internalFormat, type);
                }
                if (this.mBorder + height < textureHeight) {
                    gLCanvas.texSubImage2D(this, 0, this.mBorder + height, getBorderLine(false, config, textureWidth), internalFormat, type);
                }
            }
            freeBitmap();
            setAssociatedCanvas(gLCanvas);
            this.mState = 1;
            this.mContentValid = true;
        } catch (Throwable th) {
            freeBitmap();
            throw th;
        }
    }

    @Override // com.android.gallery3d.glrenderer.BasicTexture
    public int getHeight() {
        if (this.mWidth == -1) {
            getBitmap();
        }
        return this.mHeight;
    }

    @Override // com.android.gallery3d.glrenderer.BasicTexture
    protected int getTarget() {
        return 3553;
    }

    @Override // com.android.gallery3d.glrenderer.BasicTexture
    public int getWidth() {
        if (this.mWidth == -1) {
            getBitmap();
        }
        return this.mWidth;
    }

    protected void invalidateContent() {
        if (this.mBitmap != null) {
            freeBitmap();
        }
        this.mContentValid = false;
        this.mWidth = -1;
        this.mHeight = -1;
    }

    public boolean isContentValid() {
        return isLoaded() && this.mContentValid;
    }

    @Override // com.android.gallery3d.glrenderer.Texture
    public boolean isOpaque() {
        return this.mOpaque;
    }

    @Override // com.android.gallery3d.glrenderer.BasicTexture
    protected boolean onBind(GLCanvas gLCanvas) {
        updateContent(gLCanvas);
        return isContentValid();
    }

    protected abstract void onFreeBitmap(Bitmap bitmap);

    protected abstract Bitmap onGetBitmap();

    @Override // com.android.gallery3d.glrenderer.BasicTexture
    public void recycle() {
        super.recycle();
        if (this.mBitmap != null) {
            freeBitmap();
        }
    }

    public void updateContent(GLCanvas gLCanvas) {
        if (!isLoaded()) {
            if (this.mThrottled) {
                int i = sUploadedCount + 1;
                sUploadedCount = i;
                if (i > 100) {
                    return;
                }
            }
            uploadToCanvas(gLCanvas);
            return;
        }
        if (this.mContentValid) {
            return;
        }
        Bitmap bitmap = getBitmap();
        int internalFormat = GLUtils.getInternalFormat(bitmap);
        int type = GLUtils.getType(bitmap);
        int i2 = this.mBorder;
        gLCanvas.texSubImage2D(this, i2, i2, bitmap, internalFormat, type);
        freeBitmap();
        this.mContentValid = true;
    }
}
