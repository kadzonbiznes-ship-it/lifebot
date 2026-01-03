/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.lept4j.Numa$ByReference
 *  net.sourceforge.lept4j.Pix$ByReference
 *  net.sourceforge.lept4j.Pta$ByReference
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.lept4j.Numa;
import net.sourceforge.lept4j.Pix;
import net.sourceforge.lept4j.Pta;

public class JbData
extends Structure {
    public Pix.ByReference pix;
    public int npages;
    public int w;
    public int h;
    public int nclass;
    public int latticew;
    public int latticeh;
    public Numa.ByReference naclass;
    public Numa.ByReference napage;
    public Pta.ByReference ptaul;

    public JbData() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("pix", "npages", "w", "h", "nclass", "latticew", "latticeh", "naclass", "napage", "ptaul");
    }

    public JbData(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

