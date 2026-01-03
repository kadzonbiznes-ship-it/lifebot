/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.net.InetAddress;
import java.net.NetPermission;
import java.net.PasswordAuthentication;
import java.net.URL;

public abstract class Authenticator {
    private static volatile Authenticator theAuthenticator;
    private String requestingHost;
    private InetAddress requestingSite;
    private int requestingPort;
    private String requestingProtocol;
    private String requestingPrompt;
    private String requestingScheme;
    private URL requestingURL;
    private RequestorType requestingAuthType;

    private void reset() {
        this.requestingHost = null;
        this.requestingSite = null;
        this.requestingPort = -1;
        this.requestingProtocol = null;
        this.requestingPrompt = null;
        this.requestingScheme = null;
        this.requestingURL = null;
        this.requestingAuthType = RequestorType.SERVER;
    }

    public static synchronized void setDefault(Authenticator a) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            NetPermission setDefaultPermission = new NetPermission("setDefaultAuthenticator");
            sm.checkPermission(setDefaultPermission);
        }
        theAuthenticator = a;
    }

    public static Authenticator getDefault() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            NetPermission requestPermission = new NetPermission("requestPasswordAuthentication");
            sm.checkPermission(requestPermission);
        }
        return theAuthenticator;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static PasswordAuthentication requestPasswordAuthentication(InetAddress addr, int port, String protocol, String prompt, String scheme) {
        Authenticator a;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            NetPermission requestPermission = new NetPermission("requestPasswordAuthentication");
            sm.checkPermission(requestPermission);
        }
        if ((a = theAuthenticator) == null) {
            return null;
        }
        Authenticator authenticator = a;
        synchronized (authenticator) {
            a.reset();
            a.requestingSite = addr;
            a.requestingPort = port;
            a.requestingProtocol = protocol;
            a.requestingPrompt = prompt;
            a.requestingScheme = scheme;
            return a.getPasswordAuthentication();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static PasswordAuthentication requestPasswordAuthentication(String host, InetAddress addr, int port, String protocol, String prompt, String scheme) {
        Authenticator a;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            NetPermission requestPermission = new NetPermission("requestPasswordAuthentication");
            sm.checkPermission(requestPermission);
        }
        if ((a = theAuthenticator) == null) {
            return null;
        }
        Authenticator authenticator = a;
        synchronized (authenticator) {
            a.reset();
            a.requestingHost = host;
            a.requestingSite = addr;
            a.requestingPort = port;
            a.requestingProtocol = protocol;
            a.requestingPrompt = prompt;
            a.requestingScheme = scheme;
            return a.getPasswordAuthentication();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static PasswordAuthentication requestPasswordAuthentication(String host, InetAddress addr, int port, String protocol, String prompt, String scheme, URL url, RequestorType reqType) {
        Authenticator a;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            NetPermission requestPermission = new NetPermission("requestPasswordAuthentication");
            sm.checkPermission(requestPermission);
        }
        if ((a = theAuthenticator) == null) {
            return null;
        }
        Authenticator authenticator = a;
        synchronized (authenticator) {
            a.reset();
            a.requestingHost = host;
            a.requestingSite = addr;
            a.requestingPort = port;
            a.requestingProtocol = protocol;
            a.requestingPrompt = prompt;
            a.requestingScheme = scheme;
            a.requestingURL = url;
            a.requestingAuthType = reqType;
            return a.getPasswordAuthentication();
        }
    }

    public static PasswordAuthentication requestPasswordAuthentication(Authenticator authenticator, String host, InetAddress addr, int port, String protocol, String prompt, String scheme, URL url, RequestorType reqType) {
        Authenticator a;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            NetPermission requestPermission = new NetPermission("requestPasswordAuthentication");
            sm.checkPermission(requestPermission);
        }
        Authenticator authenticator2 = a = authenticator == null ? theAuthenticator : authenticator;
        if (a == null) {
            return null;
        }
        return a.requestPasswordAuthenticationInstance(host, addr, port, protocol, prompt, scheme, url, reqType);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public PasswordAuthentication requestPasswordAuthenticationInstance(String host, InetAddress addr, int port, String protocol, String prompt, String scheme, URL url, RequestorType reqType) {
        Authenticator authenticator = this;
        synchronized (authenticator) {
            this.reset();
            this.requestingHost = host;
            this.requestingSite = addr;
            this.requestingPort = port;
            this.requestingProtocol = protocol;
            this.requestingPrompt = prompt;
            this.requestingScheme = scheme;
            this.requestingURL = url;
            this.requestingAuthType = reqType;
            return this.getPasswordAuthentication();
        }
    }

    protected final String getRequestingHost() {
        return this.requestingHost;
    }

    protected final InetAddress getRequestingSite() {
        return this.requestingSite;
    }

    protected final int getRequestingPort() {
        return this.requestingPort;
    }

    protected final String getRequestingProtocol() {
        return this.requestingProtocol;
    }

    protected final String getRequestingPrompt() {
        return this.requestingPrompt;
    }

    protected final String getRequestingScheme() {
        return this.requestingScheme;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return null;
    }

    protected URL getRequestingURL() {
        return this.requestingURL;
    }

    protected RequestorType getRequestorType() {
        return this.requestingAuthType;
    }

    public static enum RequestorType {
        PROXY,
        SERVER;

    }
}

