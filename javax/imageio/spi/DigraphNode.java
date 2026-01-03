/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.spi;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

class DigraphNode<E>
implements Cloneable,
Serializable {
    private static final long serialVersionUID = 5308261378582246841L;
    protected E data;
    protected Set<DigraphNode<E>> outNodes = new HashSet<DigraphNode<E>>();
    protected int inDegree = 0;
    private Set<DigraphNode<E>> inNodes = new HashSet<DigraphNode<E>>();

    public DigraphNode(E data) {
        this.data = data;
    }

    public E getData() {
        return this.data;
    }

    public Iterator<DigraphNode<E>> getOutNodes() {
        return this.outNodes.iterator();
    }

    public boolean addEdge(DigraphNode<E> node) {
        if (this.outNodes.contains(node)) {
            return false;
        }
        this.outNodes.add(node);
        node.inNodes.add(this);
        node.incrementInDegree();
        return true;
    }

    public boolean hasEdge(DigraphNode<E> node) {
        return this.outNodes.contains(node);
    }

    public boolean removeEdge(DigraphNode<E> node) {
        if (!this.outNodes.contains(node)) {
            return false;
        }
        this.outNodes.remove(node);
        node.inNodes.remove(this);
        node.decrementInDegree();
        return true;
    }

    public void dispose() {
        Object[] inNodesArray = this.inNodes.toArray();
        for (int i = 0; i < inNodesArray.length; ++i) {
            DigraphNode node = (DigraphNode)inNodesArray[i];
            node.removeEdge(this);
        }
        Object[] outNodesArray = this.outNodes.toArray();
        for (int i = 0; i < outNodesArray.length; ++i) {
            DigraphNode node = (DigraphNode)outNodesArray[i];
            this.removeEdge(node);
        }
    }

    public int getInDegree() {
        return this.inDegree;
    }

    private void incrementInDegree() {
        ++this.inDegree;
    }

    private void decrementInDegree() {
        --this.inDegree;
    }
}

