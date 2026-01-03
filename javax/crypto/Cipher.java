/*
 * Decompiled with CFR 0.152.
 */
package javax.crypto;

import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import javax.crypto.BadPaddingException;
import javax.crypto.CipherSpi;
import javax.crypto.CryptoAllPermission;
import javax.crypto.CryptoPermission;
import javax.crypto.ExemptionMechanism;
import javax.crypto.ExemptionMechanismException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.JceSecurity;
import javax.crypto.JceSecurityManager;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.NullCipher;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.RC2ParameterSpec;
import javax.crypto.spec.RC5ParameterSpec;
import sun.security.jca.GetInstance;
import sun.security.jca.JCAUtil;
import sun.security.jca.ServiceId;
import sun.security.util.Debug;
import sun.security.util.KnownOIDs;

public class Cipher {
    private static final Debug debug = Debug.getInstance("jca", "Cipher");
    private static final Debug pdebug = Debug.getInstance("provider", "Provider");
    private static final boolean skipDebug = Debug.isOn("engine=") && !Debug.isOn("cipher");
    public static final int ENCRYPT_MODE = 1;
    public static final int DECRYPT_MODE = 2;
    public static final int WRAP_MODE = 3;
    public static final int UNWRAP_MODE = 4;
    public static final int PUBLIC_KEY = 1;
    public static final int PRIVATE_KEY = 2;
    public static final int SECRET_KEY = 3;
    private Provider provider;
    private CipherSpi spi;
    private final String transformation;
    private CryptoPermission cryptoPerm;
    private ExemptionMechanism exmech;
    private boolean initialized = false;
    private int opmode = 0;
    private CipherSpi firstSpi;
    private Provider.Service firstService;
    private Iterator<Provider.Service> serviceIterator;
    private List<Transform> transforms;
    private final Object lock;
    private static final String SHA512TRUNCATED = "SHA512/2";
    private static final String ATTR_MODE = "SupportedModes";
    private static final String ATTR_PAD = "SupportedPaddings";
    private static final int S_NO = 0;
    private static final int S_MAYBE = 1;
    private static final int S_YES = 2;
    private static int warnCount = 10;
    private static final int I_KEY = 1;
    private static final int I_PARAMSPEC = 2;
    private static final int I_PARAMS = 3;
    private static final int I_CERT = 4;

    protected Cipher(CipherSpi cipherSpi, Provider provider, String transformation) {
        if (!JceSecurityManager.INSTANCE.isCallerTrusted(JceSecurityManager.WALKER.getCallerClass(), provider)) {
            throw new IllegalArgumentException("Cannot construct cipher");
        }
        this.spi = cipherSpi;
        this.provider = provider;
        this.transformation = transformation;
        this.cryptoPerm = CryptoAllPermission.INSTANCE;
        this.lock = null;
    }

    Cipher(CipherSpi cipherSpi, String transformation) {
        this.spi = cipherSpi;
        this.transformation = transformation;
        this.cryptoPerm = CryptoAllPermission.INSTANCE;
        this.lock = null;
    }

    private Cipher(CipherSpi firstSpi, Provider.Service firstService, Iterator<Provider.Service> serviceIterator, String transformation, List<Transform> transforms) {
        this.firstSpi = firstSpi;
        this.firstService = firstService;
        this.serviceIterator = serviceIterator;
        this.transforms = transforms;
        this.transformation = transformation;
        this.lock = new Object();
    }

    private static String[] tokenizeTransformation(String transformation) throws NoSuchAlgorithmException {
        if (transformation == null) {
            throw new NoSuchAlgorithmException("No transformation given");
        }
        String[] parts = new String[]{"", "", ""};
        int sha512Idx = transformation.toUpperCase(Locale.ENGLISH).indexOf(SHA512TRUNCATED);
        int startIdx = sha512Idx == -1 ? 0 : sha512Idx + SHA512TRUNCATED.length();
        int endIdx = transformation.indexOf(47, startIdx);
        if (endIdx == -1) {
            parts[0] = transformation.trim();
        } else {
            parts[0] = transformation.substring(0, endIdx).trim();
            startIdx = endIdx + 1;
            if ((endIdx = transformation.indexOf(47, startIdx)) == -1) {
                throw new NoSuchAlgorithmException("Invalid transformation format:" + transformation);
            }
            parts[1] = transformation.substring(startIdx, endIdx).trim();
            parts[2] = transformation.substring(endIdx + 1).trim();
        }
        if (parts[0].isEmpty()) {
            throw new NoSuchAlgorithmException("Invalid transformation: algorithm not specified-" + transformation);
        }
        return parts;
    }

