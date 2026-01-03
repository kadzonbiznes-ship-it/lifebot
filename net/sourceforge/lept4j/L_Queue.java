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

public class L_Queue
extends Structure {
    public int nalloc;
    public int nhead;
    public int nelem;
    public PointerByReference array;
    public L_Stack.ByReference stack;

    public L_Queue() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("nalloc", "nhead", "nelem", "array", "stack");
    }

    public L_Queue(int n, int n2, int n3, PointerByReference pointerByReference, L_Stack.ByReference byReference) {
        this.nalloc = n;
        this.nhead = n2;
        this.nelem = n3;
        this.array = pointerByReference;
        this.stack = byReference;
    }

    public L_Queue(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

