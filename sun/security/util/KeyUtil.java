/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.SecureRandom;
import java.security.interfaces.DSAKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.ECKey;
import java.security.interfaces.EdECKey;
import java.security.interfaces.RSAKey;
import java.security.interfaces.XECKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import sun.security.jca.JCAUtil;
import sun.security.util.ECKeySizeParameterSpec;
import sun.security.util.Length;
import sun.security.util.NamedCurve;

public final class KeyUtil {
    public static int getKeySize(Key key) {
        int size = -1;
        if (key instanceof Length) {
            try {
                Length ruler = (Length)((Object)key);
                size = ruler.length();
            }
            catch (UnsupportedOperationException ruler) {
                // empty catch block
            }
            if (size >= 0) {
                return size;
            }
        }
        if (key instanceof SecretKey) {
            byte[] encoded;
            SecretKey sk = (SecretKey)key;
            String format = sk.getFormat();
            if ("RAW".equals(format) && (encoded = sk.getEncoded()) != null) {
                size = encoded.length * 8;
                Arrays.fill(encoded, (byte)0);
            }
        } else if (key instanceof RSAKey) {
            RSAKey pubk = (RSAKey)((Object)key);
            size = pubk.getModulus().bitLength();
        } else if (key instanceof ECKey) {
            ECKey pubk = (ECKey)((Object)key);
            size = pubk.getParams().getOrder().bitLength();
        } else if (key instanceof DSAKey) {
            DSAKey pubk = (DSAKey)((Object)key);
            DSAParams params = pubk.getParams();
            size = params != null ? params.getP().bitLength() : -1;
        } else if (key instanceof DHKey) {
            DHKey pubk = (DHKey)((Object)key);
            size = pubk.getParams().getP().bitLength();
        } else if (key instanceof XECKey) {
            String name;
            XECKey pubk = (XECKey)((Object)key);
            AlgorithmParameterSpec params = pubk.getParams();
            size = params instanceof NamedParameterSpec ? ((name = ((NamedParameterSpec)params).getName()).equalsIgnoreCase(NamedParameterSpec.X25519.getName()) ? 255 : (name.equalsIgnoreCase(NamedParameterSpec.X448.getName()) ? 448 : -1)) : -1;
        } else if (key instanceof EdECKey) {
            String nc = ((EdECKey)((Object)key)).getParams().getName();
            size = nc.equalsIgnoreCase(NamedParameterSpec.ED25519.getName()) ? 255 : (nc.equalsIgnoreCase(NamedParameterSpec.ED448.getName()) ? 448 : -1);
        }
        return size;
    }

    public static final int getKeySize(AlgorithmParameters parameters) {
        switch (parameters.getAlgorithm()) {
            case "EC": {
                AlgorithmParameterSpec ps2;
                if (parameters.getProvider().getName().equals("SunEC")) {
                    try {
                        ps2 = parameters.getParameterSpec(ECKeySizeParameterSpec.class);
                        if (ps2 != null) {
                            return ((ECKeySizeParameterSpec)ps2).getKeySize();
                        }
                    }
                    catch (InvalidParameterSpecException ps2) {
                        // empty catch block
                    }
                }
                try {
                    ps2 = parameters.getParameterSpec(ECParameterSpec.class);
                    if (ps2 != null) {
                        return ((ECParameterSpec)ps2).getOrder().bitLength();
                    }
                }
                catch (InvalidParameterSpecException ps3) {}
                break;
            }
            case "DiffieHellman": {
                try {
                    DHParameterSpec ps = parameters.getParameterSpec(DHParameterSpec.class);
                    if (ps != null) {
                        return ps.getP().bitLength();
                    }
                    break;
                }
                catch (InvalidParameterSpecException invalidParameterSpecException) {
                    // empty catch block
                }
            }
        }
        return -1;
    }

