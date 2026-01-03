/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.lept4j.DoubleLinkedList$ByReference
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.lept4j.DoubleLinkedList;

public class DoubleLinkedList
extends Structure {
    public ByReference prev;
    public ByReference next;
    public Pointer data;

    public DoubleLinkedList() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("prev", "next", "data");
    }

    public DoubleLinkedList(ByReference byReference, ByReference byReference2, Pointer pointer) {
        this.prev = byReference;
        this.next = byReference2;
        this.data = pointer;
    }

    public DoubleLinkedList(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

