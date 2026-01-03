/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.security.CryptoPrimitive;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import sun.security.ssl.SSLAlgorithmConstraints;

enum ProtocolVersion {
    TLS13(772, "TLSv1.3", false),
    TLS12(771, "TLSv1.2", false),
    TLS11(770, "TLSv1.1", false),
    TLS10(769, "TLSv1", false),
    SSL30(768, "SSLv3", false),
    SSL20Hello(2, "SSLv2Hello", false),
    DTLS12(65277, "DTLSv1.2", true),
    DTLS10(65279, "DTLSv1.0", true),
    NONE(-1, "NONE", false);

    final int id;
    final String name;
    final boolean isDTLS;
    final byte major;
    final byte minor;
    final boolean isAvailable;
    static final int LIMIT_MAX_VALUE = 65535;
    static final int LIMIT_MIN_VALUE = 0;
    static final ProtocolVersion[] PROTOCOLS_TO_10;
    static final ProtocolVersion[] PROTOCOLS_TO_11;
    static final ProtocolVersion[] PROTOCOLS_TO_12;
    static final ProtocolVersion[] PROTOCOLS_TO_13;
    static final ProtocolVersion[] PROTOCOLS_OF_NONE;
    static final ProtocolVersion[] PROTOCOLS_OF_30;
    static final ProtocolVersion[] PROTOCOLS_OF_11;
    static final ProtocolVersion[] PROTOCOLS_OF_12;
    static final ProtocolVersion[] PROTOCOLS_OF_13;
    static final ProtocolVersion[] PROTOCOLS_10_11;
    static final ProtocolVersion[] PROTOCOLS_11_12;
    static final ProtocolVersion[] PROTOCOLS_12_13;
    static final ProtocolVersion[] PROTOCOLS_10_12;
    static final ProtocolVersion[] PROTOCOLS_TO_TLS12;
    static final ProtocolVersion[] PROTOCOLS_TO_TLS11;
    static final ProtocolVersion[] PROTOCOLS_TO_TLS10;
    static final ProtocolVersion[] PROTOCOLS_EMPTY;

    private ProtocolVersion(int id, String name, boolean isDTLS) {
        this.id = id;
        this.name = name;
        this.isDTLS = isDTLS;
        this.major = (byte)(id >>> 8 & 0xFF);
        this.minor = (byte)(id & 0xFF);
        this.isAvailable = SSLAlgorithmConstraints.DEFAULT_SSL_ONLY.permits(EnumSet.of(CryptoPrimitive.KEY_AGREEMENT), name, null);
    }

    static ProtocolVersion valueOf(byte major, byte minor) {
        for (ProtocolVersion pv : ProtocolVersion.values()) {
            if (pv.major != major || pv.minor != minor) continue;
            return pv;
        }
        return null;
    }

    static ProtocolVersion valueOf(int id) {
        for (ProtocolVersion pv : ProtocolVersion.values()) {
            if (pv.id != id) continue;
            return pv;
        }
        return null;
    }

    static String nameOf(byte major, byte minor) {
        for (ProtocolVersion pv : ProtocolVersion.values()) {
            if (pv.major != major || pv.minor != minor) continue;
            return pv.name;
        }
        return "(D)TLS-" + major + "." + minor;
    }

    static String nameOf(int id) {
        return ProtocolVersion.nameOf((byte)(id >>> 8 & 0xFF), (byte)(id & 0xFF));
    }

    static ProtocolVersion nameOf(String name) {
        for (ProtocolVersion pv : ProtocolVersion.values()) {
            if (!pv.name.equals(name)) continue;
            return pv;
        }
        return null;
    }

    static boolean isNegotiable(byte major, byte minor, boolean isDTLS, boolean allowSSL20Hello) {
        int v = (major & 0xFF) << 8 | minor & 0xFF;
        if (isDTLS) {
            return v <= ProtocolVersion.DTLS10.id;
        }
        if (v < ProtocolVersion.SSL30.id) {
            return allowSSL20Hello && v == ProtocolVersion.SSL20Hello.id;
        }
        return true;
    }

    static String[] toStringArray(List<ProtocolVersion> protocolVersions) {
        if (protocolVersions != null && !protocolVersions.isEmpty()) {
            String[] protocolNames = new String[protocolVersions.size()];
            int i = 0;
            for (ProtocolVersion pv : protocolVersions) {
                protocolNames[i++] = pv.name;
            }
            return protocolNames;
        }
        return new String[0];
    }

    static String[] toStringArray(int[] protocolVersions) {
        if (protocolVersions != null && protocolVersions.length != 0) {
            String[] protocolNames = new String[protocolVersions.length];
            int i = 0;
            for (int pv : protocolVersions) {
                protocolNames[i++] = ProtocolVersion.nameOf(pv);
            }
            return protocolNames;
        }
        return new String[0];
    }

