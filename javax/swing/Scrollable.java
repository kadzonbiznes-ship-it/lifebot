/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Dimension;
import java.awt.Rectangle;

public interface Scrollable {
    public Dimension getPreferredScrollableViewportSize();

    public int getScrollableUnitIncrement(Rectangle var1, int var2, int var3);

    public int getScrollableBlockIncrement(Rectangle var1, int var2, int var3);

    public boolean getScrollableTracksViewportWidth();

    public boolean getScrollableTracksViewportHeight();
}

