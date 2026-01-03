/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Event;
import java.awt.Font;
import java.awt.MenuComponent;

public interface MenuContainer {
    public Font getFont();

    public void remove(MenuComponent var1);

    @Deprecated
    public boolean postEvent(Event var1);
}

