/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.BasicPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.PropertyPermissionCollection;

public final class PropertyPermission
extends BasicPermission {
    private static final int READ = 1;
    private static final int WRITE = 2;
    private static final int ALL = 3;
    private static final int NONE = 0;
    private transient int mask;
    private String actions;
    private static final long serialVersionUID = 885438825399942851L;

    private void init(int mask) {
        if ((mask & 3) != mask) {
            throw new IllegalArgumentException("invalid actions mask");
        }
        if (mask == 0) {
            throw new IllegalArgumentException("invalid actions mask");
        }
        if (this.getName() == null) {
            throw new NullPointerException("name can't be null");
        }
        this.mask = mask;
    }

    public PropertyPermission(String name, String actions) {
        super(name, actions);
        this.init(PropertyPermission.getMask(actions));
    }

    PropertyPermission(String name, int mask) {
        super(name, PropertyPermission.getActions(mask));
        this.mask = mask;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean implies(Permission p) {
        if (!(p instanceof PropertyPermission)) return false;
        PropertyPermission that = (PropertyPermission)p;
        if ((this.mask & that.mask) != that.mask) return false;
        if (!super.implies(that)) return false;
        return true;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PropertyPermission)) return false;
        PropertyPermission that = (PropertyPermission)obj;
        if (this.mask != that.mask) return false;
        if (!this.getName().equals(that.getName())) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    private static int getMask(String actions) {
        int mask = 0;
        if (actions == null) {
            return mask;
        }
        if (actions == "read") {
            return 1;
        }
        if (actions == "write") {
            return 2;
        }
        if (actions == "read,write") {
            return 3;
        }
        char[] a = actions.toCharArray();
        int i = a.length - 1;
        if (i < 0) {
            return mask;
        }
        while (i != -1) {
            int matchlen;
            char c;
            while (i != -1 && ((c = a[i]) == ' ' || c == '\r' || c == '\n' || c == '\f' || c == '\t')) {
                --i;
            }
            if (!(i < 3 || a[i - 3] != 'r' && a[i - 3] != 'R' || a[i - 2] != 'e' && a[i - 2] != 'E' || a[i - 1] != 'a' && a[i - 1] != 'A' || a[i] != 'd' && a[i] != 'D')) {
                matchlen = 4;
                mask |= 1;
            } else if (!(i < 4 || a[i - 4] != 'w' && a[i - 4] != 'W' || a[i - 3] != 'r' && a[i - 3] != 'R' || a[i - 2] != 'i' && a[i - 2] != 'I' || a[i - 1] != 't' && a[i - 1] != 'T' || a[i] != 'e' && a[i] != 'E')) {
                matchlen = 5;
                mask |= 2;
            } else {
                throw new IllegalArgumentException("invalid permission: " + actions);
            }
            boolean seencomma = false;
            while (i >= matchlen && !seencomma) {
                switch (a[i - matchlen]) {
                    case ',': {
                        seencomma = true;
                        break;
                    }
                    case '\t': 
                    case '\n': 
                    case '\f': 
                    case '\r': 
                    case ' ': {
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("invalid permission: " + actions);
                    }
                }
                --i;
            }
            i -= matchlen;
        }
        return mask;
    }

    static String getActions(int mask) {
        return switch (mask & 3) {
            case 1 -> "read";
            case 2 -> "write";
            case 3 -> "read,write";
            default -> "";
        };
    }

    @Override
    public String getActions() {
        if (this.actions == null) {
            this.actions = PropertyPermission.getActions(this.mask);
        }
        return this.actions;
    }

    int getMask() {
        return this.mask;
    }

    @Override
    public PermissionCollection newPermissionCollection() {
        return new PropertyPermissionCollection();
    }

    private synchronized void writeObject(ObjectOutputStream s) throws IOException {
        if (this.actions == null) {
            this.getActions();
        }
        s.defaultWriteObject();
    }

    private synchronized void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.init(PropertyPermission.getMask(this.actions));
    }
}

