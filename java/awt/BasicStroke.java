/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Shape;
import java.awt.Stroke;
import java.beans.ConstructorProperties;
import java.util.Arrays;
import sun.java2d.pipe.RenderingEngine;

public class BasicStroke
implements Stroke {
    public static final int JOIN_MITER = 0;
    public static final int JOIN_ROUND = 1;
    public static final int JOIN_BEVEL = 2;
    public static final int CAP_BUTT = 0;
    public static final int CAP_ROUND = 1;
    public static final int CAP_SQUARE = 2;
    float width;
    int join;
    int cap;
    float miterlimit;
    float[] dash;
    float dash_phase;

    @ConstructorProperties(value={"lineWidth", "endCap", "lineJoin", "miterLimit", "dashArray", "dashPhase"})
    public BasicStroke(float width, int cap, int join, float miterlimit, float[] dash, float dash_phase) {
        if (width < 0.0f) {
            throw new IllegalArgumentException("negative width");
        }
        if (cap != 0 && cap != 1 && cap != 2) {
            throw new IllegalArgumentException("illegal end cap value");
        }
        if (join == 0) {
            if (miterlimit < 1.0f) {
                throw new IllegalArgumentException("miter limit < 1");
            }
        } else if (join != 1 && join != 2) {
            throw new IllegalArgumentException("illegal line join value");
        }
        if (dash != null) {
            if (dash_phase < 0.0f) {
                throw new IllegalArgumentException("negative dash phase");
            }
            boolean allzero = true;
            for (int i = 0; i < dash.length; ++i) {
                float d = dash[i];
                if ((double)d > 0.0) {
                    allzero = false;
                    continue;
                }
                if (!((double)d < 0.0)) continue;
                throw new IllegalArgumentException("negative dash length");
            }
            if (allzero) {
                throw new IllegalArgumentException("dash lengths all zero");
            }
        }
        this.width = width;
        this.cap = cap;
        this.join = join;
        this.miterlimit = miterlimit;
        if (dash != null) {
            this.dash = (float[])dash.clone();
        }
        this.dash_phase = dash_phase;
    }

    public BasicStroke(float width, int cap, int join, float miterlimit) {
        this(width, cap, join, miterlimit, null, 0.0f);
    }

    public BasicStroke(float width, int cap, int join) {
        this(width, cap, join, 10.0f, null, 0.0f);
    }

    public BasicStroke(float width) {
        this(width, 2, 0, 10.0f, null, 0.0f);
    }

    public BasicStroke() {
        this(1.0f, 2, 0, 10.0f, null, 0.0f);
    }

    @Override
    public Shape createStrokedShape(Shape s) {
        RenderingEngine re = RenderingEngine.getInstance();
        return re.createStrokedShape(s, this.width, this.cap, this.join, this.miterlimit, this.dash, this.dash_phase);
    }

    public float getLineWidth() {
        return this.width;
    }

    public int getEndCap() {
        return this.cap;
    }

    public int getLineJoin() {
        return this.join;
    }

    public float getMiterLimit() {
        return this.miterlimit;
    }

    public float[] getDashArray() {
        if (this.dash == null) {
            return null;
        }
        return (float[])this.dash.clone();
    }

    public float getDashPhase() {
        return this.dash_phase;
    }

    public int hashCode() {
        int hash = Float.floatToIntBits(this.width);
        hash = hash * 31 + this.join;
        hash = hash * 31 + this.cap;
        hash = hash * 31 + Float.floatToIntBits(this.miterlimit);
        if (this.dash != null) {
            hash = hash * 31 + Float.floatToIntBits(this.dash_phase);
            for (int i = 0; i < this.dash.length; ++i) {
                hash = hash * 31 + Float.floatToIntBits(this.dash[i]);
            }
        }
        return hash;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof BasicStroke)) {
            return false;
        }
        BasicStroke bs = (BasicStroke)obj;
        if (this.width != bs.width) {
            return false;
        }
        if (this.join != bs.join) {
            return false;
        }
        if (this.cap != bs.cap) {
            return false;
        }
        if (this.miterlimit != bs.miterlimit) {
            return false;
        }
        if (this.dash != null) {
            if (this.dash_phase != bs.dash_phase) {
                return false;
            }
            if (!Arrays.equals(this.dash, bs.dash)) {
                return false;
            }
        } else if (bs.dash != null) {
            return false;
        }
        return true;
    }
}

