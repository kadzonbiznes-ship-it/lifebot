/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ViewportUI;

public class BasicViewportUI
extends ViewportUI {
    private static ViewportUI viewportUI;

    public static ComponentUI createUI(JComponent c) {
        if (viewportUI == null) {
            viewportUI = new BasicViewportUI();
        }
        return viewportUI;
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.installDefaults(c);
    }

    @Override
    public void uninstallUI(JComponent c) {
        this.uninstallDefaults(c);
        super.uninstallUI(c);
    }

    protected void installDefaults(JComponent c) {
        LookAndFeel.installColorsAndFont(c, "Viewport.background", "Viewport.foreground", "Viewport.font");
        LookAndFeel.installProperty(c, "opaque", Boolean.TRUE);
    }

    protected void uninstallDefaults(JComponent c) {
    }
}

