/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.security.DrbgParameters;
import java.security.SecureRandomParameters;
import sun.security.provider.EntropySource;

public class MoreDrbgParameters
implements SecureRandomParameters,
Serializable {
    private static final long serialVersionUID = 9L;
    final transient EntropySource es;
    final String mech;
    final String algorithm;
    final boolean usedf;
    final int strength;
    final DrbgParameters.Capability capability;
    byte[] nonce;
    byte[] personalizationString;

    public MoreDrbgParameters(EntropySource es, String mech, String algorithm, byte[] nonce, boolean usedf, DrbgParameters.Instantiation config) {
        this.mech = mech;
        this.algorithm = algorithm;
        this.es = es;
        this.nonce = nonce == null ? null : (byte[])nonce.clone();
        this.usedf = usedf;
        this.strength = config.getStrength();
        this.capability = config.getCapability();
        this.personalizationString = config.getPersonalizationString();
    }

    public String toString() {
        return this.mech + "," + this.algorithm + "," + this.usedf + "," + this.strength + "," + (Object)((Object)this.capability) + "," + this.personalizationString;
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (this.nonce != null) {
            this.nonce = (byte[])this.nonce.clone();
        }
        if (this.personalizationString != null) {
            this.personalizationString = (byte[])this.personalizationString.clone();
        }
        if (this.capability == null) {
            throw new IllegalArgumentException("Input data is corrupted");
        }
    }
}

