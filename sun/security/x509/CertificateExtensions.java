/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import sun.security.util.Debug;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.HexDumpEncoder;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.Extension;
import sun.security.x509.OIDMap;
import sun.security.x509.UnparseableExtension;

public class CertificateExtensions
implements DerEncoder {
    public static final String NAME = "extensions";
    private static final Debug debug = Debug.getInstance("x509");
    private final Map<String, Extension> map = Collections.synchronizedMap(new TreeMap());
    private boolean unsupportedCritExt = false;
    private Map<String, Extension> unparseableExtensions;
    private static final Class<?>[] PARAMS = new Class[]{Boolean.class, Object.class};

    public CertificateExtensions() {
    }

    public CertificateExtensions(DerInputStream in) throws IOException {
        this.init(in);
    }

    private void init(DerInputStream in) throws IOException {
        DerValue[] exts = in.getSequence(5);
        for (int i = 0; i < exts.length; ++i) {
            Extension ext = new Extension(exts[i]);
            this.parseExtension(ext);
        }
    }

    private void parseExtension(Extension ext) throws IOException {
        try {
            Class<?> extClass = OIDMap.getClass(ext.getExtensionId());
            if (extClass == null) {
                if (ext.isCritical()) {
                    this.unsupportedCritExt = true;
                }
                if (this.map.put(ext.getExtensionId().toString(), ext) == null) {
                    return;
                }
                throw new IOException("Duplicate extensions not allowed");
            }
            Constructor<?> cons = extClass.getConstructor(PARAMS);
            Object[] passed = new Object[]{ext.isCritical(), ext.getExtensionValue()};
            Extension certExt = (Extension)cons.newInstance(passed);
            if (this.map.put(certExt.getName(), certExt) != null) {
                throw new IOException("Duplicate extensions not allowed");
            }
        }
        catch (InvocationTargetException invk) {
            Throwable e = invk.getCause();
            if (!ext.isCritical()) {
                if (this.unparseableExtensions == null) {
                    this.unparseableExtensions = new TreeMap<String, Extension>();
                }
                this.unparseableExtensions.put(ext.getExtensionId().toString(), new UnparseableExtension(ext, e));
                if (debug != null) {
                    debug.println("Debug info only. Error parsing extension: " + ext);
                    e.printStackTrace();
                    HexDumpEncoder h = new HexDumpEncoder();
                    System.err.println(h.encodeBuffer(ext.getExtensionValue()));
                }
                return;
            }
            if (e instanceof IOException) {
                throw (IOException)e;
            }
            throw new IOException(e);
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void encode(DerOutputStream out) {
        this.encode(out, false);
    }

    public void encode(DerOutputStream out, boolean isCertReq) {
        DerOutputStream extOut = new DerOutputStream();
        for (Extension ext : this.map.values()) {
            ext.encode(extOut);
        }
        if (!isCertReq) {
            DerOutputStream seq = new DerOutputStream();
            seq.write((byte)48, extOut);
            out.write(DerValue.createTag((byte)-128, true, (byte)3), seq);
        } else {
            out.write((byte)48, extOut);
        }
    }

    public void setExtension(String name, Extension ext) {
        this.map.put(name, ext);
    }

    public Extension getExtension(String alias) {
        String name;
        if (alias.startsWith("x509")) {
            int index = alias.lastIndexOf(46);
            name = alias.substring(index + 1);
        } else {
            name = alias;
        }
        return this.map.get(name);
    }

    public void delete(String name) throws IOException {
        Extension obj = this.map.get(name);
        if (obj == null) {
            throw new IOException("No extension found with name " + name);
        }
        this.map.remove(name);
    }

    public String getNameByOid(ObjectIdentifier oid) {
        for (String name : this.map.keySet()) {
            if (!this.map.get(name).getExtensionId().equals(oid)) continue;
            return name;
        }
        return null;
    }

    public Collection<Extension> getAllExtensions() {
        return this.map.values();
    }

    public Map<String, Extension> getUnparseableExtensions() {
        return this.unparseableExtensions == null ? Collections.emptyMap() : this.unparseableExtensions;
    }

    public boolean hasUnsupportedCriticalExtension() {
        return this.unsupportedCritExt;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CertificateExtensions)) {
            return false;
        }
        CertificateExtensions otherCX = (CertificateExtensions)other;
        Collection<Extension> otherX = otherCX.getAllExtensions();
        if (otherX.size() != this.map.size()) {
            return false;
        }
        for (Extension otherExt : otherX) {
            String key = otherExt.getName();
            Extension thisExt = this.map.get(key);
            if (thisExt == null) {
                return false;
            }
            if (thisExt.equals(otherExt)) continue;
            return false;
        }
        return this.getUnparseableExtensions().equals(otherCX.getUnparseableExtensions());
    }

    public int hashCode() {
        return this.map.hashCode() + this.getUnparseableExtensions().hashCode();
    }

    public String toString() {
        return this.map.toString();
    }
}

