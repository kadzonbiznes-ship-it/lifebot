/*
 * Decompiled with CFR 0.152.
 */
package java.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.EventListener;

public interface VetoableChangeListener
extends EventListener {
    public void vetoableChange(PropertyChangeEvent var1) throws PropertyVetoException;
}

