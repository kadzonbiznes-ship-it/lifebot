/*
 * Decompiled with CFR 0.152.
 */
package io.netty.channel.nio;

import java.nio.channels.SelectionKey;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;

final class SelectedSelectionKeySet
extends AbstractSet<SelectionKey> {
    SelectionKey[] keys = new SelectionKey[1024];
    int size;

    SelectedSelectionKeySet() {
    }

    @Override
    public boolean add(SelectionKey o) {
        if (o == null) {
            return false;
        }
        if (this.size == this.keys.length) {
            this.increaseCapacity();
        }
        this.keys[this.size++] = o;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        SelectionKey[] array = this.keys;
        int s = this.size;
        for (int i = 0; i < s; ++i) {
            SelectionKey k = array[i];
            if (!k.equals(o)) continue;
            return true;
        }
        return false;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public Iterator<SelectionKey> iterator() {
        return new /* Unavailable Anonymous Inner Class!! */;
    }

    void reset() {
        this.reset(0);
    }

    void reset(int start) {
        Arrays.fill(this.keys, start, this.size, null);
        this.size = 0;
    }

    private void increaseCapacity() {
        SelectionKey[] newKeys = new SelectionKey[this.keys.length << 1];
        System.arraycopy(this.keys, 0, newKeys, 0, this.size);
        this.keys = newKeys;
    }
}

