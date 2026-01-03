/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.lept4j.Boxa$ByReference
 *  net.sourceforge.lept4j.L_Dnaa$ByReference
 *  net.sourceforge.lept4j.Numaa$ByReference
 *  net.sourceforge.lept4j.Pix$ByReference
 *  net.sourceforge.lept4j.Pixa$ByReference
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.lept4j.Boxa;
import net.sourceforge.lept4j.L_Dnaa;
import net.sourceforge.lept4j.Numaa;
import net.sourceforge.lept4j.Pix;
import net.sourceforge.lept4j.Pixa;

public class L_Colorfill
extends Structure {
    public Pix.ByReference pixs;
    public Pix.ByReference pixst;
    public int nx;
    public int ny;
    public int tw;
    public int th;
    public int minarea;
    public Boxa.ByReference boxas;
    public Pixa.ByReference pixas;
    public Pixa.ByReference pixam;
    public Numaa.ByReference naa;
    public L_Dnaa.ByReference dnaa;
    public Pixa.ByReference pixadb;

    public L_Colorfill() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("pixs", "pixst", "nx", "ny", "tw", "th", "minarea", "boxas", "pixas", "pixam", "naa", "dnaa", "pixadb");
    }

    public L_Colorfill(Pointer pointer) {
        super(pointer);
    }
}

