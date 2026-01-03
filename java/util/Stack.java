/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.util.EmptyStackException;
import java.util.Vector;

public class Stack<E>
extends Vector<E> {
    private static final long serialVersionUID = 1224463164541339165L;

    public E push(E item) {
        this.addElement(item);
        return item;
    }

    public synchronized E pop() {
        int len = this.size();
        E obj = this.peek();
        this.removeElementAt(len - 1);
        return obj;
    }

    public synchronized E peek() {
        int len = this.size();
        if (len == 0) {
            throw new EmptyStackException();
        }
        return this.elementAt(len - 1);
    }

    public boolean empty() {
        return this.size() == 0;
    }

    public synchronized int search(Object o) {
        int i = this.lastIndexOf(o);
        if (i >= 0) {
            return this.size() - i;
        }
        return -1;
    }
}

