/*
 * Decompiled with CFR 0.152.
 */
package java.awt.im.spi;

import java.awt.Window;
import java.awt.font.TextHitInfo;
import java.awt.im.InputMethodRequests;
import java.awt.im.spi.InputMethod;
import java.text.AttributedCharacterIterator;
import javax.swing.JFrame;

public interface InputMethodContext
extends InputMethodRequests {
    public void dispatchInputMethodEvent(int var1, AttributedCharacterIterator var2, int var3, TextHitInfo var4, TextHitInfo var5);

    public Window createInputMethodWindow(String var1, boolean var2);

    public JFrame createInputMethodJFrame(String var1, boolean var2);

    public void enableClientWindowNotification(InputMethod var1, boolean var2);
}

