/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.security.Key;
import java.util.Date;
import java.util.Set;

public interface ConstraintsParameters {
    public boolean anchorIsJdkCA();

    public Set<Key> getKeys();

    public Date getDate();

    public String getVariant();

    public String extendedExceptionMsg();
}