    static List<ProtocolVersion> namesOf(String[] protocolNames) {
        if (protocolNames == null || protocolNames.length == 0) {
            return Collections.emptyList();
        }
        ArrayList<ProtocolVersion> pvs = new ArrayList<ProtocolVersion>(protocolNames.length);
        for (String pn : protocolNames) {
            ProtocolVersion pv = ProtocolVersion.nameOf(pn);
            if (pv == null) {
                throw new IllegalArgumentException("Unsupported protocol: " + pn);
            }
            pvs.add(pv);
        }
        return Collections.unmodifiableList(pvs);
    }

    static boolean useTLS12PlusSpec(String name) {
        ProtocolVersion pv = ProtocolVersion.nameOf(name);
        if (pv != null && pv != NONE) {
            return pv.isDTLS ? pv.id <= ProtocolVersion.DTLS12.id : pv.id >= ProtocolVersion.TLS12.id;
        }
        return false;
    }

    int compare(ProtocolVersion that) {
        if (this == that) {
            return 0;
        }
        if (this == NONE) {
            return -1;
        }
        if (that == NONE) {
            return 1;
        }
        if (this.isDTLS) {
            return that.id - this.id;
        }
        return this.id - that.id;
    }

    boolean useTLS13PlusSpec() {
        return this.isDTLS ? this.id < ProtocolVersion.DTLS12.id : this.id >= ProtocolVersion.TLS13.id;
    }

    boolean useTLS12PlusSpec() {
        return this.isDTLS ? this.id <= ProtocolVersion.DTLS12.id : this.id >= ProtocolVersion.TLS12.id;
    }

    boolean useTLS11PlusSpec() {
        return this.isDTLS || this.id >= ProtocolVersion.TLS11.id;
    }

    boolean useTLS10PlusSpec() {
        return this.isDTLS || this.id >= ProtocolVersion.TLS10.id;
    }

    static boolean useTLS10PlusSpec(int id, boolean isDTLS) {
        return isDTLS || id >= ProtocolVersion.TLS10.id;
    }

    static boolean useTLS13PlusSpec(int id, boolean isDTLS) {
        return isDTLS ? id < ProtocolVersion.DTLS12.id : id >= ProtocolVersion.TLS13.id;
    }

    static ProtocolVersion selectedFrom(List<ProtocolVersion> listedVersions, int suggestedVersion) {
        ProtocolVersion selectedVersion = NONE;
        for (ProtocolVersion pv : listedVersions) {
            if (pv.id == suggestedVersion) {
                return pv;
            }
            if (pv.isDTLS) {
                if (pv.id <= suggestedVersion || pv.id >= selectedVersion.id) continue;
                selectedVersion = pv;
                continue;
            }
            if (pv.id >= suggestedVersion || pv.id <= selectedVersion.id) continue;
            selectedVersion = pv;
        }
        return selectedVersion;
    }

    static {
        PROTOCOLS_TO_10 = new ProtocolVersion[]{TLS10, SSL30};
        PROTOCOLS_TO_11 = new ProtocolVersion[]{TLS11, TLS10, SSL30, DTLS10};
        PROTOCOLS_TO_12 = new ProtocolVersion[]{TLS12, TLS11, TLS10, SSL30, DTLS12, DTLS10};
        PROTOCOLS_TO_13 = new ProtocolVersion[]{TLS13, TLS12, TLS11, TLS10, SSL30, DTLS12, DTLS10};
        PROTOCOLS_OF_NONE = new ProtocolVersion[]{NONE};
        PROTOCOLS_OF_30 = new ProtocolVersion[]{SSL30};
        PROTOCOLS_OF_11 = new ProtocolVersion[]{TLS11, DTLS10};
        PROTOCOLS_OF_12 = new ProtocolVersion[]{TLS12, DTLS12};
        PROTOCOLS_OF_13 = new ProtocolVersion[]{TLS13};
        PROTOCOLS_10_11 = new ProtocolVersion[]{TLS11, TLS10, DTLS10};
        PROTOCOLS_11_12 = new ProtocolVersion[]{TLS12, TLS11, DTLS12, DTLS10};
        PROTOCOLS_12_13 = new ProtocolVersion[]{TLS13, TLS12, DTLS12};
        PROTOCOLS_10_12 = new ProtocolVersion[]{TLS12, TLS11, TLS10, DTLS12, DTLS10};
        PROTOCOLS_TO_TLS12 = new ProtocolVersion[]{TLS12, TLS11, TLS10, SSL30};
        PROTOCOLS_TO_TLS11 = new ProtocolVersion[]{TLS11, TLS10, SSL30};
        PROTOCOLS_TO_TLS10 = new ProtocolVersion[]{TLS10, SSL30};
        PROTOCOLS_EMPTY = new ProtocolVersion[0];
    }
}

