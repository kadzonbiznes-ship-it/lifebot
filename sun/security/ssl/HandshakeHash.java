/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import javax.crypto.SecretKey;
import sun.security.ssl.CipherSuite;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.SSLHandshake;
import sun.security.util.MessageDigestSpi2;

final class HandshakeHash {
    private TranscriptHash transcriptHash = new CacheOnlyHash();
    private LinkedList<byte[]> reserves = new LinkedList();
    private boolean hasBeenUsed = false;

    HandshakeHash() {
    }

    void determine(ProtocolVersion protocolVersion, CipherSuite cipherSuite) {
        TranscriptHash transcriptHash = this.transcriptHash;
        if (!(transcriptHash instanceof CacheOnlyHash)) {
            throw new IllegalStateException("Not expected instance of transcript hash");
        }
        CacheOnlyHash coh = (CacheOnlyHash)transcriptHash;
        this.transcriptHash = protocolVersion.useTLS13PlusSpec() ? new T13HandshakeHash(cipherSuite) : (protocolVersion.useTLS12PlusSpec() ? new T12HandshakeHash(cipherSuite) : (protocolVersion.useTLS10PlusSpec() ? new T10HandshakeHash(cipherSuite) : new S30HandshakeHash(cipherSuite)));
        byte[] reserved = coh.baos.toByteArray();
        if (reserved.length != 0) {
            this.transcriptHash.update(reserved, 0, reserved.length);
        }
    }

    HandshakeHash copy() {
        if (this.transcriptHash instanceof CacheOnlyHash) {
            HandshakeHash result = new HandshakeHash();
            result.transcriptHash = ((CacheOnlyHash)this.transcriptHash).copy();
            result.reserves = new LinkedList<byte[]>(this.reserves);
            result.hasBeenUsed = this.hasBeenUsed;
            return result;
        }
        throw new IllegalStateException("Hash does not support copying");
    }

    void receive(byte[] input) {
        this.reserves.add(Arrays.copyOf(input, input.length));
    }

    void receive(ByteBuffer input, int length) {
        if (input.hasArray()) {
            int from = input.position() + input.arrayOffset();
            int to = from + length;
            this.reserves.add(Arrays.copyOfRange(input.array(), from, to));
        } else {
            int inPos = input.position();
            byte[] holder = new byte[length];
            input.get(holder);
            input.position(inPos);
            this.reserves.add(Arrays.copyOf(holder, holder.length));
        }
    }

    void receive(ByteBuffer input) {
        this.receive(input, input.remaining());
    }

    void push(byte[] input) {
        this.reserves.push(Arrays.copyOf(input, input.length));
    }

    byte[] removeLastReceived() {
        return this.reserves.removeLast();
    }

    void deliver(byte[] input) {
        this.update();
        this.transcriptHash.update(input, 0, input.length);
    }

    void deliver(byte[] input, int offset, int length) {
        this.update();
        this.transcriptHash.update(input, offset, length);
    }

    void deliver(ByteBuffer input) {
        this.update();
        if (input.hasArray()) {
            this.transcriptHash.update(input.array(), input.position() + input.arrayOffset(), input.remaining());
        } else {
            int inPos = input.position();
            byte[] holder = new byte[input.remaining()];
            input.get(holder);
            input.position(inPos);
            this.transcriptHash.update(holder, 0, holder.length);
        }
    }

    void utilize() {
        if (this.hasBeenUsed) {
            return;
        }
        if (this.reserves.size() != 0) {
            byte[] holder = this.reserves.remove();
            this.transcriptHash.update(holder, 0, holder.length);
            this.hasBeenUsed = true;
        }
    }

    void consume() {
        if (this.hasBeenUsed) {
            this.hasBeenUsed = false;
            return;
        }
        if (this.reserves.size() != 0) {
            byte[] holder = this.reserves.remove();
            this.transcriptHash.update(holder, 0, holder.length);
        }
    }

    void update() {
        while (this.reserves.size() != 0) {
            byte[] holder = this.reserves.remove();
            this.transcriptHash.update(holder, 0, holder.length);
        }
        this.hasBeenUsed = false;
    }

    byte[] digest() {
        return this.transcriptHash.digest();
    }

    void finish() {
        this.transcriptHash = new CacheOnlyHash();
        this.reserves = new LinkedList();
        this.hasBeenUsed = false;
    }

    byte[] archived() {
        return this.transcriptHash.archived();
    }

