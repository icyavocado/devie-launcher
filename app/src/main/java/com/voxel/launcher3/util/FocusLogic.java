package com.voxel.launcher3.util;

import android.view.View;
import com.voxel.launcher3.CellLayout;
import com.voxel.launcher3.ShortcutAndWidgetContainer;
import java.lang.reflect.Array;
import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
public class FocusLogic {
    private static int[][] createFullMatrix(int i, int i2) {
        int[][] iArr = (int[][]) Array.newInstance((Class<?>) int.class, i, i2);
        for (int i3 = 0; i3 < i; i3++) {
            Arrays.fill(iArr[i3], -1);
        }
        return iArr;
    }

    public static int[][] createSparseMatrix(CellLayout cellLayout) {
        ShortcutAndWidgetContainer shortcutsAndWidgets = cellLayout.getShortcutsAndWidgets();
        int countX = cellLayout.getCountX();
        int countY = cellLayout.getCountY();
        boolean zInvertLayoutHorizontally = shortcutsAndWidgets.invertLayoutHorizontally();
        int[][] iArrCreateFullMatrix = createFullMatrix(countX, countY);
        for (int i = 0; i < shortcutsAndWidgets.getChildCount(); i++) {
            int i2 = ((CellLayout.LayoutParams) shortcutsAndWidgets.getChildAt(i).getLayoutParams()).cellX;
            int i3 = ((CellLayout.LayoutParams) shortcutsAndWidgets.getChildAt(i).getLayoutParams()).cellY;
            if (zInvertLayoutHorizontally) {
                i2 = (countX - i2) - 1;
            }
            iArrCreateFullMatrix[i2][i3] = i;
        }
        return iArrCreateFullMatrix;
    }

    public static int[][] createSparseMatrix(CellLayout cellLayout, int i, int i2) {
        ShortcutAndWidgetContainer shortcutsAndWidgets = cellLayout.getShortcutsAndWidgets();
        int[][] iArrCreateFullMatrix = createFullMatrix(cellLayout.getCountX() + 1, cellLayout.getCountY());
        for (int i3 = 0; i3 < shortcutsAndWidgets.getChildCount(); i3++) {
            int i4 = ((CellLayout.LayoutParams) shortcutsAndWidgets.getChildAt(i3).getLayoutParams()).cellX;
            int i5 = ((CellLayout.LayoutParams) shortcutsAndWidgets.getChildAt(i3).getLayoutParams()).cellY;
            if (i < 0) {
                iArrCreateFullMatrix[i4 - i][i5] = i3;
            } else {
                iArrCreateFullMatrix[i4][i5] = i3;
            }
        }
        if (i < 0) {
            iArrCreateFullMatrix[0][i2] = 100;
        } else {
            iArrCreateFullMatrix[i][i2] = 100;
        }
        return iArrCreateFullMatrix;
    }

    public static int[][] createSparseMatrix(CellLayout cellLayout, CellLayout cellLayout2, boolean z, int i, boolean z2) {
        int countX;
        int countY;
        ShortcutAndWidgetContainer shortcutsAndWidgets = cellLayout.getShortcutsAndWidgets();
        ShortcutAndWidgetContainer shortcutsAndWidgets2 = cellLayout2.getShortcutsAndWidgets();
        if (z) {
            countX = cellLayout.getCountX();
            countY = cellLayout.getCountY() + cellLayout2.getCountY();
        } else {
            countX = cellLayout.getCountX() + cellLayout2.getCountX();
            countY = cellLayout.getCountY();
        }
        int[][] iArrCreateFullMatrix = createFullMatrix(countX, countY);
        for (int i2 = 0; i2 < shortcutsAndWidgets.getChildCount(); i2++) {
            iArrCreateFullMatrix[((CellLayout.LayoutParams) shortcutsAndWidgets.getChildAt(i2).getLayoutParams()).cellX][((CellLayout.LayoutParams) shortcutsAndWidgets.getChildAt(i2).getLayoutParams()).cellY] = i2;
        }
        int childCount = shortcutsAndWidgets2.getChildCount();
        while (true) {
            childCount--;
            if (childCount < (!z2 ? 1 : 0)) {
                return iArrCreateFullMatrix;
            }
            int i3 = -1;
            if (z) {
                int i4 = ((CellLayout.LayoutParams) shortcutsAndWidgets2.getChildAt(childCount).getLayoutParams()).cellX;
                if ((!z2 || i4 < i) && (z2 || i4 <= i)) {
                    i3 = 0;
                }
                iArrCreateFullMatrix[i4 + i3][cellLayout.getCountY()] = shortcutsAndWidgets.getChildCount() + childCount;
            } else {
                int i5 = ((CellLayout.LayoutParams) shortcutsAndWidgets2.getChildAt(childCount).getLayoutParams()).cellY;
                if ((!z2 || i5 < i) && (z2 || i5 <= i)) {
                    i3 = 0;
                }
                iArrCreateFullMatrix[cellLayout.getCountX()][i5 + i3] = shortcutsAndWidgets.getChildCount() + childCount;
            }
        }
    }

