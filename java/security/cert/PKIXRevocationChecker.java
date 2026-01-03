/*
 * Decompiled with CFR 0.152.
 */
package java.security.cert;

import java.net.URI;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Extension;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class PKIXRevocationChecker
extends PKIXCertPathChecker {
    private URI ocspResponder;
    private X509Certificate ocspResponderCert;
    private List<Extension> ocspExtensions = Collections.emptyList();
    private Map<X509Certificate, byte[]> ocspResponses = Collections.emptyMap();
    private Set<Option> options = Collections.emptySet();

    protected PKIXRevocationChecker() {
    }

    public void setOcspResponder(URI uri) {
        this.ocspResponder = uri;
    }

    public URI getOcspResponder() {
        return this.ocspResponder;
    }

    public void setOcspResponderCert(X509Certificate cert) {
        this.ocspResponderCert = cert;
    }

    public X509Certificate getOcspResponderCert() {
        return this.ocspResponderCert;
    }

    public void setOcspExtensions(List<Extension> extensions) {
        this.ocspExtensions = extensions == null ? Collections.emptyList() : new ArrayList<Extension>(extensions);
    }

    public List<Extension> getOcspExtensions() {
        return Collections.unmodifiableList(this.ocspExtensions);
    }

    public void setOcspResponses(Map<X509Certificate, byte[]> responses) {
        if (responses == null) {
            this.ocspResponses = Collections.emptyMap();
        } else {
            HashMap<X509Certificate, byte[]> copy = HashMap.newHashMap(responses.size());
            for (Map.Entry<X509Certificate, byte[]> e : responses.entrySet()) {
                copy.put(e.getKey(), (byte[])e.getValue().clone());
            }
            this.ocspResponses = copy;
        }
    }

    public Map<X509Certificate, byte[]> getOcspResponses() {
        HashMap<X509Certificate, byte[]> copy = HashMap.newHashMap(this.ocspResponses.size());
        for (Map.Entry<X509Certificate, byte[]> e : this.ocspResponses.entrySet()) {
            copy.put(e.getKey(), (byte[])e.getValue().clone());
        }
        return copy;
    }

    public void setOptions(Set<Option> options) {
        this.options = options == null ? Collections.emptySet() : new HashSet<Option>(options);
    }

    public Set<Option> getOptions() {
        return Collections.unmodifiableSet(this.options);
    }

    public abstract List<CertPathValidatorException> getSoftFailExceptions();

    @Override
    public PKIXRevocationChecker clone() {
        PKIXRevocationChecker copy = (PKIXRevocationChecker)super.clone();
        copy.ocspExtensions = new ArrayList<Extension>(this.ocspExtensions);
        copy.ocspResponses = new HashMap<X509Certificate, byte[]>(this.ocspResponses);
        for (Map.Entry<X509Certificate, byte[]> entry : copy.ocspResponses.entrySet()) {
            byte[] encoded = entry.getValue();
            entry.setValue((byte[])encoded.clone());
        }
        copy.options = new HashSet<Option>(this.options);
        return copy;
    }

    public static enum Option {
        ONLY_END_ENTITY,
        PREFER_CRLS,
        NO_FALLBACK,
        SOFT_FAIL;

    }
}

