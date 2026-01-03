/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import sun.nio.ch.ChannelInputStream;
import sun.nio.ch.ChannelOutputStream;
import sun.nio.ch.SocketChannelImpl;
import sun.nio.ch.SocketInputStream;
import sun.nio.ch.SocketOutputStream;

public class Streams {
    private Streams() {
    }

    public static InputStream of(ReadableByteChannel ch) {
        if (ch instanceof SocketChannelImpl) {
            SocketChannelImpl sc = (SocketChannelImpl)ch;
            return new SocketInputStream(sc);
        }
        return new ChannelInputStream(ch);
    }

    public static OutputStream of(WritableByteChannel ch) {
        if (ch instanceof SocketChannelImpl) {
            SocketChannelImpl sc = (SocketChannelImpl)ch;
            return new SocketOutputStream(sc);
        }
        return new ChannelOutputStream(ch);
    }
}

