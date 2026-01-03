/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.Utilities;
import sun.security.util.ByteArrays;

final class RandomCookie {
    final byte[] randomBytes = new byte[32];
    private static final byte[] hrrRandomBytes = new byte[]{-49, 33, -83, 116, -27, -102, 97, 17, -66, 29, -116, 2, 30, 101, -72, -111, -62, -94, 17, 22, 122, -69, -116, 94, 7, -98, 9, -30, -56, -88, 51, -100};
    private static final byte[] t12Protection = new byte[]{68, 79, 87, 78, 71, 82, 68, 1};
    private static final byte[] t11Protection = new byte[]{68, 79, 87, 78, 71, 82, 68, 0};
    static final RandomCookie hrrRandom = new RandomCookie(hrrRandomBytes);

    RandomCookie(SecureRandom generator) {
        generator.nextBytes(this.randomBytes);
    }

    RandomCookie(HandshakeContext context) {
        SecureRandom generator = context.sslContext.getSecureRandom();
        generator.nextBytes(this.randomBytes);
        byte[] protection = null;
        if (context.maximumActiveProtocol.useTLS13PlusSpec()) {
            if (!context.negotiatedProtocol.useTLS13PlusSpec()) {
                protection = context.negotiatedProtocol.useTLS12PlusSpec() ? t12Protection : t11Protection;
            }
        } else if (context.maximumActiveProtocol.useTLS12PlusSpec() && !context.negotiatedProtocol.useTLS12PlusSpec()) {
            protection = t11Protection;
        }
        if (protection != null) {
            System.arraycopy(protection, 0, this.randomBytes, this.randomBytes.length - protection.length, protection.length);
        }
    }

    RandomCookie(ByteBuffer m) {
        m.get(this.randomBytes);
    }

    private RandomCookie(byte[] randomBytes) {
        System.arraycopy(randomBytes, 0, this.randomBytes, 0, 32);
    }

    public String toString() {
        return "random_bytes = {" + Utilities.toHexString(this.randomBytes) + "}";
    }

    boolean isHelloRetryRequest() {
        return MessageDigest.isEqual(hrrRandomBytes, this.randomBytes);
    }

    boolean isVersionDowngrade(HandshakeContext context) {
        if (context.maximumActiveProtocol.useTLS13PlusSpec()) {
            if (!context.negotiatedProtocol.useTLS13PlusSpec()) {
                return this.isT12Downgrade() || this.isT11Downgrade();
            }
        } else if (context.maximumActiveProtocol.useTLS12PlusSpec() && !context.negotiatedProtocol.useTLS12PlusSpec()) {
            return this.isT11Downgrade();
        }
        return false;
    }

    private boolean isT12Downgrade() {
        return ByteArrays.isEqual(this.randomBytes, 24, 32, t12Protection, 0, 8);
    }

    private boolean isT11Downgrade() {
        return ByteArrays.isEqual(this.randomBytes, 24, 32, t11Protection, 0, 8);
    }
}

