/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.net.ssl.SSLProtocolException;
import sun.security.ssl.RandomCookie;
import sun.security.ssl.Utilities;

final class SessionId {
    static final int MAX_LENGTH = 32;
    private final byte[] sessionId;

    SessionId(boolean isRejoinable, SecureRandom generator) {
        this.sessionId = isRejoinable && generator != null ? new RandomCookie((SecureRandom)generator).randomBytes : new byte[0];
    }

    SessionId(byte[] sessionId) {
        this.sessionId = (byte[])sessionId.clone();
    }

    int length() {
        return this.sessionId.length;
    }

    byte[] getId() {
        return (byte[])this.sessionId.clone();
    }

    public String toString() {
        if (this.sessionId.length == 0) {
            return "";
        }
        return Utilities.toHexString(this.sessionId);
    }

    public int hashCode() {
        return Arrays.hashCode(this.sessionId);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof SessionId) {
            SessionId that = (SessionId)obj;
            return MessageDigest.isEqual(this.sessionId, that.sessionId);
        }
        return false;
    }

    void checkLength(int protocolVersion) throws SSLProtocolException {
        if (this.sessionId.length > 32) {
            throw new SSLProtocolException("Invalid session ID length (" + this.sessionId.length + " bytes)");
        }
    }
}

