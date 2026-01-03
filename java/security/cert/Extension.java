/*
 * Decompiled with CFR 0.152.
 */
package java.security.cert;

import java.io.IOException;
import java.io.OutputStream;

public interface Extension {
    public String getId();

    public boolean isCritical();

    public byte[] getValue();

    public void encode(OutputStream var1) throws IOException;
}

