/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class L_Compressed_Data
extends Structure {
    public int type;
    public Pointer datacomp;
    public NativeSize nbytescomp;
    public Pointer data85;
    public NativeSize nbytes85;
    public Pointer cmapdata85;
    public Pointer cmapdatahex;
    public int ncolors;
    public int w;
    public int h;
    public int bps;
    public int spp;
    public int minisblack;
    public int predictor;
    public NativeSize nbytes;
    public int res;

    public L_Compressed_Data() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("type", "datacomp", "nbytescomp", "data85", "nbytes85", "cmapdata85", "cmapdatahex", "ncolors", "w", "h", "bps", "spp", "minisblack", "predictor", "nbytes", "res");
    }

    public L_Compressed_Data(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

