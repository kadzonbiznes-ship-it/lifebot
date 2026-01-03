/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.DelegatingSocketImpl;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.SocketTimeoutException;
import java.net.SocksConsts;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Iterator;
import jdk.internal.util.StaticProperty;
import sun.net.SocksProxy;
import sun.net.spi.DefaultProxySelector;
import sun.net.www.ParseUtil;

class SocksSocketImpl
extends DelegatingSocketImpl
implements SocksConsts {
    private String server = null;
    private int serverPort = 1080;
    private InetSocketAddress external_address;
    private boolean useV4 = false;
    private Socket cmdsock = null;
    private InputStream cmdIn = null;
    private OutputStream cmdOut = null;

    SocksSocketImpl(SocketImpl delegate) {
        super(delegate);
    }

    SocksSocketImpl(Proxy proxy, SocketImpl delegate) {
        super(delegate);
        SocketAddress a = proxy.address();
        if (a instanceof InetSocketAddress) {
            InetSocketAddress ad = (InetSocketAddress)a;
            this.server = ad.getHostString();
            this.serverPort = ad.getPort();
        }
        this.useV4 = SocksSocketImpl.useV4(proxy);
    }

    private static boolean useV4(Proxy proxy) {
        if (proxy instanceof SocksProxy && ((SocksProxy)proxy).protocolVersion() == 4) {
            return true;
        }
        return DefaultProxySelector.socksProxyVersion() == 4;
    }

    private synchronized void privilegedConnect(final String host, final int port, final int timeout) throws IOException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>(this){
                final /* synthetic */ SocksSocketImpl this$0;
                {
                    this.this$0 = this$0;
                }

                @Override
                public Void run() throws IOException {
                    this.this$0.superConnectServer(host, port, timeout);
                    this.this$0.cmdIn = this.this$0.getInputStream();
                    this.this$0.cmdOut = this.this$0.getOutputStream();
                    return null;
                }
            });
        }
        catch (PrivilegedActionException pae) {
            throw (IOException)pae.getException();
        }
    }

    private void superConnectServer(String host, int port, int timeout) throws IOException {
        this.delegate.connect(new InetSocketAddress(host, port), timeout);
    }

    private static int remainingMillis(long deadlineMillis) throws IOException {
        if (deadlineMillis == 0L) {
            return 0;
        }
        long remaining = deadlineMillis - System.currentTimeMillis();
        if (remaining > 0L) {
            return (int)remaining;
        }
        throw new SocketTimeoutException();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int readSocksReply(InputStream in, byte[] data, long deadlineMillis) throws IOException {
        int received;
        int len = data.length;
        int originalTimeout = (Integer)this.getOption(4102);
        try {
            int count;
            for (received = 0; received < len; received += count) {
                int remaining = SocksSocketImpl.remainingMillis(deadlineMillis);
                this.setOption(4102, (Object)remaining);
                try {
                    count = in.read(data, received, len - received);
                }
                catch (SocketTimeoutException e) {
                    throw new SocketTimeoutException("Connect timed out");
                }
                if (count >= 0) continue;
                throw new SocketException("Malformed reply from SOCKS server");
            }
        }
        finally {
            this.setOption(4102, (Object)originalTimeout);
        }
        return received;
    }

    private boolean authenticate(byte method, InputStream in, BufferedOutputStream out, long deadlineMillis) throws IOException {
        if (method == 0) {
            return true;
        }
        if (method == 2) {
            String userName;
            String password = null;
            final InetAddress addr = InetAddress.getByName(this.server);
            PasswordAuthentication pw = AccessController.doPrivileged(new PrivilegedAction<PasswordAuthentication>(this){
                final /* synthetic */ SocksSocketImpl this$0;
                {
                    this.this$0 = this$0;
                }

                @Override
                public PasswordAuthentication run() {
                    return Authenticator.requestPasswordAuthentication(this.this$0.server, addr, this.this$0.serverPort, "SOCKS5", "SOCKS authentication", null);
                }
            });
            if (pw != null) {
                userName = pw.getUserName();
                password = new String(pw.getPassword());
            } else {
                userName = StaticProperty.userName();
            }
            if (userName == null) {
                return false;
            }
            out.write(1);
            out.write(userName.length());
            out.write(userName.getBytes(StandardCharsets.ISO_8859_1));
            if (password != null) {
                out.write(password.length());
                out.write(password.getBytes(StandardCharsets.ISO_8859_1));
            } else {
                out.write(0);
            }
            out.flush();
            byte[] data = new byte[2];
            int i = this.readSocksReply(in, data, deadlineMillis);
            if (i != 2 || data[1] != 0) {
                out.close();
                in.close();
                return false;
            }
            return true;
        }
        return false;
    }

    private void connectV4(InputStream in, OutputStream out, InetSocketAddress endpoint, long deadlineMillis) throws IOException {
        SocketException ex;
        if (!(endpoint.getAddress() instanceof Inet4Address)) {
            throw new SocketException("SOCKS V4 requires IPv4 only addresses");
        }
        out.write(4);
        out.write(1);
        out.write(endpoint.getPort() >> 8 & 0xFF);
        out.write(endpoint.getPort() >> 0 & 0xFF);
        out.write(endpoint.getAddress().getAddress());
        String userName = this.getUserName();
        out.write(userName.getBytes(StandardCharsets.ISO_8859_1));
        out.write(0);
        out.flush();
        byte[] data = new byte[8];
        int n = this.readSocksReply(in, data, deadlineMillis);
        if (n != 8) {
            throw new SocketException("Reply from SOCKS server has bad length: " + n);
        }
        if (data[0] != 0 && data[0] != 4) {
            throw new SocketException("Reply from SOCKS server has bad version");
        }
        switch (data[1]) {
            case 90: {
                this.external_address = endpoint;
                SocketException socketException = null;
                break;
            }
            case 91: {
                SocketException socketException = new SocketException("SOCKS request rejected");
                break;
            }
            case 92: {
                SocketException socketException = new SocketException("SOCKS server couldn't reach destination");
                break;
            }
            case 93: {
                SocketException socketException = new SocketException("SOCKS authentication failed");
                break;
            }
            default: {
                SocketException socketException = ex = new SocketException("Reply from SOCKS server contains bad status");
            }
        }
        if (ex != null) {
            in.close();
            out.close();
            throw ex;
        }
    }

    @Override
    protected void connect(String host, int port) throws IOException {
        this.connect(new InetSocketAddress(host, port), 0);
    }

    @Override
    protected void connect(InetAddress address, int port) throws IOException {
        this.connect(new InetSocketAddress(address, port), 0);
    }

    @Override
    protected void connect(SocketAddress endpoint, int timeout) throws IOException {
        long finish;
        long deadlineMillis = timeout == 0 ? 0L : ((finish = System.currentTimeMillis() + (long)timeout) < 0L ? Long.MAX_VALUE : finish);
        SecurityManager security = System.getSecurityManager();
        if (!(endpoint instanceof InetSocketAddress)) {
            throw new IllegalArgumentException("Unsupported address type");
        }
        InetSocketAddress epoint = (InetSocketAddress)endpoint;
        if (security != null) {
            if (epoint.isUnresolved()) {
                security.checkConnect(epoint.getHostName(), epoint.getPort());
            } else {
                security.checkConnect(epoint.getAddress().getHostAddress(), epoint.getPort());
            }
        }
        if (this.server == null) {
            Iterator<Proxy> iProxy;
            URI uri;
            ProxySelector sel = AccessController.doPrivileged(new PrivilegedAction<ProxySelector>(this){

                @Override
                public ProxySelector run() {
                    return ProxySelector.getDefault();
                }
            });
            if (sel == null) {
                this.delegate.connect(epoint, SocksSocketImpl.remainingMillis(deadlineMillis));
                return;
            }
            String host = epoint.getHostString();
            if (epoint.getAddress() instanceof Inet6Address && !host.startsWith("[") && host.indexOf(58) >= 0) {
                host = "[" + host + "]";
            }
            try {
                uri = new URI("socket://" + ParseUtil.encodePath(host) + ":" + epoint.getPort());
            }
            catch (URISyntaxException e) {
                assert (false) : e;
                uri = null;
            }
            Proxy p = null;
            Throwable savedExc = null;
            try {
                iProxy = sel.select(uri).iterator();
            }
            catch (IllegalArgumentException iae) {
                throw new IOException("Failed to select a proxy", iae);
            }
            if (iProxy == null || !iProxy.hasNext()) {
                this.delegate.connect(epoint, SocksSocketImpl.remainingMillis(deadlineMillis));
                return;
            }
            while (iProxy.hasNext()) {
                p = iProxy.next();
                if (p == null || p.type() != Proxy.Type.SOCKS) {
                    this.delegate.connect(epoint, SocksSocketImpl.remainingMillis(deadlineMillis));
                    return;
                }
                if (!(p.address() instanceof InetSocketAddress)) {
                    throw new SocketException("Unknown address type for proxy: " + p);
                }
                this.server = ((InetSocketAddress)p.address()).getHostString();
                this.serverPort = ((InetSocketAddress)p.address()).getPort();
                this.useV4 = SocksSocketImpl.useV4(p);
                try {
                    this.privilegedConnect(this.server, this.serverPort, SocksSocketImpl.remainingMillis(deadlineMillis));
                    break;
                }
                catch (IOException e) {
                    sel.connectFailed(uri, p.address(), e);
                    this.server = null;
                    this.serverPort = -1;
                    savedExc = e;
                }
            }
            if (this.server == null) {
                throw new SocketException("Can't connect to SOCKS proxy:" + savedExc.getMessage());
            }
        } else {
            try {
                this.privilegedConnect(this.server, this.serverPort, SocksSocketImpl.remainingMillis(deadlineMillis));
            }
            catch (IOException e) {
                throw new SocketException(e.getMessage(), e);
            }
        }
        BufferedOutputStream out = new BufferedOutputStream(this.cmdOut, 512);
        InputStream in = this.cmdIn;
        if (this.useV4) {
            if (epoint.isUnresolved()) {
                throw new UnknownHostException(epoint.toString());
            }
            this.connectV4(in, out, epoint, deadlineMillis);
            return;
        }
        out.write(5);
        out.write(2);
        out.write(0);
        out.write(2);
        out.flush();
        byte[] data = new byte[2];
        int i = this.readSocksReply(in, data, deadlineMillis);
        if (i != 2 || data[0] != 5) {
            if (epoint.isUnresolved()) {
                throw new UnknownHostException(epoint.toString());
            }
            this.connectV4(in, out, epoint, deadlineMillis);
            return;
        }
        if (data[1] == -1) {
            throw new SocketException("SOCKS : No acceptable methods");
        }
        if (!this.authenticate(data[1], in, out, deadlineMillis)) {
            throw new SocketException("SOCKS : authentication failed");
        }
        out.write(5);
        out.write(1);
        out.write(0);
        if (epoint.isUnresolved()) {
            out.write(3);
            out.write(epoint.getHostName().length());
            out.write(epoint.getHostName().getBytes(StandardCharsets.ISO_8859_1));
            out.write(epoint.getPort() >> 8 & 0xFF);
            out.write(epoint.getPort() >> 0 & 0xFF);
        } else if (epoint.getAddress() instanceof Inet6Address) {
            out.write(4);
            out.write(epoint.getAddress().getAddress());
            out.write(epoint.getPort() >> 8 & 0xFF);
            out.write(epoint.getPort() >> 0 & 0xFF);
        } else {
            out.write(1);
            out.write(epoint.getAddress().getAddress());
            out.write(epoint.getPort() >> 8 & 0xFF);
            out.write(epoint.getPort() >> 0 & 0xFF);
        }
        out.flush();
        data = new byte[4];
        i = this.readSocksReply(in, data, deadlineMillis);
        if (i != 4) {
            throw new SocketException("Reply from SOCKS server has bad length");
        }
        SocketException ex = null;
        block4 : switch (data[1]) {
            case 0: {
                switch (data[3]) {
                    case 1: {
                        byte[] addr = new byte[4];
                        i = this.readSocksReply(in, addr, deadlineMillis);
                        if (i != 4) {
                            throw new SocketException("Reply from SOCKS server badly formatted");
                        }
                        data = new byte[2];
                        i = this.readSocksReply(in, data, deadlineMillis);
                        if (i == 2) break block4;
                        throw new SocketException("Reply from SOCKS server badly formatted");
                    }
                    case 3: {
                        byte[] lenByte = new byte[1];
                        i = this.readSocksReply(in, lenByte, deadlineMillis);
                        if (i != 1) {
                            throw new SocketException("Reply from SOCKS server badly formatted");
                        }
                        int len = lenByte[0] & 0xFF;
                        byte[] host = new byte[len];
                        i = this.readSocksReply(in, host, deadlineMillis);
                        if (i != len) {
                            throw new SocketException("Reply from SOCKS server badly formatted");
                        }
                        data = new byte[2];
                        i = this.readSocksReply(in, data, deadlineMillis);
                        if (i == 2) break block4;
                        throw new SocketException("Reply from SOCKS server badly formatted");
                    }
                    case 4: {
                        int len = 16;
                        byte[] addr = new byte[len];
                        i = this.readSocksReply(in, addr, deadlineMillis);
                        if (i != len) {
                            throw new SocketException("Reply from SOCKS server badly formatted");
                        }
                        data = new byte[2];
                        i = this.readSocksReply(in, data, deadlineMillis);
                        if (i == 2) break block4;
                        throw new SocketException("Reply from SOCKS server badly formatted");
                    }
                }
                ex = new SocketException("Reply from SOCKS server contains wrong code");
                break;
            }
            case 1: {
                ex = new SocketException("SOCKS server general failure");
                break;
            }
            case 2: {
                ex = new SocketException("SOCKS: Connection not allowed by ruleset");
                break;
            }
            case 3: {
                ex = new SocketException("SOCKS: Network unreachable");
                break;
            }
            case 4: {
                ex = new SocketException("SOCKS: Host unreachable");
                break;
            }
            case 5: {
                ex = new SocketException("SOCKS: Connection refused");
                break;
            }
            case 6: {
                ex = new SocketException("SOCKS: TTL expired");
                break;
            }
            case 7: {
                ex = new SocketException("SOCKS: Command not supported");
                break;
            }
            case 8: {
                ex = new SocketException("SOCKS: address type not supported");
            }
        }
        if (ex != null) {
            in.close();
            out.close();
            throw ex;
        }
        this.external_address = epoint;
    }

    @Override
    protected void listen(int backlog) {
        throw new InternalError("should not get here");
    }

    @Override
    protected void accept(SocketImpl s) {
        throw new InternalError("should not get here");
    }

    @Override
    protected InetAddress getInetAddress() {
        if (this.external_address != null) {
            return this.external_address.getAddress();
        }
        return this.delegate.getInetAddress();
    }

    @Override
    protected int getPort() {
        if (this.external_address != null) {
            return this.external_address.getPort();
        }
        return this.delegate.getPort();
    }

    @Override
    protected void close() throws IOException {
        if (this.cmdsock != null) {
            this.cmdsock.close();
        }
        this.cmdsock = null;
        this.delegate.close();
    }

    private String getUserName() {
        return StaticProperty.userName();
    }

    @Override
    void reset() {
        throw new InternalError("should not get here");
    }
}

