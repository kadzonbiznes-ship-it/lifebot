/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util.math.intpoly;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import sun.security.util.math.ImmutableIntegerModuloP;
import sun.security.util.math.IntegerFieldModuloP;
import sun.security.util.math.IntegerModuloP;
import sun.security.util.math.MutableIntegerModuloP;
import sun.security.util.math.SmallValue;
import sun.security.util.math.intpoly.Curve25519OrderField;
import sun.security.util.math.intpoly.Curve448OrderField;
import sun.security.util.math.intpoly.IntegerPolynomial1305;
import sun.security.util.math.intpoly.IntegerPolynomial25519;
import sun.security.util.math.intpoly.IntegerPolynomial448;
import sun.security.util.math.intpoly.IntegerPolynomialModBinP;
import sun.security.util.math.intpoly.IntegerPolynomialP256;
import sun.security.util.math.intpoly.IntegerPolynomialP384;
import sun.security.util.math.intpoly.IntegerPolynomialP521;
import sun.security.util.math.intpoly.P256OrderField;
import sun.security.util.math.intpoly.P384OrderField;
import sun.security.util.math.intpoly.P521OrderField;

public abstract sealed class IntegerPolynomial
implements IntegerFieldModuloP
permits IntegerPolynomial1305, IntegerPolynomial25519, IntegerPolynomial448, IntegerPolynomialP256, IntegerPolynomialP384, IntegerPolynomialP521, IntegerPolynomialModBinP, P256OrderField, P384OrderField, P521OrderField, Curve25519OrderField, Curve448OrderField {
    protected static final BigInteger TWO = BigInteger.valueOf(2L);
    protected final int numLimbs;
    private final BigInteger modulus;
    protected final int bitsPerLimb;
    private final long[] posModLimbs;
    private final int maxAdds;

    protected abstract void reduce(long[] var1);

    protected void multByInt(long[] a, long b) {
        int i = 0;
        while (i < a.length) {
            int n = i++;
            a[n] = a[n] * b;
        }
        this.reduce(a);
    }

    protected abstract void mult(long[] var1, long[] var2, long[] var3);

    protected abstract void square(long[] var1, long[] var2);

    IntegerPolynomial(int bitsPerLimb, int numLimbs, int maxAdds, BigInteger modulus) {
        this.numLimbs = numLimbs;
        this.modulus = modulus;
        this.bitsPerLimb = bitsPerLimb;
        this.maxAdds = maxAdds;
        this.posModLimbs = this.setPosModLimbs();
    }

    private long[] setPosModLimbs() {
        long[] result = new long[this.numLimbs];
        this.setLimbsValuePositive(this.modulus, result);
        return result;
    }

    protected int getNumLimbs() {
        return this.numLimbs;
    }

    public int getMaxAdds() {
        return this.maxAdds;
    }

    @Override
    public BigInteger getSize() {
        return this.modulus;
    }

    @Override
    public ImmutableElement get0() {
        return new ImmutableElement(this, false);
    }

    @Override
    public ImmutableElement get1() {
        return new ImmutableElement(this, true);
    }

    @Override
    public ImmutableElement getElement(BigInteger v) {
        return new ImmutableElement(this, v);
    }

    @Override
    public SmallValue getSmallValue(int value) {
        int maxMag = 1 << this.bitsPerLimb - 1;
        if (Math.abs(value) >= maxMag) {
            throw new IllegalArgumentException("max magnitude is " + maxMag);
        }
        return new Limb(value);
    }

    protected abstract void reduceIn(long[] var1, long var2, int var4);

    private void reduceHigh(long[] limbs) {
        int extraBits = 63 - 2 * this.bitsPerLimb;
        int allowedAdds = 1 << extraBits;
        int carryPeriod = allowedAdds / this.numLimbs;
        int reduceCount = 0;
        for (int i = limbs.length - 1; i >= this.numLimbs; --i) {
            this.reduceIn(limbs, limbs[i], i);
            limbs[i] = 0L;
            if (++reduceCount % carryPeriod != 0) continue;
            this.carry(limbs, 0, i);
            this.reduceIn(limbs, limbs[i], i);
            limbs[i] = 0L;
        }
    }

    protected void encode(ByteBuffer buf, int length, byte highByte, long[] result) {
        int numHighBits = 32 - Integer.numberOfLeadingZeros(highByte);
        int numBits = 8 * length + numHighBits;
        int requiredLimbs = (numBits + this.bitsPerLimb - 1) / this.bitsPerLimb;
        if (requiredLimbs > this.numLimbs) {
            long[] temp = new long[requiredLimbs];
            this.encodeSmall(buf, length, highByte, temp);
            this.reduceHigh(temp);
            System.arraycopy(temp, 0, result, 0, result.length);
            this.reduce(result);
        } else {
            this.encodeSmall(buf, length, highByte, result);
            this.postEncodeCarry(result);
        }
    }

    protected void encodeSmall(ByteBuffer buf, int length, byte highByte, long[] result) {
        int limbIndex = 0;
        long curLimbValue = 0L;
        int bitPos = 0;
        for (int i = 0; i < length; ++i) {
            long curV = buf.get() & 0xFF;
            if (bitPos + 8 >= this.bitsPerLimb) {
                int bitsThisLimb = this.bitsPerLimb - bitPos;
                result[limbIndex++] = curLimbValue += (curV & (long)(255 >> 8 - bitsThisLimb)) << bitPos;
                curLimbValue = curV >> bitsThisLimb;
                bitPos = 8 - bitsThisLimb;
                continue;
            }
            curLimbValue += curV << bitPos;
            bitPos += 8;
        }
        if (highByte != 0) {
            long curV = highByte & 0xFF;
            if (bitPos + 8 >= this.bitsPerLimb) {
                int bitsThisLimb = this.bitsPerLimb - bitPos;
                result[limbIndex++] = curLimbValue += (curV & (long)(255 >> 8 - bitsThisLimb)) << bitPos;
                curLimbValue = curV >> bitsThisLimb;
            } else {
                curLimbValue += curV << bitPos;
            }
        }
        if (limbIndex < result.length) {
            result[limbIndex++] = curLimbValue;
        }
        Arrays.fill(result, limbIndex, result.length, 0L);
    }

    protected void encode(byte[] v, int offset, int length, byte highByte, long[] result) {
        ByteBuffer buf = ByteBuffer.wrap(v, offset, length);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        this.encode(buf, length, highByte, result);
    }

    protected void postEncodeCarry(long[] v) {
        this.reduce(v);
    }

    @Override
    public ImmutableElement getElement(byte[] v, int offset, int length, byte highByte) {
        long[] result = new long[this.numLimbs];
        this.encode(v, offset, length, highByte, result);
        return new ImmutableElement(this, result, 0);
    }

    protected BigInteger evaluate(long[] limbs) {
        BigInteger result = BigInteger.ZERO;
        for (int i = limbs.length - 1; i >= 0; --i) {
            result = result.shiftLeft(this.bitsPerLimb).add(BigInteger.valueOf(limbs[i]));
        }
        return result.mod(this.modulus);
    }

    protected long carryValue(long x) {
        return x + (long)(1 << this.bitsPerLimb - 1) >> this.bitsPerLimb;
    }

    protected void carry(long[] limbs, int start, int end) {
        for (int i = start; i < end; ++i) {
            long carry = this.carryOut(limbs, i);
            int n = i + 1;
            limbs[n] = limbs[n] + carry;
        }
    }

    protected void carry(long[] limbs) {
        this.carry(limbs, 0, limbs.length - 1);
    }

    protected long carryOut(long[] limbs, int index) {
        long carry = this.carryValue(limbs[index]);
        int n = index;
        limbs[n] = limbs[n] - (carry << this.bitsPerLimb);
        return carry;
    }

    private void setLimbsValue(BigInteger v, long[] limbs) {
        this.setLimbsValuePositive(v, limbs);
        this.carry(limbs);
    }

    protected void setLimbsValuePositive(BigInteger v, long[] limbs) {
        assert (this.bitsPerLimb < 32);
        long limbMask = (1L << this.bitsPerLimb) - 1L;
        for (int i = 0; i < limbs.length; ++i) {
            limbs[i] = (long)v.intValue() & limbMask;
            v = v.shiftRight(this.bitsPerLimb);
        }
    }

    protected abstract void finalCarryReduceLast(long[] var1);

    protected void finalReduce(long[] limbs) {
        for (int pass = 0; pass < 2; ++pass) {
            this.finalCarryReduceLast(limbs);
            long carry = 0L;
            int i = 0;
            while (i < this.numLimbs - 1) {
                int n = i;
                limbs[n] = limbs[n] + carry;
                carry = limbs[i] >> this.bitsPerLimb;
                int n2 = i++;
                limbs[n2] = limbs[n2] - (carry << this.bitsPerLimb);
            }
            int n = this.numLimbs - 1;
            limbs[n] = limbs[n] + carry;
        }
        int smallerNonNegative = 1;
        long[] smaller = new long[this.numLimbs];
        for (int i = this.numLimbs - 1; i >= 0; --i) {
            smaller[i] = limbs[i] - this.posModLimbs[i];
            smallerNonNegative *= (int)(smaller[i] >> 63) + 1;
        }
        IntegerPolynomial.conditionalSwap(smallerNonNegative, limbs, smaller);
    }

    protected void decode(long[] v, byte[] dst, int offset, int length) {
        int nextLimbIndex = 0;
        long curLimbValue = v[nextLimbIndex++];
        int bitPos = 0;
        for (int i = 0; i < length; ++i) {
            int dstIndex = i + offset;
            if (bitPos + 8 >= this.bitsPerLimb) {
                dst[dstIndex] = (byte)curLimbValue;
                curLimbValue = 0L;
                if (nextLimbIndex < v.length) {
                    curLimbValue = v[nextLimbIndex++];
                }
                int bitsAdded = this.bitsPerLimb - bitPos;
                int bitsLeft = 8 - bitsAdded;
                int n = dstIndex;
                dst[n] = (byte)(dst[n] + (byte)((curLimbValue & (long)(255 >> bitsAdded)) << bitsAdded));
                curLimbValue >>= bitsLeft;
                bitPos = bitsLeft;
                continue;
            }
            dst[dstIndex] = (byte)curLimbValue;
            curLimbValue >>= 8;
            bitPos += 8;
        }
    }

    protected void addLimbs(long[] a, long[] b, long[] dst) {
        for (int i = 0; i < dst.length; ++i) {
            dst[i] = a[i] + b[i];
        }
    }

    protected static void conditionalAssign(int set, long[] a, long[] b) {
        int maskValue = -set;
        for (int i = 0; i < a.length; ++i) {
            long dummyLimbs = (long)maskValue & (a[i] ^ b[i]);
            a[i] = dummyLimbs ^ a[i];
        }
    }

    protected static void conditionalSwap(int swap, long[] a, long[] b) {
        int maskValue = -swap;
        for (int i = 0; i < a.length; ++i) {
            long dummyLimbs = (long)maskValue & (a[i] ^ b[i]);
            a[i] = dummyLimbs ^ a[i];
            b[i] = dummyLimbs ^ b[i];
        }
    }

    protected void limbsToByteArray(long[] limbs, byte[] result) {
        long[] reducedLimbs = (long[])limbs.clone();
        this.finalReduce(reducedLimbs);
        this.decode(reducedLimbs, result, 0, result.length);
    }

    protected void addLimbsModPowerTwo(long[] limbs, long[] other, byte[] result) {
        long[] reducedOther = (long[])other.clone();
        long[] reducedLimbs = (long[])limbs.clone();
        this.finalReduce(reducedOther);
        this.finalReduce(reducedLimbs);
        this.addLimbs(reducedLimbs, reducedOther, reducedLimbs);
        long carry = 0L;
        int i = 0;
        while (i < this.numLimbs) {
            int n = i;
            reducedLimbs[n] = reducedLimbs[n] + carry;
            carry = reducedLimbs[i] >> this.bitsPerLimb;
            int n2 = i++;
            reducedLimbs[n2] = reducedLimbs[n2] - (carry << this.bitsPerLimb);
        }
        this.decode(reducedLimbs, result, 0, result.length);
    }

    class ImmutableElement
    extends Element
    implements ImmutableIntegerModuloP {
        protected ImmutableElement(IntegerPolynomial this$0, BigInteger v) {
            super(v);
        }

        protected ImmutableElement(IntegerPolynomial this$0, boolean v) {
            super(v);
        }

        protected ImmutableElement(IntegerPolynomial this$0, long[] limbs, int numAdds) {
            super(limbs, numAdds);
        }

        @Override
        public ImmutableElement fixed() {
            return this;
        }
    }

    static class Limb
    implements SmallValue {
        int value;

        Limb(int value) {
            this.value = value;
        }
    }

    protected class MutableElement
    extends Element
    implements MutableIntegerModuloP {
        protected MutableElement(long[] limbs, int numAdds) {
            super(limbs, numAdds);
        }

        @Override
        public ImmutableElement fixed() {
            return new ImmutableElement(IntegerPolynomial.this, (long[])this.limbs.clone(), this.numAdds);
        }

        @Override
        public void conditionalSet(IntegerModuloP b, int set) {
            assert (IntegerPolynomial.this == b.getField());
            Element other = (Element)b;
            IntegerPolynomial.conditionalAssign(set, this.limbs, other.limbs);
            this.numAdds = other.numAdds;
        }

        @Override
        public void conditionalSwapWith(MutableIntegerModuloP b, int swap) {
            assert (IntegerPolynomial.this == b.getField());
            MutableElement other = (MutableElement)b;
            IntegerPolynomial.conditionalSwap(swap, this.limbs, other.limbs);
            int numAddsTemp = this.numAdds;
            this.numAdds = other.numAdds;
            other.numAdds = numAddsTemp;
        }

        @Override
        public MutableElement setValue(IntegerModuloP v) {
            assert (IntegerPolynomial.this == v.getField());
            Element other = (Element)v;
            System.arraycopy(other.limbs, 0, this.limbs, 0, other.limbs.length);
            this.numAdds = other.numAdds;
            return this;
        }

        @Override
        public MutableElement setValue(byte[] arr, int offset, int length, byte highByte) {
            IntegerPolynomial.this.encode(arr, offset, length, highByte, this.limbs);
            this.numAdds = 0;
            return this;
        }

        @Override
        public MutableElement setValue(ByteBuffer buf, int length, byte highByte) {
            IntegerPolynomial.this.encode(buf, length, highByte, this.limbs);
            this.numAdds = 0;
            return this;
        }

        @Override
        public MutableElement setProduct(IntegerModuloP genB) {
            assert (IntegerPolynomial.this == genB.getField());
            Element b = (Element)genB;
            if (this.numAdds > IntegerPolynomial.this.maxAdds) {
                IntegerPolynomial.this.reduce(this.limbs);
                this.numAdds = 0;
            }
            if (b.numAdds > IntegerPolynomial.this.maxAdds) {
                IntegerPolynomial.this.reduce(b.limbs);
                b.numAdds = 0;
            }
            IntegerPolynomial.this.mult(this.limbs, b.limbs, this.limbs);
            this.numAdds = 0;
            return this;
        }

        @Override
        public MutableElement setProduct(SmallValue v) {
            if (this.numAdds > IntegerPolynomial.this.maxAdds) {
                IntegerPolynomial.this.reduce(this.limbs);
                this.numAdds = 0;
            }
            int value = ((Limb)v).value;
            IntegerPolynomial.this.multByInt(this.limbs, value);
            this.numAdds = 0;
            return this;
        }

        @Override
        public MutableElement setSum(IntegerModuloP genB) {
            assert (IntegerPolynomial.this == genB.getField());
            Element b = (Element)genB;
            if (this.numAdds > 32 - IntegerPolynomial.this.bitsPerLimb) {
                IntegerPolynomial.this.reduce(this.limbs);
                this.numAdds = 0;
            }
            if (b.numAdds > 32 - IntegerPolynomial.this.bitsPerLimb) {
                IntegerPolynomial.this.reduce(b.limbs);
                b.numAdds = 0;
            }
            for (int i = 0; i < this.limbs.length; ++i) {
                this.limbs[i] = this.limbs[i] + b.limbs[i];
            }
            this.numAdds = Math.max(this.numAdds, b.numAdds) + 1;
            return this;
        }

        @Override
        public MutableElement setDifference(IntegerModuloP genB) {
            assert (IntegerPolynomial.this == genB.getField());
            Element b = (Element)genB;
            if (this.numAdds > 32 - IntegerPolynomial.this.bitsPerLimb) {
                IntegerPolynomial.this.reduce(this.limbs);
                this.numAdds = 0;
            }
            if (b.numAdds > 32 - IntegerPolynomial.this.bitsPerLimb) {
                IntegerPolynomial.this.reduce(b.limbs);
                b.numAdds = 0;
            }
            for (int i = 0; i < this.limbs.length; ++i) {
                this.limbs[i] = this.limbs[i] - b.limbs[i];
            }
            this.numAdds = Math.max(this.numAdds, b.numAdds) + 1;
            return this;
        }

        @Override
        public MutableElement setSquare() {
            if (this.numAdds > IntegerPolynomial.this.maxAdds) {
                IntegerPolynomial.this.reduce(this.limbs);
                this.numAdds = 0;
            }
            IntegerPolynomial.this.square(this.limbs, this.limbs);
            this.numAdds = 0;
            return this;
        }

        @Override
        public MutableElement setAdditiveInverse() {
            for (int i = 0; i < this.limbs.length; ++i) {
                this.limbs[i] = -this.limbs[i];
            }
            return this;
        }
    }

    private abstract class Element
    implements IntegerModuloP {
        protected long[] limbs;
        protected int numAdds;

        public Element(BigInteger v) {
            this.limbs = new long[IntegerPolynomial.this.numLimbs];
            this.setValue(v);
        }

        public Element(boolean v) {
            this.limbs = new long[IntegerPolynomial.this.numLimbs];
            this.limbs[0] = v ? 1L : 0L;
            this.numAdds = 0;
        }

        private Element(long[] limbs, int numAdds) {
            this.limbs = limbs;
            this.numAdds = numAdds;
        }

        private void setValue(BigInteger v) {
            IntegerPolynomial.this.setLimbsValue(v, this.limbs);
            this.numAdds = 0;
        }

        @Override
        public IntegerFieldModuloP getField() {
            return IntegerPolynomial.this;
        }

        @Override
        public BigInteger asBigInteger() {
            return IntegerPolynomial.this.evaluate(this.limbs);
        }

        @Override
        public MutableElement mutable() {
            return new MutableElement((long[])this.limbs.clone(), this.numAdds);
        }

        @Override
        public ImmutableElement add(IntegerModuloP genB) {
            assert (IntegerPolynomial.this == genB.getField());
            Element b = (Element)genB;
            if (this.numAdds > 32 - IntegerPolynomial.this.bitsPerLimb) {
                IntegerPolynomial.this.reduce(this.limbs);
                this.numAdds = 0;
            }
            if (b.numAdds > 32 - IntegerPolynomial.this.bitsPerLimb) {
                IntegerPolynomial.this.reduce(b.limbs);
                b.numAdds = 0;
            }
            long[] newLimbs = new long[this.limbs.length];
            for (int i = 0; i < this.limbs.length; ++i) {
                newLimbs[i] = this.limbs[i] + b.limbs[i];
            }
            int newNumAdds = Math.max(this.numAdds, b.numAdds) + 1;
            return new ImmutableElement(IntegerPolynomial.this, newLimbs, newNumAdds);
        }

        @Override
        public ImmutableElement additiveInverse() {
            long[] newLimbs = new long[this.limbs.length];
            for (int i = 0; i < this.limbs.length; ++i) {
                newLimbs[i] = -this.limbs[i];
            }
            return new ImmutableElement(IntegerPolynomial.this, newLimbs, this.numAdds);
        }

        protected long[] cloneLow(long[] limbs) {
            long[] newLimbs = new long[IntegerPolynomial.this.numLimbs];
            this.copyLow(limbs, newLimbs);
            return newLimbs;
        }

        protected void copyLow(long[] limbs, long[] out) {
            System.arraycopy(limbs, 0, out, 0, out.length);
        }

        @Override
        public ImmutableElement multiply(IntegerModuloP genB) {
            assert (IntegerPolynomial.this == genB.getField());
            Element b = (Element)genB;
            if (this.numAdds > IntegerPolynomial.this.maxAdds) {
                IntegerPolynomial.this.reduce(this.limbs);
                this.numAdds = 0;
            }
            if (b.numAdds > IntegerPolynomial.this.maxAdds) {
                IntegerPolynomial.this.reduce(b.limbs);
                b.numAdds = 0;
            }
            long[] newLimbs = new long[this.limbs.length];
            IntegerPolynomial.this.mult(this.limbs, b.limbs, newLimbs);
            return new ImmutableElement(IntegerPolynomial.this, newLimbs, 0);
        }

        @Override
        public ImmutableElement square() {
            if (this.numAdds > IntegerPolynomial.this.maxAdds) {
                IntegerPolynomial.this.reduce(this.limbs);
                this.numAdds = 0;
            }
            long[] newLimbs = new long[this.limbs.length];
            IntegerPolynomial.this.square(this.limbs, newLimbs);
            return new ImmutableElement(IntegerPolynomial.this, newLimbs, 0);
        }

        @Override
        public void addModPowerTwo(IntegerModuloP arg, byte[] result) {
            assert (IntegerPolynomial.this == arg.getField());
            Element other = (Element)arg;
            if (this.numAdds > 32 - IntegerPolynomial.this.bitsPerLimb) {
                IntegerPolynomial.this.reduce(this.limbs);
                this.numAdds = 0;
            }
            if (other.numAdds > 32 - IntegerPolynomial.this.bitsPerLimb) {
                IntegerPolynomial.this.reduce(other.limbs);
                other.numAdds = 0;
            }
            IntegerPolynomial.this.addLimbsModPowerTwo(this.limbs, other.limbs, result);
        }

        @Override
        public void asByteArray(byte[] result) {
            if (this.numAdds != 0) {
                IntegerPolynomial.this.reduce(this.limbs);
                this.numAdds = 0;
            }
            IntegerPolynomial.this.limbsToByteArray(this.limbs, result);
        }

        @Override
        public long[] getLimbs() {
            return this.limbs;
        }
    }
}

