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

public class Pixaa
extends Structure {
    public int n;
    public int nalloc;
    public PointerByReference pixa;
    public Boxa.ByReference boxa;

    public Pixaa() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("n", "nalloc", "pixa", "boxa");
    }

    public Pixaa(int n, int n2, PointerByReference pointerByReference, Boxa.ByReference byReference) {
        this.n = n;
        this.nalloc = n2;
        this.pixa = pointerByReference;
        this.boxa = byReference;
    }

    public Pixaa(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