    byte[] digest(String algorithm) {
        T10HandshakeHash hh = (T10HandshakeHash)this.transcriptHash;
        return hh.digest(algorithm);
    }

    byte[] digest(String algorithm, SecretKey masterSecret) {
        S30HandshakeHash hh = (S30HandshakeHash)this.transcriptHash;
        return hh.digest(algorithm, masterSecret);
    }

    byte[] digest(boolean useClientLabel, SecretKey masterSecret) {
        S30HandshakeHash hh = (S30HandshakeHash)this.transcriptHash;
        return hh.digest(useClientLabel, masterSecret);
    }

    public boolean isHashable(byte handshakeType) {
        return handshakeType != SSLHandshake.HELLO_REQUEST.id && handshakeType != SSLHandshake.HELLO_VERIFY_REQUEST.id;
    }

    private static final class CacheOnlyHash
    implements TranscriptHash {
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        CacheOnlyHash() {
        }

        @Override
        public void update(byte[] input, int offset, int length) {
            this.baos.write(input, offset, length);
        }

        @Override
        public byte[] digest() {
            throw new IllegalStateException("Not expected call to handshake hash digest");
        }

        @Override
        public byte[] archived() {
            return this.baos.toByteArray();
        }

        CacheOnlyHash copy() {
            CacheOnlyHash result = new CacheOnlyHash();
            try {
                this.baos.writeTo(result.baos);
            }
            catch (IOException ex) {
                throw new RuntimeException("unable to clone hash state");
            }
            return result;
        }
    }

    static interface TranscriptHash {
        public void update(byte[] var1, int var2, int var3);

        public byte[] digest();

        public byte[] archived();
    }

