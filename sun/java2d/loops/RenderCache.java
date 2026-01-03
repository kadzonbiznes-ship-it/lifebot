/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import sun.java2d.loops.CompositeType;
import sun.java2d.loops.SurfaceType;

public final class RenderCache {
    private Entry[] entries;

    public RenderCache(int size) {
        this.entries = new Entry[size];
    }

    public synchronized Object get(SurfaceType src, CompositeType comp, SurfaceType dst) {
        int max;
        Entry e;
        for (int i = max = this.entries.length - 1; i >= 0 && (e = this.entries[i]) != null; --i) {
            if (!e.matches(src, comp, dst)) continue;
            if (i < max - 4) {
                System.arraycopy(this.entries, i + 1, this.entries, i, max - i);
                this.entries[max] = e;
            }
            return e.getValue();
        }
        return null;
    }

    public synchronized void put(SurfaceType src, CompositeType comp, SurfaceType dst, Object value) {
        Entry e = new Entry(src, comp, dst, value);
        int num = this.entries.length;
        System.arraycopy(this.entries, 1, this.entries, 0, num - 1);
        this.entries[num - 1] = e;
    }

    static final class Entry {
        private SurfaceType src;
        private CompositeType comp;
        private SurfaceType dst;
        private Object value;

        public Entry(SurfaceType src, CompositeType comp, SurfaceType dst, Object value) {
            this.src = src;
            this.comp = comp;
            this.dst = dst;
            this.value = value;
        }

        public boolean matches(SurfaceType src, CompositeType comp, SurfaceType dst) {
            return this.src == src && this.comp == comp && this.dst == dst;
        }

        public Object getValue() {
            return this.value;
        }
    }
}

