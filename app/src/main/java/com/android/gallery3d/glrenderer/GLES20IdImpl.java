package com.android.gallery3d.glrenderer;

import android.opengl.GLES20;
import javax.microedition.khronos.opengles.GL11;

/* JADX INFO: loaded from: classes.dex */
public class GLES20IdImpl implements GLId {
    private final int[] mTempIntArray = new int[1];

    @Override // com.android.gallery3d.glrenderer.GLId
    public int generateTexture() {
        GLES20.glGenTextures(1, this.mTempIntArray, 0);
        GLES20Canvas.checkError();
        return this.mTempIntArray[0];
    }

    @Override // com.android.gallery3d.glrenderer.GLId
    public void glDeleteBuffers(GL11 gl11, int i, int[] iArr, int i2) {
        GLES20.glDeleteBuffers(i, iArr, i2);
        GLES20Canvas.checkError();
    }

    @Override // com.android.gallery3d.glrenderer.GLId
    public void glDeleteTextures(GL11 gl11, int i, int[] iArr, int i2) {
        GLES20.glDeleteTextures(i, iArr, i2);
        GLES20Canvas.checkError();
    }

    @Override // com.android.gallery3d.glrenderer.GLId
    public void glGenBuffers(int i, int[] iArr, int i2) {
        GLES20.glGenBuffers(i, iArr, i2);
        GLES20Canvas.checkError();
    }
}
