/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.peer.LightweightPeer;
import sun.awt.NullComponentPeer;

final class LightweightPeerHolder {
    static final LightweightPeer lightweightMarker = new NullComponentPeer();

    private LightweightPeerHolder() {
    }
}

