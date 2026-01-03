/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Component;
import java.io.Serializable;

public final class GridBagLayoutInfo
implements Serializable {
    private static final long serialVersionUID = -4899416460737170217L;
    int width;
    int height;
    int startx;
    int starty;
    int[] minWidth;
    int[] minHeight;
    double[] weightX;
    double[] weightY;
    boolean hasBaseline;
    short[] baselineType;
    int[] maxAscent;
    int[] maxDescent;

    GridBagLayoutInfo(int width, int height) {
        this.width = width;
        this.height = height;
    }

    boolean hasConstantDescent(int row) {
        return (this.baselineType[row] & 1 << Component.BaselineResizeBehavior.CONSTANT_DESCENT.ordinal()) != 0;
    }

    boolean hasBaseline(int row) {
        return this.hasBaseline && this.baselineType[row] != 0;
    }
}

