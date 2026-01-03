/*
 * Decompiled with CFR 0.152.
 */
package javax.crypto;

import java.io.Serializable;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.Vector;
import javax.crypto.CryptoAllPermission;
import javax.crypto.CryptoPermission;

final class CryptoAllPermissionCollection
extends PermissionCollection
implements Serializable {
    private static final long serialVersionUID = 7450076868380144072L;
    private boolean all_allowed = false;

    CryptoAllPermissionCollection() {
    }

    @Override
    public void add(Permission permission) {
        if (this.isReadOnly()) {
            throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");
        }
        if (permission != CryptoAllPermission.INSTANCE) {
            return;
        }
        this.all_allowed = true;
    }

    @Override
    public boolean implies(Permission permission) {
        if (!(permission instanceof CryptoPermission)) {
            return false;
        }
        return this.all_allowed;
    }

    @Override
    public Enumeration<Permission> elements() {
        Vector<CryptoAllPermission> v = new Vector<CryptoAllPermission>(1);
        if (this.all_allowed) {
            v.add(CryptoAllPermission.INSTANCE);
        }
        return v.elements();
    }
}

