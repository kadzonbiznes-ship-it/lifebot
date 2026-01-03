/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.HostPortrange;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class URLPermission
extends Permission {
    private static final long serialVersionUID = -2702463814894478682L;
    private transient String scheme;
    private transient String ssp;
    private transient String path;
    private transient List<String> methods;
    private transient List<String> requestHeaders;
    private transient Authority authority;
    private String actions;

    public URLPermission(String url, String actions) {
        super(URLPermission.normalize(url));
        this.init(actions);
    }

    private static String normalize(String url) {
        int index = url.indexOf(63);
        if (index >= 0) {
            url = url.substring(0, index);
        } else {
            index = url.indexOf(35);
            if (index >= 0) {
                url = url.substring(0, index);
            }
        }
        return url;
    }

    private void init(String actions) {
        String headers;
        String methods;
        this.parseURI(this.getName());
        int colon = actions.indexOf(58);
        if (actions.lastIndexOf(58) != colon) {
            throw new IllegalArgumentException("Invalid actions string: \"" + actions + "\"");
        }
        if (colon == -1) {
            methods = actions;
            headers = "";
        } else {
            methods = actions.substring(0, colon);
            headers = actions.substring(colon + 1);
        }
        List<String> l = this.normalizeMethods(methods);
        Collections.sort(l);
        this.methods = Collections.unmodifiableList(l);
        l = this.normalizeHeaders(headers);
        Collections.sort(l);
        this.requestHeaders = Collections.unmodifiableList(l);
        this.actions = this.actions();
    }

    public URLPermission(String url) {
        this(url, "*:*");
    }

    @Override
    public String getActions() {
        return this.actions;
    }

    @Override
    public boolean implies(Permission p) {
        if (!(p instanceof URLPermission)) {
            return false;
        }
        URLPermission that = (URLPermission)p;
        if (this.methods.isEmpty() && !that.methods.isEmpty()) {
            return false;
        }
        if (!this.methods.isEmpty() && !this.methods.get(0).equals("*") && Collections.indexOfSubList(this.methods, that.methods) == -1) {
            return false;
        }
        if (this.requestHeaders.isEmpty() && !that.requestHeaders.isEmpty()) {
            return false;
        }
        if (!this.requestHeaders.isEmpty() && !this.requestHeaders.get(0).equals("*") && Collections.indexOfSubList(this.requestHeaders, that.requestHeaders) == -1) {
            return false;
        }
        if (!this.scheme.equals(that.scheme)) {
            return false;
        }
        if (this.ssp.equals("*")) {
            return true;
        }
        if (!this.authority.implies(that.authority)) {
            return false;
        }
        if (this.path == null) {
            return that.path == null;
        }
        if (that.path == null) {
            return false;
        }
        if (this.path.endsWith("/-")) {
            String thisprefix = this.path.substring(0, this.path.length() - 1);
            return that.path.startsWith(thisprefix);
        }
        if (this.path.endsWith("/*")) {
            String thisprefix = this.path.substring(0, this.path.length() - 1);
            if (!that.path.startsWith(thisprefix)) {
                return false;
            }
            String thatsuffix = that.path.substring(thisprefix.length());
            if (thatsuffix.indexOf(47) != -1) {
                return false;
            }
            return !thatsuffix.equals("-");
        }
        return this.path.equals(that.path);
    }

    @Override
    public boolean equals(Object p) {
        if (!(p instanceof URLPermission)) {
            return false;
        }
        URLPermission that = (URLPermission)p;
        if (!this.scheme.equals(that.scheme)) {
            return false;
        }
        if (!this.getActions().equals(that.getActions())) {
            return false;
        }
        if (!this.authority.equals(that.authority)) {
            return false;
        }
        if (this.path != null) {
            return this.path.equals(that.path);
        }
        return that.path == null;
    }

    @Override
    public int hashCode() {
        return this.getActions().hashCode() + this.scheme.hashCode() + this.authority.hashCode() + (this.path == null ? 0 : this.path.hashCode());
    }

    private List<String> normalizeMethods(String methods) {
        ArrayList<String> l = new ArrayList<String>();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < methods.length(); ++i) {
            char c = methods.charAt(i);
            if (c == ',') {
                String s = b.toString();
                if (!s.isEmpty()) {
                    l.add(s);
                }
                b = new StringBuilder();
                continue;
            }
            if (c == ' ' || c == '\t') {
                throw new IllegalArgumentException("White space not allowed in methods: \"" + methods + "\"");
            }
            if (c >= 'a' && c <= 'z') {
                c = (char)(c + 65504);
            }
            b.append(c);
        }
        String s = b.toString();
        if (!s.isEmpty()) {
            l.add(s);
        }
        return l;
    }

    private List<String> normalizeHeaders(String headers) {
        ArrayList<String> l = new ArrayList<String>();
        StringBuilder b = new StringBuilder();
        boolean capitalizeNext = true;
        for (int i = 0; i < headers.length(); ++i) {
            char c = headers.charAt(i);
            if (c >= 'a' && c <= 'z') {
                if (capitalizeNext) {
                    c = (char)(c + 65504);
                    capitalizeNext = false;
                }
                b.append(c);
                continue;
            }
            if (c == ' ' || c == '\t') {
                throw new IllegalArgumentException("White space not allowed in headers: \"" + headers + "\"");
            }
            if (c == '-') {
                capitalizeNext = true;
                b.append(c);
                continue;
            }
            if (c == ',') {
                String s = b.toString();
                if (!s.isEmpty()) {
                    l.add(s);
                }
                b = new StringBuilder();
                capitalizeNext = true;
                continue;
            }
            capitalizeNext = false;
            b.append(c);
        }
        String s = b.toString();
        if (!s.isEmpty()) {
            l.add(s);
        }
        return l;
    }

    private void parseURI(String url) {
        String auth;
        int len = url.length();
        int delim = url.indexOf(58);
        if (delim == -1 || delim + 1 == len) {
            throw new IllegalArgumentException("Invalid URL string: \"" + url + "\"");
        }
        this.scheme = url.substring(0, delim).toLowerCase(Locale.ROOT);
        this.ssp = url.substring(delim + 1);
        if (!this.ssp.startsWith("//")) {
            if (!this.ssp.equals("*")) {
                throw new IllegalArgumentException("Invalid URL string: \"" + url + "\"");
            }
            this.authority = new Authority(this.scheme, "*");
            return;
        }
        String authpath = this.ssp.substring(2);
        delim = authpath.indexOf(47);
        if (delim == -1) {
            this.path = "";
            auth = authpath;
        } else {
            auth = authpath.substring(0, delim);
            this.path = authpath.substring(delim);
        }
        this.authority = new Authority(this.scheme, auth.toLowerCase(Locale.ROOT));
    }

    private String actions() {
        return String.join((CharSequence)",", this.methods) + ":" + String.join((CharSequence)",", this.requestHeaders);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = s.readFields();
        String actions = (String)fields.get("actions", null);
        this.init(actions);
    }

    static class Authority {
        HostPortrange p;

        Authority(String scheme, String authority) {
            int at = authority.indexOf(64);
            this.p = at == -1 ? new HostPortrange(scheme, authority) : new HostPortrange(scheme, authority.substring(at + 1));
        }

        boolean implies(Authority other) {
            return this.impliesHostrange(other) && this.impliesPortrange(other);
        }

        private boolean impliesHostrange(Authority that) {
            String thishost = this.p.hostname();
            String thathost = that.p.hostname();
            if (this.p.wildcard() && thishost.isEmpty()) {
                return true;
            }
            if (that.p.wildcard() && thathost.isEmpty()) {
                return false;
            }
            if (thishost.equals(thathost)) {
                return true;
            }
            if (this.p.wildcard()) {
                return thathost.endsWith(thishost);
            }
            return false;
        }

        private boolean impliesPortrange(Authority that) {
            int[] thisrange = this.p.portrange();
            int[] thatrange = that.p.portrange();
            if (thisrange[0] == -1) {
                return true;
            }
            return thisrange[0] <= thatrange[0] && thisrange[1] >= thatrange[1];
        }

        boolean equals(Authority that) {
            return this.p.equals(that.p);
        }

        public int hashCode() {
            return this.p.hashCode();
        }
    }
}

