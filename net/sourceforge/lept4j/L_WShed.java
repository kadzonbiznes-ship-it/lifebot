/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.lept4j.Numa$ByReference
 *  net.sourceforge.lept4j.Pix$ByReference
 *  net.sourceforge.lept4j.Pixa$ByReference
 *  net.sourceforge.lept4j.Pta$ByReference
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.lept4j.Numa;
import net.sourceforge.lept4j.Pix;
import net.sourceforge.lept4j.Pixa;
import net.sourceforge.lept4j.Pta;

public class L_WShed
extends Structure {
    public Pix.ByReference pixs;
    public Pix.ByReference pixm;
    public int mindepth;
    public Pix.ByReference pixlab;
    public Pix.ByReference pixt;
    public PointerByReference lines8;
    public PointerByReference linem1;
    public PointerByReference linelab32;
    public PointerByReference linet1;
    public Pixa.ByReference pixad;
    public Pta.ByReference ptas;
    public Numa.ByReference nasi;
    public Numa.ByReference nash;
    public Numa.ByReference namh;
    public Numa.ByReference nalevels;
    public int nseeds;
    public int nother;
    public IntByReference lut;
    public Numa.ByReference[] links;
    public int arraysize;
    public int debug;

    public L_WShed() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("pixs", "pixm", "mindepth", "pixlab", "pixt", "lines8", "linem1", "linelab32", "linet1", "pixad", "ptas", "nasi", "nash", "namh", "nalevels", "nseeds", "nother", "lut", "links", "arraysize", "debug");
    }

    public L_WShed(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

