/*
 * Decompiled with CFR 0.152.
 */
package javax.net.ssl;

import java.net.IDN;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.net.ssl.SNIMatcher;
import javax.net.ssl.SNIServerName;

public final class SNIHostName
extends SNIServerName {
    private final String hostname;

    public SNIHostName(String hostname) {
        hostname = IDN.toASCII(Objects.requireNonNull(hostname, "Server name value of host_name cannot be null"), 2);
        super(0, hostname.getBytes(StandardCharsets.US_ASCII));
        this.hostname = hostname;
        this.checkHostName();
    }

    public SNIHostName(byte[] encoded) {
        super(0, encoded);
        try {
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
            this.hostname = IDN.toASCII(decoder.decode(ByteBuffer.wrap(encoded)).toString(), 2);
        }
        catch (RuntimeException | CharacterCodingException e) {
            throw new IllegalArgumentException("The encoded server name value is invalid", e);
        }
        this.checkHostName();
    }

    public String getAsciiName() {
        return this.hostname;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof SNIHostName) {
            return this.hostname.equalsIgnoreCase(((SNIHostName)other).hostname);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.hostname.toUpperCase(Locale.ENGLISH).hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "type=host_name (0), value=" + this.hostname;
    }

    public static SNIMatcher createSNIMatcher(String regex) {
        if (regex == null) {
            throw new NullPointerException("The regular expression cannot be null");
        }
        return new SNIHostNameMatcher(regex);
    }

    private void checkHostName() {
        if (this.hostname.isEmpty()) {
            throw new IllegalArgumentException("Server name value of host_name cannot be empty");
        }
        if (this.hostname.endsWith(".")) {
            throw new IllegalArgumentException("Server name value of host_name cannot have the trailing dot");
        }
    }

    private static final class SNIHostNameMatcher
    extends SNIMatcher {
        private final Pattern pattern;

        SNIHostNameMatcher(String regex) {
            super(0);
            this.pattern = Pattern.compile(regex, 2);
        }

        @Override
        public boolean matches(SNIServerName serverName) {
            SNIHostName hostname;
            if (serverName == null) {
                throw new NullPointerException("The SNIServerName argument cannot be null");
            }
            if (!(serverName instanceof SNIHostName)) {
                if (serverName.getType() != 0) {
                    throw new IllegalArgumentException("The server name type is not host_name");
                }
                try {
                    hostname = new SNIHostName(serverName.getEncoded());
                }
                catch (IllegalArgumentException | NullPointerException e) {
                    return false;
                }
            } else {
                hostname = (SNIHostName)serverName;
            }
            String asciiName = hostname.getAsciiName();
            if (this.pattern.matcher(asciiName).matches()) {
                return true;
            }
            return this.pattern.matcher(IDN.toUnicode(asciiName)).matches();
        }
    }
}

