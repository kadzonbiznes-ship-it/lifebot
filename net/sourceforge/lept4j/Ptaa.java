/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;

public class Ptaa
extends Structure {
    public int n;
    public int nalloc;
    public PointerByReference pta;

    public Ptaa() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("n", "nalloc", "pta");
    }

    public Ptaa(int n, int n2, PointerByReference pointerByReference) {
        this.n = n;
        this.nalloc = n2;
        this.pta = pointerByReference;
    }

    public Ptaa(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

