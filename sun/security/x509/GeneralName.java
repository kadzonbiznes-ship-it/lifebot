/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerEncoder;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.DNSName;
import sun.security.x509.EDIPartyName;
import sun.security.x509.GeneralNameInterface;
import sun.security.x509.IPAddressName;
import sun.security.x509.OIDName;
import sun.security.x509.OtherName;
import sun.security.x509.RFC822Name;
import sun.security.x509.URIName;
import sun.security.x509.X400Address;
import sun.security.x509.X500Name;

public class GeneralName
implements DerEncoder {
    private final GeneralNameInterface name;

    public GeneralName(GeneralNameInterface name) {
        if (name == null) {
            throw new NullPointerException("GeneralName must not be null");
        }
        this.name = name;
    }

    public GeneralName(DerValue encName) throws IOException {
        this(encName, false);
    }

    public GeneralName(DerValue encName, boolean nameConstraint) throws IOException {
        short tag = (byte)(encName.tag & 0x1F);
        switch (tag) {
            case 0: {
                if (encName.isContextSpecific() && encName.isConstructed()) {
                    encName.resetTag((byte)48);
                    this.name = new OtherName(encName);
                    break;
                }
                throw new IOException("Invalid encoding of Other-Name");
            }
            case 1: {
                if (encName.isContextSpecific() && !encName.isConstructed()) {
                    encName.resetTag((byte)22);
                    this.name = new RFC822Name(encName);
                    break;
                }
                throw new IOException("Invalid encoding of RFC822 name");
            }
            case 2: {
                if (encName.isContextSpecific() && !encName.isConstructed()) {
                    encName.resetTag((byte)22);
                    this.name = new DNSName(encName);
                    break;
                }
                throw new IOException("Invalid encoding of DNSName");
            }
            case 3: {
                if (encName.isContextSpecific() && encName.isConstructed()) {
                    encName.resetTag((byte)22);
                    this.name = new X400Address(encName);
                    break;
                }
                throw new IOException("Invalid encoding of X400Address name");
            }
            case 6: {
                if (encName.isContextSpecific() && !encName.isConstructed()) {
                    encName.resetTag((byte)22);
                    this.name = nameConstraint ? URIName.nameConstraint(encName) : new URIName(encName);
                    break;
                }
                throw new IOException("Invalid encoding of URI");
            }
            case 7: {
                if (encName.isContextSpecific() && !encName.isConstructed()) {
                    encName.resetTag((byte)4);
                    this.name = new IPAddressName(encName);
                    break;
                }
                throw new IOException("Invalid encoding of IP address");
            }
            case 8: {
                if (encName.isContextSpecific() && !encName.isConstructed()) {
                    encName.resetTag((byte)6);
                    this.name = new OIDName(encName);
                    break;
                }
                throw new IOException("Invalid encoding of OID name");
            }
            case 4: {
                if (encName.isContextSpecific() && encName.isConstructed()) {
                    this.name = new X500Name(encName.getData());
                    break;
                }
                throw new IOException("Invalid encoding of Directory name");
            }
            case 5: {
                if (encName.isContextSpecific() && encName.isConstructed()) {
                    encName.resetTag((byte)48);
                    this.name = new EDIPartyName(encName);
                    break;
                }
                throw new IOException("Invalid encoding of EDI name");
            }
            default: {
                throw new IOException("Unrecognized GeneralName tag, (" + tag + ")");
            }
        }
    }

    public int getType() {
        return this.name.getType();
    }

    public GeneralNameInterface getName() {
        return this.name;
    }

    public String toString() {
        return this.name.toString();
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof GeneralName)) {
            return false;
        }
        GeneralNameInterface otherGNI = ((GeneralName)other).name;
        try {
            return this.name.constrains(otherGNI) == 0;
        }
        catch (UnsupportedOperationException ioe) {
            return false;
        }
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public void encode(DerOutputStream out) {
        DerOutputStream tmp = new DerOutputStream();
        this.name.encode(tmp);
        int nameType = this.name.getType();
        if (nameType == 0 || nameType == 3 || nameType == 5) {
            out.writeImplicit(DerValue.createTag((byte)-128, true, (byte)nameType), tmp);
        } else if (nameType == 4) {
            out.write(DerValue.createTag((byte)-128, true, (byte)nameType), tmp);
        } else {
            out.writeImplicit(DerValue.createTag((byte)-128, false, (byte)nameType), tmp);
        }
    }
}

