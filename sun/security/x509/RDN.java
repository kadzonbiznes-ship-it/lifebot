/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AVA;
import sun.security.x509.AVAComparator;
import sun.security.x509.X500Name;

public class RDN {
    final AVA[] assertion;
    private volatile List<AVA> avaList;
    private volatile String canonicalString;

    public RDN(String name) throws IOException {
        this(name, Collections.emptyMap());
    }

    public RDN(String name, Map<String, String> keywordMap) throws IOException {
        AVA ava;
        String avaString;
        int quoteCount = 0;
        int searchOffset = 0;
        int avaOffset = 0;
        ArrayList<AVA> avaVec = new ArrayList<AVA>(3);
        int nextPlus = name.indexOf(43);
        while (nextPlus >= 0) {
            if (nextPlus > 0 && name.charAt(nextPlus - 1) != '\\' && (quoteCount += X500Name.countQuotes(name, searchOffset, nextPlus)) != 1) {
                avaString = name.substring(avaOffset, nextPlus);
                if (avaString.isEmpty()) {
                    throw new IOException("empty AVA in RDN \"" + name + "\"");
                }
                ava = new AVA((Reader)new StringReader(avaString), keywordMap);
                avaVec.add(ava);
                avaOffset = nextPlus + 1;
                quoteCount = 0;
            }
            searchOffset = nextPlus + 1;
            nextPlus = name.indexOf(43, searchOffset);
        }
        avaString = name.substring(avaOffset);
        if (avaString.isEmpty()) {
            throw new IOException("empty AVA in RDN \"" + name + "\"");
        }
        ava = new AVA((Reader)new StringReader(avaString), keywordMap);
        avaVec.add(ava);
        this.assertion = avaVec.toArray(new AVA[0]);
    }

    RDN(String name, String format) throws IOException {
        this(name, format, Collections.emptyMap());
    }

    RDN(String name, String format, Map<String, String> keywordMap) throws IOException {
        AVA ava;
        String avaString;
        if (!format.equalsIgnoreCase("RFC2253")) {
            throw new IOException("Unsupported format " + format);
        }
        int avaOffset = 0;
        ArrayList<AVA> avaVec = new ArrayList<AVA>(3);
        int nextPlus = name.indexOf(43);
        while (nextPlus >= 0) {
            if (nextPlus > 0 && name.charAt(nextPlus - 1) != '\\') {
                avaString = name.substring(avaOffset, nextPlus);
                if (avaString.isEmpty()) {
                    throw new IOException("empty AVA in RDN \"" + name + "\"");
                }
                ava = new AVA(new StringReader(avaString), 3, keywordMap);
                avaVec.add(ava);
                avaOffset = nextPlus + 1;
            }
            int searchOffset = nextPlus + 1;
            nextPlus = name.indexOf(43, searchOffset);
        }
        avaString = name.substring(avaOffset);
        if (avaString.isEmpty()) {
            throw new IOException("empty AVA in RDN \"" + name + "\"");
        }
        ava = new AVA(new StringReader(avaString), 3, keywordMap);
        avaVec.add(ava);
        this.assertion = avaVec.toArray(new AVA[0]);
    }

    RDN(DerValue rdn) throws IOException {
        if (rdn.tag != 49) {
            throw new IOException("X500 RDN");
        }
        DerInputStream dis = new DerInputStream(rdn.toByteArray());
        DerValue[] avaset = dis.getSet(5);
        this.assertion = new AVA[avaset.length];
        for (int i = 0; i < avaset.length; ++i) {
            this.assertion[i] = new AVA(avaset[i]);
        }
    }

    RDN(int i) {
        this.assertion = new AVA[i];
    }

    public RDN(AVA ava) {
        if (ava == null) {
            throw new NullPointerException();
        }
        this.assertion = new AVA[]{ava};
    }

    public RDN(AVA[] avas) {
        this.assertion = (AVA[])avas.clone();
        for (int i = 0; i < this.assertion.length; ++i) {
            if (this.assertion[i] != null) continue;
            throw new NullPointerException();
        }
    }

    public List<AVA> avas() {
        List<AVA> list = this.avaList;
        if (list == null) {
            this.avaList = list = Collections.unmodifiableList(Arrays.asList(this.assertion));
        }
        return list;
    }

    public int size() {
        return this.assertion.length;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RDN)) {
            return false;
        }
        RDN other = (RDN)obj;
        if (this.assertion.length != other.assertion.length) {
            return false;
        }
        String thisCanon = this.toRFC2253String(true);
        String otherCanon = other.toRFC2253String(true);
        return thisCanon.equals(otherCanon);
    }

    public int hashCode() {
        return this.toRFC2253String(true).hashCode();
    }

    DerValue findAttribute(ObjectIdentifier oid) {
        for (int i = 0; i < this.assertion.length; ++i) {
            if (!this.assertion[i].oid.equals(oid)) continue;
            return this.assertion[i].value;
        }
        return null;
    }

    void encode(DerOutputStream out) {
        out.putOrderedSetOf((byte)49, this.assertion);
    }

    public String toString() {
        if (this.assertion.length == 1) {
            return this.assertion[0].toString();
        }
        StringJoiner sj = new StringJoiner(" + ");
        for (int i = 0; i < this.assertion.length; ++i) {
            sj.add(this.assertion[i].toString());
        }
        return sj.toString();
    }

    public String toRFC1779String() {
        return this.toRFC1779String(Collections.emptyMap());
    }

    public String toRFC1779String(Map<String, String> oidMap) {
        if (this.assertion.length == 1) {
            return this.assertion[0].toRFC1779String(oidMap);
        }
        StringJoiner sj = new StringJoiner(" + ");
        for (int i = 0; i < this.assertion.length; ++i) {
            sj.add(this.assertion[i].toRFC1779String(oidMap));
        }
        return sj.toString();
    }

    public String toRFC2253String() {
        return this.toRFC2253StringInternal(false, Collections.emptyMap());
    }

    public String toRFC2253String(Map<String, String> oidMap) {
        return this.toRFC2253StringInternal(false, oidMap);
    }

    public String toRFC2253String(boolean canonical) {
        if (!canonical) {
            return this.toRFC2253StringInternal(false, Collections.emptyMap());
        }
        String c = this.canonicalString;
        if (c == null) {
            this.canonicalString = c = this.toRFC2253StringInternal(true, Collections.emptyMap());
        }
        return c;
    }

    private String toRFC2253StringInternal(boolean canonical, Map<String, String> oidMap) {
        if (this.assertion.length == 1) {
            return canonical ? this.assertion[0].toRFC2253CanonicalString() : this.assertion[0].toRFC2253String(oidMap);
        }
        AVA[] toOutput = this.assertion;
        if (canonical) {
            toOutput = (AVA[])this.assertion.clone();
            Arrays.sort(toOutput, AVAComparator.getInstance());
        }
        StringJoiner sj = new StringJoiner("+");
        for (AVA ava : toOutput) {
            sj.add(canonical ? ava.toRFC2253CanonicalString() : ava.toRFC2253String(oidMap));
        }
        return sj.toString();
    }
}

