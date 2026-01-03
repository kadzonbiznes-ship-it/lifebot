/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.lept4j.L_DnaHash$ByReference
 *  net.sourceforge.lept4j.Numa$ByReference
 *  net.sourceforge.lept4j.Pixa$ByReference
 *  net.sourceforge.lept4j.Pixaa$ByReference
 *  net.sourceforge.lept4j.Pta$ByReference
 *  net.sourceforge.lept4j.Sarray$ByReference
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.lept4j.L_DnaHash;
import net.sourceforge.lept4j.Numa;
import net.sourceforge.lept4j.Pixa;
import net.sourceforge.lept4j.Pixaa;
import net.sourceforge.lept4j.Pta;
import net.sourceforge.lept4j.Sarray;

public class JbClasser
extends Structure {
    public Sarray.ByReference safiles;
    public int method;
    public int components;
    public int maxwidth;
    public int maxheight;
    public int npages;
    public int baseindex;
    public Numa.ByReference nacomps;
    public int sizehaus;
    public float rankhaus;
    public float thresh;
    public float weightfactor;
    public Numa.ByReference naarea;
    public int w;
    public int h;
    public int nclass;
    public int keep_pixaa;
    public Pixaa.ByReference pixaa;
    public Pixa.ByReference pixat;
    public Pixa.ByReference pixatd;
    public L_DnaHash.ByReference dahash;
    public Numa.ByReference nafgt;
    public Pta.ByReference ptac;
    public Pta.ByReference ptact;
    public Numa.ByReference naclass;
    public Numa.ByReference napage;
    public Pta.ByReference ptaul;
    public Pta.ByReference ptall;

    public JbClasser() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("safiles", "method", "components", "maxwidth", "maxheight", "npages", "baseindex", "nacomps", "sizehaus", "rankhaus", "thresh", "weightfactor", "naarea", "w", "h", "nclass", "keep_pixaa", "pixaa", "pixat", "pixatd", "dahash", "nafgt", "ptac", "ptact", "naclass", "napage", "ptaul", "ptall");
    }

    public JbClasser(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

