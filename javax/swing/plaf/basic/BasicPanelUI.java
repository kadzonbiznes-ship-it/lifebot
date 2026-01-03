/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.PanelUI;

public class BasicPanelUI
extends PanelUI {
    private static PanelUI panelUI;

    public static ComponentUI createUI(JComponent c) {
        if (panelUI == null) {
            panelUI = new BasicPanelUI();
        }
        return panelUI;
    }

    @Override
    public void installUI(JComponent c) {
        JPanel p = (JPanel)c;
        super.installUI(p);
        this.installDefaults(p);
    }

    @Override
    public void uninstallUI(JComponent c) {
        JPanel p = (JPanel)c;
        this.uninstallDefaults(p);
        super.uninstallUI(c);
    }

    protected void installDefaults(JPanel p) {
        LookAndFeel.installColorsAndFont(p, "Panel.background", "Panel.foreground", "Panel.font");
        LookAndFeel.installBorder(p, "Panel.border");
        LookAndFeel.installProperty(p, "opaque", Boolean.TRUE);
    }

    protected void uninstallDefaults(JPanel p) {
        LookAndFeel.uninstallBorder(p);
    }

    @Override
    public int getBaseline(JComponent c, int width, int height) {
        super.getBaseline(c, width, height);
        Border border = c.getBorder();
        if (border instanceof AbstractBorder) {
            return ((AbstractBorder)border).getBaseline(c, width, height);
        }
        return -1;
    }

    @Override
    public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
        super.getBaselineResizeBehavior(c);
        Border border = c.getBorder();
        if (border instanceof AbstractBorder) {
            return ((AbstractBorder)border).getBaselineResizeBehavior(c);
        }
        return Component.BaselineResizeBehavior.OTHER;
    }
}

