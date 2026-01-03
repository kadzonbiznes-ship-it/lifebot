/*
 * Decompiled with CFR 0.152.
 */
package java.io;

import java.io.File;

@FunctionalInterface
public interface FilenameFilter {
    public boolean accept(File var1, String var2);
}

