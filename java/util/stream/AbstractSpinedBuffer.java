/*
 * Decompiled with CFR 0.152.
 */
package java.util.stream;

abstract class AbstractSpinedBuffer {
    public static final int MIN_CHUNK_POWER = 4;
    public static final int MIN_CHUNK_SIZE = 16;
    public static final int MAX_CHUNK_POWER = 30;
    public static final int MIN_SPINE_SIZE = 8;
    protected final int initialChunkPower;
    protected int elementIndex;
    protected int spineIndex;
    protected long[] priorElementCount;

    protected AbstractSpinedBuffer() {
        this.initialChunkPower = 4;
    }

    protected AbstractSpinedBuffer(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
        this.initialChunkPower = Math.max(4, 32 - Integer.numberOfLeadingZeros(initialCapacity - 1));
    }

    public boolean isEmpty() {
        return this.spineIndex == 0 && this.elementIndex == 0;
    }

    public long count() {
        return this.spineIndex == 0 ? (long)this.elementIndex : this.priorElementCount[this.spineIndex] + (long)this.elementIndex;
    }

    protected int chunkSize(int n) {
        int power = n == 0 || n == 1 ? this.initialChunkPower : Math.min(this.initialChunkPower + n - 1, 30);
        return 1 << power;
    }

    public abstract void clear();
}

