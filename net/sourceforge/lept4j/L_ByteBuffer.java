/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class L_ByteBuffer
extends Structure {
    public int nalloc;
    public int n;
    public int nwritten;
    public Pointer array;

    public L_ByteBuffer() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("nalloc", "n", "nwritten", "array");
    }

    public L_ByteBuffer(int n, int n2, int n3, Pointer pointer) {
        this.nalloc = n;
        this.n = n2;
        this.nwritten = n3;
        this.array = pointer;
    }

    public L_ByteBuffer(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

