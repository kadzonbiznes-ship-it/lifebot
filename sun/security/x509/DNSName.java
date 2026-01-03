/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.util.Locale;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.GeneralNameInterface;

public class DNSName
implements GeneralNameInterface {
    private final String name;
    private static final String alphaDigits = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public DNSName(DerValue derValue) throws IOException {
        this.name = derValue.getIA5String();
    }

    public DNSName(String name, boolean allowWildcard) throws IOException {
        if (name == null || name.isEmpty()) {
            throw new IOException("DNSName must not be null or empty");
        }
        if (name.contains(" ")) {
            throw new IOException("DNSName with blank components is not permitted");
        }
        if (name.startsWith(".") || name.endsWith(".")) {
            throw new IOException("DNSName may not begin or end with a .");
        }
        int startIndex = 0;
        while (startIndex < name.length()) {
            int endIndex = name.indexOf(46, startIndex);
            if (endIndex < 0) {
                endIndex = name.length();
            }
            if (endIndex - startIndex < 1) {
                throw new IOException("DNSName with empty components are not permitted");
            }
            if (allowWildcard) {
                if (alphaDigits.indexOf(name.charAt(startIndex)) < 0 && (name.length() < 3 || name.indexOf(42) != 0 || name.charAt(startIndex + 1) != '.' || alphaDigits.indexOf(name.charAt(startIndex + 2)) < 0)) {
                    throw new IOException("DNSName components must begin with a letter, digit, or the first component can have only a wildcard character *");
                }
            } else if (alphaDigits.indexOf(name.charAt(startIndex)) < 0) {
                throw new IOException("DNSName components must begin with a letter or digit");
            }
            for (int nonStartIndex = startIndex + 1; nonStartIndex < endIndex; ++nonStartIndex) {
                char x = name.charAt(nonStartIndex);
                if (alphaDigits.indexOf(x) >= 0 || x == '-') continue;
                throw new IOException("DNSName components must consist of letters, digits, and hyphens");
            }
            startIndex = endIndex + 1;
        }
        this.name = name;
    }

    public DNSName(String name) throws IOException {
        this(name, false);
    }

    @Override
    public int getType() {
        return 2;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public void encode(DerOutputStream out) {
        out.putIA5String(this.name);
    }

    public String toString() {
        return "DNSName: " + this.name;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DNSName)) {
            return false;
        }
        DNSName other = (DNSName)obj;
        return this.name.equalsIgnoreCase(other.name);
    }

    public int hashCode() {
        return this.name.toUpperCase(Locale.ENGLISH).hashCode();
    }

    @Override
    public int constrains(GeneralNameInterface inputName) throws UnsupportedOperationException {
        int ndx;
        int inNdx;
        String thisName;
        String inName;
        int constraintType = inputName == null ? -1 : (inputName.getType() != 2 ? -1 : ((inName = ((DNSName)inputName).getName().toLowerCase(Locale.ENGLISH)).equals(thisName = this.name.toLowerCase(Locale.ENGLISH)) ? 0 : (thisName.endsWith(inName) ? (thisName.charAt((inNdx = thisName.lastIndexOf(inName)) - 1) == '.' ^ inName.charAt(0) == '.' ? 2 : 3) : (inName.endsWith(thisName) ? (inName.charAt((ndx = inName.lastIndexOf(thisName)) - 1) == '.' ^ thisName.charAt(0) == '.' ? 1 : 3) : 3))));
        return constraintType;
    }

    @Override
    public int subtreeDepth() throws UnsupportedOperationException {
        int sum = 1;
        int i = this.name.indexOf(46);
        while (i >= 0) {
            ++sum;
            i = this.name.indexOf(46, i + 1);
        }
        return sum;
    }
}

