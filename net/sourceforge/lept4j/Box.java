/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class Box
extends Structure {
    public int x;
    public int y;
    public int w;
    public int h;
    public int refcount;

    public Box() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("x", "y", "w", "h", "refcount");
    }

    public Box(int n, int n2, int n3, int n4, int n5) {
        this.x = n;
        this.y = n2;
        this.w = n3;
        this.h = n4;
        this.refcount = n5;
    }

    public Box(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