    static final class T13HandshakeHash
    implements TranscriptHash {
        private final TranscriptHash transcriptHash;

        T13HandshakeHash(CipherSuite cipherSuite) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance(cipherSuite.hashAlg.name);
            }
            catch (NoSuchAlgorithmException nsae) {
                throw new RuntimeException("Hash algorithm " + cipherSuite.hashAlg.name + " is not available", nsae);
            }
            this.transcriptHash = md instanceof Cloneable ? new CloneableHash(md) : new NonCloneableHash(md);
        }

        @Override
        public void update(byte[] input, int offset, int length) {
            this.transcriptHash.update(input, offset, length);
        }

        @Override
        public byte[] digest() {
            return this.transcriptHash.digest();
        }

        @Override
        public byte[] archived() {
            throw new UnsupportedOperationException("TLS 1.3 does not require archived.");
        }
    }

    static final class T12HandshakeHash
    implements TranscriptHash {
        private final TranscriptHash transcriptHash;
        private final ByteArrayOutputStream baos;

        T12HandshakeHash(CipherSuite cipherSuite) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance(cipherSuite.hashAlg.name);
            }
            catch (NoSuchAlgorithmException nsae) {
                throw new RuntimeException("Hash algorithm " + cipherSuite.hashAlg.name + " is not available", nsae);
            }
            if (md instanceof Cloneable) {
                this.transcriptHash = new CloneableHash(md);
                this.baos = new ByteArrayOutputStream();
            } else {
                this.transcriptHash = new NonCloneableHash(md);
                this.baos = null;
            }
        }

        @Override
        public void update(byte[] input, int offset, int length) {
            this.transcriptHash.update(input, offset, length);
            if (this.baos != null) {
                this.baos.write(input, offset, length);
            }
        }

        @Override
        public byte[] digest() {
            return this.transcriptHash.digest();
        }

        @Override
        public byte[] archived() {
            if (this.baos != null) {
                return this.baos.toByteArray();
            }
            return this.transcriptHash.archived();
        }
    }

    static final class T10HandshakeHash
    implements TranscriptHash {
        private final TranscriptHash md5;
        private final TranscriptHash sha;
        private final ByteArrayOutputStream baos;

        T10HandshakeHash(CipherSuite cipherSuite) {
            MessageDigest mdSHA;
            MessageDigest mdMD5;
            try {
                mdMD5 = MessageDigest.getInstance("MD5");
                mdSHA = MessageDigest.getInstance("SHA");
            }
            catch (NoSuchAlgorithmException nsae) {
                throw new RuntimeException("Hash algorithm MD5 or SHA is not available", nsae);
            }
            boolean hasArchived = false;
            if (mdMD5 instanceof Cloneable) {
                this.md5 = new CloneableHash(mdMD5);
            } else {
                hasArchived = true;
                this.md5 = new NonCloneableHash(mdMD5);
            }
            if (mdSHA instanceof Cloneable) {
                this.sha = new CloneableHash(mdSHA);
            } else {
                hasArchived = true;
                this.sha = new NonCloneableHash(mdSHA);
            }
            this.baos = hasArchived ? null : new ByteArrayOutputStream();
        }

        @Override
        public void update(byte[] input, int offset, int length) {
            this.md5.update(input, offset, length);
            this.sha.update(input, offset, length);
            if (this.baos != null) {
                this.baos.write(input, offset, length);
            }
        }

        @Override
        public byte[] digest() {
            byte[] digest = new byte[36];
            System.arraycopy(this.md5.digest(), 0, digest, 0, 16);
            System.arraycopy(this.sha.digest(), 0, digest, 16, 20);
            return digest;
        }

        byte[] digest(String algorithm) {
            if ("RSA".equalsIgnoreCase(algorithm)) {
                return this.digest();
            }
            return this.sha.digest();
        }

        @Override
        public byte[] archived() {
            if (this.baos != null) {
                return this.baos.toByteArray();
            }
            if (this.md5 instanceof NonCloneableHash) {
                return this.md5.archived();
            }
            return this.sha.archived();
        }
    }

    static final class S30HandshakeHash
    implements TranscriptHash {
        static final byte[] MD5_pad1 = S30HandshakeHash.genPad(54, 48);
        static final byte[] MD5_pad2 = S30HandshakeHash.genPad(92, 48);
        static final byte[] SHA_pad1 = S30HandshakeHash.genPad(54, 40);
        static final byte[] SHA_pad2 = S30HandshakeHash.genPad(92, 40);
        private static final byte[] SSL_CLIENT = new byte[]{67, 76, 78, 84};
        private static final byte[] SSL_SERVER = new byte[]{83, 82, 86, 82};
        private final MessageDigest mdMD5;
        private final MessageDigest mdSHA;
        private final TranscriptHash md5;
        private final TranscriptHash sha;
        private final ByteArrayOutputStream baos;

        S30HandshakeHash(CipherSuite cipherSuite) {
            try {
                this.mdMD5 = MessageDigest.getInstance("MD5");
                this.mdSHA = MessageDigest.getInstance("SHA");
            }
            catch (NoSuchAlgorithmException nsae) {
                throw new RuntimeException("Hash algorithm MD5 or SHA is not available", nsae);
            }
            boolean hasArchived = false;
            if (this.mdMD5 instanceof Cloneable) {
                this.md5 = new CloneableHash(this.mdMD5);
            } else {
                hasArchived = true;
                this.md5 = new NonCloneableHash(this.mdMD5);
            }
            if (this.mdSHA instanceof Cloneable) {
                this.sha = new CloneableHash(this.mdSHA);
            } else {
                hasArchived = true;
                this.sha = new NonCloneableHash(this.mdSHA);
            }
            this.baos = hasArchived ? null : new ByteArrayOutputStream();
        }

        @Override
        public void update(byte[] input, int offset, int length) {
            this.md5.update(input, offset, length);
            this.sha.update(input, offset, length);
            if (this.baos != null) {
                this.baos.write(input, offset, length);
            }
        }

        @Override
        public byte[] digest() {
            byte[] digest = new byte[36];
            System.arraycopy(this.md5.digest(), 0, digest, 0, 16);
            System.arraycopy(this.sha.digest(), 0, digest, 16, 20);
            return digest;
        }

        @Override
        public byte[] archived() {
            if (this.baos != null) {
                return this.baos.toByteArray();
            }
            if (this.md5 instanceof NonCloneableHash) {
                return this.md5.archived();
            }
            return this.sha.archived();
        }

        byte[] digest(boolean useClientLabel, SecretKey masterSecret) {
            MessageDigest md5Clone = this.cloneMd5();
            MessageDigest shaClone = this.cloneSha();
            if (useClientLabel) {
                md5Clone.update(SSL_CLIENT);
                shaClone.update(SSL_CLIENT);
            } else {
                md5Clone.update(SSL_SERVER);
                shaClone.update(SSL_SERVER);
            }
            S30HandshakeHash.updateDigest(md5Clone, MD5_pad1, MD5_pad2, masterSecret);
            S30HandshakeHash.updateDigest(shaClone, SHA_pad1, SHA_pad2, masterSecret);
            byte[] digest = new byte[36];
            System.arraycopy(md5Clone.digest(), 0, digest, 0, 16);
            System.arraycopy(shaClone.digest(), 0, digest, 16, 20);
            return digest;
        }

        byte[] digest(String algorithm, SecretKey masterSecret) {
            if ("RSA".equalsIgnoreCase(algorithm)) {
                MessageDigest md5Clone = this.cloneMd5();
                MessageDigest shaClone = this.cloneSha();
                S30HandshakeHash.updateDigest(md5Clone, MD5_pad1, MD5_pad2, masterSecret);
                S30HandshakeHash.updateDigest(shaClone, SHA_pad1, SHA_pad2, masterSecret);
                byte[] digest = new byte[36];
                System.arraycopy(md5Clone.digest(), 0, digest, 0, 16);
                System.arraycopy(shaClone.digest(), 0, digest, 16, 20);
                return digest;
            }
            MessageDigest shaClone = this.cloneSha();
            S30HandshakeHash.updateDigest(shaClone, SHA_pad1, SHA_pad2, masterSecret);
            return shaClone.digest();
        }

        private static byte[] genPad(int b, int count) {
            byte[] padding = new byte[count];
            Arrays.fill(padding, (byte)b);
            return padding;
        }

        private MessageDigest cloneMd5() {
            MessageDigest md5Clone;
            if (this.mdMD5 instanceof Cloneable) {
                try {
                    md5Clone = (MessageDigest)this.mdMD5.clone();
                }
                catch (CloneNotSupportedException ex) {
                    throw new RuntimeException("MessageDigest does no support clone operation");
                }
            }
            try {
                md5Clone = MessageDigest.getInstance("MD5");
            }
            catch (NoSuchAlgorithmException nsae) {
                throw new RuntimeException("Hash algorithm MD5 is not available", nsae);
            }
            md5Clone.update(this.md5.archived());
            return md5Clone;
        }

        private MessageDigest cloneSha() {
            MessageDigest shaClone;
            if (this.mdSHA instanceof Cloneable) {
                try {
                    shaClone = (MessageDigest)this.mdSHA.clone();
                }
                catch (CloneNotSupportedException ex) {
                    throw new RuntimeException("MessageDigest does no support clone operation");
                }
            }
            try {
                shaClone = MessageDigest.getInstance("SHA");
            }
            catch (NoSuchAlgorithmException nsae) {
                throw new RuntimeException("Hash algorithm SHA is not available", nsae);
            }
            shaClone.update(this.sha.archived());
            return shaClone;
        }

        private static void updateDigest(MessageDigest md, byte[] pad1, byte[] pad2, SecretKey masterSecret) {
            byte[] keyBytes;
            byte[] byArray = keyBytes = "RAW".equals(masterSecret.getFormat()) ? masterSecret.getEncoded() : null;
            if (keyBytes != null) {
                md.update(keyBytes);
            } else {
                S30HandshakeHash.digestKey(md, masterSecret);
            }
            md.update(pad1);
            byte[] temp = md.digest();
            if (keyBytes != null) {
                md.update(keyBytes);
            } else {
                S30HandshakeHash.digestKey(md, masterSecret);
            }
            md.update(pad2);
            md.update(temp);
        }

        private static void digestKey(MessageDigest md, SecretKey key) {
            try {
                if (!(md instanceof MessageDigestSpi2)) {
                    throw new Exception("Digest does not support implUpdate(SecretKey)");
                }
                ((MessageDigestSpi2)((Object)md)).engineUpdate(key);
            }
            catch (Exception e) {
                throw new RuntimeException("Could not obtain encoded key and MessageDigest cannot digest key", e);
            }
        }
    }

    static final class NonCloneableHash
    implements TranscriptHash {
        private final MessageDigest md;
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        NonCloneableHash(MessageDigest md) {
            this.md = md;
        }

        @Override
        public void update(byte[] input, int offset, int length) {
            this.baos.write(input, offset, length);
        }

        @Override
        public byte[] digest() {
            byte[] bytes = this.baos.toByteArray();
            this.md.reset();
            return this.md.digest(bytes);
        }

        @Override
        public byte[] archived() {
            return this.baos.toByteArray();
        }
    }

    static final class CloneableHash
    implements TranscriptHash {
        private final MessageDigest md;

        CloneableHash(MessageDigest md) {
            this.md = md;
        }

        @Override
        public void update(byte[] input, int offset, int length) {
            this.md.update(input, offset, length);
        }

        @Override
        public byte[] digest() {
            try {
                return ((MessageDigest)this.md.clone()).digest();
            }
            catch (CloneNotSupportedException ex) {
                return new byte[0];
            }
        }

        @Override
        public byte[] archived() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}

