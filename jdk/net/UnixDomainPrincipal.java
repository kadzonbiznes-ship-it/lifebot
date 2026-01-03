/*
 * Decompiled with CFR 0.152.
 */
package jdk.net;

import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.util.Objects;

public record UnixDomainPrincipal(UserPrincipal user, GroupPrincipal group) {
    public UnixDomainPrincipal {
        Objects.requireNonNull(user);
        Objects.requireNonNull(group);
    }
}

