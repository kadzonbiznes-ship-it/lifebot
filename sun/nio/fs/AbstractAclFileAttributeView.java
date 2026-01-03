/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.fs;

import java.io.IOException;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sun.nio.fs.DynamicFileAttributeView;

abstract class AbstractAclFileAttributeView
implements AclFileAttributeView,
DynamicFileAttributeView {
    private static final String OWNER_NAME = "owner";
    private static final String ACL_NAME = "acl";

    AbstractAclFileAttributeView() {
    }

    @Override
    public final String name() {
        return ACL_NAME;
    }

    @Override
    public final void setAttribute(String attribute, Object value) throws IOException {
        if (attribute.equals(OWNER_NAME)) {
            this.setOwner((UserPrincipal)value);
            return;
        }
        if (attribute.equals(ACL_NAME)) {
            this.setAcl((List)value);
            return;
        }
        throw new IllegalArgumentException("'" + this.name() + ":" + attribute + "' not recognized");
    }

    @Override
    public final Map<String, Object> readAttributes(String[] attributes) throws IOException {
        boolean acl = false;
        boolean owner = false;
        for (String attribute : attributes) {
            if (attribute.equals("*")) {
                owner = true;
                acl = true;
                continue;
            }
            if (attribute.equals(ACL_NAME)) {
                acl = true;
                continue;
            }
            if (attribute.equals(OWNER_NAME)) {
                owner = true;
                continue;
            }
            throw new IllegalArgumentException("'" + this.name() + ":" + attribute + "' not recognized");
        }
        HashMap<String, Object> result = new HashMap<String, Object>(2);
        if (acl) {
            result.put(ACL_NAME, this.getAcl());
        }
        if (owner) {
            result.put(OWNER_NAME, this.getOwner());
        }
        return Collections.unmodifiableMap(result);
    }
}

