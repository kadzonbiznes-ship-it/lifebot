/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.DNSName;
import sun.security.x509.GeneralNameInterface;
import sun.security.x509.IPAddressName;

public class URIName
implements GeneralNameInterface {
    private final URI uri;
    private final String host;
    private DNSName hostDNS;
    private IPAddressName hostIP;

    public URIName(DerValue derValue) throws IOException {
        this(derValue.getIA5String());
    }

    public URIName(String name) throws IOException {
        try {
            this.uri = new URI(name);
        }
        catch (URISyntaxException use) {
            throw new IOException("invalid URI name:" + name, use);
        }
        if (this.uri.getScheme() == null) {
            throw new IOException("URI name must include scheme:" + name);
        }
        this.host = this.uri.getHost();
        if (this.host != null) {
            if (this.host.charAt(0) == '[') {
                String ipV6Host = this.host.substring(1, this.host.length() - 1);
                try {
                    this.hostIP = new IPAddressName(ipV6Host);
                }
                catch (IOException ioe) {
                    throw new IOException("invalid URI name (host portion is not a valid IPv6 address):" + name);
                }
            }
            try {
                this.hostDNS = new DNSName(this.host);
            }
            catch (IOException ioe) {
                try {
                    this.hostIP = new IPAddressName(this.host);
                }
                catch (Exception ioe2) {
                    throw new IOException("invalid URI name (host portion is not a valid DNSName, IPv4 address, or IPv6 address):" + name);
                }
            }
        }
    }

    public static URIName nameConstraint(DerValue value) throws IOException {
        URI uri;
        String name = value.getIA5String();
        try {
            uri = new URI(name);
        }
        catch (URISyntaxException use) {
            throw new IOException("invalid URI name constraint:" + name, use);
        }
        if (uri.getScheme() == null) {
            String host = uri.getSchemeSpecificPart();
            try {
                DNSName hostDNS = host.startsWith(".") ? new DNSName(host.substring(1)) : new DNSName(host);
                return new URIName(uri, host, hostDNS);
            }
            catch (IOException ioe) {
                throw new IOException("invalid URI name constraint:" + name, ioe);
            }
        }
        throw new IOException("invalid URI name constraint (should not include scheme):" + name);
    }

    URIName(URI uri, String host, DNSName hostDNS) {
        this.uri = uri;
        this.host = host;
        this.hostDNS = hostDNS;
    }

    @Override
    public int getType() {
        return 6;
    }

    @Override
    public void encode(DerOutputStream out) {
        out.putIA5String(this.uri.toASCIIString());
    }

    public String toString() {
        return "URIName: " + this.uri.toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof URIName)) {
            return false;
        }
        URIName other = (URIName)obj;
        return this.uri.equals(other.getURI());
    }

    public URI getURI() {
        return this.uri;
    }

    public String getName() {
        return this.uri.toString();
    }

    public String getScheme() {
        return this.uri.getScheme();
    }

    public String getHost() {
        return this.host;
    }

    public Object getHostObject() {
        if (this.hostIP != null) {
            return this.hostIP;
        }
        return this.hostDNS;
    }

    public int hashCode() {
        return this.uri.hashCode();
    }

    @Override
    public int constrains(GeneralNameInterface inputName) throws UnsupportedOperationException {
        int constraintType;
        if (inputName == null) {
            constraintType = -1;
        } else if (inputName.getType() != 6) {
            constraintType = -1;
        } else {
            String otherHost = ((URIName)inputName).getHost();
            if (otherHost.equalsIgnoreCase(this.host)) {
                constraintType = 0;
            } else {
                Object otherHostObject = ((URIName)inputName).getHostObject();
                if (this.hostDNS == null || !(otherHostObject instanceof DNSName)) {
                    constraintType = 3;
                } else {
                    DNSName otherDNS = (DNSName)otherHostObject;
                    boolean thisDomain = this.host.charAt(0) == '.';
                    boolean otherDomain = otherHost.charAt(0) == '.';
                    constraintType = this.hostDNS.constrains(otherDNS);
                    if (!(thisDomain || otherDomain || constraintType != 2 && constraintType != 1)) {
                        constraintType = 3;
                    }
                    if (thisDomain != otherDomain && constraintType == 0) {
                        constraintType = thisDomain ? 2 : 1;
                    }
                }
            }
        }
        return constraintType;
    }

    @Override
    public int subtreeDepth() throws UnsupportedOperationException {
        DNSName dnsName;
        try {
            dnsName = new DNSName(this.host);
        }
        catch (IOException ioe) {
            throw new UnsupportedOperationException(ioe.getMessage());
        }
        return dnsName.subtreeDepth();
    }
}

