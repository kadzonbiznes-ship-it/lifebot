/*
 * Decompiled with CFR 0.152.
 */
package java.security.cert;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertSelector;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.util.Set;

public class PKIXBuilderParameters
extends PKIXParameters {
    private int maxPathLength = 5;

    public PKIXBuilderParameters(Set<TrustAnchor> trustAnchors, CertSelector targetConstraints) throws InvalidAlgorithmParameterException {
        super(trustAnchors);
        this.setTargetCertConstraints(targetConstraints);
    }

    public PKIXBuilderParameters(KeyStore keystore, CertSelector targetConstraints) throws KeyStoreException, InvalidAlgorithmParameterException {
        super(keystore);
        this.setTargetCertConstraints(targetConstraints);
    }

    public void setMaxPathLength(int maxPathLength) {
        if (maxPathLength < -1) {
            throw new InvalidParameterException("the maximum path length parameter can not be less than -1");
        }
        this.maxPathLength = maxPathLength;
    }

    public int getMaxPathLength() {
        return this.maxPathLength;
    }

    @Override
    public String toString() {
        return "[\n" + super.toString() + "  Maximum Path Length: " + this.maxPathLength + "\n]\n";
    }
}

