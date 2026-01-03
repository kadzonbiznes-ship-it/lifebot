/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import javax.security.auth.x500.X500Principal;
import sun.security.jca.JCAUtil;
import sun.security.provider.X509Factory;
import sun.security.util.Debug;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.HexDumpEncoder;
import sun.security.util.KnownOIDs;
import sun.security.util.ObjectIdentifier;
import sun.security.util.Pem;
import sun.security.util.SignatureUtil;
import sun.security.x509.AccessDescription;
import sun.security.x509.AlgorithmId;
import sun.security.x509.AuthorityInfoAccessExtension;
import sun.security.x509.AuthorityKeyIdentifierExtension;
import sun.security.x509.BasicConstraintsExtension;
import sun.security.x509.CRLDistributionPointsExtension;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateExtensions;
import sun.security.x509.CertificatePoliciesExtension;
import sun.security.x509.CertificateValidity;
import sun.security.x509.DNSName;
import sun.security.x509.ExtendedKeyUsageExtension;
import sun.security.x509.Extension;
import sun.security.x509.GeneralName;
import sun.security.x509.GeneralNameInterface;
import sun.security.x509.GeneralNames;
import sun.security.x509.IPAddressName;
import sun.security.x509.IssuerAlternativeNameExtension;
import sun.security.x509.KeyIdentifier;
import sun.security.x509.KeyUsageExtension;
import sun.security.x509.NameConstraintsExtension;
import sun.security.x509.OIDMap;
import sun.security.x509.OIDName;
import sun.security.x509.OtherName;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.PolicyConstraintsExtension;
import sun.security.x509.PolicyMappingsExtension;
import sun.security.x509.PrivateKeyUsageExtension;
import sun.security.x509.RFC822Name;
import sun.security.x509.SerialNumber;
import sun.security.x509.SubjectAlternativeNameExtension;
import sun.security.x509.SubjectKeyIdentifierExtension;
import sun.security.x509.URIName;
import sun.security.x509.UniqueIdentity;
import sun.security.x509.UnparseableExtension;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertInfo;

