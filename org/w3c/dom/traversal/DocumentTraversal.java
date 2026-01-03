/*
 * Decompiled with CFR 0.152.
 */
package org.w3c.dom.traversal;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.traversal.TreeWalker;

public interface DocumentTraversal {
    public NodeIterator createNodeIterator(Node var1, int var2, NodeFilter var3, boolean var4) throws DOMException;

    public TreeWalker createTreeWalker(Node var1, int var2, NodeFilter var3, boolean var4) throws DOMException;
}