    public static View getAdjacentChildInNextPage(ShortcutAndWidgetContainer shortcutAndWidgetContainer, View view, int i) {
        int i2 = ((CellLayout.LayoutParams) view.getLayoutParams()).cellY;
        for (int countX = (i == -9) ^ shortcutAndWidgetContainer.invertLayoutHorizontally() ? 0 : ((CellLayout) shortcutAndWidgetContainer.getParent()).getCountX() - 1; countX >= 0; countX--) {
            for (int i3 = i2; i3 >= 0; i3--) {
                View childAt = shortcutAndWidgetContainer.getChildAt(countX, i3);
                if (childAt != null) {
                    return childAt;
                }
            }
        }
        return null;
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    private static int handleDpadHorizontal(int i, int i2, int i3, int[][] iArr, int i4) {
        if (iArr == null) {
            throw new IllegalStateException("Dpad navigation requires a matrix.");
        }
        int i5 = 0;
        int i6 = -1;
        int i7 = -1;
        while (i5 < i2) {
            int i8 = i7;
            int i9 = i6;
            for (int i10 = 0; i10 < i3; i10++) {
                if (iArr[i5][i10] == i) {
                    i9 = i5;
                    i8 = i10;
                }
            }
            i5++;
            i6 = i9;
            i7 = i8;
        }
        int i11 = i6 + i4;
        int iInspectMatrix = -1;
        while (i11 >= 0 && i11 < i2) {
            iInspectMatrix = inspectMatrix(i11, i7, i2, i3, iArr);
            if (iInspectMatrix != -1) {
                return iInspectMatrix;
            }
            i11 += i4;
        }
        for (int i12 = 1; i12 < i3; i12++) {
            int i13 = i12 * i4;
            int i14 = i7 + i13;
            int i15 = i7 - i13;
            int i16 = i13 + i6;
            while (i16 >= 0 && i16 < i2) {
                int iInspectMatrix2 = inspectMatrix(i16, i14, i2, i3, iArr);
                if (iInspectMatrix2 != -1) {
                    return iInspectMatrix2;
                }
                iInspectMatrix = inspectMatrix(i16, i15, i2, i3, iArr);
                if (iInspectMatrix != -1) {
                    return iInspectMatrix;
                }
                i16 += i4;
            }
        }
        return iInspectMatrix;
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    private static int handleDpadVertical(int i, int i2, int i3, int[][] iArr, int i4) {
        if (iArr == null) {
            throw new IllegalStateException("Dpad navigation requires a matrix.");
        }
        int i5 = 0;
        int i6 = -1;
        int i7 = -1;
        while (i5 < i2) {
            int i8 = i7;
            int i9 = i6;
            for (int i10 = 0; i10 < i3; i10++) {
                if (iArr[i5][i10] == i) {
                    i8 = i5;
                    i9 = i10;
                }
            }
            i5++;
            i6 = i9;
            i7 = i8;
        }
        int i11 = i6 + i4;
        int iInspectMatrix = -1;
        while (i11 >= 0 && i11 < i3 && i11 >= 0) {
            iInspectMatrix = inspectMatrix(i7, i11, i2, i3, iArr);
            if (iInspectMatrix != -1) {
                return iInspectMatrix;
            }
            i11 += i4;
        }
        for (int i12 = 1; i12 < i2; i12++) {
            int i13 = i12 * i4;
            int i14 = i7 + i13;
            int i15 = i7 - i13;
            int i16 = i13 + i6;
            while (i16 >= 0 && i16 < i3) {
                int iInspectMatrix2 = inspectMatrix(i14, i16, i2, i3, iArr);
                if (iInspectMatrix2 != -1) {
                    return iInspectMatrix2;
                }
                iInspectMatrix = inspectMatrix(i15, i16, i2, i3, iArr);
                if (iInspectMatrix != -1) {
                    return iInspectMatrix;
                }
                i16 += i4;
            }
        }
        return iInspectMatrix;
    }

    public static int handleKeyEvent(int i, int i2, int i3, int[][] iArr, int i4, int i5, int i6, boolean z) {
        int iHandleDpadHorizontal;
        switch (i) {
            case 19:
                return handleDpadVertical(i4, i2, i3, iArr, -1);
            case 20:
                return handleDpadVertical(i4, i2, i3, iArr, 1);
            case 21:
                iHandleDpadHorizontal = handleDpadHorizontal(i4, i2, i3, iArr, -1);
                if (z && iHandleDpadHorizontal == -1 && i5 > 0) {
                    return -2;
                }
                if (z && iHandleDpadHorizontal == -1 && i5 < i6 - 1) {
                    return -10;
                }
                break;
            case 22:
                iHandleDpadHorizontal = handleDpadHorizontal(i4, i2, i3, iArr, 1);
                if (z && iHandleDpadHorizontal == -1 && i5 < i6 - 1) {
                    return -9;
                }
                if (z && iHandleDpadHorizontal == -1 && i5 > 0) {
                    return -5;
                }
                break;
            case 92:
                return handlePageUp(i5);
            case 93:
                return handlePageDown(i5, i6);
            case 122:
                return handleMoveHome();
            case 123:
                return handleMoveEnd();
            default:
                return -1;
        }
        return iHandleDpadHorizontal;
    }

    private static int handleMoveEnd() {
        return -7;
    }

    private static int handleMoveHome() {
        return -6;
    }

    private static int handlePageDown(int i, int i2) {
        return i < i2 + (-1) ? -8 : -7;
    }

    private static int handlePageUp(int i) {
        return i > 0 ? -3 : -6;
    }

    private static int inspectMatrix(int i, int i2, int i3, int i4, int[][] iArr) {
        if (!isValid(i, i2, i3, i4) || iArr[i][i2] == -1) {
            return -1;
        }
        return iArr[i][i2];
    }

    private static boolean isValid(int i, int i2, int i3, int i4) {
        return i >= 0 && i < i3 && i2 >= 0 && i2 < i4;
    }

    public static boolean shouldConsume(int i) {
        return i == 21 || i == 22 || i == 19 || i == 20 || i == 122 || i == 123 || i == 92 || i == 93 || i == 67 || i == 112;
    }
}
