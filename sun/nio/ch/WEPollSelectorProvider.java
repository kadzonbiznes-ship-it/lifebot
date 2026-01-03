/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.IOException;
import java.nio.channels.spi.AbstractSelector;
import sun.nio.ch.SelectorProviderImpl;
import sun.nio.ch.WEPollSelectorImpl;

public class WEPollSelectorProvider
extends SelectorProviderImpl {
    @Override
    public AbstractSelector openSelector() throws IOException {
        return new WEPollSelectorImpl(this);
    }
}

