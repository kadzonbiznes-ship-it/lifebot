/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.spi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import javax.imageio.spi.DigraphNode;

class PartialOrderIterator<E>
implements Iterator<E> {
    LinkedList<DigraphNode<E>> zeroList = new LinkedList();
    Map<DigraphNode<E>, Integer> inDegrees = new HashMap<DigraphNode<E>, Integer>();

    public PartialOrderIterator(Iterator<DigraphNode<E>> iter) {
        while (iter.hasNext()) {
            DigraphNode<E> node = iter.next();
            int inDegree = node.getInDegree();
            this.inDegrees.put(node, inDegree);
            if (inDegree != 0) continue;
            this.zeroList.add(node);
        }
    }

    @Override
    public boolean hasNext() {
        return !this.zeroList.isEmpty();
    }

    @Override
    public E next() {
        DigraphNode<E> first = this.zeroList.removeFirst();
        Iterator<DigraphNode<E>> outNodes = first.getOutNodes();
        while (outNodes.hasNext()) {
            DigraphNode<E> node = outNodes.next();
            int inDegree = this.inDegrees.get(node) - 1;
            this.inDegrees.put(node, inDegree);
            if (inDegree != 0) continue;
            this.zeroList.add(node);
        }
        return first.getData();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

