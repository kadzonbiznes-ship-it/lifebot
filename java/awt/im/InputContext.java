/*
 * Decompiled with CFR 0.152.
 */
package java.awt.im;

import java.awt.AWTEvent;
import java.awt.Component;
import java.beans.Transient;
import java.util.Locale;
import java.util.Objects;
import sun.awt.im.InputMethodContext;

public class InputContext {
    protected InputContext() {
    }

    public static InputContext getInstance() {
        return new InputMethodContext();
    }

    public boolean selectInputMethod(Locale locale) {
        Objects.requireNonNull(locale);
        return false;
    }

    public Locale getLocale() {
        return null;
    }

    public void setCharacterSubsets(Character.Subset[] subsets) {
    }

    public void setCompositionEnabled(boolean enable) {
        throw new UnsupportedOperationException();
    }

    @Transient
    public boolean isCompositionEnabled() {
        throw new UnsupportedOperationException();
    }

    public void reconvert() {
        throw new UnsupportedOperationException();
    }

    public void dispatchEvent(AWTEvent event) {
        Objects.requireNonNull(event);
    }

    public void removeNotify(Component client) {
        Objects.requireNonNull(client);
    }

    public void endComposition() {
    }

    public void dispose() {
    }

    public Object getInputMethodControlObject() {
        return null;
    }
}

