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

public class L_Rbtree
extends Structure {
    public L_Rbtree_Node.ByReference root;
    public int keytype;

    public L_Rbtree() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("root", "keytype");
    }

    public L_Rbtree(L_Rbtree_Node.ByReference byReference, int n) {
        this.root = byReference;
        this.keytype = n;
    }

    public L_Rbtree(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

