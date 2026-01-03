/*
 * Decompiled with CFR 0.152.
 */
package java.security;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.InvalidKeyException;
import java.security.MessageDigestSpi;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.Objects;
import javax.crypto.SecretKey;
import sun.security.jca.GetInstance;
import sun.security.util.Debug;
import sun.security.util.MessageDigestSpi2;

public abstract class MessageDigest
extends MessageDigestSpi {
    private static final Debug pdebug = Debug.getInstance("provider", "Provider");
    private static final boolean skipDebug = Debug.isOn("engine=") && !Debug.isOn("messagedigest");
    private final String algorithm;
    private static final int INITIAL = 0;
    private static final int IN_PROGRESS = 1;
    private int state = 0;
    private Provider provider;

    protected MessageDigest(String algorithm) {
        this.algorithm = algorithm;
    }

    private MessageDigest(String algorithm, Provider p) {
        this.algorithm = algorithm;
        this.provider = p;
    }

    public static MessageDigest getInstance(String algorithm) throws NoSuchAlgorithmException {
        MessageDigest md;
        Objects.requireNonNull(algorithm, "null algorithm name");
        GetInstance.Instance instance = GetInstance.getInstance("MessageDigest", MessageDigestSpi.class, algorithm);
        Object object = instance.impl;
        if (object instanceof MessageDigest) {
            MessageDigest messageDigest;
            md = messageDigest = (MessageDigest)object;
            md.provider = instance.provider;
        } else {
            md = Delegate.of((MessageDigestSpi)instance.impl, algorithm, instance.provider);
        }
        if (!skipDebug && pdebug != null) {
            pdebug.println("MessageDigest." + algorithm + " algorithm from: " + md.provider.getName());
        }
        return md;
    }

    public static MessageDigest getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        MessageDigest md;
        Objects.requireNonNull(algorithm, "null algorithm name");
        if (provider == null || provider.isEmpty()) {
            throw new IllegalArgumentException("missing provider");
        }
        GetInstance.Instance instance = GetInstance.getInstance("MessageDigest", MessageDigestSpi.class, algorithm, provider);
        Object object = instance.impl;
        if (object instanceof MessageDigest) {
            MessageDigest messageDigest;
            md = messageDigest = (MessageDigest)object;
            md.provider = instance.provider;
        } else {
            md = Delegate.of((MessageDigestSpi)instance.impl, algorithm, instance.provider);
        }
        return md;
    }

    public static MessageDigest getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        if (provider == null) {
            throw new IllegalArgumentException("missing provider");
        }
        Object[] objs = Security.getImpl(algorithm, "MessageDigest", provider);
        Object object = objs[0];
        if (object instanceof MessageDigest) {
            MessageDigest md = (MessageDigest)object;
            md.provider = (Provider)objs[1];
            return md;
        }
        return Delegate.of((MessageDigestSpi)objs[0], algorithm, (Provider)objs[1]);
    }

    public final Provider getProvider() {
        return this.provider;
    }

    public void update(byte input) {
        this.engineUpdate(input);
        this.state = 1;
    }

    public void update(byte[] input, int offset, int len) {
        if (input == null) {
            throw new IllegalArgumentException("No input buffer given");
        }
        if (input.length - offset < len) {
            throw new IllegalArgumentException("Input buffer too short");
        }
        this.engineUpdate(input, offset, len);
        this.state = 1;
    }

    public void update(byte[] input) {
        this.engineUpdate(input, 0, input.length);
        this.state = 1;
    }

    public final void update(ByteBuffer input) {
        if (input == null) {
            throw new NullPointerException();
        }
        this.engineUpdate(input);
        this.state = 1;
    }

    public byte[] digest() {
        byte[] result = this.engineDigest();
        this.state = 0;
        return result;
    }

    public int digest(byte[] buf, int offset, int len) throws DigestException {
        if (buf == null) {
            throw new IllegalArgumentException("No output buffer given");
        }
        if (buf.length - offset < len) {
            throw new IllegalArgumentException("Output buffer too small for specified offset and length");
        }
        int numBytes = this.engineDigest(buf, offset, len);
        this.state = 0;
        return numBytes;
    }

    public byte[] digest(byte[] input) {
        this.update(input);
        return this.digest();
    }

    private String getProviderName() {
        return this.provider == null ? "(no provider)" : this.provider.getName();
    }

    public String toString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream p = new PrintStream(baos);
        p.print(this.algorithm + " Message Digest from " + this.getProviderName() + ", ");
        switch (this.state) {
            case 0: {
                p.print("<initialized>");
                break;
            }
            case 1: {
                p.print("<in progress>");
            }
        }
        p.println();
        return baos.toString();
    }

    public static boolean isEqual(byte[] digesta, byte[] digestb) {
        if (digesta == digestb) {
            return true;
        }
        if (digesta == null || digestb == null) {
            return false;
        }
        int lenA = digesta.length;
        int lenB = digestb.length;
        if (lenB == 0) {
            return lenA == 0;
        }
        int result = 0;
        result |= lenA - lenB;
        for (int i = 0; i < lenA; ++i) {
            int indexB = (i - lenB >>> 31) * i;
            result |= digesta[i] ^ digestb[indexB];
        }
        return result == 0;
    }

    public void reset() {
        this.engineReset();
        this.state = 0;
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public final int getDigestLength() {
        int digestLen = this.engineGetDigestLength();
        if (digestLen == 0) {
            try {
                MessageDigest md = (MessageDigest)this.clone();
                byte[] digest = md.digest();
                return digest.length;
            }
            catch (CloneNotSupportedException e) {
                return digestLen;
            }
        }
        return digestLen;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        }
        throw new CloneNotSupportedException();
    }

    private static class Delegate
    extends MessageDigest
    implements MessageDigestSpi2 {
        private final MessageDigestSpi digestSpi;

        static Delegate of(MessageDigestSpi digestSpi, String algo, Provider p) {
            Objects.requireNonNull(digestSpi);
            boolean isCloneable = digestSpi instanceof Cloneable;
            if (isCloneable && p.getName().startsWith("SunPKCS11") && p.getClass().getModule().getName().equals("jdk.crypto.cryptoki")) {
                try {
                    digestSpi.clone();
                }
                catch (CloneNotSupportedException cnse) {
                    isCloneable = false;
                }
            }
            return isCloneable ? new CloneableDelegate(digestSpi, algo, p) : new Delegate(digestSpi, algo, p);
        }

        private Delegate(MessageDigestSpi digestSpi, String algorithm, Provider p) {
            super(algorithm, p);
            this.digestSpi = digestSpi;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            if (this instanceof Cloneable) {
                CloneableDelegate that = new CloneableDelegate((MessageDigestSpi)this.digestSpi.clone(), this.algorithm, this.provider);
                that.state = this.state;
                return that;
            }
            throw new CloneNotSupportedException();
        }

        @Override
        protected int engineGetDigestLength() {
            return this.digestSpi.engineGetDigestLength();
        }

        @Override
        protected void engineUpdate(byte input) {
            this.digestSpi.engineUpdate(input);
        }

        @Override
        protected void engineUpdate(byte[] input, int offset, int len) {
            this.digestSpi.engineUpdate(input, offset, len);
        }

        @Override
        protected void engineUpdate(ByteBuffer input) {
            this.digestSpi.engineUpdate(input);
        }

        @Override
        public void engineUpdate(SecretKey key) throws InvalidKeyException {
            if (!(this.digestSpi instanceof MessageDigestSpi2)) {
                throw new UnsupportedOperationException("Digest does not support update of SecretKey object");
            }
            ((MessageDigestSpi2)((Object)this.digestSpi)).engineUpdate(key);
        }

        @Override
        protected byte[] engineDigest() {
            return this.digestSpi.engineDigest();
        }

        @Override
        protected int engineDigest(byte[] buf, int offset, int len) throws DigestException {
            return this.digestSpi.engineDigest(buf, offset, len);
        }

        @Override
        protected void engineReset() {
            this.digestSpi.engineReset();
        }

        private static final class CloneableDelegate
        extends Delegate
        implements Cloneable {
            private CloneableDelegate(MessageDigestSpi digestSpi, String algorithm, Provider p) {
                super(digestSpi, algorithm, p);
            }
        }
    }
}

