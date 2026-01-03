/*
 * Decompiled with CFR 0.152.
 */
package java.security;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

public class DigestInputStream
extends FilterInputStream {
    private boolean on = true;
    protected MessageDigest digest;

    public DigestInputStream(InputStream stream, MessageDigest digest) {
        super(stream);
        this.setMessageDigest(digest);
    }

    public MessageDigest getMessageDigest() {
        return this.digest;
    }

    public void setMessageDigest(MessageDigest digest) {
        this.digest = digest;
    }

    @Override
    public int read() throws IOException {
        int ch = this.in.read();
        if (this.on && ch != -1) {
            this.digest.update((byte)ch);
        }
        return ch;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = this.in.read(b, off, len);
        if (this.on && result != -1) {
            this.digest.update(b, off, result);
        }
        return result;
    }

    public void on(boolean on) {
        this.on = on;
    }

    public String toString() {
        return "[Digest Input Stream] " + this.digest.toString();
    }
}

