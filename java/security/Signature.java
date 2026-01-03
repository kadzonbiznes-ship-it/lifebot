/*
 * Decompiled with CFR 0.152.
 */
package java.security;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import jdk.internal.access.JavaSecuritySignatureAccess;
import jdk.internal.access.SharedSecrets;
import sun.security.jca.GetInstance;
import sun.security.jca.ServiceId;
import sun.security.util.Debug;
import sun.security.util.KnownOIDs;

public abstract class Signature
extends SignatureSpi {
    private static final Debug debug;
    private static final Debug pdebug;
    private static final boolean skipDebug;
    private String algorithm;
    Provider provider;
    protected static final int UNINITIALIZED = 0;
    protected static final int SIGN = 2;
    protected static final int VERIFY = 3;
    protected int state = 0;
    private static final String RSA_SIGNATURE = "NONEwithRSA";
    private static final String RSA_CIPHER = "RSA/ECB/PKCS1Padding";
    private static final List<ServiceId> rsaIds;
    private static final Map<String, Boolean> signatureInfo;

    protected Signature(String algorithm) {
        this.algorithm = algorithm;
    }

    public static Signature getInstance(String algorithm) throws NoSuchAlgorithmException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        List<Provider.Service> list = algorithm.equalsIgnoreCase(RSA_SIGNATURE) ? GetInstance.getServices(rsaIds) : GetInstance.getServices("Signature", algorithm);
        Iterator<Provider.Service> t = list.iterator();
        if (!t.hasNext()) {
            throw new NoSuchAlgorithmException(algorithm + " Signature not available");
        }
        Provider.Service s;
        while (!Signature.isSpi(s = t.next())) {
            try {
                GetInstance.Instance instance = GetInstance.getInstance(s, SignatureSpi.class);
                return Signature.getInstance(instance, algorithm);
            }
            catch (NoSuchAlgorithmException e) {
                NoSuchAlgorithmException failure = e;
                if (t.hasNext()) continue;
                throw failure;
            }
            break;
        }
        return new Delegate(s, t, algorithm);
    }

    private static Signature getInstance(GetInstance.Instance instance, String algorithm) {
        Signature sig;
        if (instance.impl instanceof Signature) {
            sig = (Signature)instance.impl;
            sig.algorithm = algorithm;
        } else {
            SignatureSpi spi = (SignatureSpi)instance.impl;
            sig = Delegate.of(spi, algorithm);
        }
        sig.provider = instance.provider;
        return sig;
    }

    private static boolean isSpi(Provider.Service s) {
        if (s.getType().equals("Cipher")) {
            return true;
        }
        String className = s.getClassName();
        Boolean result = signatureInfo.get(className);
        if (result == null) {
            try {
                boolean r;
                Object instance = s.newInstance(null);
                boolean bl = r = instance instanceof SignatureSpi && !(instance instanceof Signature);
                if (debug != null && !r) {
                    debug.println("Not a SignatureSpi " + className);
                    debug.println("Delayed provider selection may not be available for algorithm " + s.getAlgorithm());
                }
                result = r;
                signatureInfo.put(className, result);
            }
            catch (Exception e) {
                return false;
            }
        }
        return result;
    }

    public static Signature getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        if (algorithm.equalsIgnoreCase(RSA_SIGNATURE)) {
            if (provider == null || provider.isEmpty()) {
                throw new IllegalArgumentException("missing provider");
            }
            Provider p = Security.getProvider(provider);
            if (p == null) {
                throw new NoSuchProviderException("no such provider: " + provider);
            }
            return Signature.getInstanceRSA(p);
        }
        GetInstance.Instance instance = GetInstance.getInstance("Signature", SignatureSpi.class, algorithm, provider);
        return Signature.getInstance(instance, algorithm);
    }

    public static Signature getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        if (algorithm.equalsIgnoreCase(RSA_SIGNATURE)) {
            if (provider == null) {
                throw new IllegalArgumentException("missing provider");
            }
            return Signature.getInstanceRSA(provider);
        }
        GetInstance.Instance instance = GetInstance.getInstance("Signature", SignatureSpi.class, algorithm, provider);
        return Signature.getInstance(instance, algorithm);
    }

    private static Signature getInstanceRSA(Provider p) throws NoSuchAlgorithmException {
        Provider.Service s = p.getService("Signature", RSA_SIGNATURE);
        if (s != null) {
            GetInstance.Instance instance = GetInstance.getInstance(s, SignatureSpi.class);
            return Signature.getInstance(instance, RSA_SIGNATURE);
        }
        try {
            Cipher c = Cipher.getInstance(RSA_CIPHER, p);
            return Delegate.of(new CipherAdapter(c), RSA_SIGNATURE);
        }
        catch (GeneralSecurityException e) {
            throw new NoSuchAlgorithmException("no such algorithm: NONEwithRSA for provider " + p.getName(), e);
        }
    }

    public final Provider getProvider() {
        this.chooseFirstProvider();
        return this.provider;
    }

    private String getProviderName() {
        return this.provider == null ? "(no provider)" : this.provider.getName();
    }

    void chooseFirstProvider() {
    }

    public final void initVerify(PublicKey publicKey) throws InvalidKeyException {
        this.engineInitVerify(publicKey);
        this.state = 3;
        if (!skipDebug && pdebug != null) {
            pdebug.println("Signature." + this.algorithm + " verification algorithm from: " + this.getProviderName());
        }
    }

    final void initVerify(PublicKey publicKey, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.engineInitVerify(publicKey, params);
        this.state = 3;
        if (!skipDebug && pdebug != null) {
            pdebug.println("Signature." + this.algorithm + " verification algorithm from: " + this.getProviderName());
        }
    }

    private static PublicKey getPublicKeyFromCert(Certificate cert) throws InvalidKeyException {
        boolean[] keyUsageInfo;
        X509Certificate xcert;
        Set<String> critSet;
        if (cert instanceof X509Certificate && (critSet = (xcert = (X509Certificate)cert).getCriticalExtensionOIDs()) != null && !critSet.isEmpty() && critSet.contains(KnownOIDs.KeyUsage.value()) && (keyUsageInfo = xcert.getKeyUsage()) != null && !keyUsageInfo[0]) {
            throw new InvalidKeyException("Wrong key usage");
        }
        return cert.getPublicKey();
    }

    public final void initVerify(Certificate certificate) throws InvalidKeyException {
        this.engineInitVerify(Signature.getPublicKeyFromCert(certificate));
        this.state = 3;
        if (!skipDebug && pdebug != null) {
            pdebug.println("Signature." + this.algorithm + " verification algorithm from: " + this.getProviderName());
        }
    }

    final void initVerify(Certificate certificate, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.engineInitVerify(Signature.getPublicKeyFromCert(certificate), params);
        this.state = 3;
        if (!skipDebug && pdebug != null) {
            pdebug.println("Signature." + this.algorithm + " verification algorithm from: " + this.getProviderName());
        }
    }

    public final void initSign(PrivateKey privateKey) throws InvalidKeyException {
        this.engineInitSign(privateKey);
        this.state = 2;
        if (!skipDebug && pdebug != null) {
            pdebug.println("Signature." + this.algorithm + " signing algorithm from: " + this.getProviderName());
        }
    }

    public final void initSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
        this.engineInitSign(privateKey, random);
        this.state = 2;
        if (!skipDebug && pdebug != null) {
            pdebug.println("Signature." + this.algorithm + " signing algorithm from: " + this.getProviderName());
        }
    }

    final void initSign(PrivateKey privateKey, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.engineInitSign(privateKey, params, random);
        this.state = 2;
        if (!skipDebug && pdebug != null) {
            pdebug.println("Signature." + this.algorithm + " signing algorithm from: " + this.getProviderName());
        }
    }

    public final byte[] sign() throws SignatureException {
        if (this.state == 2) {
            return this.engineSign();
        }
        throw new SignatureException("object not initialized for signing");
    }

    public final int sign(byte[] outbuf, int offset, int len) throws SignatureException {
        if (outbuf == null) {
            throw new IllegalArgumentException("No output buffer given");
        }
        if (offset < 0 || len < 0) {
            throw new IllegalArgumentException("offset or len is less than 0");
        }
        if (outbuf.length - offset < len) {
            throw new IllegalArgumentException("Output buffer too small for specified offset and length");
        }
        if (this.state != 2) {
            throw new SignatureException("object not initialized for signing");
        }
        return this.engineSign(outbuf, offset, len);
    }

    public final boolean verify(byte[] signature) throws SignatureException {
        if (this.state == 3) {
            return this.engineVerify(signature);
        }
        throw new SignatureException("object not initialized for verification");
    }

    public final boolean verify(byte[] signature, int offset, int length) throws SignatureException {
        if (this.state == 3) {
            if (signature == null) {
                throw new IllegalArgumentException("signature is null");
            }
            if (offset < 0 || length < 0) {
                throw new IllegalArgumentException("offset or length is less than 0");
            }
            if (signature.length - offset < length) {
                throw new IllegalArgumentException("signature too small for specified offset and length");
            }
            return this.engineVerify(signature, offset, length);
        }
        throw new SignatureException("object not initialized for verification");
    }

    public final void update(byte b) throws SignatureException {
        if (this.state != 3 && this.state != 2) {
            throw new SignatureException("object not initialized for signature or verification");
        }
        this.engineUpdate(b);
    }

    public final void update(byte[] data) throws SignatureException {
        this.update(data, 0, data.length);
    }

    public final void update(byte[] data, int off, int len) throws SignatureException {
        if (this.state == 2 || this.state == 3) {
            if (data == null) {
                throw new IllegalArgumentException("data is null");
            }
            if (off < 0 || len < 0) {
                throw new IllegalArgumentException("off or len is less than 0");
            }
            if (data.length - off < len) {
                throw new IllegalArgumentException("data too small for specified offset and length");
            }
        } else {
            throw new SignatureException("object not initialized for signature or verification");
        }
        this.engineUpdate(data, off, len);
    }

    public final void update(ByteBuffer data) throws SignatureException {
        if (this.state != 2 && this.state != 3) {
            throw new SignatureException("object not initialized for signature or verification");
        }
        if (data == null) {
            throw new NullPointerException();
        }
        this.engineUpdate(data);
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public String toString() {
        String initState = switch (this.state) {
            case 0 -> "<not initialized>";
            case 3 -> "<initialized for verifying>";
            case 2 -> "<initialized for signing>";
            default -> "";
        };
        return "Signature object: " + this.getAlgorithm() + initState;
    }

    @Deprecated
    public final void setParameter(String param, Object value) throws InvalidParameterException {
        this.engineSetParameter(param, value);
    }

    public final void setParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
        this.engineSetParameter(params);
    }

    public final AlgorithmParameters getParameters() {
        return this.engineGetParameters();
    }

    @Deprecated
    public final Object getParameter(String param) throws InvalidParameterException {
        return this.engineGetParameter(param);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        }
        throw new CloneNotSupportedException();
    }

    static {
        SharedSecrets.setJavaSecuritySignatureAccess(new JavaSecuritySignatureAccess(){

            @Override
            public void initVerify(Signature s, PublicKey publicKey, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
                s.initVerify(publicKey, params);
            }

            @Override
            public void initVerify(Signature s, Certificate certificate, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
                s.initVerify(certificate, params);
            }

            @Override
            public void initSign(Signature s, PrivateKey privateKey, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
                s.initSign(privateKey, params, random);
            }
        });
        debug = Debug.getInstance("jca", "Signature");
        pdebug = Debug.getInstance("provider", "Provider");
        skipDebug = Debug.isOn("engine=") && !Debug.isOn("signature");
        rsaIds = List.of(new ServiceId("Signature", RSA_SIGNATURE), new ServiceId("Cipher", RSA_CIPHER), new ServiceId("Cipher", "RSA/ECB"), new ServiceId("Cipher", "RSA//PKCS1Padding"), new ServiceId("Cipher", "RSA"));
        signatureInfo = new ConcurrentHashMap<String, Boolean>();
        signatureInfo.put("sun.security.provider.DSA$RawDSA", true);
        signatureInfo.put("sun.security.provider.DSA$SHA1withDSA", true);
        signatureInfo.put("sun.security.rsa.RSASignature$MD2withRSA", true);
        signatureInfo.put("sun.security.rsa.RSASignature$MD5withRSA", true);
        signatureInfo.put("sun.security.rsa.RSASignature$SHA1withRSA", true);
        signatureInfo.put("sun.security.rsa.RSASignature$SHA256withRSA", true);
        signatureInfo.put("sun.security.rsa.RSASignature$SHA384withRSA", true);
        signatureInfo.put("sun.security.rsa.RSASignature$SHA512withRSA", true);
        signatureInfo.put("sun.security.rsa.RSAPSSSignature", true);
        signatureInfo.put("sun.security.pkcs11.P11Signature", true);
    }

    private static class Delegate
    extends Signature {
        private SignatureSpi sigSpi;
        private final Object lock;
        private Provider.Service firstService;
        private Iterator<Provider.Service> serviceIterator;
        private static int warnCount = 10;
        private static final int I_PUB = 1;
        private static final int I_PRIV = 2;
        private static final int I_PRIV_SR = 3;
        private static final int I_PUB_PARAM = 4;
        private static final int I_PRIV_PARAM_SR = 5;
        private static final int S_PARAM = 6;

        static Delegate of(SignatureSpi sigSpi, String algorithm) {
            if (sigSpi instanceof Cloneable) {
                return new CloneableDelegate(sigSpi, algorithm);
            }
            return new Delegate(sigSpi, algorithm);
        }

        private Delegate(SignatureSpi sigSpi, String algorithm) {
            super(algorithm);
            this.sigSpi = sigSpi;
            this.lock = null;
        }

        private Delegate(Provider.Service service, Iterator<Provider.Service> iterator, String algorithm) {
            super(algorithm);
            this.firstService = service;
            this.serviceIterator = iterator;
            this.lock = new Object();
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            this.chooseFirstProvider();
            if (this.sigSpi instanceof Cloneable) {
                CloneableDelegate that = new CloneableDelegate((SignatureSpi)this.sigSpi.clone(), this.algorithm);
                that.provider = this.provider;
                return that;
            }
            throw new CloneNotSupportedException();
        }

        private static SignatureSpi newInstance(Provider.Service s) throws NoSuchAlgorithmException {
            if (s.getType().equals("Cipher")) {
                try {
                    Cipher c = Cipher.getInstance(Signature.RSA_CIPHER, s.getProvider());
                    return new CipherAdapter(c);
                }
                catch (NoSuchPaddingException e) {
                    throw new NoSuchAlgorithmException(e);
                }
            }
            Object o = s.newInstance(null);
            if (!(o instanceof SignatureSpi)) {
                throw new NoSuchAlgorithmException("Not a SignatureSpi: " + o.getClass().getName());
            }
            return (SignatureSpi)o;
        }

        @Override
        void chooseFirstProvider() {
            if (this.sigSpi != null) {
                return;
            }
            Object object = this.lock;
            synchronized (object) {
                int w;
                if (this.sigSpi != null) {
                    return;
                }
                if (debug != null && (w = --warnCount) >= 0) {
                    debug.println("Signature.init() not first method called, disabling delayed provider selection");
                    if (w == 0) {
                        debug.println("Further warnings of this type will be suppressed");
                    }
                    new Exception("Debug call trace").printStackTrace();
                }
                NoSuchAlgorithmException lastException = null;
                while (this.firstService != null || this.serviceIterator.hasNext()) {
                    Provider.Service s;
                    if (this.firstService != null) {
                        s = this.firstService;
                        this.firstService = null;
                    } else {
                        s = this.serviceIterator.next();
                    }
                    if (!Signature.isSpi(s)) continue;
                    try {
                        this.sigSpi = Delegate.newInstance(s);
                        this.provider = s.getProvider();
                        this.firstService = null;
                        this.serviceIterator = null;
                        return;
                    }
                    catch (NoSuchAlgorithmException e) {
                        lastException = e;
                    }
                }
                ProviderException e = new ProviderException("Could not construct SignatureSpi instance");
                if (lastException != null) {
                    e.initCause(lastException);
                }
                throw e;
            }
        }

        private void chooseProvider(int type, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
            Object object = this.lock;
            synchronized (object) {
                if (this.sigSpi != null) {
                    return;
                }
                Exception lastException = null;
                while (this.firstService != null || this.serviceIterator.hasNext()) {
                    Provider.Service s;
                    if (this.firstService != null) {
                        s = this.firstService;
                        this.firstService = null;
                    } else {
                        s = this.serviceIterator.next();
                    }
                    if (key != null && !s.supportsParameter(key) || !Signature.isSpi(s)) continue;
                    try {
                        SignatureSpi spi = Delegate.newInstance(s);
                        this.tryOperation(spi, type, key, params, random);
                        this.provider = s.getProvider();
                        this.sigSpi = spi;
                        this.firstService = null;
                        this.serviceIterator = null;
                        return;
                    }
                    catch (Exception e) {
                        if (lastException != null) continue;
                        lastException = e;
                    }
                }
                if (lastException instanceof InvalidKeyException) {
                    throw (InvalidKeyException)lastException;
                }
                if (lastException instanceof RuntimeException) {
                    throw (RuntimeException)lastException;
                }
                if (lastException instanceof InvalidAlgorithmParameterException) {
                    throw (InvalidAlgorithmParameterException)lastException;
                }
                String k = key != null ? key.getClass().getName() : "(null)";
                throw new InvalidKeyException("No installed provider supports this key: " + k, lastException);
            }
        }

        private void tryOperation(SignatureSpi spi, int type, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
            switch (type) {
                case 1: {
                    spi.engineInitVerify((PublicKey)key);
                    break;
                }
                case 4: {
                    spi.engineInitVerify((PublicKey)key, params);
                    break;
                }
                case 2: {
                    spi.engineInitSign((PrivateKey)key);
                    break;
                }
                case 3: {
                    spi.engineInitSign((PrivateKey)key, random);
                    break;
                }
                case 5: {
                    spi.engineInitSign((PrivateKey)key, params, random);
                    break;
                }
                case 6: {
                    spi.engineSetParameter(params);
                    break;
                }
                default: {
                    throw new AssertionError((Object)("Internal error: " + type));
                }
            }
        }

        @Override
        protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
            if (this.sigSpi != null) {
                this.sigSpi.engineInitVerify(publicKey);
            } else {
                try {
                    this.chooseProvider(1, publicKey, null, null);
                }
                catch (InvalidAlgorithmParameterException iape) {
                    throw new InvalidKeyException(iape);
                }
            }
        }

        @Override
        void engineInitVerify(PublicKey publicKey, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
            if (this.sigSpi != null) {
                this.sigSpi.engineInitVerify(publicKey, params);
            } else {
                this.chooseProvider(4, publicKey, params, null);
            }
        }

        @Override
        protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
            if (this.sigSpi != null) {
                this.sigSpi.engineInitSign(privateKey);
            } else {
                try {
                    this.chooseProvider(2, privateKey, null, null);
                }
                catch (InvalidAlgorithmParameterException iape) {
                    throw new InvalidKeyException(iape);
                }
            }
        }

        @Override
        protected void engineInitSign(PrivateKey privateKey, SecureRandom sr) throws InvalidKeyException {
            if (this.sigSpi != null) {
                this.sigSpi.engineInitSign(privateKey, sr);
            } else {
                try {
                    this.chooseProvider(3, privateKey, null, sr);
                }
                catch (InvalidAlgorithmParameterException iape) {
                    throw new InvalidKeyException(iape);
                }
            }
        }

        @Override
        void engineInitSign(PrivateKey privateKey, AlgorithmParameterSpec params, SecureRandom sr) throws InvalidKeyException, InvalidAlgorithmParameterException {
            if (this.sigSpi != null) {
                this.sigSpi.engineInitSign(privateKey, params, sr);
            } else {
                this.chooseProvider(5, privateKey, params, sr);
            }
        }

        @Override
        protected void engineUpdate(byte b) throws SignatureException {
            this.chooseFirstProvider();
            this.sigSpi.engineUpdate(b);
        }

        @Override
        protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
            this.chooseFirstProvider();
            this.sigSpi.engineUpdate(b, off, len);
        }

        @Override
        protected void engineUpdate(ByteBuffer data) {
            this.chooseFirstProvider();
            this.sigSpi.engineUpdate(data);
        }

        @Override
        protected byte[] engineSign() throws SignatureException {
            this.chooseFirstProvider();
            return this.sigSpi.engineSign();
        }

        @Override
        protected int engineSign(byte[] outbuf, int offset, int len) throws SignatureException {
            this.chooseFirstProvider();
            return this.sigSpi.engineSign(outbuf, offset, len);
        }

        @Override
        protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
            this.chooseFirstProvider();
            return this.sigSpi.engineVerify(sigBytes);
        }

        @Override
        protected boolean engineVerify(byte[] sigBytes, int offset, int length) throws SignatureException {
            this.chooseFirstProvider();
            return this.sigSpi.engineVerify(sigBytes, offset, length);
        }

        @Override
        protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
            this.chooseFirstProvider();
            this.sigSpi.engineSetParameter(param, value);
        }

        @Override
        protected void engineSetParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            if (this.sigSpi != null) {
                this.sigSpi.engineSetParameter(params);
            } else {
                try {
                    this.chooseProvider(6, null, params, null);
                }
                catch (InvalidKeyException ike) {
                    throw new InvalidAlgorithmParameterException(ike);
                }
            }
        }

        @Override
        protected Object engineGetParameter(String param) throws InvalidParameterException {
            this.chooseFirstProvider();
            return this.sigSpi.engineGetParameter(param);
        }

        @Override
        protected AlgorithmParameters engineGetParameters() {
            this.chooseFirstProvider();
            return this.sigSpi.engineGetParameters();
        }

        private static final class CloneableDelegate
        extends Delegate
        implements Cloneable {
            private CloneableDelegate(SignatureSpi digestSpi, String algorithm) {
                super(digestSpi, algorithm);
            }
        }
    }

    private static class CipherAdapter
    extends SignatureSpi {
        private final Cipher cipher;
        private ByteArrayOutputStream data;

        CipherAdapter(Cipher cipher) {
            this.cipher = cipher;
        }

        @Override
        protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
            this.cipher.init(2, publicKey);
            if (this.data == null) {
                this.data = new ByteArrayOutputStream(128);
            } else {
                this.data.reset();
            }
        }

        @Override
        protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
            this.cipher.init(1, privateKey);
            this.data = null;
        }

        @Override
        protected void engineInitSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
            this.cipher.init(1, (Key)privateKey, random);
            this.data = null;
        }

        @Override
        protected void engineUpdate(byte b) throws SignatureException {
            this.engineUpdate(new byte[]{b}, 0, 1);
        }

        @Override
        protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
            if (this.data != null) {
                this.data.write(b, off, len);
                return;
            }
            byte[] out = this.cipher.update(b, off, len);
            if (out != null && out.length != 0) {
                throw new SignatureException("Cipher unexpectedly returned data");
            }
        }

        @Override
        protected byte[] engineSign() throws SignatureException {
            try {
                return this.cipher.doFinal();
            }
            catch (BadPaddingException | IllegalBlockSizeException e) {
                throw new SignatureException("doFinal() failed", e);
            }
        }

        @Override
        protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
            try {
                byte[] out = this.cipher.doFinal(sigBytes);
                byte[] dataBytes = this.data.toByteArray();
                this.data.reset();
                return MessageDigest.isEqual(out, dataBytes);
            }
            catch (BadPaddingException e) {
                return false;
            }
            catch (IllegalBlockSizeException e) {
                throw new SignatureException("doFinal() failed", e);
            }
        }

        @Override
        protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
            throw new InvalidParameterException("Parameters not supported");
        }

        @Override
        protected Object engineGetParameter(String param) throws InvalidParameterException {
            throw new InvalidParameterException("Parameters not supported");
        }
    }
}

