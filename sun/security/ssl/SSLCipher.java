/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import sun.security.ssl.Authenticator;
import sun.security.ssl.CipherType;
import sun.security.ssl.ContentType;
import sun.security.ssl.Plaintext;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.SSLLogger;

enum SSLCipher {
    B_NULL("NULL", CipherType.NULL_CIPHER, 0, 0, 0, 0, true, true, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<NullReadCipherGenerator, ProtocolVersion[]>(new NullReadCipherGenerator(), ProtocolVersion.PROTOCOLS_OF_NONE), new AbstractMap.SimpleImmutableEntry<NullReadCipherGenerator, ProtocolVersion[]>(new NullReadCipherGenerator(), ProtocolVersion.PROTOCOLS_TO_13)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<NullWriteCipherGenerator, ProtocolVersion[]>(new NullWriteCipherGenerator(), ProtocolVersion.PROTOCOLS_OF_NONE), new AbstractMap.SimpleImmutableEntry<NullWriteCipherGenerator, ProtocolVersion[]>(new NullWriteCipherGenerator(), ProtocolVersion.PROTOCOLS_TO_13)}),
    B_RC4_40("RC4", CipherType.STREAM_CIPHER, 5, 16, 0, 0, true, true, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<StreamReadCipherGenerator, ProtocolVersion[]>(new StreamReadCipherGenerator(), ProtocolVersion.PROTOCOLS_TO_10)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<StreamWriteCipherGenerator, ProtocolVersion[]>(new StreamWriteCipherGenerator(), ProtocolVersion.PROTOCOLS_TO_10)}),
    B_RC2_40("RC2", CipherType.BLOCK_CIPHER, 5, 16, 8, 0, false, true, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<StreamReadCipherGenerator, ProtocolVersion[]>(new StreamReadCipherGenerator(), ProtocolVersion.PROTOCOLS_TO_10)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<StreamWriteCipherGenerator, ProtocolVersion[]>(new StreamWriteCipherGenerator(), ProtocolVersion.PROTOCOLS_TO_10)}),
    B_DES_40("DES/CBC/NoPadding", CipherType.BLOCK_CIPHER, 5, 8, 8, 0, true, true, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<T10BlockReadCipherGenerator, ProtocolVersion[]>(new T10BlockReadCipherGenerator(), ProtocolVersion.PROTOCOLS_TO_10)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<T10BlockWriteCipherGenerator, ProtocolVersion[]>(new T10BlockWriteCipherGenerator(), ProtocolVersion.PROTOCOLS_TO_10)}),
    B_RC4_128("RC4", CipherType.STREAM_CIPHER, 16, 16, 0, 0, true, false, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<StreamReadCipherGenerator, ProtocolVersion[]>(new StreamReadCipherGenerator(), ProtocolVersion.PROTOCOLS_TO_12)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<StreamWriteCipherGenerator, ProtocolVersion[]>(new StreamWriteCipherGenerator(), ProtocolVersion.PROTOCOLS_TO_12)}),
    B_DES("DES/CBC/NoPadding", CipherType.BLOCK_CIPHER, 8, 8, 8, 0, true, false, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<T10BlockReadCipherGenerator, ProtocolVersion[]>(new T10BlockReadCipherGenerator(), ProtocolVersion.PROTOCOLS_TO_10), new AbstractMap.SimpleImmutableEntry<T11BlockReadCipherGenerator, ProtocolVersion[]>(new T11BlockReadCipherGenerator(), ProtocolVersion.PROTOCOLS_OF_11)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<T10BlockWriteCipherGenerator, ProtocolVersion[]>(new T10BlockWriteCipherGenerator(), ProtocolVersion.PROTOCOLS_TO_10), new AbstractMap.SimpleImmutableEntry<T11BlockWriteCipherGenerator, ProtocolVersion[]>(new T11BlockWriteCipherGenerator(), ProtocolVersion.PROTOCOLS_OF_11)}),
    B_3DES("DESede/CBC/NoPadding", CipherType.BLOCK_CIPHER, 24, 24, 8, 0, true, false, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<T10BlockReadCipherGenerator, ProtocolVersion[]>(new T10BlockReadCipherGenerator(), ProtocolVersion.PROTOCOLS_TO_10), new AbstractMap.SimpleImmutableEntry<T11BlockReadCipherGenerator, ProtocolVersion[]>(new T11BlockReadCipherGenerator(), ProtocolVersion.PROTOCOLS_11_12)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<T10BlockWriteCipherGenerator, ProtocolVersion[]>(new T10BlockWriteCipherGenerator(), ProtocolVersion.PROTOCOLS_TO_10), new AbstractMap.SimpleImmutableEntry<T11BlockWriteCipherGenerator, ProtocolVersion[]>(new T11BlockWriteCipherGenerator(), ProtocolVersion.PROTOCOLS_11_12)}),
    B_IDEA("IDEA", CipherType.BLOCK_CIPHER, 16, 16, 8, 0, false, false, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<Object, ProtocolVersion[]>(null, ProtocolVersion.PROTOCOLS_TO_12)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<Object, ProtocolVersion[]>(null, ProtocolVersion.PROTOCOLS_TO_12)}),
    B_AES_128("AES/CBC/NoPadding", CipherType.BLOCK_CIPHER, 16, 16, 16, 0, true, false, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<T10BlockReadCipherGenerator, ProtocolVersion[]>(new T10BlockReadCipherGenerator(), ProtocolVersion.PROTOCOLS_TO_10), new AbstractMap.SimpleImmutableEntry<T11BlockReadCipherGenerator, ProtocolVersion[]>(new T11BlockReadCipherGenerator(), ProtocolVersion.PROTOCOLS_11_12)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<T10BlockWriteCipherGenerator, ProtocolVersion[]>(new T10BlockWriteCipherGenerator(), ProtocolVersion.PROTOCOLS_TO_10), new AbstractMap.SimpleImmutableEntry<T11BlockWriteCipherGenerator, ProtocolVersion[]>(new T11BlockWriteCipherGenerator(), ProtocolVersion.PROTOCOLS_11_12)}),
    B_AES_256("AES/CBC/NoPadding", CipherType.BLOCK_CIPHER, 32, 32, 16, 0, true, false, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<T10BlockReadCipherGenerator, ProtocolVersion[]>(new T10BlockReadCipherGenerator(), ProtocolVersion.PROTOCOLS_TO_10), new AbstractMap.SimpleImmutableEntry<T11BlockReadCipherGenerator, ProtocolVersion[]>(new T11BlockReadCipherGenerator(), ProtocolVersion.PROTOCOLS_11_12)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<T10BlockWriteCipherGenerator, ProtocolVersion[]>(new T10BlockWriteCipherGenerator(), ProtocolVersion.PROTOCOLS_TO_10), new AbstractMap.SimpleImmutableEntry<T11BlockWriteCipherGenerator, ProtocolVersion[]>(new T11BlockWriteCipherGenerator(), ProtocolVersion.PROTOCOLS_11_12)}),
    B_AES_128_GCM("AES/GCM/NoPadding", CipherType.AEAD_CIPHER, 16, 16, 12, 4, true, false, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<T12GcmReadCipherGenerator, ProtocolVersion[]>(new T12GcmReadCipherGenerator(), ProtocolVersion.PROTOCOLS_OF_12)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<T12GcmWriteCipherGenerator, ProtocolVersion[]>(new T12GcmWriteCipherGenerator(), ProtocolVersion.PROTOCOLS_OF_12)}),
    B_AES_256_GCM("AES/GCM/NoPadding", CipherType.AEAD_CIPHER, 32, 32, 12, 4, true, false, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<T12GcmReadCipherGenerator, ProtocolVersion[]>(new T12GcmReadCipherGenerator(), ProtocolVersion.PROTOCOLS_OF_12)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<T12GcmWriteCipherGenerator, ProtocolVersion[]>(new T12GcmWriteCipherGenerator(), ProtocolVersion.PROTOCOLS_OF_12)}),
    B_AES_128_GCM_IV("AES/GCM/NoPadding", CipherType.AEAD_CIPHER, 16, 16, 12, 0, true, false, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<T13GcmReadCipherGenerator, ProtocolVersion[]>(new T13GcmReadCipherGenerator(), ProtocolVersion.PROTOCOLS_OF_13)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<T13GcmWriteCipherGenerator, ProtocolVersion[]>(new T13GcmWriteCipherGenerator(), ProtocolVersion.PROTOCOLS_OF_13)}),
    B_AES_256_GCM_IV("AES/GCM/NoPadding", CipherType.AEAD_CIPHER, 32, 32, 12, 0, true, false, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<T13GcmReadCipherGenerator, ProtocolVersion[]>(new T13GcmReadCipherGenerator(), ProtocolVersion.PROTOCOLS_OF_13)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<T13GcmWriteCipherGenerator, ProtocolVersion[]>(new T13GcmWriteCipherGenerator(), ProtocolVersion.PROTOCOLS_OF_13)}),
    B_CC20_P1305("ChaCha20-Poly1305", CipherType.AEAD_CIPHER, 32, 32, 12, 12, true, false, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<T12CC20P1305ReadCipherGenerator, ProtocolVersion[]>(new T12CC20P1305ReadCipherGenerator(), ProtocolVersion.PROTOCOLS_OF_12), new AbstractMap.SimpleImmutableEntry<T13CC20P1305ReadCipherGenerator, ProtocolVersion[]>(new T13CC20P1305ReadCipherGenerator(), ProtocolVersion.PROTOCOLS_OF_13)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<T12CC20P1305WriteCipherGenerator, ProtocolVersion[]>(new T12CC20P1305WriteCipherGenerator(), ProtocolVersion.PROTOCOLS_OF_12), new AbstractMap.SimpleImmutableEntry<T13CC20P1305WriteCipherGenerator, ProtocolVersion[]>(new T13CC20P1305WriteCipherGenerator(), ProtocolVersion.PROTOCOLS_OF_13)});

    final String description;
    final String transformation;
    final String algorithm;
    final boolean allowed;
    final int keySize;
    final int expandedKeySize;
    final int ivSize;
    final int fixedIvSize;
    final boolean exportable;
    final CipherType cipherType;
    final int tagSize = 16;
    private final boolean isAvailable;
    private final Map.Entry<ReadCipherGenerator, ProtocolVersion[]>[] readCipherGenerators;
    private final Map.Entry<WriteCipherGenerator, ProtocolVersion[]>[] writeCipherGenerators;
    private static final HashMap<String, Long> cipherLimits;
    static final String[] tag;

    private SSLCipher(String transformation, CipherType cipherType, int keySize, int expandedKeySize, int ivSize, int fixedIvSize, boolean allowed, boolean exportable, Map.Entry<ReadCipherGenerator, ProtocolVersion[]>[] readCipherGenerators, Map.Entry<WriteCipherGenerator, ProtocolVersion[]>[] writeCipherGenerators) {
        this.transformation = transformation;
        String[] splits = transformation.split("/");
        this.algorithm = splits[0];
        this.cipherType = cipherType;
        this.description = this.algorithm + "/" + (keySize << 3);
        this.keySize = keySize;
        this.ivSize = ivSize;
        this.fixedIvSize = fixedIvSize;
        this.allowed = allowed;
        this.expandedKeySize = expandedKeySize;
        this.exportable = exportable;
        this.isAvailable = allowed && SSLCipher.isUnlimited(keySize, transformation) && SSLCipher.isTransformationAvailable(transformation);
        this.readCipherGenerators = readCipherGenerators;
        this.writeCipherGenerators = writeCipherGenerators;
    }

    private static boolean isTransformationAvailable(String transformation) {
        if (transformation.equals("NULL")) {
            return true;
        }
        try {
            Cipher.getInstance(transformation);
            return true;
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                SSLLogger.fine("Transformation " + transformation + " is not available.", new Object[0]);
            }
            return false;
        }
    }

    SSLReadCipher createReadCipher(Authenticator authenticator, ProtocolVersion protocolVersion, SecretKey key, IvParameterSpec iv, SecureRandom random) throws GeneralSecurityException {
        if (this.readCipherGenerators.length == 0) {
            return null;
        }
        ReadCipherGenerator rcg = null;
        block0: for (Map.Entry<ReadCipherGenerator, ProtocolVersion[]> me : this.readCipherGenerators) {
            for (ProtocolVersion pv : me.getValue()) {
                if (protocolVersion != pv) continue;
                rcg = me.getKey();
                continue block0;
            }
        }
        if (rcg != null) {
            return rcg.createCipher(this, authenticator, protocolVersion, this.transformation, key, iv, random);
        }
        return null;
    }

    SSLWriteCipher createWriteCipher(Authenticator authenticator, ProtocolVersion protocolVersion, SecretKey key, IvParameterSpec iv, SecureRandom random) throws GeneralSecurityException {
        if (this.writeCipherGenerators.length == 0) {
            return null;
        }
        WriteCipherGenerator wcg = null;
        block0: for (Map.Entry<WriteCipherGenerator, ProtocolVersion[]> me : this.writeCipherGenerators) {
            for (ProtocolVersion pv : me.getValue()) {
                if (protocolVersion != pv) continue;
                wcg = me.getKey();
                continue block0;
            }
        }
        if (wcg != null) {
            return wcg.createCipher(this, authenticator, protocolVersion, this.transformation, key, iv, random);
        }
        return null;
    }

    boolean isAvailable() {
        return this.isAvailable;
    }

    private static boolean isUnlimited(int keySize, String transformation) {
        int keySizeInBits = keySize * 8;
        if (keySizeInBits > 128) {
            try {
                if (Cipher.getMaxAllowedKeyLength(transformation) < keySizeInBits) {
                    return false;
                }
            }
            catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        return this.description;
    }

    private static void addMac(Authenticator.MAC signer, ByteBuffer destination, byte contentType) {
        if (signer.macAlg().size != 0) {
            int dstContent = destination.position();
            byte[] hash = signer.compute(contentType, destination, false);
            destination.limit(destination.limit() + hash.length);
            destination.put(hash);
            destination.position(dstContent);
        }
    }

    private static void checkStreamMac(Authenticator.MAC signer, ByteBuffer bb, byte contentType, byte[] sequence) throws BadPaddingException {
        int tagLen = signer.macAlg().size;
        if (tagLen != 0) {
            int contentLen = bb.remaining() - tagLen;
            if (contentLen < 0) {
                throw new BadPaddingException("bad record");
            }
            if (SSLCipher.checkMacTags(contentType, bb, signer, sequence, false)) {
                throw new BadPaddingException("bad record MAC");
            }
        }
    }

    private static void checkCBCMac(Authenticator.MAC signer, ByteBuffer bb, byte contentType, int cipheredLength, byte[] sequence) throws BadPaddingException {
        BadPaddingException reservedBPE = null;
        int tagLen = signer.macAlg().size;
        int pos = bb.position();
        if (tagLen != 0) {
            int contentLen = bb.remaining() - tagLen;
            if (contentLen < 0) {
                reservedBPE = new BadPaddingException("bad record");
                contentLen = cipheredLength - tagLen;
                bb.limit(pos + cipheredLength);
            }
            if (SSLCipher.checkMacTags(contentType, bb, signer, sequence, false) && reservedBPE == null) {
                reservedBPE = new BadPaddingException("bad record MAC");
            }
            int remainingLen = SSLCipher.calculateRemainingLen(signer, cipheredLength, contentLen);
            ByteBuffer temporary = ByteBuffer.allocate(remainingLen += signer.macAlg().size);
            SSLCipher.checkMacTags(contentType, temporary, signer, sequence, true);
        }
        if (reservedBPE != null) {
            throw reservedBPE;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean checkMacTags(byte contentType, ByteBuffer bb, Authenticator.MAC signer, byte[] sequence, boolean isSimulated) {
        int tagLen = signer.macAlg().size;
        int position = bb.position();
        int lim = bb.limit();
        int macOffset = lim - tagLen;
        bb.limit(macOffset);
        byte[] hash = signer.compute(contentType, bb, sequence, isSimulated);
        if (hash == null || tagLen != hash.length) {
            throw new RuntimeException("Internal MAC error");
        }
        bb.position(macOffset);
        bb.limit(lim);
        try {
            int[] results = SSLCipher.compareMacTags(bb, hash);
            boolean bl = results[0] != 0;
            return bl;
        }
        finally {
            bb.position(position);
            bb.limit(macOffset);
        }
    }

    private static int[] compareMacTags(ByteBuffer bb, byte[] tag) {
        int[] results = new int[]{0, 0};
        for (byte t : tag) {
            if (bb.get() != t) {
                results[0] = results[0] + 1;
                continue;
            }
            results[1] = results[1] + 1;
        }
        return results;
    }

    private static int calculateRemainingLen(Authenticator.MAC signer, int fullLen, int usedLen) {
        int blockLen = signer.macAlg().hashBlockSize;
        int minimalPaddingLen = signer.macAlg().minimalPaddingSize;
        return 1 + (int)(Math.ceil((double)(fullLen += 13 - (blockLen - minimalPaddingLen)) / (1.0 * (double)blockLen)) - Math.ceil((double)(usedLen += 13 - (blockLen - minimalPaddingLen)) / (1.0 * (double)blockLen))) * blockLen;
    }

    private static int addPadding(ByteBuffer bb, int blockSize) {
        int len = bb.remaining();
        int offset = bb.position();
        int newlen = len + 1;
        if (newlen % blockSize != 0) {
            newlen += blockSize - 1;
            newlen -= newlen % blockSize;
        }
        int pad = newlen - len;
        bb.limit(newlen + offset);
        offset += len;
        for (int i = 0; i < pad; ++i) {
            bb.put(offset++, (byte)(pad - 1));
        }
        bb.position(offset);
        bb.limit(offset);
        return newlen;
    }

    private static int removePadding(ByteBuffer bb, int tagLen, int blockSize, ProtocolVersion protocolVersion) throws BadPaddingException {
        int offset;
        int padOffset;
        int padLen;
        int len = bb.remaining();
        int newLen = len - ((padLen = bb.get(padOffset = (offset = bb.position()) + len - 1) & 0xFF) + 1);
        if (newLen - tagLen < 0) {
            SSLCipher.checkPadding(bb.duplicate(), (byte)(padLen & 0xFF));
            throw new BadPaddingException("Invalid Padding length: " + padLen);
        }
        int[] results = SSLCipher.checkPadding(bb.duplicate().position(offset + newLen), (byte)(padLen & 0xFF));
        if (protocolVersion.useTLS10PlusSpec()) {
            if (results[0] != 0) {
                throw new BadPaddingException("Invalid TLS padding data");
            }
        } else if (padLen > blockSize) {
            throw new BadPaddingException("Padding length (" + padLen + ") of SSLv3 message should not be bigger than the block size (" + blockSize + ")");
        }
        bb.limit(offset + newLen);
        return newLen;
    }

    private static int[] checkPadding(ByteBuffer bb, byte pad) {
        if (!bb.hasRemaining()) {
            throw new RuntimeException("hasRemaining() must be positive");
        }
        int[] results = new int[]{0, 0};
        bb.mark();
        int i = 0;
        while (i <= 256) {
            while (bb.hasRemaining() && i <= 256) {
                if (bb.get() != pad) {
                    results[0] = results[0] + 1;
                } else {
                    results[1] = results[1] + 1;
                }
                ++i;
            }
            bb.reset();
        }
        return results;
    }

    static {
        cipherLimits = new HashMap();
        tag = new String[]{"KEYUPDATE"};
        long max = 0x4000000000000000L;
        String prop = AccessController.doPrivileged(new PrivilegedAction<String>(){

            @Override
            public String run() {
                return Security.getProperty("jdk.tls.keyLimits");
            }
        });
        if (prop != null) {
            String[] propvalue;
            for (String entry : propvalue = prop.split(",")) {
                long size;
                String[] values = entry.trim().toUpperCase(Locale.ENGLISH).split(" ");
                if (!values[1].contains(tag[0])) {
                    if (!SSLLogger.isOn || !SSLLogger.isOn("ssl")) continue;
                    SSLLogger.fine("jdk.tls.keyLimits:  Unknown action:  " + entry, new Object[0]);
                    continue;
                }
                int index = 0;
                int i = values[2].indexOf("^");
                try {
                    size = i >= 0 ? (long)Math.pow(2.0, Integer.parseInt(values[2].substring(i + 1))) : Long.parseLong(values[2]);
                    if (size < 1L || size > 0x4000000000000000L) {
                        throw new NumberFormatException("Length exceeded limits");
                    }
                }
                catch (NumberFormatException e) {
                    if (!SSLLogger.isOn || !SSLLogger.isOn("ssl")) continue;
                    SSLLogger.fine("jdk.tls.keyLimits:  " + e.getMessage() + ":  " + entry, new Object[0]);
                    continue;
                }
                if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                    SSLLogger.fine("jdk.tls.keyLimits:  entry = " + entry + ". " + values[0] + ":" + tag[index] + " = " + size, new Object[0]);
                }
                cipherLimits.put(values[0] + ":" + tag[index], size);
            }
        }
    }

    static interface ReadCipherGenerator {
        public SSLReadCipher createCipher(SSLCipher var1, Authenticator var2, ProtocolVersion var3, String var4, Key var5, AlgorithmParameterSpec var6, SecureRandom var7) throws GeneralSecurityException;
    }

    static abstract class SSLReadCipher {
        final Authenticator authenticator;
        final ProtocolVersion protocolVersion;
        boolean keyLimitEnabled = false;
        long keyLimitCountdown = 0L;
        SecretKey baseSecret;

        SSLReadCipher(Authenticator authenticator, ProtocolVersion protocolVersion) {
            this.authenticator = authenticator;
            this.protocolVersion = protocolVersion;
        }

        static final SSLReadCipher nullTlsReadCipher() {
            try {
                return B_NULL.createReadCipher(Authenticator.nullTlsMac(), ProtocolVersion.NONE, null, null, null);
            }
            catch (GeneralSecurityException gse) {
                throw new RuntimeException("Cannot create NULL SSLCipher", gse);
            }
        }

        static final SSLReadCipher nullDTlsReadCipher() {
            try {
                return B_NULL.createReadCipher(Authenticator.nullDtlsMac(), ProtocolVersion.NONE, null, null, null);
            }
            catch (GeneralSecurityException gse) {
                throw new RuntimeException("Cannot create NULL SSLCipher", gse);
            }
        }

        abstract Plaintext decrypt(byte var1, ByteBuffer var2, byte[] var3) throws GeneralSecurityException;

        void dispose() {
        }

        abstract int estimateFragmentSize(int var1, int var2);

        boolean isNullCipher() {
            return false;
        }

        public boolean atKeyLimit() {
            if (this.keyLimitCountdown >= 0L) {
                return false;
            }
            this.keyLimitEnabled = false;
            return true;
        }
    }

    static interface WriteCipherGenerator {
        public SSLWriteCipher createCipher(SSLCipher var1, Authenticator var2, ProtocolVersion var3, String var4, Key var5, AlgorithmParameterSpec var6, SecureRandom var7) throws GeneralSecurityException;
    }

    static abstract class SSLWriteCipher {
        final Authenticator authenticator;
        final ProtocolVersion protocolVersion;
        boolean keyLimitEnabled = false;
        long keyLimitCountdown = 0L;
        SecretKey baseSecret;

        SSLWriteCipher(Authenticator authenticator, ProtocolVersion protocolVersion) {
            this.authenticator = authenticator;
            this.protocolVersion = protocolVersion;
        }

        abstract int encrypt(byte var1, ByteBuffer var2);

        static final SSLWriteCipher nullTlsWriteCipher() {
            try {
                return B_NULL.createWriteCipher(Authenticator.nullTlsMac(), ProtocolVersion.NONE, null, null, null);
            }
            catch (GeneralSecurityException gse) {
                throw new RuntimeException("Cannot create NULL SSL write Cipher", gse);
            }
        }

        static final SSLWriteCipher nullDTlsWriteCipher() {
            try {
                return B_NULL.createWriteCipher(Authenticator.nullDtlsMac(), ProtocolVersion.NONE, null, null, null);
            }
            catch (GeneralSecurityException gse) {
                throw new RuntimeException("Cannot create NULL SSL write Cipher", gse);
            }
        }

        void dispose() {
        }

        abstract int getExplicitNonceSize();

        abstract int calculateFragmentSize(int var1, int var2);

        abstract int calculatePacketSize(int var1, int var2);

        boolean isCBCMode() {
            return false;
        }

        boolean isNullCipher() {
            return false;
        }

        public boolean atKeyLimit() {
            if (this.keyLimitCountdown >= 0L) {
                return false;
            }
            this.keyLimitEnabled = false;
            return true;
        }
    }

    private static final class NullReadCipherGenerator
    implements ReadCipherGenerator {
        private NullReadCipherGenerator() {
        }

        @Override
        public SSLReadCipher createCipher(SSLCipher sslCipher, Authenticator authenticator, ProtocolVersion protocolVersion, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
            return new NullReadCipher(authenticator, protocolVersion);
        }

        static final class NullReadCipher
        extends SSLReadCipher {
            NullReadCipher(Authenticator authenticator, ProtocolVersion protocolVersion) {
                super(authenticator, protocolVersion);
            }

            @Override
            public Plaintext decrypt(byte contentType, ByteBuffer bb, byte[] sequence) throws GeneralSecurityException {
                Authenticator.MAC signer = (Authenticator.MAC)((Object)this.authenticator);
                if (signer.macAlg().size != 0) {
                    SSLCipher.checkStreamMac(signer, bb, contentType, sequence);
                } else {
                    this.authenticator.increaseSequenceNumber();
                }
                return new Plaintext(contentType, ProtocolVersion.NONE.major, ProtocolVersion.NONE.minor, -1, -1L, bb.slice());
            }

            @Override
            int estimateFragmentSize(int packetSize, int headerSize) {
                int macLen = ((Authenticator.MAC)((Object)this.authenticator)).macAlg().size;
                return packetSize - headerSize - macLen;
            }

            @Override
            boolean isNullCipher() {
                return true;
            }
        }
    }

    private static final class NullWriteCipherGenerator
    implements WriteCipherGenerator {
        private NullWriteCipherGenerator() {
        }

        @Override
        public SSLWriteCipher createCipher(SSLCipher sslCipher, Authenticator authenticator, ProtocolVersion protocolVersion, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
            return new NullWriteCipher(authenticator, protocolVersion);
        }

        static final class NullWriteCipher
        extends SSLWriteCipher {
            NullWriteCipher(Authenticator authenticator, ProtocolVersion protocolVersion) {
                super(authenticator, protocolVersion);
            }

            @Override
            public int encrypt(byte contentType, ByteBuffer bb) {
                Authenticator.MAC signer = (Authenticator.MAC)((Object)this.authenticator);
                if (signer.macAlg().size != 0) {
                    SSLCipher.addMac(signer, bb, contentType);
                } else {
                    this.authenticator.increaseSequenceNumber();
                }
                int len = bb.remaining();
                bb.position(bb.limit());
                return len;
            }

            @Override
            int getExplicitNonceSize() {
                return 0;
            }

            @Override
            int calculateFragmentSize(int packetLimit, int headerSize) {
                int macLen = ((Authenticator.MAC)((Object)this.authenticator)).macAlg().size;
                return packetLimit - headerSize - macLen;
            }

            @Override
            int calculatePacketSize(int fragmentSize, int headerSize) {
                int macLen = ((Authenticator.MAC)((Object)this.authenticator)).macAlg().size;
                return fragmentSize + headerSize + macLen;
            }

            @Override
            boolean isNullCipher() {
                return true;
            }
        }
    }

    private static final class StreamReadCipherGenerator
    implements ReadCipherGenerator {
        private StreamReadCipherGenerator() {
        }

        @Override
        public SSLReadCipher createCipher(SSLCipher sslCipher, Authenticator authenticator, ProtocolVersion protocolVersion, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
            return new StreamReadCipher(authenticator, protocolVersion, algorithm, key, params, random);
        }

        static final class StreamReadCipher
        extends SSLReadCipher {
            private final Cipher cipher;

            StreamReadCipher(Authenticator authenticator, ProtocolVersion protocolVersion, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
                super(authenticator, protocolVersion);
                this.cipher = Cipher.getInstance(algorithm);
                this.cipher.init(2, key, params, random);
            }

            @Override
            public Plaintext decrypt(byte contentType, ByteBuffer bb, byte[] sequence) throws GeneralSecurityException {
                int pos;
                ByteBuffer pt;
                int len = bb.remaining();
                if (!bb.isReadOnly()) {
                    pt = bb.duplicate();
                    pos = bb.position();
                } else {
                    pt = ByteBuffer.allocate(bb.remaining());
                    pos = 0;
                }
                try {
                    if (len != this.cipher.update(bb, pt)) {
                        throw new RuntimeException("Unexpected number of plaintext bytes");
                    }
                }
                catch (ShortBufferException sbe) {
                    throw new RuntimeException("Cipher buffering error in JCE provider " + this.cipher.getProvider().getName(), sbe);
                }
                pt.position(pos);
                if (SSLLogger.isOn && SSLLogger.isOn("plaintext")) {
                    SSLLogger.fine("Plaintext after DECRYPTION", pt.duplicate());
                }
                Authenticator.MAC signer = (Authenticator.MAC)((Object)this.authenticator);
                if (signer.macAlg().size != 0) {
                    SSLCipher.checkStreamMac(signer, pt, contentType, sequence);
                } else {
                    this.authenticator.increaseSequenceNumber();
                }
                return new Plaintext(contentType, ProtocolVersion.NONE.major, ProtocolVersion.NONE.minor, -1, -1L, pt.slice());
            }

            @Override
            void dispose() {
                if (this.cipher != null) {
                    try {
                        this.cipher.doFinal();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }

            @Override
            int estimateFragmentSize(int packetSize, int headerSize) {
                int macLen = ((Authenticator.MAC)((Object)this.authenticator)).macAlg().size;
                return packetSize - headerSize - macLen;
            }
        }
    }

    private static final class StreamWriteCipherGenerator
    implements WriteCipherGenerator {
        private StreamWriteCipherGenerator() {
        }

        @Override
        public SSLWriteCipher createCipher(SSLCipher sslCipher, Authenticator authenticator, ProtocolVersion protocolVersion, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
            return new StreamWriteCipher(authenticator, protocolVersion, algorithm, key, params, random);
        }

        static final class StreamWriteCipher
        extends SSLWriteCipher {
            private final Cipher cipher;

            StreamWriteCipher(Authenticator authenticator, ProtocolVersion protocolVersion, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
                super(authenticator, protocolVersion);
                this.cipher = Cipher.getInstance(algorithm);
                this.cipher.init(1, key, params, random);
            }

            @Override
            public int encrypt(byte contentType, ByteBuffer bb) {
                Authenticator.MAC signer = (Authenticator.MAC)((Object)this.authenticator);
                if (signer.macAlg().size != 0) {
                    SSLCipher.addMac(signer, bb, contentType);
                } else {
                    this.authenticator.increaseSequenceNumber();
                }
                if (SSLLogger.isOn && SSLLogger.isOn("plaintext")) {
                    SSLLogger.finest("Padded plaintext before ENCRYPTION", bb.duplicate());
                }
                int len = bb.remaining();
                ByteBuffer dup = bb.duplicate();
                try {
                    if (len != this.cipher.update(dup, bb)) {
                        throw new RuntimeException("Unexpected number of plaintext bytes");
                    }
                    if (bb.position() != dup.position()) {
                        throw new RuntimeException("Unexpected ByteBuffer position");
                    }
                }
                catch (ShortBufferException sbe) {
                    throw new RuntimeException("Cipher buffering error in JCE provider " + this.cipher.getProvider().getName(), sbe);
                }
                return len;
            }

            @Override
            void dispose() {
                if (this.cipher != null) {
                    try {
                        this.cipher.doFinal();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }

            @Override
            int getExplicitNonceSize() {
                return 0;
            }

            @Override
            int calculateFragmentSize(int packetLimit, int headerSize) {
                int macLen = ((Authenticator.MAC)((Object)this.authenticator)).macAlg().size;
                return packetLimit - headerSize - macLen;
            }

            @Override
            int calculatePacketSize(int fragmentSize, int headerSize) {
                int macLen = ((Authenticator.MAC)((Object)this.authenticator)).macAlg().size;
                return fragmentSize + headerSize + macLen;
            }
        }
    }

    private static final class T10BlockReadCipherGenerator
    implements ReadCipherGenerator {
        private T10BlockReadCipherGenerator() {
        }

        @Override
        public SSLReadCipher createCipher(SSLCipher sslCipher, Authenticator authenticator, ProtocolVersion protocolVersion, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
            return new BlockReadCipher(authenticator, protocolVersion, algorithm, key, params, random);
        }

        static final class BlockReadCipher
        extends SSLReadCipher {
            private final Cipher cipher;

            BlockReadCipher(Authenticator authenticator, ProtocolVersion protocolVersion, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
                super(authenticator, protocolVersion);
                this.cipher = Cipher.getInstance(algorithm);
                this.cipher.init(2, key, params, random);
            }

            @Override
            public Plaintext decrypt(byte contentType, ByteBuffer bb, byte[] sequence) throws GeneralSecurityException {
                ByteBuffer pt;
                BadPaddingException reservedBPE;
                block15: {
                    int tagLen;
                    int cipheredLength;
                    Authenticator.MAC signer;
                    block14: {
                        int pos;
                        reservedBPE = null;
                        signer = (Authenticator.MAC)((Object)this.authenticator);
                        cipheredLength = bb.remaining();
                        tagLen = signer.macAlg().size;
                        if (tagLen != 0 && !this.sanityCheck(tagLen, cipheredLength)) {
                            reservedBPE = new BadPaddingException("ciphertext sanity check failed");
                        }
                        if (!bb.isReadOnly()) {
                            pt = bb.duplicate();
                            pos = bb.position();
                        } else {
                            pt = ByteBuffer.allocate(cipheredLength);
                            pos = 0;
                        }
                        try {
                            if (cipheredLength != this.cipher.update(bb, pt)) {
                                throw new RuntimeException("Unexpected number of plaintext bytes");
                            }
                        }
                        catch (ShortBufferException sbe) {
                            throw new RuntimeException("Cipher buffering error in JCE provider " + this.cipher.getProvider().getName(), sbe);
                        }
                        if (SSLLogger.isOn && SSLLogger.isOn("plaintext")) {
                            SSLLogger.fine("Padded plaintext after DECRYPTION", pt.duplicate().position(pos));
                        }
                        pt.position(pos);
                        try {
                            SSLCipher.removePadding(pt, tagLen, this.cipher.getBlockSize(), this.protocolVersion);
                        }
                        catch (BadPaddingException bpe) {
                            if (reservedBPE != null) break block14;
                            reservedBPE = bpe;
                        }
                    }
                    try {
                        if (tagLen != 0) {
                            SSLCipher.checkCBCMac(signer, pt, contentType, cipheredLength, sequence);
                        } else {
                            this.authenticator.increaseSequenceNumber();
                        }
                    }
                    catch (BadPaddingException bpe) {
                        if (reservedBPE != null) break block15;
                        reservedBPE = bpe;
                    }
                }
                if (reservedBPE != null) {
                    throw reservedBPE;
                }
                return new Plaintext(contentType, ProtocolVersion.NONE.major, ProtocolVersion.NONE.minor, -1, -1L, pt.slice());
            }

            @Override
            void dispose() {
                if (this.cipher != null) {
                    try {
                        this.cipher.doFinal();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }

            @Override
            int estimateFragmentSize(int packetSize, int headerSize) {
                int macLen = ((Authenticator.MAC)((Object)this.authenticator)).macAlg().size;
                return packetSize - headerSize - macLen - 1;
            }

            private boolean sanityCheck(int tagLen, int fragmentLen) {
                int blockSize = this.cipher.getBlockSize();
                if (fragmentLen % blockSize == 0) {
                    int minimal = tagLen + 1;
                    return fragmentLen >= (minimal = Math.max(minimal, blockSize));
                }
                return false;
            }
        }
    }

    private static final class T10BlockWriteCipherGenerator
    implements WriteCipherGenerator {
        private T10BlockWriteCipherGenerator() {
        }

        @Override
        public SSLWriteCipher createCipher(SSLCipher sslCipher, Authenticator authenticator, ProtocolVersion protocolVersion, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
            return new BlockWriteCipher(authenticator, protocolVersion, algorithm, key, params, random);
        }

        static final class BlockWriteCipher
        extends SSLWriteCipher {
            private final Cipher cipher;

            BlockWriteCipher(Authenticator authenticator, ProtocolVersion protocolVersion, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
                super(authenticator, protocolVersion);
                this.cipher = Cipher.getInstance(algorithm);
                this.cipher.init(1, key, params, random);
            }

            @Override
            public int encrypt(byte contentType, ByteBuffer bb) {
                int pos = bb.position();
                Authenticator.MAC signer = (Authenticator.MAC)((Object)this.authenticator);
                if (signer.macAlg().size != 0) {
                    SSLCipher.addMac(signer, bb, contentType);
                } else {
                    this.authenticator.increaseSequenceNumber();
                }
                int blockSize = this.cipher.getBlockSize();
                int len = SSLCipher.addPadding(bb, blockSize);
                bb.position(pos);
                if (SSLLogger.isOn && SSLLogger.isOn("plaintext")) {
                    SSLLogger.fine("Padded plaintext before ENCRYPTION", bb.duplicate());
                }
                ByteBuffer dup = bb.duplicate();
                try {
                    if (len != this.cipher.update(dup, bb)) {
                        throw new RuntimeException("Unexpected number of plaintext bytes");
                    }
                    if (bb.position() != dup.position()) {
                        throw new RuntimeException("Unexpected ByteBuffer position");
                    }
                }
                catch (ShortBufferException sbe) {
                    throw new RuntimeException("Cipher buffering error in JCE provider " + this.cipher.getProvider().getName(), sbe);
                }
                return len;
            }

            @Override
            void dispose() {
                if (this.cipher != null) {
                    try {
                        this.cipher.doFinal();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }

            @Override
            int getExplicitNonceSize() {
                return 0;
            }

            @Override
            int calculateFragmentSize(int packetLimit, int headerSize) {
                int macLen = ((Authenticator.MAC)((Object)this.authenticator)).macAlg().size;
                int blockSize = this.cipher.getBlockSize();
                int fragLen = packetLimit - headerSize;
                fragLen -= fragLen % blockSize;
                --fragLen;
                return fragLen -= macLen;
            }

            @Override
            int calculatePacketSize(int fragmentSize, int headerSize) {
                int macLen = ((Authenticator.MAC)((Object)this.authenticator)).macAlg().size;
                int paddedLen = fragmentSize + macLen + 1;
                int blockSize = this.cipher.getBlockSize();
                if (paddedLen % blockSize != 0) {
                    paddedLen += blockSize - 1;
                    paddedLen -= paddedLen % blockSize;
                }
                return headerSize + paddedLen;
            }

            @Override
            boolean isCBCMode() {
                return true;
            }
        }
    }

    private static final class T11BlockReadCipherGenerator
    implements ReadCipherGenerator {
        private T11BlockReadCipherGenerator() {
        }

        @Override
        public SSLReadCipher createCipher(SSLCipher sslCipher, Authenticator authenticator, ProtocolVersion protocolVersion, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
            return new BlockReadCipher(authenticator, protocolVersion, sslCipher, algorithm, key, params, random);
        }

        static final class BlockReadCipher
        extends SSLReadCipher {
            private final Cipher cipher;

            BlockReadCipher(Authenticator authenticator, ProtocolVersion protocolVersion, SSLCipher sslCipher, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
                super(authenticator, protocolVersion);
                this.cipher = Cipher.getInstance(algorithm);
                if (params == null) {
                    params = new IvParameterSpec(new byte[sslCipher.ivSize]);
                }
                this.cipher.init(2, key, params, random);
            }

            @Override
            public Plaintext decrypt(byte contentType, ByteBuffer bb, byte[] sequence) throws GeneralSecurityException {
                ByteBuffer pt;
                BadPaddingException reservedBPE;
                block15: {
                    int tagLen;
                    int cipheredLength;
                    Authenticator.MAC signer;
                    block14: {
                        int pos;
                        reservedBPE = null;
                        signer = (Authenticator.MAC)((Object)this.authenticator);
                        cipheredLength = bb.remaining();
                        tagLen = signer.macAlg().size;
                        if (tagLen != 0 && !this.sanityCheck(tagLen, cipheredLength)) {
                            reservedBPE = new BadPaddingException("ciphertext sanity check failed");
                        }
                        if (!bb.isReadOnly()) {
                            pt = bb.duplicate();
                            pos = bb.position();
                        } else {
                            pt = ByteBuffer.allocate(cipheredLength);
                            pos = 0;
                        }
                        try {
                            if (cipheredLength != this.cipher.update(bb, pt)) {
                                throw new RuntimeException("Unexpected number of plaintext bytes");
                            }
                        }
                        catch (ShortBufferException sbe) {
                            throw new RuntimeException("Cipher buffering error in JCE provider " + this.cipher.getProvider().getName(), sbe);
                        }
                        if (SSLLogger.isOn && SSLLogger.isOn("plaintext")) {
                            SSLLogger.fine("Padded plaintext after DECRYPTION", pt.duplicate().position(pos));
                        }
                        int blockSize = this.cipher.getBlockSize();
                        pt.position(pos += blockSize);
                        try {
                            SSLCipher.removePadding(pt, tagLen, blockSize, this.protocolVersion);
                        }
                        catch (BadPaddingException bpe) {
                            if (reservedBPE != null) break block14;
                            reservedBPE = bpe;
                        }
                    }
                    try {
                        if (tagLen != 0) {
                            SSLCipher.checkCBCMac(signer, pt, contentType, cipheredLength, sequence);
                        } else {
                            this.authenticator.increaseSequenceNumber();
                        }
                    }
                    catch (BadPaddingException bpe) {
                        if (reservedBPE != null) break block15;
                        reservedBPE = bpe;
                    }
                }
                if (reservedBPE != null) {
                    throw reservedBPE;
                }
                return new Plaintext(contentType, ProtocolVersion.NONE.major, ProtocolVersion.NONE.minor, -1, -1L, pt.slice());
            }

            @Override
            void dispose() {
                if (this.cipher != null) {
                    try {
                        this.cipher.doFinal();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }

            @Override
            int estimateFragmentSize(int packetSize, int headerSize) {
                int macLen = ((Authenticator.MAC)((Object)this.authenticator)).macAlg().size;
                int nonceSize = this.cipher.getBlockSize();
                return packetSize - headerSize - nonceSize - macLen - 1;
            }

            private boolean sanityCheck(int tagLen, int fragmentLen) {
                int blockSize = this.cipher.getBlockSize();
                if (fragmentLen % blockSize == 0) {
                    int minimal = tagLen + 1;
                    minimal = Math.max(minimal, blockSize);
                    return fragmentLen >= (minimal += blockSize);
                }
                return false;
            }
        }
    }

    private static final class T11BlockWriteCipherGenerator
    implements WriteCipherGenerator {
        private T11BlockWriteCipherGenerator() {
        }

        @Override
        public SSLWriteCipher createCipher(SSLCipher sslCipher, Authenticator authenticator, ProtocolVersion protocolVersion, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
            return new BlockWriteCipher(authenticator, protocolVersion, sslCipher, algorithm, key, params, random);
        }

        static final class BlockWriteCipher
        extends SSLWriteCipher {
            private final Cipher cipher;
            private final SecureRandom random;

            BlockWriteCipher(Authenticator authenticator, ProtocolVersion protocolVersion, SSLCipher sslCipher, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
                super(authenticator, protocolVersion);
                this.cipher = Cipher.getInstance(algorithm);
                this.random = random;
                if (params == null) {
                    params = new IvParameterSpec(new byte[sslCipher.ivSize]);
                }
                this.cipher.init(1, key, params, random);
            }

            @Override
            public int encrypt(byte contentType, ByteBuffer bb) {
                int pos = bb.position();
                Authenticator.MAC signer = (Authenticator.MAC)((Object)this.authenticator);
                if (signer.macAlg().size != 0) {
                    SSLCipher.addMac(signer, bb, contentType);
                } else {
                    this.authenticator.increaseSequenceNumber();
                }
                byte[] nonce = new byte[this.cipher.getBlockSize()];
                this.random.nextBytes(nonce);
                bb.position(pos -= nonce.length);
                bb.put(nonce);
                bb.position(pos);
                int blockSize = this.cipher.getBlockSize();
                int len = SSLCipher.addPadding(bb, blockSize);
                bb.position(pos);
                if (SSLLogger.isOn && SSLLogger.isOn("plaintext")) {
                    SSLLogger.fine("Padded plaintext before ENCRYPTION", bb.duplicate());
                }
                ByteBuffer dup = bb.duplicate();
                try {
                    if (len != this.cipher.update(dup, bb)) {
                        throw new RuntimeException("Unexpected number of plaintext bytes");
                    }
                    if (bb.position() != dup.position()) {
                        throw new RuntimeException("Unexpected ByteBuffer position");
                    }
                }
                catch (ShortBufferException sbe) {
                    throw new RuntimeException("Cipher buffering error in JCE provider " + this.cipher.getProvider().getName(), sbe);
                }
                return len;
            }

            @Override
            void dispose() {
                if (this.cipher != null) {
                    try {
                        this.cipher.doFinal();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }

            @Override
            int getExplicitNonceSize() {
                return this.cipher.getBlockSize();
            }

            @Override
            int calculateFragmentSize(int packetLimit, int headerSize) {
                int macLen = ((Authenticator.MAC)((Object)this.authenticator)).macAlg().size;
                int blockSize = this.cipher.getBlockSize();
                int fragLen = packetLimit - headerSize - blockSize;
                fragLen -= fragLen % blockSize;
                --fragLen;
                return fragLen -= macLen;
            }

            @Override
            int calculatePacketSize(int fragmentSize, int headerSize) {
                int macLen = ((Authenticator.MAC)((Object)this.authenticator)).macAlg().size;
                int paddedLen = fragmentSize + macLen + 1;
                int blockSize = this.cipher.getBlockSize();
                if (paddedLen % blockSize != 0) {
                    paddedLen += blockSize - 1;
                    paddedLen -= paddedLen % blockSize;
                }
                return headerSize + blockSize + paddedLen;
            }

            @Override
            boolean isCBCMode() {
                return true;
            }
        }
    }

    private static final class T12GcmReadCipherGenerator
    implements ReadCipherGenerator {
        private T12GcmReadCipherGenerator() {
        }

        @Override
        public SSLReadCipher createCipher(SSLCipher sslCipher, Authenticator authenticator, ProtocolVersion protocolVersion, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
            return new GcmReadCipher(authenticator, protocolVersion, sslCipher, algorithm, key, params, random);
        }

        static final class GcmReadCipher
        extends SSLReadCipher {
            private final Cipher cipher;
            private final int tagSize;
            private final Key key;
            private final byte[] fixedIv;
            private final int recordIvSize;
            private final SecureRandom random;

            GcmReadCipher(Authenticator authenticator, ProtocolVersion protocolVersion, SSLCipher sslCipher, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
                super(authenticator, protocolVersion);
                this.cipher = Cipher.getInstance(algorithm);
                this.tagSize = sslCipher.tagSize;
                this.key = key;
                this.fixedIv = ((IvParameterSpec)params).getIV();
                this.recordIvSize = sslCipher.ivSize - sslCipher.fixedIvSize;
                this.random = random;
            }

            @Override
            public Plaintext decrypt(byte contentType, ByteBuffer bb, byte[] sequence) throws GeneralSecurityException {
                int len;
                int pos;
                ByteBuffer pt;
                if (bb.remaining() < this.recordIvSize + this.tagSize) {
                    throw new BadPaddingException("Insufficient buffer remaining for AEAD cipher fragment (" + bb.remaining() + "). Needs to be more than or equal to IV size (" + this.recordIvSize + ") + tag size (" + this.tagSize + ")");
                }
                byte[] iv = Arrays.copyOf(this.fixedIv, this.fixedIv.length + this.recordIvSize);
                bb.get(iv, this.fixedIv.length, this.recordIvSize);
                GCMParameterSpec spec = new GCMParameterSpec(this.tagSize * 8, iv);
                try {
                    this.cipher.init(2, this.key, spec, this.random);
                }
                catch (InvalidAlgorithmParameterException | InvalidKeyException ikae) {
                    throw new RuntimeException("invalid key or spec in GCM mode", ikae);
                }
                byte[] aad = this.authenticator.acquireAuthenticationBytes(contentType, bb.remaining() - this.tagSize, sequence);
                this.cipher.updateAAD(aad);
                if (!bb.isReadOnly()) {
                    pt = bb.duplicate();
                    pos = bb.position();
                } else {
                    pt = ByteBuffer.allocate(bb.remaining());
                    pos = 0;
                }
                try {
                    len = this.cipher.doFinal(bb, pt);
                }
                catch (IllegalBlockSizeException ibse) {
                    throw new RuntimeException("Cipher error in AEAD mode \"" + ibse.getMessage() + " \"in JCE provider " + this.cipher.getProvider().getName());
                }
                catch (ShortBufferException sbe) {
                    throw new RuntimeException("Cipher buffering error in JCE provider " + this.cipher.getProvider().getName(), sbe);
                }
                pt.position(pos);
                pt.limit(pos + len);
                if (SSLLogger.isOn && SSLLogger.isOn("plaintext")) {
                    SSLLogger.fine("Plaintext after DECRYPTION", pt.duplicate());
                }
                return new Plaintext(contentType, ProtocolVersion.NONE.major, ProtocolVersion.NONE.minor, -1, -1L, pt.slice());
            }

            @Override
            int estimateFragmentSize(int packetSize, int headerSize) {
                return packetSize - headerSize - this.recordIvSize - this.tagSize;
            }
        }
    }

    private static final class T12GcmWriteCipherGenerator
    implements WriteCipherGenerator {
        private T12GcmWriteCipherGenerator() {
        }

        @Override
        public SSLWriteCipher createCipher(SSLCipher sslCipher, Authenticator authenticator, ProtocolVersion protocolVersion, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
            return new GcmWriteCipher(authenticator, protocolVersion, sslCipher, algorithm, key, params, random);
        }

        private static final class GcmWriteCipher
        extends SSLWriteCipher {
            private final Cipher cipher;
            private final int tagSize;
            private final Key key;
            private final byte[] fixedIv;
            private final int recordIvSize;
            private final SecureRandom random;

            GcmWriteCipher(Authenticator authenticator, ProtocolVersion protocolVersion, SSLCipher sslCipher, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
                super(authenticator, protocolVersion);
                this.cipher = Cipher.getInstance(algorithm);
                this.tagSize = sslCipher.tagSize;
                this.key = key;
                this.fixedIv = ((IvParameterSpec)params).getIV();
                this.recordIvSize = sslCipher.ivSize - sslCipher.fixedIvSize;
                this.random = random;
            }

            @Override
            public int encrypt(byte contentType, ByteBuffer bb) {
                int len;
                ByteBuffer dup;
                int outputSize;
                byte[] nonce = this.authenticator.sequenceNumber();
                byte[] iv = Arrays.copyOf(this.fixedIv, this.fixedIv.length + nonce.length);
                System.arraycopy(nonce, 0, iv, this.fixedIv.length, nonce.length);
                GCMParameterSpec spec = new GCMParameterSpec(this.tagSize * 8, iv);
                try {
                    this.cipher.init(1, this.key, spec, this.random);
                }
                catch (InvalidAlgorithmParameterException | InvalidKeyException ikae) {
                    throw new RuntimeException("invalid key or spec in GCM mode", ikae);
                }
                byte[] aad = this.authenticator.acquireAuthenticationBytes(contentType, bb.remaining(), null);
                this.cipher.updateAAD(aad);
                bb.position(bb.position() - nonce.length);
                bb.put(nonce);
                int pos = bb.position();
                if (SSLLogger.isOn && SSLLogger.isOn("plaintext")) {
                    SSLLogger.fine("Plaintext before ENCRYPTION", bb.duplicate());
                }
                if ((outputSize = this.cipher.getOutputSize((dup = bb.duplicate()).remaining())) > bb.remaining()) {
                    bb.limit(pos + outputSize);
                }
                try {
                    len = this.cipher.doFinal(dup, bb);
                }
                catch (BadPaddingException | IllegalBlockSizeException | ShortBufferException ibse) {
                    throw new RuntimeException("Cipher error in AEAD mode in JCE provider " + this.cipher.getProvider().getName(), ibse);
                }
                if (len != outputSize) {
                    throw new RuntimeException("Cipher buffering error in JCE provider " + this.cipher.getProvider().getName());
                }
                return len + nonce.length;
            }

            @Override
            int getExplicitNonceSize() {
                return this.recordIvSize;
            }

            @Override
            int calculateFragmentSize(int packetLimit, int headerSize) {
                return packetLimit - headerSize - this.recordIvSize - this.tagSize;
            }

            @Override
            int calculatePacketSize(int fragmentSize, int headerSize) {
                return fragmentSize + headerSize + this.recordIvSize + this.tagSize;
            }
        }
    }

    private static final class T13GcmReadCipherGenerator
    implements ReadCipherGenerator {
        private T13GcmReadCipherGenerator() {
        }

        @Override
        public SSLReadCipher createCipher(SSLCipher sslCipher, Authenticator authenticator, ProtocolVersion protocolVersion, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
            return new GcmReadCipher(authenticator, protocolVersion, sslCipher, algorithm, key, params, random);
        }

        static final class GcmReadCipher
        extends SSLReadCipher {
            private final Cipher cipher;
            private final int tagSize;
            private final Key key;
            private final byte[] iv;
            private final SecureRandom random;

            GcmReadCipher(Authenticator authenticator, ProtocolVersion protocolVersion, SSLCipher sslCipher, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
                super(authenticator, protocolVersion);
                this.cipher = Cipher.getInstance(algorithm);
                this.tagSize = sslCipher.tagSize;
                this.key = key;
                this.iv = ((IvParameterSpec)params).getIV();
                this.random = random;
                this.keyLimitCountdown = cipherLimits.getOrDefault(algorithm.toUpperCase(Locale.ENGLISH) + ":" + tag[0], 0L);
                if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                    SSLLogger.fine("KeyLimit read side: algorithm = " + algorithm + ":" + tag[0] + "\ncountdown value = " + this.keyLimitCountdown, new Object[0]);
                }
                if (this.keyLimitCountdown > 0L) {
                    this.keyLimitEnabled = true;
                }
            }

            @Override
            public Plaintext decrypt(byte contentType, ByteBuffer bb, byte[] sequence) throws GeneralSecurityException {
                int i;
                int len;
                int pos;
                ByteBuffer pt;
                if (contentType == ContentType.CHANGE_CIPHER_SPEC.id) {
                    return new Plaintext(contentType, ProtocolVersion.NONE.major, ProtocolVersion.NONE.minor, -1, -1L, bb.slice());
                }
                if (bb.remaining() <= this.tagSize) {
                    throw new BadPaddingException("Insufficient buffer remaining for AEAD cipher fragment (" + bb.remaining() + "). Needs to be more than tag size (" + this.tagSize + ")");
                }
                byte[] sn = sequence;
                if (sn == null) {
                    sn = this.authenticator.sequenceNumber();
                }
                byte[] nonce = (byte[])this.iv.clone();
                int offset = nonce.length - sn.length;
                for (int i2 = 0; i2 < sn.length; ++i2) {
                    int n = offset + i2;
                    nonce[n] = (byte)(nonce[n] ^ sn[i2]);
                }
                GCMParameterSpec spec = new GCMParameterSpec(this.tagSize * 8, nonce);
                try {
                    this.cipher.init(2, this.key, spec, this.random);
                }
                catch (InvalidAlgorithmParameterException | InvalidKeyException ikae) {
                    throw new RuntimeException("invalid key or spec in GCM mode", ikae);
                }
                byte[] aad = this.authenticator.acquireAuthenticationBytes(contentType, bb.remaining(), sn);
                this.cipher.updateAAD(aad);
                if (!bb.isReadOnly()) {
                    pt = bb.duplicate();
                    pos = bb.position();
                } else {
                    pt = ByteBuffer.allocate(bb.remaining());
                    pos = 0;
                }
                try {
                    len = this.cipher.doFinal(bb, pt);
                }
                catch (IllegalBlockSizeException ibse) {
                    throw new RuntimeException("Cipher error in AEAD mode \"" + ibse.getMessage() + " \"in JCE provider " + this.cipher.getProvider().getName());
                }
                catch (ShortBufferException sbe) {
                    throw new RuntimeException("Cipher buffering error in JCE provider " + this.cipher.getProvider().getName(), sbe);
                }
                pt.position(pos);
                pt.limit(pos + len);
                for (i = pt.limit() - 1; i > 0 && pt.get(i) == 0; --i) {
                }
                if (i < pos + 1) {
                    throw new BadPaddingException("Incorrect inner plaintext: no content type");
                }
                contentType = pt.get(i);
                pt.limit(i);
                if (SSLLogger.isOn && SSLLogger.isOn("plaintext")) {
                    SSLLogger.fine("Plaintext after DECRYPTION", pt.duplicate());
                }
                if (this.keyLimitEnabled) {
                    this.keyLimitCountdown -= (long)len;
                }
                return new Plaintext(contentType, ProtocolVersion.NONE.major, ProtocolVersion.NONE.minor, -1, -1L, pt.slice());
            }

            @Override
            int estimateFragmentSize(int packetSize, int headerSize) {
                return packetSize - headerSize - this.tagSize;
            }
        }
    }

    private static final class T13GcmWriteCipherGenerator
    implements WriteCipherGenerator {
        private T13GcmWriteCipherGenerator() {
        }

        @Override
        public SSLWriteCipher createCipher(SSLCipher sslCipher, Authenticator authenticator, ProtocolVersion protocolVersion, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
            return new GcmWriteCipher(authenticator, protocolVersion, sslCipher, algorithm, key, params, random);
        }

        private static final class GcmWriteCipher
        extends SSLWriteCipher {
            private final Cipher cipher;
            private final int tagSize;
            private final Key key;
            private final byte[] iv;
            private final SecureRandom random;

            GcmWriteCipher(Authenticator authenticator, ProtocolVersion protocolVersion, SSLCipher sslCipher, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
                super(authenticator, protocolVersion);
                this.cipher = Cipher.getInstance(algorithm);
                this.tagSize = sslCipher.tagSize;
                this.key = key;
                this.iv = ((IvParameterSpec)params).getIV();
                this.random = random;
                this.keyLimitCountdown = cipherLimits.getOrDefault(algorithm.toUpperCase(Locale.ENGLISH) + ":" + tag[0], 0L);
                if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                    SSLLogger.fine("KeyLimit write side: algorithm = " + algorithm + ":" + tag[0] + "\ncountdown value = " + this.keyLimitCountdown, new Object[0]);
                }
                if (this.keyLimitCountdown > 0L) {
                    this.keyLimitEnabled = true;
                }
            }

            @Override
            public int encrypt(byte contentType, ByteBuffer bb) {
                int len;
                byte[] sn = this.authenticator.sequenceNumber();
                byte[] nonce = (byte[])this.iv.clone();
                int offset = nonce.length - sn.length;
                for (int i = 0; i < sn.length; ++i) {
                    int n = offset + i;
                    nonce[n] = (byte)(nonce[n] ^ sn[i]);
                }
                GCMParameterSpec spec = new GCMParameterSpec(this.tagSize * 8, nonce);
                try {
                    this.cipher.init(1, this.key, spec, this.random);
                }
                catch (InvalidAlgorithmParameterException | InvalidKeyException ikae) {
                    throw new RuntimeException("invalid key or spec in GCM mode", ikae);
                }
                int outputSize = this.cipher.getOutputSize(bb.remaining());
                byte[] aad = this.authenticator.acquireAuthenticationBytes(contentType, outputSize, sn);
                this.cipher.updateAAD(aad);
                int pos = bb.position();
                if (SSLLogger.isOn && SSLLogger.isOn("plaintext")) {
                    SSLLogger.fine("Plaintext before ENCRYPTION", bb.duplicate());
                }
                ByteBuffer dup = bb.duplicate();
                if (outputSize > bb.remaining()) {
                    bb.limit(pos + outputSize);
                }
                try {
                    len = this.cipher.doFinal(dup, bb);
                }
                catch (BadPaddingException | IllegalBlockSizeException | ShortBufferException ibse) {
                    throw new RuntimeException("Cipher error in AEAD mode in JCE provider " + this.cipher.getProvider().getName(), ibse);
                }
                if (len != outputSize) {
                    throw new RuntimeException("Cipher buffering error in JCE provider " + this.cipher.getProvider().getName());
                }
                if (this.keyLimitEnabled) {
                    this.keyLimitCountdown -= (long)len;
                }
                return len;
            }

            @Override
            int getExplicitNonceSize() {
                return 0;
            }

            @Override
            int calculateFragmentSize(int packetLimit, int headerSize) {
                return packetLimit - headerSize - this.tagSize;
            }

            @Override
            int calculatePacketSize(int fragmentSize, int headerSize) {
                return fragmentSize + headerSize + this.tagSize;
            }
        }
    }

    private static final class T12CC20P1305ReadCipherGenerator
    implements ReadCipherGenerator {
        private T12CC20P1305ReadCipherGenerator() {
        }

        @Override
        public SSLReadCipher createCipher(SSLCipher sslCipher, Authenticator authenticator, ProtocolVersion protocolVersion, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
            return new CC20P1305ReadCipher(authenticator, protocolVersion, sslCipher, algorithm, key, params, random);
        }

        static final class CC20P1305ReadCipher
        extends SSLReadCipher {
            private final Cipher cipher;
            private final int tagSize;
            private final Key key;
            private final byte[] iv;
            private final SecureRandom random;

            CC20P1305ReadCipher(Authenticator authenticator, ProtocolVersion protocolVersion, SSLCipher sslCipher, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
                super(authenticator, protocolVersion);
                this.cipher = Cipher.getInstance(algorithm);
                this.tagSize = sslCipher.tagSize;
                this.key = key;
                this.iv = ((IvParameterSpec)params).getIV();
                this.random = random;
            }

            @Override
            public Plaintext decrypt(byte contentType, ByteBuffer bb, byte[] sequence) throws GeneralSecurityException {
                int len;
                int pos;
                ByteBuffer pt;
                if (bb.remaining() <= this.tagSize) {
                    throw new BadPaddingException("Insufficient buffer remaining for AEAD cipher fragment (" + bb.remaining() + "). Needs to be more than tag size (" + this.tagSize + ")");
                }
                byte[] sn = sequence;
                if (sn == null) {
                    sn = this.authenticator.sequenceNumber();
                }
                byte[] nonce = new byte[this.iv.length];
                System.arraycopy(sn, 0, nonce, nonce.length - sn.length, sn.length);
                for (int i = 0; i < nonce.length; ++i) {
                    int n = i;
                    nonce[n] = (byte)(nonce[n] ^ this.iv[i]);
                }
                IvParameterSpec spec = new IvParameterSpec(nonce);
                try {
                    this.cipher.init(2, this.key, spec, this.random);
                }
                catch (InvalidAlgorithmParameterException | InvalidKeyException ikae) {
                    throw new RuntimeException("invalid key or spec in AEAD mode", ikae);
                }
                byte[] aad = this.authenticator.acquireAuthenticationBytes(contentType, bb.remaining() - this.tagSize, sequence);
                this.cipher.updateAAD(aad);
                if (!bb.isReadOnly()) {
                    pt = bb.duplicate();
                    pos = bb.position();
                } else {
                    pt = ByteBuffer.allocate(bb.remaining());
                    pos = 0;
                }
                try {
                    len = this.cipher.doFinal(bb, pt);
                }
                catch (IllegalBlockSizeException ibse) {
                    throw new RuntimeException("Cipher error in AEAD mode \"" + ibse.getMessage() + " \"in JCE provider " + this.cipher.getProvider().getName());
                }
                catch (ShortBufferException sbe) {
                    throw new RuntimeException("Cipher buffering error in JCE provider " + this.cipher.getProvider().getName(), sbe);
                }
                pt.position(pos);
                pt.limit(pos + len);
                if (SSLLogger.isOn && SSLLogger.isOn("plaintext")) {
                    SSLLogger.fine("Plaintext after DECRYPTION", pt.duplicate());
                }
                return new Plaintext(contentType, ProtocolVersion.NONE.major, ProtocolVersion.NONE.minor, -1, -1L, pt.slice());
            }

            @Override
            int estimateFragmentSize(int packetSize, int headerSize) {
                return packetSize - headerSize - this.tagSize;
            }
        }
    }

    private static final class T13CC20P1305ReadCipherGenerator
    implements ReadCipherGenerator {
        private T13CC20P1305ReadCipherGenerator() {
        }

        @Override
        public SSLReadCipher createCipher(SSLCipher sslCipher, Authenticator authenticator, ProtocolVersion protocolVersion, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
            return new CC20P1305ReadCipher(authenticator, protocolVersion, sslCipher, algorithm, key, params, random);
        }

        static final class CC20P1305ReadCipher
        extends SSLReadCipher {
            private final Cipher cipher;
            private final int tagSize;
            private final Key key;
            private final byte[] iv;
            private final SecureRandom random;

            CC20P1305ReadCipher(Authenticator authenticator, ProtocolVersion protocolVersion, SSLCipher sslCipher, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
                super(authenticator, protocolVersion);
                this.cipher = Cipher.getInstance(algorithm);
                this.tagSize = sslCipher.tagSize;
                this.key = key;
                this.iv = ((IvParameterSpec)params).getIV();
                this.random = random;
            }

            @Override
            public Plaintext decrypt(byte contentType, ByteBuffer bb, byte[] sequence) throws GeneralSecurityException {
                int i;
                int len;
                int pos;
                ByteBuffer pt;
                if (contentType == ContentType.CHANGE_CIPHER_SPEC.id) {
                    return new Plaintext(contentType, ProtocolVersion.NONE.major, ProtocolVersion.NONE.minor, -1, -1L, bb.slice());
                }
                if (bb.remaining() <= this.tagSize) {
                    throw new BadPaddingException("Insufficient buffer remaining for AEAD cipher fragment (" + bb.remaining() + "). Needs to be more than tag size (" + this.tagSize + ")");
                }
                byte[] sn = sequence;
                if (sn == null) {
                    sn = this.authenticator.sequenceNumber();
                }
                byte[] nonce = new byte[this.iv.length];
                System.arraycopy(sn, 0, nonce, nonce.length - sn.length, sn.length);
                for (int i2 = 0; i2 < nonce.length; ++i2) {
                    int n = i2;
                    nonce[n] = (byte)(nonce[n] ^ this.iv[i2]);
                }
                IvParameterSpec spec = new IvParameterSpec(nonce);
                try {
                    this.cipher.init(2, this.key, spec, this.random);
                }
                catch (InvalidAlgorithmParameterException | InvalidKeyException ikae) {
                    throw new RuntimeException("invalid key or spec in AEAD mode", ikae);
                }
                byte[] aad = this.authenticator.acquireAuthenticationBytes(contentType, bb.remaining(), sn);
                this.cipher.updateAAD(aad);
                if (!bb.isReadOnly()) {
                    pt = bb.duplicate();
                    pos = bb.position();
                } else {
                    pt = ByteBuffer.allocate(bb.remaining());
                    pos = 0;
                }
                try {
                    len = this.cipher.doFinal(bb, pt);
                }
                catch (IllegalBlockSizeException ibse) {
                    throw new RuntimeException("Cipher error in AEAD mode \"" + ibse.getMessage() + " \"in JCE provider " + this.cipher.getProvider().getName());
                }
                catch (ShortBufferException sbe) {
                    throw new RuntimeException("Cipher buffering error in JCE provider " + this.cipher.getProvider().getName(), sbe);
                }
                pt.position(pos);
                pt.limit(pos + len);
                for (i = pt.limit() - 1; i > 0 && pt.get(i) == 0; --i) {
                }
                if (i < pos + 1) {
                    throw new BadPaddingException("Incorrect inner plaintext: no content type");
                }
                contentType = pt.get(i);
                pt.limit(i);
                if (SSLLogger.isOn && SSLLogger.isOn("plaintext")) {
                    SSLLogger.fine("Plaintext after DECRYPTION", pt.duplicate());
                }
                return new Plaintext(contentType, ProtocolVersion.NONE.major, ProtocolVersion.NONE.minor, -1, -1L, pt.slice());
            }

            @Override
            int estimateFragmentSize(int packetSize, int headerSize) {
                return packetSize - headerSize - this.tagSize;
            }
        }
    }

    private static final class T12CC20P1305WriteCipherGenerator
    implements WriteCipherGenerator {
        private T12CC20P1305WriteCipherGenerator() {
        }

        @Override
        public SSLWriteCipher createCipher(SSLCipher sslCipher, Authenticator authenticator, ProtocolVersion protocolVersion, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
            return new CC20P1305WriteCipher(authenticator, protocolVersion, sslCipher, algorithm, key, params, random);
        }

        private static final class CC20P1305WriteCipher
        extends SSLWriteCipher {
            private final Cipher cipher;
            private final int tagSize;
            private final Key key;
            private final byte[] iv;
            private final SecureRandom random;

            CC20P1305WriteCipher(Authenticator authenticator, ProtocolVersion protocolVersion, SSLCipher sslCipher, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
                super(authenticator, protocolVersion);
                this.cipher = Cipher.getInstance(algorithm);
                this.tagSize = sslCipher.tagSize;
                this.key = key;
                this.iv = ((IvParameterSpec)params).getIV();
                this.random = random;
                this.keyLimitCountdown = cipherLimits.getOrDefault(algorithm.toUpperCase(Locale.ENGLISH) + ":" + tag[0], 0L);
                if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                    SSLLogger.fine("algorithm = " + algorithm + ":" + tag[0] + "\ncountdown value = " + this.keyLimitCountdown, new Object[0]);
                }
                if (this.keyLimitCountdown > 0L) {
                    this.keyLimitEnabled = true;
                }
            }

            @Override
            public int encrypt(byte contentType, ByteBuffer bb) {
                int len;
                ByteBuffer dup;
                int outputSize;
                byte[] sn = this.authenticator.sequenceNumber();
                byte[] nonce = new byte[this.iv.length];
                System.arraycopy(sn, 0, nonce, nonce.length - sn.length, sn.length);
                for (int i = 0; i < nonce.length; ++i) {
                    int n = i;
                    nonce[n] = (byte)(nonce[n] ^ this.iv[i]);
                }
                IvParameterSpec spec = new IvParameterSpec(nonce);
                try {
                    this.cipher.init(1, this.key, spec, this.random);
                }
                catch (InvalidAlgorithmParameterException | InvalidKeyException ikae) {
                    throw new RuntimeException("invalid key or spec in AEAD mode", ikae);
                }
                byte[] aad = this.authenticator.acquireAuthenticationBytes(contentType, bb.remaining(), null);
                this.cipher.updateAAD(aad);
                int pos = bb.position();
                if (SSLLogger.isOn && SSLLogger.isOn("plaintext")) {
                    SSLLogger.fine("Plaintext before ENCRYPTION", bb.duplicate());
                }
                if ((outputSize = this.cipher.getOutputSize((dup = bb.duplicate()).remaining())) > bb.remaining()) {
                    bb.limit(pos + outputSize);
                }
                try {
                    len = this.cipher.doFinal(dup, bb);
                }
                catch (BadPaddingException | IllegalBlockSizeException | ShortBufferException ibse) {
                    throw new RuntimeException("Cipher error in AEAD mode in JCE provider " + this.cipher.getProvider().getName(), ibse);
                }
                if (len != outputSize) {
                    throw new RuntimeException("Cipher buffering error in JCE provider " + this.cipher.getProvider().getName());
                }
                return len;
            }

            @Override
            int getExplicitNonceSize() {
                return 0;
            }

            @Override
            int calculateFragmentSize(int packetLimit, int headerSize) {
                return packetLimit - headerSize - this.tagSize;
            }

            @Override
            int calculatePacketSize(int fragmentSize, int headerSize) {
                return fragmentSize + headerSize + this.tagSize;
            }
        }
    }

    private static final class T13CC20P1305WriteCipherGenerator
    implements WriteCipherGenerator {
        private T13CC20P1305WriteCipherGenerator() {
        }

        @Override
        public SSLWriteCipher createCipher(SSLCipher sslCipher, Authenticator authenticator, ProtocolVersion protocolVersion, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
            return new CC20P1305WriteCipher(authenticator, protocolVersion, sslCipher, algorithm, key, params, random);
        }

        private static final class CC20P1305WriteCipher
        extends SSLWriteCipher {
            private final Cipher cipher;
            private final int tagSize;
            private final Key key;
            private final byte[] iv;
            private final SecureRandom random;

            CC20P1305WriteCipher(Authenticator authenticator, ProtocolVersion protocolVersion, SSLCipher sslCipher, String algorithm, Key key, AlgorithmParameterSpec params, SecureRandom random) throws GeneralSecurityException {
                super(authenticator, protocolVersion);
                this.cipher = Cipher.getInstance(algorithm);
                this.tagSize = sslCipher.tagSize;
                this.key = key;
                this.iv = ((IvParameterSpec)params).getIV();
                this.random = random;
                this.keyLimitCountdown = cipherLimits.getOrDefault(algorithm.toUpperCase(Locale.ENGLISH) + ":" + tag[0], 0L);
                if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                    SSLLogger.fine("algorithm = " + algorithm + ":" + tag[0] + "\ncountdown value = " + this.keyLimitCountdown, new Object[0]);
                }
                if (this.keyLimitCountdown > 0L) {
                    this.keyLimitEnabled = true;
                }
            }

            @Override
            public int encrypt(byte contentType, ByteBuffer bb) {
                int len;
                byte[] sn = this.authenticator.sequenceNumber();
                byte[] nonce = new byte[this.iv.length];
                System.arraycopy(sn, 0, nonce, nonce.length - sn.length, sn.length);
                for (int i = 0; i < nonce.length; ++i) {
                    int n = i;
                    nonce[n] = (byte)(nonce[n] ^ this.iv[i]);
                }
                IvParameterSpec spec = new IvParameterSpec(nonce);
                try {
                    this.cipher.init(1, this.key, spec, this.random);
                }
                catch (InvalidAlgorithmParameterException | InvalidKeyException ikae) {
                    throw new RuntimeException("invalid key or spec in AEAD mode", ikae);
                }
                int outputSize = this.cipher.getOutputSize(bb.remaining());
                byte[] aad = this.authenticator.acquireAuthenticationBytes(contentType, outputSize, sn);
                this.cipher.updateAAD(aad);
                int pos = bb.position();
                if (SSLLogger.isOn && SSLLogger.isOn("plaintext")) {
                    SSLLogger.fine("Plaintext before ENCRYPTION", bb.duplicate());
                }
                ByteBuffer dup = bb.duplicate();
                if (outputSize > bb.remaining()) {
                    bb.limit(pos + outputSize);
                }
                try {
                    len = this.cipher.doFinal(dup, bb);
                }
                catch (BadPaddingException | IllegalBlockSizeException | ShortBufferException ibse) {
                    throw new RuntimeException("Cipher error in AEAD mode in JCE provider " + this.cipher.getProvider().getName(), ibse);
                }
                if (len != outputSize) {
                    throw new RuntimeException("Cipher buffering error in JCE provider " + this.cipher.getProvider().getName());
                }
                if (this.keyLimitEnabled) {
                    this.keyLimitCountdown -= (long)len;
                }
                return len;
            }

            @Override
            int getExplicitNonceSize() {
                return 0;
            }

            @Override
            int calculateFragmentSize(int packetLimit, int headerSize) {
                return packetLimit - headerSize - this.tagSize;
            }

            @Override
            int calculatePacketSize(int fragmentSize, int headerSize) {
                return fragmentSize + headerSize + this.tagSize;
            }
        }
    }
}

