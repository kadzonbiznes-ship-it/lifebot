/*
 * Decompiled with CFR 0.152.
 */
package com.sun.java.swing;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Window;
import java.awt.geom.AffineTransform;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.RepaintManager;
import sun.awt.AppContext;
import sun.awt.SunToolkit;
import sun.java2d.pipe.Region;
import sun.swing.MenuItemLayoutHelper;
import sun.swing.SwingUtilities2;

public class SwingUtilities3 {
    private static final Object DELEGATE_REPAINT_MANAGER_KEY = new StringBuilder("DelegateRepaintManagerKey");
    private static Color disabledForeground;
    private static Color acceleratorSelectionForeground;
    private static Color acceleratorForeground;
    private static final Map<Container, Boolean> vsyncedMap;

    public static void setDelegateRepaintManager(JComponent component, RepaintManager repaintManager) {
        AppContext.getAppContext().put(DELEGATE_REPAINT_MANAGER_KEY, Boolean.TRUE);
        component.putClientProperty(DELEGATE_REPAINT_MANAGER_KEY, repaintManager);
    }

    public static void setVsyncRequested(Container rootContainer, boolean isRequested) {
        assert (rootContainer instanceof Applet || rootContainer instanceof Window);
        if (isRequested) {
            vsyncedMap.put(rootContainer, Boolean.TRUE);
        } else {
            vsyncedMap.remove(rootContainer);
        }
    }

    public static boolean isVsyncRequested(Container rootContainer) {
        assert (rootContainer instanceof Applet || rootContainer instanceof Window);
        return Boolean.TRUE == vsyncedMap.get(rootContainer);
    }

    public static RepaintManager getDelegateRepaintManager(Component component) {
        RepaintManager delegate = null;
        if (Boolean.TRUE == SunToolkit.targetToAppContext(component).get(DELEGATE_REPAINT_MANAGER_KEY)) {
            while (delegate == null && component != null) {
                while (component != null && !(component instanceof JComponent)) {
                    component = component.getParent();
                }
                if (component == null) continue;
                delegate = (RepaintManager)((JComponent)component).getClientProperty(DELEGATE_REPAINT_MANAGER_KEY);
                component = component.getParent();
            }
        }
        return delegate;
    }

    public static void applyInsets(Rectangle rect, Insets insets) {
        if (insets != null) {
            rect.x += insets.left;
            rect.y += insets.top;
            rect.width -= insets.right + rect.x;
            rect.height -= insets.bottom + rect.y;
        }
    }

    public static void paintCheckIcon(Graphics g, MenuItemLayoutHelper lh, MenuItemLayoutHelper.LayoutResult lr, Color holdc, Color foreground) {
        if (lh.getCheckIcon() != null) {
            ButtonModel model = lh.getMenuItem().getModel();
            if (model.isArmed() || lh.getMenuItem() instanceof JMenu && model.isSelected()) {
                g.setColor(foreground);
            } else {
                g.setColor(holdc);
            }
            if (lh.useCheckAndArrow()) {
                lh.getCheckIcon().paintIcon(lh.getMenuItem(), g, lr.getCheckRect().x, lr.getCheckRect().y);
            }
            g.setColor(holdc);
        }
    }

    public static void paintIcon(Graphics g, MenuItemLayoutHelper lh, MenuItemLayoutHelper.LayoutResult lr, Color holdc) {
        if (lh.getIcon() != null) {
            Icon icon;
            ButtonModel model = lh.getMenuItem().getModel();
            if (!model.isEnabled()) {
                icon = lh.getMenuItem().getDisabledIcon();
            } else if (model.isPressed() && model.isArmed()) {
                icon = lh.getMenuItem().getPressedIcon();
                if (icon == null) {
                    icon = lh.getMenuItem().getIcon();
                }
            } else {
                icon = lh.getMenuItem().getIcon();
            }
            if (icon != null) {
                icon.paintIcon(lh.getMenuItem(), g, lr.getIconRect().x, lr.getIconRect().y);
                g.setColor(holdc);
            }
        }
    }

