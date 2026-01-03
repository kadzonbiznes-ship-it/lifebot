/*
 * Decompiled with CFR 0.152.
 */
package java.awt.peer;

import java.awt.Insets;
import java.awt.peer.ComponentPeer;

public interface ContainerPeer
extends ComponentPeer {
    public Insets getInsets();

    public void beginValidate();

    public void endValidate();

    public void beginLayout();

    public void endLayout();
}

