/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.lept4j.PixColormap;

public class Pix
extends Structure {
    public int w;
    public int h;
    public int d;
    public int spp;
    public int wpl;
    public int refcount;
    public int xres;
    public int yres;
    public int informat;
    public int special;
    public Pointer text;
    public PixColormap.ByReference colormap;
    public IntByReference data;

    public Pix() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("w", "h", "d", "spp", "wpl", "refcount", "xres", "yres", "informat", "special", "text", "colormap", "data");
    }

    public Pix(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