public class X509CertImpl
extends X509Certificate
implements DerEncoder {
    private static final long serialVersionUID = -3457612960190864406L;
    public static final String NAME = "x509";
    private byte[] signedCert = null;
    protected X509CertInfo info = null;
    protected AlgorithmId algId = null;
    protected byte[] signature = null;
    private static final int NUM_STANDARD_KEY_USAGE = 9;
    private Collection<List<?>> subjectAlternativeNames;
    private Collection<List<?>> issuerAlternativeNames;
    private List<String> extKeyUsage;
    private Set<AccessDescription> authInfoAccess;
    private PublicKey verifiedPublicKey;
    private String verifiedProvider;
    private boolean verificationResult;
    private final ConcurrentHashMap<String, String> fingerprints = new ConcurrentHashMap(2);

    public X509CertImpl(X509CertInfo info, AlgorithmId algId, byte[] signature, byte[] signedCert) {
        this.info = info;
        this.algId = algId;
        this.signature = signature;
        this.signedCert = Objects.requireNonNull(signedCert);
    }

    public X509CertImpl(byte[] certData) throws CertificateException {
        try {
            this.parse(new DerValue(certData));
        }
        catch (IOException e) {
            throw new CertificateException("Unable to initialize, " + e, e);
        }
    }

    public X509CertImpl(DerValue derVal) throws CertificateException {
        try {
            this.parse(derVal);
        }
        catch (IOException e) {
            throw new CertificateException("Unable to initialize, " + e, e);
        }
    }

    public X509CertImpl(InputStream in) throws CertificateException {
        DerValue der;
        BufferedInputStream inBuffered = new BufferedInputStream(in);
        try {
            inBuffered.mark(Integer.MAX_VALUE);
            der = this.readRFC1421Cert(inBuffered);
        }
        catch (IOException ioe) {
            try {
                inBuffered.reset();
                der = new DerValue(inBuffered);
            }
            catch (IOException ioe1) {
                throw new CertificateException("Input stream must be either DER-encoded bytes or RFC1421 hex-encoded DER-encoded bytes: " + ioe1.getMessage(), ioe1);
            }
        }
        try {
            this.parse(der);
        }
        catch (IOException ioe) {
            this.signedCert = null;
            throw new CertificateException("Unable to parse DER value of certificate, " + ioe, ioe);
        }
    }

    private DerValue readRFC1421Cert(InputStream in) throws IOException {
        String line;
        DerValue der = null;
        BufferedReader certBufferedReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.US_ASCII));
        try {
            line = certBufferedReader.readLine();
        }
        catch (IOException ioe1) {
            throw new IOException("Unable to read InputStream: " + ioe1.getMessage());
        }
        if (line.equals("-----BEGIN CERTIFICATE-----")) {
            ByteArrayOutputStream decstream = new ByteArrayOutputStream();
            try {
                while ((line = certBufferedReader.readLine()) != null) {
                    if (line.equals("-----END CERTIFICATE-----")) {
                        der = new DerValue(decstream.toByteArray());
                    }
                    decstream.write(Pem.decode(line));
                }
            }
            catch (IOException ioe2) {
                throw new IOException("Unable to read InputStream: " + ioe2.getMessage());
            }
        } else {
            throw new IOException("InputStream is not RFC1421 hex-encoded DER bytes");
        }
        return der;
    }

    public static X509CertImpl newX509CertImpl(byte[] certData) throws CertificateException {
        X509CertImpl cert = new X509CertImpl(certData);
        JCAUtil.tryCommitCertEvent(cert);
        return cert;
    }

    @Override
    public void encode(DerOutputStream out) {
        out.writeBytes(this.signedCert);
    }

    @Override
    public byte[] getEncoded() throws CertificateEncodingException {
        return (byte[])this.getEncodedInternal().clone();
    }

    public byte[] getEncodedInternal() throws CertificateEncodingException {
        return this.signedCert;
    }

    @Override
    public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        this.verify(key, "");
    }

    @Override
    public synchronized void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        if (sigProvider == null) {
            sigProvider = "";
        }
        if (this.verifiedPublicKey != null && this.verifiedPublicKey.equals(key) && sigProvider.equals(this.verifiedProvider)) {
            if (this.verificationResult) {
                return;
            }
            throw new SignatureException("Signature does not match.");
        }
        String sigName = this.algId.getName();
        Signature sigVerf = sigProvider.isEmpty() ? Signature.getInstance(sigName) : Signature.getInstance(sigName, sigProvider);
        try {
            SignatureUtil.initVerifyWithParam(sigVerf, key, SignatureUtil.getParamSpec(sigName, this.getSigAlgParams()));
        }
        catch (ProviderException e) {
            throw new CertificateException(e.getMessage(), e.getCause());
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new CertificateException(e);
        }
        byte[] rawCert = this.info.getEncodedInfo();
        sigVerf.update(rawCert, 0, rawCert.length);
        this.verificationResult = sigVerf.verify(this.signature);
        this.verifiedPublicKey = key;
        this.verifiedProvider = sigProvider;
        if (!this.verificationResult) {
            throw new SignatureException("Signature does not match.");
        }
    }

    @Override
    public synchronized void verify(PublicKey key, Provider sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String sigName = this.algId.getName();
        Signature sigVerf = sigProvider == null ? Signature.getInstance(sigName) : Signature.getInstance(sigName, sigProvider);
        try {
            SignatureUtil.initVerifyWithParam(sigVerf, key, SignatureUtil.getParamSpec(sigName, this.getSigAlgParams()));
        }
        catch (ProviderException e) {
            throw new CertificateException(e.getMessage(), e.getCause());
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new CertificateException(e);
        }
        byte[] rawCert = this.info.getEncodedInfo();
        sigVerf.update(rawCert, 0, rawCert.length);
        this.verificationResult = sigVerf.verify(this.signature);
        this.verifiedPublicKey = key;
        if (!this.verificationResult) {
            throw new SignatureException("Signature does not match.");
        }
    }

    public static X509CertImpl newSigned(X509CertInfo info, PrivateKey key, String algorithm) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        return X509CertImpl.newSigned(info, key, algorithm, null);
    }

    public static X509CertImpl newSigned(X509CertInfo info, PrivateKey key, String algorithm, String provider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        Signature sigEngine = SignatureUtil.fromKey(algorithm, key, provider);
        AlgorithmId algId = SignatureUtil.fromSignature(sigEngine, key);
        DerOutputStream out = new DerOutputStream();
        DerOutputStream tmp = new DerOutputStream();
        info.setAlgorithmId(new CertificateAlgorithmId(algId));
        info.encode(tmp);
        byte[] rawCert = tmp.toByteArray();
        algId.encode(tmp);
        sigEngine.update(rawCert, 0, rawCert.length);
        byte[] signature = sigEngine.sign();
        tmp.putBitString(signature);
        out.write((byte)48, tmp);
        byte[] signedCert = out.toByteArray();
        return new X509CertImpl(info, algId, signature, signedCert);
    }

    @Override
    public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
        Date date = new Date();
        this.checkValidity(date);
    }

    @Override
    public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
        CertificateValidity interval;
        try {
            interval = this.info.getValidity();
        }
        catch (Exception e) {
            throw new CertificateNotYetValidException("Incorrect validity period");
        }
        if (interval == null) {
            throw new CertificateNotYetValidException("Null validity period");
        }
        interval.valid(date);
    }

    public X509CertInfo getInfo() {
        return this.info;
    }

    @Override
    public String toString() {
        if (this.info == null || this.algId == null || this.signature == null) {
            return "";
        }
        HexDumpEncoder encoder = new HexDumpEncoder();
        return "[\n" + this.info + '\n' + "  Algorithm: [" + this.algId + "]\n  Signature:\n" + encoder.encodeBuffer(this.signature) + "\n]";
    }

    @Override
    public PublicKey getPublicKey() {
        if (this.info == null) {
            return null;
        }
        return this.info.getKey().getKey();
    }

    @Override
    public int getVersion() {
        if (this.info == null) {
            return -1;
        }
        try {
            int vers = this.info.getVersion().getVersion();
            return vers + 1;
        }
        catch (Exception e) {
            return -1;
        }
    }

    @Override
    public BigInteger getSerialNumber() {
        SerialNumber ser = this.getSerialNumberObject();
        return ser != null ? ser.getNumber() : null;
    }

    public SerialNumber getSerialNumberObject() {
        if (this.info == null) {
            return null;
        }
        return this.info.getSerialNumber().getSerial();
    }

    @Override
    public Principal getSubjectDN() {
        if (this.info == null) {
            return null;
        }
        return this.info.getSubject();
    }

    @Override
    public X500Principal getSubjectX500Principal() {
        if (this.info == null) {
            return null;
        }
        try {
            return this.info.getSubject().asX500Principal();
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public Principal getIssuerDN() {
        if (this.info == null) {
            return null;
        }
        return this.info.getIssuer();
    }

    @Override
    public X500Principal getIssuerX500Principal() {
        if (this.info == null) {
            return null;
        }
        try {
            return this.info.getIssuer().asX500Principal();
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public Date getNotBefore() {
        if (this.info == null) {
            return null;
        }
        return this.info.getValidity().getNotBefore();
    }

    @Override
    public Date getNotAfter() {
        if (this.info == null) {
            return null;
        }
        return this.info.getValidity().getNotAfter();
    }

    @Override
    public byte[] getTBSCertificate() throws CertificateEncodingException {
        if (this.info != null) {
            return this.info.getEncodedInfo();
        }
        throw new CertificateEncodingException("Uninitialized certificate");
    }

    @Override
    public byte[] getSignature() {
        if (this.signature == null) {
            return null;
        }
        return (byte[])this.signature.clone();
    }

    @Override
    public String getSigAlgName() {
        if (this.algId == null) {
            return null;
        }
        return this.algId.getName();
    }

    @Override
    public String getSigAlgOID() {
        if (this.algId == null) {
            return null;
        }
        ObjectIdentifier oid = this.algId.getOID();
        return oid.toString();
    }

    public AlgorithmId getSigAlg() {
        return this.algId;
    }

    @Override
    public byte[] getSigAlgParams() {
        return this.algId == null ? null : this.algId.getEncodedParams();
    }

    @Override
    public boolean[] getIssuerUniqueID() {
        if (this.info == null) {
            return null;
        }
        UniqueIdentity id = this.info.getIssuerUniqueId();
        if (id == null) {
            return null;
        }
        return id.getId();
    }

    @Override
    public boolean[] getSubjectUniqueID() {
        if (this.info == null) {
            return null;
        }
        UniqueIdentity id = this.info.getSubjectUniqueId();
        if (id == null) {
            return null;
        }
        return id.getId();
    }

    public KeyIdentifier getAuthKeyId() {
        AuthorityKeyIdentifierExtension aki = this.getAuthorityKeyIdentifierExtension();
        if (aki != null) {
            return aki.getKeyIdentifier();
        }
        return null;
    }

    public KeyIdentifier getSubjectKeyId() {
        SubjectKeyIdentifierExtension ski = this.getSubjectKeyIdentifierExtension();
        if (ski != null) {
            return ski.getKeyIdentifier();
        }
        return null;
    }

    public AuthorityKeyIdentifierExtension getAuthorityKeyIdentifierExtension() {
        return (AuthorityKeyIdentifierExtension)this.getExtension(PKIXExtensions.AuthorityKey_Id);
    }

    public BasicConstraintsExtension getBasicConstraintsExtension() {
        return (BasicConstraintsExtension)this.getExtension(PKIXExtensions.BasicConstraints_Id);
    }

    public CertificatePoliciesExtension getCertificatePoliciesExtension() {
        return (CertificatePoliciesExtension)this.getExtension(PKIXExtensions.CertificatePolicies_Id);
    }

    public ExtendedKeyUsageExtension getExtendedKeyUsageExtension() {
        return (ExtendedKeyUsageExtension)this.getExtension(PKIXExtensions.ExtendedKeyUsage_Id);
    }

    public IssuerAlternativeNameExtension getIssuerAlternativeNameExtension() {
        return (IssuerAlternativeNameExtension)this.getExtension(PKIXExtensions.IssuerAlternativeName_Id);
    }

    public NameConstraintsExtension getNameConstraintsExtension() {
        return (NameConstraintsExtension)this.getExtension(PKIXExtensions.NameConstraints_Id);
    }

    public PolicyConstraintsExtension getPolicyConstraintsExtension() {
        return (PolicyConstraintsExtension)this.getExtension(PKIXExtensions.PolicyConstraints_Id);
    }

    public PolicyMappingsExtension getPolicyMappingsExtension() {
        return (PolicyMappingsExtension)this.getExtension(PKIXExtensions.PolicyMappings_Id);
    }

    public PrivateKeyUsageExtension getPrivateKeyUsageExtension() {
        return (PrivateKeyUsageExtension)this.getExtension(PKIXExtensions.PrivateKeyUsage_Id);
    }

    public SubjectAlternativeNameExtension getSubjectAlternativeNameExtension() {
        return (SubjectAlternativeNameExtension)this.getExtension(PKIXExtensions.SubjectAlternativeName_Id);
    }

    public SubjectKeyIdentifierExtension getSubjectKeyIdentifierExtension() {
        return (SubjectKeyIdentifierExtension)this.getExtension(PKIXExtensions.SubjectKey_Id);
    }

    public CRLDistributionPointsExtension getCRLDistributionPointsExtension() {
        return (CRLDistributionPointsExtension)this.getExtension(PKIXExtensions.CRLDistributionPoints_Id);
    }

    @Override
    public boolean hasUnsupportedCriticalExtension() {
        if (this.info == null) {
            return false;
        }
        CertificateExtensions exts = this.info.getExtensions();
        if (exts == null) {
            return false;
        }
        return exts.hasUnsupportedCriticalExtension();
    }

    @Override
    public Set<String> getCriticalExtensionOIDs() {
        if (this.info == null) {
            return null;
        }
        try {
            CertificateExtensions exts = this.info.getExtensions();
            if (exts == null) {
                return null;
            }
            TreeSet<String> extSet = new TreeSet<String>();
            for (Extension ex : exts.getAllExtensions()) {
                if (!ex.isCritical()) continue;
                extSet.add(ex.getExtensionId().toString());
            }
            return extSet;
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public Set<String> getNonCriticalExtensionOIDs() {
        if (this.info == null) {
            return null;
        }
        try {
            CertificateExtensions exts = this.info.getExtensions();
            if (exts == null) {
                return null;
            }
            TreeSet<String> extSet = new TreeSet<String>();
            for (Extension ex : exts.getAllExtensions()) {
                if (ex.isCritical()) continue;
                extSet.add(ex.getExtensionId().toString());
            }
            extSet.addAll(exts.getUnparseableExtensions().keySet());
            return extSet;
        }
        catch (Exception e) {
            return null;
        }
    }

    public Extension getExtension(ObjectIdentifier oid) {
        if (this.info == null) {
            return null;
        }
        CertificateExtensions extensions = this.info.getExtensions();
        if (extensions != null) {
            Extension ex = extensions.getExtension(oid.toString());
            if (ex != null) {
                return ex;
            }
            for (Extension ex2 : extensions.getAllExtensions()) {
                if (!ex2.getExtensionId().equals(oid)) continue;
                return ex2;
            }
        }
        return null;
    }

    public Extension getUnparseableExtension(ObjectIdentifier oid) {
        if (this.info == null) {
            return null;
        }
        CertificateExtensions extensions = this.info.getExtensions();
        if (extensions == null) {
            return null;
        }
        return extensions.getUnparseableExtensions().get(oid.toString());
    }

    @Override
    public byte[] getExtensionValue(String oid) {
        try {
            byte[] extData;
            ObjectIdentifier findOID = ObjectIdentifier.of(oid);
            String extAlias = OIDMap.getName(findOID);
            Extension certExt = null;
            CertificateExtensions exts = this.info.getExtensions();
            if (extAlias == null) {
                if (exts == null) {
                    return null;
                }
                for (Extension ex : exts.getAllExtensions()) {
                    ObjectIdentifier inCertOID = ex.getExtensionId();
                    if (!inCertOID.equals(findOID)) continue;
                    certExt = ex;
                    break;
                }
            } else {
                certExt = this.getInfo().getExtensions().getExtension(extAlias);
            }
            if (certExt == null) {
                if (exts != null) {
                    certExt = exts.getUnparseableExtensions().get(oid);
                }
                if (certExt == null) {
                    return null;
                }
            }
            if ((extData = certExt.getExtensionValue()) == null) {
                return null;
            }
            DerOutputStream out = new DerOutputStream();
            out.putOctetString(extData);
            return out.toByteArray();
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean[] getKeyUsage() {
        try {
            KeyUsageExtension certExt = (KeyUsageExtension)this.getInfo().getExtensions().getExtension("KeyUsage");
            if (certExt == null) {
                return null;
            }
            boolean[] ret = certExt.getBits();
            if (ret.length < 9) {
                boolean[] usageBits = new boolean[9];
                System.arraycopy(ret, 0, usageBits, 0, ret.length);
                ret = usageBits;
            }
            return ret;
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public synchronized List<String> getExtendedKeyUsage() throws CertificateParsingException {
        if (this.extKeyUsage != null) {
            return this.extKeyUsage;
        }
        ExtendedKeyUsageExtension ext = (ExtendedKeyUsageExtension)this.getExtensionIfParseable(PKIXExtensions.ExtendedKeyUsage_Id);
        if (ext == null) {
            return null;
        }
        this.extKeyUsage = Collections.unmodifiableList(ext.getExtendedKeyUsage());
        return this.extKeyUsage;
    }

    private Extension getExtensionIfParseable(ObjectIdentifier oid) throws CertificateParsingException {
        UnparseableExtension unparseableExt;
        Extension ext = this.getExtension(oid);
        if (ext == null && (unparseableExt = (UnparseableExtension)this.getUnparseableExtension(oid)) != null) {
            throw new CertificateParsingException(unparseableExt.exceptionMessage());
        }
        return ext;
    }

    public static List<String> getExtendedKeyUsage(X509Certificate cert) throws CertificateParsingException {
        try {
            byte[] ext = cert.getExtensionValue(KnownOIDs.extendedKeyUsage.value());
            if (ext == null) {
                return null;
            }
            DerValue val = new DerValue(ext);
            byte[] data = val.getOctetString();
            ExtendedKeyUsageExtension ekuExt = new ExtendedKeyUsageExtension(Boolean.FALSE, data);
            return Collections.unmodifiableList(ekuExt.getExtendedKeyUsage());
        }
        catch (IOException ioe) {
            throw new CertificateParsingException(ioe);
        }
    }

    @Override
    public int getBasicConstraints() {
        try {
            BasicConstraintsExtension certExt = this.getBasicConstraintsExtension();
            if (certExt == null) {
                return -1;
            }
            if (certExt.isCa()) {
                return certExt.getPathLen();
            }
            return -1;
        }
        catch (Exception e) {
            return -1;
        }
    }

    private static Collection<List<?>> makeAltNames(GeneralNames names) {
        if (names.isEmpty()) {
            return Collections.emptySet();
        }
        ArrayList newNames = new ArrayList();
        for (GeneralName gname : names.names()) {
            GeneralNameInterface name = gname.getName();
            ArrayList<Object> nameEntry = new ArrayList<Object>(2);
            nameEntry.add(name.getType());
            switch (name.getType()) {
                case 1: {
                    nameEntry.add(((RFC822Name)name).getName());
                    break;
                }
                case 2: {
                    nameEntry.add(((DNSName)name).getName());
                    break;
                }
                case 4: {
                    nameEntry.add(((X500Name)name).getRFC2253Name());
                    break;
                }
                case 6: {
                    nameEntry.add(((URIName)name).getName());
                    break;
                }
                case 7: {
                    try {
                        nameEntry.add(((IPAddressName)name).getName());
                        break;
                    }
                    catch (IOException ioe) {
                        throw new RuntimeException("IPAddress cannot be parsed", ioe);
                    }
                }
                case 8: {
                    nameEntry.add(((OIDName)name).getOID().toString());
                    break;
                }
                default: {
                    DerOutputStream derOut = new DerOutputStream();
                    name.encode(derOut);
                    nameEntry.add(derOut.toByteArray());
                    if (name.getType() != 0 || !(name instanceof OtherName)) break;
                    OtherName oname = (OtherName)name;
                    nameEntry.add(oname.getOID().toString());
                    byte[] nameValue = oname.getNameValue();
                    try {
                        String v = new DerValue(nameValue).getAsString();
                        nameEntry.add(v == null ? nameValue : (byte[])v);
                        break;
                    }
                    catch (IOException ioe) {
                        nameEntry.add(nameValue);
                    }
                }
            }
            newNames.add(Collections.unmodifiableList(nameEntry));
        }
        return Collections.unmodifiableCollection(newNames);
    }

    private static Collection<List<?>> cloneAltNames(Collection<List<?>> altNames) {
        boolean mustClone = false;
        for (List<?> nameEntry : altNames) {
            if (!(nameEntry.get(1) instanceof byte[])) continue;
            mustClone = true;
            break;
        }
        if (mustClone) {
            ArrayList namesCopy = new ArrayList();
            for (List<?> nameEntry : altNames) {
                Object nameObject = nameEntry.get(1);
                if (nameObject instanceof byte[]) {
                    ArrayList nameEntryCopy = new ArrayList(nameEntry);
                    nameEntryCopy.set(1, ((byte[])nameObject).clone());
                    namesCopy.add(Collections.unmodifiableList(nameEntryCopy));
                    continue;
                }
                namesCopy.add(nameEntry);
            }
            return Collections.unmodifiableCollection(namesCopy);
        }
        return altNames;
    }

    @Override
    public synchronized Collection<List<?>> getSubjectAlternativeNames() throws CertificateParsingException {
        if (this.subjectAlternativeNames != null) {
            return X509CertImpl.cloneAltNames(this.subjectAlternativeNames);
        }
        SubjectAlternativeNameExtension subjectAltNameExt = (SubjectAlternativeNameExtension)this.getExtensionIfParseable(PKIXExtensions.SubjectAlternativeName_Id);
        if (subjectAltNameExt == null) {
            return null;
        }
        GeneralNames names = subjectAltNameExt.getNames();
        this.subjectAlternativeNames = X509CertImpl.makeAltNames(names);
        return this.subjectAlternativeNames;
    }

    public static Collection<List<?>> getSubjectAlternativeNames(X509Certificate cert) throws CertificateParsingException {
        try {
            byte[] ext = cert.getExtensionValue(KnownOIDs.SubjectAlternativeName.value());
            if (ext == null) {
                return null;
            }
            DerValue val = new DerValue(ext);
            byte[] data = val.getOctetString();
            SubjectAlternativeNameExtension subjectAltNameExt = new SubjectAlternativeNameExtension(Boolean.FALSE, data);
            GeneralNames names = subjectAltNameExt.getNames();
            return X509CertImpl.makeAltNames(names);
        }
        catch (IOException ioe) {
            throw new CertificateParsingException(ioe);
        }
    }

    @Override
    public synchronized Collection<List<?>> getIssuerAlternativeNames() throws CertificateParsingException {
        if (this.issuerAlternativeNames != null) {
            return X509CertImpl.cloneAltNames(this.issuerAlternativeNames);
        }
        IssuerAlternativeNameExtension issuerAltNameExt = (IssuerAlternativeNameExtension)this.getExtensionIfParseable(PKIXExtensions.IssuerAlternativeName_Id);
        if (issuerAltNameExt == null) {
            return null;
        }
        GeneralNames names = issuerAltNameExt.getNames();
        this.issuerAlternativeNames = X509CertImpl.makeAltNames(names);
        return this.issuerAlternativeNames;
    }

    public static Collection<List<?>> getIssuerAlternativeNames(X509Certificate cert) throws CertificateParsingException {
        try {
            byte[] ext = cert.getExtensionValue(KnownOIDs.IssuerAlternativeName.value());
            if (ext == null) {
                return null;
            }
            DerValue val = new DerValue(ext);
            byte[] data = val.getOctetString();
            IssuerAlternativeNameExtension issuerAltNameExt = new IssuerAlternativeNameExtension(Boolean.FALSE, data);
            GeneralNames names = issuerAltNameExt.getNames();
            return X509CertImpl.makeAltNames(names);
        }
        catch (IOException ioe) {
            throw new CertificateParsingException(ioe);
        }
    }

    public AuthorityInfoAccessExtension getAuthorityInfoAccessExtension() {
        return (AuthorityInfoAccessExtension)this.getExtension(PKIXExtensions.AuthInfoAccess_Id);
    }

    private void parse(DerValue val) throws CertificateException, IOException {
        if (val.data == null || val.tag != 48) {
            throw new CertificateParsingException("invalid DER-encoded certificate data");
        }
        this.signedCert = val.toByteArray();
        DerValue[] seq = new DerValue[]{val.data.getDerValue(), val.data.getDerValue(), val.data.getDerValue()};
        if (val.data.available() != 0) {
            throw new CertificateParsingException("signed overrun, bytes = " + val.data.available());
        }
        if (seq[0].tag != 48) {
            throw new CertificateParsingException("signed fields invalid");
        }
        this.algId = AlgorithmId.parse(seq[1]);
        this.signature = seq[2].getBitString();
        if (seq[1].data.available() != 0) {
            throw new CertificateParsingException("algid field overrun");
        }
        if (seq[2].data.available() != 0) {
            throw new CertificateParsingException("signed fields overrun");
        }
        this.info = new X509CertInfo(seq[0]);
        AlgorithmId infoSigAlg = this.info.getAlgorithmId().getAlgId();
        if (!this.algId.equals(infoSigAlg)) {
            throw new CertificateException("Signature algorithm mismatch");
        }
    }

    private static X500Principal getX500Principal(X509Certificate cert, boolean getIssuer) throws Exception {
        byte[] encoded = cert.getEncoded();
        DerInputStream derIn = new DerInputStream(encoded);
        DerValue tbsCert = derIn.getSequence(3)[0];
        DerInputStream tbsIn = tbsCert.data;
        DerValue tmp = tbsIn.getDerValue();
        if (tmp.isContextSpecific((byte)0)) {
            tmp = tbsIn.getDerValue();
        }
        tmp = tbsIn.getDerValue();
        tmp = tbsIn.getDerValue();
        if (!getIssuer) {
            tmp = tbsIn.getDerValue();
            tmp = tbsIn.getDerValue();
        }
        byte[] principalBytes = tmp.toByteArray();
        return new X500Principal(principalBytes);
    }

    public static X500Principal getSubjectX500Principal(X509Certificate cert) {
        try {
            return X509CertImpl.getX500Principal(cert, false);
        }
        catch (Exception e) {
            throw new RuntimeException("Could not parse subject", e);
        }
    }

    public static X500Principal getIssuerX500Principal(X509Certificate cert) {
        try {
            return X509CertImpl.getX500Principal(cert, true);
        }
        catch (Exception e) {
            throw new RuntimeException("Could not parse issuer", e);
        }
    }

    public static byte[] getEncodedInternal(Certificate cert) throws CertificateEncodingException {
        if (cert instanceof X509CertImpl) {
            return ((X509CertImpl)cert).getEncodedInternal();
        }
        return cert.getEncoded();
    }

    public static X509CertImpl toImpl(X509Certificate cert) throws CertificateException {
        if (cert instanceof X509CertImpl) {
            return (X509CertImpl)cert;
        }
        return X509Factory.intern(cert);
    }

    public static boolean isSelfIssued(X509Certificate cert) {
        X500Principal subject = cert.getSubjectX500Principal();
        X500Principal issuer = cert.getIssuerX500Principal();
        return subject.equals(issuer);
    }

    public static boolean isSelfSigned(X509Certificate cert, String sigProvider) {
        if (X509CertImpl.isSelfIssued(cert)) {
            try {
                if (sigProvider == null) {
                    cert.verify(cert.getPublicKey());
                } else {
                    cert.verify(cert.getPublicKey(), sigProvider);
                }
                return true;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return false;
    }

    private String getFingerprint(String algorithm, Debug debug) {
        return this.fingerprints.computeIfAbsent(algorithm, x -> {
            try {
                return X509CertImpl.getFingerprintInternal(x, this.getEncodedInternal(), debug);
            }
            catch (CertificateEncodingException e) {
                if (debug != null) {
                    debug.println("Cannot encode certificate: " + e);
                }
                return null;
            }
        });
    }

    private static String getFingerprintInternal(String algorithm, byte[] encodedCert, Debug debug) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] digest = md.digest(encodedCert);
            return HexFormat.of().withUpperCase().formatHex(digest);
        }
        catch (NoSuchAlgorithmException e) {
            if (debug != null) {
                debug.println("Cannot create " + algorithm + " MessageDigest: " + e);
            }
            return null;
        }
    }

    public static String getFingerprint(String algorithm, X509Certificate cert, Debug debug) {
        if (cert instanceof X509CertImpl) {
            return ((X509CertImpl)cert).getFingerprint(algorithm, debug);
        }
        try {
            return X509CertImpl.getFingerprintInternal(algorithm, cert.getEncoded(), debug);
        }
        catch (CertificateEncodingException e) {
            if (debug != null) {
                debug.println("Cannot encode certificate: " + e);
            }
            return null;
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException("X509CertImpls are not directly deserializable");
    }
}

