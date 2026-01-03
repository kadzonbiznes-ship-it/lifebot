/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf;

import java.awt.Rectangle;
import javax.swing.JTabbedPane;
import javax.swing.plaf.ComponentUI;

public abstract class TabbedPaneUI
extends ComponentUI {
    protected TabbedPaneUI() {
    }

    public abstract int tabForCoordinate(JTabbedPane var1, int var2, int var3);

    public abstract Rectangle getTabBounds(JTabbedPane var1, int var2);

    public abstract int getTabRunCount(JTabbedPane var1);
}

