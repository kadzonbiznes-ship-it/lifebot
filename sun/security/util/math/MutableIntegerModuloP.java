/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util.math;

import java.nio.ByteBuffer;
import sun.security.util.math.IntegerModuloP;
import sun.security.util.math.SmallValue;

public interface MutableIntegerModuloP
extends IntegerModuloP {
    public void conditionalSet(IntegerModuloP var1, int var2);

    public void conditionalSwapWith(MutableIntegerModuloP var1, int var2);

    public MutableIntegerModuloP setValue(IntegerModuloP var1);

    public MutableIntegerModuloP setValue(byte[] var1, int var2, int var3, byte var4);

    public MutableIntegerModuloP setValue(ByteBuffer var1, int var2, byte var3);

    public MutableIntegerModuloP setSquare();

    public MutableIntegerModuloP setSum(IntegerModuloP var1);

    public MutableIntegerModuloP setDifference(IntegerModuloP var1);

    public MutableIntegerModuloP setProduct(IntegerModuloP var1);

    public MutableIntegerModuloP setProduct(SmallValue var1);

    public MutableIntegerModuloP setAdditiveInverse();
}