    private static List<Transform> getTransforms(String transformation) throws NoSuchAlgorithmException {
        String[] parts = Cipher.tokenizeTransformation(transformation);
        String alg = parts[0];
        String mode = parts[1];
        String pad = parts[2];
        if (mode.length() == 0 && pad.length() == 0) {
            Transform tr = new Transform(alg, "", null, null);
            return Collections.singletonList(tr);
        }
        ArrayList<Transform> list = new ArrayList<Transform>(4);
        list.add(new Transform(alg, "/" + mode + "/" + pad, null, null));
        list.add(new Transform(alg, "/" + mode, null, pad));
        list.add(new Transform(alg, "//" + pad, mode, null));
        list.add(new Transform(alg, "", mode, pad));
        return list;
    }

    private static Transform getTransform(Provider.Service s, List<Transform> transforms) {
        String alg = s.getAlgorithm().toUpperCase(Locale.ENGLISH);
        for (Transform tr : transforms) {
            if (!alg.endsWith(tr.suffix)) continue;
            return tr;
        }
        return null;
    }

    public static final Cipher getInstance(String transformation) throws NoSuchAlgorithmException, NoSuchPaddingException {
        if (transformation == null || transformation.isEmpty()) {
            throw new NoSuchAlgorithmException("Null or empty transformation");
        }
        List<Transform> transforms = Cipher.getTransforms(transformation);
        ArrayList<ServiceId> cipherServices = new ArrayList<ServiceId>(transforms.size());
        for (Transform transform : transforms) {
            cipherServices.add(new ServiceId("Cipher", transform.transform));
        }
        List<Provider.Service> services = GetInstance.getServices(cipherServices);
        Iterator<Provider.Service> t = services.iterator();
        Exception failure = null;
        while (t.hasNext()) {
            int canuse;
            Transform tr;
            Provider.Service s = t.next();
            if (!JceSecurity.canUseProvider(s.getProvider()) || (tr = Cipher.getTransform(s, transforms)) == null || (canuse = tr.supportsModePadding(s)) == 0) continue;
            try {
                CipherSpi spi = (CipherSpi)s.newInstance(null);
                tr.setModePadding(spi);
                return new Cipher(null, s, t, transformation, transforms);
            }
            catch (Exception e) {
                failure = e;
            }
        }
        throw new NoSuchAlgorithmException("Cannot find any provider supporting " + transformation, failure);
    }

