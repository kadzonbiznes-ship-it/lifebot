/*
 * Decompiled with CFR 0.152.
 */
package java.awt.im.spi;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.im.spi.InputMethod;
import java.util.Locale;

public interface InputMethodDescriptor {
    public Locale[] getAvailableLocales() throws AWTException;

    public boolean hasDynamicLocaleList();

    public String getInputMethodDisplayName(Locale var1, Locale var2);

    public Image getInputMethodIcon(Locale var1);

    public InputMethod createInputMethod() throws Exception;
}

