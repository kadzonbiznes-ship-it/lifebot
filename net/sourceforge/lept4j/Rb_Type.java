/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Union;

public class Rb_Type
extends Union {
    public long itype;
    public long utype;
    public double ftype;
    public Pointer ptype;

    public Rb_Type() {
    }

    public Rb_Type(long l) {
        this.utype = this.itype = l;
        this.setType(Long.TYPE);
    }

    public Rb_Type(double d) {
        this.ftype = d;
        this.setType(Double.TYPE);
    }

    public Rb_Type(Pointer pointer) {
        this.ptype = pointer;
        this.setType(Pointer.class);
    }

    public static class ByValue
    extends Rb_Type
    implements Structure.ByValue {
    }
}

