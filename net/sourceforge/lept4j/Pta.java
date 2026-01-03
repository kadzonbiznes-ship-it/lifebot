/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.FloatByReference;
import java.util.Arrays;
import java.util.List;

public class Pta
extends Structure {
    public int n;
    public int nalloc;
    public int refcount;
    public FloatByReference x;
    public FloatByReference y;

    public Pta() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("n", "nalloc", "refcount", "x", "y");
    }

    public Pta(int n, int n2, int n3, FloatByReference floatByReference, FloatByReference floatByReference2) {
        this.n = n;
        this.nalloc = n2;
        this.refcount = n3;
        this.x = floatByReference;
        this.y = floatByReference2;
    }

    public Pta(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

