/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;

public class L_Heap
extends Structure {
    public int nalloc;
    public int n;
    public PointerByReference array;
    public int direction;

    public L_Heap() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("nalloc", "n", "array", "direction");
    }

    public L_Heap(int n, int n2, PointerByReference pointerByReference, int n3) {
        this.nalloc = n;
        this.n = n2;
        this.array = pointerByReference;
        this.direction = n3;
    }

    public L_Heap(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

