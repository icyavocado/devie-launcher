package com.android.photos.views;

import android.content.Context;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.widget.FrameLayout;
import com.android.gallery3d.glrenderer.BasicTexture;
import com.android.gallery3d.glrenderer.GLES20Canvas;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/* JADX INFO: loaded from: classes.dex */
public class TiledImageView extends FrameLayout {
    private Choreographer.FrameCallback mFrameCallback;
    private Runnable mFreeTextures;
    GLSurfaceView mGLSurfaceView;
    boolean mInvalPending;
    protected Object mLock;
    protected ImageRendererWrapper mRenderer;
    private RectF mTempRectF;
    private float[] mValues;

    protected static class ImageRendererWrapper {
        public int centerX;
        public int centerY;
        TiledImageRenderer image;
        Runnable isReadyCallback;
        public int rotation;
        public float scale;
        public TiledImageRenderer.TileSource source;

        protected ImageRendererWrapper() {
        }
    }

    class TileRenderer implements GLSurfaceView.Renderer {
        private GLES20Canvas mCanvas;

        TileRenderer() {
        }

        @Override // android.opengl.GLSurfaceView.Renderer
        public void onDrawFrame(GL10 gl10) {
            Runnable runnable;
            this.mCanvas.clearBuffer();
            synchronized (TiledImageView.this.mLock) {
                runnable = TiledImageView.this.mRenderer.isReadyCallback;
                TiledImageView.this.mRenderer.image.setModel(TiledImageView.this.mRenderer.source, TiledImageView.this.mRenderer.rotation);
                TiledImageView.this.mRenderer.image.setPosition(TiledImageView.this.mRenderer.centerX, TiledImageView.this.mRenderer.centerY, TiledImageView.this.mRenderer.scale);
            }
            if (!TiledImageView.this.mRenderer.image.draw(this.mCanvas) || runnable == null) {
                return;
            }
            synchronized (TiledImageView.this.mLock) {
                if (TiledImageView.this.mRenderer.isReadyCallback == runnable) {
                    TiledImageView.this.mRenderer.isReadyCallback = null;
                }
            }
            if (runnable != null) {
                TiledImageView.this.post(runnable);
            }
        }

        @Override // android.opengl.GLSurfaceView.Renderer
        public void onSurfaceChanged(GL10 gl10, int i, int i2) {
            this.mCanvas.setSize(i, i2);
            TiledImageView.this.mRenderer.image.setViewSize(i, i2);
        }

        @Override // android.opengl.GLSurfaceView.Renderer
        public void onSurfaceCreated(GL10 gl10, EGLConfig eGLConfig) {
            this.mCanvas = new GLES20Canvas();
            BasicTexture.invalidateAllTextures();
            TiledImageView.this.mRenderer.image.setModel(TiledImageView.this.mRenderer.source, TiledImageView.this.mRenderer.rotation);
        }
    }

    public TiledImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mInvalPending = false;
        this.mValues = new float[9];
        this.mLock = new Object();
        this.mFreeTextures = new Runnable() { // from class: com.android.photos.views.TiledImageView.1
            @Override // java.lang.Runnable
            public void run() {
                TiledImageView.this.mRenderer.image.freeTextures();
            }
        };
        this.mTempRectF = new RectF();
        this.mRenderer = new ImageRendererWrapper();
        this.mRenderer.image = new TiledImageRenderer(this);
        this.mGLSurfaceView = new GLSurfaceView(context);
        this.mGLSurfaceView.setEGLContextClientVersion(2);
        this.mGLSurfaceView.setRenderer(new TileRenderer());
        this.mGLSurfaceView.setRenderMode(0);
        addView(this.mGLSurfaceView, new FrameLayout.LayoutParams(-1, -1));
    }

    private void invalOnVsync() {
        if (this.mInvalPending) {
            return;
        }
        this.mInvalPending = true;
        if (this.mFrameCallback == null) {
            this.mFrameCallback = new Choreographer.FrameCallback() { // from class: com.android.photos.views.-$$Lambda$TiledImageView$mBaKafax0zul7Db-CieDOfe0P70
                @Override // android.view.Choreographer.FrameCallback
                public final void doFrame(long j) {
                    TiledImageView.this.lambda$invalOnVsync$0( j);
                }
            };
        }
        Choreographer.getInstance().postFrameCallback(this.mFrameCallback);
    }

    public void lambda$invalOnVsync$0(long j) {
        this.mInvalPending = false;
        this.mGLSurfaceView.requestRender();
    }

    private void updateScaleIfNecessaryLocked(ImageRendererWrapper imageRendererWrapper) {
        if (imageRendererWrapper == null || imageRendererWrapper.source == null || imageRendererWrapper.scale > 0.0f || getWidth() == 0) {
            return;
        }
        imageRendererWrapper.scale = Math.min(getWidth() / imageRendererWrapper.source.getImageWidth(), getHeight() / imageRendererWrapper.source.getImageHeight());
    }

    public void destroy() {
        this.mGLSurfaceView.queueEvent(this.mFreeTextures);
    }

    public TiledImageRenderer.TileSource getTileSource() {
        return this.mRenderer.source;
    }

    @Override // android.view.View
    public void invalidate() {
        invalOnVsync();
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        synchronized (this.mLock) {
            updateScaleIfNecessaryLocked(this.mRenderer);
        }
    }

    public void setTileSource(TiledImageRenderer.TileSource tileSource, Runnable runnable) {
        synchronized (this.mLock) {
            this.mRenderer.source = tileSource;
            this.mRenderer.isReadyCallback = runnable;
            this.mRenderer.centerX = tileSource != null ? tileSource.getImageWidth() / 2 : 0;
            this.mRenderer.centerY = tileSource != null ? tileSource.getImageHeight() / 2 : 0;
            this.mRenderer.rotation = tileSource != null ? tileSource.getRotation() : 0;
            this.mRenderer.scale = 0.0f;
            updateScaleIfNecessaryLocked(this.mRenderer);
        }
        invalidate();
    }

    @Override // android.view.View
    public void setVisibility(int i) {
        super.setVisibility(i);
        this.mGLSurfaceView.setVisibility(i);
    }
}
