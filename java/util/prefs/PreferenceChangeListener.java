/*
 * Decompiled with CFR 0.152.
 */
package java.util.prefs;

import java.util.EventListener;
import java.util.prefs.PreferenceChangeEvent;

@FunctionalInterface
public interface PreferenceChangeListener
extends EventListener {
    public void preferenceChange(PreferenceChangeEvent var1);
}

