package com.evie.common.iconpack.shader;

/* JADX INFO: loaded from: classes.dex */
public final class ShaderInstruction {
    public final boolean hasValue;
    public final int inputChannel;
    public final int inputRegister;
    public final int mode;
    public final int targetChannel;
    public final int targetRegister;
    public final float value;

    public ShaderInstruction(int i, int i2, int i3, int i4, int i5, boolean z, float f) {
        this.targetRegister = i;
        this.targetChannel = i2;
        this.mode = i3;
        this.inputRegister = i4;
        this.inputChannel = i5;
        this.hasValue = z;
        this.value = f;
    }

    public boolean verify() {
        int i;
        int i2;
        int i3;
        int i4;
        int i5 = this.targetRegister;
        if ((i5 == 1 || i5 == 2) && (i = this.targetRegister) >= 0 && i <= 3 && (i2 = this.mode) >= 0 && i2 <= 4) {
            return this.hasValue || ((i3 = this.inputRegister) >= 0 && i3 <= 2 && (i4 = this.inputChannel) >= 0 && i4 <= 5);
        }
        return false;
    }
}
