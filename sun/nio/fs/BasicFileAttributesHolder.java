/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.fs;

import java.nio.file.attribute.BasicFileAttributes;

public interface BasicFileAttributesHolder {
    public BasicFileAttributes get();

    public void invalidate();
}

