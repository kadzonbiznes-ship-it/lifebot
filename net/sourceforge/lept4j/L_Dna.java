/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.DoubleByReference;
import java.util.Arrays;
import java.util.List;

public class L_Dna
extends Structure {
    public int nalloc;
    public int n;
    public int refcount;
    public double startx;
    public double delx;
    public DoubleByReference array;

    public L_Dna() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("nalloc", "n", "refcount", "startx", "delx", "array");
    }

    public L_Dna(int n, int n2, int n3, double d, double d2, DoubleByReference doubleByReference) {
        this.nalloc = n;
        this.n = n2;
        this.refcount = n3;
        this.startx = d;
        this.delx = d2;
        this.array = doubleByReference;
    }

    public L_Dna(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

