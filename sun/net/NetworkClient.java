/*
 * Decompiled with CFR 0.152.
 */
package sun.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;

public class NetworkClient {
    public static final int DEFAULT_READ_TIMEOUT = -1;
    public static final int DEFAULT_CONNECT_TIMEOUT = -1;
    protected Proxy proxy = Proxy.NO_PROXY;
    protected Socket serverSocket = null;
    public PrintStream serverOutput;
    public InputStream serverInput;
    protected static int defaultSoTimeout;
    protected static int defaultConnectTimeout;
    protected int readTimeout = -1;
    protected int connectTimeout = -1;
    protected static String encoding;

    private static boolean isASCIISuperset(String encoding) throws Exception {
        String chkS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_.!~*'();/?:@&=+$,";
        byte[] chkB = new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 45, 95, 46, 33, 126, 42, 39, 40, 41, 59, 47, 63, 58, 64, 38, 61, 43, 36, 44};
        byte[] b = chkS.getBytes(encoding);
        return Arrays.equals(b, chkB);
    }

    public void openServer(String server, int port) throws IOException, UnknownHostException {
        if (this.serverSocket != null) {
            this.closeServer();
        }
        this.serverSocket = this.doConnect(server, port);
        try {
            this.serverOutput = new PrintStream((OutputStream)new BufferedOutputStream(this.serverSocket.getOutputStream()), true, encoding);
        }
        catch (UnsupportedEncodingException e) {
            throw new InternalError(encoding + "encoding not found", e);
        }
        this.serverInput = new BufferedInputStream(this.serverSocket.getInputStream());
    }

    protected Socket doConnect(String server, int port) throws IOException, UnknownHostException {
        Socket s = this.proxy != null ? (this.proxy.type() == Proxy.Type.SOCKS ? AccessController.doPrivileged(new PrivilegedAction<Socket>(){

            @Override
            public Socket run() {
                return new Socket(NetworkClient.this.proxy);
            }
        }) : (this.proxy.type() == Proxy.Type.DIRECT ? this.createSocket() : new Socket(Proxy.NO_PROXY))) : this.createSocket();
        if (this.connectTimeout >= 0) {
            s.connect(new InetSocketAddress(server, port), this.connectTimeout);
        } else if (defaultConnectTimeout > 0) {
            s.connect(new InetSocketAddress(server, port), defaultConnectTimeout);
        } else {
            s.connect(new InetSocketAddress(server, port));
        }
        if (this.readTimeout >= 0) {
            s.setSoTimeout(this.readTimeout);
        } else if (defaultSoTimeout > 0) {
            s.setSoTimeout(defaultSoTimeout);
        }
        return s;
    }

    protected Socket createSocket() throws IOException {
        return new Socket(Proxy.NO_PROXY);
    }

    protected InetAddress getLocalAddress() throws IOException {
        if (this.serverSocket == null) {
            throw new IOException("not connected");
        }
        return AccessController.doPrivileged(new PrivilegedAction<InetAddress>(){

            @Override
            public InetAddress run() {
                return NetworkClient.this.serverSocket.getLocalAddress();
            }
        });
    }

    public void closeServer() throws IOException {
        if (!this.serverIsOpen()) {
            return;
        }
        this.serverSocket.close();
        this.serverSocket = null;
        this.serverInput = null;
        this.serverOutput = null;
    }

    public boolean serverIsOpen() {
        return this.serverSocket != null;
    }

    public NetworkClient(String host, int port) throws IOException {
        this.openServer(host, port);
    }

    public NetworkClient() {
    }

    public void setConnectTimeout(int timeout) {
        this.connectTimeout = timeout;
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    public void setReadTimeout(int timeout) {
        if (timeout == -1) {
            timeout = defaultSoTimeout;
        }
        if (this.serverSocket != null && timeout >= 0) {
            try {
                this.serverSocket.setSoTimeout(timeout);
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        this.readTimeout = timeout;
    }

    public int getReadTimeout() {
        return this.readTimeout;
    }

    static {
        final int[] vals = new int[]{0, 0};
        final String[] encs = new String[]{null};
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Void run() {
                vals[0] = Integer.getInteger("sun.net.client.defaultReadTimeout", 0);
                vals[1] = Integer.getInteger("sun.net.client.defaultConnectTimeout", 0);
                encs[0] = System.getProperty("file.encoding", "ISO8859_1");
                return null;
            }
        });
        if (vals[0] != 0) {
            defaultSoTimeout = vals[0];
        }
        if (vals[1] != 0) {
            defaultConnectTimeout = vals[1];
        }
        encoding = encs[0];
        try {
            if (!NetworkClient.isASCIISuperset(encoding)) {
                encoding = "ISO8859_1";
            }
        }
        catch (Exception e) {
            encoding = "ISO8859_1";
        }
    }
}

