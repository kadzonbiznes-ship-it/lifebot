/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.lept4j.L_Rbtree_Node$ByReference
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.lept4j.L_Rbtree_Node;
import net.sourceforge.lept4j.Rb_Type;

public class L_Rbtree_Node
extends Structure {
    public Rb_Type key;
    public Rb_Type value;
    public ByReference left;
    public ByReference right;
    public ByReference parent;
    public int color;

    public L_Rbtree_Node() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("key", "value", "left", "right", "parent", "color");
    }

    public L_Rbtree_Node(Rb_Type rb_Type, Rb_Type rb_Type2, ByReference byReference, ByReference byReference2, ByReference byReference3, int n) {
        this.key = rb_Type;
        this.value = rb_Type2;
        this.left = byReference;
        this.right = byReference2;
        this.parent = byReference3;
        this.color = n;
    }

    public L_Rbtree_Node(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

