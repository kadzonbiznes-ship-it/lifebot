/*
 * Decompiled with CFR 0.152.
 */
package sun.security.pkcs;

import java.io.IOException;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.Timestamp;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PSSParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.PKCS9Attribute;
import sun.security.pkcs.PKCS9Attributes;
import sun.security.pkcs.ParsingException;
import sun.security.provider.SHAKE256;
import sun.security.timestamp.TimestampToken;
import sun.security.util.Debug;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.DisabledAlgorithmConstraints;
import sun.security.util.HexDumpEncoder;
import sun.security.util.JarConstraintsParameters;
import sun.security.util.KnownOIDs;
import sun.security.util.ObjectIdentifier;
import sun.security.util.SignatureUtil;
import sun.security.x509.AlgorithmId;
import sun.security.x509.KeyUsageExtension;
import sun.security.x509.X500Name;

public class SignerInfo
implements DerEncoder {
    private static final DisabledAlgorithmConstraints JAR_DISABLED_CHECK = DisabledAlgorithmConstraints.jarConstraints();
    BigInteger version;
    X500Name issuerName;
    BigInteger certificateSerialNumber;
    AlgorithmId digestAlgorithmId;
    AlgorithmId digestEncryptionAlgorithmId;
    byte[] encryptedDigest;
    Timestamp timestamp;
    private boolean hasTimestamp = true;
    private static final Debug debug = Debug.getInstance("jar");
    PKCS9Attributes authenticatedAttributes;
    PKCS9Attributes unauthenticatedAttributes;
    private final Map<AlgorithmId, AlgorithmInfo> algorithms = new HashMap<AlgorithmId, AlgorithmInfo>();

    public SignerInfo(X500Name issuerName, BigInteger serial, AlgorithmId digestAlgorithmId, AlgorithmId digestEncryptionAlgorithmId, byte[] encryptedDigest) {
        this(issuerName, serial, digestAlgorithmId, null, digestEncryptionAlgorithmId, encryptedDigest, null);
    }

    public SignerInfo(X500Name issuerName, BigInteger serial, AlgorithmId digestAlgorithmId, PKCS9Attributes authenticatedAttributes, AlgorithmId digestEncryptionAlgorithmId, byte[] encryptedDigest, PKCS9Attributes unauthenticatedAttributes) {
        this.version = BigInteger.ONE;
        this.issuerName = issuerName;
        this.certificateSerialNumber = serial;
        this.digestAlgorithmId = digestAlgorithmId;
        this.authenticatedAttributes = authenticatedAttributes;
        this.digestEncryptionAlgorithmId = digestEncryptionAlgorithmId;
        this.encryptedDigest = encryptedDigest;
        this.unauthenticatedAttributes = unauthenticatedAttributes;
    }

    public SignerInfo(DerInputStream derin) throws IOException {
        this(derin, false);
    }

    public SignerInfo(DerInputStream derin, boolean oldStyle) throws IOException {
        this.version = derin.getBigInteger();
        DerValue[] issuerAndSerialNumber = derin.getSequence(2);
        if (issuerAndSerialNumber.length != 2) {
            throw new ParsingException("Invalid length for IssuerAndSerialNumber");
        }
        byte[] issuerBytes = issuerAndSerialNumber[0].toByteArray();
        this.issuerName = new X500Name(new DerValue(48, issuerBytes));
        this.certificateSerialNumber = issuerAndSerialNumber[1].getBigInteger();
        DerValue tmp = derin.getDerValue();
        this.digestAlgorithmId = AlgorithmId.parse(tmp);
        if (oldStyle) {
            derin.getSet(0);
        } else if ((byte)derin.peekByte() == -96) {
            this.authenticatedAttributes = new PKCS9Attributes(derin);
        }
        tmp = derin.getDerValue();
        this.digestEncryptionAlgorithmId = AlgorithmId.parse(tmp);
        this.encryptedDigest = derin.getOctetString();
        if (oldStyle) {
            derin.getSet(0);
        } else if (derin.available() != 0 && (byte)derin.peekByte() == -95) {
            this.unauthenticatedAttributes = new PKCS9Attributes(derin, true);
        }
        if (derin.available() != 0) {
            throw new ParsingException("extra data at the end");
        }
        this.checkCMSAlgorithmProtection();
    }

    private void checkCMSAlgorithmProtection() throws IOException {
        if (this.authenticatedAttributes == null) {
            return;
        }
        PKCS9Attribute ap = this.authenticatedAttributes.getAttribute(PKCS9Attribute.CMS_ALGORITHM_PROTECTION_OID);
        if (ap == null) {
            return;
        }
        DerValue dv = new DerValue((byte[])ap.getValue());
        DerInputStream data = dv.data();
        AlgorithmId d = AlgorithmId.parse(data.getDerValue());
        DerValue ds = data.getDerValue();
        if (data.available() > 0) {
            throw new IOException("Unknown field in CMSAlgorithmProtection");
        }
        if (!ds.isContextSpecific((byte)1)) {
            throw new IOException("No signature algorithm in CMSAlgorithmProtection");
        }
        AlgorithmId s = AlgorithmId.parse(ds.withTag((byte)48));
        if (!s.equals(this.digestEncryptionAlgorithmId) || !d.equals(this.digestAlgorithmId)) {
            throw new IOException("CMSAlgorithmProtection check failed");
        }
    }

    @Override
    public void encode(DerOutputStream out) {
        DerOutputStream seq = new DerOutputStream();
        seq.putInteger(this.version);
        DerOutputStream issuerAndSerialNumber = new DerOutputStream();
        this.issuerName.encode(issuerAndSerialNumber);
        issuerAndSerialNumber.putInteger(this.certificateSerialNumber);
        seq.write((byte)48, issuerAndSerialNumber);
        this.digestAlgorithmId.encode(seq);
        if (this.authenticatedAttributes != null) {
            this.authenticatedAttributes.encode((byte)-96, seq);
        }
        this.digestEncryptionAlgorithmId.encode(seq);
        seq.putOctetString(this.encryptedDigest);
        if (this.unauthenticatedAttributes != null) {
            this.unauthenticatedAttributes.encode((byte)-95, seq);
        }
        out.write((byte)48, seq);
    }

    public X509Certificate getCertificate(PKCS7 block) throws IOException {
        return block.getCertificate(this.certificateSerialNumber, this.issuerName);
    }

    public ArrayList<X509Certificate> getCertificateChain(PKCS7 block) throws IOException {
        boolean match;
        X509Certificate userCert = block.getCertificate(this.certificateSerialNumber, this.issuerName);
        if (userCert == null) {
            return null;
        }
        ArrayList<X509Certificate> certList = new ArrayList<X509Certificate>();
        certList.add(userCert);
        X509Certificate[] pkcsCerts = block.getCertificates();
        if (pkcsCerts == null || userCert.getSubjectX500Principal().equals(userCert.getIssuerX500Principal())) {
            return certList;
        }
        X500Principal issuer = userCert.getIssuerX500Principal();
        int start = 0;
        block0: do {
            match = false;
            for (int i = start; i < pkcsCerts.length; ++i) {
                if (!issuer.equals(pkcsCerts[i].getSubjectX500Principal())) continue;
                certList.add(pkcsCerts[i]);
                if (pkcsCerts[i].getSubjectX500Principal().equals(pkcsCerts[i].getIssuerX500Principal())) {
                    start = pkcsCerts.length;
                } else {
                    issuer = pkcsCerts[i].getIssuerX500Principal();
                    X509Certificate tmpCert = pkcsCerts[start];
                    pkcsCerts[start] = pkcsCerts[i];
                    pkcsCerts[i] = tmpCert;
                    ++start;
                }
                match = true;
                continue block0;
            }
        } while (match);
        return certList;
    }

    SignerInfo verify(PKCS7 block, byte[] data) throws NoSuchAlgorithmException, SignatureException {
        try {
            X509Certificate cert;
            byte[] dataSigned;
            block23: {
                Timestamp timestamp = null;
                try {
                    timestamp = this.getTimestamp();
                }
                catch (Exception e) {
                    if (debug == null) break block23;
                    debug.println("Unexpected exception while getting timestamp: " + e);
                }
            }
            ContentInfo content = block.getContentInfo();
            if (data == null) {
                data = content.getContentBytes();
            }
            String digestAlgName = this.digestAlgorithmId.getName();
            this.algorithms.put(this.digestAlgorithmId, new AlgorithmInfo("SignerInfo digestAlgorithm field", false));
            if (this.authenticatedAttributes == null) {
                dataSigned = data;
            } else {
                byte[] computedMessageDigest;
                ObjectIdentifier contentType = (ObjectIdentifier)this.authenticatedAttributes.getAttributeValue(PKCS9Attribute.CONTENT_TYPE_OID);
                if (contentType == null || !contentType.equals(content.contentType)) {
                    return null;
                }
                byte[] messageDigest = (byte[])this.authenticatedAttributes.getAttributeValue(PKCS9Attribute.MESSAGE_DIGEST_OID);
                if (messageDigest == null) {
                    return null;
                }
                if (digestAlgName.equals("SHAKE256") || digestAlgName.equals("SHAKE256-LEN")) {
                    if (digestAlgName.equals("SHAKE256-LEN")) {
                        byte[] params = this.digestAlgorithmId.getEncodedParams();
                        if (params == null) {
                            throw new SignatureException("id-shake256-len oid missing length");
                        }
                        int v = new DerValue(params).getInteger();
                        if (v != 512) {
                            throw new SignatureException("Unsupported id-shake256-" + v);
                        }
                    }
                    md = new SHAKE256(64);
                    ((SHAKE256)md).update(data, 0, data.length);
                    computedMessageDigest = ((SHAKE256)md).digest();
                } else {
                    md = MessageDigest.getInstance(digestAlgName);
                    computedMessageDigest = ((MessageDigest)md).digest(data);
                }
                if (!MessageDigest.isEqual(messageDigest, computedMessageDigest)) {
                    return null;
                }
                dataSigned = this.authenticatedAttributes.getDerEncoding();
            }
            String sigAlgName = SignerInfo.makeSigAlg(this.digestAlgorithmId, this.digestEncryptionAlgorithmId, this.authenticatedAttributes == null);
            KnownOIDs oid = KnownOIDs.findMatch(sigAlgName);
            if (oid != null) {
                AlgorithmId sigAlgId = new AlgorithmId(ObjectIdentifier.of(oid), this.digestEncryptionAlgorithmId.getParameters());
                this.algorithms.put(sigAlgId, new AlgorithmInfo("SignerInfo digestEncryptionAlgorithm field", true));
            }
            if ((cert = this.getCertificate(block)) == null) {
                return null;
            }
            PublicKey key = cert.getPublicKey();
            if (cert.hasUnsupportedCriticalExtension()) {
                throw new SignatureException("Certificate has unsupported critical extension(s)");
            }
            boolean[] keyUsageBits = cert.getKeyUsage();
            if (keyUsageBits != null) {
                KeyUsageExtension keyUsage = new KeyUsageExtension(keyUsageBits);
                boolean digSigAllowed = keyUsage.get("digital_signature");
                boolean nonRepuAllowed = keyUsage.get("non_repudiation");
                if (!digSigAllowed && !nonRepuAllowed) {
                    throw new SignatureException("Key usage restricted: cannot be used for digital signatures");
                }
            }
            Signature sig = Signature.getInstance(sigAlgName);
            AlgorithmParameters ap = this.digestEncryptionAlgorithmId.getParameters();
            try {
                SignatureUtil.initVerifyWithParam(sig, key, SignatureUtil.getParamSpec(sigAlgName, ap));
            }
            catch (InvalidAlgorithmParameterException | InvalidKeyException | ProviderException e) {
                throw new SignatureException(e.getMessage(), e);
            }
            sig.update(dataSigned);
            if (sig.verify(this.encryptedDigest)) {
                return this;
            }
        }
        catch (IOException e) {
            throw new SignatureException("Error verifying signature", e);
        }
        return null;
    }

    public static String makeSigAlg(AlgorithmId digAlgId, AlgorithmId encAlgId, boolean directSign) throws NoSuchAlgorithmException {
        String encAlg;
        switch (encAlg = encAlgId.getName()) {
            case "RSASSA-PSS": {
                PSSParameterSpec spec = (PSSParameterSpec)SignatureUtil.getParamSpec(encAlg, encAlgId.getParameters());
                if (spec == null) {
                    throw new NoSuchAlgorithmException("Missing PSSParameterSpec for RSASSA-PSS algorithm");
                }
                if (!AlgorithmId.get(spec.getDigestAlgorithm()).equals(digAlgId)) {
                    throw new NoSuchAlgorithmException("Incompatible digest algorithm");
                }
                return encAlg;
            }
            case "Ed25519": {
                if (!digAlgId.equals(SignatureUtil.EdDSADigestAlgHolder.sha512)) {
                    throw new NoSuchAlgorithmException("Incompatible digest algorithm");
                }
                return encAlg;
            }
            case "Ed448": {
                if (directSign ? !digAlgId.equals(SignatureUtil.EdDSADigestAlgHolder.shake256) : !digAlgId.equals(SignatureUtil.EdDSADigestAlgHolder.shake256$512)) {
                    throw new NoSuchAlgorithmException("Incompatible digest algorithm");
                }
                return encAlg;
            }
        }
        String digAlg = digAlgId.getName();
        String keyAlg = SignatureUtil.extractKeyAlgFromDwithE(encAlg);
        if (keyAlg == null) {
            keyAlg = encAlg;
        }
        if (digAlg.startsWith("SHA-")) {
            digAlg = "SHA" + digAlg.substring(4);
        }
        if (keyAlg.equals("EC")) {
            keyAlg = "ECDSA";
        }
        String sigAlg = digAlg + "with" + keyAlg;
        try {
            Signature.getInstance(sigAlg);
            return sigAlg;
        }
        catch (NoSuchAlgorithmException e) {
            return encAlg;
        }
    }

    SignerInfo verify(PKCS7 block) throws NoSuchAlgorithmException, SignatureException {
        return this.verify(block, null);
    }

    public BigInteger getVersion() {
        return this.version;
    }

    public X500Name getIssuerName() {
        return this.issuerName;
    }

    public BigInteger getCertificateSerialNumber() {
        return this.certificateSerialNumber;
    }

    public AlgorithmId getDigestAlgorithmId() {
        return this.digestAlgorithmId;
    }

    public PKCS9Attributes getAuthenticatedAttributes() {
        return this.authenticatedAttributes;
    }

    public AlgorithmId getDigestEncryptionAlgorithmId() {
        return this.digestEncryptionAlgorithmId;
    }

    public byte[] getEncryptedDigest() {
        return this.encryptedDigest;
    }

    public PKCS9Attributes getUnauthenticatedAttributes() {
        return this.unauthenticatedAttributes;
    }

    public PKCS7 getTsToken() throws IOException {
        if (this.unauthenticatedAttributes == null) {
            return null;
        }
        PKCS9Attribute tsTokenAttr = this.unauthenticatedAttributes.getAttribute(PKCS9Attribute.SIGNATURE_TIMESTAMP_TOKEN_OID);
        if (tsTokenAttr == null) {
            return null;
        }
        return new PKCS7((byte[])tsTokenAttr.getValue());
    }

    public Timestamp getTimestamp() throws IOException, NoSuchAlgorithmException, SignatureException, CertificateException {
        if (this.timestamp != null || !this.hasTimestamp) {
            return this.timestamp;
        }
        PKCS7 tsToken = this.getTsToken();
        if (tsToken == null) {
            this.hasTimestamp = false;
            return null;
        }
        byte[] encTsTokenInfo = tsToken.getContentInfo().getData();
        SignerInfo[] tsa = tsToken.verify(encTsTokenInfo);
        if (tsa == null || tsa.length == 0) {
            throw new SignatureException("Unable to verify timestamp");
        }
        ArrayList<X509Certificate> chain = tsa[0].getCertificateChain(tsToken);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        CertPath tsaChain = cf.generateCertPath(chain);
        TimestampToken tsTokenInfo = new TimestampToken(encTsTokenInfo);
        this.verifyTimestamp(tsTokenInfo);
        this.algorithms.putAll(tsa[0].algorithms);
        this.timestamp = new Timestamp(tsTokenInfo.getDate(), tsaChain);
        return this.timestamp;
    }

    private void verifyTimestamp(TimestampToken token) throws NoSuchAlgorithmException, SignatureException {
        AlgorithmId digestAlgId = token.getHashAlgorithm();
        this.algorithms.put(digestAlgId, new AlgorithmInfo("TimestampToken digestAlgorithm field", false));
        MessageDigest md = MessageDigest.getInstance(digestAlgId.getName());
        if (!MessageDigest.isEqual(token.getHashedMessage(), md.digest(this.encryptedDigest))) {
            throw new SignatureException("Signature timestamp (#" + Debug.toString((BigInteger)token.getSerialNumber()) + ") generated on " + token.getDate() + " is inapplicable");
        }
        if (debug != null) {
            debug.println();
            debug.println("Detected signature timestamp (#" + Debug.toString((BigInteger)token.getSerialNumber()) + ") generated on " + token.getDate());
            debug.println();
        }
    }

    public String toString() {
        HexDumpEncoder hexDump = new HexDumpEncoder();
        String out = "";
        out = out + "Signer Info for (issuer): " + this.issuerName + "\n";
        out = out + "\tversion: " + Debug.toHexString(this.version) + "\n";
        out = out + "\tcertificateSerialNumber: " + Debug.toHexString(this.certificateSerialNumber) + "\n";
        out = out + "\tdigestAlgorithmId: " + this.digestAlgorithmId + "\n";
        if (this.authenticatedAttributes != null) {
            out = out + "\tauthenticatedAttributes: " + this.authenticatedAttributes + "\n";
        }
        out = out + "\tdigestEncryptionAlgorithmId: " + this.digestEncryptionAlgorithmId + "\n";
        out = out + "\tencryptedDigest: \n" + hexDump.encodeBuffer(this.encryptedDigest) + "\n";
        if (this.unauthenticatedAttributes != null) {
            out = out + "\tunauthenticatedAttributes: " + this.unauthenticatedAttributes + "\n";
        }
        return out;
    }

    public static Set<String> verifyAlgorithms(SignerInfo[] infos, JarConstraintsParameters params, String name) throws SignatureException {
        HashMap<AlgorithmId, AlgorithmInfo> algorithms = new HashMap<AlgorithmId, AlgorithmInfo>();
        for (SignerInfo signerInfo : infos) {
            algorithms.putAll(signerInfo.algorithms);
        }
        HashSet<String> enabledAlgorithms = new HashSet<String>();
        try {
            for (Map.Entry algEntry : algorithms.entrySet()) {
                AlgorithmInfo algorithmInfo = (AlgorithmInfo)algEntry.getValue();
                params.setExtendedExceptionMsg(name, algorithmInfo.field());
                AlgorithmId algId = (AlgorithmId)algEntry.getKey();
                JAR_DISABLED_CHECK.permits(algId.getName(), algId.getParameters(), params, algorithmInfo.checkKey());
                enabledAlgorithms.add(algId.getName());
            }
        }
        catch (CertPathValidatorException e) {
            throw new SignatureException(e);
        }
        return enabledAlgorithms;
    }

    private record AlgorithmInfo(String field, boolean checkKey) {
    }
}

