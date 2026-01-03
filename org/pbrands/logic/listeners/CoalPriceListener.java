/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.logic.listeners;

import java.util.ArrayList;
import java.util.List;

public interface CoalPriceListener {
    public static final List<CoalPriceListener> listeners = new ArrayList<CoalPriceListener>();

    public void coalPriceUpdated(double var1);

    public static void addListener(CoalPriceListener listener) {
        listeners.add(listener);
    }
}

