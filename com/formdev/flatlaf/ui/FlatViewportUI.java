/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.Component;
import java.awt.Graphics;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicViewportUI;

public class FlatViewportUI
extends BasicViewportUI {
    public static ComponentUI createUI(JComponent c) {
        return FlatUIUtils.createSharedUI(FlatViewportUI.class, FlatViewportUI::new);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        Component view = ((JViewport)c).getView();
        if (view instanceof JComponent) {
            try {
                Method m = view.getClass().getMethod("getUI", new Class[0]);
                Object ui = m.invoke((Object)view, new Object[0]);
                if (ui instanceof ViewportPainter) {
                    ((ViewportPainter)ui).paintViewport(g, (JComponent)view, (JViewport)c);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public static interface ViewportPainter {
        public void paintViewport(Graphics var1, JComponent var2, JViewport var3);
    }
}

