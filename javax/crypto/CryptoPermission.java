/*
 * Decompiled with CFR 0.152.
 */
package javax.crypto;

import java.security.Permission;
import java.security.PermissionCollection;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.CryptoPermissionCollection;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.RC2ParameterSpec;
import javax.crypto.spec.RC5ParameterSpec;

class CryptoPermission
extends Permission {
    private static final long serialVersionUID = 8987399626114087514L;
    private final String alg;
    private int maxKeySize = Integer.MAX_VALUE;
    private String exemptionMechanism = null;
    private AlgorithmParameterSpec algParamSpec = null;
    private boolean checkParam = false;
    static final String ALG_NAME_WILDCARD = "*";

    CryptoPermission(String alg) {
        super(null);
        this.alg = alg;
    }

    CryptoPermission(String alg, int maxKeySize) {
        super(null);
        this.alg = alg;
        this.maxKeySize = maxKeySize;
    }

    CryptoPermission(String alg, int maxKeySize, AlgorithmParameterSpec algParamSpec) {
        super(null);
        this.alg = alg;
        this.maxKeySize = maxKeySize;
        this.checkParam = true;
        this.algParamSpec = algParamSpec;
    }

    CryptoPermission(String alg, String exemptionMechanism) {
        super(null);
        this.alg = alg;
        this.exemptionMechanism = exemptionMechanism;
    }

    CryptoPermission(String alg, int maxKeySize, String exemptionMechanism) {
        super(null);
        this.alg = alg;
        this.exemptionMechanism = exemptionMechanism;
        this.maxKeySize = maxKeySize;
    }

    CryptoPermission(String alg, int maxKeySize, AlgorithmParameterSpec algParamSpec, String exemptionMechanism) {
        super(null);
        this.alg = alg;
        this.exemptionMechanism = exemptionMechanism;
        this.maxKeySize = maxKeySize;
        this.checkParam = true;
        this.algParamSpec = algParamSpec;
    }

    @Override
    public boolean implies(Permission p) {
        if (!(p instanceof CryptoPermission)) {
            return false;
        }
        CryptoPermission cp = (CryptoPermission)p;
        if (!this.alg.equalsIgnoreCase(cp.alg) && !this.alg.equalsIgnoreCase(ALG_NAME_WILDCARD)) {
            return false;
        }
        if (cp.maxKeySize <= this.maxKeySize) {
            if (!this.impliesParameterSpec(cp.checkParam, cp.algParamSpec)) {
                return false;
            }
            return this.impliesExemptionMechanism(cp.exemptionMechanism);
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CryptoPermission)) {
            return false;
        }
        CryptoPermission that = (CryptoPermission)obj;
        if (!this.alg.equalsIgnoreCase(that.alg) || this.maxKeySize != that.maxKeySize) {
            return false;
        }
        if (this.checkParam != that.checkParam) {
            return false;
        }
        return this.equalObjects(this.exemptionMechanism, that.exemptionMechanism) && this.equalObjects(this.algParamSpec, that.algParamSpec);
    }

    @Override
    public int hashCode() {
        int retval = this.alg.hashCode();
        retval ^= this.maxKeySize;
        if (this.exemptionMechanism != null) {
            retval ^= this.exemptionMechanism.hashCode();
        }
        if (this.checkParam) {
            retval ^= 0x64;
        }
        if (this.algParamSpec != null) {
            retval ^= this.algParamSpec.hashCode();
        }
        return retval;
    }

    @Override
    public String getActions() {
        return null;
    }

    @Override
    public PermissionCollection newPermissionCollection() {
        return new CryptoPermissionCollection();
    }

    final String getAlgorithm() {
        return this.alg;
    }

    final String getExemptionMechanism() {
        return this.exemptionMechanism;
    }

    final int getMaxKeySize() {
        return this.maxKeySize;
    }

    final boolean getCheckParam() {
        return this.checkParam;
    }

    final AlgorithmParameterSpec getAlgorithmParameterSpec() {
        return this.algParamSpec;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(100);
        buf.append("(CryptoPermission " + this.alg + " " + this.maxKeySize);
        if (this.algParamSpec != null) {
            if (this.algParamSpec instanceof RC2ParameterSpec) {
                buf.append(" , effective " + ((RC2ParameterSpec)this.algParamSpec).getEffectiveKeyBits());
            } else if (this.algParamSpec instanceof RC5ParameterSpec) {
                buf.append(" , rounds " + ((RC5ParameterSpec)this.algParamSpec).getRounds());
            }
        }
        if (this.exemptionMechanism != null) {
            buf.append(" " + this.exemptionMechanism);
        }
        buf.append(")");
        return buf.toString();
    }

    private boolean impliesExemptionMechanism(String exemptionMechanism) {
        if (this.exemptionMechanism == null) {
            return true;
        }
        if (exemptionMechanism == null) {
            return false;
        }
        return this.exemptionMechanism.equals(exemptionMechanism);
    }

    private boolean impliesParameterSpec(boolean checkParam, AlgorithmParameterSpec algParamSpec) {
        if (this.checkParam && checkParam) {
            if (algParamSpec == null) {
                return true;
            }
            if (this.algParamSpec == null) {
                return false;
            }
            if (this.algParamSpec.getClass() != algParamSpec.getClass()) {
                return false;
            }
            if (algParamSpec instanceof RC2ParameterSpec && ((RC2ParameterSpec)algParamSpec).getEffectiveKeyBits() <= ((RC2ParameterSpec)this.algParamSpec).getEffectiveKeyBits()) {
                return true;
            }
            if (algParamSpec instanceof RC5ParameterSpec && ((RC5ParameterSpec)algParamSpec).getRounds() <= ((RC5ParameterSpec)this.algParamSpec).getRounds()) {
                return true;
            }
            if (algParamSpec instanceof PBEParameterSpec && ((PBEParameterSpec)algParamSpec).getIterationCount() <= ((PBEParameterSpec)this.algParamSpec).getIterationCount()) {
                return true;
            }
            return this.algParamSpec.equals(algParamSpec);
        }
        return !this.checkParam;
    }

    private boolean equalObjects(Object obj1, Object obj2) {
        if (obj1 == null) {
            return obj2 == null;
        }
        return obj1.equals(obj2);
    }
}