    public static void paintAccText(Graphics g, MenuItemLayoutHelper lh, MenuItemLayoutHelper.LayoutResult lr) {
        if (!lh.getAccText().isEmpty()) {
            ButtonModel model = lh.getMenuItem().getModel();
            g.setFont(lh.getAccFontMetrics().getFont());
            if (!model.isEnabled()) {
                if (disabledForeground != null) {
                    g.setColor(disabledForeground);
                    SwingUtilities2.drawString((JComponent)lh.getMenuItem(), g, lh.getAccText(), lr.getAccRect().x, lr.getAccRect().y + lh.getAccFontMetrics().getAscent());
                } else {
                    g.setColor(lh.getMenuItem().getBackground().brighter());
                    SwingUtilities2.drawString((JComponent)lh.getMenuItem(), g, lh.getAccText(), lr.getAccRect().x, lr.getAccRect().y + lh.getAccFontMetrics().getAscent());
                    g.setColor(lh.getMenuItem().getBackground().darker());
                    SwingUtilities2.drawString((JComponent)lh.getMenuItem(), g, lh.getAccText(), lr.getAccRect().x - 1, lr.getAccRect().y + lh.getFontMetrics().getAscent() - 1);
                }
            } else {
                if (model.isArmed() || lh.getMenuItem() instanceof JMenu && model.isSelected()) {
                    g.setColor(acceleratorSelectionForeground);
                } else {
                    g.setColor(acceleratorForeground);
                }
                SwingUtilities2.drawString((JComponent)lh.getMenuItem(), g, lh.getAccText(), lr.getAccRect().x, lr.getAccRect().y + lh.getAccFontMetrics().getAscent());
            }
        }
    }

    public static void setDisabledForeground(Color disabledFg) {
        disabledForeground = disabledFg;
    }

    public static void setAcceleratorSelectionForeground(Color acceleratorSelectionFg) {
        acceleratorSelectionForeground = acceleratorSelectionFg;
    }

    public static void setAcceleratorForeground(Color acceleratorFg) {
        acceleratorForeground = acceleratorFg;
    }

    public static void paintArrowIcon(Graphics g, MenuItemLayoutHelper lh, MenuItemLayoutHelper.LayoutResult lr, Color foreground) {
        if (lh.getArrowIcon() != null) {
            ButtonModel model = lh.getMenuItem().getModel();
            if (model.isArmed() || lh.getMenuItem() instanceof JMenu && model.isSelected()) {
                g.setColor(foreground);
            }
            if (lh.useCheckAndArrow()) {
                lh.getArrowIcon().paintIcon(lh.getMenuItem(), g, lr.getArrowRect().x, lr.getArrowRect().y);
            }
        }
    }

    public static void paintBorder(Component c, Graphics g, int x, int y, int w, int h, UnscaledBorderPainter painter) {
        Graphics2D g2d;
        AffineTransform at = null;
        Stroke oldStroke = null;
        boolean resetTransform = false;
        double scaleFactor = 1.0;
        int xtranslation = x;
        int ytranslation = y;
        int width = w;
        int height = h;
        if (g instanceof Graphics2D) {
            g2d = (Graphics2D)g;
            at = g2d.getTransform();
            oldStroke = g2d.getStroke();
            scaleFactor = Math.min(at.getScaleX(), at.getScaleY());
            boolean bl = resetTransform = at.getShearX() == 0.0 && at.getShearY() == 0.0 && (at.getScaleX() > 1.0 || at.getScaleY() > 1.0);
            if (resetTransform) {
                g2d.setTransform(new AffineTransform());
                double xx = at.getScaleX() * (double)x + at.getTranslateX();
                double yy = at.getScaleY() * (double)y + at.getTranslateY();
                xtranslation = Region.clipRound(xx);
                ytranslation = Region.clipRound(yy);
                width = Region.clipRound(at.getScaleX() * (double)w + xx) - xtranslation;
                height = Region.clipRound(at.getScaleY() * (double)h + yy) - ytranslation;
            }
        }
        g.translate(xtranslation, ytranslation);
        painter.paintUnscaledBorder(c, g, width, height, scaleFactor);
        g.translate(-xtranslation, -ytranslation);
        if (g instanceof Graphics2D) {
            g2d = (Graphics2D)g;
            g2d.setStroke(oldStroke);
            if (resetTransform) {
                g2d.setTransform(at);
            }
        }
    }

    static {
        vsyncedMap = Collections.synchronizedMap(new WeakHashMap());
    }

    @FunctionalInterface
    public static interface UnscaledBorderPainter {
        public void paintUnscaledBorder(Component var1, Graphics var2, int var3, int var4, double var5);
    }
}

