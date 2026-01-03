/*
 * Decompiled with CFR 0.152.
 */
package sun.security.pkcs;

import java.io.IOException;
import sun.security.pkcs.ParsingException;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.KnownOIDs;
import sun.security.util.ObjectIdentifier;

public class ContentInfo
implements DerEncoder {
    public static ObjectIdentifier PKCS7_OID = ObjectIdentifier.of(KnownOIDs.PKCS7);
    public static ObjectIdentifier DATA_OID = ObjectIdentifier.of(KnownOIDs.Data);
    public static ObjectIdentifier SIGNED_DATA_OID = ObjectIdentifier.of(KnownOIDs.SignedData);
    public static ObjectIdentifier ENVELOPED_DATA_OID = ObjectIdentifier.of(KnownOIDs.EnvelopedData);
    public static ObjectIdentifier SIGNED_AND_ENVELOPED_DATA_OID = ObjectIdentifier.of(KnownOIDs.SignedAndEnvelopedData);
    public static ObjectIdentifier DIGESTED_DATA_OID = ObjectIdentifier.of(KnownOIDs.DigestedData);
    public static ObjectIdentifier ENCRYPTED_DATA_OID = ObjectIdentifier.of(KnownOIDs.EncryptedData);
    public static ObjectIdentifier OLD_SIGNED_DATA_OID = ObjectIdentifier.of(KnownOIDs.JDK_OLD_SignedData);
    public static ObjectIdentifier OLD_DATA_OID = ObjectIdentifier.of(KnownOIDs.JDK_OLD_Data);
    public static ObjectIdentifier NETSCAPE_CERT_SEQUENCE_OID = ObjectIdentifier.of(KnownOIDs.NETSCAPE_CertSequence);
    public static ObjectIdentifier TIMESTAMP_TOKEN_INFO_OID = ObjectIdentifier.of(KnownOIDs.TimeStampTokenInfo);
    ObjectIdentifier contentType;
    DerValue content;

    public ContentInfo(ObjectIdentifier contentType, DerValue content) {
        this.contentType = contentType;
        this.content = content;
    }

    public ContentInfo(byte[] bytes) {
        DerValue octetString = new DerValue(4, bytes);
        this.contentType = DATA_OID;
        this.content = octetString;
    }

    public ContentInfo(DerInputStream derin) throws IOException {
        this(derin, false);
    }

    public ContentInfo(DerInputStream derin, boolean oldStyle) throws IOException {
        DerValue[] typeAndContent = derin.getSequence(2);
        if (typeAndContent.length < 1 || typeAndContent.length > 2) {
            throw new ParsingException("Invalid length for ContentInfo");
        }
        DerValue type = typeAndContent[0];
        DerInputStream disType = new DerInputStream(type.toByteArray());
        this.contentType = disType.getOID();
        if (oldStyle) {
            if (typeAndContent.length > 1) {
                this.content = typeAndContent[1];
            }
        } else if (typeAndContent.length > 1) {
            DerValue taggedContent = typeAndContent[1];
            DerInputStream disTaggedContent = new DerInputStream(taggedContent.toByteArray());
            DerValue[] contents = disTaggedContent.getSet(1, true);
            if (contents.length != 1) {
                throw new ParsingException("ContentInfo encoding error");
            }
            this.content = contents[0];
        }
    }

    public DerValue getContent() {
        return this.content;
    }

    public ObjectIdentifier getContentType() {
        return this.contentType;
    }

    public byte[] getData() throws IOException {
        if (this.contentType.equals(DATA_OID) || this.contentType.equals(OLD_DATA_OID) || this.contentType.equals(TIMESTAMP_TOKEN_INFO_OID)) {
            if (this.content == null) {
                return null;
            }
            return this.content.getOctetString();
        }
        throw new IOException("content type is not DATA: " + this.contentType);
    }

    @Override
    public void encode(DerOutputStream out) {
        DerOutputStream seq = new DerOutputStream();
        seq.putOID(this.contentType);
        if (this.content != null) {
            DerOutputStream contentDerCode = new DerOutputStream();
            this.content.encode(contentDerCode);
            DerValue taggedContent = new DerValue(-96, contentDerCode.toByteArray());
            seq.putDerValue(taggedContent);
        }
        out.write((byte)48, seq);
    }

    public byte[] getContentBytes() throws IOException {
        if (this.content == null) {
            return null;
        }
        DerValue v = new DerValue(this.content.toByteArray());
        return v.getOctetString();
    }

    public String toString() {
        String out = "";
        out = out + "Content Info Sequence\n\tContent type: " + this.contentType + "\n";
        out = out + "\tContent: " + this.content;
        return out;
    }
}

