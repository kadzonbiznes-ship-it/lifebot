/*
 * Decompiled with CFR 0.152.
 */
package java.awt.geom;

import java.awt.Shape;
import java.awt.geom.Path2D;

public final class GeneralPath
extends Path2D.Float {
    private static final long serialVersionUID = -8327096662768731142L;

    public GeneralPath() {
        super(1, 20);
    }

    public GeneralPath(int rule) {
        super(rule, 20);
    }

    public GeneralPath(int rule, int initialCapacity) {
        super(rule, initialCapacity);
    }

    public GeneralPath(Shape s) {
        super(s, null);
    }

    GeneralPath(int windingRule, byte[] pointTypes, int numTypes, float[] pointCoords, int numCoords) {
        this.windingRule = windingRule;
        this.pointTypes = pointTypes;
        this.numTypes = numTypes;
        this.floatCoords = pointCoords;
        this.numCoords = numCoords;
    }
}

