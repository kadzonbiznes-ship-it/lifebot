/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec;

import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.NamedParameterSpec;
import java.util.HashMap;
import java.util.function.Function;
import sun.security.ec.ParametersMap;
import sun.security.util.KnownOIDs;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;

public class XECParameters {
    static final XECParameters X25519;
    static final XECParameters X448;
    static ParametersMap<XECParameters> namedParams;
    private final ObjectIdentifier oid;
    private final String name;
    private final int bits;
    private final BigInteger p;
    private final int logCofactor;
    private final int a24;
    private final byte basePoint;

    public XECParameters(int bits, BigInteger p, int a24, byte basePoint, int logCofactor, ObjectIdentifier oid, String name) {
        this.bits = bits;
        this.logCofactor = logCofactor;
        this.p = p;
        this.a24 = a24;
        this.basePoint = basePoint;
        this.oid = oid;
        this.name = name;
    }

    public int getBits() {
        return this.bits;
    }

    public int getBytes() {
        return (this.bits + 7) / 8;
    }

    public int getLogCofactor() {
        return this.logCofactor;
    }

    public BigInteger getP() {
        return this.p;
    }

    public int getA24() {
        return this.a24;
    }

    public byte getBasePoint() {
        return this.basePoint;
    }

    public ObjectIdentifier getOid() {
        return this.oid;
    }

    public String getName() {
        return this.name;
    }

    private static XECParameters addParameters(int bits, BigInteger p, int a24, byte basePoint, int logCofactor, KnownOIDs koid, String name) {
        ObjectIdentifier oid = ObjectIdentifier.of(koid);
        XECParameters params = new XECParameters(bits, p, a24, basePoint, logCofactor, oid, name);
        namedParams.put(name.toLowerCase(), oid, bits, params);
        return params;
    }

    boolean oidEquals(XECParameters other) {
        return this.oid.equals(other.getOid());
    }

    public static <T extends Throwable> XECParameters getBySize(Function<String, T> exception, int size) throws T {
        return namedParams.getBySize(exception, size);
    }

    public static <T extends Throwable> XECParameters get(Function<String, T> exception, AlgorithmId algId) throws T {
        return namedParams.get(exception, algId);
    }

    public static <T extends Throwable> XECParameters get(Function<String, T> exception, AlgorithmParameterSpec params) throws T {
        return namedParams.get(exception, params);
    }

    static {
        namedParams = new ParametersMap();
        BigInteger TWO = BigInteger.valueOf(2L);
        HashMap bySize = new HashMap();
        HashMap byOid = new HashMap();
        HashMap byName = new HashMap();
        BigInteger p2 = TWO.pow(255).subtract(BigInteger.valueOf(19L));
        X25519 = XECParameters.addParameters(255, p2, 121665, (byte)9, 3, KnownOIDs.X25519, NamedParameterSpec.X25519.getName());
        BigInteger p4 = TWO.pow(448).subtract(TWO.pow(224)).subtract(BigInteger.ONE);
        X448 = XECParameters.addParameters(448, p4, 39081, (byte)5, 2, KnownOIDs.X448, NamedParameterSpec.X448.getName());
        namedParams.fix();
    }
}

