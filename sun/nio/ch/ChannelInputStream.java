/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.Objects;
import jdk.internal.util.ArraysSupport;
import sun.nio.ch.ChannelOutputStream;
import sun.nio.ch.SocketChannelImpl;
import sun.nio.ch.SocketOutputStream;

class ChannelInputStream
extends InputStream {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private final ReadableByteChannel ch;
    private ByteBuffer bb;
    private byte[] bs;
    private byte[] b1;

    ChannelInputStream(ReadableByteChannel ch) {
        this.ch = ch;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int read(ByteBuffer bb) throws IOException {
        Object object = this.ch;
        if (object instanceof SelectableChannel) {
            SelectableChannel sc = (SelectableChannel)object;
            object = sc.blockingLock();
            synchronized (object) {
                if (!sc.isBlocking()) {
                    throw new IllegalBlockingModeException();
                }
                return this.ch.read(bb);
            }
        }
        return this.ch.read(bb);
    }

    @Override
    public synchronized int read() throws IOException {
        int n;
        if (this.b1 == null) {
            this.b1 = new byte[1];
        }
        if ((n = this.read(this.b1)) == 1) {
            return this.b1[0] & 0xFF;
        }
        return -1;
    }

    @Override
    public synchronized int read(byte[] bs, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, bs.length);
        if (len == 0) {
            return 0;
        }
        ByteBuffer bb = this.bs == bs ? this.bb : ByteBuffer.wrap(bs);
        bb.limit(Math.min(off + len, bb.capacity()));
        bb.position(off);
        this.bb = bb;
        this.bs = bs;
        return this.read(bb);
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        ReadableByteChannel readableByteChannel = this.ch;
        if (!(readableByteChannel instanceof SeekableByteChannel)) {
            return super.readAllBytes();
        }
        SeekableByteChannel sbc = (SeekableByteChannel)readableByteChannel;
        long length = sbc.size();
        long position = sbc.position();
        long size = length - position;
        if (length <= 0L || size <= 0L) {
            return super.readAllBytes();
        }
        if (size > Integer.MAX_VALUE) {
            String msg = String.format("Required array size too large: %d = %d - %d", size, length, position);
            throw new OutOfMemoryError(msg);
        }
        int capacity = (int)size;
        byte[] buf = new byte[capacity];
        int nread = 0;
        while (true) {
            int n;
            if ((n = this.read(buf, nread, capacity - nread)) > 0) {
                nread += n;
                continue;
            }
            if (n < 0 || (n = this.read()) < 0) break;
            capacity = Math.max(ArraysSupport.newLength(capacity, 1, capacity), 8192);
            buf = Arrays.copyOf(buf, capacity);
            buf[nread++] = (byte)n;
        }
        return capacity == nread ? buf : Arrays.copyOf(buf, nread);
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        int n;
        if (len < 0) {
            throw new IllegalArgumentException("len < 0");
        }
        if (len == 0) {
            return new byte[0];
        }
        ReadableByteChannel readableByteChannel = this.ch;
        if (!(readableByteChannel instanceof SeekableByteChannel)) {
            return super.readNBytes(len);
        }
        SeekableByteChannel sbc = (SeekableByteChannel)readableByteChannel;
        long length = sbc.size();
        long position = sbc.position();
        long size = length - position;
        if (length <= 0L || size <= 0L) {
            return super.readNBytes(len);
        }
        int capacity = (int)Math.min((long)len, size);
        byte[] buf = new byte[capacity];
        int remaining = capacity;
        int nread = 0;
        do {
            if ((n = this.read(buf, nread, remaining)) > 0) {
                nread += n;
                remaining -= n;
                continue;
            }
            if (n != 0) continue;
            byte b = (byte)this.read();
            if (b == -1) break;
            buf[nread++] = b;
            --remaining;
        } while (n >= 0 && remaining > 0);
        return capacity == nread ? buf : Arrays.copyOf(buf, nread);
    }

    @Override
    public int available() throws IOException {
        ReadableByteChannel readableByteChannel = this.ch;
        if (readableByteChannel instanceof SeekableByteChannel) {
            SeekableByteChannel sbc = (SeekableByteChannel)readableByteChannel;
            long rem = Math.max(0L, sbc.size() - sbc.position());
            return rem > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)rem;
        }
        return 0;
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        ReadableByteChannel readableByteChannel = this.ch;
        if (readableByteChannel instanceof SeekableByteChannel) {
            long newPos;
            SeekableByteChannel sbc = (SeekableByteChannel)readableByteChannel;
            long pos = sbc.position();
            if (n > 0L) {
                newPos = pos + n;
                long size = sbc.size();
                if (newPos < 0L || newPos > size) {
                    newPos = size;
                }
            } else {
                newPos = Long.max(pos + n, 0L);
            }
            sbc.position(newPos);
            return newPos - pos;
        }
        return super.skip(n);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public long transferTo(OutputStream out) throws IOException {
        ChannelOutputStream cos;
        WritableByteChannel wbc2;
        Objects.requireNonNull(out, "out");
        ReadableByteChannel readableByteChannel = this.ch;
        if (readableByteChannel instanceof FileChannel) {
            FileChannel fc = (FileChannel)readableByteChannel;
            if (out instanceof SocketOutputStream) {
                SocketOutputStream sos = (SocketOutputStream)out;
                SocketChannelImpl sc = sos.channel();
                Object object = sc.blockingLock();
                synchronized (object) {
                    if (!sc.isBlocking()) {
                        throw new IllegalBlockingModeException();
                    }
                    return ChannelInputStream.transfer(fc, sc);
                }
            }
            if (out instanceof ChannelOutputStream) {
                ChannelOutputStream cos2 = (ChannelOutputStream)out;
                WritableByteChannel wbc2 = cos2.channel();
                if (wbc2 instanceof SelectableChannel) {
                    SelectableChannel sc = (SelectableChannel)((Object)wbc2);
                    Object object = sc.blockingLock();
                    synchronized (object) {
                        if (!sc.isBlocking()) {
                            throw new IllegalBlockingModeException();
                        }
                        return ChannelInputStream.transfer(fc, wbc2);
                    }
                }
                return ChannelInputStream.transfer(fc, wbc2);
            }
        }
        if (out instanceof ChannelOutputStream && (wbc2 = (cos = (ChannelOutputStream)out).channel()) instanceof FileChannel) {
            FileChannel fc = (FileChannel)wbc2;
            ReadableByteChannel rbc = this.ch;
            if (rbc instanceof SelectableChannel) {
                SelectableChannel sc = (SelectableChannel)((Object)rbc);
                Object object = sc.blockingLock();
                synchronized (object) {
                    if (!sc.isBlocking()) {
                        throw new IllegalBlockingModeException();
                    }
                    return ChannelInputStream.transfer(rbc, fc);
                }
            }
            return ChannelInputStream.transfer(rbc, fc);
        }
        return super.transferTo(out);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static long transfer(FileChannel fc, WritableByteChannel target) throws IOException {
        long initialPos;
        long pos;
        try {
            for (pos = initialPos = fc.position(); pos < fc.size(); pos += fc.transferTo(pos, Long.MAX_VALUE, target)) {
            }
        }
        finally {
            fc.position(pos);
        }
        return pos - initialPos;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static long transfer(ReadableByteChannel src, FileChannel dst) throws IOException {
        long initialPos;
        long pos = initialPos = dst.position();
        try {
            long n;
            while ((n = dst.transferFrom(src, pos, Long.MAX_VALUE)) > 0L) {
                pos += n;
            }
        }
        finally {
            dst.position(pos);
        }
        return pos - initialPos;
    }

    @Override
    public void close() throws IOException {
        this.ch.close();
    }
}

