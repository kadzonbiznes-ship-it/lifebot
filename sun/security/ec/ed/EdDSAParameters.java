/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec.ed;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.EdDSAParameterSpec;
import java.security.spec.NamedParameterSpec;
import java.util.function.Function;
import sun.security.ec.ParametersMap;
import sun.security.ec.ed.Ed25519Operations;
import sun.security.ec.ed.Ed448Operations;
import sun.security.ec.ed.EdECOperations;
import sun.security.provider.SHAKE256;
import sun.security.util.KnownOIDs;
import sun.security.util.ObjectIdentifier;
import sun.security.util.math.ImmutableIntegerModuloP;
import sun.security.util.math.IntegerFieldModuloP;
import sun.security.util.math.intpoly.Curve25519OrderField;
import sun.security.util.math.intpoly.Curve448OrderField;
import sun.security.util.math.intpoly.IntegerPolynomial25519;
import sun.security.util.math.intpoly.IntegerPolynomial448;
import sun.security.x509.AlgorithmId;

public class EdDSAParameters {
    static ParametersMap<EdDSAParameters> namedParams = new ParametersMap();
    private final String name;
    private final ObjectIdentifier oid;
    private final IntegerFieldModuloP field;
    private final IntegerFieldModuloP orderField;
    private final ImmutableIntegerModuloP d;
    private final EdECOperations edOperations;
    private final DigesterFactory digester;
    private final int keyLength;
    private final int bits;
    private final int logCofactor;
    private final Function<EdDSAParameterSpec, byte[]> dom;
    private static final String prefixStr25519 = "SigEd25519 no Ed25519 collisions";
    private static final String prefixStr448 = "SigEd448";

    public EdDSAParameters(String name, ObjectIdentifier oid, IntegerFieldModuloP field, IntegerFieldModuloP orderField, ImmutableIntegerModuloP d, EdECOperations edOps, DigesterFactory digester, Function<EdDSAParameterSpec, byte[]> dom, int keyLength, int bits, int logCofactor) {
        this.oid = oid;
        this.name = name;
        this.field = field;
        this.orderField = orderField;
        this.d = d;
        this.edOperations = edOps;
        this.digester = digester;
        this.keyLength = keyLength;
        this.bits = bits;
        this.logCofactor = logCofactor;
        this.dom = dom;
    }

    public String getName() {
        return this.name;
    }

    public ObjectIdentifier getOid() {
        return this.oid;
    }

    public IntegerFieldModuloP getField() {
        return this.field;
    }

    public IntegerFieldModuloP getOrderField() {
        return this.orderField;
    }

    public ImmutableIntegerModuloP getD() {
        return this.d;
    }

    public EdECOperations getEdOperations() {
        return this.edOperations;
    }

    public int getKeyLength() {
        return this.keyLength;
    }

    public int getBits() {
        return this.bits;
    }

    public int getLogCofactor() {
        return this.logCofactor;
    }

    public Digester createDigester() {
        return this.digester.createDigester();
    }

    public Digester createDigester(int len) {
        return this.digester.createDigester(len);
    }

    public byte[] digest(byte[] ... data) {
        return this.digester.digest(data);
    }

    public byte[] dom(EdDSAParameterSpec sigParams) {
        return this.dom.apply(sigParams);
    }

    static byte[] dom2(EdDSAParameterSpec sigParams) {
        if (!sigParams.isPrehash() && !sigParams.getContext().isPresent()) {
            return new byte[0];
        }
        return EdDSAParameters.domImpl(prefixStr25519, sigParams);
    }

    static byte[] dom4(EdDSAParameterSpec sigParams) {
        return EdDSAParameters.domImpl(prefixStr448, sigParams);
    }

    static byte[] domImpl(String prefixStr, EdDSAParameterSpec sigParams) {
        byte x;
        byte[] prefix = prefixStr.getBytes(StandardCharsets.US_ASCII);
        byte[] context = sigParams.getContext().orElse(new byte[0]);
        int length = prefix.length + 2 + context.length;
        byte[] result = new byte[length];
        System.arraycopy(prefix, 0, result, 0, prefix.length);
        result[prefix.length] = x = (byte)(sigParams.isPrehash() ? 1 : 0);
        result[prefix.length + 1] = (byte)context.length;
        System.arraycopy(context, 0, result, prefix.length + 2, context.length);
        return result;
    }

    public static <T extends Throwable> EdDSAParameters getBySize(Function<String, T> exception, int size) throws T {
        return namedParams.getBySize(exception, size);
    }

    public static <T extends Throwable> EdDSAParameters get(Function<String, T> exception, AlgorithmId algId) throws T {
        return namedParams.get(exception, algId);
    }

