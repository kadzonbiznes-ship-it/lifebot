/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.FloatByReference;
import java.util.Arrays;
import java.util.List;

public class Numa
extends Structure {
    public int nalloc;
    public int n;
    public int refcount;
    public float startx;
    public float delx;
    public FloatByReference array;

    public Numa() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("nalloc", "n", "refcount", "startx", "delx", "array");
    }

    public Numa(int n, int n2, int n3, float f, float f2, FloatByReference floatByReference) {
        this.nalloc = n;
        this.n = n2;
        this.refcount = n3;
        this.startx = f;
        this.delx = f2;
        this.array = floatByReference;
    }

    public Numa(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

