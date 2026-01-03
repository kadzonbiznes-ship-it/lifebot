/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.namespace;

import java.io.Serializable;

public class QName
implements Serializable {
    private static final long serialVersionUID = -9120448754896609940L;
    private final String namespaceURI;
    private final String localPart;
    private final String prefix;

    public QName(String namespaceURI, String localPart) {
        this(namespaceURI, localPart, "");
    }

    public QName(String namespaceURI, String localPart, String prefix) {
        this.namespaceURI = namespaceURI == null ? "" : namespaceURI;
        if (localPart == null) {
            throw new IllegalArgumentException("local part cannot be \"null\" when creating a QName");
        }
        this.localPart = localPart;
        if (prefix == null) {
            throw new IllegalArgumentException("prefix cannot be \"null\" when creating a QName");
        }
        this.prefix = prefix;
    }

    public QName(String localPart) {
        this("", localPart, "");
    }

    public String getNamespaceURI() {
        return this.namespaceURI;
    }

    public String getLocalPart() {
        return this.localPart;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public final boolean equals(Object objectToTest) {
        if (objectToTest == this) {
            return true;
        }
        if (objectToTest == null || !(objectToTest instanceof QName)) {
            return false;
        }
        QName qName = (QName)objectToTest;
        return this.localPart.equals(qName.localPart) && this.namespaceURI.equals(qName.namespaceURI);
    }

    public final int hashCode() {
        return this.namespaceURI.hashCode() ^ this.localPart.hashCode();
    }

    public String toString() {
        if (this.namespaceURI.equals("")) {
            return this.localPart;
        }
        return "{" + this.namespaceURI + "}" + this.localPart;
    }

    public static QName valueOf(String qNameAsString) {
        if (qNameAsString == null) {
            throw new IllegalArgumentException("cannot create QName from \"null\" or \"\" String");
        }
        if (qNameAsString.length() == 0) {
            return new QName("", qNameAsString, "");
        }
        if (qNameAsString.charAt(0) != '{') {
            return new QName("", qNameAsString, "");
        }
        if (qNameAsString.startsWith("{}")) {
            throw new IllegalArgumentException("Namespace URI .equals(XMLConstants.NULL_NS_URI), .equals(\"\"), only the local part, \"" + qNameAsString.substring(2 + "".length()) + "\", should be provided.");
        }
        int endOfNamespaceURI = qNameAsString.indexOf(125);
        if (endOfNamespaceURI == -1) {
            throw new IllegalArgumentException("cannot create QName from \"" + qNameAsString + "\", missing closing \"}\"");
        }
        return new QName(qNameAsString.substring(1, endOfNamespaceURI), qNameAsString.substring(endOfNamespaceURI + 1), "");
    }
}

