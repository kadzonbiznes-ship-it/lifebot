/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.MenuSelectionManager;

public interface MenuElement {
    public void processMouseEvent(MouseEvent var1, MenuElement[] var2, MenuSelectionManager var3);

    public void processKeyEvent(KeyEvent var1, MenuElement[] var2, MenuSelectionManager var3);

    public void menuSelectionChanged(boolean var1);

    public MenuElement[] getSubElements();

    public Component getComponent();
}

