/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.DoubleByReference;
import java.util.Arrays;
import java.util.List;

public class L_DnaHash
extends Structure {
    public int nbuckets;
    public int initsize;
    public DoubleByReference dna;

    public L_DnaHash() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("nbuckets", "initsize", "dna");
    }

    public L_DnaHash(int n, int n2, DoubleByReference doubleByReference) {
        this.nbuckets = n;
        this.initsize = n2;
        this.dna = doubleByReference;
    }

    public L_DnaHash(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

