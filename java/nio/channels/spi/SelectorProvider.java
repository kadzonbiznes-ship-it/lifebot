/*
 * Decompiled with CFR 0.152.
 */
package java.nio.channels.spi;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ProtocolFamily;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import sun.nio.ch.DefaultSelectorProvider;

public abstract class SelectorProvider {
    private static Void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("selectorProvider"));
        }
        return null;
    }

    private SelectorProvider(Void ignore) {
    }

    protected SelectorProvider() {
        this(SelectorProvider.checkPermission());
    }

    public static SelectorProvider provider() {
        return Holder.INSTANCE;
    }

    public abstract DatagramChannel openDatagramChannel() throws IOException;

    public abstract DatagramChannel openDatagramChannel(ProtocolFamily var1) throws IOException;

    public abstract Pipe openPipe() throws IOException;

    public abstract AbstractSelector openSelector() throws IOException;

    public abstract ServerSocketChannel openServerSocketChannel() throws IOException;

    public abstract SocketChannel openSocketChannel() throws IOException;

    public Channel inheritedChannel() throws IOException {
        return null;
    }

    public SocketChannel openSocketChannel(ProtocolFamily family) throws IOException {
        Objects.requireNonNull(family);
        throw new UnsupportedOperationException("Protocol family not supported");
    }

    public ServerSocketChannel openServerSocketChannel(ProtocolFamily family) throws IOException {
        Objects.requireNonNull(family);
        throw new UnsupportedOperationException("Protocol family not supported");
    }

    private static class Holder {
        static final SelectorProvider INSTANCE = Holder.provider();

        private Holder() {
        }

        static SelectorProvider provider() {
            PrivilegedAction<SelectorProvider> pa = () -> {
                SelectorProvider sp = Holder.loadProviderFromProperty();
                if (sp != null) {
                    return sp;
                }
                sp = Holder.loadProviderAsService();
                if (sp != null) {
                    return sp;
                }
                return DefaultSelectorProvider.get();
            };
            return AccessController.doPrivileged(pa);
        }

        private static SelectorProvider loadProviderFromProperty() {
            String cn = System.getProperty("java.nio.channels.spi.SelectorProvider");
            if (cn == null) {
                return null;
            }
            try {
                Class<?> clazz = Class.forName(cn, true, ClassLoader.getSystemClassLoader());
                return (SelectorProvider)clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
            }
            catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException x) {
                throw new ServiceConfigurationError(null, x);
            }
        }

        private static SelectorProvider loadProviderAsService() {
            ServiceLoader<SelectorProvider> sl = ServiceLoader.load(SelectorProvider.class, ClassLoader.getSystemClassLoader());
            Iterator<SelectorProvider> i = sl.iterator();
            while (true) {
                try {
                    return i.hasNext() ? i.next() : null;
                }
                catch (ServiceConfigurationError sce) {
                    if (sce.getCause() instanceof SecurityException) continue;
                    throw sce;
                }
                break;
            }
        }
    }
}

