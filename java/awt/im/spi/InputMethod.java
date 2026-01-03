/*
 * Decompiled with CFR 0.152.
 */
package java.awt.im.spi;

import java.awt.AWTEvent;
import java.awt.Rectangle;
import java.awt.im.spi.InputMethodContext;
import java.util.Locale;

public interface InputMethod {
    public void setInputMethodContext(InputMethodContext var1);

    public boolean setLocale(Locale var1);

    public Locale getLocale();

    public void setCharacterSubsets(Character.Subset[] var1);

    public void setCompositionEnabled(boolean var1);

    public boolean isCompositionEnabled();

    public void reconvert();

    public void dispatchEvent(AWTEvent var1);

    public void notifyClientWindowChange(Rectangle var1);

    public void activate();

    public void deactivate(boolean var1);

    public void hideWindows();

    public void removeNotify();

    public void endComposition();

    public void dispose();

    public Object getControlObject();
}

