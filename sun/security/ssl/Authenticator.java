/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import sun.security.ssl.CipherSuite;
import sun.security.ssl.ProtocolVersion;

abstract class Authenticator {
    protected final byte[] block;

    private Authenticator(byte[] block) {
        this.block = block;
    }

    static Authenticator valueOf(ProtocolVersion protocolVersion) {
        if (protocolVersion.isDTLS) {
            if (protocolVersion.useTLS13PlusSpec()) {
                return new DTLS13Authenticator(protocolVersion);
            }
            return new DTLS10Authenticator(protocolVersion);
        }
        if (protocolVersion.useTLS13PlusSpec()) {
            return new TLS13Authenticator(protocolVersion);
        }
        if (protocolVersion.useTLS10PlusSpec()) {
            return new TLS10Authenticator(protocolVersion);
        }
        return new SSL30Authenticator();
    }

    static <T extends Authenticator> T valueOf(ProtocolVersion protocolVersion, CipherSuite.MacAlg macAlg, SecretKey key) throws NoSuchAlgorithmException, InvalidKeyException {
        if (protocolVersion.isDTLS) {
            if (protocolVersion.useTLS13PlusSpec()) {
                throw new RuntimeException("No MacAlg used in DTLS 1.3");
            }
            return (T)new DTLS10Mac(protocolVersion, macAlg, key);
        }
        if (protocolVersion.useTLS13PlusSpec()) {
            throw new RuntimeException("No MacAlg used in TLS 1.3");
        }
        if (protocolVersion.useTLS10PlusSpec()) {
            return (T)new TLS10Mac(protocolVersion, macAlg, key);
        }
        return (T)new SSL30Mac(protocolVersion, macAlg, key);
    }

    static Authenticator nullTlsMac() {
        return new SSLNullMac();
    }

    static Authenticator nullDtlsMac() {
        return new DTLSNullMac();
    }

    abstract boolean seqNumOverflow();

    abstract boolean seqNumIsHuge();

    final byte[] sequenceNumber() {
        return Arrays.copyOf(this.block, 8);
    }

    void setEpochNumber(int epoch) {
        throw new UnsupportedOperationException("Epoch numbers apply to DTLS protocols only");
    }

    final void increaseSequenceNumber() {
        int k = 7;
        while (k >= 0) {
            int n = k--;
            this.block[n] = (byte)(this.block[n] + 1);
            if (this.block[n] == 0) continue;
        }
    }

    byte[] acquireAuthenticationBytes(byte type, int length, byte[] sequence) {
        throw new UnsupportedOperationException("Used by AEAD algorithms only");
    }

    static final long toLong(byte[] recordEnS) {
        if (recordEnS != null && recordEnS.length == 8) {
            return ((long)recordEnS[0] & 0xFFL) << 56 | ((long)recordEnS[1] & 0xFFL) << 48 | ((long)recordEnS[2] & 0xFFL) << 40 | ((long)recordEnS[3] & 0xFFL) << 32 | ((long)recordEnS[4] & 0xFFL) << 24 | ((long)recordEnS[5] & 0xFFL) << 16 | ((long)recordEnS[6] & 0xFFL) << 8 | (long)recordEnS[7] & 0xFFL;
        }
        return -1L;
    }

    private static final class DTLS13Authenticator
    extends DTLSAuthenticator {
        private static final int BLOCK_SIZE = 13;

        private DTLS13Authenticator(ProtocolVersion protocolVersion) {
            super(new byte[13]);
            this.block[9] = ProtocolVersion.TLS12.major;
            this.block[10] = ProtocolVersion.TLS12.minor;
        }

        @Override
        byte[] acquireAuthenticationBytes(byte type, int length, byte[] sequence) {
            byte[] ad = Arrays.copyOfRange(this.block, 8, 13);
            this.increaseSequenceNumber();
            ad[0] = type;
            ad[3] = (byte)(length >> 8);
            ad[4] = (byte)(length & 0xFF);
            return ad;
        }
    }

    private static class DTLS10Authenticator
    extends DTLSAuthenticator {
        private static final int BLOCK_SIZE = 13;

        private DTLS10Authenticator(ProtocolVersion protocolVersion) {
            super(new byte[13]);
            this.block[9] = protocolVersion.major;
            this.block[10] = protocolVersion.minor;
        }

        @Override
        byte[] acquireAuthenticationBytes(byte type, int length, byte[] sequence) {
            byte[] ad = (byte[])this.block.clone();
            if (sequence != null) {
                if (sequence.length != 8) {
                    throw new RuntimeException("Insufficient explicit sequence number bytes");
                }
                System.arraycopy(sequence, 0, ad, 0, sequence.length);
            } else {
                this.increaseSequenceNumber();
            }
            ad[8] = type;
            ad[11] = (byte)(length >> 8);
            ad[12] = (byte)length;
            return ad;
        }
    }

