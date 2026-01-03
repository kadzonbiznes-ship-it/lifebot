/*
 * Decompiled with CFR 0.152.
 */
package sun.security.pkcs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyRep;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import jdk.internal.access.SharedSecrets;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.InternalPrivateKey;
import sun.security.x509.AlgorithmId;

public class PKCS8Key
implements PrivateKey,
InternalPrivateKey {
    private static final long serialVersionUID = -3836890099307167124L;
    protected AlgorithmId algid;
    protected byte[] key;
    protected byte[] encodedKey;
    private static final int V1 = 0;
    private static final int V2 = 1;

    protected PKCS8Key() {
    }

    protected PKCS8Key(byte[] input) throws InvalidKeyException {
        try {
            this.decode(new DerValue(input));
        }
        catch (IOException e) {
            throw new InvalidKeyException("Unable to decode key", e);
        }
    }

    private void decode(DerValue val) throws InvalidKeyException {
        try {
            if (val.tag != 48) {
                throw new InvalidKeyException("invalid key format");
            }
            int version = val.data.getInteger();
            if (version != 0 && version != 1) {
                throw new InvalidKeyException("unknown version: " + version);
            }
            this.algid = AlgorithmId.parse(val.data.getDerValue());
            this.key = val.data.getOctetString();
            if (val.data.available() == 0) {
                return;
            }
            DerValue next = val.data.getDerValue();
            if (next.isContextSpecific((byte)0)) {
                if (val.data.available() == 0) {
                    return;
                }
                next = val.data.getDerValue();
            }
            if (next.isContextSpecific((byte)1)) {
                if (version == 0) {
                    throw new InvalidKeyException("publicKey seen in v1");
                }
                if (val.data.available() == 0) {
                    return;
                }
            }
            try {
                throw new InvalidKeyException("Extra bytes");
            }
            catch (IOException e) {
                throw new InvalidKeyException("Unable to decode key", e);
            }
        }
        finally {
            if (val != null) {
                val.clear();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static PrivateKey parseKey(byte[] encoded) throws IOException {
        try {
            PKCS8Key rawKey = new PKCS8Key(encoded);
            byte[] internal = rawKey.getEncodedInternal();
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(internal);
            PrivateKey result = null;
            try {
                result = KeyFactory.getInstance(rawKey.algid.getName()).generatePrivate(pkcs8KeySpec);
            }
            catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                result = rawKey;
            }
            finally {
                if (result != rawKey) {
                    rawKey.clear();
                }
                SharedSecrets.getJavaSecuritySpecAccess().clearEncodedKeySpec(pkcs8KeySpec);
            }
            return result;
        }
        catch (InvalidKeyException e) {
            throw new IOException("corrupt private key", e);
        }
    }

    @Override
    public String getAlgorithm() {
        return this.algid.getName();
    }

    public AlgorithmId getAlgorithmId() {
        return this.algid;
    }

    @Override
    public byte[] getEncoded() {
        return (byte[])this.getEncodedInternal().clone();
    }

    @Override
    public String getFormat() {
        return "PKCS#8";
    }

    private synchronized byte[] getEncodedInternal() {
        if (this.encodedKey == null) {
            DerOutputStream tmp = new DerOutputStream();
            tmp.putInteger(0);
            this.algid.encode(tmp);
            tmp.putOctetString(this.key);
            DerValue out = DerValue.wrap((byte)48, tmp);
            this.encodedKey = out.toByteArray();
            out.clear();
        }
        return this.encodedKey;
    }

    protected Object writeReplace() throws ObjectStreamException {
        return new KeyRep(KeyRep.Type.PRIVATE, this.getAlgorithm(), this.getFormat(), this.getEncodedInternal());
    }

    private void readObject(ObjectInputStream stream) throws IOException {
        try {
            this.decode(new DerValue(stream));
        }
        catch (InvalidKeyException e) {
            throw new IOException("deserialized key is invalid", e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof PKCS8Key) {
            return MessageDigest.isEqual(this.getEncodedInternal(), ((PKCS8Key)object).getEncodedInternal());
        }
        if (object instanceof Key) {
            byte[] otherEncoded = ((Key)object).getEncoded();
            try {
                boolean bl = MessageDigest.isEqual(this.getEncodedInternal(), otherEncoded);
                return bl;
            }
            finally {
                if (otherEncoded != null) {
                    Arrays.fill(otherEncoded, (byte)0);
                }
            }
        }
        return false;
    }

    public int hashCode() {
        return Arrays.hashCode(this.getEncodedInternal());
    }

    public void clear() {
        if (this.encodedKey != null) {
            Arrays.fill(this.encodedKey, (byte)0);
        }
        Arrays.fill(this.key, (byte)0);
    }
}

