/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.ui.FlatRootPaneUI$FlatRootLayout
 *  com.formdev.flatlaf.ui.FlatRootPaneUI$FlatWindowTitleBorder
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.ui.FlatNativeWindowBorder;
import com.formdev.flatlaf.ui.FlatRootPaneUI;
import com.formdev.flatlaf.ui.FlatTitlePane;
import com.formdev.flatlaf.ui.FlatWindowResizer;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.IllegalComponentStateException;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.RootPaneUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicRootPaneUI;

public class FlatRootPaneUI
extends BasicRootPaneUI {
    protected final Color borderColor = UIManager.getColor("TitlePane.borderColor");
    protected JRootPane rootPane;
    protected FlatTitlePane titlePane;
    protected FlatWindowResizer windowResizer;
    private Object nativeWindowBorderData;
    private LayoutManager oldLayout;
    private PropertyChangeListener ancestorListener;
    private ComponentListener componentListener;
    protected static final Integer TITLE_PANE_LAYER = JLayeredPane.FRAME_CONTENT_LAYER - 1;

    public static ComponentUI createUI(JComponent c) {
        return new FlatRootPaneUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.rootPane = (JRootPane)c;
        if (this.rootPane.getWindowDecorationStyle() != 0) {
            this.installClientDecorations();
        } else {
            this.installBorder();
        }
        this.installNativeWindowBorder();
    }

    protected void installBorder() {
        Border b;
        if (this.borderColor != null && ((b = this.rootPane.getBorder()) == null || b instanceof UIResource)) {
            this.rootPane.setBorder((Border)new FlatWindowTitleBorder(this.borderColor));
        }
    }

    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        this.uninstallNativeWindowBorder();
        this.uninstallClientDecorations();
        this.rootPane = null;
    }

    @Override
    protected void installDefaults(JRootPane c) {
        Color background;
        Container parent;
        super.installDefaults(c);
        if (!c.isBackgroundSet() || c.getBackground() instanceof UIResource) {
            c.setBackground(UIManager.getColor("RootPane.background"));
        }
        if (!c.isForegroundSet() || c.getForeground() instanceof UIResource) {
            c.setForeground(UIManager.getColor("RootPane.foreground"));
        }
        if (!c.isFontSet() || c.getFont() instanceof UIResource) {
            c.setFont(UIManager.getFont("RootPane.font"));
        }
        if (((parent = c.getParent()) instanceof JFrame || parent instanceof JDialog) && ((background = parent.getBackground()) == null || background instanceof UIResource)) {
            parent.setBackground(UIManager.getColor("control"));
        }
        if (SystemInfo.isJetBrainsJVM && SystemInfo.isMacOS_10_14_Mojave_orLater) {
            c.putClientProperty("jetbrains.awt.windowDarkAppearance", FlatLaf.isLafDark());
        }
    }

    @Override
    protected void uninstallDefaults(JRootPane c) {
        super.uninstallDefaults(c);
        if (c.isBackgroundSet() && c.getBackground() instanceof UIResource) {
            c.setBackground(null);
        }
        if (c.isForegroundSet() && c.getForeground() instanceof UIResource) {
            c.setForeground(null);
        }
        if (c.isFontSet() && c.getFont() instanceof UIResource) {
            c.setFont(null);
        }
    }

    @Override
    protected void installListeners(final JRootPane root) {
        super.installListeners(root);
        if (SystemInfo.isJava_9_orLater) {
            this.ancestorListener = e -> {
                Object oldValue = e.getOldValue();
                Object newValue = e.getNewValue();
                if (newValue instanceof Window) {
                    if (this.componentListener == null) {
                        this.componentListener = new ComponentAdapter(){

                            @Override
                            public void componentShown(ComponentEvent e) {
                                root.getParent().repaint(root.getX(), root.getY(), root.getWidth(), root.getHeight());
                            }
                        };
                    }
                    ((Window)newValue).addComponentListener(this.componentListener);
                } else if (newValue == null && oldValue instanceof Window && this.componentListener != null) {
                    ((Window)oldValue).removeComponentListener(this.componentListener);
                }
            };
            root.addPropertyChangeListener("ancestor", this.ancestorListener);
        }
    }

    @Override
    protected void uninstallListeners(JRootPane root) {
        super.uninstallListeners(root);
        if (SystemInfo.isJava_9_orLater) {
            if (this.componentListener != null) {
                Window window = SwingUtilities.windowForComponent(root);
                if (window != null) {
                    window.removeComponentListener(this.componentListener);
                }
                this.componentListener = null;
            }
            root.removePropertyChangeListener("ancestor", this.ancestorListener);
            this.ancestorListener = null;
        }
    }

    protected void installNativeWindowBorder() {
        this.nativeWindowBorderData = FlatNativeWindowBorder.install(this.rootPane);
    }

    protected void uninstallNativeWindowBorder() {
        FlatNativeWindowBorder.uninstall(this.rootPane, this.nativeWindowBorderData);
        this.nativeWindowBorderData = null;
    }

    public static void updateNativeWindowBorder(JRootPane rootPane) {
        RootPaneUI rui = rootPane.getUI();
        if (!(rui instanceof FlatRootPaneUI)) {
            return;
        }
        FlatRootPaneUI ui = (FlatRootPaneUI)rui;
        ui.uninstallNativeWindowBorder();
        ui.installNativeWindowBorder();
    }

    protected void installClientDecorations() {
        boolean isNativeWindowBorderSupported = FlatNativeWindowBorder.isSupported();
        if (this.rootPane.getWindowDecorationStyle() != 0 && !isNativeWindowBorderSupported) {
            LookAndFeel.installBorder(this.rootPane, "RootPane.border");
        } else {
            LookAndFeel.uninstallBorder(this.rootPane);
        }
        this.setTitlePane(this.createTitlePane());
        this.oldLayout = this.rootPane.getLayout();
        this.rootPane.setLayout((LayoutManager)this.createRootLayout());
        if (!isNativeWindowBorderSupported) {
            this.windowResizer = this.createWindowResizer();
        }
    }

    protected void uninstallClientDecorations() {
        LookAndFeel.uninstallBorder(this.rootPane);
        this.setTitlePane(null);
        if (this.windowResizer != null) {
            this.windowResizer.uninstall();
            this.windowResizer = null;
        }
        if (this.oldLayout != null) {
            this.rootPane.setLayout(this.oldLayout);
            this.oldLayout = null;
        }
        if (this.rootPane.getWindowDecorationStyle() == 0) {
            this.rootPane.revalidate();
            this.rootPane.repaint();
        }
    }

    protected FlatRootLayout createRootLayout() {
        return new FlatRootLayout(this);
    }

    protected FlatWindowResizer createWindowResizer() {
        return new FlatWindowResizer.WindowResizer(this.rootPane);
    }

    protected FlatTitlePane createTitlePane() {
        return new FlatTitlePane(this.rootPane);
    }

    protected void setTitlePane(FlatTitlePane newTitlePane) {
        JLayeredPane layeredPane = this.rootPane.getLayeredPane();
        if (this.titlePane != null) {
            layeredPane.remove(this.titlePane);
        }
        if (newTitlePane != null) {
            layeredPane.add((Component)newTitlePane, TITLE_PANE_LAYER);
        }
        this.titlePane = newTitlePane;
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);
        switch (e.getPropertyName()) {
            case "windowDecorationStyle": {
                this.uninstallClientDecorations();
                if (this.rootPane.getWindowDecorationStyle() != 0) {
                    this.installClientDecorations();
                    break;
                }
                this.installBorder();
                break;
            }
            case "JRootPane.useWindowDecorations": {
                FlatRootPaneUI.updateNativeWindowBorder(this.rootPane);
                break;
            }
            case "JRootPane.menuBarEmbedded": {
                if (this.titlePane == null) break;
                this.titlePane.menuBarChanged();
                this.rootPane.revalidate();
                this.rootPane.repaint();
                break;
            }
            case "JRootPane.titleBarShowIcon": {
                if (this.titlePane == null) break;
                this.titlePane.updateIcon();
                break;
            }
            case "JRootPane.titleBarShowTitle": 
            case "JRootPane.titleBarShowIconify": 
            case "JRootPane.titleBarShowMaximize": 
            case "JRootPane.titleBarShowClose": {
                if (this.titlePane == null) break;
                this.titlePane.updateVisibility();
                break;
            }
            case "JRootPane.titleBarBackground": 
            case "JRootPane.titleBarForeground": {
                if (this.titlePane == null) break;
                this.titlePane.titleBarColorsChanged();
                break;
            }
            case "JRootPane.glassPaneFullHeight": {
                this.rootPane.revalidate();
                break;
            }
            case "Window.style": {
                if (!this.rootPane.isDisplayable()) break;
                throw new IllegalComponentStateException("The client property 'Window.style' must be set before the window becomes displayable.");
            }
        }
    }

    protected static boolean isMenuBarEmbedded(JRootPane rootPane) {
        RootPaneUI ui = rootPane.getUI();
        return ui instanceof FlatRootPaneUI && ((FlatRootPaneUI)ui).titlePane != null && ((FlatRootPaneUI)ui).titlePane.isMenuBarEmbedded();
    }

    protected static FlatTitlePane getTitlePane(JRootPane rootPane) {
        RootPaneUI ui = rootPane.getUI();
        return ui instanceof FlatRootPaneUI ? ((FlatRootPaneUI)ui).titlePane : null;
    }
}

