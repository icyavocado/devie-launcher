package com.android.gallery3d.glrenderer;

import android.graphics.Bitmap;
import android.graphics.RectF;

/* JADX INFO: loaded from: classes.dex */
public interface GLCanvas {
    void drawTexture(BasicTexture basicTexture, int i, int i2, int i3, int i4);

    void drawTexture(BasicTexture basicTexture, RectF rectF, RectF rectF2);

    GLId getGLId();

    void initializeTexture(BasicTexture basicTexture, Bitmap bitmap);

    void initializeTextureSize(BasicTexture basicTexture, int i, int i2);

    void restore();

    void rotate(float f, float f2, float f3, float f4);

    void save(int i);

    void setTextureParameters(BasicTexture basicTexture);

    void texSubImage2D(BasicTexture basicTexture, int i, int i2, Bitmap bitmap, int i3, int i4);

    void translate(float f, float f2);

    boolean unloadTexture(BasicTexture basicTexture);
}
