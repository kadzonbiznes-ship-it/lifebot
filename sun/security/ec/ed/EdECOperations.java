/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec.ed;

import java.util.function.Function;
import sun.security.ec.point.AffinePoint;
import sun.security.ec.point.ImmutablePoint;
import sun.security.ec.point.MutablePoint;
import sun.security.ec.point.Point;
import sun.security.util.math.IntegerModuloP;
import sun.security.util.math.MutableIntegerModuloP;

public abstract class EdECOperations {
    public abstract Point basePointMultiply(byte[] var1);

    public abstract <T extends Throwable> AffinePoint decodeAffinePoint(Function<String, T> var1, int var2, IntegerModuloP var3) throws T;

    public abstract ImmutablePoint of(AffinePoint var1);

    public MutablePoint setSum(MutablePoint p1, MutablePoint p2) {
        MutableIntegerModuloP t1 = p2.getField().get1().mutable();
        MutableIntegerModuloP t2 = p2.getField().get1().mutable();
        MutableIntegerModuloP t3 = p2.getField().get1().mutable();
        return this.setSum(p1, p2, t1, t2, t3);
    }

    public MutablePoint setProduct(MutablePoint p1, byte[] s) {
        MutablePoint p = p1.mutable();
        p1.setValue(this.getNeutral());
        MutablePoint addResult = this.getNeutral().mutable();
        MutableIntegerModuloP t1 = p.getField().get0().mutable();
        MutableIntegerModuloP t2 = p.getField().get0().mutable();
        MutableIntegerModuloP t3 = p.getField().get0().mutable();
        for (int i = 0; i < s.length * 8; ++i) {
            addResult.setValue(p1);
            this.setSum(addResult, p, t1, t2, t3);
            int swap = EdECOperations.bitAt(s, i);
            p1.conditionalSet(addResult, swap);
            this.setDouble(p, t1, t2);
        }
        return p1;
    }

    protected abstract ImmutablePoint getNeutral();

    protected abstract MutablePoint setSum(MutablePoint var1, MutablePoint var2, MutableIntegerModuloP var3, MutableIntegerModuloP var4, MutableIntegerModuloP var5);

    protected abstract MutablePoint setDouble(MutablePoint var1, MutableIntegerModuloP var2, MutableIntegerModuloP var3);

    private static int bitAt(byte[] arr, int index) {
        int byteIndex = index / 8;
        int bitIndex = index % 8;
        return (arr[byteIndex] & 1 << bitIndex) >> bitIndex;
    }
}

