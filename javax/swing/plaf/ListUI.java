/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf;

import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JList;
import javax.swing.plaf.ComponentUI;

public abstract class ListUI
extends ComponentUI {
    protected ListUI() {
    }

    public abstract int locationToIndex(JList<?> var1, Point var2);

    public abstract Point indexToLocation(JList<?> var1, int var2);

    public abstract Rectangle getCellBounds(JList<?> var1, int var2, int var3);
}

