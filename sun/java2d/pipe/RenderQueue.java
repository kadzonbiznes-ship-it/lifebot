/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.util.HashSet;
import java.util.Set;
import sun.awt.SunToolkit;
import sun.java2d.pipe.RenderBuffer;

public abstract class RenderQueue {
    private static final int BUFFER_SIZE = 32000;
    protected RenderBuffer buf;
    protected Set<Object> refSet = new HashSet<Object>();

    protected RenderQueue() {
        this.buf = RenderBuffer.allocate(32000);
    }

    public final void lock() {
        SunToolkit.awtLock();
    }

    public final boolean tryLock() {
        return SunToolkit.awtTryLock();
    }

    public final void unlock() {
        SunToolkit.awtUnlock();
    }

    public final void addReference(Object ref) {
        this.refSet.add(ref);
    }

    public final RenderBuffer getBuffer() {
        return this.buf;
    }

    public final void ensureCapacity(int opsize) {
        if (this.buf.remaining() < opsize) {
            this.flushNow();
        }
    }

    public final void ensureCapacityAndAlignment(int opsize, int first8ByteValueOffset) {
        this.ensureCapacity(opsize + 4);
        this.ensureAlignment(first8ByteValueOffset);
    }

    public final void ensureAlignment(int first8ByteValueOffset) {
        int first8ByteValuePosition = this.buf.position() + first8ByteValueOffset;
        if ((first8ByteValuePosition & 7) != 0) {
            this.buf.putInt(90);
        }
    }

    public abstract void flushNow();

    public abstract void flushAndInvokeNow(Runnable var1);

    public void flushNow(int position) {
        this.buf.position(position);
        this.flushNow();
    }
}

