/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import sun.security.pkcs.EncryptedPrivateKeyInfo;
import sun.security.pkcs12.PKCS12KeyStore;
import sun.security.provider.KeyProtector;
import sun.security.util.Debug;
import sun.security.util.IOUtils;
import sun.security.util.KeyStoreDelegator;

public abstract sealed class JavaKeyStore
extends KeyStoreSpi {
    private static final Debug debug = Debug.getInstance("keystore");
    private static final int MAGIC = -17957139;
    private static final int VERSION_1 = 1;
    private static final int VERSION_2 = 2;
    private final Hashtable<String, Object> entries = new Hashtable();

    JavaKeyStore() {
    }

    abstract String convertAlias(String var1);

    @Override
    public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        Object entry = this.entries.get(this.convertAlias(alias));
        if (!(entry instanceof KeyEntry)) {
            return null;
        }
        KeyEntry keyEntry = (KeyEntry)entry;
        if (password == null) {
            throw new UnrecoverableKeyException("Password must not be null");
        }
        byte[] passwordBytes = this.convertToBytes(password);
        KeyProtector keyProtector = new KeyProtector(passwordBytes);
        byte[] encrBytes = keyEntry.protectedPrivKey;
        try {
            EncryptedPrivateKeyInfo encrInfo = new EncryptedPrivateKeyInfo(encrBytes);
            Key key = keyProtector.recover(encrInfo);
            return key;
        }
        catch (IOException ioe) {
            throw new UnrecoverableKeyException("Private key not stored as PKCS #8 EncryptedPrivateKeyInfo");
        }
        finally {
            Arrays.fill(passwordBytes, (byte)0);
        }
    }

    @Override
    public Certificate[] engineGetCertificateChain(String alias) {
        Object entry = this.entries.get(this.convertAlias(alias));
        if (entry instanceof KeyEntry) {
            KeyEntry keyEntry = (KeyEntry)entry;
            if (keyEntry.chain == null) {
                return null;
            }
            return (Certificate[])keyEntry.chain.clone();
        }
        return null;
    }

    @Override
    public Certificate engineGetCertificate(String alias) {
        Object entry = this.entries.get(this.convertAlias(alias));
        if (entry != null) {
            if (entry instanceof TrustedCertEntry) {
                return ((TrustedCertEntry)entry).cert;
            }
            if (((KeyEntry)entry).chain == null) {
                return null;
            }
            return ((KeyEntry)entry).chain[0];
        }
        return null;
    }

    @Override
    public Date engineGetCreationDate(String alias) {
        Object entry = this.entries.get(this.convertAlias(alias));
        if (entry != null) {
            if (entry instanceof TrustedCertEntry) {
                return new Date(((TrustedCertEntry)entry).date.getTime());
            }
            return new Date(((KeyEntry)entry).date.getTime());
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
        byte[] passwordBytes = null;
        if (!(key instanceof PrivateKey)) {
            throw new KeyStoreException("Cannot store non-PrivateKeys");
        }
        if (password == null) {
            throw new KeyStoreException("password can't be null");
        }
        try {
            Hashtable<String, Object> hashtable = this.entries;
            synchronized (hashtable) {
                KeyEntry entry = new KeyEntry();
                entry.date = new Date();
                passwordBytes = this.convertToBytes(password);
                KeyProtector keyProtector = new KeyProtector(passwordBytes);
                entry.protectedPrivKey = keyProtector.protect(key);
                entry.chain = chain != null && chain.length != 0 ? (Certificate[])chain.clone() : null;
                this.entries.put(this.convertAlias(alias), entry);
            }
            if (passwordBytes == null) return;
        }
        catch (NoSuchAlgorithmException nsae) {
            try {
                throw new KeyStoreException("Key protection algorithm not found");
            }
            catch (Throwable throwable) {
                if (passwordBytes == null) throw throwable;
                Arrays.fill(passwordBytes, (byte)0);
                throw throwable;
            }
        }
        Arrays.fill(passwordBytes, (byte)0);
        return;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
        Hashtable<String, Object> hashtable = this.entries;
        synchronized (hashtable) {
            try {
                new EncryptedPrivateKeyInfo(key);
            }
            catch (IOException ioe) {
                throw new KeyStoreException("key is not encoded as EncryptedPrivateKeyInfo");
            }
            KeyEntry entry = new KeyEntry();
            entry.date = new Date();
            entry.protectedPrivKey = (byte[])key.clone();
            entry.chain = chain != null && chain.length != 0 ? (Certificate[])chain.clone() : null;
            this.entries.put(this.convertAlias(alias), entry);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
        Hashtable<String, Object> hashtable = this.entries;
        synchronized (hashtable) {
            Object entry = this.entries.get(this.convertAlias(alias));
            if (entry instanceof KeyEntry) {
                throw new KeyStoreException("Cannot overwrite own certificate");
            }
            TrustedCertEntry trustedCertEntry = new TrustedCertEntry();
            trustedCertEntry.cert = cert;
            trustedCertEntry.date = new Date();
            this.entries.put(this.convertAlias(alias), trustedCertEntry);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void engineDeleteEntry(String alias) throws KeyStoreException {
        Hashtable<String, Object> hashtable = this.entries;
        synchronized (hashtable) {
            this.entries.remove(this.convertAlias(alias));
        }
    }

    @Override
    public Enumeration<String> engineAliases() {
        return this.entries.keys();
    }

    @Override
    public boolean engineContainsAlias(String alias) {
        return this.entries.containsKey(this.convertAlias(alias));
    }

    @Override
    public int engineSize() {
        return this.entries.size();
    }

    @Override
    public boolean engineIsKeyEntry(String alias) {
        Object entry = this.entries.get(this.convertAlias(alias));
        return entry instanceof KeyEntry;
    }

    @Override
    public boolean engineIsCertificateEntry(String alias) {
        Object entry = this.entries.get(this.convertAlias(alias));
        return entry instanceof TrustedCertEntry;
    }

    @Override
    public String engineGetCertificateAlias(Certificate cert) {
        for (Map.Entry<String, Object> e : this.entries.entrySet()) {
            Certificate certElem;
            String alias = e.getKey();
            Object entry = e.getValue();
            if (entry instanceof TrustedCertEntry) {
                certElem = ((TrustedCertEntry)entry).cert;
            } else {
                if (((KeyEntry)entry).chain == null) continue;
                certElem = ((KeyEntry)entry).chain[0];
            }
            if (!certElem.equals(cert)) continue;
            return alias;
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void engineStore(OutputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        Hashtable<String, Object> hashtable = this.entries;
        synchronized (hashtable) {
            if (password == null) {
                throw new IllegalArgumentException("password can't be null");
            }
            MessageDigest md = this.getPreKeyedHash(password);
            DataOutputStream dos = new DataOutputStream(new DigestOutputStream(stream, md));
            dos.writeInt(-17957139);
            dos.writeInt(2);
            dos.writeInt(this.entries.size());
            for (Map.Entry<String, Object> e : this.entries.entrySet()) {
                byte[] encoded;
                String alias = e.getKey();
                Object entry = e.getValue();
                if (entry instanceof KeyEntry) {
                    dos.writeInt(1);
                    dos.writeUTF(alias);
                    dos.writeLong(((KeyEntry)entry).date.getTime());
                    dos.writeInt(((KeyEntry)entry).protectedPrivKey.length);
                    dos.write(((KeyEntry)entry).protectedPrivKey);
                    int chainLen = ((KeyEntry)entry).chain == null ? 0 : ((KeyEntry)entry).chain.length;
                    dos.writeInt(chainLen);
                    for (int i = 0; i < chainLen; ++i) {
                        encoded = ((KeyEntry)entry).chain[i].getEncoded();
                        dos.writeUTF(((KeyEntry)entry).chain[i].getType());
                        dos.writeInt(encoded.length);
                        dos.write(encoded);
                    }
                    continue;
                }
                dos.writeInt(2);
                dos.writeUTF(alias);
                dos.writeLong(((TrustedCertEntry)entry).date.getTime());
                encoded = ((TrustedCertEntry)entry).cert.getEncoded();
                dos.writeUTF(((TrustedCertEntry)entry).cert.getType());
                dos.writeInt(encoded.length);
                dos.write(encoded);
            }
            byte[] digest = md.digest();
            dos.write(digest);
            dos.flush();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void engineLoad(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        Hashtable<String, Object> hashtable = this.entries;
        synchronized (hashtable) {
            byte[] actual;
            byte[] computed;
            DataInputStream dis;
            MessageDigest md = null;
            CertificateFactory cf = null;
            Hashtable<String, CertificateFactory> cfs = null;
            int trustedKeyCount = 0;
            int privateKeyCount = 0;
            if (stream == null) {
                return;
            }
            if (password != null) {
                md = this.getPreKeyedHash(password);
                dis = new DataInputStream(new DigestInputStream(stream, md));
            } else {
                dis = new DataInputStream(stream);
            }
            int xMagic = dis.readInt();
            int xVersion = dis.readInt();
            if (xMagic != -17957139 || xVersion != 1 && xVersion != 2) {
                throw new IOException("Invalid keystore format");
            }
            if (xVersion == 1) {
                cf = CertificateFactory.getInstance("X509");
            } else {
                cfs = new Hashtable<String, CertificateFactory>(3);
            }
            this.entries.clear();
            int count = dis.readInt();
            for (int i = 0; i < count; ++i) {
                ByteArrayInputStream bais;
                byte[] encoded;
                String alias;
                Object entry;
                int tag = dis.readInt();
                if (tag == 1) {
                    ++privateKeyCount;
                    entry = new KeyEntry();
                    alias = dis.readUTF();
                    ((KeyEntry)entry).date = new Date(dis.readLong());
                    ((KeyEntry)entry).protectedPrivKey = IOUtils.readExactlyNBytes(dis, dis.readInt());
                    int numOfCerts = dis.readInt();
                    if (numOfCerts > 0) {
                        ArrayList<Certificate> certs = new ArrayList<Certificate>(Math.min(numOfCerts, 10));
                        for (int j = 0; j < numOfCerts; ++j) {
                            if (xVersion == 2) {
                                String certType = dis.readUTF();
                                if (cfs.containsKey(certType)) {
                                    cf = (CertificateFactory)cfs.get(certType);
                                } else {
                                    cf = CertificateFactory.getInstance(certType);
                                    cfs.put(certType, cf);
                                }
                            }
                            encoded = IOUtils.readExactlyNBytes(dis, dis.readInt());
                            bais = new ByteArrayInputStream(encoded);
                            certs.add(cf.generateCertificate(bais));
                            bais.close();
                        }
                        ((KeyEntry)entry).chain = certs.toArray(new Certificate[numOfCerts]);
                    }
                    this.entries.put(alias, entry);
                    continue;
                }
                if (tag == 2) {
                    ++trustedKeyCount;
                    entry = new TrustedCertEntry();
                    alias = dis.readUTF();
                    ((TrustedCertEntry)entry).date = new Date(dis.readLong());
                    if (xVersion == 2) {
                        String certType = dis.readUTF();
                        if (cfs.containsKey(certType)) {
                            cf = (CertificateFactory)cfs.get(certType);
                        } else {
                            cf = CertificateFactory.getInstance(certType);
                            cfs.put(certType, cf);
                        }
                    }
                    encoded = IOUtils.readExactlyNBytes(dis, dis.readInt());
                    bais = new ByteArrayInputStream(encoded);
                    ((TrustedCertEntry)entry).cert = cf.generateCertificate(bais);
                    bais.close();
                    this.entries.put(alias, entry);
                    continue;
                }
                throw new IOException("Unrecognized keystore entry: " + tag);
            }
            if (debug != null) {
                debug.println("JavaKeyStore load: private key count: " + privateKeyCount + ". trusted key count: " + trustedKeyCount);
            }
            if (password != null && !MessageDigest.isEqual(computed = md.digest(), actual = IOUtils.readExactlyNBytes(dis, computed.length))) {
                UnrecoverableKeyException t = new UnrecoverableKeyException("Password verification failed");
                throw new IOException("Keystore was tampered with, or password was incorrect", t);
            }
        }
    }

    private MessageDigest getPreKeyedHash(char[] password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA");
        byte[] passwdBytes = this.convertToBytes(password);
        md.update(passwdBytes);
        Arrays.fill(passwdBytes, (byte)0);
        md.update("Mighty Aphrodite".getBytes(StandardCharsets.UTF_8));
        return md;
    }

    private byte[] convertToBytes(char[] password) {
        byte[] passwdBytes = new byte[password.length * 2];
        int j = 0;
        for (int i = 0; i < password.length; ++i) {
            passwdBytes[j++] = (byte)(password[i] >> 8);
            passwdBytes[j++] = (byte)password[i];
        }
        return passwdBytes;
    }

    private static class KeyEntry {
        Date date;
        byte[] protectedPrivKey;
        Certificate[] chain;

        private KeyEntry() {
        }
    }

    private static class TrustedCertEntry {
        Date date;
        Certificate cert;

        private TrustedCertEntry() {
        }
    }

    public static final class DualFormatJKS
    extends KeyStoreDelegator {
        public DualFormatJKS() {
            super("JKS", JKS.class, "PKCS12", PKCS12KeyStore.class);
        }

        @Override
        public boolean engineProbe(InputStream stream) throws IOException {
            DataInputStream dataStream = stream instanceof DataInputStream ? (DataInputStream)stream : new DataInputStream(stream);
            return -17957139 == dataStream.readInt();
        }
    }

    public static final class CaseExactJKS
    extends JavaKeyStore {
        @Override
        String convertAlias(String alias) {
            return alias;
        }
    }

    public static final class JKS
    extends JavaKeyStore {
        @Override
        String convertAlias(String alias) {
            return alias.toLowerCase(Locale.ENGLISH);
        }
    }
}

