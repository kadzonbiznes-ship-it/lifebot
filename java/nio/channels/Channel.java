/*
 * Decompiled with CFR 0.152.
 */
package java.nio.channels;

import java.io.Closeable;
import java.io.IOException;

public interface Channel
extends Closeable {
    public boolean isOpen();

    @Override
    public void close() throws IOException;
}

