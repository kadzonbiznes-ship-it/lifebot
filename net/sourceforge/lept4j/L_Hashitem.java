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

public class L_Hashitem
extends Structure {
    public long key;
    public long val;
    public int count;
    public ByReference next;

    public L_Hashitem() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("key", "val", "count", "next");
    }

    public L_Hashitem(long l, long l2, int n, ByReference byReference) {
        this.key = l;
        this.val = l2;
        this.count = n;
        this.next = byReference;
    }

    public L_Hashitem(Pointer pointer) {
        super(pointer);
    }
}

