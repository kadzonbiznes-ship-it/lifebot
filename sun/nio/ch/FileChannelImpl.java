/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.FileLockInterruptionException;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Iterator;
import java.util.Objects;
import jdk.internal.access.JavaIOFileDescriptorAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.access.foreign.UnmapperProxy;
import jdk.internal.foreign.MappedMemorySegmentImpl;
import jdk.internal.foreign.MemorySessionImpl;
import jdk.internal.misc.Blocker;
import jdk.internal.misc.ExtendedMapMode;
import jdk.internal.misc.Unsafe;
import jdk.internal.misc.VM;
import jdk.internal.ref.Cleaner;
import jdk.internal.ref.CleanerFactory;
import sun.nio.ch.DirectBuffer;
import sun.nio.ch.FileDispatcher;
import sun.nio.ch.FileDispatcherImpl;
import sun.nio.ch.FileLockImpl;
import sun.nio.ch.FileLockTable;
import sun.nio.ch.IOStatus;
import sun.nio.ch.IOUtil;
import sun.nio.ch.NativeDispatcher;
import sun.nio.ch.NativeThreadSet;
import sun.nio.ch.SelChImpl;
import sun.nio.ch.SinkChannelImpl;
import sun.nio.ch.Util;

public class FileChannelImpl
extends FileChannel {
    private static final JavaIOFileDescriptorAccess fdAccess = SharedSecrets.getJavaIOFileDescriptorAccess();
    private static final FileDispatcher nd = new FileDispatcherImpl();
    private final FileDescriptor fd;
    private final boolean writable;
    private final boolean readable;
    private final Closeable parent;
    private final String path;
    private final NativeThreadSet threads = new NativeThreadSet(2);
    private final Object positionLock = new Object();
    private volatile boolean uninterruptible;
    private final boolean direct;
    private final int alignment;
    private final Cleaner.Cleanable closer;
    private static volatile boolean transferToNotSupported;
    private static volatile boolean pipeSupported;
    private static volatile boolean fileSupported;
    private static final long MAPPED_TRANSFER_THRESHOLD = 16384L;
    private static final long MAPPED_TRANSFER_SIZE = 0x800000L;
    private static volatile boolean transferFromNotSupported;
    private static final int TRANSFER_SIZE = 8192;
    private static final int MAP_INVALID = -1;
    private static final int MAP_RO = 0;
    private static final int MAP_RW = 1;
    private static final int MAP_PV = 2;
    private volatile FileLockTable fileLockTable;

    private FileChannelImpl(FileDescriptor fd, String path, boolean readable, boolean writable, boolean direct, Closeable parent) {
        this.fd = fd;
        this.path = path;
        this.readable = readable;
        this.writable = writable;
        this.direct = direct;
        this.parent = parent;
        if (direct) {
            assert (path != null);
            this.alignment = nd.setDirectIO(fd, path);
        } else {
            this.alignment = -1;
        }
        this.closer = parent != null ? null : CleanerFactory.cleaner().register(this, new Closer(fd));
    }

    public static FileChannel open(FileDescriptor fd, String path, boolean readable, boolean writable, boolean direct, Closeable parent) {
        return new FileChannelImpl(fd, path, readable, writable, direct, parent);
    }

    private void ensureOpen() throws IOException {
        if (!this.isOpen()) {
            throw new ClosedChannelException();
        }
    }

    public void setUninterruptible() {
        this.uninterruptible = true;
    }

    private void beginBlocking() {
        if (!this.uninterruptible) {
            this.begin();
        }
    }

    private void endBlocking(boolean completed) throws AsynchronousCloseException {
        if (!this.uninterruptible) {
            this.end(completed);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void implCloseChannel() throws IOException {
        if (!this.fd.valid()) {
            return;
        }
        if (this.fileLockTable != null) {
            Iterator<FileLock> iterator = this.fileLockTable.removeAll().iterator();
            while (iterator.hasNext()) {
                FileLock fl;
                FileLock fileLock = fl = iterator.next();
                synchronized (fileLock) {
                    if (fl.isValid()) {
                        nd.release(this.fd, fl.position(), fl.size());
                        ((FileLockImpl)fl).invalidate();
                    }
                }
            }
        }
        this.threads.signalAndWait();
        if (this.parent != null) {
            this.parent.close();
        } else {
            try {
                this.closer.clean();
            }
            catch (UncheckedIOException uioe) {
                throw uioe.getCause();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public int read(ByteBuffer dst) throws IOException {
        this.ensureOpen();
        if (!this.readable) {
            throw new NonReadableChannelException();
        }
        Object object = this.positionLock;
        synchronized (object) {
            int n3;
            int ti;
            int n;
            block14: {
                if (this.direct) {
                    Util.checkChannelPositionAligned(this.position(), this.alignment);
                }
                n = 0;
                ti = -1;
                this.beginBlocking();
                ti = this.threads.add();
                if (this.isOpen()) break block14;
                int n2 = 0;
                this.threads.remove(ti);
                this.endBlocking(n > 0);
                assert (IOStatus.check(n));
                return n2;
            }
            try {
                do {
                    long comp = Blocker.begin();
                    try {
                        n = IOUtil.read(this.fd, dst, -1L, this.direct, this.alignment, nd);
                    }
                    finally {
                        Blocker.end(comp);
                    }
                } while (n == -3 && this.isOpen());
                n3 = IOStatus.normalize(n);
                this.threads.remove(ti);
                this.endBlocking(n > 0);
            }
            catch (Throwable throwable) {
                this.threads.remove(ti);
                this.endBlocking(n > 0);
                assert (IOStatus.check(n));
                throw throwable;
            }
            assert (IOStatus.check(n));
            return n3;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        Objects.checkFromIndexSize(offset, length, dsts.length);
        this.ensureOpen();
        if (!this.readable) {
            throw new NonReadableChannelException();
        }
        Object object = this.positionLock;
        synchronized (object) {
            long l;
            int ti;
            long n;
            block14: {
                if (this.direct) {
                    Util.checkChannelPositionAligned(this.position(), this.alignment);
                }
                n = 0L;
                ti = -1;
                this.beginBlocking();
                ti = this.threads.add();
                if (this.isOpen()) break block14;
                long l2 = 0L;
                this.threads.remove(ti);
                this.endBlocking(n > 0L);
                assert (IOStatus.check(n));
                return l2;
            }
            try {
                do {
                    long comp = Blocker.begin();
                    try {
                        n = IOUtil.read(this.fd, dsts, offset, length, this.direct, this.alignment, (NativeDispatcher)nd);
                    }
                    finally {
                        Blocker.end(comp);
                    }
                } while (n == -3L && this.isOpen());
                l = IOStatus.normalize(n);
                this.threads.remove(ti);
                this.endBlocking(n > 0L);
            }
            catch (Throwable throwable) {
                this.threads.remove(ti);
                this.endBlocking(n > 0L);
                assert (IOStatus.check(n));
                throw throwable;
            }
            assert (IOStatus.check(n));
            return l;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public int write(ByteBuffer src) throws IOException {
        this.ensureOpen();
        if (!this.writable) {
            throw new NonWritableChannelException();
        }
        Object object = this.positionLock;
        synchronized (object) {
            int n3;
            int ti;
            int n;
            block14: {
                if (this.direct) {
                    Util.checkChannelPositionAligned(this.position(), this.alignment);
                }
                n = 0;
                ti = -1;
                this.beginBlocking();
                ti = this.threads.add();
                if (this.isOpen()) break block14;
                int n2 = 0;
                this.threads.remove(ti);
                this.endBlocking(n > 0);
                assert (IOStatus.check(n));
                return n2;
            }
            try {
                do {
                    long comp = Blocker.begin();
                    try {
                        n = IOUtil.write(this.fd, src, -1L, this.direct, this.alignment, nd);
                    }
                    finally {
                        Blocker.end(comp);
                    }
                } while (n == -3 && this.isOpen());
                n3 = IOStatus.normalize(n);
                this.threads.remove(ti);
                this.endBlocking(n > 0);
            }
            catch (Throwable throwable) {
                this.threads.remove(ti);
                this.endBlocking(n > 0);
                assert (IOStatus.check(n));
                throw throwable;
            }
            assert (IOStatus.check(n));
            return n3;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        Objects.checkFromIndexSize(offset, length, srcs.length);
        this.ensureOpen();
        if (!this.writable) {
            throw new NonWritableChannelException();
        }
        Object object = this.positionLock;
        synchronized (object) {
            long l;
            int ti;
            long n;
            block14: {
                if (this.direct) {
                    Util.checkChannelPositionAligned(this.position(), this.alignment);
                }
                n = 0L;
                ti = -1;
                this.beginBlocking();
                ti = this.threads.add();
                if (this.isOpen()) break block14;
                long l2 = 0L;
                this.threads.remove(ti);
                this.endBlocking(n > 0L);
                assert (IOStatus.check(n));
                return l2;
            }
            try {
                do {
                    long comp = Blocker.begin();
                    try {
                        n = IOUtil.write(this.fd, srcs, offset, length, this.direct, this.alignment, (NativeDispatcher)nd);
                    }
                    finally {
                        Blocker.end(comp);
                    }
                } while (n == -3L && this.isOpen());
                l = IOStatus.normalize(n);
                this.threads.remove(ti);
                this.endBlocking(n > 0L);
            }
            catch (Throwable throwable) {
                this.threads.remove(ti);
                this.endBlocking(n > 0L);
                assert (IOStatus.check(n));
                throw throwable;
            }
            assert (IOStatus.check(n));
            return l;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public long position() throws IOException {
        this.ensureOpen();
        Object object = this.positionLock;
        synchronized (object) {
            long l;
            int ti;
            long p;
            block12: {
                p = -1L;
                ti = -1;
                this.beginBlocking();
                ti = this.threads.add();
                if (this.isOpen()) break block12;
                long l2 = 0L;
                this.threads.remove(ti);
                this.endBlocking(p > -1L);
                assert (IOStatus.check(p));
                return l2;
            }
            try {
                boolean append = fdAccess.getAppend(this.fd);
                do {
                    long comp = Blocker.begin();
                    try {
                        p = append ? nd.size(this.fd) : nd.seek(this.fd, -1L);
                    }
                    finally {
                        Blocker.end(comp);
                    }
                } while (p == -3L && this.isOpen());
                l = IOStatus.normalize(p);
                this.threads.remove(ti);
                this.endBlocking(p > -1L);
            }
            catch (Throwable throwable) {
                this.threads.remove(ti);
                this.endBlocking(p > -1L);
                assert (IOStatus.check(p));
                throw throwable;
            }
            assert (IOStatus.check(p));
            return l;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public FileChannel position(long newPosition) throws IOException {
        this.ensureOpen();
        if (newPosition < 0L) {
            throw new IllegalArgumentException();
        }
        Object object = this.positionLock;
        synchronized (object) {
            FileChannelImpl fileChannelImpl;
            int ti;
            long p;
            block13: {
                p = -1L;
                ti = -1;
                this.beginBlocking();
                ti = this.threads.add();
                if (this.isOpen()) break block13;
                FileChannel fileChannel = null;
                this.threads.remove(ti);
                this.endBlocking(p > -1L);
                assert (IOStatus.check(p));
                return fileChannel;
            }
            try {
                do {
                    long comp = Blocker.begin();
                    try {
                        p = nd.seek(this.fd, newPosition);
                    }
                    finally {
                        Blocker.end(comp);
                    }
                } while (p == -3L && this.isOpen());
                fileChannelImpl = this;
                this.threads.remove(ti);
                this.endBlocking(p > -1L);
            }
            catch (Throwable throwable) {
                this.threads.remove(ti);
                this.endBlocking(p > -1L);
                assert (IOStatus.check(p));
                throw throwable;
            }
            assert (IOStatus.check(p));
            return fileChannelImpl;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public long size() throws IOException {
        this.ensureOpen();
        Object object = this.positionLock;
        synchronized (object) {
            long l;
            int ti;
            long s;
            block12: {
                s = -1L;
                ti = -1;
                this.beginBlocking();
                ti = this.threads.add();
                if (this.isOpen()) break block12;
                long l2 = -1L;
                this.threads.remove(ti);
                this.endBlocking(s > -1L);
                assert (IOStatus.check(s));
                return l2;
            }
            try {
                do {
                    long comp = Blocker.begin();
                    try {
                        s = nd.size(this.fd);
                    }
                    finally {
                        Blocker.end(comp);
                    }
                } while (s == -3L && this.isOpen());
                l = IOStatus.normalize(s);
                this.threads.remove(ti);
                this.endBlocking(s > -1L);
            }
            catch (Throwable throwable) {
                this.threads.remove(ti);
                this.endBlocking(s > -1L);
                assert (IOStatus.check(s));
                throw throwable;
            }
            assert (IOStatus.check(s));
            return l;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public FileChannel truncate(long newSize) throws IOException {
        this.ensureOpen();
        if (newSize < 0L) {
            throw new IllegalArgumentException("Negative size");
        }
        if (!this.writable) {
            throw new NonWritableChannelException();
        }
        Object object = this.positionLock;
        synchronized (object) {
            FileChannelImpl fileChannelImpl;
            long comp2;
            long rp;
            int ti;
            long p;
            int rv;
            block37: {
                long size;
                block36: {
                    block35: {
                        block34: {
                            rv = -1;
                            p = -1L;
                            ti = -1;
                            rp = -1L;
                            this.beginBlocking();
                            ti = this.threads.add();
                            if (this.isOpen()) break block34;
                            FileChannel fileChannel = null;
                            this.threads.remove(ti);
                            this.endBlocking(rv > -1);
                            assert (IOStatus.check(rv));
                            return fileChannel;
                        }
                        do {
                            comp2 = Blocker.begin();
                            try {
                                size = nd.size(this.fd);
                            }
                            finally {
                                Blocker.end(comp2);
                            }
                        } while (size == -3L && this.isOpen());
                        if (this.isOpen()) break block35;
                        FileChannel comp2 = null;
                        this.threads.remove(ti);
                        this.endBlocking(rv > -1);
                        assert (IOStatus.check(rv));
                        return comp2;
                    }
                    do {
                        comp2 = Blocker.begin();
                        try {
                            p = nd.seek(this.fd, -1L);
                        }
                        finally {
                            Blocker.end(comp2);
                        }
                    } while (p == -3L && this.isOpen());
                    if (this.isOpen()) break block36;
                    FileChannel comp3 = null;
                    this.threads.remove(ti);
                    this.endBlocking(rv > -1);
                    assert (IOStatus.check(rv));
                    return comp3;
                }
                assert (p >= 0L);
                if (newSize >= size) break block37;
                do {
                    comp2 = Blocker.begin();
                    try {
                        rv = nd.truncate(this.fd, newSize);
                    }
                    finally {
                        Blocker.end(comp2);
                    }
                } while (rv == -3 && this.isOpen());
                if (this.isOpen()) break block37;
                FileChannel comp4 = null;
                this.threads.remove(ti);
                this.endBlocking(rv > -1);
                assert (IOStatus.check(rv));
                return comp4;
            }
            try {
                if (p > newSize) {
                    p = newSize;
                }
                do {
                    comp2 = Blocker.begin();
                    try {
                        rp = nd.seek(this.fd, p);
                    }
                    finally {
                        Blocker.end(comp2);
                    }
                } while (rp == -3L && this.isOpen());
                fileChannelImpl = this;
                this.threads.remove(ti);
                this.endBlocking(rv > -1);
            }
            catch (Throwable throwable) {
                this.threads.remove(ti);
                this.endBlocking(rv > -1);
                assert (IOStatus.check(rv));
                throw throwable;
            }
            assert (IOStatus.check(rv));
            return fileChannelImpl;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void force(boolean metaData) throws IOException {
        int ti;
        int rv;
        block10: {
            this.ensureOpen();
            rv = -1;
            ti = -1;
            this.beginBlocking();
            ti = this.threads.add();
            if (this.isOpen()) break block10;
            this.threads.remove(ti);
            this.endBlocking(rv > -1);
            assert (IOStatus.check(rv));
            return;
        }
        try {
            do {
                long comp = Blocker.begin();
                try {
                    rv = nd.force(this.fd, metaData);
                }
                finally {
                    Blocker.end(comp);
                }
            } while (rv == -3 && this.isOpen());
            this.threads.remove(ti);
            this.endBlocking(rv > -1);
        }
        catch (Throwable throwable) {
            this.threads.remove(ti);
            this.endBlocking(rv > -1);
            assert (IOStatus.check(rv));
            throw throwable;
        }
        assert (IOStatus.check(rv));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private long transferToDirectlyInternal(long position, int icount, WritableByteChannel target, FileDescriptor targetFD) throws IOException {
        long l;
        int ti;
        long n;
        block14: {
            block13: {
                block12: {
                    assert (!nd.transferToDirectlyNeedsPositionLock() || Thread.holdsLock(this.positionLock));
                    n = -1L;
                    ti = -1;
                    this.beginBlocking();
                    ti = this.threads.add();
                    if (this.isOpen()) break block12;
                    long l2 = -1L;
                    this.threads.remove(ti);
                    this.end(n > -1L);
                    return l2;
                }
                try {
                    boolean append = fdAccess.getAppend(targetFD);
                    do {
                        long comp = Blocker.begin();
                        try {
                            n = nd.transferTo(this.fd, position, icount, targetFD, append);
                        }
                        finally {
                            Blocker.end(comp);
                        }
                    } while (n == -3L && this.isOpen());
                    if (n != -6L) break block13;
                    if (target instanceof SinkChannelImpl) {
                        pipeSupported = false;
                    }
                    if (target instanceof FileChannelImpl) {
                        fileSupported = false;
                    }
                    l = -6L;
                    this.threads.remove(ti);
                    this.end(n > -1L);
                }
                catch (Throwable throwable) {
                    this.threads.remove(ti);
                    this.end(n > -1L);
                    throw throwable;
                }
                return l;
            }
            if (n != -4L) break block14;
            transferToNotSupported = true;
            l = -4L;
            this.threads.remove(ti);
            this.end(n > -1L);
            return l;
        }
        l = IOStatus.normalize(n);
        this.threads.remove(ti);
        this.end(n > -1L);
        return l;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private long transferToDirectly(long position, int icount, WritableByteChannel target) throws IOException {
        int targetFDVal;
        if (transferToNotSupported) {
            return -4L;
        }
        FileDescriptor targetFD = null;
        if (target instanceof FileChannelImpl) {
            if (!fileSupported) {
                return -6L;
            }
            targetFD = ((FileChannelImpl)target).fd;
        } else if (target instanceof SelChImpl) {
            if (target instanceof SinkChannelImpl && !pipeSupported) {
                return -6L;
            }
            SelectableChannel sc = (SelectableChannel)((Object)target);
            if (!nd.canTransferToDirectly(sc)) {
                return -6L;
            }
            targetFD = ((SelChImpl)((Object)target)).getFD();
        }
        if (targetFD == null) {
            return -4L;
        }
        int thisFDVal = IOUtil.fdVal(this.fd);
        if (thisFDVal == (targetFDVal = IOUtil.fdVal(targetFD))) {
            return -4L;
        }
        if (nd.transferToDirectlyNeedsPositionLock()) {
            Object object = this.positionLock;
            synchronized (object) {
                long l;
                long pos = this.position();
                try {
                    l = this.transferToDirectlyInternal(position, icount, target, targetFD);
                }
                catch (Throwable throwable) {
                    this.position(pos);
                    throw throwable;
                }
                this.position(pos);
                return l;
            }
        }
        return this.transferToDirectlyInternal(position, icount, target, targetFD);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private long transferToTrustedChannel(long position, long count, WritableByteChannel target) throws IOException {
        long posThis;
        if (count < 16384L) {
            return -6L;
        }
        boolean isSelChImpl = target instanceof SelChImpl;
        if (!(target instanceof FileChannelImpl) && !isSelChImpl) {
            return -4L;
        }
        if (target == this && (posThis = this.position()) - count + 1L <= position && position - count + 1L <= posThis && !nd.canTransferToFromOverlappedMap()) {
            return -6L;
        }
        long remaining = count;
        while (remaining > 0L) {
            long size = Math.min(remaining, 0x800000L);
            try {
                MappedByteBuffer dbb = this.map(FileChannel.MapMode.READ_ONLY, position, size);
                try {
                    int n = target.write(dbb);
                    assert (n >= 0);
                    remaining -= (long)n;
                    if (isSelChImpl) break;
                    assert (n > 0);
                    position += (long)n;
                }
                finally {
                    FileChannelImpl.unmap(dbb);
                }
            }
            catch (ClosedByInterruptException e) {
                assert (!target.isOpen());
                try {
                    this.close();
                }
                catch (Throwable suppressed) {
                    e.addSuppressed(suppressed);
                }
                throw e;
            }
            catch (IOException ioe) {
                if (remaining != count) break;
                throw ioe;
            }
        }
        return count - remaining;
    }

    private long transferToArbitraryChannel(long position, long count, WritableByteChannel target) throws IOException {
        int c = (int)Math.min(count, 8192L);
        ByteBuffer bb = ByteBuffer.allocate(c);
        long tw = 0L;
        long pos = position;
        try {
            while (tw < count) {
                bb.limit((int)Math.min(count - tw, 8192L));
                int nr = this.read(bb, pos);
                if (nr <= 0) break;
                bb.flip();
                int nw = target.write(bb);
                tw += (long)nw;
                if (nw != nr) break;
                pos += (long)nw;
                bb.clear();
            }
            return tw;
        }
        catch (IOException x) {
            if (tw > 0L) {
                return tw;
            }
            throw x;
        }
    }

    @Override
    public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
        this.ensureOpen();
        if (!target.isOpen()) {
            throw new ClosedChannelException();
        }
        if (!this.readable) {
            throw new NonReadableChannelException();
        }
        if (target instanceof FileChannelImpl && !((FileChannelImpl)target).writable) {
            throw new NonWritableChannelException();
        }
        if (position < 0L || count < 0L) {
            throw new IllegalArgumentException();
        }
        long sz = this.size();
        if (position > sz) {
            return 0L;
        }
        if (sz > 0L) {
            int icount;
            long n;
            long remaining = sz - position;
            if (remaining >= 0L && remaining < count) {
                count = remaining;
            }
            if ((n = this.transferToDirectly(position, icount = (int)Math.min(count, (long)nd.maxDirectTransferSize()), target)) >= 0L) {
                return n;
            }
            n = this.transferToTrustedChannel(position, count, target);
            if (n >= 0L) {
                return n;
            }
        }
        return this.transferToArbitraryChannel(position, count, target);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private long transferFromDirectlyInternal(FileDescriptor srcFD, long position, long count) throws IOException {
        long l;
        int ti;
        long n;
        block9: {
            block8: {
                n = -1L;
                ti = -1;
                this.beginBlocking();
                ti = this.threads.add();
                if (this.isOpen()) break block8;
                long l2 = -1L;
                this.threads.remove(ti);
                this.end(n > -1L);
                return l2;
            }
            try {
                do {
                    long comp = Blocker.begin();
                    try {
                        boolean append = fdAccess.getAppend(this.fd);
                        n = nd.transferFrom(srcFD, this.fd, position, count, append);
                    }
                    finally {
                        Blocker.end(comp);
                    }
                } while (n == -3L && this.isOpen());
                if (n != -4L) break block9;
                transferFromNotSupported = true;
                l = -4L;
                this.threads.remove(ti);
                this.end(n > -1L);
            }
            catch (Throwable throwable) {
                this.threads.remove(ti);
                this.end(n > -1L);
                throw throwable;
            }
            return l;
        }
        l = IOStatus.normalize(n);
        this.threads.remove(ti);
        this.end(n > -1L);
        return l;
    }

    private long transferFromDirectly(FileChannelImpl src, long position, long count) throws IOException {
        if (!src.readable) {
            throw new NonReadableChannelException();
        }
        if (transferFromNotSupported) {
            return -4L;
        }
        FileDescriptor srcFD = src.fd;
        if (srcFD == null) {
            return -6L;
        }
        return this.transferFromDirectlyInternal(srcFD, position, count);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private long transferFromFileChannel(FileChannelImpl src, long position, long count) throws IOException {
        if (!src.readable) {
            throw new NonReadableChannelException();
        }
        if (count < 16384L) {
            return -6L;
        }
        Object object = src.positionLock;
        synchronized (object) {
            long pos = src.position();
            long max = Math.min(count, src.size() - pos);
            if (src == this && this.position() - max + 1L <= pos && pos - max + 1L <= this.position() && !nd.canTransferToFromOverlappedMap()) {
                return -6L;
            }
            long remaining = max;
            long p = pos;
            while (remaining > 0L) {
                long size = Math.min(remaining, 0x800000L);
                MappedByteBuffer bb = src.map(FileChannel.MapMode.READ_ONLY, p, size);
                try {
                    long n = this.write(bb, position);
                    assert (n > 0L);
                    p += n;
                    position += n;
                    remaining -= n;
                }
                catch (IOException ioe) {
                    if (remaining != max) break;
                    throw ioe;
                }
                finally {
                    FileChannelImpl.unmap(bb);
                }
            }
            long nwritten = max - remaining;
            src.position(pos + nwritten);
            return nwritten;
        }
    }

    private long transferFromArbitraryChannel(ReadableByteChannel src, long position, long count) throws IOException {
        int c = (int)Math.min(count, 8192L);
        ByteBuffer bb = ByteBuffer.allocate(c);
        long tw = 0L;
        long pos = position;
        try {
            while (tw < count) {
                bb.limit((int)Math.min(count - tw, 8192L));
                int nr = src.read(bb);
                if (nr <= 0) break;
                bb.flip();
                int nw = this.write(bb, pos);
                tw += (long)nw;
                if (nw != nr) break;
                pos += (long)nw;
                bb.clear();
            }
            return tw;
        }
        catch (IOException x) {
            if (tw > 0L) {
                return tw;
            }
            throw x;
        }
    }

    @Override
    public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
        FileChannelImpl fci;
        this.ensureOpen();
        if (!src.isOpen()) {
            throw new ClosedChannelException();
        }
        if (src instanceof FileChannelImpl) {
            fci = (FileChannelImpl)src;
            if (!fci.readable) {
                throw new NonReadableChannelException();
            }
        }
        if (!this.writable) {
            throw new NonWritableChannelException();
        }
        if (position < 0L || count < 0L) {
            throw new IllegalArgumentException();
        }
        if (src instanceof FileChannelImpl && (fci = (FileChannelImpl)src).size() > 0L) {
            long n = this.transferFromDirectly(fci, position, count);
            if (n >= 0L) {
                return n;
            }
            n = this.transferFromFileChannel(fci, position, count);
            if (n >= 0L) {
                return n;
            }
        }
        return this.transferFromArbitraryChannel(src, position, count);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int read(ByteBuffer dst, long position) throws IOException {
        if (dst == null) {
            throw new NullPointerException();
        }
        if (position < 0L) {
            throw new IllegalArgumentException("Negative position");
        }
        this.ensureOpen();
        if (!this.readable) {
            throw new NonReadableChannelException();
        }
        if (this.direct) {
            Util.checkChannelPositionAligned(position, this.alignment);
        }
        if (nd.needsPositionLock()) {
            Object object = this.positionLock;
            synchronized (object) {
                return this.readInternal(dst, position);
            }
        }
        return this.readInternal(dst, position);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int readInternal(ByteBuffer dst, long position) throws IOException {
        int n;
        int ti;
        int n2;
        block11: {
            assert (!nd.needsPositionLock() || Thread.holdsLock(this.positionLock));
            n2 = 0;
            ti = -1;
            this.beginBlocking();
            ti = this.threads.add();
            if (this.isOpen()) break block11;
            int n3 = -1;
            this.threads.remove(ti);
            this.endBlocking(n2 > 0);
            assert (IOStatus.check(n2));
            return n3;
        }
        try {
            do {
                long comp = Blocker.begin();
                try {
                    n2 = IOUtil.read(this.fd, dst, position, this.direct, this.alignment, nd);
                }
                finally {
                    Blocker.end(comp);
                }
            } while (n2 == -3 && this.isOpen());
            n = IOStatus.normalize(n2);
            this.threads.remove(ti);
            this.endBlocking(n2 > 0);
        }
        catch (Throwable throwable) {
            this.threads.remove(ti);
            this.endBlocking(n2 > 0);
            assert (IOStatus.check(n2));
            throw throwable;
        }
        assert (IOStatus.check(n2));
        return n;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int write(ByteBuffer src, long position) throws IOException {
        if (src == null) {
            throw new NullPointerException();
        }
        if (position < 0L) {
            throw new IllegalArgumentException("Negative position");
        }
        this.ensureOpen();
        if (!this.writable) {
            throw new NonWritableChannelException();
        }
        if (this.direct) {
            Util.checkChannelPositionAligned(position, this.alignment);
        }
        if (nd.needsPositionLock()) {
            Object object = this.positionLock;
            synchronized (object) {
                return this.writeInternal(src, position);
            }
        }
        return this.writeInternal(src, position);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int writeInternal(ByteBuffer src, long position) throws IOException {
        int n;
        int ti;
        int n2;
        block11: {
            assert (!nd.needsPositionLock() || Thread.holdsLock(this.positionLock));
            n2 = 0;
            ti = -1;
            this.beginBlocking();
            ti = this.threads.add();
            if (this.isOpen()) break block11;
            int n3 = -1;
            this.threads.remove(ti);
            this.endBlocking(n2 > 0);
            assert (IOStatus.check(n2));
            return n3;
        }
        try {
            do {
                long comp = Blocker.begin();
                try {
                    n2 = IOUtil.write(this.fd, src, position, this.direct, this.alignment, nd);
                }
                finally {
                    Blocker.end(comp);
                }
            } while (n2 == -3 && this.isOpen());
            n = IOStatus.normalize(n2);
            this.threads.remove(ti);
            this.endBlocking(n2 > 0);
        }
        catch (Throwable throwable) {
            this.threads.remove(ti);
            this.endBlocking(n2 > 0);
            assert (IOStatus.check(n2));
            throw throwable;
        }
        assert (IOStatus.check(n2));
        return n;
    }

    private static void unmap(MappedByteBuffer bb) {
        Cleaner cl = ((DirectBuffer)((Object)bb)).cleaner();
        if (cl != null) {
            cl.clean();
        }
    }

    @Override
    public MappedByteBuffer map(FileChannel.MapMode mode, long position, long size) throws IOException {
        if (size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Size exceeds Integer.MAX_VALUE");
        }
        boolean isSync = this.isSync(Objects.requireNonNull(mode, "Mode is null"));
        int prot = this.toProt(mode);
        Unmapper unmapper = this.mapInternal(mode, position, size, prot, isSync);
        if (unmapper == null) {
            FileDescriptor dummy = new FileDescriptor();
            if (!this.writable || prot == 0) {
                return Util.newMappedByteBufferR(0, 0L, dummy, null, isSync);
            }
            return Util.newMappedByteBuffer(0, 0L, dummy, null, isSync);
        }
        if (!this.writable || prot == 0) {
            return Util.newMappedByteBufferR((int)unmapper.capacity(), unmapper.address(), unmapper.fileDescriptor(), unmapper, unmapper.isSync());
        }
        return Util.newMappedByteBuffer((int)unmapper.capacity(), unmapper.address(), unmapper.fileDescriptor(), unmapper, unmapper.isSync());
    }

    @Override
    public MemorySegment map(FileChannel.MapMode mode, long offset, long size, Arena arena) throws IOException {
        Objects.requireNonNull(mode, "Mode is null");
        Objects.requireNonNull(arena, "Arena is null");
        MemorySessionImpl sessionImpl = MemorySessionImpl.toMemorySession(arena);
        sessionImpl.checkValidState();
        if (offset < 0L) {
            throw new IllegalArgumentException("Requested bytes offset must be >= 0.");
        }
        if (size < 0L) {
            throw new IllegalArgumentException("Requested bytes size must be >= 0.");
        }
        boolean isSync = this.isSync(mode);
        int prot = this.toProt(mode);
        Unmapper unmapper = this.mapInternal(mode, offset, size, prot, isSync);
        boolean readOnly = false;
        if (mode == FileChannel.MapMode.READ_ONLY) {
            readOnly = true;
        }
        if (unmapper != null) {
            MappedMemorySegmentImpl segment = new MappedMemorySegmentImpl(unmapper.address(), unmapper, size, readOnly, sessionImpl);
            VM.BufferPool resource = new VM.BufferPool(this, unmapper){

                @Override
                public String getName() {
                    return "mapped";
                }

                @Override
                public long getCount() {
                    return DefaultUnmapper.count;
                }

                @Override
                public long getTotalCapacity() {
                    return DefaultUnmapper.totalCapacity;
                }

                @Override
                public long getMemoryUsed() {
                    return DefaultUnmapper.totalSize;
                }
            };
            sessionImpl.addOrCleanupIfFail((MemorySessionImpl.ResourceList.ResourceCleanup)((Object)resource));
            return segment;
        }
        return new MappedMemorySegmentImpl(0L, null, 0L, readOnly, sessionImpl);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    private Unmapper mapInternal(FileChannel.MapMode mode, long position, long size, int prot, boolean isSync) throws IOException {
        block34: {
            this.ensureOpen();
            if (mode == null) {
                throw new NullPointerException("Mode is null");
            }
            if (position < 0L) {
                throw new IllegalArgumentException("Negative position");
            }
            if (size < 0L) {
                throw new IllegalArgumentException("Negative size");
            }
            if (position + size < 0L) {
                throw new IllegalArgumentException("Position + size overflow");
            }
            this.checkMode(mode, prot, isSync);
            addr = -1L;
            ti = -1;
            this.beginBlocking();
            ti = this.threads.add();
            if (this.isOpen()) break block34;
            var11_8 = null;
            this.threads.remove(ti);
            this.endBlocking(IOStatus.checkAll(addr));
            return var11_8;
        }
        var14_10 = this.positionLock;
        synchronized (var14_10) {
            do {
                comp = Blocker.begin();
                try {
                    filesize = FileChannelImpl.nd.size(this.fd);
                }
                finally {
                    Blocker.end(comp);
                }
            } while (filesize == -3L && this.isOpen());
            if (this.isOpen()) ** break block35
            comp = null;
            // MONITOREXIT @DISABLED, blocks:[1, 2, 13] lbl36 : MonitorExitStatement: MONITOREXIT : var14_10
            this.threads.remove(ti);
        }
        this.endBlocking(IOStatus.checkAll(addr));
        return comp;
        {
            if (filesize >= position + size) ** break block36
            if (!this.writable) {
                throw new IOException("Channel not open for writing - cannot extend file to required size");
            }
            do {
                comp = Blocker.begin();
                try {
                    rv = FileChannelImpl.nd.truncate(this.fd, position + size);
                }
                finally {
                    Blocker.end(comp);
                }
            } while (rv == -3 && this.isOpen());
            if (this.isOpen()) ** break block36
            var18_22 = null;
            // MONITOREXIT @DISABLED, blocks:[4, 13] lbl55 : MonitorExitStatement: MONITOREXIT : var14_10
            this.threads.remove(ti);
        }
        this.endBlocking(IOStatus.checkAll(addr));
        return var18_22;
        {
            if (size != 0L) ** break block37
            rv = null;
            // MONITOREXIT @DISABLED, blocks:[6, 13] lbl62 : MonitorExitStatement: MONITOREXIT : var14_10
            this.threads.remove(ti);
        }
        this.endBlocking(IOStatus.checkAll(addr));
        return rv;
        {
            pagePosition = (int)(position % FileChannelImpl.nd.allocationGranularity());
            mapPosition = position - (long)pagePosition;
            mapSize = size + (long)pagePosition;
            try {
                addr = FileChannelImpl.nd.map(this.fd, prot, mapPosition, mapSize, isSync);
            }
            catch (OutOfMemoryError x) {
                System.gc();
                try {
                    Thread.sleep(100L);
                }
                catch (InterruptedException y) {
                    Thread.currentThread().interrupt();
                }
                try {
                    addr = FileChannelImpl.nd.map(this.fd, prot, mapPosition, mapSize, isSync);
                }
                catch (OutOfMemoryError y) {
                    throw new IOException("Map failed", y);
                }
            }
            {
                catch (Throwable var21_27) {
                    throw var21_27;
                }
            }
            try {
                mfd = FileChannelImpl.nd.duplicateForMapping(this.fd);
            }
            catch (IOException ioe) {
                FileChannelImpl.nd.unmap(addr, mapSize);
                throw ioe;
            }
            if (!FileChannelImpl.$assertionsDisabled && !IOStatus.checkAll(addr)) {
                throw new AssertionError();
            }
            if (!FileChannelImpl.$assertionsDisabled && addr % FileChannelImpl.nd.allocationGranularity() != 0L) {
                throw new AssertionError();
            }
            var16_28 /* !! */  = um /* !! */  = isSync != false ? new SyncUnmapper(addr, mapSize, size, mfd, pagePosition) : new DefaultUnmapper(addr, mapSize, size, mfd, pagePosition);
            return var16_28 /* !! */ ;
        }
        {
            finally {
                this.threads.remove(ti);
                this.endBlocking(IOStatus.checkAll(addr));
            }
        }
    }

    private boolean isSync(FileChannel.MapMode mode) {
        return !VM.isModuleSystemInited() ? false : mode == ExtendedMapMode.READ_ONLY_SYNC || mode == ExtendedMapMode.READ_WRITE_SYNC;
    }

    private int toProt(FileChannel.MapMode mode) {
        int prot = mode == FileChannel.MapMode.READ_ONLY ? 0 : (mode == FileChannel.MapMode.READ_WRITE ? 1 : (mode == FileChannel.MapMode.PRIVATE ? 2 : (mode == ExtendedMapMode.READ_ONLY_SYNC ? 0 : (mode == ExtendedMapMode.READ_WRITE_SYNC ? 1 : -1))));
        return prot;
    }

    private void checkMode(FileChannel.MapMode mode, int prot, boolean isSync) {
        if (prot == -1) {
            throw new UnsupportedOperationException();
        }
        if (mode != FileChannel.MapMode.READ_ONLY && mode != ExtendedMapMode.READ_ONLY_SYNC && !this.writable) {
            throw new NonWritableChannelException();
        }
        if (!this.readable) {
            throw new NonReadableChannelException();
        }
        if (isSync && !Unsafe.isWritebackEnabled()) {
            throw new UnsupportedOperationException();
        }
    }

    public static VM.BufferPool getMappedBufferPool() {
        return new VM.BufferPool(){

            @Override
            public String getName() {
                return "mapped - 'non-volatile memory'";
            }

            @Override
            public long getCount() {
                return SyncUnmapper.count;
            }

            @Override
            public long getTotalCapacity() {
                return SyncUnmapper.totalCapacity;
            }

            @Override
            public long getMemoryUsed() {
                return SyncUnmapper.totalSize;
            }
        };
    }

    public static VM.BufferPool getSyncMappedBufferPool() {
        return new /* Unavailable Anonymous Inner Class!! */;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private FileLockTable fileLockTable() throws IOException {
        if (this.fileLockTable == null) {
            FileChannelImpl fileChannelImpl = this;
            synchronized (fileChannelImpl) {
                if (this.fileLockTable == null) {
                    int ti = this.threads.add();
                    try {
                        this.ensureOpen();
                        this.fileLockTable = new FileLockTable(this, this.fd);
                    }
                    finally {
                        this.threads.remove(ti);
                    }
                }
            }
        }
        return this.fileLockTable;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public FileLock lock(long position, long size, boolean shared) throws IOException {
        this.ensureOpen();
        if (shared && !this.readable) {
            throw new NonReadableChannelException();
        }
        if (!shared && !this.writable) {
            throw new NonWritableChannelException();
        }
        if (size == 0L) {
            size = Long.MAX_VALUE - Math.max(0L, position);
        }
        FileLockImpl fli = new FileLockImpl(this, position, size, shared);
        FileLockTable flt = this.fileLockTable();
        flt.add(fli);
        boolean completed = false;
        int ti = -1;
        try {
            int n;
            this.beginBlocking();
            ti = this.threads.add();
            if (!this.isOpen()) {
                FileLock fileLock = null;
                return fileLock;
            }
            do {
                long comp = Blocker.begin();
                try {
                    n = nd.lock(this.fd, true, position, size, shared);
                }
                finally {
                    Blocker.end(comp);
                }
            } while (n == 2 && this.isOpen());
            if (this.isOpen()) {
                if (n == 1) {
                    assert (shared);
                    FileLockImpl fli2 = new FileLockImpl(this, position, size, false);
                    flt.replace(fli, fli2);
                    fli = fli2;
                }
                completed = true;
            }
        }
        finally {
            if (!completed) {
                flt.remove(fli);
            }
            this.threads.remove(ti);
            try {
                this.endBlocking(completed);
            }
            catch (ClosedByInterruptException e) {
                throw new FileLockInterruptionException();
            }
        }
        return fli;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public FileLock tryLock(long position, long size, boolean shared) throws IOException {
        this.ensureOpen();
        if (shared && !this.readable) {
            throw new NonReadableChannelException();
        }
        if (!shared && !this.writable) {
            throw new NonWritableChannelException();
        }
        if (size == 0L) {
            size = Long.MAX_VALUE - Math.max(0L, position);
        }
        FileLockImpl fli = new FileLockImpl(this, position, size, shared);
        FileLockTable flt = this.fileLockTable();
        flt.add(fli);
        int ti = this.threads.add();
        try {
            int result;
            try {
                this.ensureOpen();
                result = nd.lock(this.fd, false, position, size, shared);
            }
            catch (IOException e) {
                flt.remove(fli);
                throw e;
            }
            if (result == -1) {
                flt.remove(fli);
                FileLock e = null;
                return e;
            }
            if (result == 1) {
                assert (shared);
                FileLockImpl fli2 = new FileLockImpl(this, position, size, false);
                flt.replace(fli, fli2);
                FileLockImpl fileLockImpl = fli2;
                return fileLockImpl;
            }
            FileLockImpl fileLockImpl = fli;
            return fileLockImpl;
        }
        finally {
            this.threads.remove(ti);
        }
    }

    void release(FileLockImpl fli) throws IOException {
        int ti = this.threads.add();
        try {
            this.ensureOpen();
            nd.release(this.fd, fli.position(), fli.size());
        }
        finally {
            this.threads.remove(ti);
        }
        assert (this.fileLockTable != null);
        this.fileLockTable.remove(fli);
    }

    static {
        pipeSupported = true;
        fileSupported = true;
    }

    private static class Closer
    implements Runnable {
        private final FileDescriptor fd;

        Closer(FileDescriptor fd) {
            this.fd = fd;
        }

        @Override
        public void run() {
            try {
                fdAccess.close(this.fd);
            }
            catch (IOException ioe) {
                throw new UncheckedIOException("close", ioe);
            }
        }
    }

    private static abstract sealed class Unmapper
    implements Runnable,
    UnmapperProxy
    permits DefaultUnmapper, SyncUnmapper {
        private final long address;
        protected final long size;
        protected final long cap;
        private final FileDescriptor fd;
        private final int pagePosition;

        private Unmapper(long address, long size, long cap, FileDescriptor fd, int pagePosition) {
            assert (address != 0L);
            this.address = address;
            this.size = size;
            this.cap = cap;
            this.fd = fd;
            this.pagePosition = pagePosition;
        }

        @Override
        public long address() {
            return this.address + (long)this.pagePosition;
        }

        @Override
        public FileDescriptor fileDescriptor() {
            return this.fd;
        }

        @Override
        public void run() {
            this.unmap();
        }

        public long capacity() {
            return this.cap;
        }

        @Override
        public void unmap() {
            nd.unmap(this.address, this.size);
            if (this.fd.valid()) {
                try {
                    nd.close(this.fd);
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }
            this.decrementStats();
        }

        protected abstract void incrementStats();

        protected abstract void decrementStats();
    }

    private static final class SyncUnmapper
    extends Unmapper {
        static volatile int count;
        static volatile long totalSize;
        static volatile long totalCapacity;

        public SyncUnmapper(long address, long size, long cap, FileDescriptor fd, int pagePosition) {
            super(address, size, cap, fd, pagePosition);
            this.incrementStats();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        protected void incrementStats() {
            Class<SyncUnmapper> clazz = SyncUnmapper.class;
            synchronized (SyncUnmapper.class) {
                ++count;
                totalSize += this.size;
                totalCapacity += this.cap;
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        protected void decrementStats() {
            Class<SyncUnmapper> clazz = SyncUnmapper.class;
            synchronized (SyncUnmapper.class) {
                --count;
                totalSize -= this.size;
                totalCapacity -= this.cap;
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return;
            }
        }

        @Override
        public boolean isSync() {
            return true;
        }
    }

    private static final class DefaultUnmapper
    extends Unmapper {
        static volatile int count;
        static volatile long totalSize;
        static volatile long totalCapacity;

        public DefaultUnmapper(long address, long size, long cap, FileDescriptor fd, int pagePosition) {
            super(address, size, cap, fd, pagePosition);
            this.incrementStats();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        protected void incrementStats() {
            Class<DefaultUnmapper> clazz = DefaultUnmapper.class;
            synchronized (DefaultUnmapper.class) {
                ++count;
                totalSize += this.size;
                totalCapacity += this.cap;
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        protected void decrementStats() {
            Class<DefaultUnmapper> clazz = DefaultUnmapper.class;
            synchronized (DefaultUnmapper.class) {
                --count;
                totalSize -= this.size;
                totalCapacity -= this.cap;
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return;
            }
        }

        @Override
        public boolean isSync() {
            return false;
        }
    }
}

