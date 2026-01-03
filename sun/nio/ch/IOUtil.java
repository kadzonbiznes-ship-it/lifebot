/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import jdk.internal.access.JavaNioAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.loader.BootLoader;
import sun.nio.ch.DirectBuffer;
import sun.nio.ch.IOVecWrapper;
import sun.nio.ch.NativeDispatcher;
import sun.nio.ch.Util;

public final class IOUtil {
    static final int IOV_MAX;
    static final long WRITEV_MAX;
    private static final JavaNioAccess NIO_ACCESS;

    private IOUtil() {
    }

    static int write(FileDescriptor fd, ByteBuffer src, long position, NativeDispatcher nd) throws IOException {
        return IOUtil.write(fd, src, position, false, false, -1, nd);
    }

    static int write(FileDescriptor fd, ByteBuffer src, long position, boolean async, NativeDispatcher nd) throws IOException {
        return IOUtil.write(fd, src, position, false, async, -1, nd);
    }

    static int write(FileDescriptor fd, ByteBuffer src, long position, boolean directIO, int alignment, NativeDispatcher nd) throws IOException {
        return IOUtil.write(fd, src, position, directIO, false, alignment, nd);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static int write(FileDescriptor fd, ByteBuffer src, long position, boolean directIO, boolean async, int alignment, NativeDispatcher nd) throws IOException {
        ByteBuffer bb;
        int rem;
        if (src instanceof DirectBuffer) {
            return IOUtil.writeFromNativeBuffer(fd, src, position, directIO, async, alignment, nd);
        }
        int pos = src.position();
        int lim = src.limit();
        assert (pos <= lim);
        int n = rem = pos <= lim ? lim - pos : 0;
        if (directIO) {
            Util.checkRemainingBufferSizeAligned(rem, alignment);
            bb = Util.getTemporaryAlignedDirectBuffer(rem, alignment);
        } else {
            bb = Util.getTemporaryDirectBuffer(rem);
        }
        try {
            bb.put(src);
            bb.flip();
            src.position(pos);
            int n2 = IOUtil.writeFromNativeBuffer(fd, bb, position, directIO, async, alignment, nd);
            if (n2 > 0) {
                src.position(pos + n2);
            }
            int n3 = n2;
            return n3;
        }
        finally {
            Util.offerFirstTemporaryDirectBuffer(bb);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static int writeFromNativeBuffer(FileDescriptor fd, ByteBuffer bb, long position, boolean directIO, boolean async, int alignment, NativeDispatcher nd) throws IOException {
        int rem;
        int pos = bb.position();
        int lim = bb.limit();
        assert (pos <= lim);
        int n = rem = pos <= lim ? lim - pos : 0;
        if (directIO) {
            Util.checkBufferPositionAligned(bb, pos, alignment);
            Util.checkRemainingBufferSizeAligned(rem, alignment);
        }
        int written = 0;
        if (rem == 0) {
            return 0;
        }
        IOUtil.acquireScope(bb, async);
        try {
            written = position != -1L ? nd.pwrite(fd, IOUtil.bufferAddress(bb) + (long)pos, rem, position) : nd.write(fd, IOUtil.bufferAddress(bb) + (long)pos, rem);
        }
        finally {
            IOUtil.releaseScope(bb);
        }
        if (written > 0) {
            bb.position(pos + written);
        }
        return written;
    }

    static long write(FileDescriptor fd, ByteBuffer[] bufs, boolean async, NativeDispatcher nd) throws IOException {
        return IOUtil.write(fd, bufs, 0, bufs.length, false, async, -1, nd);
    }

    static long write(FileDescriptor fd, ByteBuffer[] bufs, int offset, int length, NativeDispatcher nd) throws IOException {
        return IOUtil.write(fd, bufs, offset, length, false, false, -1, nd);
    }

    static long write(FileDescriptor fd, ByteBuffer[] bufs, int offset, int length, boolean direct, int alignment, NativeDispatcher nd) throws IOException {
        return IOUtil.write(fd, bufs, offset, length, direct, false, alignment, nd);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static long write(FileDescriptor fd, ByteBuffer[] bufs, int offset, int length, boolean directIO, boolean async, int alignment, NativeDispatcher nd) throws IOException {
        long l;
        LinkedRunnable handleReleasers;
        int iov_len;
        boolean completed;
        IOVecWrapper vec;
        block21: {
            vec = IOVecWrapper.get(length);
            completed = false;
            iov_len = 0;
            handleReleasers = null;
            long writevLen = 0L;
            int count = offset + length;
            for (int i = offset; i < count && iov_len < IOV_MAX && writevLen < WRITEV_MAX; ++i) {
                int rem;
                ByteBuffer buf = bufs[i];
                IOUtil.acquireScope(buf, async);
                if (NIO_ACCESS.hasSession(buf)) {
                    handleReleasers = LinkedRunnable.of(Releaser.of(buf), handleReleasers);
                }
                int pos = buf.position();
                int lim = buf.limit();
                assert (pos <= lim);
                int n = rem = pos <= lim ? lim - pos : 0;
                if (directIO) {
                    Util.checkRemainingBufferSizeAligned(rem, alignment);
                }
                if (rem <= 0) continue;
                long headroom = WRITEV_MAX - writevLen;
                if (headroom < (long)rem) {
                    rem = (int)headroom;
                }
                vec.setBuffer(iov_len, buf, pos, rem);
                if (!(buf instanceof DirectBuffer)) {
                    ByteBuffer shadow = directIO ? Util.getTemporaryAlignedDirectBuffer(rem, alignment) : Util.getTemporaryDirectBuffer(rem);
                    shadow.put(shadow.position(), buf, pos, rem);
                    shadow.flip();
                    vec.setShadow(iov_len, shadow);
                    buf = shadow;
                    pos = shadow.position();
                }
                vec.putBase(iov_len, IOUtil.bufferAddress(buf) + (long)pos);
                vec.putLen(iov_len, rem);
                ++iov_len;
                writevLen += (long)rem;
            }
            if (iov_len != 0) break block21;
            long buf = 0L;
            IOUtil.releaseScopes(handleReleasers);
            if (!completed) {
                for (int j = 0; j < iov_len; ++j) {
                    ByteBuffer shadow = vec.getShadow(j);
                    if (shadow != null) {
                        Util.offerLastTemporaryDirectBuffer(shadow);
                    }
                    vec.clearRefs(j);
                }
            }
            return buf;
        }
        try {
            long bytesWritten;
            long left = bytesWritten = nd.writev(fd, vec.address, iov_len);
            for (int j = 0; j < iov_len; ++j) {
                ByteBuffer shadow;
                if (left > 0L) {
                    ByteBuffer buf = vec.getBuffer(j);
                    int pos = vec.getPosition(j);
                    int rem = vec.getRemaining(j);
                    int n = left > (long)rem ? rem : (int)left;
                    buf.position(pos + n);
                    left -= (long)n;
                }
                if ((shadow = vec.getShadow(j)) != null) {
                    Util.offerLastTemporaryDirectBuffer(shadow);
                }
                vec.clearRefs(j);
            }
            completed = true;
            l = bytesWritten;
        }
        catch (Throwable throwable) {
            IOUtil.releaseScopes(handleReleasers);
            if (!completed) {
                for (int j = 0; j < iov_len; ++j) {
                    ByteBuffer shadow = vec.getShadow(j);
                    if (shadow != null) {
                        Util.offerLastTemporaryDirectBuffer(shadow);
                    }
                    vec.clearRefs(j);
                }
            }
            throw throwable;
        }
        IOUtil.releaseScopes(handleReleasers);
        if (!completed) {
            for (int j = 0; j < iov_len; ++j) {
                ByteBuffer shadow = vec.getShadow(j);
                if (shadow != null) {
                    Util.offerLastTemporaryDirectBuffer(shadow);
                }
                vec.clearRefs(j);
            }
        }
        return l;
    }

    static int read(FileDescriptor fd, ByteBuffer dst, long position, NativeDispatcher nd) throws IOException {
        return IOUtil.read(fd, dst, position, false, false, -1, nd);
    }

    static int read(FileDescriptor fd, ByteBuffer dst, long position, boolean async, NativeDispatcher nd) throws IOException {
        return IOUtil.read(fd, dst, position, false, async, -1, nd);
    }

    static int read(FileDescriptor fd, ByteBuffer dst, long position, boolean directIO, int alignment, NativeDispatcher nd) throws IOException {
        return IOUtil.read(fd, dst, position, directIO, false, alignment, nd);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static int read(FileDescriptor fd, ByteBuffer dst, long position, boolean directIO, boolean async, int alignment, NativeDispatcher nd) throws IOException {
        ByteBuffer bb;
        if (dst.isReadOnly()) {
            throw new IllegalArgumentException("Read-only buffer");
        }
        if (dst instanceof DirectBuffer) {
            return IOUtil.readIntoNativeBuffer(fd, dst, position, directIO, async, alignment, nd);
        }
        int rem = dst.remaining();
        if (directIO) {
            Util.checkRemainingBufferSizeAligned(rem, alignment);
            bb = Util.getTemporaryAlignedDirectBuffer(rem, alignment);
        } else {
            bb = Util.getTemporaryDirectBuffer(rem);
        }
        try {
            int n = IOUtil.readIntoNativeBuffer(fd, bb, position, directIO, async, alignment, nd);
            bb.flip();
            if (n > 0) {
                dst.put(bb);
            }
            int n2 = n;
            return n2;
        }
        finally {
            Util.offerFirstTemporaryDirectBuffer(bb);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static int readIntoNativeBuffer(FileDescriptor fd, ByteBuffer bb, long position, boolean directIO, boolean async, int alignment, NativeDispatcher nd) throws IOException {
        int rem;
        int pos = bb.position();
        int lim = bb.limit();
        assert (pos <= lim);
        int n = rem = pos <= lim ? lim - pos : 0;
        if (directIO) {
            Util.checkBufferPositionAligned(bb, pos, alignment);
            Util.checkRemainingBufferSizeAligned(rem, alignment);
        }
        if (rem == 0) {
            return 0;
        }
        int n2 = 0;
        IOUtil.acquireScope(bb, async);
        try {
            n2 = position != -1L ? nd.pread(fd, IOUtil.bufferAddress(bb) + (long)pos, rem, position) : nd.read(fd, IOUtil.bufferAddress(bb) + (long)pos, rem);
        }
        finally {
            IOUtil.releaseScope(bb);
        }
        if (n2 > 0) {
            bb.position(pos + n2);
        }
        return n2;
    }

    static long read(FileDescriptor fd, ByteBuffer[] bufs, NativeDispatcher nd) throws IOException {
        return IOUtil.read(fd, bufs, 0, bufs.length, false, false, -1, nd);
    }

    static long read(FileDescriptor fd, ByteBuffer[] bufs, boolean async, NativeDispatcher nd) throws IOException {
        return IOUtil.read(fd, bufs, 0, bufs.length, false, async, -1, nd);
    }

    static long read(FileDescriptor fd, ByteBuffer[] bufs, int offset, int length, NativeDispatcher nd) throws IOException {
        return IOUtil.read(fd, bufs, offset, length, false, false, -1, nd);
    }

    static long read(FileDescriptor fd, ByteBuffer[] bufs, int offset, int length, boolean directIO, int alignment, NativeDispatcher nd) throws IOException {
        return IOUtil.read(fd, bufs, offset, length, directIO, false, alignment, nd);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static long read(FileDescriptor fd, ByteBuffer[] bufs, int offset, int length, boolean directIO, boolean async, int alignment, NativeDispatcher nd) throws IOException {
        long l;
        LinkedRunnable handleReleasers;
        int iov_len;
        boolean completed;
        IOVecWrapper vec;
        block23: {
            vec = IOVecWrapper.get(length);
            completed = false;
            iov_len = 0;
            handleReleasers = null;
            int count = offset + length;
            for (int i = offset; i < count && iov_len < IOV_MAX; ++i) {
                int rem;
                ByteBuffer buf = bufs[i];
                if (buf.isReadOnly()) {
                    throw new IllegalArgumentException("Read-only buffer");
                }
                IOUtil.acquireScope(buf, async);
                if (NIO_ACCESS.hasSession(buf)) {
                    handleReleasers = LinkedRunnable.of(Releaser.of(buf), handleReleasers);
                }
                int pos = buf.position();
                int lim = buf.limit();
                assert (pos <= lim);
                int n = rem = pos <= lim ? lim - pos : 0;
                if (directIO) {
                    Util.checkRemainingBufferSizeAligned(rem, alignment);
                }
                if (rem <= 0) continue;
                vec.setBuffer(iov_len, buf, pos, rem);
                if (!(buf instanceof DirectBuffer)) {
                    ByteBuffer shadow = directIO ? Util.getTemporaryAlignedDirectBuffer(rem, alignment) : Util.getTemporaryDirectBuffer(rem);
                    vec.setShadow(iov_len, shadow);
                    buf = shadow;
                    pos = shadow.position();
                }
                vec.putBase(iov_len, IOUtil.bufferAddress(buf) + (long)pos);
                vec.putLen(iov_len, rem);
                ++iov_len;
            }
            if (iov_len != 0) break block23;
            long buf = 0L;
            IOUtil.releaseScopes(handleReleasers);
            if (!completed) {
                for (int j = 0; j < iov_len; ++j) {
                    ByteBuffer shadow = vec.getShadow(j);
                    if (shadow != null) {
                        Util.offerLastTemporaryDirectBuffer(shadow);
                    }
                    vec.clearRefs(j);
                }
            }
            return buf;
        }
        try {
            long bytesRead;
            long left = bytesRead = nd.readv(fd, vec.address, iov_len);
            for (int j = 0; j < iov_len; ++j) {
                ByteBuffer shadow = vec.getShadow(j);
                if (left > 0L) {
                    int n;
                    ByteBuffer buf = vec.getBuffer(j);
                    int rem = vec.getRemaining(j);
                    int n2 = n = left > (long)rem ? rem : (int)left;
                    if (shadow == null) {
                        int pos = vec.getPosition(j);
                        buf.position(pos + n);
                    } else {
                        shadow.limit(shadow.position() + n);
                        buf.put(shadow);
                    }
                    left -= (long)n;
                }
                if (shadow != null) {
                    Util.offerLastTemporaryDirectBuffer(shadow);
                }
                vec.clearRefs(j);
            }
            completed = true;
            l = bytesRead;
        }
        catch (Throwable throwable) {
            IOUtil.releaseScopes(handleReleasers);
            if (!completed) {
                for (int j = 0; j < iov_len; ++j) {
                    ByteBuffer shadow = vec.getShadow(j);
                    if (shadow != null) {
                        Util.offerLastTemporaryDirectBuffer(shadow);
                    }
                    vec.clearRefs(j);
                }
            }
            throw throwable;
        }
        IOUtil.releaseScopes(handleReleasers);
        if (!completed) {
            for (int j = 0; j < iov_len; ++j) {
                ByteBuffer shadow = vec.getShadow(j);
                if (shadow != null) {
                    Util.offerLastTemporaryDirectBuffer(shadow);
                }
                vec.clearRefs(j);
            }
        }
        return l;
    }

    static void acquireScope(ByteBuffer bb, boolean async) {
        if (async && NIO_ACCESS.isThreadConfined(bb)) {
            throw new IllegalStateException("Confined session not supported");
        }
        NIO_ACCESS.acquireSession(bb);
    }

    private static void releaseScope(ByteBuffer bb) {
        try {
            NIO_ACCESS.releaseSession(bb);
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static Runnable acquireScopes(ByteBuffer[] buffers) {
        return IOUtil.acquireScopes(null, buffers);
    }

    static Runnable acquireScopes(ByteBuffer buf, ByteBuffer[] buffers) {
        if (buffers == null) {
            assert (buf != null);
            IOUtil.acquireScope(buf, true);
            return Releaser.of(buf);
        }
        assert (buf == null);
        LinkedRunnable handleReleasers = null;
        for (ByteBuffer b : buffers) {
            IOUtil.acquireScope(b, true);
            handleReleasers = LinkedRunnable.of(Releaser.of(b), handleReleasers);
        }
        return handleReleasers;
    }

    static void releaseScopes(Runnable releasers) {
        if (releasers != null) {
            releasers.run();
        }
    }

    static long bufferAddress(ByteBuffer buf) {
        return NIO_ACCESS.getBufferAddress(buf);
    }

    public static FileDescriptor newFD(int i) {
        FileDescriptor fd = new FileDescriptor();
        IOUtil.setfdVal(fd, i);
        return fd;
    }

    static native boolean randomBytes(byte[] var0);

    static native long makePipe(boolean var0) throws IOException;

    static native int write1(int var0, byte var1) throws IOException;

    static native boolean drain(int var0) throws IOException;

    static native int drain1(int var0) throws IOException;

    public static native void configureBlocking(FileDescriptor var0, boolean var1) throws IOException;

    public static native int fdVal(FileDescriptor var0);

    static native void setfdVal(FileDescriptor var0, int var1);

    static native int fdLimit();

    static native int iovMax();

    static native long writevMax();

    static native void initIDs();

    public static void load() {
    }

    static {
        NIO_ACCESS = SharedSecrets.getJavaNioAccess();
        BootLoader.loadLibrary("net");
        BootLoader.loadLibrary("nio");
        IOUtil.initIDs();
        IOV_MAX = IOUtil.iovMax();
        WRITEV_MAX = IOUtil.writevMax();
    }

    record Releaser(ByteBuffer bb) implements Runnable
    {
        Releaser {
            Objects.requireNonNull(bb);
        }

        @Override
        public void run() {
            IOUtil.releaseScope(this.bb);
        }

        static Runnable of(ByteBuffer bb) {
            return NIO_ACCESS.hasSession(bb) ? new Releaser(bb) : () -> {};
        }
    }

    record LinkedRunnable(Runnable node, Runnable next) implements Runnable
    {
        LinkedRunnable {
            Objects.requireNonNull(node);
        }

        @Override
        public void run() {
            try {
                this.node.run();
            }
            finally {
                if (this.next != null) {
                    this.next.run();
                }
            }
        }

        static LinkedRunnable of(Runnable first, Runnable second) {
            return new LinkedRunnable(first, second);
        }
    }
}