    private static final class TLS13Authenticator
    extends SSLAuthenticator {
        private static final int BLOCK_SIZE = 13;

        private TLS13Authenticator(ProtocolVersion protocolVersion) {
            super(new byte[13]);
            this.block[9] = ProtocolVersion.TLS12.major;
            this.block[10] = ProtocolVersion.TLS12.minor;
        }

        @Override
        byte[] acquireAuthenticationBytes(byte type, int length, byte[] sequence) {
            byte[] ad = Arrays.copyOfRange(this.block, 8, 13);
            this.increaseSequenceNumber();
            ad[0] = type;
            ad[3] = (byte)(length >> 8);
            ad[4] = (byte)(length & 0xFF);
            return ad;
        }
    }

    private static class TLS10Authenticator
    extends SSLAuthenticator {
        private static final int BLOCK_SIZE = 13;

        private TLS10Authenticator(ProtocolVersion protocolVersion) {
            super(new byte[13]);
            this.block[9] = protocolVersion.major;
            this.block[10] = protocolVersion.minor;
        }

        @Override
        byte[] acquireAuthenticationBytes(byte type, int length, byte[] sequence) {
            byte[] ad = (byte[])this.block.clone();
            if (sequence != null) {
                if (sequence.length != 8) {
                    throw new RuntimeException("Insufficient explicit sequence number bytes");
                }
                System.arraycopy(sequence, 0, ad, 0, sequence.length);
            } else {
                this.increaseSequenceNumber();
            }
            ad[8] = type;
            ad[11] = (byte)(length >> 8);
            ad[12] = (byte)length;
            return ad;
        }
    }

    private static class SSL30Authenticator
    extends SSLAuthenticator {
        private static final int BLOCK_SIZE = 11;

        private SSL30Authenticator() {
            super(new byte[11]);
        }

        @Override
        byte[] acquireAuthenticationBytes(byte type, int length, byte[] sequence) {
            byte[] ad = (byte[])this.block.clone();
            this.increaseSequenceNumber();
            ad[8] = type;
            ad[9] = (byte)(length >> 8);
            ad[10] = (byte)length;
            return ad;
        }
    }

    private static final class DTLS10Mac
    extends DTLS10Authenticator
    implements MAC {
        private final MacImpl macImpl;

        public DTLS10Mac(ProtocolVersion protocolVersion, CipherSuite.MacAlg macAlg, SecretKey key) throws NoSuchAlgorithmException, InvalidKeyException {
            super(protocolVersion);
            this.macImpl = new MacImpl(protocolVersion, macAlg, key);
        }

        @Override
        public CipherSuite.MacAlg macAlg() {
            return this.macImpl.macAlg;
        }

        @Override
        public byte[] compute(byte type, ByteBuffer bb, byte[] sequence, boolean isSimulated) {
            return this.macImpl.compute(type, bb, sequence, isSimulated);
        }
    }

    private static final class TLS10Mac
    extends TLS10Authenticator
    implements MAC {
        private final MacImpl macImpl;

        public TLS10Mac(ProtocolVersion protocolVersion, CipherSuite.MacAlg macAlg, SecretKey key) throws NoSuchAlgorithmException, InvalidKeyException {
            super(protocolVersion);
            this.macImpl = new MacImpl(protocolVersion, macAlg, key);
        }

        @Override
        public CipherSuite.MacAlg macAlg() {
            return this.macImpl.macAlg;
        }

        @Override
        public byte[] compute(byte type, ByteBuffer bb, byte[] sequence, boolean isSimulated) {
            return this.macImpl.compute(type, bb, sequence, isSimulated);
        }
    }

    private static final class SSL30Mac
    extends SSL30Authenticator
    implements MAC {
        private final MacImpl macImpl;

        public SSL30Mac(ProtocolVersion protocolVersion, CipherSuite.MacAlg macAlg, SecretKey key) throws NoSuchAlgorithmException, InvalidKeyException {
            this.macImpl = new MacImpl(protocolVersion, macAlg, key);
        }

        @Override
        public CipherSuite.MacAlg macAlg() {
            return this.macImpl.macAlg;
        }

        @Override
        public byte[] compute(byte type, ByteBuffer bb, byte[] sequence, boolean isSimulated) {
            return this.macImpl.compute(type, bb, sequence, isSimulated);
        }
    }

