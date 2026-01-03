/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import sun.security.util.BitArray;
import sun.security.util.DerEncoder;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.HexDumpEncoder;
import sun.security.x509.AlgorithmId;

public class X509Key
implements PublicKey,
DerEncoder {
    private static final long serialVersionUID = -5359250853002055002L;
    protected AlgorithmId algid;
    @Deprecated
    protected byte[] key = null;
    @Deprecated
    private int unusedBits = 0;
    private transient BitArray bitStringKey = null;
    protected byte[] encodedKey;

    public X509Key() {
    }

    private X509Key(AlgorithmId algid, BitArray key) {
        this.algid = algid;
        this.setKey(key);
        this.encode();
    }

    protected void setKey(BitArray key) {
        this.bitStringKey = (BitArray)key.clone();
        this.key = key.toByteArray();
        int remaining = key.length() % 8;
        this.unusedBits = remaining == 0 ? 0 : 8 - remaining;
    }

    protected BitArray getKey() {
        this.bitStringKey = new BitArray(this.key.length * 8 - this.unusedBits, this.key);
        return (BitArray)this.bitStringKey.clone();
    }

    public static PublicKey parse(DerValue in) throws IOException {
        PublicKey subjectKey;
        if (in.tag != 48) {
            throw new IOException("corrupt subject key");
        }
        AlgorithmId algorithm = AlgorithmId.parse(in.data.getDerValue());
        try {
            subjectKey = X509Key.buildX509Key(algorithm, in.data.getUnalignedBitString());
        }
        catch (InvalidKeyException e) {
            throw new IOException("subject key, " + e.getMessage(), e);
        }
        if (in.data.available() != 0) {
            throw new IOException("excess subject key");
        }
        return subjectKey;
    }

    protected void parseKeyBits() throws InvalidKeyException {
        this.encode();
    }

    static PublicKey buildX509Key(AlgorithmId algid, BitArray key) throws IOException, InvalidKeyException {
        DerOutputStream x509EncodedKeyStream = new DerOutputStream();
        X509Key.encode(x509EncodedKeyStream, algid, key);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(x509EncodedKeyStream.toByteArray());
        try {
            KeyFactory keyFac = KeyFactory.getInstance(algid.getName());
            return keyFac.generatePublic(x509KeySpec);
        }
        catch (NoSuchAlgorithmException keyFac) {
        }
        catch (InvalidKeySpecException e) {
            throw new InvalidKeyException(e.getMessage(), e);
        }
        String classname = "";
        try {
            Object inst;
            Class<?> keyClass;
            block11: {
                Provider sunProvider = Security.getProvider("SUN");
                if (sunProvider == null) {
                    throw new InstantiationException();
                }
                classname = sunProvider.getProperty("PublicKey.X.509." + algid.getName());
                if (classname == null) {
                    throw new InstantiationException();
                }
                keyClass = null;
                try {
                    keyClass = Class.forName(classname);
                }
                catch (ClassNotFoundException e) {
                    ClassLoader cl = ClassLoader.getSystemClassLoader();
                    if (cl == null) break block11;
                    keyClass = cl.loadClass(classname);
                }
            }
            Object v0 = inst = keyClass != null ? keyClass.newInstance() : null;
            if (inst instanceof X509Key) {
                X509Key result = inst;
                result.algid = algid;
                result.setKey(key);
                result.parseKeyBits();
                return result;
            }
        }
        catch (ClassNotFoundException | InstantiationException sunProvider) {
        }
        catch (IllegalAccessException e) {
            throw new IOException(classname + " [internal error]");
        }
        return new X509Key(algid, key);
    }

    @Override
    public String getAlgorithm() {
        return this.algid.getName();
    }

    public AlgorithmId getAlgorithmId() {
        return this.algid;
    }

    @Override
    public final void encode(DerOutputStream out) {
        X509Key.encode(out, this.algid, this.getKey());
    }

    @Override
    public byte[] getEncoded() {
        return (byte[])this.getEncodedInternal().clone();
    }

    public byte[] getEncodedInternal() {
        byte[] encoded = this.encodedKey;
        if (encoded == null) {
            DerOutputStream out = new DerOutputStream();
            this.encode(out);
            this.encodedKey = encoded = out.toByteArray();
        }
        return encoded;
    }

    @Override
    public String getFormat() {
        return "X.509";
    }

    public byte[] encode() {
        return (byte[])this.getEncodedInternal().clone();
    }

    public String toString() {
        HexDumpEncoder encoder = new HexDumpEncoder();
        return "algorithm = " + this.algid.toString() + ", unparsed keybits = \n" + encoder.encodeBuffer(this.key);
    }

    void decode(DerValue val) throws InvalidKeyException {
        try {
            if (val.tag != 48) {
                throw new InvalidKeyException("invalid key format");
            }
            this.algid = AlgorithmId.parse(val.data.getDerValue());
            this.setKey(val.data.getUnalignedBitString());
            this.parseKeyBits();
            if (val.data.available() != 0) {
                throw new InvalidKeyException("excess key data");
            }
        }
        catch (IOException e) {
            throw new InvalidKeyException("Unable to decode key", e);
        }
    }

    public void decode(byte[] encodedKey) throws InvalidKeyException {
        try {
            this.decode(new DerValue(encodedKey));
        }
        catch (IOException e) {
            throw new InvalidKeyException("Unable to decode key", e);
        }
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.write(this.getEncoded());
    }

    private void readObject(ObjectInputStream stream) throws IOException {
        try {
            this.decode(new DerValue(stream));
        }
        catch (InvalidKeyException e) {
            throw new IOException("deserialized key is invalid", e);
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Key)) {
            return false;
        }
        byte[] thisEncoded = this.getEncodedInternal();
        byte[] otherEncoded = obj instanceof X509Key ? ((X509Key)obj).getEncodedInternal() : ((Key)obj).getEncoded();
        return Arrays.equals(thisEncoded, otherEncoded);
    }

    public int hashCode() {
        byte[] b1 = this.getEncodedInternal();
        int r = b1.length;
        for (int i = 0; i < b1.length; ++i) {
            r += (b1[i] & 0xFF) * 37;
        }
        return r;
    }

    static void encode(DerOutputStream out, AlgorithmId algid, BitArray key) {
        DerOutputStream tmp = new DerOutputStream();
        algid.encode(tmp);
        tmp.putUnalignedBitString(key);
        out.write((byte)48, tmp);
    }
}

