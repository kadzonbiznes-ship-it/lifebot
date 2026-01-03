/*
 * Decompiled with CFR 0.152.
 */
package sun.security.pkcs12;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PKCS12Attribute;
import java.security.PrivateKey;
import java.security.PrivilegedAction;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.x500.X500Principal;
import jdk.internal.access.SharedSecrets;
import sun.security.action.GetPropertyAction;
import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.EncryptedPrivateKeyInfo;
import sun.security.pkcs12.MacData;
import sun.security.provider.JavaKeyStore;
import sun.security.tools.KeyStoreUtil;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.KeyStoreDelegator;
import sun.security.util.KnownOIDs;
import sun.security.util.ObjectIdentifier;
import sun.security.util.SecurityProperties;
import sun.security.x509.AlgorithmId;
import sun.security.x509.AuthorityKeyIdentifierExtension;

public final class PKCS12KeyStore
extends KeyStoreSpi {
    private static final String DEFAULT_CERT_PBE_ALGORITHM = "PBEWithHmacSHA256AndAES_256";
    private static final String DEFAULT_KEY_PBE_ALGORITHM = "PBEWithHmacSHA256AndAES_256";
    private static final String DEFAULT_MAC_ALGORITHM = "HmacPBESHA256";
    private static final int DEFAULT_CERT_PBE_ITERATION_COUNT = 10000;
    private static final int DEFAULT_KEY_PBE_ITERATION_COUNT = 10000;
    private static final int DEFAULT_MAC_ITERATION_COUNT = 10000;
    private static final String LEGACY_CERT_PBE_ALGORITHM = "PBEWithSHA1AndRC2_40";
    private static final String LEGACY_KEY_PBE_ALGORITHM = "PBEWithSHA1AndDESede";
    private static final String LEGACY_MAC_ALGORITHM = "HmacPBESHA1";
    private static final int LEGACY_PBE_ITERATION_COUNT = 50000;
    private static final int LEGACY_MAC_ITERATION_COUNT = 100000;
    private static final String USE_LEGACY_PROP = "keystore.pkcs12.legacy";
    public static final int VERSION_3 = 3;
    private static final int MAX_ITERATION_COUNT = 5000000;
    private static final int SALT_LEN = 20;
    private static final KnownOIDs[] CORE_ATTRIBUTES = new KnownOIDs[]{KnownOIDs.FriendlyName, KnownOIDs.LocalKeyID, KnownOIDs.ORACLE_TrustedKeyUsage};
    private static final Debug debug = Debug.getInstance("pkcs12");
    private static final ObjectIdentifier PKCS8ShroudedKeyBag_OID = ObjectIdentifier.of(KnownOIDs.PKCS8ShroudedKeyBag);
    private static final ObjectIdentifier CertBag_OID = ObjectIdentifier.of(KnownOIDs.CertBag);
    private static final ObjectIdentifier SecretBag_OID = ObjectIdentifier.of(KnownOIDs.SecretBag);
    private static final ObjectIdentifier PKCS9FriendlyName_OID = ObjectIdentifier.of(KnownOIDs.FriendlyName);
    private static final ObjectIdentifier PKCS9LocalKeyId_OID = ObjectIdentifier.of(KnownOIDs.LocalKeyID);
    private static final ObjectIdentifier PKCS9CertType_OID = ObjectIdentifier.of(KnownOIDs.CertTypeX509);
    private static final ObjectIdentifier pbes2_OID = ObjectIdentifier.of(KnownOIDs.PBES2);
    private static final ObjectIdentifier TrustedKeyUsage_OID = ObjectIdentifier.of(KnownOIDs.ORACLE_TrustedKeyUsage);
    private static final ObjectIdentifier[] AnyUsage = new ObjectIdentifier[]{ObjectIdentifier.of(KnownOIDs.anyExtendedKeyUsage)};
    private int counter = 0;
    private int privateKeyCount = 0;
    private int secretKeyCount = 0;
    private int certificateCount = 0;
    private String certProtectionAlgorithm = null;
    private int certPbeIterationCount = -1;
    private String macAlgorithm = null;
    private int macIterationCount = -1;
    private SecureRandom random;
    private final Map<String, Entry> entries = Collections.synchronizedMap(new LinkedHashMap());
    private final ArrayList<KeyEntry> keyList = new ArrayList();
    private final List<X509Certificate> allCerts = new ArrayList<X509Certificate>();
    private final ArrayList<CertEntry> certEntries = new ArrayList();
    private static final long[][] PKCS12_HEADER_PATTERNS = new long[][]{{3494795514211237894L, 660487941423303937L, 504861105470833664L}, {3495356260826546992L, -9079256822855334328L, -8721487855316000640L}, {3458766717192241158L, 660487941423303937L, 504860555169759232L}, {3495074794423136385L, 1698928106440439L, 937037901038944260L}, {3495356260826546992L, -9151307806378932090L, -644858042040418048L}, {3495637735769768195L, 3495356260894190214L, 5226136052833911200L}, {3495637735769768195L, 3495637735770032426L, -8770611878670825727L}, {3495919210746348033L, 229827617019135497L, 3064216340979843335L}, {3495919210746348033L, 229828716530761734L, 660487941423303937L}};
    private static final long[][] PKCS12_HEADER_MASKS = new long[][]{{-1L, -1L, -256L}, {-281470681743361L, -72056494526300161L, -16L}, {-71776119061282561L, -1L, -1095233437696L}, {-280375465082881L, 0xFFFFFFFFFFFFFFL, -65281L}, {-281470681743361L, -71776119061217281L, -256L}, {-281474959933441L, -281470681743361L, -1L}, {-281474959933441L, -281474959933441L, -1L}, {-281474976645121L, -1099511562241L, -1L}, {-281474976645121L, -1099511627521L, -1L}};

    @Override
    public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        Key key;
        AlgorithmParameters algParams;
        AlgorithmId aid;
        byte[] encryptedKey;
        byte[] encrBytes;
        Entry entry = this.entries.get(alias.toLowerCase(Locale.ENGLISH));
        if (!(entry instanceof KeyEntry)) {
            return null;
        }
        if (entry instanceof PrivateKeyEntry) {
            encrBytes = ((PrivateKeyEntry)entry).protectedPrivKey;
        } else if (entry instanceof SecretKeyEntry) {
            encrBytes = ((SecretKeyEntry)entry).protectedSecretKey;
        } else {
            throw new UnrecoverableKeyException("Error locating key");
        }
        try {
            EncryptedPrivateKeyInfo encrInfo = new EncryptedPrivateKeyInfo(encrBytes);
            encryptedKey = encrInfo.getEncryptedData();
            DerValue val = new DerValue(encrInfo.getAlgorithm().encode());
            DerInputStream in = val.toDerInputStream();
            aid = AlgorithmId.parse(val);
            algParams = aid.getParameters();
        }
        catch (IOException ioe) {
            UnrecoverableKeyException uke = new UnrecoverableKeyException("Private key not stored as PKCS#8 EncryptedPrivateKeyInfo: " + ioe);
            uke.initCause(ioe);
            throw uke;
        }
        try {
            int ic;
            if (algParams != null) {
                PBEParameterSpec pbeSpec;
                try {
                    pbeSpec = algParams.getParameterSpec(PBEParameterSpec.class);
                }
                catch (InvalidParameterSpecException ipse) {
                    throw new IOException("Invalid PBE algorithm parameters");
                }
                ic = pbeSpec.getIterationCount();
                if (ic > 5000000) {
                    throw new IOException("key PBE iteration count too large");
                }
            } else {
                ic = 0;
            }
            key = RetryWithZero.run(pass -> {
                SecretKeySpec secretKeySpec;
                Cipher cipher = Cipher.getInstance(aid.getName());
                SecretKey skey = this.getPBEKey(pass);
                try {
                    cipher.init(2, (Key)skey, algParams);
                }
                finally {
                    this.destroyPBEKey(skey);
                }
                byte[] keyInfo = cipher.doFinal(encryptedKey);
                DerValue val = new DerValue(keyInfo);
                DerInputStream in = val.toDerInputStream();
                int i = in.getInteger();
                DerValue[] value = in.getSequence(2);
                if (value.length < 1 || value.length > 2) {
                    throw new IOException("Invalid length for AlgorithmIdentifier");
                }
                AlgorithmId algId = new AlgorithmId(value[0].getOID());
                String keyAlgo = algId.getName();
                if (entry instanceof PrivateKeyEntry) {
                    KeyFactory kfac = KeyFactory.getInstance(keyAlgo);
                    PKCS8EncodedKeySpec kspec = new PKCS8EncodedKeySpec(keyInfo);
                    try {
                        PrivateKey tmp = kfac.generatePrivate(kspec);
                        if (debug != null) {
                            debug.println("Retrieved a protected private key at alias '" + alias + "' (" + aid.getName() + " iterations: " + ic + ")");
                        }
                        PrivateKey privateKey = tmp;
                        return privateKey;
                    }
                    finally {
                        SharedSecrets.getJavaSecuritySpecAccess().clearEncodedKeySpec(kspec);
                    }
                }
                byte[] keyBytes = in.getOctetString();
                if (keyAlgo.equals(KnownOIDs.OIW_DES_CBC.stdName())) {
                    keyAlgo = "DES";
                } else if (keyAlgo.equals(KnownOIDs.RC2$CBC$PKCS5Padding.stdName())) {
                    keyAlgo = "RC2";
                }
                SecretKeySpec secretKeySpec2 = new SecretKeySpec(keyBytes, keyAlgo);
                try {
                    SecretKey tmp;
                    if (keyAlgo.startsWith("PBE")) {
                        SecretKeyFactory sKeyFactory = SecretKeyFactory.getInstance(keyAlgo);
                        KeySpec pbeKeySpec = sKeyFactory.getKeySpec(secretKeySpec2, PBEKeySpec.class);
                        try {
                            tmp = sKeyFactory.generateSecret(pbeKeySpec);
                        }
                        finally {
                            ((PBEKeySpec)pbeKeySpec).clearPassword();
                            SharedSecrets.getJavaxCryptoSpecAccess().clearSecretKeySpec(secretKeySpec2);
                        }
                    } else {
                        tmp = secretKeySpec2;
                    }
                    if (debug != null) {
                        debug.println("Retrieved a protected secret key at alias '" + alias + "' (" + aid.getName() + " iterations: " + ic + ")");
                    }
                    secretKeySpec = tmp;
                }
                catch (Throwable throwable) {
                    Arrays.fill(keyBytes, (byte)0);
                    throw throwable;
                }
                Arrays.fill(keyBytes, (byte)0);
                return secretKeySpec;
                finally {
                    val.clear();
                    Arrays.fill(keyInfo, (byte)0);
                }
            }, password);
        }
        catch (Exception e) {
            UnrecoverableKeyException uke = new UnrecoverableKeyException("Get Key failed: " + e.getMessage());
            uke.initCause(e);
            throw uke;
        }
        return key;
    }

    @Override
    public Certificate[] engineGetCertificateChain(String alias) {
        Entry entry = this.entries.get(alias.toLowerCase(Locale.ENGLISH));
        if (entry instanceof PrivateKeyEntry) {
            PrivateKeyEntry privateKeyEntry = (PrivateKeyEntry)entry;
            if (privateKeyEntry.chain == null) {
                return null;
            }
            if (debug != null) {
                debug.println("Retrieved a " + privateKeyEntry.chain.length + "-certificate chain at alias '" + alias + "'");
            }
            return (Certificate[])privateKeyEntry.chain.clone();
        }
        return null;
    }

    @Override
    public Certificate engineGetCertificate(String alias) {
        Entry entry = this.entries.get(alias.toLowerCase(Locale.ENGLISH));
        if (entry == null) {
            return null;
        }
        if (entry instanceof CertEntry && ((CertEntry)entry).trustedKeyUsage != null) {
            if (debug != null) {
                if (Arrays.equals(AnyUsage, ((CertEntry)entry).trustedKeyUsage)) {
                    debug.println("Retrieved a certificate at alias '" + alias + "' (trusted for any purpose)");
                } else {
                    debug.println("Retrieved a certificate at alias '" + alias + "' (trusted for limited purposes)");
                }
            }
            return ((CertEntry)entry).cert;
        }
        if (entry instanceof PrivateKeyEntry) {
            if (((PrivateKeyEntry)entry).chain == null) {
                return null;
            }
            if (debug != null) {
                debug.println("Retrieved a certificate at alias '" + alias + "'");
            }
            return ((PrivateKeyEntry)entry).chain[0];
        }
        return null;
    }

    @Override
    public Date engineGetCreationDate(String alias) {
        Entry entry = this.entries.get(alias.toLowerCase(Locale.ENGLISH));
        if (entry != null) {
            return new Date(entry.date.getTime());
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
        KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(password);
        try {
            this.setKeyEntry(alias, key, passwordProtection, chain, null);
        }
        finally {
            try {
                passwordProtection.destroy();
            }
            catch (DestroyFailedException destroyFailedException) {}
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setKeyEntry(String alias, Key key, KeyStore.PasswordProtection passwordProtection, Certificate[] chain, Set<KeyStore.Entry.Attribute> attributes) throws KeyStoreException {
        try {
            KeyEntry entry;
            if (key instanceof PrivateKey) {
                PKCS12KeyStore.checkX509Certs(chain);
                PrivateKeyEntry keyEntry = new PrivateKeyEntry();
                keyEntry.date = new Date();
                if (key.getFormat().equals("PKCS#8") || key.getFormat().equals("PKCS8")) {
                    if (debug != null) {
                        debug.println("Setting a protected private key at alias '" + alias + "'");
                    }
                    byte[] encoded = key.getEncoded();
                    try {
                        keyEntry.protectedPrivKey = this.encryptPrivateKey(encoded, passwordProtection);
                    }
                    finally {
                        if (encoded != null) {
                            Arrays.fill(encoded, (byte)0);
                        }
                    }
                } else {
                    throw new KeyStoreException("Private key is not encoded as PKCS#8");
                }
                if (chain != null) {
                    if (chain.length > 1 && !this.validateChain(chain)) {
                        throw new KeyStoreException("Certificate chain is not valid");
                    }
                    keyEntry.chain = (Certificate[])chain.clone();
                    this.certificateCount += chain.length;
                    if (debug != null) {
                        debug.println("Setting a " + chain.length + "-certificate chain at alias '" + alias + "'");
                    }
                }
                ++this.privateKeyCount;
                entry = keyEntry;
            } else if (key instanceof SecretKey) {
                SecretKeyEntry keyEntry = new SecretKeyEntry();
                keyEntry.date = new Date();
                DerOutputStream secretKeyInfo = new DerOutputStream();
                secretKeyInfo.putInteger(0);
                AlgorithmId algId = AlgorithmId.get(key.getAlgorithm());
                algId.encode(secretKeyInfo);
                byte[] encoded = key.getEncoded();
                secretKeyInfo.putOctetString(encoded);
                Arrays.fill(encoded, (byte)0);
                DerValue pkcs8 = DerValue.wrap((byte)48, secretKeyInfo);
                byte[] p8Array = pkcs8.toByteArray();
                pkcs8.clear();
                try {
                    keyEntry.protectedSecretKey = this.encryptPrivateKey(p8Array, passwordProtection);
                }
                finally {
                    Arrays.fill(p8Array, (byte)0);
                }
                if (debug != null) {
                    debug.println("Setting a protected secret key at alias '" + alias + "'");
                }
                ++this.secretKeyCount;
                entry = keyEntry;
            } else {
                throw new KeyStoreException("Unsupported Key type");
            }
            entry.attributes = new HashSet<KeyStore.Entry.Attribute>();
            if (attributes != null) {
                entry.attributes.addAll(attributes);
            }
            entry.keyId = ("Time " + entry.date.getTime()).getBytes(StandardCharsets.UTF_8);
            entry.alias = alias.toLowerCase(Locale.ENGLISH);
            this.entries.put(alias.toLowerCase(Locale.ENGLISH), entry);
        }
        catch (KeyStoreException kse) {
            throw kse;
        }
        catch (Exception nsae) {
            throw new KeyStoreException("Key protection algorithm not found: " + nsae, nsae);
        }
    }

    @Override
    public synchronized void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
        PKCS12KeyStore.checkX509Certs(chain);
        try {
            new EncryptedPrivateKeyInfo(key);
        }
        catch (IOException ioe) {
            throw new KeyStoreException("Private key is not stored as PKCS#8 EncryptedPrivateKeyInfo: " + ioe, ioe);
        }
        PrivateKeyEntry entry = new PrivateKeyEntry();
        entry.date = new Date();
        if (debug != null) {
            debug.println("Setting a protected private key at alias '" + alias + "'");
        }
        entry.keyId = ("Time " + entry.date.getTime()).getBytes(StandardCharsets.UTF_8);
        entry.alias = alias.toLowerCase(Locale.ENGLISH);
        entry.protectedPrivKey = (byte[])key.clone();
        if (chain != null) {
            if (chain.length > 1 && !this.validateChain(chain)) {
                throw new KeyStoreException("Certificate chain is not valid");
            }
            entry.chain = (Certificate[])chain.clone();
            this.certificateCount += chain.length;
            if (debug != null) {
                debug.println("Setting a " + entry.chain.length + "-certificate chain at alias '" + alias + "'");
            }
        }
        ++this.privateKeyCount;
        this.entries.put(alias.toLowerCase(Locale.ENGLISH), entry);
    }

    private byte[] getSalt() {
        byte[] salt = new byte[20];
        if (this.random == null) {
            this.random = new SecureRandom();
        }
        this.random.nextBytes(salt);
        return salt;
    }

    private AlgorithmParameters getPBEAlgorithmParameters(String algorithm, int iterationCount) throws IOException {
        AlgorithmParameters algParams;
        byte[] salt = this.getSalt();
        if (KnownOIDs.findMatch(algorithm) == KnownOIDs.PBEWithMD5AndDES) {
            salt = Arrays.copyOf(salt, 8);
        }
        PBEParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);
        try {
            algParams = AlgorithmParameters.getInstance(algorithm);
            algParams.init(paramSpec);
        }
        catch (Exception e) {
            throw new IOException("getPBEAlgorithmParameters failed: " + e.getMessage(), e);
        }
        return algParams;
    }

    private SecretKey getPBEKey(char[] password) throws IOException {
        SecretKey skey;
        PBEKeySpec keySpec = new PBEKeySpec(password);
        try {
            SecretKeyFactory skFac = SecretKeyFactory.getInstance("PBE");
            skey = skFac.generateSecret(keySpec);
        }
        catch (Exception e) {
            throw new IOException("getSecretKey failed: " + e.getMessage(), e);
        }
        finally {
            keySpec.clearPassword();
        }
        return skey;
    }

    private void destroyPBEKey(SecretKey key) {
        try {
            key.destroy();
        }
        catch (DestroyFailedException destroyFailedException) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private byte[] encryptPrivateKey(byte[] data, KeyStore.PasswordProtection passwordProtection) throws UnrecoverableKeyException {
        byte[] key;
        try {
            AlgorithmParameters algParams;
            String algorithm = passwordProtection.getProtectionAlgorithm();
            if (algorithm != null) {
                AlgorithmParameterSpec algParamSpec = passwordProtection.getProtectionParameters();
                if (algParamSpec != null) {
                    algParams = AlgorithmParameters.getInstance(algorithm);
                    algParams.init(algParamSpec);
                } else {
                    algParams = this.getPBEAlgorithmParameters(algorithm, PKCS12KeyStore.defaultKeyPbeIterationCount());
                }
            } else {
                algorithm = PKCS12KeyStore.defaultKeyProtectionAlgorithm();
                algParams = this.getPBEAlgorithmParameters(algorithm, PKCS12KeyStore.defaultKeyPbeIterationCount());
            }
            ObjectIdentifier pbeOID = PKCS12KeyStore.mapPBEAlgorithmToOID(algorithm);
            if (pbeOID == null) {
                throw new IOException("PBE algorithm '" + algorithm + " 'is not supported for key entry protection");
            }
            Cipher cipher = Cipher.getInstance(algorithm);
            SecretKey skey = this.getPBEKey(passwordProtection.getPassword());
            try {
                cipher.init(1, (Key)skey, algParams);
            }
            finally {
                this.destroyPBEKey(skey);
            }
            byte[] encryptedKey = cipher.doFinal(data);
            AlgorithmId algid = new AlgorithmId(pbeOID, cipher.getParameters());
            if (debug != null) {
                debug.println("  (Cipher algorithm: " + cipher.getAlgorithm() + ")");
            }
            EncryptedPrivateKeyInfo encrInfo = new EncryptedPrivateKeyInfo(algid, encryptedKey);
            key = encrInfo.getEncoded();
        }
        catch (Exception e) {
            UnrecoverableKeyException uke = new UnrecoverableKeyException("Encrypt Private Key failed: " + e.getMessage());
            uke.initCause(e);
            throw uke;
        }
        return key;
    }

    private static ObjectIdentifier mapPBEAlgorithmToOID(String algorithm) throws NoSuchAlgorithmException {
        if (algorithm.toLowerCase(Locale.ENGLISH).startsWith("pbewithhmacsha")) {
            return pbes2_OID;
        }
        return AlgorithmId.get(algorithm).getOID();
    }

    @Override
    public synchronized void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
        this.setCertEntry(alias, cert, null);
    }

    private void setCertEntry(String alias, Certificate cert, Set<KeyStore.Entry.Attribute> attributes) throws KeyStoreException {
        if (cert != null && !(cert instanceof X509Certificate)) {
            throw new KeyStoreException("Only X.509 certificates are supported - rejecting class: " + cert.getClass().getName());
        }
        Entry entry = this.entries.get(alias.toLowerCase(Locale.ENGLISH));
        if (entry instanceof KeyEntry) {
            throw new KeyStoreException("Cannot overwrite own certificate");
        }
        CertEntry certEntry = new CertEntry((X509Certificate)cert, null, alias, AnyUsage, attributes);
        ++this.certificateCount;
        this.entries.put(alias.toLowerCase(Locale.ENGLISH), certEntry);
        if (debug != null) {
            debug.println("Setting a trusted certificate at alias '" + alias + "'");
        }
    }

    @Override
    public synchronized void engineDeleteEntry(String alias) throws KeyStoreException {
        Entry entry;
        if (debug != null) {
            debug.println("Removing entry at alias '" + alias + "'");
        }
        if ((entry = this.entries.get(alias.toLowerCase(Locale.ENGLISH))) instanceof PrivateKeyEntry) {
            PrivateKeyEntry keyEntry = (PrivateKeyEntry)entry;
            if (keyEntry.chain != null) {
                this.certificateCount -= keyEntry.chain.length;
            }
            --this.privateKeyCount;
        } else if (entry instanceof CertEntry) {
            --this.certificateCount;
        } else if (entry instanceof SecretKeyEntry) {
            --this.secretKeyCount;
        }
        this.entries.remove(alias.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public Enumeration<String> engineAliases() {
        return Collections.enumeration(this.entries.keySet());
    }

    @Override
    public boolean engineContainsAlias(String alias) {
        return this.entries.containsKey(alias.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public int engineSize() {
        return this.entries.size();
    }

    @Override
    public boolean engineIsKeyEntry(String alias) {
        Entry entry = this.entries.get(alias.toLowerCase(Locale.ENGLISH));
        return entry instanceof KeyEntry;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean engineIsCertificateEntry(String alias) {
        Entry entry = this.entries.get(alias.toLowerCase(Locale.ENGLISH));
        if (!(entry instanceof CertEntry)) return false;
        CertEntry certEntry = (CertEntry)entry;
        if (certEntry.trustedKeyUsage == null) return false;
        return true;
    }

    @Override
    public boolean engineEntryInstanceOf(String alias, Class<? extends KeyStore.Entry> entryClass) {
        if (entryClass == KeyStore.TrustedCertificateEntry.class) {
            return this.engineIsCertificateEntry(alias);
        }
        Entry entry = this.entries.get(alias.toLowerCase(Locale.ENGLISH));
        if (entryClass == KeyStore.PrivateKeyEntry.class) {
            return entry instanceof PrivateKeyEntry;
        }
        if (entryClass == KeyStore.SecretKeyEntry.class) {
            return entry instanceof SecretKeyEntry;
        }
        return false;
    }

    @Override
    public String engineGetCertificateAlias(Certificate cert) {
        Certificate certElem = null;
        Enumeration<String> e = this.engineAliases();
        while (e.hasMoreElements()) {
            String alias = e.nextElement();
            Entry entry = this.entries.get(alias);
            if (entry instanceof PrivateKeyEntry) {
                if (((PrivateKeyEntry)entry).chain != null) {
                    certElem = ((PrivateKeyEntry)entry).chain[0];
                }
            } else {
                if (!(entry instanceof CertEntry) || ((CertEntry)entry).trustedKeyUsage == null) continue;
                certElem = ((CertEntry)entry).cert;
            }
            if (certElem == null || !certElem.equals(cert)) continue;
            return alias;
        }
        return null;
    }

    @Override
    public synchronized void engineStore(OutputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        ContentInfo dataContentInfo;
        DerOutputStream pfx = new DerOutputStream();
        DerOutputStream version = new DerOutputStream();
        version.putInteger(3);
        byte[] pfxVersion = version.toByteArray();
        pfx.writeBytes(pfxVersion);
        DerOutputStream authSafe = new DerOutputStream();
        DerOutputStream authSafeContentInfo = new DerOutputStream();
        if (this.privateKeyCount > 0 || this.secretKeyCount > 0) {
            if (debug != null) {
                debug.println("Storing " + (this.privateKeyCount + this.secretKeyCount) + " protected key(s) in a PKCS#7 data");
            }
            byte[] safeContentData = this.createSafeContent();
            dataContentInfo = new ContentInfo(safeContentData);
            dataContentInfo.encode(authSafeContentInfo);
        }
        if (this.certificateCount > 0) {
            if (this.certProtectionAlgorithm == null) {
                this.certProtectionAlgorithm = PKCS12KeyStore.defaultCertProtectionAlgorithm();
            }
            if (this.certPbeIterationCount < 0) {
                this.certPbeIterationCount = PKCS12KeyStore.defaultCertPbeIterationCount();
            }
            if (debug != null) {
                debug.println("Storing " + this.certificateCount + " certificate(s) in a PKCS#7 encryptedData");
            }
            byte[] certsData = this.getCertificateData();
            if (password != null && !this.certProtectionAlgorithm.equalsIgnoreCase("NONE")) {
                DerOutputStream encrData = new DerOutputStream();
                encrData.putInteger(0);
                encrData.writeBytes(this.encryptContent(certsData, password));
                DerOutputStream encrDataContent = new DerOutputStream();
                encrDataContent.write((byte)48, encrData);
                ContentInfo encrContentInfo = new ContentInfo(ContentInfo.ENCRYPTED_DATA_OID, new DerValue(encrDataContent.toByteArray()));
                encrContentInfo.encode(authSafeContentInfo);
            } else {
                dataContentInfo = new ContentInfo(certsData);
                dataContentInfo.encode(authSafeContentInfo);
            }
        }
        DerOutputStream cInfo = new DerOutputStream();
        cInfo.write((byte)48, authSafeContentInfo);
        byte[] authenticatedSafe = cInfo.toByteArray();
        ContentInfo contentInfo = new ContentInfo(authenticatedSafe);
        contentInfo.encode(authSafe);
        byte[] authSafeData = authSafe.toByteArray();
        pfx.writeBytes(authSafeData);
        if (this.macAlgorithm == null) {
            this.macAlgorithm = PKCS12KeyStore.defaultMacAlgorithm();
        }
        if (this.macIterationCount < 0) {
            this.macIterationCount = PKCS12KeyStore.defaultMacIterationCount();
        }
        if (password != null && !this.macAlgorithm.equalsIgnoreCase("NONE")) {
            byte[] macData = this.calculateMac(password, authenticatedSafe);
            pfx.write(macData);
        }
        DerOutputStream pfxout = new DerOutputStream();
        pfxout.write((byte)48, pfx);
        byte[] pfxData = pfxout.toByteArray();
        stream.write(pfxData);
        stream.flush();
    }

    @Override
    public Set<KeyStore.Entry.Attribute> engineGetAttributes(String alias) {
        if (!this.engineContainsAlias(alias)) {
            return super.engineGetAttributes(alias);
        }
        Entry entry = this.entries.get(alias.toLowerCase(Locale.ENGLISH));
        return Collections.unmodifiableSet(new HashSet<KeyStore.Entry.Attribute>(this.getAttributes(entry)));
    }

    @Override
    public KeyStore.Entry engineGetEntry(String alias, KeyStore.ProtectionParameter protParam) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException {
        if (!this.engineContainsAlias(alias)) {
            return null;
        }
        Entry entry = this.entries.get(alias.toLowerCase(Locale.ENGLISH));
        if (protParam == null) {
            if (this.engineIsCertificateEntry(alias)) {
                if (entry instanceof CertEntry && ((CertEntry)entry).trustedKeyUsage != null) {
                    if (debug != null) {
                        debug.println("Retrieved a trusted certificate at alias '" + alias + "'");
                    }
                    return new KeyStore.TrustedCertificateEntry(((CertEntry)entry).cert, this.getAttributes(entry));
                }
            } else {
                throw new UnrecoverableKeyException("requested entry requires a password");
            }
        }
        if (protParam instanceof KeyStore.PasswordProtection) {
            if (this.engineIsCertificateEntry(alias)) {
                throw new UnsupportedOperationException("trusted certificate entries are not password-protected");
            }
            if (this.engineIsKeyEntry(alias)) {
                KeyStore.PasswordProtection pp = (KeyStore.PasswordProtection)protParam;
                char[] password = pp.getPassword();
                Key key = this.engineGetKey(alias, password);
                if (key instanceof PrivateKey) {
                    Certificate[] chain = this.engineGetCertificateChain(alias);
                    return new KeyStore.PrivateKeyEntry((PrivateKey)key, chain, this.getAttributes(entry));
                }
                if (key instanceof SecretKey) {
                    return new KeyStore.SecretKeyEntry((SecretKey)key, this.getAttributes(entry));
                }
            } else if (!this.engineIsKeyEntry(alias)) {
                throw new UnsupportedOperationException("untrusted certificate entries are not password-protected");
            }
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized void engineSetEntry(String alias, KeyStore.Entry entry, KeyStore.ProtectionParameter protParam) throws KeyStoreException {
        if (protParam != null && !(protParam instanceof KeyStore.PasswordProtection)) {
            throw new KeyStoreException("unsupported protection parameter");
        }
        KeyStore.PasswordProtection pProtect = null;
        if (protParam != null) {
            pProtect = (KeyStore.PasswordProtection)protParam;
        }
        if (entry instanceof KeyStore.TrustedCertificateEntry) {
            if (protParam != null && pProtect.getPassword() != null) {
                throw new KeyStoreException("trusted certificate entries are not password-protected");
            }
            KeyStore.TrustedCertificateEntry tce = (KeyStore.TrustedCertificateEntry)entry;
            this.setCertEntry(alias, tce.getTrustedCertificate(), tce.getAttributes());
            return;
        }
        if (entry instanceof KeyStore.PrivateKeyEntry) {
            if (pProtect == null || pProtect.getPassword() == null) {
                throw new KeyStoreException("non-null password required to create PrivateKeyEntry");
            }
            KeyStore.PrivateKeyEntry pke = (KeyStore.PrivateKeyEntry)entry;
            this.setKeyEntry(alias, pke.getPrivateKey(), pProtect, pke.getCertificateChain(), pke.getAttributes());
            return;
        }
        if (entry instanceof KeyStore.SecretKeyEntry) {
            if (pProtect == null || pProtect.getPassword() == null) {
                throw new KeyStoreException("non-null password required to create SecretKeyEntry");
            }
            KeyStore.SecretKeyEntry ske = (KeyStore.SecretKeyEntry)entry;
            this.setKeyEntry(alias, ske.getSecretKey(), pProtect, null, ske.getAttributes());
            return;
        }
        throw new KeyStoreException("unsupported entry type: " + entry.getClass().getName());
    }

    private Set<KeyStore.Entry.Attribute> getAttributes(Entry entry) {
        Object[] trustedKeyUsageValue;
        if (entry.attributes == null) {
            entry.attributes = new HashSet<KeyStore.Entry.Attribute>();
        }
        entry.attributes.add(new PKCS12Attribute(PKCS9FriendlyName_OID.toString(), entry.alias));
        byte[] keyIdValue = entry.keyId;
        if (keyIdValue != null) {
            entry.attributes.add(new PKCS12Attribute(PKCS9LocalKeyId_OID.toString(), Debug.toString(keyIdValue)));
        }
        if (entry instanceof CertEntry && (trustedKeyUsageValue = ((CertEntry)entry).trustedKeyUsage) != null) {
            if (trustedKeyUsageValue.length == 1) {
                entry.attributes.add(new PKCS12Attribute(TrustedKeyUsage_OID.toString(), ((ObjectIdentifier)trustedKeyUsageValue[0]).toString()));
            } else {
                entry.attributes.add(new PKCS12Attribute(TrustedKeyUsage_OID.toString(), Arrays.toString(trustedKeyUsageValue)));
            }
        }
        return entry.attributes;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private byte[] calculateMac(char[] passwd, byte[] data) throws IOException {
        byte[] mData;
        String algName = this.macAlgorithm.substring(7);
        try {
            byte[] salt = this.getSalt();
            Mac m = Mac.getInstance(this.macAlgorithm);
            PBEParameterSpec params = new PBEParameterSpec(salt, this.macIterationCount);
            SecretKey key = this.getPBEKey(passwd);
            try {
                m.init(key, params);
            }
            finally {
                this.destroyPBEKey(key);
            }
            m.update(data);
            byte[] macResult = m.doFinal();
            MacData macData = new MacData(algName, macResult, salt, this.macIterationCount);
            DerOutputStream bytes = new DerOutputStream();
            bytes.write(macData.getEncoded());
            mData = bytes.toByteArray();
        }
        catch (Exception e) {
            throw new IOException("calculateMac failed: " + e, e);
        }
        return mData;
    }

    private boolean validateChain(Certificate[] certChain) {
        for (int i = 0; i < certChain.length - 1; ++i) {
            X500Principal subjectDN;
            X500Principal issuerDN = ((X509Certificate)certChain[i]).getIssuerX500Principal();
            if (issuerDN.equals(subjectDN = ((X509Certificate)certChain[i + 1]).getSubjectX500Principal())) continue;
            return false;
        }
        HashSet<Certificate> set = new HashSet<Certificate>(Arrays.asList(certChain));
        return set.size() == certChain.length;
    }

    private static void checkX509Certs(Certificate[] certs) throws KeyStoreException {
        if (certs != null) {
            for (Certificate cert : certs) {
                if (cert instanceof X509Certificate) continue;
                throw new KeyStoreException("Only X.509 certificates are supported - rejecting class: " + cert.getClass().getName());
            }
        }
    }

    private byte[] getBagAttributes(String alias, byte[] keyId, Set<KeyStore.Entry.Attribute> attributes) {
        return this.getBagAttributes(alias, keyId, null, attributes);
    }

    private byte[] getBagAttributes(String alias, byte[] keyId, ObjectIdentifier[] trustedUsage, Set<KeyStore.Entry.Attribute> attributes) {
        byte[] localKeyID = null;
        byte[] friendlyName = null;
        byte[] trustedKeyUsage = null;
        if (alias == null && keyId == null) {
            return null;
        }
        DerOutputStream bagAttrs = new DerOutputStream();
        if (alias != null) {
            DerOutputStream bagAttr1 = new DerOutputStream();
            bagAttr1.putOID(PKCS9FriendlyName_OID);
            DerOutputStream bagAttrContent1 = new DerOutputStream();
            DerOutputStream bagAttrValue1 = new DerOutputStream();
            bagAttrContent1.putBMPString(alias);
            bagAttr1.write((byte)49, bagAttrContent1);
            bagAttrValue1.write((byte)48, bagAttr1);
            friendlyName = bagAttrValue1.toByteArray();
        }
        if (keyId != null) {
            DerOutputStream bagAttr2 = new DerOutputStream();
            bagAttr2.putOID(PKCS9LocalKeyId_OID);
            DerOutputStream bagAttrContent2 = new DerOutputStream();
            DerOutputStream bagAttrValue2 = new DerOutputStream();
            bagAttrContent2.putOctetString(keyId);
            bagAttr2.write((byte)49, bagAttrContent2);
            bagAttrValue2.write((byte)48, bagAttr2);
            localKeyID = bagAttrValue2.toByteArray();
        }
        if (trustedUsage != null) {
            DerOutputStream bagAttr3 = new DerOutputStream();
            bagAttr3.putOID(TrustedKeyUsage_OID);
            DerOutputStream bagAttrContent3 = new DerOutputStream();
            DerOutputStream bagAttrValue3 = new DerOutputStream();
            for (ObjectIdentifier usage : trustedUsage) {
                bagAttrContent3.putOID(usage);
            }
            bagAttr3.write((byte)49, bagAttrContent3);
            bagAttrValue3.write((byte)48, bagAttr3);
            trustedKeyUsage = bagAttrValue3.toByteArray();
        }
        DerOutputStream attrs = new DerOutputStream();
        if (friendlyName != null) {
            attrs.writeBytes(friendlyName);
        }
        if (localKeyID != null) {
            attrs.writeBytes(localKeyID);
        }
        if (trustedKeyUsage != null) {
            attrs.writeBytes(trustedKeyUsage);
        }
        if (attributes != null) {
            for (KeyStore.Entry.Attribute attribute : attributes) {
                String attributeName = attribute.getName();
                if (CORE_ATTRIBUTES[0].value().equals(attributeName) || CORE_ATTRIBUTES[1].value().equals(attributeName) || CORE_ATTRIBUTES[2].value().equals(attributeName)) continue;
                attrs.writeBytes(((PKCS12Attribute)attribute).getEncoded());
            }
        }
        bagAttrs.write((byte)49, attrs);
        return bagAttrs.toByteArray();
    }

    private byte[] getCertificateData() throws CertificateException {
        DerOutputStream out = new DerOutputStream();
        Enumeration<String> e = this.engineAliases();
        while (e.hasMoreElements()) {
            Certificate[] certs;
            String alias = e.nextElement();
            Entry entry = this.entries.get(alias);
            if (entry instanceof PrivateKeyEntry) {
                PrivateKeyEntry keyEntry = (PrivateKeyEntry)entry;
                certs = keyEntry.chain != null ? keyEntry.chain : new Certificate[]{};
            } else {
                certs = entry instanceof CertEntry ? new Certificate[]{((CertEntry)entry).cert} : new Certificate[]{};
            }
            for (int i = 0; i < certs.length; ++i) {
                byte[] bagAttrs;
                DerOutputStream safeBag = new DerOutputStream();
                safeBag.putOID(CertBag_OID);
                DerOutputStream certBag = new DerOutputStream();
                certBag.putOID(PKCS9CertType_OID);
                DerOutputStream certValue = new DerOutputStream();
                X509Certificate cert = (X509Certificate)certs[i];
                certValue.putOctetString(cert.getEncoded());
                certBag.write(DerValue.createTag((byte)-128, true, (byte)0), certValue);
                DerOutputStream certout = new DerOutputStream();
                certout.write((byte)48, certBag);
                byte[] certBagValue = certout.toByteArray();
                DerOutputStream bagValue = new DerOutputStream();
                bagValue.writeBytes(certBagValue);
                safeBag.write(DerValue.createTag((byte)-128, true, (byte)0), bagValue);
                if (i == 0) {
                    if (entry instanceof KeyEntry) {
                        KeyEntry keyEntry = (KeyEntry)entry;
                        bagAttrs = this.getBagAttributes(keyEntry.alias, keyEntry.keyId, keyEntry.attributes);
                    } else {
                        CertEntry certEntry = (CertEntry)entry;
                        bagAttrs = this.getBagAttributes(certEntry.alias, certEntry.keyId, certEntry.trustedKeyUsage, certEntry.attributes);
                    }
                } else {
                    bagAttrs = this.getBagAttributes(cert.getSubjectX500Principal().getName(), null, entry.attributes);
                }
                if (bagAttrs != null) {
                    safeBag.writeBytes(bagAttrs);
                }
                out.write((byte)48, safeBag);
            }
        }
        DerOutputStream safeBagValue = new DerOutputStream();
        safeBagValue.write((byte)48, out);
        return safeBagValue.toByteArray();
    }

    private byte[] createSafeContent() throws IOException {
        DerOutputStream out = new DerOutputStream();
        Enumeration<String> e = this.engineAliases();
        while (e.hasMoreElements()) {
            String alias = e.nextElement();
            Entry entry = this.entries.get(alias);
            if (!(entry instanceof KeyEntry)) continue;
            KeyEntry keyEntry = (KeyEntry)entry;
            DerOutputStream safeBag = new DerOutputStream();
            if (keyEntry instanceof PrivateKeyEntry) {
                EncryptedPrivateKeyInfo encrInfo;
                safeBag.putOID(PKCS8ShroudedKeyBag_OID);
                byte[] encrBytes = ((PrivateKeyEntry)keyEntry).protectedPrivKey;
                try {
                    encrInfo = new EncryptedPrivateKeyInfo(encrBytes);
                }
                catch (IOException ioe) {
                    throw new IOException("Private key not stored as PKCS#8 EncryptedPrivateKeyInfo" + ioe.getMessage());
                }
                DerOutputStream bagValue = new DerOutputStream();
                bagValue.writeBytes(encrInfo.getEncoded());
                safeBag.write(DerValue.createTag((byte)-128, true, (byte)0), bagValue);
            } else {
                if (!(keyEntry instanceof SecretKeyEntry)) continue;
                safeBag.putOID(SecretBag_OID);
                DerOutputStream secretBag = new DerOutputStream();
                secretBag.putOID(PKCS8ShroudedKeyBag_OID);
                DerOutputStream secretKeyValue = new DerOutputStream();
                secretKeyValue.putOctetString(((SecretKeyEntry)keyEntry).protectedSecretKey);
                secretBag.write(DerValue.createTag((byte)-128, true, (byte)0), secretKeyValue);
                DerOutputStream secretBagSeq = new DerOutputStream();
                secretBagSeq.write((byte)48, secretBag);
                byte[] secretBagValue = secretBagSeq.toByteArray();
                DerOutputStream bagValue = new DerOutputStream();
                bagValue.writeBytes(secretBagValue);
                safeBag.write(DerValue.createTag((byte)-128, true, (byte)0), bagValue);
            }
            byte[] bagAttrs = this.getBagAttributes(alias, entry.keyId, entry.attributes);
            safeBag.writeBytes(bagAttrs);
            out.write((byte)48, safeBag);
        }
        DerOutputStream safeBagValue = new DerOutputStream();
        safeBagValue.write((byte)48, out);
        return safeBagValue.toByteArray();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private byte[] encryptContent(byte[] data, char[] password) throws IOException {
        try {
            AlgorithmParameters algParams = this.getPBEAlgorithmParameters(this.certProtectionAlgorithm, this.certPbeIterationCount);
            DerOutputStream bytes = new DerOutputStream();
            Cipher cipher = Cipher.getInstance(this.certProtectionAlgorithm);
            SecretKey skey = this.getPBEKey(password);
            try {
                cipher.init(1, (Key)skey, algParams);
            }
            finally {
                this.destroyPBEKey(skey);
            }
            byte[] encryptedData = cipher.doFinal(data);
            AlgorithmId algId = new AlgorithmId(PKCS12KeyStore.mapPBEAlgorithmToOID(this.certProtectionAlgorithm), cipher.getParameters());
            algId.encode(bytes);
            byte[] encodedAlgId = bytes.toByteArray();
            if (debug != null) {
                debug.println("  (Cipher algorithm: " + cipher.getAlgorithm() + ")");
            }
            DerOutputStream bytes2 = new DerOutputStream();
            bytes2.putOID(ContentInfo.DATA_OID);
            bytes2.writeBytes(encodedAlgId);
            DerOutputStream tmpout2 = new DerOutputStream();
            tmpout2.putOctetString(encryptedData);
            bytes2.writeImplicit(DerValue.createTag((byte)-128, false, (byte)0), tmpout2);
            DerOutputStream out = new DerOutputStream();
            out.write((byte)48, bytes2);
            return out.toByteArray();
        }
        catch (IOException ioe) {
            throw ioe;
        }
        catch (Exception e) {
            throw new IOException("Failed to encrypt safe contents entry: " + e, e);
        }
    }

    @Override
    public synchronized void engineLoad(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        this.certProtectionAlgorithm = null;
        this.certPbeIterationCount = -1;
        this.macAlgorithm = null;
        this.macIterationCount = -1;
        if (stream == null) {
            return;
        }
        this.counter = 0;
        DerValue val = new DerValue(stream);
        DerInputStream s = val.toDerInputStream();
        int version = s.getInteger();
        if (version != 3) {
            throw new IOException("PKCS12 keystore not in version 3 format");
        }
        this.entries.clear();
        ContentInfo authSafe = new ContentInfo(s);
        ObjectIdentifier contentType = authSafe.getContentType();
        if (!contentType.equals(ContentInfo.DATA_OID)) {
            throw new IOException("public key protected PKCS12 not supported");
        }
        byte[] authSafeData = authSafe.getData();
        DerInputStream as = new DerInputStream(authSafeData);
        DerValue[] safeContentsArray = as.getSequence(2);
        int count = safeContentsArray.length;
        this.privateKeyCount = 0;
        this.secretKeyCount = 0;
        this.certificateCount = 0;
        boolean seeEncBag = false;
        for (int i = 0; i < count; ++i) {
            byte[] eAlgId = null;
            DerInputStream sci = new DerInputStream(safeContentsArray[i].toByteArray());
            ContentInfo safeContents = new ContentInfo(sci);
            contentType = safeContents.getContentType();
            if (contentType.equals(ContentInfo.DATA_OID)) {
                if (debug != null) {
                    debug.println("Loading PKCS#7 data");
                }
                this.loadSafeContents(new DerInputStream(safeContents.getData()));
                continue;
            }
            if (contentType.equals(ContentInfo.ENCRYPTED_DATA_OID)) {
                if (password == null) {
                    if (debug == null) continue;
                    debug.println("Warning: skipping PKCS#7 encryptedData - no password was supplied");
                    continue;
                }
                DerInputStream edi = safeContents.getContent().toDerInputStream();
                int edVersion = edi.getInteger();
                DerValue[] seq = edi.getSequence(3);
                if (seq.length != 3) {
                    throw new IOException("Invalid length for EncryptedContentInfo");
                }
                ObjectIdentifier edContentType = seq[0].getOID();
                eAlgId = seq[1].toByteArray();
                if (!seq[2].isContextSpecific((byte)0)) {
                    throw new IOException("unsupported encrypted content type " + seq[2].tag);
                }
                byte newTag = 4;
                if (seq[2].isConstructed()) {
                    newTag = (byte)(newTag | 0x20);
                }
                seq[2].resetTag(newTag);
                byte[] rawData = seq[2].getOctetString();
                AlgorithmId aid = AlgorithmId.parse(seq[1]);
                AlgorithmParameters algParams = aid.getParameters();
                int ic = 0;
                if (algParams != null) {
                    PBEParameterSpec pbeSpec;
                    try {
                        pbeSpec = algParams.getParameterSpec(PBEParameterSpec.class);
                    }
                    catch (InvalidParameterSpecException ipse) {
                        throw new IOException("Invalid PBE algorithm parameters");
                    }
                    ic = pbeSpec.getIterationCount();
                    if (ic > 5000000) {
                        throw new IOException("cert PBE iteration count too large");
                    }
                    this.certProtectionAlgorithm = aid.getName();
                    this.certPbeIterationCount = ic;
                    seeEncBag = true;
                }
                if (debug != null) {
                    debug.println("Loading PKCS#7 encryptedData (" + this.certProtectionAlgorithm + " iterations: " + ic + ")");
                }
                try {
                    RetryWithZero.run(pass -> {
                        Cipher cipher = Cipher.getInstance(this.certProtectionAlgorithm);
                        SecretKey skey = this.getPBEKey(pass);
                        try {
                            cipher.init(2, (Key)skey, algParams);
                        }
                        finally {
                            this.destroyPBEKey(skey);
                        }
                        this.loadSafeContents(new DerInputStream(cipher.doFinal(rawData)));
                        return null;
                    }, password);
                    continue;
                }
                catch (Exception e) {
                    throw new IOException("keystore password was incorrect", new UnrecoverableKeyException("failed to decrypt safe contents entry: " + e));
                }
            }
            throw new IOException("public key protected PKCS12 not supported");
        }
        if (!seeEncBag && this.certificateCount > 0) {
            this.certProtectionAlgorithm = "NONE";
        }
        if (s.available() > 0) {
            if (password != null) {
                MacData macData = new MacData(s);
                int ic = macData.getIterations();
                try {
                    if (ic > 5000000) {
                        throw new InvalidAlgorithmParameterException("MAC iteration count too large: " + ic);
                    }
                    String algName = macData.getDigestAlgName().toUpperCase(Locale.ENGLISH);
                    algName = algName.replace("-", "");
                    this.macAlgorithm = "HmacPBE" + algName;
                    this.macIterationCount = ic;
                    Mac m = Mac.getInstance(this.macAlgorithm);
                    PBEParameterSpec params = new PBEParameterSpec(macData.getSalt(), ic);
                    RetryWithZero.run(pass -> {
                        SecretKey key = this.getPBEKey(pass);
                        try {
                            m.init(key, params);
                        }
                        finally {
                            this.destroyPBEKey(key);
                        }
                        m.update(authSafeData);
                        byte[] macResult = m.doFinal();
                        if (debug != null) {
                            debug.println("Checking keystore integrity (" + m.getAlgorithm() + " iterations: " + ic + ")");
                        }
                        if (!MessageDigest.isEqual(macData.getDigest(), macResult)) {
                            throw new UnrecoverableKeyException("Failed PKCS12 integrity checking");
                        }
                        return null;
                    }, password);
                }
                catch (Exception e) {
                    throw new IOException("Integrity check failed: " + e, e);
                }
            }
        } else {
            this.macAlgorithm = "NONE";
        }
        PrivateKeyEntry[] list = this.keyList.toArray(new PrivateKeyEntry[0]);
        for (int m = 0; m < list.length; ++m) {
            PrivateKeyEntry entry = list[m];
            if (entry.keyId == null) continue;
            ArrayList<X509Certificate> chain = new ArrayList<X509Certificate>();
            X509Certificate cert = this.findMatchedCertificate(entry);
            block8: while (cert != null) {
                if (!chain.isEmpty()) {
                    for (X509Certificate chainCert : chain) {
                        if (!cert.equals(chainCert)) continue;
                        if (debug == null) break block8;
                        debug.println("Loop detected in certificate chain. Skip adding repeated cert to chain. Subject: " + cert.getSubjectX500Principal().toString());
                        break block8;
                    }
                }
                chain.add(cert);
                if (KeyStoreUtil.isSelfSigned(cert)) break;
                cert = this.findIssuer(cert);
            }
            if (chain.size() <= 0) continue;
            entry.chain = chain.toArray(new Certificate[0]);
        }
        if (debug != null) {
            debug.println("PKCS12KeyStore load: private key count: " + this.privateKeyCount + ". secret key count: " + this.secretKeyCount + ". certificate count: " + this.certificateCount);
        }
        this.certEntries.clear();
        this.allCerts.clear();
        this.keyList.clear();
    }

    private X509Certificate findIssuer(X509Certificate input) {
        X509Certificate fallback = null;
        X500Principal issuerPrinc = input.getIssuerX500Principal();
        byte[] issuerIdExtension = input.getExtensionValue(KnownOIDs.AuthorityKeyID.value());
        byte[] issuerId = null;
        if (issuerIdExtension != null) {
            try {
                issuerId = new AuthorityKeyIdentifierExtension(false, new DerValue(issuerIdExtension).getOctetString()).getEncodedKeyIdentifier();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        for (X509Certificate cert : this.allCerts) {
            if (!cert.getSubjectX500Principal().equals(issuerPrinc)) continue;
            if (issuerId != null) {
                byte[] subjectIdExtension = cert.getExtensionValue(KnownOIDs.SubjectKeyID.value());
                byte[] subjectId = null;
                if (subjectIdExtension != null) {
                    try {
                        subjectId = new DerValue(subjectIdExtension).getOctetString();
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                }
                if (subjectId != null) {
                    if (!Arrays.equals(issuerId, subjectId)) continue;
                    return cert;
                }
                fallback = cert;
                continue;
            }
            return cert;
        }
        return fallback;
    }

    public static boolean isPasswordless(File f) throws IOException {
        try (FileInputStream stream = new FileInputStream(f);){
            DerValue val = new DerValue(stream);
            DerInputStream s = val.toDerInputStream();
            s.getInteger();
            ContentInfo authSafe = new ContentInfo(s);
            DerInputStream as = new DerInputStream(authSafe.getData());
            for (DerValue seq : as.getSequence(2)) {
                DerInputStream sci = new DerInputStream(seq.toByteArray());
                ContentInfo safeContents = new ContentInfo(sci);
                if (!safeContents.getContentType().equals(ContentInfo.ENCRYPTED_DATA_OID)) continue;
                boolean bl = false;
                return bl;
            }
            if (s.available() > 0) {
                boolean bl = false;
                return bl;
            }
        }
        return true;
    }

    private X509Certificate findMatchedCertificate(PrivateKeyEntry entry) {
        CertEntry keyIdMatch = null;
        CertEntry aliasMatch = null;
        for (CertEntry ce : this.certEntries) {
            if (Arrays.equals(entry.keyId, ce.keyId)) {
                keyIdMatch = ce;
                if (!entry.alias.equalsIgnoreCase(ce.alias)) continue;
                return ce.cert;
            }
            if (!entry.alias.equalsIgnoreCase(ce.alias)) continue;
            aliasMatch = ce;
        }
        if (keyIdMatch != null) {
            return keyIdMatch.cert;
        }
        if (aliasMatch != null) {
            return aliasMatch.cert;
        }
        return null;
    }

    private void loadSafeContents(DerInputStream stream) throws IOException, CertificateException {
        DerValue[] safeBags = stream.getSequence(2);
        int count = safeBags.length;
        for (int i = 0; i < count; ++i) {
            DerValue[] attrSet;
            Object bagItem = null;
            DerInputStream sbi = safeBags[i].toDerInputStream();
            ObjectIdentifier bagId = sbi.getOID();
            DerValue bagValue = sbi.getDerValue();
            if (!bagValue.isContextSpecific((byte)0)) {
                throw new IOException("unsupported PKCS12 bag value type " + bagValue.tag);
            }
            bagValue = bagValue.data.getDerValue();
            if (bagId.equals(PKCS8ShroudedKeyBag_OID)) {
                PrivateKeyEntry kEntry = new PrivateKeyEntry();
                kEntry.protectedPrivKey = bagValue.toByteArray();
                bagItem = kEntry;
                ++this.privateKeyCount;
            } else if (bagId.equals(CertBag_OID)) {
                DerInputStream cs = new DerInputStream(bagValue.toByteArray());
                DerValue[] certValues = cs.getSequence(2);
                if (certValues.length != 2) {
                    throw new IOException("Invalid length for CertBag");
                }
                ObjectIdentifier certId = certValues[0].getOID();
                if (!certValues[1].isContextSpecific((byte)0)) {
                    throw new IOException("unsupported PKCS12 cert value type " + certValues[1].tag);
                }
                DerValue certValue = certValues[1].data.getDerValue();
                CertificateFactory cf = CertificateFactory.getInstance("X509");
                X509Certificate cert = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(certValue.getOctetString()));
                bagItem = cert;
                ++this.certificateCount;
            } else if (bagId.equals(SecretBag_OID)) {
                DerInputStream ss = new DerInputStream(bagValue.toByteArray());
                DerValue[] secretValues = ss.getSequence(2);
                if (secretValues.length != 2) {
                    throw new IOException("Invalid length for SecretBag");
                }
                ObjectIdentifier secretId = secretValues[0].getOID();
                if (!secretValues[1].isContextSpecific((byte)0)) {
                    throw new IOException("unsupported PKCS12 secret value type " + secretValues[1].tag);
                }
                DerValue secretValue = secretValues[1].data.getDerValue();
                SecretKeyEntry kEntry = new SecretKeyEntry();
                kEntry.protectedSecretKey = secretValue.getOctetString();
                bagItem = kEntry;
                ++this.secretKeyCount;
            } else if (debug != null) {
                debug.println("Unsupported PKCS12 bag type: " + bagId);
            }
            try {
                attrSet = sbi.getSet(3);
            }
            catch (IOException e) {
                attrSet = null;
            }
            String alias = null;
            byte[] keyId = null;
            ObjectIdentifier[] trustedKeyUsage = null;
            HashSet<PKCS12Attribute> attributes = new HashSet<PKCS12Attribute>();
            if (attrSet != null) {
                for (int j = 0; j < attrSet.length; ++j) {
                    DerValue[] valSet;
                    byte[] encoded = attrSet[j].toByteArray();
                    DerInputStream as = new DerInputStream(encoded);
                    DerValue[] attrSeq = as.getSequence(2);
                    if (attrSeq.length != 2) {
                        throw new IOException("Invalid length for Attribute");
                    }
                    ObjectIdentifier attrId = attrSeq[0].getOID();
                    DerInputStream vs = new DerInputStream(attrSeq[1].toByteArray());
                    try {
                        valSet = vs.getSet(1);
                    }
                    catch (IOException e) {
                        throw new IOException("Attribute " + attrId + " should have a value " + e.getMessage());
                    }
                    if (attrId.equals(PKCS9FriendlyName_OID)) {
                        alias = valSet[0].getBMPString();
                        continue;
                    }
                    if (attrId.equals(PKCS9LocalKeyId_OID)) {
                        keyId = valSet[0].getOctetString();
                        continue;
                    }
                    if (attrId.equals(TrustedKeyUsage_OID)) {
                        trustedKeyUsage = new ObjectIdentifier[valSet.length];
                        for (int k = 0; k < valSet.length; ++k) {
                            trustedKeyUsage[k] = valSet[k].getOID();
                        }
                        continue;
                    }
                    attributes.add(new PKCS12Attribute(encoded));
                }
            }
            if (bagItem instanceof KeyEntry) {
                KeyEntry entry = (KeyEntry)bagItem;
                if (keyId == null) {
                    if (bagItem instanceof PrivateKeyEntry) {
                        if (this.privateKeyCount != 1) continue;
                        keyId = "01".getBytes(StandardCharsets.UTF_8);
                    } else {
                        keyId = "00".getBytes(StandardCharsets.UTF_8);
                    }
                }
                entry.keyId = keyId;
                String keyIdStr = new String(keyId, StandardCharsets.UTF_8);
                Date date = null;
                if (keyIdStr.startsWith("Time ")) {
                    try {
                        date = new Date(Long.parseLong(keyIdStr.substring(5)));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                if (date == null) {
                    date = new Date();
                }
                entry.date = date;
                if (bagItem instanceof PrivateKeyEntry) {
                    this.keyList.add(entry);
                }
                if (entry.attributes == null) {
                    entry.attributes = new HashSet();
                }
                entry.attributes.addAll(attributes);
                if (alias == null) {
                    alias = this.getUnfriendlyName();
                }
                entry.alias = alias;
                this.entries.put(alias.toLowerCase(Locale.ENGLISH), entry);
                continue;
            }
            if (!(bagItem instanceof X509Certificate)) continue;
            X509Certificate cert = (X509Certificate)bagItem;
            if (keyId == null && this.privateKeyCount == 1 && i == 0) {
                keyId = "01".getBytes(StandardCharsets.UTF_8);
            }
            if (trustedKeyUsage != null) {
                if (alias == null) {
                    alias = this.getUnfriendlyName();
                }
                CertEntry certEntry = new CertEntry(cert, keyId, alias, trustedKeyUsage, attributes);
                this.entries.put(alias.toLowerCase(Locale.ENGLISH), certEntry);
            } else {
                this.certEntries.add(new CertEntry(cert, keyId, alias));
            }
            this.allCerts.add(cert);
        }
    }

    private String getUnfriendlyName() {
        ++this.counter;
        return String.valueOf(this.counter);
    }

    @Override
    public boolean engineProbe(InputStream stream) throws IOException {
        DataInputStream dataStream = stream instanceof DataInputStream ? (DataInputStream)stream : new DataInputStream(stream);
        long firstPeek = dataStream.readLong();
        long nextPeek = dataStream.readLong();
        long finalPeek = dataStream.readLong();
        boolean result = false;
        for (int i = 0; i < PKCS12_HEADER_PATTERNS.length; ++i) {
            if (PKCS12_HEADER_PATTERNS[i][0] != (firstPeek & PKCS12_HEADER_MASKS[i][0]) || PKCS12_HEADER_PATTERNS[i][1] != (nextPeek & PKCS12_HEADER_MASKS[i][1]) || PKCS12_HEADER_PATTERNS[i][2] != (finalPeek & PKCS12_HEADER_MASKS[i][2])) continue;
            result = true;
            break;
        }
        return result;
    }

    private static boolean useLegacy() {
        return GetPropertyAction.privilegedGetProperty(USE_LEGACY_PROP) != null;
    }

    private static String defaultCertProtectionAlgorithm() {
        if (PKCS12KeyStore.useLegacy()) {
            return LEGACY_CERT_PBE_ALGORITHM;
        }
        String result = SecurityProperties.privilegedGetOverridable("keystore.pkcs12.certProtectionAlgorithm");
        return result != null && !result.isEmpty() ? result : "PBEWithHmacSHA256AndAES_256";
    }

    private static int defaultCertPbeIterationCount() {
        if (PKCS12KeyStore.useLegacy()) {
            return 50000;
        }
        String result = SecurityProperties.privilegedGetOverridable("keystore.pkcs12.certPbeIterationCount");
        return result != null && !result.isEmpty() ? PKCS12KeyStore.string2IC("certPbeIterationCount", result) : 10000;
    }

    private static String defaultKeyProtectionAlgorithm() {
        if (PKCS12KeyStore.useLegacy()) {
            return LEGACY_KEY_PBE_ALGORITHM;
        }
        String result = AccessController.doPrivileged(new PrivilegedAction<String>(){

            @Override
            public String run() {
                String name1 = "keystore.pkcs12.keyProtectionAlgorithm";
                String name2 = "keystore.PKCS12.keyProtectionAlgorithm";
                String result = System.getProperty(name1);
                if (result != null) {
                    return result;
                }
                result = System.getProperty(name2);
                if (result != null) {
                    return result;
                }
                result = Security.getProperty(name1);
                if (result != null) {
                    return result;
                }
                return Security.getProperty(name2);
            }
        });
        return result != null && !result.isEmpty() ? result : "PBEWithHmacSHA256AndAES_256";
    }

    private static int defaultKeyPbeIterationCount() {
        if (PKCS12KeyStore.useLegacy()) {
            return 50000;
        }
        String result = SecurityProperties.privilegedGetOverridable("keystore.pkcs12.keyPbeIterationCount");
        return result != null && !result.isEmpty() ? PKCS12KeyStore.string2IC("keyPbeIterationCount", result) : 10000;
    }

    private static String defaultMacAlgorithm() {
        if (PKCS12KeyStore.useLegacy()) {
            return LEGACY_MAC_ALGORITHM;
        }
        String result = SecurityProperties.privilegedGetOverridable("keystore.pkcs12.macAlgorithm");
        return result != null && !result.isEmpty() ? result : DEFAULT_MAC_ALGORITHM;
    }

    private static int defaultMacIterationCount() {
        if (PKCS12KeyStore.useLegacy()) {
            return 100000;
        }
        String result = SecurityProperties.privilegedGetOverridable("keystore.pkcs12.macIterationCount");
        return result != null && !result.isEmpty() ? PKCS12KeyStore.string2IC("macIterationCount", result) : 10000;
    }

    private static int string2IC(String type, String value) {
        int number;
        try {
            number = Integer.parseInt(value);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("keystore.pkcs12." + type + " is not a number: " + value);
        }
        if (number <= 0 || number > 5000000) {
            throw new IllegalArgumentException("Invalid keystore.pkcs12." + type + ": " + value);
        }
        return number;
    }

    private static class Entry {
        Date date;
        String alias;
        byte[] keyId;
        Set<KeyStore.Entry.Attribute> attributes;

        private Entry() {
        }
    }

    private static class KeyEntry
    extends Entry {
        private KeyEntry() {
        }
    }

    private static class PrivateKeyEntry
    extends KeyEntry {
        byte[] protectedPrivKey;
        Certificate[] chain;

        private PrivateKeyEntry() {
        }
    }

    private static class SecretKeyEntry
    extends KeyEntry {
        byte[] protectedSecretKey;

        private SecretKeyEntry() {
        }
    }

    @FunctionalInterface
    private static interface RetryWithZero<T> {
        public T tryOnce(char[] var1) throws Exception;

        public static <S> S run(RetryWithZero<S> f, char[] password) throws Exception {
            try {
                return f.tryOnce(password);
            }
            catch (Exception e) {
                if (password.length == 0) {
                    if (debug != null) {
                        debug.println("Retry with a NUL password");
                    }
                    return f.tryOnce(new char[1]);
                }
                throw e;
            }
        }
    }

    private static class CertEntry
    extends Entry {
        final X509Certificate cert;
        ObjectIdentifier[] trustedKeyUsage;

        CertEntry(X509Certificate cert, byte[] keyId, String alias) {
            this(cert, keyId, alias, null, null);
        }

        CertEntry(X509Certificate cert, byte[] keyId, String alias, ObjectIdentifier[] trustedKeyUsage, Set<? extends KeyStore.Entry.Attribute> attributes) {
            this.date = new Date();
            this.cert = cert;
            this.keyId = keyId;
            this.alias = alias;
            this.trustedKeyUsage = trustedKeyUsage;
            this.attributes = new HashSet();
            if (attributes != null) {
                this.attributes.addAll(attributes);
            }
        }
    }

    public static final class DualFormatPKCS12
    extends KeyStoreDelegator {
        public DualFormatPKCS12() {
            super("PKCS12", PKCS12KeyStore.class, "JKS", JavaKeyStore.JKS.class);
        }
    }
}

