/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.FloatByReference;
import java.util.Arrays;
import java.util.List;

public class FPix
extends Structure {
    public int w;
    public int h;
    public int wpl;
    public int refcount;
    public int xres;
    public int yres;
    public FloatByReference data;

    public FPix() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("w", "h", "wpl", "refcount", "xres", "yres", "data");
    }

    public FPix(int n, int n2, int n3, int n4, int n5, int n6, FloatByReference floatByReference) {
        this.w = n;
        this.h = n2;
        this.wpl = n3;
        this.refcount = n4;
        this.xres = n5;
        this.yres = n6;
        this.data = floatByReference;
    }

    public FPix(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

