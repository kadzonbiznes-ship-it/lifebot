/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.net.SocketAddress;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public final class UnixDomainSocketAddress
extends SocketAddress {
    static final long serialVersionUID = 92902496589351288L;
    private final transient Path path;

    private Object writeReplace() throws ObjectStreamException {
        return new Ser(this.path.toString());
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private void readObjectNoData() throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private UnixDomainSocketAddress(Path path) {
        this.path = path;
    }

    public static UnixDomainSocketAddress of(String pathname) {
        return UnixDomainSocketAddress.of(Path.of(pathname, new String[0]));
    }

    public static UnixDomainSocketAddress of(Path path) {
        FileSystem fs = path.getFileSystem();
        if (fs != FileSystems.getDefault()) {
            throw new IllegalArgumentException();
        }
        if (fs.getClass().getModule() != Object.class.getModule()) {
            throw new IllegalArgumentException();
        }
        return new UnixDomainSocketAddress(path);
    }

    public Path getPath() {
        return this.path;
    }

    public int hashCode() {
        return this.path.hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof UnixDomainSocketAddress)) {
            return false;
        }
        UnixDomainSocketAddress that = (UnixDomainSocketAddress)o;
        return this.path.equals(that.path);
    }

    public String toString() {
        return this.path.toString();
    }

    private static final class Ser
    implements Serializable {
        static final long serialVersionUID = -7955684448513979814L;
        private final String pathname;

        Ser(String pathname) {
            this.pathname = pathname;
        }

        private Object readResolve() {
            return UnixDomainSocketAddress.of(this.pathname);
        }
    }
}

