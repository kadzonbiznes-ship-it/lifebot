/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class PixColormap
extends Structure {
    public Pointer array;
    public int depth;
    public int nalloc;
    public int n;

    public PixColormap() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("array", "depth", "nalloc", "n");
    }

    public PixColormap(Pointer pointer, int n, int n2, int n3) {
        this.array = pointer;
        this.depth = n;
        this.nalloc = n2;
        this.n = n3;
    }

    public PixColormap(Pointer pointer) {
        super(pointer);
        this.read();
    }

    public static class ByReference
    extends PixColormap
    implements Structure.ByReference {
    }
}