    public static final Cipher getInstance(String transformation, String provider) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
        if (transformation == null || transformation.isEmpty()) {
            throw new NoSuchAlgorithmException("Null or empty transformation");
        }
        if (provider == null || provider.isEmpty()) {
            throw new IllegalArgumentException("Missing provider");
        }
        Provider p = Security.getProvider(provider);
        if (p == null) {
            throw new NoSuchProviderException("No such provider: " + provider);
        }
        return Cipher.getInstance(transformation, p);
    }

    private String getProviderName() {
        return this.provider == null ? "(no provider)" : this.provider.getName();
    }

    public static final Cipher getInstance(String transformation, Provider provider) throws NoSuchAlgorithmException, NoSuchPaddingException {
        if (transformation == null || transformation.isEmpty()) {
            throw new NoSuchAlgorithmException("Null or empty transformation");
        }
        if (provider == null) {
            throw new IllegalArgumentException("Missing provider");
        }
        Exception failure = null;
        List<Transform> transforms = Cipher.getTransforms(transformation);
        boolean providerChecked = false;
        String paddingError = null;
        for (Transform tr : transforms) {
            Provider.Service s = provider.getService("Cipher", tr.transform);
            if (s == null) continue;
            if (!providerChecked) {
                Exception ve = JceSecurity.getVerificationResult(provider);
                if (ve != null) {
                    String msg = "JCE cannot authenticate the provider " + provider.getName();
                    throw new SecurityException(msg, ve);
                }
                providerChecked = true;
            }
            if (tr.supportsMode(s) == 0) continue;
            if (tr.supportsPadding(s) == 0) {
                paddingError = tr.pad;
                continue;
            }
            try {
                CipherSpi spi = (CipherSpi)s.newInstance(null);
                tr.setModePadding(spi);
                Cipher cipher = new Cipher(spi, transformation);
                cipher.provider = s.getProvider();
                cipher.initCryptoPermission();
                return cipher;
            }
            catch (Exception e) {
                failure = e;
            }
        }
        if (failure instanceof NoSuchPaddingException) {
            throw (NoSuchPaddingException)failure;
        }
        if (paddingError != null) {
            throw new NoSuchPaddingException("Padding not supported: " + paddingError);
        }
        throw new NoSuchAlgorithmException("No such algorithm: " + transformation, failure);
    }

    private void initCryptoPermission() throws NoSuchAlgorithmException {
        if (!JceSecurity.isRestricted()) {
            this.cryptoPerm = CryptoAllPermission.INSTANCE;
            this.exmech = null;
            return;
        }
        this.cryptoPerm = Cipher.getConfiguredPermission(this.transformation);
        String exmechName = this.cryptoPerm.getExemptionMechanism();
        if (exmechName != null) {
            this.exmech = ExemptionMechanism.getInstance(exmechName);
        }
    }

    void chooseFirstProvider() {
        if (this.spi != null) {
            return;
        }
        Object object = this.lock;
        synchronized (object) {
            int w;
            if (this.spi != null) {
                return;
            }
            if (debug != null && (w = --warnCount) >= 0) {
                debug.println("Cipher.init() not first method called, disabling delayed provider selection");
                if (w == 0) {
                    debug.println("Further warnings of this type will be suppressed");
                }
                new Exception("Call trace").printStackTrace();
            }
            Exception lastException = null;
            while (this.firstService != null || this.serviceIterator.hasNext()) {
                Transform tr;
                CipherSpi thisSpi;
                Provider.Service s;
                if (this.firstService != null) {
                    s = this.firstService;
                    thisSpi = this.firstSpi;
                    this.firstService = null;
                    this.firstSpi = null;
                } else {
                    s = this.serviceIterator.next();
                    thisSpi = null;
                }
                if (!JceSecurity.canUseProvider(s.getProvider()) || (tr = Cipher.getTransform(s, this.transforms)) == null || tr.supportsModePadding(s) == 0) continue;
                try {
                    if (thisSpi == null) {
                        Object obj = s.newInstance(null);
                        if (!(obj instanceof CipherSpi)) continue;
                        thisSpi = (CipherSpi)obj;
                    }
                    tr.setModePadding(thisSpi);
                    this.initCryptoPermission();
                    this.spi = thisSpi;
                    this.provider = s.getProvider();
                    this.firstService = null;
                    this.serviceIterator = null;
                    this.transforms = null;
                    return;
                }
                catch (Exception e) {
                    lastException = e;
                }
            }
            ProviderException e = new ProviderException("Could not construct CipherSpi instance");
            if (lastException != null) {
                e.initCause(lastException);
            }
            throw e;
        }
    }

    private void implInit(CipherSpi thisSpi, int type, int opmode, Key key, AlgorithmParameterSpec paramSpec, AlgorithmParameters params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        switch (type) {
            case 1: {
                this.checkCryptoPerm(thisSpi, key);
                thisSpi.engineInit(opmode, key, random);
                break;
            }
            case 2: {
                this.checkCryptoPerm(thisSpi, key, paramSpec);
                thisSpi.engineInit(opmode, key, paramSpec, random);
                break;
            }
            case 3: {
                this.checkCryptoPerm(thisSpi, key, params);
                thisSpi.engineInit(opmode, key, params, random);
                break;
            }
            case 4: {
                this.checkCryptoPerm(thisSpi, key);
                thisSpi.engineInit(opmode, key, random);
                break;
            }
            default: {
                throw new AssertionError((Object)("Internal Cipher error: " + type));
            }
        }
    }

    private void chooseProvider(int initType, int opmode, Key key, AlgorithmParameterSpec paramSpec, AlgorithmParameters params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        Object object = this.lock;
        synchronized (object) {
            if (this.spi != null) {
                this.implInit(this.spi, initType, opmode, key, paramSpec, params, random);
                return;
            }
            Exception lastException = null;
            while (this.firstService != null || this.serviceIterator.hasNext()) {
                Transform tr;
                CipherSpi thisSpi;
                Provider.Service s;
                if (this.firstService != null) {
                    s = this.firstService;
                    thisSpi = this.firstSpi;
                    this.firstService = null;
                    this.firstSpi = null;
                } else {
                    s = this.serviceIterator.next();
                    thisSpi = null;
                }
                if (!s.supportsParameter(key) || !JceSecurity.canUseProvider(s.getProvider()) || (tr = Cipher.getTransform(s, this.transforms)) == null || tr.supportsModePadding(s) == 0) continue;
                try {
                    if (thisSpi == null) {
                        thisSpi = (CipherSpi)s.newInstance(null);
                    }
                    tr.setModePadding(thisSpi);
                    this.initCryptoPermission();
                    this.implInit(thisSpi, initType, opmode, key, paramSpec, params, random);
                    this.provider = s.getProvider();
                    this.spi = thisSpi;
                    this.firstService = null;
                    this.serviceIterator = null;
                    this.transforms = null;
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
            if (lastException instanceof InvalidAlgorithmParameterException) {
                throw (InvalidAlgorithmParameterException)lastException;
            }
            if (lastException instanceof RuntimeException) {
                throw (RuntimeException)lastException;
            }
            String kName = key != null ? key.getClass().getName() : "(null)";
            throw new InvalidKeyException("No installed provider supports this key: " + kName, lastException);
        }
    }

    public final Provider getProvider() {
        this.chooseFirstProvider();
        return this.provider;
    }

    public final String getAlgorithm() {
        return this.transformation;
    }

    public final int getBlockSize() {
        this.chooseFirstProvider();
        return this.spi.engineGetBlockSize();
    }

    public final int getOutputSize(int inputLen) {
        if (!this.initialized && !(this instanceof NullCipher)) {
            throw new IllegalStateException("Cipher not initialized");
        }
        if (inputLen < 0) {
            throw new IllegalArgumentException("Input size must be equal to or greater than zero");
        }
        this.chooseFirstProvider();
        return this.spi.engineGetOutputSize(inputLen);
    }

    public final byte[] getIV() {
        this.chooseFirstProvider();
        return this.spi.engineGetIV();
    }

    public final AlgorithmParameters getParameters() {
        this.chooseFirstProvider();
        return this.spi.engineGetParameters();
    }

    public final ExemptionMechanism getExemptionMechanism() {
        this.chooseFirstProvider();
        return this.exmech;
    }

    private void checkCryptoPerm(CipherSpi checkSpi, Key key) throws InvalidKeyException {
        AlgorithmParameterSpec params;
        if (this.cryptoPerm == CryptoAllPermission.INSTANCE) {
            return;
        }
        try {
            params = this.getAlgorithmParameterSpec(checkSpi.engineGetParameters());
        }
        catch (InvalidParameterSpecException ipse) {
            throw new InvalidKeyException("Unsupported default algorithm parameters");
        }
        if (!this.passCryptoPermCheck(checkSpi, key, params)) {
            throw new InvalidKeyException("Illegal key size or default parameters");
        }
    }

    private void checkCryptoPerm(CipherSpi checkSpi, Key key, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (this.cryptoPerm == CryptoAllPermission.INSTANCE) {
            return;
        }
        if (!this.passCryptoPermCheck(checkSpi, key, null)) {
            throw new InvalidKeyException("Illegal key size");
        }
        if (params != null && !this.passCryptoPermCheck(checkSpi, key, params)) {
            throw new InvalidAlgorithmParameterException("Illegal parameters");
        }
    }

    private void checkCryptoPerm(CipherSpi checkSpi, Key key, AlgorithmParameters params) throws InvalidKeyException, InvalidAlgorithmParameterException {
        AlgorithmParameterSpec pSpec;
        if (this.cryptoPerm == CryptoAllPermission.INSTANCE) {
            return;
        }
        try {
            pSpec = this.getAlgorithmParameterSpec(params);
        }
        catch (InvalidParameterSpecException ipse) {
            throw new InvalidAlgorithmParameterException("Failed to retrieve algorithm parameter specification");
        }
        this.checkCryptoPerm(checkSpi, key, pSpec);
    }

    private boolean passCryptoPermCheck(CipherSpi checkSpi, Key key, AlgorithmParameterSpec params) throws InvalidKeyException {
        String em = this.cryptoPerm.getExemptionMechanism();
        int keySize = checkSpi.engineGetKeySize(key);
        int index = this.transformation.indexOf(47);
        String algComponent = index != -1 ? this.transformation.substring(0, index) : this.transformation;
        CryptoPermission checkPerm = new CryptoPermission(algComponent, keySize, params, em);
        if (!this.cryptoPerm.implies(checkPerm)) {
            if (debug != null) {
                debug.println("Crypto Permission check failed");
                debug.println("granted: " + this.cryptoPerm);
                debug.println("requesting: " + checkPerm);
            }
            return false;
        }
        if (this.exmech == null) {
            return true;
        }
        try {
            if (!this.exmech.isCryptoAllowed(key)) {
                if (debug != null) {
                    debug.println(this.exmech.getName() + " isn't enforced");
                }
                return false;
            }
        }
        catch (ExemptionMechanismException eme) {
            if (debug != null) {
                debug.println("Cannot determine whether " + this.exmech.getName() + " has been enforced");
                eme.printStackTrace();
            }
            return false;
        }
        return true;
    }

    private static void checkOpmode(int opmode) {
        if (opmode < 1 || opmode > 4) {
            throw new InvalidParameterException("Invalid operation mode");
        }
    }

    public final void init(int opmode, Key key) throws InvalidKeyException {
        this.init(opmode, key, JCAUtil.getDefSecureRandom());
    }

    public final void init(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
        this.initialized = false;
        Cipher.checkOpmode(opmode);
        if (this.spi != null) {
            this.checkCryptoPerm(this.spi, key);
            this.spi.engineInit(opmode, key, random);
        } else {
            try {
                this.chooseProvider(1, opmode, key, null, null, random);
            }
            catch (InvalidAlgorithmParameterException e) {
                throw new InvalidKeyException(e);
            }
        }
        this.initialized = true;
        this.opmode = opmode;
        if (!skipDebug && pdebug != null) {
            pdebug.println(this.toString());
        }
    }

    public final void init(int opmode, Key key, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.init(opmode, key, params, JCAUtil.getDefSecureRandom());
    }

    public final void init(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.initialized = false;
        Cipher.checkOpmode(opmode);
        if (this.spi != null) {
            this.checkCryptoPerm(this.spi, key, params);
            this.spi.engineInit(opmode, key, params, random);
        } else {
            this.chooseProvider(2, opmode, key, params, null, random);
        }
        this.initialized = true;
        this.opmode = opmode;
        if (!skipDebug && pdebug != null) {
            pdebug.println(this.toString());
        }
    }

    public final void init(int opmode, Key key, AlgorithmParameters params) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.init(opmode, key, params, JCAUtil.getDefSecureRandom());
    }

    public final void init(int opmode, Key key, AlgorithmParameters params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.initialized = false;
        Cipher.checkOpmode(opmode);
        if (this.spi != null) {
            this.checkCryptoPerm(this.spi, key, params);
            this.spi.engineInit(opmode, key, params, random);
        } else {
            this.chooseProvider(3, opmode, key, null, params, random);
        }
        this.initialized = true;
        this.opmode = opmode;
        if (!skipDebug && pdebug != null) {
            pdebug.println(this.toString());
        }
    }

    public final void init(int opmode, Certificate certificate) throws InvalidKeyException {
        this.init(opmode, certificate, JCAUtil.getDefSecureRandom());
    }

    public final void init(int opmode, Certificate certificate, SecureRandom random) throws InvalidKeyException {
        PublicKey publicKey;
        boolean[] keyUsageInfo;
        X509Certificate cert;
        Set<String> critSet;
        this.initialized = false;
        Cipher.checkOpmode(opmode);
        if (certificate instanceof X509Certificate && (critSet = (cert = (X509Certificate)certificate).getCriticalExtensionOIDs()) != null && !critSet.isEmpty() && critSet.contains(KnownOIDs.KeyUsage.value()) && (keyUsageInfo = cert.getKeyUsage()) != null && (opmode == 1 && keyUsageInfo.length > 3 && !keyUsageInfo[3] || opmode == 3 && keyUsageInfo.length > 2 && !keyUsageInfo[2])) {
            throw new InvalidKeyException("Wrong key usage");
        }
        PublicKey publicKey2 = publicKey = certificate == null ? null : certificate.getPublicKey();
        if (this.spi != null) {
            this.checkCryptoPerm(this.spi, publicKey);
            this.spi.engineInit(opmode, publicKey, random);
        } else {
            try {
                this.chooseProvider(4, opmode, publicKey, null, null, random);
            }
            catch (InvalidAlgorithmParameterException e) {
                throw new InvalidKeyException(e);
            }
        }
        this.initialized = true;
        this.opmode = opmode;
        if (!skipDebug && pdebug != null) {
            pdebug.println(this.toString());
        }
    }

    private void checkCipherState() {
        if (!(this instanceof NullCipher)) {
            if (!this.initialized) {
                throw new IllegalStateException("Cipher not initialized");
            }
            if (this.opmode != 1 && this.opmode != 2) {
                throw new IllegalStateException("Cipher not initialized for encryption/decryption");
            }
        }
    }

    public final byte[] update(byte[] input) {
        this.checkCipherState();
        if (input == null) {
            throw new IllegalArgumentException("Null input buffer");
        }
        this.chooseFirstProvider();
        if (input.length == 0) {
            return null;
        }
        return this.spi.engineUpdate(input, 0, input.length);
    }

    public final byte[] update(byte[] input, int inputOffset, int inputLen) {
        this.checkCipherState();
        if (input == null || inputOffset < 0 || inputLen > input.length - inputOffset || inputLen < 0) {
            throw new IllegalArgumentException("Bad arguments");
        }
        this.chooseFirstProvider();
        if (inputLen == 0) {
            return null;
        }
        return this.spi.engineUpdate(input, inputOffset, inputLen);
    }

    public final int update(byte[] input, int inputOffset, int inputLen, byte[] output) throws ShortBufferException {
        this.checkCipherState();
        if (input == null || inputOffset < 0 || inputLen > input.length - inputOffset || inputLen < 0) {
            throw new IllegalArgumentException("Bad arguments");
        }
        this.chooseFirstProvider();
        if (inputLen == 0) {
            return 0;
        }
        return this.spi.engineUpdate(input, inputOffset, inputLen, output, 0);
    }

    public final int update(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException {
        this.checkCipherState();
        if (input == null || inputOffset < 0 || inputLen > input.length - inputOffset || inputLen < 0 || outputOffset < 0) {
            throw new IllegalArgumentException("Bad arguments");
        }
        this.chooseFirstProvider();
        if (inputLen == 0) {
            return 0;
        }
        return this.spi.engineUpdate(input, inputOffset, inputLen, output, outputOffset);
    }

    public final int update(ByteBuffer input, ByteBuffer output) throws ShortBufferException {
        this.checkCipherState();
        if (input == null || output == null) {
            throw new IllegalArgumentException("Buffers must not be null");
        }
        if (input == output) {
            throw new IllegalArgumentException("Input and output buffers must not be the same object, consider using buffer.duplicate()");
        }
        if (output.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        this.chooseFirstProvider();
        return this.spi.engineUpdate(input, output);
    }

    public final byte[] doFinal() throws IllegalBlockSizeException, BadPaddingException {
        this.checkCipherState();
        this.chooseFirstProvider();
        return this.spi.engineDoFinal(null, 0, 0);
    }

    public final int doFinal(byte[] output, int outputOffset) throws IllegalBlockSizeException, ShortBufferException, BadPaddingException {
        this.checkCipherState();
        if (output == null || outputOffset < 0) {
            throw new IllegalArgumentException("Bad arguments");
        }
        this.chooseFirstProvider();
        return this.spi.engineDoFinal(null, 0, 0, output, outputOffset);
    }

    public final byte[] doFinal(byte[] input) throws IllegalBlockSizeException, BadPaddingException {
        this.checkCipherState();
        if (input == null) {
            throw new IllegalArgumentException("Null input buffer");
        }
        this.chooseFirstProvider();
        return this.spi.engineDoFinal(input, 0, input.length);
    }

    public final byte[] doFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException, BadPaddingException {
        this.checkCipherState();
        if (input == null || inputOffset < 0 || inputLen > input.length - inputOffset || inputLen < 0) {
            throw new IllegalArgumentException("Bad arguments");
        }
        this.chooseFirstProvider();
        return this.spi.engineDoFinal(input, inputOffset, inputLen);
    }

    public final int doFinal(byte[] input, int inputOffset, int inputLen, byte[] output) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        this.checkCipherState();
        if (input == null || inputOffset < 0 || inputLen > input.length - inputOffset || inputLen < 0) {
            throw new IllegalArgumentException("Bad arguments");
        }
        this.chooseFirstProvider();
        return this.spi.engineDoFinal(input, inputOffset, inputLen, output, 0);
    }

    public final int doFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        this.checkCipherState();
        if (input == null || inputOffset < 0 || inputLen > input.length - inputOffset || inputLen < 0 || outputOffset < 0) {
            throw new IllegalArgumentException("Bad arguments");
        }
        this.chooseFirstProvider();
        return this.spi.engineDoFinal(input, inputOffset, inputLen, output, outputOffset);
    }

    public final int doFinal(ByteBuffer input, ByteBuffer output) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        this.checkCipherState();
        if (input == null || output == null) {
            throw new IllegalArgumentException("Buffers must not be null");
        }
        if (input == output) {
            throw new IllegalArgumentException("Input and output buffers must not be the same object, consider using buffer.duplicate()");
        }
        if (output.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        this.chooseFirstProvider();
        return this.spi.engineDoFinal(input, output);
    }

    public final byte[] wrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
        if (!(this instanceof NullCipher)) {
            if (!this.initialized) {
                throw new IllegalStateException("Cipher not initialized");
            }
            if (this.opmode != 3) {
                throw new IllegalStateException("Cipher not initialized for wrapping keys");
            }
        }
        this.chooseFirstProvider();
        return this.spi.engineWrap(key);
    }

    public final Key unwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType) throws InvalidKeyException, NoSuchAlgorithmException {
        if (!(this instanceof NullCipher)) {
            if (!this.initialized) {
                throw new IllegalStateException("Cipher not initialized");
            }
            if (this.opmode != 4) {
                throw new IllegalStateException("Cipher not initialized for unwrapping keys");
            }
        }
        if (wrappedKeyType != 3 && wrappedKeyType != 2 && wrappedKeyType != 1) {
            throw new InvalidParameterException("Invalid key type");
        }
        this.chooseFirstProvider();
        return this.spi.engineUnwrap(wrappedKey, wrappedKeyAlgorithm, wrappedKeyType);
    }

    private AlgorithmParameterSpec getAlgorithmParameterSpec(AlgorithmParameters params) throws InvalidParameterSpecException {
        if (params == null) {
            return null;
        }
        String alg = params.getAlgorithm().toUpperCase(Locale.ENGLISH);
        if (alg.equalsIgnoreCase("RC2")) {
            return params.getParameterSpec(RC2ParameterSpec.class);
        }
        if (alg.equalsIgnoreCase("RC5")) {
            return params.getParameterSpec(RC5ParameterSpec.class);
        }
        if (alg.startsWith("PBE")) {
            return params.getParameterSpec(PBEParameterSpec.class);
        }
        if (alg.startsWith("DES")) {
            return params.getParameterSpec(IvParameterSpec.class);
        }
        return null;
    }

    private static CryptoPermission getConfiguredPermission(String transformation) throws NullPointerException, NoSuchAlgorithmException {
        if (transformation == null) {
            throw new NullPointerException();
        }
        String[] parts = Cipher.tokenizeTransformation(transformation);
        return JceSecurityManager.INSTANCE.getCryptoPermission(parts[0]);
    }

    public static final int getMaxAllowedKeyLength(String transformation) throws NoSuchAlgorithmException {
        CryptoPermission cp = Cipher.getConfiguredPermission(transformation);
        return cp.getMaxKeySize();
    }

    public static final AlgorithmParameterSpec getMaxAllowedParameterSpec(String transformation) throws NoSuchAlgorithmException {
        CryptoPermission cp = Cipher.getConfiguredPermission(transformation);
        return cp.getAlgorithmParameterSpec();
    }

    public final void updateAAD(byte[] src) {
        if (src == null) {
            throw new IllegalArgumentException("src buffer is null");
        }
        this.updateAAD(src, 0, src.length);
    }

    public final void updateAAD(byte[] src, int offset, int len) {
        this.checkCipherState();
        if (src == null || offset < 0 || len < 0 || len > src.length - offset) {
            throw new IllegalArgumentException("Bad arguments");
        }
        this.chooseFirstProvider();
        if (len == 0) {
            return;
        }
        this.spi.engineUpdateAAD(src, offset, len);
    }

    public final void updateAAD(ByteBuffer src) {
        this.checkCipherState();
        if (src == null) {
            throw new IllegalArgumentException("src ByteBuffer is null");
        }
        this.chooseFirstProvider();
        if (src.remaining() == 0) {
            return;
        }
        this.spi.engineUpdateAAD(src);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Cipher.").append(this.transformation).append(", mode: ");
        switch (this.opmode) {
            case 0: {
                sb.append("not initialized");
                break;
            }
            case 1: {
                sb.append("encryption");
                break;
            }
            case 2: {
                sb.append("decryption");
                break;
            }
            case 3: {
                sb.append("key wrapping");
                break;
            }
            case 4: {
                sb.append("key unwrapping");
                break;
            }
            default: {
                sb.append("error:").append(this.opmode);
            }
        }
        sb.append(", algorithm from: ").append(this.getProviderName());
        return sb.toString();
    }

    private static class Transform {
        final String transform;
        final String suffix;
        final String mode;
        final String pad;
        private static final ConcurrentMap<String, Pattern> patternCache = new ConcurrentHashMap<String, Pattern>();

        Transform(String alg, String suffix, String mode, String pad) {
            this.transform = alg + suffix;
            this.suffix = suffix.toUpperCase(Locale.ENGLISH);
            this.mode = mode;
            this.pad = pad;
        }

        void setModePadding(CipherSpi spi) throws NoSuchAlgorithmException, NoSuchPaddingException {
            if (this.mode != null) {
                spi.engineSetMode(this.mode);
            }
            if (this.pad != null) {
                spi.engineSetPadding(this.pad);
            }
        }

        int supportsModePadding(Provider.Service s) {
            int smode = this.supportsMode(s);
            if (smode == 0) {
                return smode;
            }
            int spad = this.supportsPadding(s);
            return Math.min(smode, spad);
        }

        int supportsMode(Provider.Service s) {
            return Transform.supports(s, Cipher.ATTR_MODE, this.mode);
        }

        int supportsPadding(Provider.Service s) {
            return Transform.supports(s, Cipher.ATTR_PAD, this.pad);
        }

        private static int supports(Provider.Service s, String attrName, String value) {
            if (value == null) {
                return 2;
            }
            String regexp = s.getAttribute(attrName);
            if (regexp == null) {
                return 1;
            }
            return Transform.matches(regexp, value) ? 2 : 0;
        }

        private static boolean matches(String regexp, String str) {
            Pattern pattern = (Pattern)patternCache.get(regexp);
            if (pattern == null) {
                pattern = Pattern.compile(regexp);
                patternCache.putIfAbsent(regexp, pattern);
            }
            return pattern.matcher(str.toUpperCase(Locale.ENGLISH)).matches();
        }
    }
}

