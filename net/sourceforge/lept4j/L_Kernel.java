/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;

public class L_Kernel
extends Structure {
    public int sy;
    public int sx;
    public int cy;
    public int cx;
    public PointerByReference data;

    public L_Kernel() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("sy", "sx", "cy", "cx", "data");
    }

    public L_Kernel(int n, int n2, int n3, int n4, PointerByReference pointerByReference) {
        this.sy = n;
        this.sx = n2;
        this.cy = n3;
        this.cx = n4;
        this.data = pointerByReference;
    }

    public L_Kernel(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

