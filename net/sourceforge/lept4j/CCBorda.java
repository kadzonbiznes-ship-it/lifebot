/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.lept4j.Pix$ByReference
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.lept4j.Pix;

public class CCBorda
extends Structure {
    public Pix.ByReference pix;
    public int w;
    public int h;
    public int n;
    public int nalloc;
    public PointerByReference ccb;

    public CCBorda() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("pix", "w", "h", "n", "nalloc", "ccb");
    }

    public CCBorda(Pix.ByReference byReference, int n, int n2, int n3, int n4, PointerByReference pointerByReference) {
        this.pix = byReference;
        this.w = n;
        this.h = n2;
        this.n = n3;
        this.nalloc = n4;
        this.ccb = pointerByReference;
    }

    public CCBorda(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

