/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.lept4j.Sarray$ByReference
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.lept4j.Sarray;

public class L_StrCode
extends Structure {
    public int fileno;
    public int ifunc;
    public Sarray.ByReference function;
    public Sarray.ByReference data;
    public Sarray.ByReference descr;
    public int n;

    public L_StrCode() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("fileno", "ifunc", "function", "data", "descr", "n");
    }

    public L_StrCode(int n, int n2, Sarray.ByReference byReference, Sarray.ByReference byReference2, Sarray.ByReference byReference3, int n3) {
        this.fileno = n;
        this.ifunc = n2;
        this.function = byReference;
        this.data = byReference2;
        this.descr = byReference3;
        this.n = n3;
    }

    public L_StrCode(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

