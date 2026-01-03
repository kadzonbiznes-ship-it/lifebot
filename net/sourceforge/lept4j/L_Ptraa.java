/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;

public class L_Ptraa
extends Structure {
    public int nalloc;
    public PointerByReference ptra;

    public L_Ptraa() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("nalloc", "ptra");
    }

    public L_Ptraa(int n, PointerByReference pointerByReference) {
        this.nalloc = n;
        this.ptra = pointerByReference;
    }

    public L_Ptraa(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

