/*
 * Decompiled with CFR 0.152.
 */
package sun.security.pkcs;

import java.io.IOException;
import java.util.Date;
import sun.security.pkcs.SignerInfo;
import sun.security.pkcs.SigningCertificateInfo;
import sun.security.util.Debug;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.HexDumpEncoder;
import sun.security.util.KnownOIDs;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.CertificateExtensions;

public class PKCS9Attribute
implements DerEncoder {
    private static final Debug debug = Debug.getInstance("jar");
    static final ObjectIdentifier[] PKCS9_OIDS = new ObjectIdentifier[19];
    private static final Class<?> BYTE_ARRAY_CLASS;
    public static final ObjectIdentifier EMAIL_ADDRESS_OID;
    public static final ObjectIdentifier UNSTRUCTURED_NAME_OID;
    public static final ObjectIdentifier CONTENT_TYPE_OID;
    public static final ObjectIdentifier MESSAGE_DIGEST_OID;
    public static final ObjectIdentifier SIGNING_TIME_OID;
    public static final ObjectIdentifier COUNTERSIGNATURE_OID;
    public static final ObjectIdentifier CHALLENGE_PASSWORD_OID;
    public static final ObjectIdentifier UNSTRUCTURED_ADDRESS_OID;
    public static final ObjectIdentifier EXTENDED_CERTIFICATE_ATTRIBUTES_OID;
    public static final ObjectIdentifier ISSUER_SERIALNUMBER_OID;
    public static final ObjectIdentifier EXTENSION_REQUEST_OID;
    public static final ObjectIdentifier SIGNING_CERTIFICATE_OID;
    public static final ObjectIdentifier SIGNATURE_TIMESTAMP_TOKEN_OID;
    public static final ObjectIdentifier CMS_ALGORITHM_PROTECTION_OID;
    private static final Byte[][] PKCS9_VALUE_TAGS;
    private static final Class<?>[] VALUE_CLASSES;
    private static final boolean[] SINGLE_VALUED;
    private ObjectIdentifier oid;
    private int index;
    private Object value;

    public PKCS9Attribute(ObjectIdentifier oid, Object value) throws IllegalArgumentException {
        this.init(oid, value);
    }

    private void init(ObjectIdentifier oid, Object value) throws IllegalArgumentException {
        Class<?> clazz;
        this.oid = oid;
        this.index = PKCS9Attribute.indexOf(oid, PKCS9_OIDS, 1);
        Class<?> clazz2 = clazz = this.index == -1 ? BYTE_ARRAY_CLASS : VALUE_CLASSES[this.index];
        if (clazz == null) {
            throw new IllegalArgumentException("No value class supported  for attribute " + oid + " constructing PKCS9Attribute");
        }
        if (!clazz.isInstance(value)) {
            throw new IllegalArgumentException("Wrong value class  for attribute " + oid + " constructing PKCS9Attribute; was " + value.getClass().toString() + ", should be " + clazz.toString());
        }
        this.value = value;
    }

    public PKCS9Attribute(DerValue derVal) throws IOException {
        DerInputStream derIn = new DerInputStream(derVal.toByteArray());
        DerValue[] val = derIn.getSequence(2);
        if (derIn.available() != 0) {
            throw new IOException("Excess data parsing PKCS9Attribute");
        }
        if (val.length != 2) {
            throw new IOException("PKCS9Attribute doesn't have two components");
        }
        this.oid = val[0].getOID();
        byte[] content = val[1].toByteArray();
        DerValue[] elems = new DerInputStream(content).getSet(1);
        this.index = PKCS9Attribute.indexOf(this.oid, PKCS9_OIDS, 1);
        if (this.index == -1) {
            if (debug != null) {
                debug.println("Unsupported signer attribute: " + this.oid);
            }
            this.value = content;
            return;
        }
        if (SINGLE_VALUED[this.index] && elems.length > 1) {
            this.throwSingleValuedException();
        }
        for (DerValue elem : elems) {
            Byte tag = elem.tag;
            if (PKCS9Attribute.indexOf(tag, PKCS9_VALUE_TAGS[this.index], 0) != -1) continue;
            this.throwTagException(tag);
        }
        switch (this.index) {
            case 1: 
            case 2: 
            case 8: {
                String[] values = new String[elems.length];
                for (int i = 0; i < elems.length; ++i) {
                    values[i] = elems[i].getAsString();
                }
                this.value = values;
                break;
            }
            case 3: {
                this.value = elems[0].getOID();
                break;
            }
            case 4: {
                this.value = elems[0].getOctetString();
                break;
            }
            case 5: {
                byte elemTag = elems[0].getTag();
                DerInputStream dis = new DerInputStream(elems[0].toByteArray());
                this.value = elemTag == 24 ? dis.getGeneralizedTime() : dis.getUTCTime();
                break;
            }
            case 6: {
                SignerInfo[] values = new SignerInfo[elems.length];
                for (int i = 0; i < elems.length; ++i) {
                    values[i] = new SignerInfo(elems[i].toDerInputStream());
                }
                this.value = values;
                break;
            }
            case 7: {
                this.value = elems[0].getAsString();
                break;
            }
            case 9: {
                throw new IOException("PKCS9 extended-certificate attribute not supported.");
            }
            case 10: {
                throw new IOException("PKCS9 IssuerAndSerialNumber attribute not supported.");
            }
            case 11: 
            case 12: {
                throw new IOException("PKCS9 RSA DSI attributes 11 and 12, not supported.");
            }
            case 13: {
                throw new IOException("PKCS9 attribute #13 not supported.");
            }
            case 14: {
                this.value = new CertificateExtensions(new DerInputStream(elems[0].toByteArray()));
                break;
            }
            case 15: {
                throw new IOException("PKCS9 SMIMECapability attribute not supported.");
            }
            case 16: {
                this.value = new SigningCertificateInfo(elems[0].toByteArray());
                break;
            }
            case 17: 
            case 18: {
                this.value = elems[0].toByteArray();
                break;
            }
        }
    }

    @Override
    public void encode(DerOutputStream out) {
        DerOutputStream temp = new DerOutputStream();
        temp.putOID(this.oid);
        switch (this.index) {
            case -1: {
                temp.writeBytes((byte[])this.value);
                break;
            }
            case 1: 
            case 2: {
                String[] values = (String[])this.value;
                DerEncoder[] temps = new DerOutputStream[values.length];
                for (int i = 0; i < values.length; ++i) {
                    temps[i] = new DerOutputStream();
                    ((DerOutputStream)temps[i]).putIA5String(values[i]);
                }
                temp.putOrderedSetOf((byte)49, temps);
                break;
            }
            case 3: {
                DerOutputStream temp2 = new DerOutputStream();
                temp2.putOID((ObjectIdentifier)this.value);
                temp.write((byte)49, temp2.toByteArray());
                break;
            }
            case 4: {
                DerOutputStream temp2 = new DerOutputStream();
                temp2.putOctetString((byte[])this.value);
                temp.write((byte)49, temp2.toByteArray());
                break;
            }
            case 5: {
                DerOutputStream temp2 = new DerOutputStream();
                temp2.putUTCTime((Date)this.value);
                temp.write((byte)49, temp2.toByteArray());
                break;
            }
            case 6: {
                temp.putOrderedSetOf((byte)49, (DerEncoder[])this.value);
                break;
            }
            case 7: {
                DerOutputStream temp2 = new DerOutputStream();
                temp2.putPrintableString((String)this.value);
                temp.write((byte)49, temp2.toByteArray());
                break;
            }
            case 8: {
                String[] values = (String[])this.value;
                DerEncoder[] temps = new DerOutputStream[values.length];
                for (int i = 0; i < values.length; ++i) {
                    temps[i] = new DerOutputStream();
                    ((DerOutputStream)temps[i]).putPrintableString(values[i]);
                }
                temp.putOrderedSetOf((byte)49, temps);
                break;
            }
            case 9: {
                throw new IllegalArgumentException("PKCS9 extended-certificate attribute not supported.");
            }
            case 10: {
                throw new IllegalArgumentException("PKCS9 IssuerAndSerialNumber attribute not supported.");
            }
            case 11: 
            case 12: {
                throw new IllegalArgumentException("PKCS9 RSA DSI attributes 11 and 12, not supported.");
            }
            case 13: {
                throw new IllegalArgumentException("PKCS9 attribute #13 not supported.");
            }
            case 14: {
                DerOutputStream temp2 = new DerOutputStream();
                CertificateExtensions exts = (CertificateExtensions)this.value;
                exts.encode(temp2, true);
                temp.write((byte)49, temp2.toByteArray());
                break;
            }
            case 15: {
                throw new IllegalArgumentException("PKCS9 attribute #15 not supported.");
            }
            case 16: {
                DerOutputStream temp2 = new DerOutputStream();
                SigningCertificateInfo info = (SigningCertificateInfo)this.value;
                temp2.writeBytes(info.toByteArray());
                temp.write((byte)49, temp2.toByteArray());
                break;
            }
            case 17: 
            case 18: {
                temp.write((byte)49, (byte[])this.value);
                break;
            }
        }
        out.write((byte)48, temp.toByteArray());
    }

    public boolean isKnown() {
        return this.index != -1;
    }

    public Object getValue() {
        return this.value;
    }

    public boolean isSingleValued() {
        return this.index == -1 || SINGLE_VALUED[this.index];
    }

    public ObjectIdentifier getOID() {
        return this.oid;
    }

    public String getName() {
        String n = this.oid.toString();
        KnownOIDs os = KnownOIDs.findMatch(n);
        return os == null ? n : os.stdName();
    }

    public static ObjectIdentifier getOID(String name) {
        KnownOIDs o = KnownOIDs.findMatch(name);
        if (o != null) {
            return ObjectIdentifier.of(o);
        }
        return null;
    }

    public static String getName(ObjectIdentifier oid) {
        return KnownOIDs.findMatch(oid.toString()).stdName();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append("[");
        if (this.index == -1) {
            sb.append(this.oid.toString());
        } else {
            sb.append(PKCS9Attribute.getName(this.oid));
        }
        sb.append(": ");
        if (this.index == -1 || SINGLE_VALUED[this.index]) {
            if (this.value instanceof byte[]) {
                HexDumpEncoder hexDump = new HexDumpEncoder();
                sb.append(hexDump.encodeBuffer((byte[])this.value));
            } else {
                sb.append(this.value.toString());
            }
            sb.append("]");
        } else {
            Object[] values;
            boolean first = true;
            for (Object curVal : values = (Object[])this.value) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(curVal.toString());
            }
        }
        return sb.toString();
    }

    static int indexOf(Object obj, Object[] a, int start) {
        for (int i = start; i < a.length; ++i) {
            if (!obj.equals(a[i])) continue;
            return i;
        }
        return -1;
    }

    private void throwSingleValuedException() throws IOException {
        throw new IOException("Single-value attribute " + this.oid + " (" + this.getName() + ") has multiple values.");
    }

    private void throwTagException(Byte tag) throws IOException {
        Byte[] expectedTags = PKCS9_VALUE_TAGS[this.index];
        StringBuilder msg = new StringBuilder(100);
        msg.append("Value of attribute ");
        msg.append(this.oid.toString());
        msg.append(" (");
        msg.append(this.getName());
        msg.append(") has wrong tag: ");
        msg.append(tag.toString());
        msg.append(".  Expected tags: ");
        msg.append(expectedTags[0].toString());
        for (int i = 1; i < expectedTags.length; ++i) {
            msg.append(", ");
            msg.append(expectedTags[i].toString());
        }
        msg.append(".");
        throw new IOException(msg.toString());
    }

    static {
        PKCS9Attribute.PKCS9_OIDS[15] = null;
        PKCS9Attribute.PKCS9_OIDS[13] = null;
        PKCS9Attribute.PKCS9_OIDS[12] = null;
        PKCS9Attribute.PKCS9_OIDS[11] = null;
        PKCS9Attribute.PKCS9_OIDS[0] = null;
        try {
            BYTE_ARRAY_CLASS = Class.forName("[B");
        }
        catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e.toString());
        }
        EMAIL_ADDRESS_OID = PKCS9Attribute.PKCS9_OIDS[1] = ObjectIdentifier.of(KnownOIDs.EmailAddress);
        UNSTRUCTURED_NAME_OID = PKCS9Attribute.PKCS9_OIDS[2] = ObjectIdentifier.of(KnownOIDs.UnstructuredName);
        CONTENT_TYPE_OID = PKCS9Attribute.PKCS9_OIDS[3] = ObjectIdentifier.of(KnownOIDs.ContentType);
        MESSAGE_DIGEST_OID = PKCS9Attribute.PKCS9_OIDS[4] = ObjectIdentifier.of(KnownOIDs.MessageDigest);
        SIGNING_TIME_OID = PKCS9Attribute.PKCS9_OIDS[5] = ObjectIdentifier.of(KnownOIDs.SigningTime);
        COUNTERSIGNATURE_OID = PKCS9Attribute.PKCS9_OIDS[6] = ObjectIdentifier.of(KnownOIDs.CounterSignature);
        CHALLENGE_PASSWORD_OID = PKCS9Attribute.PKCS9_OIDS[7] = ObjectIdentifier.of(KnownOIDs.ChallengePassword);
        UNSTRUCTURED_ADDRESS_OID = PKCS9Attribute.PKCS9_OIDS[8] = ObjectIdentifier.of(KnownOIDs.UnstructuredAddress);
        EXTENDED_CERTIFICATE_ATTRIBUTES_OID = PKCS9Attribute.PKCS9_OIDS[9] = ObjectIdentifier.of(KnownOIDs.ExtendedCertificateAttributes);
        ISSUER_SERIALNUMBER_OID = PKCS9Attribute.PKCS9_OIDS[10] = ObjectIdentifier.of(KnownOIDs.IssuerAndSerialNumber);
        EXTENSION_REQUEST_OID = PKCS9Attribute.PKCS9_OIDS[14] = ObjectIdentifier.of(KnownOIDs.ExtensionRequest);
        SIGNING_CERTIFICATE_OID = PKCS9Attribute.PKCS9_OIDS[16] = ObjectIdentifier.of(KnownOIDs.SigningCertificate);
        SIGNATURE_TIMESTAMP_TOKEN_OID = PKCS9Attribute.PKCS9_OIDS[17] = ObjectIdentifier.of(KnownOIDs.SignatureTimestampToken);
        CMS_ALGORITHM_PROTECTION_OID = PKCS9Attribute.PKCS9_OIDS[18] = ObjectIdentifier.of(KnownOIDs.CMSAlgorithmProtection);
        PKCS9_VALUE_TAGS = new Byte[][]{null, {(byte)22}, {(byte)22, (byte)19, (byte)20, (byte)30, (byte)28, (byte)12}, {(byte)6}, {(byte)4}, {(byte)23, (byte)24}, {(byte)48}, {(byte)19, (byte)20, (byte)30, (byte)28, (byte)12}, {(byte)19, (byte)20, (byte)30, (byte)28, (byte)12}, {(byte)49}, {(byte)48}, null, null, null, {(byte)48}, {(byte)48}, {(byte)48}, {(byte)48}, {(byte)48}};
        VALUE_CLASSES = new Class[19];
        try {
            Class<?> str = Class.forName("[Ljava.lang.String;");
            PKCS9Attribute.VALUE_CLASSES[0] = null;
            PKCS9Attribute.VALUE_CLASSES[1] = str;
            PKCS9Attribute.VALUE_CLASSES[2] = str;
            PKCS9Attribute.VALUE_CLASSES[3] = Class.forName("sun.security.util.ObjectIdentifier");
            PKCS9Attribute.VALUE_CLASSES[4] = BYTE_ARRAY_CLASS;
            PKCS9Attribute.VALUE_CLASSES[5] = Class.forName("java.util.Date");
            PKCS9Attribute.VALUE_CLASSES[6] = Class.forName("[Lsun.security.pkcs.SignerInfo;");
            PKCS9Attribute.VALUE_CLASSES[7] = Class.forName("java.lang.String");
            PKCS9Attribute.VALUE_CLASSES[8] = str;
            PKCS9Attribute.VALUE_CLASSES[9] = null;
            PKCS9Attribute.VALUE_CLASSES[10] = null;
            PKCS9Attribute.VALUE_CLASSES[11] = null;
            PKCS9Attribute.VALUE_CLASSES[12] = null;
            PKCS9Attribute.VALUE_CLASSES[13] = null;
            PKCS9Attribute.VALUE_CLASSES[14] = Class.forName("sun.security.x509.CertificateExtensions");
            PKCS9Attribute.VALUE_CLASSES[15] = null;
            PKCS9Attribute.VALUE_CLASSES[16] = null;
            PKCS9Attribute.VALUE_CLASSES[17] = BYTE_ARRAY_CLASS;
            PKCS9Attribute.VALUE_CLASSES[18] = BYTE_ARRAY_CLASS;
        }
        catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e.toString());
        }
        SINGLE_VALUED = new boolean[]{false, false, false, true, true, true, false, true, false, false, true, false, false, false, true, true, true, true, true};
    }
}

