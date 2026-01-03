/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class PixComp
extends Structure {
    public int w;
    public int h;
    public int d;
    public int xres;
    public int yres;
    public int comptype;
    public Pointer text;
    public int cmapflag;
    public Pointer data;
    public NativeSize size;

    public PixComp() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("w", "h", "d", "xres", "yres", "comptype", "text", "cmapflag", "data", "size");
    }

    public PixComp(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

