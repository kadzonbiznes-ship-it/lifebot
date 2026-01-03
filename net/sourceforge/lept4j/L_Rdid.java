/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.lept4j.Boxa$ByReference
 *  net.sourceforge.lept4j.Numa$ByReference
 *  net.sourceforge.lept4j.Pix$ByReference
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.lept4j.Boxa;
import net.sourceforge.lept4j.Numa;
import net.sourceforge.lept4j.Pix;

public class L_Rdid
extends Structure {
    public Pix.ByReference pixs;
    public PointerByReference counta;
    public PointerByReference delya;
    public int narray;
    public int size;
    public IntByReference setwidth;
    public Numa.ByReference nasum;
    public Numa.ByReference namoment;
    public int fullarrays;
    public FloatByReference beta;
    public FloatByReference gamma;
    public FloatByReference trellisscore;
    public IntByReference trellistempl;
    public Numa.ByReference natempl;
    public Numa.ByReference naxloc;
    public Numa.ByReference nadely;
    public Numa.ByReference nawidth;
    public Boxa.ByReference boxa;
    public Numa.ByReference nascore;
    public Numa.ByReference natempl_r;
    public Numa.ByReference nasample_r;
    public Numa.ByReference naxloc_r;
    public Numa.ByReference nadely_r;
    public Numa.ByReference nawidth_r;
    public Numa.ByReference nascore_r;

    public L_Rdid() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("pixs", "counta", "delya", "narray", "size", "setwidth", "nasum", "namoment", "fullarrays", "beta", "gamma", "trellisscore", "trellistempl", "natempl", "naxloc", "nadely", "nawidth", "boxa", "nascore", "natempl_r", "nasample_r", "naxloc_r", "nadely_r", "nawidth_r", "nascore_r");
    }

    public L_Rdid(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

