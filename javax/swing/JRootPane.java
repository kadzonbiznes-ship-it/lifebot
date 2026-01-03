/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.IllegalComponentStateException;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.beans.BeanProperty;
import java.io.Serializable;
import java.security.AccessController;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.RepaintManager;
import javax.swing.UIManager;
import javax.swing.plaf.RootPaneUI;
import sun.security.action.GetBooleanAction;

public class JRootPane
extends JComponent
implements Accessible {
    private static final String uiClassID = "RootPaneUI";
    private static final boolean LOG_DISABLE_TRUE_DOUBLE_BUFFERING = AccessController.doPrivileged(new GetBooleanAction("swing.logDoubleBufferingDisable"));
    private static final boolean IGNORE_DISABLE_TRUE_DOUBLE_BUFFERING = AccessController.doPrivileged(new GetBooleanAction("swing.ignoreDoubleBufferingDisable"));
    public static final int NONE = 0;
    public static final int FRAME = 1;
    public static final int PLAIN_DIALOG = 2;
    public static final int INFORMATION_DIALOG = 3;
    public static final int ERROR_DIALOG = 4;
    public static final int COLOR_CHOOSER_DIALOG = 5;
    public static final int FILE_CHOOSER_DIALOG = 6;
    public static final int QUESTION_DIALOG = 7;
    public static final int WARNING_DIALOG = 8;
    private int windowDecorationStyle;
    protected JMenuBar menuBar;
    protected Container contentPane;
    protected JLayeredPane layeredPane;
    protected Component glassPane;
    protected JButton defaultButton;
    boolean useTrueDoubleBuffering = true;

    public JRootPane() {
        this.setGlassPane(this.createGlassPane());
        this.setLayeredPane(this.createLayeredPane());
        this.setContentPane(this.createContentPane());
        this.setLayout(this.createRootLayout());
        this.setDoubleBuffered(true);
        this.updateUI();
    }

    @Override
    public void setDoubleBuffered(boolean aFlag) {
        if (this.isDoubleBuffered() != aFlag) {
            super.setDoubleBuffered(aFlag);
            RepaintManager.currentManager(this).doubleBufferingChanged(this);
        }
    }

    public int getWindowDecorationStyle() {
        return this.windowDecorationStyle;
    }

    @BeanProperty(expert=true, visualUpdate=true, enumerationValues={"JRootPane.NONE", "JRootPane.FRAME", "JRootPane.PLAIN_DIALOG", "JRootPane.INFORMATION_DIALOG", "JRootPane.ERROR_DIALOG", "JRootPane.COLOR_CHOOSER_DIALOG", "JRootPane.FILE_CHOOSER_DIALOG", "JRootPane.QUESTION_DIALOG", "JRootPane.WARNING_DIALOG"}, description="Identifies the type of Window decorations to provide")
    public void setWindowDecorationStyle(int windowDecorationStyle) {
        if (windowDecorationStyle < 0 || windowDecorationStyle > 8) {
            throw new IllegalArgumentException("Invalid decoration style");
        }
        int oldWindowDecorationStyle = this.getWindowDecorationStyle();
        this.windowDecorationStyle = windowDecorationStyle;
        this.firePropertyChange("windowDecorationStyle", oldWindowDecorationStyle, windowDecorationStyle);
    }

    @Override
    public RootPaneUI getUI() {
        return (RootPaneUI)this.ui;
    }

    @BeanProperty(expert=true, hidden=true, visualUpdate=true, description="The UI object that implements the Component's LookAndFeel.")
    public void setUI(RootPaneUI ui) {
        super.setUI(ui);
    }

    @Override
    public void updateUI() {
        this.setUI((RootPaneUI)UIManager.getUI(this));
    }

    @Override
    public String getUIClassID() {
        return uiClassID;
    }

    protected JLayeredPane createLayeredPane() {
        JLayeredPane p = new JLayeredPane();
        p.setName(this.getName() + ".layeredPane");
        return p;
    }

    protected Container createContentPane() {
        JPanel c = new JPanel();
        c.setName(this.getName() + ".contentPane");
        c.setLayout(new BorderLayout(){

            @Override
            public void addLayoutComponent(Component comp, Object constraints) {
                if (constraints == null) {
                    constraints = "Center";
                }
                super.addLayoutComponent(comp, constraints);
            }
        });
        return c;
    }

    protected Component createGlassPane() {
        JPanel c = new JPanel();
        c.setName(this.getName() + ".glassPane");
        c.setVisible(false);
        c.setOpaque(false);
        return c;
    }

    protected LayoutManager createRootLayout() {
        return new RootLayout();
    }

    public void setJMenuBar(JMenuBar menu) {
        if (this.menuBar != null && this.menuBar.getParent() == this.layeredPane) {
            this.layeredPane.remove(this.menuBar);
        }
        this.menuBar = menu;
        if (this.menuBar != null) {
            this.menuBar.updateUI();
            this.layeredPane.add((Component)this.menuBar, JLayeredPane.FRAME_CONTENT_LAYER);
        }
    }

    @Deprecated
    public void setMenuBar(JMenuBar menu) {
        if (this.menuBar != null && this.menuBar.getParent() == this.layeredPane) {
            this.layeredPane.remove(this.menuBar);
        }
        this.menuBar = menu;
        if (this.menuBar != null) {
            this.layeredPane.add((Component)this.menuBar, JLayeredPane.FRAME_CONTENT_LAYER);
        }
    }

    public JMenuBar getJMenuBar() {
        return this.menuBar;
    }

    @Deprecated
    public JMenuBar getMenuBar() {
        return this.menuBar;
    }

    public void setContentPane(Container content) {
        if (content == null) {
            throw new IllegalComponentStateException("contentPane cannot be set to null.");
        }
        if (this.contentPane != null && this.contentPane.getParent() == this.layeredPane) {
            this.layeredPane.remove(this.contentPane);
        }
        this.contentPane = content;
        this.layeredPane.add((Component)this.contentPane, JLayeredPane.FRAME_CONTENT_LAYER);
    }

    public Container getContentPane() {
        return this.contentPane;
    }

    public void setLayeredPane(JLayeredPane layered) {
        if (layered == null) {
            throw new IllegalComponentStateException("layeredPane cannot be set to null.");
        }
        if (this.layeredPane != null && this.layeredPane.getParent() == this) {
            this.remove(this.layeredPane);
        }
        this.layeredPane = layered;
        this.add((Component)this.layeredPane, -1);
    }

    public JLayeredPane getLayeredPane() {
        return this.layeredPane;
    }

    public void setGlassPane(Component glass) {
        if (glass == null) {
            throw new NullPointerException("glassPane cannot be set to null.");
        }
        glass.setMixingCutoutShape(new Rectangle());
        boolean visible = false;
        if (this.glassPane != null && this.glassPane.getParent() == this) {
            this.remove(this.glassPane);
            visible = this.glassPane.isVisible();
        }
        glass.setVisible(visible);
        this.glassPane = glass;
        this.add(this.glassPane, 0);
        if (visible) {
            this.repaint();
        }
    }

    public Component getGlassPane() {
        return this.glassPane;
    }

    @Override
    public boolean isValidateRoot() {
        return true;
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return !this.glassPane.isVisible();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        this.enableEvents(8L);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
    }

    @BeanProperty(description="The button activated by default in this root pane")
    public void setDefaultButton(JButton defaultButton) {
        JButton oldDefault = this.defaultButton;
        if (oldDefault != defaultButton) {
            this.defaultButton = defaultButton;
            if (oldDefault != null) {
                oldDefault.repaint();
            }
            if (defaultButton != null) {
                defaultButton.repaint();
            }
        }
        this.firePropertyChange("defaultButton", oldDefault, defaultButton);
    }

    public JButton getDefaultButton() {
        return this.defaultButton;
    }

    final void setUseTrueDoubleBuffering(boolean useTrueDoubleBuffering) {
        this.useTrueDoubleBuffering = useTrueDoubleBuffering;
    }

    final boolean getUseTrueDoubleBuffering() {
        return this.useTrueDoubleBuffering;
    }

    final void disableTrueDoubleBuffering() {
        if (this.useTrueDoubleBuffering && !IGNORE_DISABLE_TRUE_DOUBLE_BUFFERING) {
            if (LOG_DISABLE_TRUE_DOUBLE_BUFFERING) {
                System.out.println("Disabling true double buffering for " + String.valueOf(this));
                Thread.dumpStack();
            }
            this.useTrueDoubleBuffering = false;
            RepaintManager.currentManager(this).doubleBufferingChanged(this);
        }
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        super.addImpl(comp, constraints, index);
        if (this.glassPane != null && this.glassPane.getParent() == this && this.getComponent(0) != this.glassPane) {
            this.add(this.glassPane, 0);
        }
    }

    @Override
    protected String paramString() {
        return super.paramString();
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJRootPane();
        }
        return this.accessibleContext;
    }

    protected class RootLayout
    implements LayoutManager2,
    Serializable {
        protected RootLayout() {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Insets i = JRootPane.this.getInsets();
            Dimension rd = JRootPane.this.contentPane != null ? JRootPane.this.contentPane.getPreferredSize() : parent.getSize();
            Dimension mbd = JRootPane.this.menuBar != null && JRootPane.this.menuBar.isVisible() ? JRootPane.this.menuBar.getPreferredSize() : new Dimension(0, 0);
            return new Dimension(Math.max(rd.width, mbd.width) + i.left + i.right, rd.height + mbd.height + i.top + i.bottom);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            Insets i = JRootPane.this.getInsets();
            Dimension rd = JRootPane.this.contentPane != null ? JRootPane.this.contentPane.getMinimumSize() : parent.getSize();
            Dimension mbd = JRootPane.this.menuBar != null && JRootPane.this.menuBar.isVisible() ? JRootPane.this.menuBar.getMinimumSize() : new Dimension(0, 0);
            return new Dimension(Math.max(rd.width, mbd.width) + i.left + i.right, rd.height + mbd.height + i.top + i.bottom);
        }

        @Override
        public Dimension maximumLayoutSize(Container target) {
            Insets i = JRootPane.this.getInsets();
            Dimension mbd = JRootPane.this.menuBar != null && JRootPane.this.menuBar.isVisible() ? JRootPane.this.menuBar.getMaximumSize() : new Dimension(0, 0);
            Dimension rd = JRootPane.this.contentPane != null ? JRootPane.this.contentPane.getMaximumSize() : new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE - i.top - i.bottom - mbd.height - 1);
            return new Dimension(Math.max(rd.width, mbd.width) + i.left + i.right, rd.height + mbd.height + i.top + i.bottom);
        }

        @Override
        public void layoutContainer(Container parent) {
            Rectangle b = parent.getBounds();
            Insets i = JRootPane.this.getInsets();
            int contentY = 0;
            int w = b.width - i.right - i.left;
            int h = b.height - i.top - i.bottom;
            if (JRootPane.this.layeredPane != null) {
                JRootPane.this.layeredPane.setBounds(i.left, i.top, w, h);
            }
            if (JRootPane.this.glassPane != null) {
                JRootPane.this.glassPane.setBounds(i.left, i.top, w, h);
            }
            if (JRootPane.this.menuBar != null && JRootPane.this.menuBar.isVisible()) {
                Dimension mbd = JRootPane.this.menuBar.getPreferredSize();
                JRootPane.this.menuBar.setBounds(0, 0, w, mbd.height);
                contentY += mbd.height;
            }
            if (JRootPane.this.contentPane != null) {
                JRootPane.this.contentPane.setBounds(0, contentY, w, h - contentY);
            }
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public void addLayoutComponent(Component comp, Object constraints) {
        }

        @Override
        public float getLayoutAlignmentX(Container target) {
            return 0.0f;
        }

        @Override
        public float getLayoutAlignmentY(Container target) {
            return 0.0f;
        }

        @Override
        public void invalidateLayout(Container target) {
        }
    }

    protected class AccessibleJRootPane
    extends JComponent.AccessibleJComponent {
        protected AccessibleJRootPane() {
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.ROOT_PANE;
        }

        @Override
        public int getAccessibleChildrenCount() {
            return super.getAccessibleChildrenCount();
        }

        @Override
        public Accessible getAccessibleChild(int i) {
            return super.getAccessibleChild(i);
        }
    }
}

