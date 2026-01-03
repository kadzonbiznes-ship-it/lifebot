/*
 * Decompiled with CFR 0.152.
 */
package java.nio.channels;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import sun.nio.ch.Streams;
import sun.nio.cs.StreamDecoder;
import sun.nio.cs.StreamEncoder;

public final class Channels {
    private Channels() {
        throw new Error("no instances");
    }

    public static InputStream newInputStream(ReadableByteChannel ch) {
        Objects.requireNonNull(ch, "ch");
        return Streams.of(ch);
    }

    public static OutputStream newOutputStream(WritableByteChannel ch) {
        Objects.requireNonNull(ch, "ch");
        return Streams.of(ch);
    }

    public static InputStream newInputStream(final AsynchronousByteChannel ch) {
        Objects.requireNonNull(ch, "ch");
        return new InputStream(){
            private ByteBuffer bb;
            private byte[] bs;
            private byte[] b1;

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
                bb.position(off);
                bb.limit(Math.min(off + len, bb.capacity()));
                this.bb = bb;
                this.bs = bs;
                boolean interrupted = false;
                while (true) {
                    try {
                        int n = ch.read(bb).get();
                        return n;
                    }
                    catch (ExecutionException ee) {
                        throw new IOException(ee.getCause());
                    }
                    catch (InterruptedException ie) {
                        interrupted = true;
                        continue;
                    }
                    break;
                }
                finally {
                    if (interrupted) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            @Override
            public void close() throws IOException {
                ch.close();
            }
        };
    }

    public static OutputStream newOutputStream(final AsynchronousByteChannel ch) {
        Objects.requireNonNull(ch, "ch");
        return new OutputStream(){
            private ByteBuffer bb;
            private byte[] bs;
            private byte[] b1;

            @Override
            public synchronized void write(int b) throws IOException {
                if (this.b1 == null) {
                    this.b1 = new byte[1];
                }
                this.b1[0] = (byte)b;
                this.write(this.b1);
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public synchronized void write(byte[] bs, int off, int len) throws IOException {
                if (off < 0 || off > bs.length || len < 0 || off + len > bs.length || off + len < 0) {
                    throw new IndexOutOfBoundsException();
                }
                if (len == 0) {
                    return;
                }
                ByteBuffer bb = this.bs == bs ? this.bb : ByteBuffer.wrap(bs);
                bb.limit(Math.min(off + len, bb.capacity()));
                bb.position(off);
                this.bb = bb;
                this.bs = bs;
                boolean interrupted = false;
                try {
                    while (bb.remaining() > 0) {
                        try {
                            ch.write(bb).get();
                        }
                        catch (ExecutionException ee) {
                            throw new IOException(ee.getCause());
                        }
                        catch (InterruptedException ie) {
                            interrupted = true;
                        }
                    }
                }
                finally {
                    if (interrupted) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            @Override
            public void close() throws IOException {
                ch.close();
            }
        };
    }

    public static ReadableByteChannel newChannel(InputStream in) {
        Objects.requireNonNull(in, "in");
        if (in.getClass() == FileInputStream.class) {
            return ((FileInputStream)in).getChannel();
        }
        return new ReadableByteChannelImpl(in);
    }

    public static WritableByteChannel newChannel(OutputStream out) {
        Objects.requireNonNull(out, "out");
        if (out.getClass() == FileOutputStream.class) {
            return ((FileOutputStream)out).getChannel();
        }
        return new WritableByteChannelImpl(out);
    }

    public static Reader newReader(ReadableByteChannel ch, CharsetDecoder dec, int minBufferCap) {
        Objects.requireNonNull(ch, "ch");
        return StreamDecoder.forDecoder(ch, dec.reset(), minBufferCap);
    }

    public static Reader newReader(ReadableByteChannel ch, String csName) {
        Objects.requireNonNull(csName, "csName");
        return Channels.newReader(ch, Charset.forName(csName).newDecoder(), -1);
    }

    public static Reader newReader(ReadableByteChannel ch, Charset charset) {
        Objects.requireNonNull(charset, "charset");
        return Channels.newReader(ch, charset.newDecoder(), -1);
    }

    public static Writer newWriter(WritableByteChannel ch, CharsetEncoder enc, int minBufferCap) {
        Objects.requireNonNull(ch, "ch");
        return StreamEncoder.forEncoder((WritableByteChannel)ch, (CharsetEncoder)enc.reset(), (int)minBufferCap);
    }

    public static Writer newWriter(WritableByteChannel ch, String csName) {
        Objects.requireNonNull(csName, "csName");
        return Channels.newWriter(ch, Charset.forName(csName).newEncoder(), -1);
    }

    public static Writer newWriter(WritableByteChannel ch, Charset charset) {
        Objects.requireNonNull(charset, "charset");
        return Channels.newWriter(ch, charset.newEncoder(), -1);
    }

    private static class ReadableByteChannelImpl
    extends AbstractInterruptibleChannel
    implements ReadableByteChannel {
        private final InputStream in;
        private static final int TRANSFER_SIZE = 8192;
        private byte[] buf = new byte[0];
        private final Object readLock = new Object();

        ReadableByteChannelImpl(InputStream in) {
            this.in = in;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public int read(ByteBuffer dst) throws IOException {
            if (!this.isOpen()) {
                throw new ClosedChannelException();
            }
            if (dst.isReadOnly()) {
                throw new IllegalArgumentException();
            }
            int len = dst.remaining();
            int bytesRead = 0;
            Object object = this.readLock;
            synchronized (object) {
                int totalRead;
                for (totalRead = 0; totalRead < len; totalRead += bytesRead) {
                    int bytesToRead = Math.min(len - totalRead, 8192);
                    if (this.buf.length < bytesToRead) {
                        this.buf = new byte[bytesToRead];
                    }
                    if (totalRead > 0 && this.in.available() <= 0) break;
                    try {
                        this.begin();
                        bytesRead = this.in.read(this.buf, 0, bytesToRead);
                        this.end(bytesRead > 0);
                    }
                    catch (Throwable throwable) {
                        this.end(bytesRead > 0);
                        throw throwable;
                    }
                    if (bytesRead < 0) break;
                    dst.put(this.buf, 0, bytesRead);
                }
                if (bytesRead < 0 && totalRead == 0) {
                    return -1;
                }
                return totalRead;
            }
        }

        @Override
        protected void implCloseChannel() throws IOException {
            this.in.close();
        }
    }

    private static class WritableByteChannelImpl
    extends AbstractInterruptibleChannel
    implements WritableByteChannel {
        private final OutputStream out;
        private static final int TRANSFER_SIZE = 8192;
        private byte[] buf = new byte[0];
        private final Object writeLock = new Object();

        WritableByteChannelImpl(OutputStream out) {
            this.out = out;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public int write(ByteBuffer src) throws IOException {
            if (!this.isOpen()) {
                throw new ClosedChannelException();
            }
            int len = src.remaining();
            Object object = this.writeLock;
            synchronized (object) {
                int totalWritten;
                int bytesToWrite;
                for (totalWritten = 0; totalWritten < len; totalWritten += bytesToWrite) {
                    bytesToWrite = Math.min(len - totalWritten, 8192);
                    if (this.buf.length < bytesToWrite) {
                        this.buf = new byte[bytesToWrite];
                    }
                    src.get(this.buf, 0, bytesToWrite);
                    try {
                        this.begin();
                        this.out.write(this.buf, 0, bytesToWrite);
                        this.end(bytesToWrite > 0);
                    }
                    catch (Throwable throwable) {
                        this.end(bytesToWrite > 0);
                        throw throwable;
                    }
                }
                return totalWritten;
            }
        }

        @Override
        protected void implCloseChannel() throws IOException {
            this.out.close();
        }
    }
}

