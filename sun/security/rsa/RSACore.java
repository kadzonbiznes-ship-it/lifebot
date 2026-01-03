/*
 * Decompiled with CFR 0.152.
 */
package sun.security.rsa;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import javax.crypto.BadPaddingException;
import sun.security.jca.JCAUtil;

public final class RSACore {
    private static final boolean ENABLE_BLINDING = true;
    private static final Map<BigInteger, ConcurrentLinkedQueue<BlindingParameters>> blindingCache = new WeakHashMap<BigInteger, ConcurrentLinkedQueue<BlindingParameters>>();
    private static final ReentrantLock lock = new ReentrantLock();

    private RSACore() {
    }

    public static int getByteLength(BigInteger b) {
        int n = b.bitLength();
        return n + 7 >> 3;
    }

    public static int getByteLength(RSAKey key) {
        return RSACore.getByteLength(key.getModulus());
    }

    public static byte[] convert(byte[] b, int ofs, int len) {
        if (ofs == 0 && len == b.length) {
            return b;
        }
        byte[] t = new byte[len];
        System.arraycopy(b, ofs, t, 0, len);
        return t;
    }

    public static byte[] rsa(byte[] msg, RSAPublicKey key) throws BadPaddingException {
        return RSACore.crypt(msg, key.getModulus(), key.getPublicExponent());
    }

    @Deprecated
    public static byte[] rsa(byte[] msg, RSAPrivateKey key) throws BadPaddingException {
        return RSACore.rsa(msg, key, true);
    }

    public static byte[] rsa(byte[] msg, RSAPrivateKey key, boolean verify) throws BadPaddingException {
        if (key instanceof RSAPrivateCrtKey) {
            return RSACore.crtCrypt(msg, (RSAPrivateCrtKey)key, verify);
        }
        return RSACore.priCrypt(msg, key.getModulus(), key.getPrivateExponent());
    }

    private static byte[] crypt(byte[] msg, BigInteger n, BigInteger exp) throws BadPaddingException {
        BigInteger m = RSACore.parseMsg(msg, n);
        BigInteger c = m.modPow(exp, n);
        return RSACore.toByteArray(c, RSACore.getByteLength(n));
    }

    private static byte[] priCrypt(byte[] msg, BigInteger n, BigInteger exp) throws BadPaddingException {
        BigInteger c = RSACore.parseMsg(msg, n);
        BlindingRandomPair brp = RSACore.getBlindingRandomPair(null, exp, n);
        c = c.multiply(brp.u).mod(n);
        BigInteger m = c.modPow(exp, n);
        m = m.multiply(brp.v).mod(n);
        return RSACore.toByteArray(m, RSACore.getByteLength(n));
    }

    private static byte[] crtCrypt(byte[] msg, RSAPrivateCrtKey key, boolean verify) throws BadPaddingException {
        BigInteger m2;
        BigInteger c0;
        BigInteger n = key.getModulus();
        BigInteger c = c0 = RSACore.parseMsg(msg, n);
        BigInteger p = key.getPrimeP();
        BigInteger q = key.getPrimeQ();
        BigInteger dP = key.getPrimeExponentP();
        BigInteger dQ = key.getPrimeExponentQ();
        BigInteger qInv = key.getCrtCoefficient();
        BigInteger e = key.getPublicExponent();
        BigInteger d = key.getPrivateExponent();
        BlindingRandomPair brp = RSACore.getBlindingRandomPair(e, d, n);
        BigInteger m1 = (c = c.multiply(brp.u).mod(n)).modPow(dP, p);
        BigInteger mtmp = m1.subtract(m2 = c.modPow(dQ, q));
        if (mtmp.signum() < 0) {
            mtmp = mtmp.add(p);
        }
        BigInteger h = mtmp.multiply(qInv).mod(p);
        BigInteger m = h.multiply(q).add(m2);
        m = m.multiply(brp.v).mod(n);
        if (verify && !c0.equals(m.modPow(e, n))) {
            throw new BadPaddingException("RSA private key operation failed");
        }
        return RSACore.toByteArray(m, RSACore.getByteLength(n));
    }

