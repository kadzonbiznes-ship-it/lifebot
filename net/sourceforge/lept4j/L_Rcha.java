/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.lept4j.Numa$ByReference
 *  net.sourceforge.lept4j.Sarray$ByReference
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.lept4j.Numa;
import net.sourceforge.lept4j.Sarray;

public class L_Rcha
extends Structure {
    public Numa.ByReference naindex;
    public Numa.ByReference nascore;
    public Sarray.ByReference satext;
    public Numa.ByReference nasample;
    public Numa.ByReference naxloc;
    public Numa.ByReference nayloc;
    public Numa.ByReference nawidth;

    public L_Rcha() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("naindex", "nascore", "satext", "nasample", "naxloc", "nayloc", "nawidth");
    }

    public L_Rcha(Numa.ByReference byReference, Numa.ByReference byReference2, Sarray.ByReference byReference3, Numa.ByReference byReference4, Numa.ByReference byReference5, Numa.ByReference byReference6, Numa.ByReference byReference7) {
        this.naindex = byReference;
        this.nascore = byReference2;
        this.satext = byReference3;
        this.nasample = byReference4;
        this.naxloc = byReference5;
        this.nayloc = byReference6;
        this.nawidth = byReference7;
    }

    public L_Rcha(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

