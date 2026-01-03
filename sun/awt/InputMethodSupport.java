/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.AWTException;
import java.awt.Window;
import java.awt.im.spi.InputMethodDescriptor;
import java.util.Locale;
import sun.awt.im.InputContext;

public interface InputMethodSupport {
    public InputMethodDescriptor getInputMethodAdapterDescriptor() throws AWTException;

    public Window createInputMethodWindow(String var1, InputContext var2);

    public boolean enableInputMethodsForTextComponent();

    public Locale getDefaultKeyboardLocale();
}

