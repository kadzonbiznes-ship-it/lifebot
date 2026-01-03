/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class L_Rch
extends Structure {
    public int index;
    public float score;
    public Pointer text;
    public int sample;
    public int xloc;
    public int yloc;
    public int width;

    public L_Rch() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("index", "score", "text", "sample", "xloc", "yloc", "width");
    }

    public L_Rch(int n, float f, Pointer pointer, int n2, int n3, int n4, int n5) {
        this.index = n;
        this.score = f;
        this.text = pointer;
        this.sample = n2;
        this.xloc = n3;
        this.yloc = n4;
        this.width = n5;
    }

    public L_Rch(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

