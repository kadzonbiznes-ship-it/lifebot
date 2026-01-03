/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;

public class L_Ptra
extends Structure {
    public int nalloc;
    public int imax;
    public int nactual;
    public PointerByReference array;

    public L_Ptra() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("nalloc", "imax", "nactual", "array");
    }

    public L_Ptra(int n, int n2, int n3, PointerByReference pointerByReference) {
        this.nalloc = n;
        this.imax = n2;
        this.nactual = n3;
        this.array = pointerByReference;
    }

    public L_Ptra(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

