/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.lept4j.Boxa$ByReference
 *  net.sourceforge.lept4j.PixComp$ByReference
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.lept4j.Boxa;
import net.sourceforge.lept4j.PixComp;

public class PixaComp
extends Structure {
    public int n;
    public int nalloc;
    public int offset;
    public PixComp.ByReference[] pixc;
    public Boxa.ByReference boxa;

    public PixaComp() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("n", "nalloc", "offset", "pixc", "boxa");
    }

    public PixaComp(int n, int n2, int n3, PixComp.ByReference[] byReferenceArray, Boxa.ByReference byReference) {
        this.n = n;
        this.nalloc = n2;
        this.offset = n3;
        if (byReferenceArray.length != this.pixc.length) {
            throw new IllegalArgumentException("Wrong array size !");
        }
        this.pixc = byReferenceArray;
        this.boxa = byReference;
    }

    public PixaComp(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

