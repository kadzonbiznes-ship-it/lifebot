/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.file.Files;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.SecureRandom;
import java.util.Random;
import sun.nio.ch.SinkChannelImpl;
import sun.nio.ch.SourceChannelImpl;
import sun.nio.ch.UnixDomainSockets;

class PipeImpl
extends Pipe {
    private static final int NUM_SECRET_BYTES = 16;
    private static final Random RANDOM_NUMBER_GENERATOR = new SecureRandom();
    private final SourceChannelImpl source;
    private final SinkChannelImpl sink;

    PipeImpl(SelectorProvider sp) throws IOException {
        this(sp, false, true);
    }

    PipeImpl(SelectorProvider sp, boolean preferAfUnix, boolean buffering) throws IOException {
        Initializer initializer = new Initializer(sp, preferAfUnix);
        try {
            AccessController.doPrivileged(initializer);
            SinkChannelImpl sink = initializer.sink;
            if (sink.isNetSocket() && !buffering) {
                sink.setOption(StandardSocketOptions.TCP_NODELAY, true);
            }
        }
        catch (PrivilegedActionException pae) {
            throw (IOException)pae.getCause();
        }
        this.source = initializer.source;
        this.sink = initializer.sink;
    }

    @Override
    public SourceChannelImpl source() {
        return this.source;
    }

    @Override
    public SinkChannelImpl sink() {
        return this.sink;
    }

    private static ServerSocketChannel createListener(boolean preferUnixDomain) throws IOException {
        ServerSocketChannel listener;
        block3: {
            listener = null;
            if (preferUnixDomain && UnixDomainSockets.isSupported()) {
                try {
                    listener = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
                    listener.bind(null);
                    return listener;
                }
                catch (IOException | UnsupportedOperationException e) {
                    if (listener == null) break block3;
                    listener.close();
                }
            }
        }
        listener = ServerSocketChannel.open();
        InetAddress lb = InetAddress.getLoopbackAddress();
        listener.bind(new InetSocketAddress(lb, 0));
        return listener;
    }

    private static class Initializer
    implements PrivilegedExceptionAction<Void> {
        private final SelectorProvider sp;
        private final boolean preferUnixDomain;
        private IOException ioe;
        SourceChannelImpl source;
        SinkChannelImpl sink;

        private Initializer(SelectorProvider sp, boolean preferUnixDomain) {
            this.sp = sp;
            this.preferUnixDomain = preferUnixDomain;
        }

        @Override
        public Void run() throws IOException {
            LoopbackConnector connector = new LoopbackConnector();
            connector.run();
            if (this.ioe instanceof ClosedByInterruptException) {
                this.ioe = null;
                Thread connThread = new Thread(this, (Runnable)connector){

                    @Override
                    public void interrupt() {
                    }
                };
                connThread.start();
                while (true) {
                    try {
                        connThread.join();
                    }
                    catch (InterruptedException interruptedException) {
                        continue;
                    }
                    break;
                }
                Thread.currentThread().interrupt();
            }
            if (this.ioe != null) {
                throw new IOException("Unable to establish loopback connection", this.ioe);
            }
            return null;
        }

        private class LoopbackConnector
        implements Runnable {
            private LoopbackConnector() {
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                AbstractInterruptibleChannel ssc = null;
                AbstractInterruptibleChannel sc1 = null;
                AbstractInterruptibleChannel sc2 = null;
                SocketAddress sa = null;
                try {
                    ByteBuffer secret = ByteBuffer.allocate(16);
                    ByteBuffer bb = ByteBuffer.allocate(16);
                    while (true) {
                        if (ssc == null || !ssc.isOpen()) {
                            ssc = PipeImpl.createListener(Initializer.this.preferUnixDomain);
                            sa = ((ServerSocketChannel)ssc).getLocalAddress();
                        }
                        sc1 = SocketChannel.open(sa);
                        RANDOM_NUMBER_GENERATOR.nextBytes(secret.array());
                        do {
                            ((SocketChannel)sc1).write(secret);
                        } while (secret.hasRemaining());
                        secret.rewind();
                        sc2 = ((ServerSocketChannel)ssc).accept();
                        do {
                            ((SocketChannel)sc2).read(bb);
                        } while (bb.hasRemaining());
                        bb.rewind();
                        if (bb.equals(secret)) break;
                        sc2.close();
                        sc1.close();
                    }
                    Initializer.this.source = new SourceChannelImpl(Initializer.this.sp, (SocketChannel)sc1);
                    Initializer.this.sink = new SinkChannelImpl(Initializer.this.sp, (SocketChannel)sc2);
                }
                catch (IOException e) {
                    try {
                        if (sc1 != null) {
                            sc1.close();
                        }
                        if (sc2 != null) {
                            sc2.close();
                        }
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                    Initializer.this.ioe = e;
                }
                finally {
                    try {
                        if (ssc != null) {
                            ssc.close();
                        }
                        if (sa instanceof UnixDomainSocketAddress) {
                            UnixDomainSocketAddress uaddr = (UnixDomainSocketAddress)sa;
                            Files.deleteIfExists(uaddr.getPath());
                        }
                    }
                    catch (IOException iOException) {}
                }
            }
        }
    }
}

