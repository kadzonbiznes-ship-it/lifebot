/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.lept4j.FPix$ByReference
 *  net.sourceforge.lept4j.L_Dewarpa$ByReference
 *  net.sourceforge.lept4j.Numa$ByReference
 *  net.sourceforge.lept4j.Pix$ByReference
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.lept4j.FPix;
import net.sourceforge.lept4j.L_Dewarpa;
import net.sourceforge.lept4j.Numa;
import net.sourceforge.lept4j.Pix;

public class L_Dewarp
extends Structure {
    public L_Dewarpa.ByReference dewa;
    public Pix.ByReference pixs;
    public FPix.ByReference sampvdispar;
    public FPix.ByReference samphdispar;
    public FPix.ByReference sampydispar;
    public FPix.ByReference fullvdispar;
    public FPix.ByReference fullhdispar;
    public FPix.ByReference fullydispar;
    public Numa.ByReference namidys;
    public Numa.ByReference nacurves;
    public int w;
    public int h;
    public int pageno;
    public int sampling;
    public int redfactor;
    public int minlines;
    public int nlines;
    public int mincurv;
    public int maxcurv;
    public int leftslope;
    public int rightslope;
    public int leftcurv;
    public int rightcurv;
    public int nx;
    public int ny;
    public int hasref;
    public int refpage;
    public int vsuccess;
    public int hsuccess;
    public int ysuccess;
    public int vvalid;
    public int hvalid;
    public int skip_horiz;
    public int debug;

    public L_Dewarp() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("dewa", "pixs", "sampvdispar", "samphdispar", "sampydispar", "fullvdispar", "fullhdispar", "fullydispar", "namidys", "nacurves", "w", "h", "pageno", "sampling", "redfactor", "minlines", "nlines", "mincurv", "maxcurv", "leftslope", "rightslope", "leftcurv", "rightcurv", "nx", "ny", "hasref", "refpage", "vsuccess", "hsuccess", "ysuccess", "vvalid", "hvalid", "skip_horiz", "debug");
    }

    public L_Dewarp(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

