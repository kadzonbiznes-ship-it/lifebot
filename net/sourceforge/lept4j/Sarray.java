/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;

public class Sarray
extends Structure {
    public int nalloc;
    public int n;
    public int refcount;
    public PointerByReference array;

    public Sarray() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("nalloc", "n", "refcount", "array");
    }

    public Sarray(int n, int n2, int n3, PointerByReference pointerByReference) {
        this.nalloc = n;
        this.n = n2;
        this.refcount = n3;
        this.array = pointerByReference;
    }

    public Sarray(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

