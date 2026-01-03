/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.lept4j.Pix$ByReference
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.lept4j.Pix;

public class PixTiling
extends Structure {
    public Pix.ByReference pix;
    public int nx;
    public int ny;
    public int w;
    public int h;
    public int xoverlap;
    public int yoverlap;
    public int strip;

    public PixTiling() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("pix", "nx", "ny", "w", "h", "xoverlap", "yoverlap", "strip");
    }

    public PixTiling(Pix.ByReference byReference, int n, int n2, int n3, int n4, int n5, int n6, int n7) {
        this.pix = byReference;
        this.nx = n;
        this.ny = n2;
        this.w = n3;
        this.h = n4;
        this.xoverlap = n5;
        this.yoverlap = n6;
        this.strip = n7;
    }

    public PixTiling(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

