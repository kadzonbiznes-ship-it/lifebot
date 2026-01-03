/*
 * Decompiled with CFR 0.152.
 */
package javax.crypto;

import java.security.Permission;
import java.security.PermissionCollection;
import javax.crypto.CryptoAllPermissionCollection;
import javax.crypto.CryptoPermission;

final class CryptoAllPermission
extends CryptoPermission {
    private static final long serialVersionUID = -5066513634293192112L;
    static final String ALG_NAME = "CryptoAllPermission";
    static final CryptoAllPermission INSTANCE = new CryptoAllPermission();

    private CryptoAllPermission() {
        super(ALG_NAME);
    }

    @Override
    public boolean implies(Permission p) {
        return p instanceof CryptoPermission;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == INSTANCE;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public PermissionCollection newPermissionCollection() {
        return new CryptoAllPermissionCollection();
    }
}

