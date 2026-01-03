/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.io.Serializable;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.KnownOIDs;
import sun.security.util.ObjectIdentifier;

public class AlgorithmId
implements Serializable,
DerEncoder {
    private static final long serialVersionUID = 7205873507486557157L;
    private ObjectIdentifier algid;
    private AlgorithmParameters algParams;
    protected transient byte[] encodedParams;
    private static volatile Map<String, String> aliasOidsTable;
    public static final ObjectIdentifier MD2_oid;
    public static final ObjectIdentifier MD5_oid;
    public static final ObjectIdentifier SHA_oid;
    public static final ObjectIdentifier SHA224_oid;
    public static final ObjectIdentifier SHA256_oid;
    public static final ObjectIdentifier SHA384_oid;
    public static final ObjectIdentifier SHA512_oid;
    public static final ObjectIdentifier SHA512_224_oid;
    public static final ObjectIdentifier SHA512_256_oid;
    public static final ObjectIdentifier SHA3_224_oid;
    public static final ObjectIdentifier SHA3_256_oid;
    public static final ObjectIdentifier SHA3_384_oid;
    public static final ObjectIdentifier SHA3_512_oid;
    public static final ObjectIdentifier DSA_oid;
    public static final ObjectIdentifier EC_oid;
    public static final ObjectIdentifier RSAEncryption_oid;
    public static final ObjectIdentifier RSASSA_PSS_oid;
    public static final ObjectIdentifier MGF1_oid;
    public static final ObjectIdentifier SHA1withRSA_oid;
    public static final ObjectIdentifier SHA224withRSA_oid;
    public static final ObjectIdentifier SHA256withRSA_oid;
    public static final ObjectIdentifier SHA384withRSA_oid;
    public static final ObjectIdentifier SHA512withRSA_oid;
    public static final ObjectIdentifier SHA512$224withRSA_oid;
    public static final ObjectIdentifier SHA512$256withRSA_oid;
    public static final ObjectIdentifier MD2withRSA_oid;
    public static final ObjectIdentifier MD5withRSA_oid;
    public static final ObjectIdentifier SHA3_224withRSA_oid;
    public static final ObjectIdentifier SHA3_256withRSA_oid;
    public static final ObjectIdentifier SHA3_384withRSA_oid;
    public static final ObjectIdentifier SHA3_512withRSA_oid;

    @Deprecated
    public AlgorithmId() {
    }

    public AlgorithmId(ObjectIdentifier oid) {
        this.algid = oid;
    }

    public AlgorithmId(ObjectIdentifier oid, AlgorithmParameters algparams) {
        this.algid = oid;
        this.algParams = algparams;
        if (this.algParams != null) {
            try {
                this.encodedParams = this.algParams.getEncoded();
            }
            catch (IOException ioe) {
                throw new IllegalStateException("AlgorithmParameters not initialized or cannot be decoded", ioe);
            }
        }
    }

    public AlgorithmId(ObjectIdentifier oid, DerValue params) throws IOException {
        this.algid = oid;
        if (params != null) {
            this.encodedParams = params.toByteArray();
            this.decodeParams();
        }
    }

    protected void decodeParams() throws IOException {
        String algidName = this.getName();
        try {
            this.algParams = AlgorithmParameters.getInstance(algidName);
        }
        catch (NoSuchAlgorithmException e) {
            this.algParams = null;
            return;
        }
        this.algParams.init((byte[])this.encodedParams.clone());
    }

    @Override
    public void encode(DerOutputStream out) {
        DerOutputStream bytes = new DerOutputStream();
        bytes.putOID(this.algid);
        if (this.encodedParams == null) {
            if (this.algid.equals(RSAEncryption_oid) || this.algid.equals(MD2_oid) || this.algid.equals(MD5_oid) || this.algid.equals(SHA_oid) || this.algid.equals(SHA224_oid) || this.algid.equals(SHA256_oid) || this.algid.equals(SHA384_oid) || this.algid.equals(SHA512_oid) || this.algid.equals(SHA512_224_oid) || this.algid.equals(SHA512_256_oid) || this.algid.equals(SHA3_224_oid) || this.algid.equals(SHA3_256_oid) || this.algid.equals(SHA3_384_oid) || this.algid.equals(SHA3_512_oid) || this.algid.equals(SHA1withRSA_oid) || this.algid.equals(SHA224withRSA_oid) || this.algid.equals(SHA256withRSA_oid) || this.algid.equals(SHA384withRSA_oid) || this.algid.equals(SHA512withRSA_oid) || this.algid.equals(SHA512$224withRSA_oid) || this.algid.equals(SHA512$256withRSA_oid) || this.algid.equals(MD2withRSA_oid) || this.algid.equals(MD5withRSA_oid) || this.algid.equals(SHA3_224withRSA_oid) || this.algid.equals(SHA3_256withRSA_oid) || this.algid.equals(SHA3_384withRSA_oid) || this.algid.equals(SHA3_512withRSA_oid)) {
                bytes.putNull();
            }
        } else {
            bytes.writeBytes(this.encodedParams);
        }
        out.write((byte)48, bytes);
    }

    public final byte[] encode() {
        DerOutputStream out = new DerOutputStream();
        this.encode(out);
        return out.toByteArray();
    }

    public final ObjectIdentifier getOID() {
        return this.algid;
    }

    public String getName() {
        String oidStr = this.algid.toString();
        KnownOIDs o = KnownOIDs.findMatch(oidStr);
        if (o == KnownOIDs.SpecifiedSHA2withECDSA) {
            if (this.encodedParams != null) {
                try {
                    AlgorithmId digestParams = AlgorithmId.parse(new DerValue(this.encodedParams));
                    String digestAlg = digestParams.getName();
                    return digestAlg.replace("-", "") + "withECDSA";
                }
                catch (IOException digestParams) {}
            }
        } else if (o == KnownOIDs.PBES2 && this.algParams != null) {
            return this.algParams.toString();
        }
        if (o != null) {
            return o.stdName();
        }
        String n = AlgorithmId.aliasOidsTable().get(oidStr);
        return n != null ? n : this.algid.toString();
    }

    public AlgorithmParameters getParameters() {
        return this.algParams;
    }

    public byte[] getEncodedParams() {
        return this.encodedParams == null || this.algid.toString().equals(KnownOIDs.SpecifiedSHA2withECDSA.value()) ? null : (byte[])this.encodedParams.clone();
    }

    public boolean equals(AlgorithmId other) {
        return this.algid.equals(other.algid) && Arrays.equals(this.encodedParams, other.encodedParams);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof AlgorithmId) {
            return this.equals((AlgorithmId)other);
        }
        if (other instanceof ObjectIdentifier) {
            return this.equals((ObjectIdentifier)other);
        }
        return false;
    }

    public final boolean equals(ObjectIdentifier id) {
        return this.algid.equals(id);
    }

    public int hashCode() {
        int hashCode = this.algid.hashCode();
        hashCode = 31 * hashCode + Arrays.hashCode(this.encodedParams);
        return hashCode;
    }

    protected String paramsToString() {
        if (this.encodedParams == null) {
            return "";
        }
        if (this.algParams != null) {
            return ", " + this.algParams.toString();
        }
        return ", params unparsed";
    }

    public String toString() {
        return this.getName() + this.paramsToString();
    }

    public static AlgorithmId parse(DerValue val) throws IOException {
        DerValue params;
        if (val.tag != 48) {
            throw new IOException("algid parse error, not a sequence");
        }
        DerInputStream in = val.toDerInputStream();
        ObjectIdentifier algid = in.getOID();
        if (in.available() == 0) {
            params = null;
        } else {
            params = in.getDerValue();
            if (params.tag == 5) {
                if (params.length() != 0) {
                    throw new IOException("invalid NULL");
                }
                params = null;
            }
            if (in.available() != 0) {
                throw new IOException("Invalid AlgorithmIdentifier: extra data");
            }
        }
        return new AlgorithmId(algid, params);
    }

    @Deprecated
    public static AlgorithmId getAlgorithmId(String algname) throws NoSuchAlgorithmException {
        return AlgorithmId.get(algname);
    }

    public static AlgorithmId get(String algname) throws NoSuchAlgorithmException {
        ObjectIdentifier oid;
        try {
            oid = AlgorithmId.algOID(algname);
        }
        catch (IOException ioe) {
            throw new NoSuchAlgorithmException("Invalid ObjectIdentifier " + algname);
        }
        if (oid == null) {
            throw new NoSuchAlgorithmException("unrecognized algorithm name: " + algname);
        }
        return new AlgorithmId(oid);
    }

    public static AlgorithmId get(AlgorithmParameters algparams) throws NoSuchAlgorithmException {
        ObjectIdentifier oid;
        String algname = algparams.getAlgorithm();
        try {
            oid = AlgorithmId.algOID(algname);
        }
        catch (IOException ioe) {
            throw new NoSuchAlgorithmException("Invalid ObjectIdentifier " + algname);
        }
        if (oid == null) {
            throw new NoSuchAlgorithmException("unrecognized algorithm name: " + algname);
        }
        return new AlgorithmId(oid, algparams);
    }

    private static ObjectIdentifier algOID(String name) throws IOException {
        KnownOIDs k;
        if (name.startsWith("OID.")) {
            name = name.substring("OID.".length());
        }
        if ((k = KnownOIDs.findMatch(name)) != null) {
            return ObjectIdentifier.of(k);
        }
        if (!name.contains(".")) {
            name = name.toUpperCase(Locale.ENGLISH);
            String oidStr = AlgorithmId.aliasOidsTable().get(name);
            if (oidStr != null) {
                return ObjectIdentifier.of(oidStr);
            }
            return null;
        }
        return ObjectIdentifier.of(name);
    }

    public static void clearAliasOidsTable() {
        aliasOidsTable = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static Map<String, String> aliasOidsTable() {
        Map<String, String> tab = aliasOidsTable;
        if (tab != null) return tab;
        Class<AlgorithmId> clazz = AlgorithmId.class;
        synchronized (AlgorithmId.class) {
            tab = aliasOidsTable;
            if (tab != null) return tab;
            aliasOidsTable = tab = AlgorithmId.collectOIDAliases();
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return tab;
        }
    }

    private static boolean isKnownProvider(Provider p) {
        String pn = p.getName();
        String mn = p.getClass().getModule().getName();
        if (pn != null && mn != null) {
            return mn.equals("java.base") && (pn.equals("SUN") || pn.equals("SunRsaSign") || pn.equals("SunJCE") || pn.equals("SunJSSE")) || mn.equals("jdk.crypto.ec") && pn.equals("SunEC") || mn.equals("jdk.crypto.mscapi") && pn.equals("SunMSCAPI") || mn.equals("jdk.crypto.cryptoki") && pn.startsWith("SunPKCS11");
        }
        return false;
    }

    private static ConcurrentHashMap<String, String> collectOIDAliases() {
        ConcurrentHashMap<String, String> t = new ConcurrentHashMap<String, String>();
        block0: for (Provider provider : Security.getProviders()) {
            if (AlgorithmId.isKnownProvider(provider)) continue;
            for (Object key : provider.keySet()) {
                int index;
                String alias = (String)key;
                String upperCaseAlias = alias.toUpperCase(Locale.ENGLISH);
                if (!upperCaseAlias.startsWith("ALG.ALIAS") || (index = upperCaseAlias.indexOf("OID.")) == -1) continue;
                if ((index += "OID.".length()) == alias.length()) continue block0;
                String ostr = alias.substring(index);
                String stdAlgName = provider.getProperty(alias);
                if (stdAlgName == null) continue;
                String upperStdAlgName = stdAlgName.toUpperCase(Locale.ENGLISH);
                if (KnownOIDs.findMatch(upperStdAlgName) == null) {
                    t.putIfAbsent(upperStdAlgName, ostr);
                }
                if (KnownOIDs.findMatch(ostr) != null) continue;
                t.putIfAbsent(ostr, stdAlgName);
            }
        }
        return t;
    }

    static {
        MD2_oid = ObjectIdentifier.of(KnownOIDs.MD2);
        MD5_oid = ObjectIdentifier.of(KnownOIDs.MD5);
        SHA_oid = ObjectIdentifier.of(KnownOIDs.SHA_1);
        SHA224_oid = ObjectIdentifier.of(KnownOIDs.SHA_224);
        SHA256_oid = ObjectIdentifier.of(KnownOIDs.SHA_256);
        SHA384_oid = ObjectIdentifier.of(KnownOIDs.SHA_384);
        SHA512_oid = ObjectIdentifier.of(KnownOIDs.SHA_512);
        SHA512_224_oid = ObjectIdentifier.of(KnownOIDs.SHA_512$224);
        SHA512_256_oid = ObjectIdentifier.of(KnownOIDs.SHA_512$256);
        SHA3_224_oid = ObjectIdentifier.of(KnownOIDs.SHA3_224);
        SHA3_256_oid = ObjectIdentifier.of(KnownOIDs.SHA3_256);
        SHA3_384_oid = ObjectIdentifier.of(KnownOIDs.SHA3_384);
        SHA3_512_oid = ObjectIdentifier.of(KnownOIDs.SHA3_512);
        DSA_oid = ObjectIdentifier.of(KnownOIDs.DSA);
        EC_oid = ObjectIdentifier.of(KnownOIDs.EC);
        RSAEncryption_oid = ObjectIdentifier.of(KnownOIDs.RSA);
        RSASSA_PSS_oid = ObjectIdentifier.of(KnownOIDs.RSASSA_PSS);
        MGF1_oid = ObjectIdentifier.of(KnownOIDs.MGF1);
        SHA1withRSA_oid = ObjectIdentifier.of(KnownOIDs.SHA1withRSA);
        SHA224withRSA_oid = ObjectIdentifier.of(KnownOIDs.SHA224withRSA);
        SHA256withRSA_oid = ObjectIdentifier.of(KnownOIDs.SHA256withRSA);
        SHA384withRSA_oid = ObjectIdentifier.of(KnownOIDs.SHA384withRSA);
        SHA512withRSA_oid = ObjectIdentifier.of(KnownOIDs.SHA512withRSA);
        SHA512$224withRSA_oid = ObjectIdentifier.of(KnownOIDs.SHA512$224withRSA);
        SHA512$256withRSA_oid = ObjectIdentifier.of(KnownOIDs.SHA512$256withRSA);
        MD2withRSA_oid = ObjectIdentifier.of(KnownOIDs.MD2withRSA);
        MD5withRSA_oid = ObjectIdentifier.of(KnownOIDs.MD5withRSA);
        SHA3_224withRSA_oid = ObjectIdentifier.of(KnownOIDs.SHA3_224withRSA);
        SHA3_256withRSA_oid = ObjectIdentifier.of(KnownOIDs.SHA3_256withRSA);
        SHA3_384withRSA_oid = ObjectIdentifier.of(KnownOIDs.SHA3_384withRSA);
        SHA3_512withRSA_oid = ObjectIdentifier.of(KnownOIDs.SHA3_512withRSA);
    }
}

