/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.lept4j.L_Hashitem$ByReference
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.lept4j.L_Hashitem;

public class L_Hashmap
extends Structure {
    public int nitems;
    public int ntogo;
    public int maxocc;
    public L_Hashitem.ByReference[] hashtab;
    public int tabsize;

    public L_Hashmap() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("nitems", "ntogo", "maxocc", "hashtab", "tabsize");
    }

    public L_Hashmap(int n, int n2, int n3, L_Hashitem.ByReference[] byReferenceArray, int n4) {
        this.nitems = n;
        this.ntogo = n2;
        this.maxocc = n3;
        if (byReferenceArray.length != this.hashtab.length) {
            throw new IllegalArgumentException("Wrong array size !");
        }
        this.hashtab = byReferenceArray;
        this.tabsize = n4;
    }

    public L_Hashmap(Pointer pointer) {
        super(pointer);
    }
}

