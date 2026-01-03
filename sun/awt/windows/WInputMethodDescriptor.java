/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.windows;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.im.spi.InputMethod;
import java.awt.im.spi.InputMethodDescriptor;
import java.util.Locale;
import sun.awt.windows.WInputMethod;

final class WInputMethodDescriptor
implements InputMethodDescriptor {
    WInputMethodDescriptor() {
    }

    @Override
    public Locale[] getAvailableLocales() {
        Locale[] locales = WInputMethodDescriptor.getAvailableLocalesInternal();
        Locale[] tmp = new Locale[locales.length];
        System.arraycopy(locales, 0, tmp, 0, locales.length);
        return tmp;
    }

    static Locale[] getAvailableLocalesInternal() {
        return WInputMethodDescriptor.getNativeAvailableLocales();
    }

    @Override
    public boolean hasDynamicLocaleList() {
        return true;
    }

    @Override
    public synchronized String getInputMethodDisplayName(Locale inputLocale, Locale displayLanguage) {
        String name = "System Input Methods";
        if (Locale.getDefault().equals(displayLanguage)) {
            name = Toolkit.getProperty("AWT.HostInputMethodDisplayName", name);
        }
        return name;
    }

    @Override
    public Image getInputMethodIcon(Locale inputLocale) {
        return null;
    }

    @Override
    public InputMethod createInputMethod() throws Exception {
        return new WInputMethod();
    }

    private static native Locale[] getNativeAvailableLocales();
}

