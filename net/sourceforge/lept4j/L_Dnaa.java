/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;

public class L_Dnaa
extends Structure {
    public int nalloc;
    public int n;
    public PointerByReference dna;

    public L_Dnaa() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("nalloc", "n", "dna");
    }

    public L_Dnaa(int n, int n2, PointerByReference pointerByReference) {
        this.nalloc = n;
        this.n = n2;
        this.dna = pointerByReference;
    }

    public L_Dnaa(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

