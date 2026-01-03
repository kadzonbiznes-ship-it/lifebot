/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.lept4j.Numa$ByReference
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.lept4j.Numa;

public class L_Dewarpa
extends Structure {
    public int nalloc;
    public int maxpage;
    public PointerByReference dewarp;
    public PointerByReference dewarpcache;
    public Numa.ByReference namodels;
    public Numa.ByReference napages;
    public int redfactor;
    public int sampling;
    public int minlines;
    public int maxdist;
    public int max_linecurv;
    public int min_diff_linecurv;
    public int max_diff_linecurv;
    public int max_edgeslope;
    public int max_edgecurv;
    public int max_diff_edgecurv;
    public int useboth;
    public int check_columns;
    public int modelsready;

    public L_Dewarpa() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("nalloc", "maxpage", "dewarp", "dewarpcache", "namodels", "napages", "redfactor", "sampling", "minlines", "maxdist", "max_linecurv", "min_diff_linecurv", "max_diff_linecurv", "max_edgeslope", "max_edgecurv", "max_diff_edgecurv", "useboth", "check_columns", "modelsready");
    }

    public L_Dewarpa(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

