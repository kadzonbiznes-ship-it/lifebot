/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.Serializable;

public class Insets
implements Cloneable,
Serializable {
    public int top;
    public int left;
    public int bottom;
    public int right;
    private static final long serialVersionUID = -2272572637695466749L;

    public Insets(int top, int left, int bottom, int right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    public void set(int top, int left, int bottom, int right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Insets) {
            Insets insets = (Insets)obj;
            return this.top == insets.top && this.left == insets.left && this.bottom == insets.bottom && this.right == insets.right;
        }
        return false;
    }

    public int hashCode() {
        int sum1 = this.left + this.bottom;
        int sum2 = this.right + this.top;
        int val1 = sum1 * (sum1 + 1) / 2 + this.left;
        int val2 = sum2 * (sum2 + 1) / 2 + this.top;
        int sum3 = val1 + val2;
        return sum3 * (sum3 + 1) / 2 + val2;
    }

    public String toString() {
        return this.getClass().getName() + "[top=" + this.top + ",left=" + this.left + ",bottom=" + this.bottom + ",right=" + this.right + "]";
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    private static native void initIDs();

    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            Insets.initIDs();
        }
    }
}

