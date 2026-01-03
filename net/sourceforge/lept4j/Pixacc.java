/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.lept4j.Pix$ByReference
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.lept4j.Pix;

public class Pixacc
extends Structure {
    public int w;
    public int h;
    public int offset;
    public Pix.ByReference pix;

    public Pixacc() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("w", "h", "offset", "pix");
    }

    public Pixacc(int n, int n2, int n3, Pix.ByReference byReference) {
        this.w = n;
        this.h = n2;
        this.offset = n3;
        this.pix = byReference;
    }

    public Pixacc(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

