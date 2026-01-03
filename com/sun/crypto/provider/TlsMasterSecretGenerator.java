/*
 * Decompiled with CFR 0.152.
 */
package com.sun.crypto.provider;

import com.sun.crypto.provider.TlsPrfGenerator;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.security.DigestException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.KeyGeneratorSpi;
import javax.crypto.SecretKey;
import sun.security.internal.interfaces.TlsMasterSecret;
import sun.security.internal.spec.TlsMasterSecretParameterSpec;

public final class TlsMasterSecretGenerator
extends KeyGeneratorSpi {
    private static final String MSG = "TlsMasterSecretGenerator must be initialized using a TlsMasterSecretParameterSpec";
    private TlsMasterSecretParameterSpec spec;
    private int protocolVersion;

    @Override
    protected void engineInit(SecureRandom random) {
        throw new InvalidParameterException(MSG);
    }

    @Override
    protected void engineInit(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
        if (!(params instanceof TlsMasterSecretParameterSpec)) {
            throw new InvalidAlgorithmParameterException(MSG);
        }
        this.spec = (TlsMasterSecretParameterSpec)params;
        if (!"RAW".equals(this.spec.getPremasterSecret().getFormat())) {
            throw new InvalidAlgorithmParameterException("Key format must be RAW");
        }
        this.protocolVersion = this.spec.getMajorVersion() << 8 | this.spec.getMinorVersion();
        if (this.protocolVersion < 768 || this.protocolVersion > 771) {
            throw new InvalidAlgorithmParameterException("Only SSL 3.0, TLS 1.0/1.1/1.2 supported");
        }
    }

    @Override
    protected void engineInit(int keysize, SecureRandom random) {
        throw new InvalidParameterException(MSG);
    }

    @Override
    protected SecretKey engineGenerateKey() {
        int premasterMinor;
        int premasterMajor;
        if (this.spec == null) {
            throw new IllegalStateException("TlsMasterSecretGenerator must be initialized");
        }
        SecretKey premasterKey = this.spec.getPremasterSecret();
        byte[] premaster = premasterKey.getEncoded();
        if (premasterKey.getAlgorithm().equals("TlsRsaPremasterSecret")) {
            premasterMajor = premaster[0] & 0xFF;
            premasterMinor = premaster[1] & 0xFF;
        } else {
            premasterMajor = -1;
            premasterMinor = -1;
        }
        try {
            byte[] master;
            if (this.protocolVersion >= 769) {
                byte[] seed;
                byte[] label;
                byte[] extendedMasterSecretSessionHash = this.spec.getExtendedMasterSecretSessionHash();
                if (extendedMasterSecretSessionHash.length != 0) {
                    label = TlsPrfGenerator.LABEL_EXTENDED_MASTER_SECRET;
                    seed = extendedMasterSecretSessionHash;
                } else {
                    byte[] clientRandom = this.spec.getClientRandom();
                    byte[] serverRandom = this.spec.getServerRandom();
                    label = TlsPrfGenerator.LABEL_MASTER_SECRET;
                    seed = TlsPrfGenerator.concat(clientRandom, serverRandom);
                }
                master = this.protocolVersion >= 771 ? TlsPrfGenerator.doTLS12PRF(premaster, label, seed, 48, this.spec.getPRFHashAlg(), this.spec.getPRFHashLength(), this.spec.getPRFBlockSize()) : TlsPrfGenerator.doTLS10PRF(premaster, label, seed, 48);
            } else {
                master = new byte[48];
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                MessageDigest sha = MessageDigest.getInstance("SHA");
                byte[] clientRandom = this.spec.getClientRandom();
                byte[] serverRandom = this.spec.getServerRandom();
                byte[] tmp = new byte[20];
                for (int i = 0; i < 3; ++i) {
                    sha.update(TlsPrfGenerator.SSL3_CONST[i]);
                    sha.update(premaster);
                    sha.update(clientRandom);
                    sha.update(serverRandom);
                    sha.digest(tmp, 0, 20);
                    sha.reset();
                    md5.update(premaster);
                    md5.update(tmp);
                    md5.digest(master, i << 4, 16);
                    md5.reset();
                }
            }
            TlsMasterSecretKey tlsMasterSecretKey = new TlsMasterSecretKey(master, premasterMajor, premasterMinor);
            return tlsMasterSecretKey;
        }
        catch (DigestException | NoSuchAlgorithmException e) {
            throw new ProviderException(e);
        }
        finally {
            if (premaster != null) {
                Arrays.fill(premaster, (byte)0);
            }
        }
    }

    private static final class TlsMasterSecretKey
    implements TlsMasterSecret {
        private static final long serialVersionUID = 1019571680375368880L;
        private byte[] key;
        private final int majorVersion;
        private final int minorVersion;

        TlsMasterSecretKey(byte[] key, int majorVersion, int minorVersion) {
            this.key = key;
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
        }

        @Override
        public int getMajorVersion() {
            return this.majorVersion;
        }

        @Override
        public int getMinorVersion() {
            return this.minorVersion;
        }

        @Override
        public String getAlgorithm() {
            return "TlsMasterSecret";
        }

        @Override
        public String getFormat() {
            return "RAW";
        }

        @Override
        public byte[] getEncoded() {
            return (byte[])this.key.clone();
        }

        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
            if (this.key == null || this.key.length == 0) {
                throw new InvalidObjectException("TlsMasterSecretKey is null");
            }
            this.key = (byte[])this.key.clone();
        }
    }
}

