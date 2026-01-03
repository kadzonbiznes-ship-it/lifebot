/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;

public class Sela
extends Structure {
    public int n;
    public int nalloc;
    public PointerByReference sel;

    public Sela() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("n", "nalloc", "sel");
    }

    public Sela(int n, int n2, PointerByReference pointerByReference) {
        this.n = n;
        this.nalloc = n2;
        this.sel = pointerByReference;
    }

    public Sela(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

