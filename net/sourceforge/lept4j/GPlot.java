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

public class GPlot
extends Structure {
    public Pointer rootname;
    public Pointer cmdname;
    public Sarray.ByReference cmddata;
    public Sarray.ByReference datanames;
    public Sarray.ByReference plotdata;
    public Sarray.ByReference plotlabels;
    public Numa.ByReference plotstyles;
    public int nplots;
    public Pointer outname;
    public int outformat;
    public int scaling;
    public Pointer title;
    public Pointer xlabel;
    public Pointer ylabel;

    public GPlot() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("rootname", "cmdname", "cmddata", "datanames", "plotdata", "plotlabels", "plotstyles", "nplots", "outname", "outformat", "scaling", "title", "xlabel", "ylabel");
    }

    public GPlot(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

