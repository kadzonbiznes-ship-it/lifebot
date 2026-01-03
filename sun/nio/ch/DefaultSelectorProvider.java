/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.nio.ch.SelectorProviderImpl;
import sun.nio.ch.WEPollSelectorProvider;

public class DefaultSelectorProvider {
    private static final SelectorProviderImpl INSTANCE;

    private DefaultSelectorProvider() {
    }

    public static SelectorProviderImpl get() {
        return INSTANCE;
    }

    static {
        PrivilegedAction<SelectorProviderImpl> pa = WEPollSelectorProvider::new;
        INSTANCE = AccessController.doPrivileged(pa);
    }
}

