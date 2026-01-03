/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.tree;

import java.util.Enumeration;

public interface TreeNode {
    public TreeNode getChildAt(int var1);

    public int getChildCount();

    public TreeNode getParent();

    public int getIndex(TreeNode var1);

    public boolean getAllowsChildren();

    public boolean isLeaf();

    public Enumeration<? extends TreeNode> children();
}

