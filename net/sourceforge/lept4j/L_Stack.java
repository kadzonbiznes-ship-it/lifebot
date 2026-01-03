/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.lept4j.L_Stack$ByReference
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.lept4j.L_Stack;

public class L_Stack
extends Structure {
    public int nalloc;
    public int n;
    public PointerByReference array;
    public ByReference auxstack;

    public L_Stack() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("nalloc", "n", "array", "auxstack");
    }

    public L_Stack(int n, int n2, PointerByReference pointerByReference, ByReference byReference) {
        this.nalloc = n;
        this.n = n2;
        this.array = pointerByReference;
        this.auxstack = byReference;
    }

    public L_Stack(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

