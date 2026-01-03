/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.lept4j.Pixa$ByReference
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.lept4j.Pixa;

public class L_Bmf
extends Structure {
    public Pixa.ByReference pixa;
    public int size;
    public Pointer directory;
    public int baseline1;
    public int baseline2;
    public int baseline3;
    public int lineheight;
    public int kernwidth;
    public int spacewidth;
    public int vertlinesep;
    public IntByReference fonttab;
    public IntByReference baselinetab;

    public L_Bmf() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("pixa", "size", "directory", "baseline1", "baseline2", "baseline3", "lineheight", "kernwidth", "spacewidth", "vertlinesep", "fonttab", "baselinetab");
    }

    public L_Bmf(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

