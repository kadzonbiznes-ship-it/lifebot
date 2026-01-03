/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.marlin;

import sun.java2d.marlin.MarlinConst;

public final class FloatMath
implements MarlinConst {
    static final boolean CHECK_OVERFLOW = true;
    static final boolean CHECK_NAN = true;

    private FloatMath() {
    }

    static int max(int a, int b) {
        return a >= b ? a : b;
    }

    static int min(int a, int b) {
        return a <= b ? a : b;
    }

    public static int ceil_int(double a) {
        int intpart = (int)a;
        if (a <= (double)intpart || intpart == Integer.MAX_VALUE || Double.isNaN(a)) {
            return intpart;
        }
        return intpart + 1;
    }

    public static int floor_int(double a) {
        int intpart = (int)a;
        if (a >= (double)intpart || intpart == Integer.MIN_VALUE || Double.isNaN(a)) {
            return intpart;
        }
        return intpart - 1;
    }
}

