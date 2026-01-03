/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.PropertyPermission;
import java.util.concurrent.ConcurrentHashMap;

final class PropertyPermissionCollection
extends PermissionCollection
implements Serializable {
    private transient ConcurrentHashMap<String, PropertyPermission> perms = new ConcurrentHashMap(32);
    private boolean all_allowed = false;
    private static final long serialVersionUID = 7015263904581634791L;
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("permissions", Hashtable.class), new ObjectStreamField("all_allowed", Boolean.TYPE)};

    @Override
    public void add(Permission permission) {
        if (!(permission instanceof PropertyPermission)) {
            throw new IllegalArgumentException("invalid permission: " + permission);
        }
        PropertyPermission pp = (PropertyPermission)permission;
        if (this.isReadOnly()) {
            throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");
        }
        String propName = pp.getName();
        this.perms.merge(propName, pp, (existingVal, newVal) -> {
            int newMask;
            int oldMask = existingVal.getMask();
            if (oldMask != (newMask = newVal.getMask())) {
                int effective = oldMask | newMask;
                if (effective == newMask) {
                    return newVal;
                }
                if (effective != oldMask) {
                    return new PropertyPermission(propName, effective);
                }
            }
            return existingVal;
        });
        if (!this.all_allowed && propName.equals("*")) {
            this.all_allowed = true;
        }
    }

    @Override
    public boolean implies(Permission permission) {
        int last;
        PropertyPermission x;
        if (!(permission instanceof PropertyPermission)) {
            return false;
        }
        PropertyPermission pp = (PropertyPermission)permission;
        int desired = pp.getMask();
        int effective = 0;
        if (this.all_allowed && (x = this.perms.get("*")) != null && ((effective |= x.getMask()) & desired) == desired) {
            return true;
        }
        String name = pp.getName();
        x = this.perms.get(name);
        if (x != null && ((effective |= x.getMask()) & desired) == desired) {
            return true;
        }
        int offset = name.length() - 1;
        while ((last = name.lastIndexOf(46, offset)) != -1) {
            x = this.perms.get(name = name.substring(0, last + 1) + "*");
            if (x != null && ((effective |= x.getMask()) & desired) == desired) {
                return true;
            }
            offset = last - 1;
        }
        return false;
    }

    @Override
    public Enumeration<Permission> elements() {
        return this.perms.elements();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        Hashtable<String, PropertyPermission> permissions = new Hashtable<String, PropertyPermission>(this.perms.size() * 2);
        permissions.putAll(this.perms);
        ObjectOutputStream.PutField pfields = out.putFields();
        pfields.put("all_allowed", this.all_allowed);
        pfields.put("permissions", permissions);
        out.writeFields();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField gfields = in.readFields();
        this.all_allowed = gfields.get("all_allowed", false);
        Hashtable permissions = (Hashtable)gfields.get("permissions", null);
        this.perms = new ConcurrentHashMap(permissions.size() * 2);
        this.perms.putAll(permissions);
    }
}

