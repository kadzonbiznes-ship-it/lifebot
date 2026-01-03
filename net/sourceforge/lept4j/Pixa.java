/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.lept4j.Boxa$ByReference
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.lept4j.Boxa;

public class Pixa
extends Structure {
    public int n;
    public int nalloc;
    public int refcount;
    public PointerByReference pix;
    public Boxa.ByReference boxa;

    public Pixa() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("n", "nalloc", "refcount", "pix", "boxa");
    }

    public Pixa(int n, int n2, int n3, PointerByReference pointerByReference, Boxa.ByReference byReference) {
        this.n = n;
        this.nalloc = n2;
        this.refcount = n3;
        this.pix = pointerByReference;
        this.boxa = byReference;
    }

    public Pixa(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

