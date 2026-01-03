/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.lept4j.Boxa$ByReference
 *  net.sourceforge.lept4j.Numaa$ByReference
 *  net.sourceforge.lept4j.Pix$ByReference
 *  net.sourceforge.lept4j.Pta$ByReference
 *  net.sourceforge.lept4j.Ptaa$ByReference
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.lept4j.Boxa;
import net.sourceforge.lept4j.Numaa;
import net.sourceforge.lept4j.Pix;
import net.sourceforge.lept4j.Pta;
import net.sourceforge.lept4j.Ptaa;

public class CCBord
extends Structure {
    public Pix.ByReference pix;
    public Boxa.ByReference boxa;
    public Pta.ByReference start;
    public int refcount;
    public Ptaa.ByReference local;
    public Ptaa.ByReference global;
    public Numaa.ByReference step;
    public Pta.ByReference splocal;
    public Pta.ByReference spglobal;

    public CCBord() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("pix", "boxa", "start", "refcount", "local", "global", "step", "splocal", "spglobal");
    }

    public CCBord(Pix.ByReference byReference, Boxa.ByReference byReference2, Pta.ByReference byReference3, int n, Ptaa.ByReference byReference4, Ptaa.ByReference byReference5, Numaa.ByReference byReference6, Pta.ByReference byReference7, Pta.ByReference byReference8) {
        this.pix = byReference;
        this.boxa = byReference2;
        this.start = byReference3;
        this.refcount = n;
        this.local = byReference4;
        this.global = byReference5;
        this.step = byReference6;
        this.splocal = byReference7;
        this.spglobal = byReference8;
    }

    public CCBord(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