    public static <T extends Throwable> EdDSAParameters get(Function<String, T> exception, AlgorithmParameterSpec params) throws T {
        return namedParams.get(exception, params);
    }

    static {
        IntegerPolynomial25519 ed25519Field = IntegerPolynomial25519.ONE;
        Curve25519OrderField ed25519OrderField = Curve25519OrderField.ONE;
        BigInteger biD = new BigInteger("37095705934669439343138083508754565189542113879843219016388785533085940283555");
        ImmutableIntegerModuloP d = ed25519Field.getElement(biD);
        BigInteger baseX = new BigInteger("15112221349535400772501151409588531511454012693041857206046113283949847762202");
        BigInteger baseY = new BigInteger("46316835694926478169428394003475163141307993866256225615783033603165251855960");
        EdECOperations edOps = new Ed25519Operations(d, baseX, baseY);
        String name = NamedParameterSpec.ED25519.getName();
        ObjectIdentifier oid = ObjectIdentifier.of(KnownOIDs.Ed25519);
        int bits = 255;
        DigesterFactory digester = new SHA512DigesterFactory();
        EdDSAParameters params = new EdDSAParameters(name, oid, ed25519Field, ed25519OrderField, d, edOps, digester, EdDSAParameters::dom2, 32, bits, 3);
        namedParams.put(name, oid, bits, params);
        IntegerPolynomial448 ed448Field = IntegerPolynomial448.ONE;
        Curve448OrderField ed448OrderField = Curve448OrderField.ONE;
        biD = ed448Field.getSize().subtract(new BigInteger("39081"));
        d = ed448Field.getElement(biD);
        baseX = new BigInteger("224580040295924300187604334099896036246789641632564134246125461686950415467406032909029192869357953282578032075146446173674602635247710");
        baseY = new BigInteger("298819210078481492676017930443930673437544040154080242095928241372331506189835876003536878655418784733982303233503462500531545062832660");
        edOps = new Ed448Operations(d, baseX, baseY);
        name = NamedParameterSpec.ED448.getName();
        oid = ObjectIdentifier.of(KnownOIDs.Ed448);
        bits = 448;
        digester = new SHAKE256DigesterFactory();
        params = new EdDSAParameters(name, oid, ed448Field, ed448OrderField, d, edOps, digester, EdDSAParameters::dom4, 57, bits, 2);
        namedParams.put(name, oid, bits, params);
        namedParams.fix();
    }

    public static interface DigesterFactory {
        public Digester createDigester();

        default public Digester createDigester(int len) {
            return this.createDigester();
        }

        default public byte[] digest(byte[] ... data) {
            Digester d = this.createDigester();
            for (byte[] curData : data) {
                d.update(curData, 0, curData.length);
            }
            return d.digest();
        }
    }

    public static interface Digester {
        public void update(byte var1);

        public void update(byte[] var1, int var2, int var3);

        public byte[] digest();
    }

    private static class SHA512DigesterFactory
    implements DigesterFactory {
        private SHA512DigesterFactory() {
        }

        @Override
        public Digester createDigester() {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-512");
                return new MessageDigester(md);
            }
            catch (NoSuchAlgorithmException ex) {
                throw new ProviderException(ex);
            }
        }
    }

    private static class SHAKE256DigesterFactory
    implements DigesterFactory {
        private SHAKE256DigesterFactory() {
        }

        @Override
        public Digester createDigester() {
            return new SHAKE256Digester(114);
        }

        @Override
        public Digester createDigester(int len) {
            return new SHAKE256Digester(len);
        }
    }

    private static class SHAKE256Digester
    implements Digester {
        SHAKE256 md;

        SHAKE256Digester(int len) {
            this.md = new SHAKE256(len);
        }

        @Override
        public void update(byte data) {
            this.md.update(data);
        }

        @Override
        public void update(byte[] data, int off, int len) {
            this.md.update(data, off, len);
        }

        @Override
        public byte[] digest() {
            try {
                byte[] byArray = this.md.digest();
                return byArray;
            }
            finally {
                this.md.reset();
            }
        }
    }

    private static class MessageDigester
    implements Digester {
        private final MessageDigest md;

        private MessageDigester(MessageDigest md) {
            this.md = md;
        }

        @Override
        public void update(byte data) {
            this.md.update(data);
        }

        @Override
        public void update(byte[] data, int off, int len) {
            this.md.update(data, off, len);
        }

        @Override
        public byte[] digest() {
            try {
                byte[] byArray = this.md.digest();
                return byArray;
            }
            finally {
                this.md.reset();
            }
        }
    }
}

