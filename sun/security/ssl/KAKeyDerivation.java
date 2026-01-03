/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLHandshakeException;
import sun.security.ssl.CipherSuite;
import sun.security.ssl.HKDF;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.SSLKeyDerivation;
import sun.security.ssl.SSLMasterKeyDerivation;
import sun.security.ssl.SSLSecretDerivation;

public class KAKeyDerivation
implements SSLKeyDerivation {
    private final String algorithmName;
    private final HandshakeContext context;
    private final PrivateKey localPrivateKey;
    private final PublicKey peerPublicKey;

    KAKeyDerivation(String algorithmName, HandshakeContext context, PrivateKey localPrivateKey, PublicKey peerPublicKey) {
        this.algorithmName = algorithmName;
        this.context = context;
        this.localPrivateKey = localPrivateKey;
        this.peerPublicKey = peerPublicKey;
    }

    @Override
    public SecretKey deriveKey(String algorithm, AlgorithmParameterSpec params) throws IOException {
        if (!this.context.negotiatedProtocol.useTLS13PlusSpec()) {
            return this.t12DeriveKey(algorithm, params);
        }
        return this.t13DeriveKey(algorithm, params);
    }

    private SecretKey t12DeriveKey(String algorithm, AlgorithmParameterSpec params) throws IOException {
        try {
            KeyAgreement ka = KeyAgreement.getInstance(this.algorithmName);
            ka.init(this.localPrivateKey);
            ka.doPhase(this.peerPublicKey, true);
            SecretKey preMasterSecret = ka.generateSecret("TlsPremasterSecret");
            SSLMasterKeyDerivation mskd = SSLMasterKeyDerivation.valueOf(this.context.negotiatedProtocol);
            if (mskd == null) {
                throw new SSLHandshakeException("No expected master key derivation for protocol: " + this.context.negotiatedProtocol.name);
            }
            SSLKeyDerivation kd = mskd.createKeyDerivation(this.context, preMasterSecret);
            return kd.deriveKey("MasterSecret", params);
        }
        catch (GeneralSecurityException gse) {
            throw new SSLHandshakeException("Could not generate secret", gse);
        }
    }

    private SecretKey t13DeriveKey(String algorithm, AlgorithmParameterSpec params) throws IOException {
        try {
            KeyAgreement ka = KeyAgreement.getInstance(this.algorithmName);
            ka.init(this.localPrivateKey);
            ka.doPhase(this.peerPublicKey, true);
            SecretKey sharedSecret = ka.generateSecret("TlsPremasterSecret");
            CipherSuite.HashAlg hashAlg = this.context.negotiatedCipherSuite.hashAlg;
            SSLKeyDerivation kd = this.context.handshakeKeyDerivation;
            HKDF hkdf = new HKDF(hashAlg.name);
            if (kd == null) {
                byte[] zeros = new byte[hashAlg.hashLength];
                SecretKeySpec ikm = new SecretKeySpec(zeros, "TlsPreSharedSecret");
                SecretKey earlySecret = hkdf.extract(zeros, (SecretKey)ikm, "TlsEarlySecret");
                kd = new SSLSecretDerivation(this.context, earlySecret);
            }
            SecretKey saltSecret = kd.deriveKey("TlsSaltSecret", null);
            return hkdf.extract(saltSecret, sharedSecret, algorithm);
        }
        catch (GeneralSecurityException gse) {
            throw new SSLHandshakeException("Could not generate secret", gse);
        }
    }
}

