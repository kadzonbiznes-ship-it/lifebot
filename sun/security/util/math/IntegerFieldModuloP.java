/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util.math;

import java.math.BigInteger;
import sun.security.util.math.ImmutableIntegerModuloP;
import sun.security.util.math.SmallValue;

public interface IntegerFieldModuloP {
    public BigInteger getSize();

    public ImmutableIntegerModuloP get0();

    public ImmutableIntegerModuloP get1();

    public ImmutableIntegerModuloP getElement(BigInteger var1);

    public SmallValue getSmallValue(int var1);

    default public ImmutableIntegerModuloP getElement(byte[] v) {
        return this.getElement(v, 0, v.length, (byte)0);
    }

    public ImmutableIntegerModuloP getElement(byte[] var1, int var2, int var3, byte var4);
}

