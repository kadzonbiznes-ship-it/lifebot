/*
 * Decompiled with CFR 0.152.
 */
package java.security;

import java.io.Serializable;
import java.security.AllPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import sun.security.util.SecurityConstants;

final class AllPermissionCollection
extends PermissionCollection
implements Serializable {
    private static final long serialVersionUID = -4023755556366636806L;
    private boolean all_allowed = false;

    @Override
    public void add(Permission permission) {
        if (!(permission instanceof AllPermission)) {
            throw new IllegalArgumentException("invalid permission: " + permission);
        }
        if (this.isReadOnly()) {
            throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");
        }
        this.all_allowed = true;
    }

    @Override
    public boolean implies(Permission permission) {
        return this.all_allowed;
    }

    @Override
    public Enumeration<Permission> elements() {
        return new Enumeration<Permission>(){
            private boolean hasMore;
            {
                this.hasMore = AllPermissionCollection.this.all_allowed;
            }

            @Override
            public boolean hasMoreElements() {
                return this.hasMore;
            }

            @Override
            public Permission nextElement() {
                this.hasMore = false;
                return SecurityConstants.ALL_PERMISSION;
            }
        };
    }
}

