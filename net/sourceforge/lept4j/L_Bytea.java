/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class L_Bytea
extends Structure {
    public NativeSize nalloc;
    public NativeSize size;
    public int refcount;
    public Pointer data;

    public L_Bytea() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("nalloc", "size", "refcount", "data");
    }

    public L_Bytea(NativeSize nativeSize, NativeSize nativeSize2, int n, Pointer pointer) {
        this.nalloc = nativeSize;
        this.size = nativeSize2;
        this.refcount = n;
        this.data = pointer;
    }

    public L_Bytea(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

