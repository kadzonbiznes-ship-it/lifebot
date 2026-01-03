/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.spi;

import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.imageio.spi.DigraphNode;
import javax.imageio.spi.PartialOrderIterator;

class PartiallyOrderedSet<E>
extends AbstractSet<E> {
    private Map<E, DigraphNode<E>> poNodes = new HashMap<E, DigraphNode<E>>();
    private Set<E> nodes = this.poNodes.keySet();

    @Override
    public int size() {
        return this.nodes.size();
    }

    @Override
    public boolean contains(Object o) {
        return this.nodes.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return new PartialOrderIterator<E>(this.poNodes.values().iterator());
    }

    @Override
    public boolean add(E o) {
        if (this.nodes.contains(o)) {
            return false;
        }
        DigraphNode<E> node = new DigraphNode<E>(o);
        this.poNodes.put(o, node);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        DigraphNode<E> node = this.poNodes.get(o);
        if (node == null) {
            return false;
        }
        this.poNodes.remove(o);
        node.dispose();
        return true;
    }

    @Override
    public void clear() {
        this.poNodes.clear();
    }

    public boolean setOrdering(E first, E second) {
        DigraphNode<E> firstPONode = this.poNodes.get(first);
        DigraphNode<E> secondPONode = this.poNodes.get(second);
        secondPONode.removeEdge(firstPONode);
        return firstPONode.addEdge(secondPONode);
    }

    public boolean unsetOrdering(E first, E second) {
        DigraphNode<E> secondPONode;
        DigraphNode<E> firstPONode = this.poNodes.get(first);
        return firstPONode.removeEdge(secondPONode = this.poNodes.get(second)) || secondPONode.removeEdge(firstPONode);
    }

    public boolean hasOrdering(E preferred, E other) {
        DigraphNode<E> preferredPONode = this.poNodes.get(preferred);
        DigraphNode<E> otherPONode = this.poNodes.get(other);
        return preferredPONode.hasEdge(otherPONode);
    }
}

