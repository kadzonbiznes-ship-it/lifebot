/*
 * Decompiled with CFR 0.152.
 */
package java.util.prefs;

import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;
import java.util.prefs.WindowsPreferences;

class WindowsPreferencesFactory
implements PreferencesFactory {
    WindowsPreferencesFactory() {
    }

    @Override
    public Preferences userRoot() {
        return WindowsPreferences.getUserRoot();
    }

    @Override
    public Preferences systemRoot() {
        return WindowsPreferences.getSystemRoot();
    }
}

