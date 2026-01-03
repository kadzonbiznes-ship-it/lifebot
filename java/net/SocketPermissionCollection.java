/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.net.SocketPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

final class SocketPermissionCollection
extends PermissionCollection
implements Serializable {
    private transient Map<String, SocketPermission> perms = new ConcurrentHashMap<String, SocketPermission>();
    private static final long serialVersionUID = 2787186408602843674L;
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("permissions", Vector.class)};

    @Override
    public void add(Permission permission) {
        if (!(permission instanceof SocketPermission)) {
            throw new IllegalArgumentException("invalid permission: " + permission);
        }
        SocketPermission sp = (SocketPermission)permission;
        if (this.isReadOnly()) {
            throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");
        }
        this.perms.merge(sp.getName(), sp, (existingVal, newVal) -> {
            int newMask;
            int oldMask = existingVal.getMask();
            if (oldMask != (newMask = newVal.getMask())) {
                int effective = oldMask | newMask;
                if (effective == newMask) {
                    return newVal;
                }
                if (effective != oldMask) {
                    return new SocketPermission(sp.getName(), effective);
                }
            }
            return existingVal;
        });
    }

    @Override
    public boolean implies(Permission permission) {
        if (!(permission instanceof SocketPermission)) {
            return false;
        }
        SocketPermission np = (SocketPermission)permission;
        int desired = np.getMask();
        int effective = 0;
        int needed = desired;
        SocketPermission hit = this.perms.get(np.getName());
        if (hit != null && (needed & hit.getMask()) != 0 && hit.impliesIgnoreMask(np)) {
            if (((effective |= hit.getMask()) & desired) == desired) {
                return true;
            }
            needed = desired & ~effective;
        }
        for (SocketPermission x : this.perms.values()) {
            if ((needed & x.getMask()) == 0 || !x.impliesIgnoreMask(np)) continue;
            if (((effective |= x.getMask()) & desired) == desired) {
                return true;
            }
            needed = desired & ~effective;
        }
        return false;
    }

    @Override
    public Enumeration<Permission> elements() {
        return Collections.enumeration(this.perms.values());
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        Vector<SocketPermission> permissions = new Vector<SocketPermission>(this.perms.values());
        ObjectOutputStream.PutField pfields = out.putFields();
        pfields.put("permissions", permissions);
        out.writeFields();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField gfields = in.readFields();
        Vector permissions = (Vector)gfields.get("permissions", null);
        this.perms = new ConcurrentHashMap<String, SocketPermission>(permissions.size());
        for (SocketPermission sp : permissions) {
            this.perms.put(sp.getName(), sp);
        }
    }
}