    public static final String fullDisplayAlgName(Key key) {
        String result = key.getAlgorithm();
        if (key instanceof ECKey) {
            ECParameterSpec paramSpec = ((ECKey)((Object)key)).getParams();
            if (paramSpec instanceof NamedCurve) {
                NamedCurve nc = (NamedCurve)paramSpec;
                result = result + " (" + nc.getNameAndAliases()[0] + ")";
            }
        } else if (key instanceof EdECKey) {
            result = ((EdECKey)((Object)key)).getParams().getName();
        }
        return result;
    }

    public static final void validate(Key key) throws InvalidKeyException {
        if (key == null) {
            throw new NullPointerException("The key to be validated cannot be null");
        }
        if (key instanceof DHPublicKey) {
            KeyUtil.validateDHPublicKey((DHPublicKey)key);
        }
    }

    public static final void validate(KeySpec keySpec) throws InvalidKeyException {
        if (keySpec == null) {
            throw new NullPointerException("The key spec to be validated cannot be null");
        }
        if (keySpec instanceof DHPublicKeySpec) {
            KeyUtil.validateDHPublicKey((DHPublicKeySpec)keySpec);
        }
    }

    public static final boolean isOracleJCEProvider(String providerName) {
        return providerName != null && (providerName.equals("SunJCE") || providerName.equals("SunMSCAPI") || providerName.startsWith("SunPKCS11"));
    }

    public static byte[] checkTlsPreMasterSecretKey(int clientVersion, int serverVersion, SecureRandom random, byte[] encoded, boolean failure) {
        if (random == null) {
            random = JCAUtil.getSecureRandom();
        }
        byte[] replacer = new byte[48];
        random.nextBytes(replacer);
        byte[] tmp = failure ? replacer : encoded;
        encoded = tmp == null ? replacer : tmp;
        tmp = encoded.length != 48 ? replacer : encoded;
        int encodedVersion = (tmp[0] & 0xFF) << 8 | tmp[1] & 0xFF;
        int check1 = clientVersion - encodedVersion | encodedVersion - clientVersion;
        int check2 = 769 - clientVersion;
        int check3 = serverVersion - encodedVersion | encodedVersion - serverVersion;
        check1 = (check1 & (check2 | check3)) >> 24;
        check2 = ~check1;
        for (int i = 0; i < 48; ++i) {
            tmp[i] = (byte)(tmp[i] & check2 | replacer[i] & check1);
        }
        return tmp;
    }

    private static void validateDHPublicKey(DHPublicKey publicKey) throws InvalidKeyException {
        DHParameterSpec paramSpec = publicKey.getParams();
        BigInteger p = paramSpec.getP();
        BigInteger g = paramSpec.getG();
        BigInteger y = publicKey.getY();
        KeyUtil.validateDHPublicKey(p, g, y);
    }

    private static void validateDHPublicKey(DHPublicKeySpec publicKeySpec) throws InvalidKeyException {
        KeyUtil.validateDHPublicKey(publicKeySpec.getP(), publicKeySpec.getG(), publicKeySpec.getY());
    }

    private static void validateDHPublicKey(BigInteger p, BigInteger g, BigInteger y) throws InvalidKeyException {
        BigInteger leftOpen = BigInteger.ONE;
        BigInteger rightOpen = p.subtract(BigInteger.ONE);
        if (y.compareTo(leftOpen) <= 0) {
            throw new InvalidKeyException("Diffie-Hellman public key is too small");
        }
        if (y.compareTo(rightOpen) >= 0) {
            throw new InvalidKeyException("Diffie-Hellman public key is too large");
        }
        BigInteger r = p.remainder(y);
        if (r.equals(BigInteger.ZERO)) {
            throw new InvalidKeyException("Invalid Diffie-Hellman parameters");
        }
    }

    public static byte[] trimZeroes(byte[] b) {
        int i;
        for (i = 0; i < b.length - 1 && b[i] == 0; ++i) {
        }
        if (i == 0) {
            return b;
        }
        byte[] t = new byte[b.length - i];
        System.arraycopy(b, i, t, 0, t.length);
        return t;
    }
}