    private static final class SSLNullMac
    extends SSLNullAuthenticator
    implements MAC {
        private final MacImpl macImpl = new MacImpl();

        @Override
        public CipherSuite.MacAlg macAlg() {
            return this.macImpl.macAlg;
        }

        @Override
        public byte[] compute(byte type, ByteBuffer bb, byte[] sequence, boolean isSimulated) {
            return this.macImpl.compute(type, bb, sequence, isSimulated);
        }
    }

    private static final class DTLSNullMac
    extends DTLSNullAuthenticator
    implements MAC {
        private final MacImpl macImpl = new MacImpl();

        @Override
        public CipherSuite.MacAlg macAlg() {
            return this.macImpl.macAlg;
        }

        @Override
        public byte[] compute(byte type, ByteBuffer bb, byte[] sequence, boolean isSimulated) {
            return this.macImpl.compute(type, bb, sequence, isSimulated);
        }
    }

    private class MacImpl
    implements MAC {
        private final CipherSuite.MacAlg macAlg;
        private final Mac mac;

        private MacImpl() {
            this.macAlg = CipherSuite.MacAlg.M_NULL;
            this.mac = null;
        }

        private MacImpl(ProtocolVersion protocolVersion, CipherSuite.MacAlg macAlg, SecretKey key) throws NoSuchAlgorithmException, InvalidKeyException {
            if (macAlg == null) {
                throw new RuntimeException("Null MacAlg");
            }
            boolean useSSLMac = protocolVersion.id < ProtocolVersion.TLS10.id;
            Mac m = Mac.getInstance(switch (macAlg) {
                case CipherSuite.MacAlg.M_MD5 -> useSSLMac ? "SslMacMD5" : "HmacMD5";
                case CipherSuite.MacAlg.M_SHA -> useSSLMac ? "SslMacSHA1" : "HmacSHA1";
                case CipherSuite.MacAlg.M_SHA256 -> "HmacSHA256";
                case CipherSuite.MacAlg.M_SHA384 -> "HmacSHA384";
                default -> throw new RuntimeException("Unknown MacAlg " + (Object)((Object)macAlg));
            });
            m.init(key);
            this.macAlg = macAlg;
            this.mac = m;
        }

        @Override
        public CipherSuite.MacAlg macAlg() {
            return this.macAlg;
        }

        @Override
        public byte[] compute(byte type, ByteBuffer bb, byte[] sequence, boolean isSimulated) {
            if (this.macAlg.size == 0) {
                return new byte[0];
            }
            if (!isSimulated) {
                byte[] additional = Authenticator.this.acquireAuthenticationBytes(type, bb.remaining(), sequence);
                this.mac.update(additional);
            }
            this.mac.update(bb);
            return this.mac.doFinal();
        }
    }

    static interface MAC {
        public CipherSuite.MacAlg macAlg();

        public byte[] compute(byte var1, ByteBuffer var2, byte[] var3, boolean var4);

        default public byte[] compute(byte type, ByteBuffer bb, boolean isSimulated) {
            return this.compute(type, bb, null, isSimulated);
        }
    }

    private static class DTLSNullAuthenticator
    extends DTLSAuthenticator {
        private DTLSNullAuthenticator() {
            super(new byte[8]);
        }
    }

    private static class DTLSAuthenticator
    extends Authenticator {
        private DTLSAuthenticator(byte[] block) {
            super(block);
        }

        @Override
        boolean seqNumOverflow() {
            return this.block.length != 0 && this.block[2] == -1 && this.block[3] == -1 && this.block[4] == -1 && this.block[5] == -1 && this.block[6] == -1;
        }

        @Override
        boolean seqNumIsHuge() {
            return this.block.length != 0 && this.block[2] == -1 && this.block[3] == -1;
        }

        @Override
        void setEpochNumber(int epoch) {
            this.block[0] = (byte)(epoch >> 8 & 0xFF);
            this.block[1] = (byte)(epoch & 0xFF);
        }
    }

    private static class SSLNullAuthenticator
    extends SSLAuthenticator {
        private SSLNullAuthenticator() {
            super(new byte[8]);
        }
    }

    private static class SSLAuthenticator
    extends Authenticator {
        private SSLAuthenticator(byte[] block) {
            super(block);
        }

        @Override
        boolean seqNumOverflow() {
            return this.block.length != 0 && this.block[0] == -1 && this.block[1] == -1 && this.block[2] == -1 && this.block[3] == -1 && this.block[4] == -1 && this.block[5] == -1 && this.block[6] == -1;
        }

        @Override
        boolean seqNumIsHuge() {
            return this.block.length != 0 && this.block[0] == -1 && this.block[1] == -1 && this.block[2] == -1 && this.block[3] == -1;
        }
    }
}

