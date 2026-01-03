/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import jdk.internal.misc.TerminatingThreadLocal;
import jdk.internal.misc.Unsafe;
import sun.nio.ch.DirectBuffer;
import sun.nio.ch.IOUtil;
import sun.security.action.GetPropertyAction;

public class Util {
    private static final int TEMP_BUF_POOL_SIZE = IOUtil.IOV_MAX;
    private static final long MAX_CACHED_BUFFER_SIZE = Util.getMaxCachedBufferSize();
    private static TerminatingThreadLocal<BufferCache> bufferCache = new TerminatingThreadLocal<BufferCache>(){

        @Override
        protected BufferCache initialValue() {
            return new BufferCache();
        }

        @Override
        protected void threadTerminated(BufferCache cache) {
            while (!cache.isEmpty()) {
                ByteBuffer bb = cache.removeFirst();
                Util.free(bb);
            }
        }
    };
    private static Unsafe unsafe = Unsafe.getUnsafe();
    private static int pageSize = -1;
    private static volatile Constructor<?> directByteBufferConstructor;
    private static volatile Constructor<?> directByteBufferRConstructor;

    private static long getMaxCachedBufferSize() {
        String s = GetPropertyAction.privilegedGetProperty("jdk.nio.maxCachedBufferSize");
        if (s != null) {
            try {
                long m = Long.parseLong(s);
                if (m >= 0L) {
                    return m;
                }
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return Long.MAX_VALUE;
    }

    private static boolean isBufferTooLarge(int size) {
        return (long)size > MAX_CACHED_BUFFER_SIZE;
    }

    private static boolean isBufferTooLarge(ByteBuffer buf) {
        return Util.isBufferTooLarge(buf.capacity());
    }

    public static ByteBuffer getTemporaryDirectBuffer(int size) {
        if (Util.isBufferTooLarge(size)) {
            return ByteBuffer.allocateDirect(size);
        }
        BufferCache cache = (BufferCache)bufferCache.get();
        ByteBuffer buf = cache.get(size);
        if (buf != null) {
            return buf;
        }
        if (!cache.isEmpty()) {
            buf = cache.removeFirst();
            Util.free(buf);
        }
        return ByteBuffer.allocateDirect(size);
    }

    public static ByteBuffer getTemporaryAlignedDirectBuffer(int size, int alignment) {
        if (Util.isBufferTooLarge(size)) {
            return ByteBuffer.allocateDirect(size + alignment - 1).alignedSlice(alignment);
        }
        BufferCache cache = (BufferCache)bufferCache.get();
        ByteBuffer buf = cache.get(size);
        if (buf != null) {
            if (buf.alignmentOffset(0, alignment) == 0) {
                return buf;
            }
        } else if (!cache.isEmpty()) {
            buf = cache.removeFirst();
            Util.free(buf);
        }
        return ByteBuffer.allocateDirect(size + alignment - 1).alignedSlice(alignment);
    }

    public static void releaseTemporaryDirectBuffer(ByteBuffer buf) {
        Util.offerFirstTemporaryDirectBuffer(buf);
    }

    static void offerFirstTemporaryDirectBuffer(ByteBuffer buf) {
        if (Util.isBufferTooLarge(buf)) {
            Util.free(buf);
            return;
        }
        assert (buf != null);
        BufferCache cache = (BufferCache)bufferCache.get();
        if (!cache.offerFirst(buf)) {
            Util.free(buf);
        }
    }

    static void offerLastTemporaryDirectBuffer(ByteBuffer buf) {
        if (Util.isBufferTooLarge(buf)) {
            Util.free(buf);
            return;
        }
        assert (buf != null);
        BufferCache cache = (BufferCache)bufferCache.get();
        if (!cache.offerLast(buf)) {
            Util.free(buf);
        }
    }

    private static void free(ByteBuffer buf) {
        ((DirectBuffer)((Object)buf)).cleaner().clean();
    }

    static ByteBuffer[] subsequence(ByteBuffer[] bs, int offset, int length) {
        if (offset == 0 && length == bs.length) {
            return bs;
        }
        int n = length;
        ByteBuffer[] bs2 = new ByteBuffer[n];
        for (int i = 0; i < n; ++i) {
            bs2[i] = bs[offset + i];
        }
        return bs2;
    }

    static <E> Set<E> ungrowableSet(final Set<E> s) {
        return new Set<E>(){

            @Override
            public int size() {
                return s.size();
            }

            @Override
            public boolean isEmpty() {
                return s.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return s.contains(o);
            }

            @Override
            public Object[] toArray() {
                return s.toArray();
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return s.toArray(a);
            }

            public String toString() {
                return s.toString();
            }

            @Override
            public Iterator<E> iterator() {
                return s.iterator();
            }

            @Override
            public boolean equals(Object o) {
                return s.equals(o);
            }

            @Override
            public int hashCode() {
                return s.hashCode();
            }

            @Override
            public void clear() {
                s.clear();
            }

            @Override
            public boolean remove(Object o) {
                return s.remove(o);
            }

            @Override
            public boolean containsAll(Collection<?> coll) {
                return s.containsAll(coll);
            }

            @Override
            public boolean removeAll(Collection<?> coll) {
                return s.removeAll(coll);
            }

            @Override
            public boolean retainAll(Collection<?> coll) {
                return s.retainAll(coll);
            }

            @Override
            public boolean add(E o) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean addAll(Collection<? extends E> coll) {
                throw new UnsupportedOperationException();
            }
        };
    }

    private static byte _get(long a) {
        return unsafe.getByte(a);
    }

    private static void _put(long a, byte b) {
        unsafe.putByte(a, b);
    }

    static void erase(ByteBuffer bb) {
        unsafe.setMemory(((DirectBuffer)((Object)bb)).address(), bb.capacity(), (byte)0);
    }

    static Unsafe unsafe() {
        return unsafe;
    }

    static int pageSize() {
        if (pageSize == -1) {
            pageSize = Util.unsafe().pageSize();
        }
        return pageSize;
    }

    private static void initDBBConstructor() {
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                try {
                    Class<?> cl = Class.forName("java.nio.DirectByteBuffer");
                    Constructor<?> ctor = cl.getDeclaredConstructor(Integer.TYPE, Long.TYPE, FileDescriptor.class, Runnable.class, Boolean.TYPE, MemorySegment.class);
                    ctor.setAccessible(true);
                    directByteBufferConstructor = ctor;
                }
                catch (ClassCastException | ClassNotFoundException | IllegalArgumentException | NoSuchMethodException x) {
                    throw new InternalError(x);
                }
                return null;
            }
        });
    }

    static MappedByteBuffer newMappedByteBuffer(int size, long addr, FileDescriptor fd, Runnable unmapper, boolean isSync) {
        MappedByteBuffer dbb;
        if (directByteBufferConstructor == null) {
            Util.initDBBConstructor();
        }
        try {
            dbb = (MappedByteBuffer)directByteBufferConstructor.newInstance(size, addr, fd, unmapper, isSync, null);
        }
        catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new InternalError(e);
        }
        return dbb;
    }

    private static void initDBBRConstructor() {
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                try {
                    Class<?> cl = Class.forName("java.nio.DirectByteBufferR");
                    Constructor<?> ctor = cl.getDeclaredConstructor(Integer.TYPE, Long.TYPE, FileDescriptor.class, Runnable.class, Boolean.TYPE, MemorySegment.class);
                    ctor.setAccessible(true);
                    directByteBufferRConstructor = ctor;
                }
                catch (ClassCastException | ClassNotFoundException | IllegalArgumentException | NoSuchMethodException x) {
                    throw new InternalError(x);
                }
                return null;
            }
        });
    }

    static MappedByteBuffer newMappedByteBufferR(int size, long addr, FileDescriptor fd, Runnable unmapper, boolean isSync) {
        MappedByteBuffer dbb;
        if (directByteBufferRConstructor == null) {
            Util.initDBBRConstructor();
        }
        try {
            dbb = (MappedByteBuffer)directByteBufferRConstructor.newInstance(size, addr, fd, unmapper, isSync, null);
        }
        catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new InternalError(e);
        }
        return dbb;
    }

    static void checkBufferPositionAligned(ByteBuffer bb, int pos, int alignment) throws IOException {
        int alignmentOffset = bb.alignmentOffset(pos, alignment);
        if (alignmentOffset != 0) {
            throw new IOException("Current position of the bytebuffer (" + pos + ") is not a multiple of the block size (" + alignment + "): alignment offset = " + alignmentOffset);
        }
    }

    static void checkRemainingBufferSizeAligned(int rem, int alignment) throws IOException {
        if (rem % alignment != 0) {
            throw new IOException("Number of remaining bytes (" + rem + ") is not a multiple of the block size (" + alignment + ")");
        }
    }

    static void checkChannelPositionAligned(long position, int alignment) throws IOException {
        if (position % (long)alignment != 0L) {
            throw new IOException("Channel position (" + position + ") is not a multiple of the block size (" + alignment + ")");
        }
    }

    private static class BufferCache {
        private ByteBuffer[] buffers = new ByteBuffer[TEMP_BUF_POOL_SIZE];
        private int count;
        private int start;

        private int next(int i) {
            return (i + 1) % TEMP_BUF_POOL_SIZE;
        }

        BufferCache() {
        }

        ByteBuffer get(int size) {
            assert (!Util.isBufferTooLarge(size));
            if (this.count == 0) {
                return null;
            }
            ByteBuffer[] buffers = this.buffers;
            ByteBuffer buf = buffers[this.start];
            if (buf.capacity() < size) {
                ByteBuffer bb;
                buf = null;
                int i = this.start;
                while ((i = this.next(i)) != this.start && (bb = buffers[i]) != null) {
                    if (bb.capacity() < size) continue;
                    buf = bb;
                    break;
                }
                if (buf == null) {
                    return null;
                }
                buffers[i] = buffers[this.start];
            }
            buffers[this.start] = null;
            this.start = this.next(this.start);
            --this.count;
            buf.rewind();
            buf.limit(size);
            return buf;
        }

        boolean offerFirst(ByteBuffer buf) {
            assert (!Util.isBufferTooLarge(buf));
            if (this.count >= TEMP_BUF_POOL_SIZE) {
                return false;
            }
            this.start = (this.start + TEMP_BUF_POOL_SIZE - 1) % TEMP_BUF_POOL_SIZE;
            this.buffers[this.start] = buf;
            ++this.count;
            return true;
        }

        boolean offerLast(ByteBuffer buf) {
            assert (!Util.isBufferTooLarge(buf));
            if (this.count >= TEMP_BUF_POOL_SIZE) {
                return false;
            }
            int next = (this.start + this.count) % TEMP_BUF_POOL_SIZE;
            this.buffers[next] = buf;
            ++this.count;
            return true;
        }

        boolean isEmpty() {
            return this.count == 0;
        }

        ByteBuffer removeFirst() {
            assert (this.count > 0);
            ByteBuffer buf = this.buffers[this.start];
            this.buffers[this.start] = null;
            this.start = this.next(this.start);
            --this.count;
            return buf;
        }
    }
}

