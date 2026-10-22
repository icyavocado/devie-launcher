package com.evie.common.iconpack.shader;

import java.lang.reflect.Array;
import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
class ShaderRegister {
    private float brightness;
    private float[][] channels;
    private float[] intensity;
    private int size = -1;
    private boolean intensityDirty = true;
    private boolean brightnessDirty = true;

    ShaderRegister() {
    }

    private void dirty() {
        this.intensityDirty = true;
        this.brightnessDirty = true;
    }

    private void ensureSize(int i) {
        if (this.size < i) {
            this.channels = (float[][]) Array.newInstance((Class<?>) Float.TYPE, 4, i);
        }
    }

    private void recalculateBrightnessIfNeeded() {
        if (this.brightnessDirty) {
            double d = 0.0d;
            double d2 = 0.0d;
            for (int i = 0; i < this.size; i++) {
                float[][] fArr = this.channels;
                double d3 = (fArr[0][i] * ((fArr[1][i] + fArr[2][i]) + fArr[3][i])) / 3.0f;
                Double.isNaN(d3);
                d += d3;
                double d4 = fArr[0][i];
                Double.isNaN(d4);
                d2 += d4;
            }
            this.brightness = (float) (d / d2);
            this.brightnessDirty = false;
        }
    }

    private void recalculateIntensityIfNeeded() {
        if (this.intensityDirty) {
            float[] fArr = this.intensity;
            if (fArr == null || fArr.length < this.size) {
                this.intensity = new float[this.size];
            }
            for (int i = 0; i < this.size; i++) {
                float[] fArr2 = this.intensity;
                float[][] fArr3 = this.channels;
                fArr2[i] = ((fArr3[1][i] + fArr3[2][i]) + fArr3[3][i]) / 3.0f;
            }
            this.intensityDirty = false;
        }
    }

    void addChannel(int i, float[] fArr, int i2) {
        float[] fArr2 = this.channels[i];
        for (int i3 = 0; i3 < i2; i3++) {
            fArr2[i3] = fArr2[i3] + fArr[i3];
        }
        dirty();
    }

    void addValue(int i, float f, int i2) {
        float[] fArr = this.channels[i];
        for (int i3 = 0; i3 < i2; i3++) {
            fArr[i3] = fArr[i3] + f;
        }
        dirty();
    }

    void divideChannel(int i, float[] fArr, int i2) {
        float[] fArr2 = this.channels[i];
        for (int i3 = 0; i3 < i2; i3++) {
            fArr2[i3] = fArr2[i3] / fArr[i3];
        }
        dirty();
    }

    void divideValue(int i, float f, int i2) {
        float[] fArr = this.channels[i];
        for (int i3 = 0; i3 < i2; i3++) {
            fArr[i3] = fArr[i3] / f;
        }
        dirty();
    }

    float getBrightness() {
        recalculateBrightnessIfNeeded();
        return this.brightness;
    }

    float[] getChannel(int i) {
        switch (i) {
            case 0:
            case 1:
            case 2:
            case 3:
                return this.channels[i];
            case 4:
                recalculateIntensityIfNeeded();
                return this.intensity;
            case 5:
                throw new UnsupportedOperationException("Use getBrightness() instead");
            default:
                throw new IllegalArgumentException("Attempted to retrieve invalid channel: " + i);
        }
    }

    void multiplyChannel(int i, float[] fArr, int i2) {
        float[] fArr2 = this.channels[i];
        for (int i3 = 0; i3 < i2; i3++) {
            fArr2[i3] = fArr2[i3] * fArr[i3];
        }
        dirty();
    }

    void multiplyValue(int i, float f, int i2) {
        float[] fArr = this.channels[i];
        for (int i3 = 0; i3 < i2; i3++) {
            fArr[i3] = fArr[i3] * f;
        }
        dirty();
    }

    void setPixels(int[] iArr, int i) {
        ensureSize(i);
        for (int i2 = 0; i2 < i; i2++) {
            float[][] fArr = this.channels;
            fArr[3][i2] = iArr[i2] & 255;
            fArr[2][i2] = (iArr[i2] >> 8) & 255;
            fArr[1][i2] = (iArr[i2] >> 16) & 255;
            fArr[0][i2] = iArr[i2] >>> 24;
        }
        this.intensityDirty = true;
        this.brightnessDirty = true;
    }

    void setSize(int i) {
        ensureSize(i);
        this.size = i;
    }

    void subtractChannel(int i, float[] fArr, int i2) {
        float[] fArr2 = this.channels[i];
        for (int i3 = 0; i3 < i2; i3++) {
            fArr2[i3] = fArr2[i3] - fArr[i3];
        }
        dirty();
    }

    void subtractValue(int i, float f, int i2) {
        float[] fArr = this.channels[i];
        for (int i3 = 0; i3 < i2; i3++) {
            fArr[i3] = fArr[i3] - f;
        }
        dirty();
    }

    void writeChannel(int i, float[] fArr, int i2) {
        System.arraycopy(fArr, 0, this.channels[i], 0, i2);
        dirty();
    }

    void writeValue(int i, float f, int i2) {
        Arrays.fill(this.channels[i], 0, i2, f);
        dirty();
    }
}
