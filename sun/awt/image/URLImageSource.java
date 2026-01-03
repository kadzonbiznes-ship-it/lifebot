/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import sun.awt.image.ImageDecoder;
import sun.awt.image.InputStreamImageSource;
import sun.net.util.URLUtil;

public class URLImageSource
extends InputStreamImageSource {
    URL url;
    URLConnection conn;
    String actualHost;
    int actualPort;

    public URLImageSource(URL u) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            try {
                Permission perm = URLUtil.getConnectPermission(u);
                if (perm != null) {
                    sm.checkPermission(perm);
                }
            }
            catch (IOException ioe) {
                sm.checkConnect(u.getHost(), u.getPort());
            }
        }
        this.url = u;
    }

    public URLImageSource(String href) throws MalformedURLException {
        this(URLImageSource.newURL(null, href));
    }

    public URLImageSource(URL u, URLConnection uc) {
        this(u);
        this.conn = uc;
    }

    public URLImageSource(URLConnection uc) {
        this(uc.getURL(), uc);
    }

    @Override
    final boolean checkSecurity(Object context, boolean quiet) {
        if (this.actualHost != null) {
            try {
                SecurityManager security = System.getSecurityManager();
                if (security != null) {
                    security.checkConnect(this.actualHost, this.actualPort, context);
                }
            }
            catch (SecurityException e) {
                if (!quiet) {
                    throw e;
                }
                return false;
            }
        }
        return true;
    }

    private synchronized URLConnection getConnection() throws IOException {
        URLConnection c;
        if (this.conn != null) {
            c = this.conn;
            this.conn = null;
        } else {
            c = this.url.openConnection();
        }
        return c;
    }

    @Override
    protected ImageDecoder getDecoder() {
        InputStream is = null;
        String type = null;
        URLConnection c = null;
        try {
            c = this.getConnection();
            is = c.getInputStream();
            type = c.getContentType();
            URL u = c.getURL();
            if (!(u == this.url || u.getHost().equals(this.url.getHost()) && u.getPort() == this.url.getPort())) {
                if (!(this.actualHost == null || this.actualHost.equals(u.getHost()) && this.actualPort == u.getPort())) {
                    throw new SecurityException("image moved!");
                }
                this.actualHost = u.getHost();
                this.actualPort = u.getPort();
            }
        }
        catch (IOException e) {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException iOException) {}
            } else if (c instanceof HttpURLConnection) {
                ((HttpURLConnection)c).disconnect();
            }
            return null;
        }
        ImageDecoder id = this.decoderForType(is, type);
        if (id == null) {
            id = this.getDecoder(is);
        }
        if (id == null) {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException iOException) {}
            } else if (c instanceof HttpURLConnection) {
                ((HttpURLConnection)c).disconnect();
            }
        }
        return id;
    }

    private static URL newURL(URL context, String spec) throws MalformedURLException {
        return new URL(context, spec);
    }
}

