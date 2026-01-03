/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.DoubleByReference;
import java.util.Arrays;
import java.util.List;

public class DPix
extends Structure {
    public int w;
    public int h;
    public int wpl;
    public int refcount;
    public int xres;
    public int yres;
    public DoubleByReference data;

    public DPix() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("w", "h", "wpl", "refcount", "xres", "yres", "data");
    }

    public DPix(int n, int n2, int n3, int n4, int n5, int n6, DoubleByReference doubleByReference) {
        this.w = n;
        this.h = n2;
        this.wpl = n3;
        this.refcount = n4;
        this.xres = n5;
        this.yres = n6;
        this.data = doubleByReference;
    }

    public DPix(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