    private static BigInteger parseMsg(byte[] msg, BigInteger n) throws BadPaddingException {
        BigInteger m = new BigInteger(1, msg);
        if (m.compareTo(n) >= 0) {
            throw new BadPaddingException("Message is larger than modulus");
        }
        return m;
    }

    private static byte[] toByteArray(BigInteger bi, int len) {
        byte[] b = bi.toByteArray();
        int n = b.length;
        if (n == len) {
            return b;
        }
        if (n == len + 1 && b[0] == 0) {
            byte[] t = new byte[len];
            System.arraycopy(b, 1, t, 0, len);
            Arrays.fill(b, (byte)0);
            return t;
        }
        assert (n < len);
        byte[] t = new byte[len];
        System.arraycopy(b, 0, t, len - n, n);
        Arrays.fill(b, (byte)0);
        return t;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static BlindingRandomPair getBlindingRandomPair(BigInteger e, BigInteger d, BigInteger n) {
        ConcurrentLinkedQueue queue;
        lock.lock();
        try {
            queue = blindingCache.computeIfAbsent(n, ignored -> new ConcurrentLinkedQueue());
        }
        finally {
            lock.unlock();
        }
        BlindingParameters bps = (BlindingParameters)queue.poll();
        if (bps == null) {
            bps = new BlindingParameters(e, d, n);
        }
        BlindingRandomPair brp = null;
        while (brp == null) {
            brp = bps.getBlindingRandomPair(e, d, n);
            if (brp != null || (bps = (BlindingParameters)queue.poll()) != null) continue;
            bps = new BlindingParameters(e, d, n);
        }
        if (bps.isReusable()) {
            queue.add(bps);
        }
        return brp;
    }

    private static final class BlindingRandomPair {
        final BigInteger u;
        final BigInteger v;

        BlindingRandomPair(BigInteger u, BigInteger v) {
            this.u = u;
            this.v = v;
        }
    }

    private static final class BlindingParameters {
        private static final BigInteger BIG_TWO = BigInteger.valueOf(2L);
        private final BigInteger e;
        private final BigInteger d;
        private BigInteger u = null;
        private BigInteger v = null;

        BlindingParameters(BigInteger e, BigInteger d, BigInteger n) {
            this.e = e;
            this.d = d;
            int len = n.bitLength();
            SecureRandom random = JCAUtil.getSecureRandom();
            this.u = new BigInteger(len, random).mod(n);
            if (this.u.equals(BigInteger.ZERO)) {
                this.u = BigInteger.ONE;
            }
            try {
                this.v = this.u.modInverse(n);
            }
            catch (ArithmeticException ae) {
                this.u = BigInteger.ONE;
                this.v = BigInteger.ONE;
            }
            if (e != null) {
                this.u = this.u.modPow(e, n);
            } else {
                this.v = this.v.modPow(d, n);
            }
        }

        BlindingRandomPair getBlindingRandomPair(BigInteger e, BigInteger d, BigInteger n) {
            if (this.e != null && this.e.equals(e) || this.d != null && this.d.equals(d)) {
                BlindingRandomPair brp = new BlindingRandomPair(this.u, this.v);
                if (this.u.compareTo(BigInteger.ONE) <= 0 || this.v.compareTo(BigInteger.ONE) <= 0) {
                    this.u = BigInteger.ZERO;
                    this.v = BigInteger.ZERO;
                } else {
                    this.u = this.u.modPow(BIG_TWO, n);
                    this.v = this.v.modPow(BIG_TWO, n);
                }
                return brp;
            }
            return null;
        }

        boolean isReusable() {
            return !this.u.equals(BigInteger.ZERO) && !this.v.equals(BigInteger.ZERO);
        }
    }
}

