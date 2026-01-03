/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;

public class FPixa
extends Structure {
    public int n;
    public int nalloc;
    public int refcount;
    public PointerByReference fpix;

    public FPixa() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("n", "nalloc", "refcount", "fpix");
    }

    public FPixa(int n, int n2, int n3, PointerByReference pointerByReference) {
        this.n = n;
        this.nalloc = n2;
        this.refcount = n3;
        this.fpix = pointerByReference;
    }

    public FPixa(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

