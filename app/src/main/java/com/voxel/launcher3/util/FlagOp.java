package com.voxel.launcher3.util;

/* JADX INFO: loaded from: classes.dex */
public abstract class FlagOp {
    public static FlagOp NO_OP = new FlagOp() { // from class: com.voxel.launcher3.util.FlagOp.1
    };

    private FlagOp() {
    }

    public static FlagOp addFlag(final int i) {
        return new FlagOp() { // from class: com.voxel.launcher3.util.FlagOp.2

            @Override // com.voxel.launcher3.util.FlagOp
            public int apply(int i2) {
                return i2 | i;
            }
        };
    }

    public static FlagOp removeFlag(final int i) {
        return new FlagOp() { // from class: com.voxel.launcher3.util.FlagOp.3

            @Override // com.voxel.launcher3.util.FlagOp
            public int apply(int i2) {
                return i2 & (i ^ (-1));
            }
        };
    }

    public int apply(int i) {
        return i;
    }
}
