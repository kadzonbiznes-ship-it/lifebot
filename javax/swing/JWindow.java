/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.WindowListener;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.RepaintManager;
import javax.swing.RootPaneContainer;
import javax.swing.SwingContainer;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import sun.awt.SunToolkit;

@JavaBean(defaultProperty="accessibleContext", description="A toplevel window which has no system border or controls.")
@SwingContainer(delegate="getContentPane")
public class JWindow
extends Window
implements Accessible,
RootPaneContainer,
TransferHandler.HasGetTransferHandler {
    protected JRootPane rootPane;
    protected boolean rootPaneCheckingEnabled = false;
    private TransferHandler transferHandler;
    protected AccessibleContext accessibleContext = null;

    public JWindow() {
        this((Frame)null);
    }

    public JWindow(GraphicsConfiguration gc) {
        this(null, gc);
        super.setFocusableWindowState(false);
    }

    public JWindow(Frame owner) {
        super(owner == null ? SwingUtilities.getSharedOwnerFrame() : owner);
        if (owner == null) {
            WindowListener ownerShutdownListener = SwingUtilities.getSharedOwnerFrameShutdownListener();
            this.addWindowListener(ownerShutdownListener);
        }
        this.windowInit();
    }

    public JWindow(Window owner) {
        super(owner == null ? SwingUtilities.getSharedOwnerFrame() : owner);
        if (owner == null) {
            WindowListener ownerShutdownListener = SwingUtilities.getSharedOwnerFrameShutdownListener();
            this.addWindowListener(ownerShutdownListener);
        }
        this.windowInit();
    }

    public JWindow(Window owner, GraphicsConfiguration gc) {
        super(owner == null ? SwingUtilities.getSharedOwnerFrame() : owner, gc);
        if (owner == null) {
            WindowListener ownerShutdownListener = SwingUtilities.getSharedOwnerFrameShutdownListener();
            this.addWindowListener(ownerShutdownListener);
        }
        this.windowInit();
    }

    protected void windowInit() {
        this.setLocale(JComponent.getDefaultLocale());
        this.setRootPane(this.createRootPane());
        this.setRootPaneCheckingEnabled(true);
        SunToolkit.checkAndSetPolicy(this);
    }

    protected JRootPane createRootPane() {
        JRootPane rp = new JRootPane();
        rp.setOpaque(true);
        return rp;
    }

    protected boolean isRootPaneCheckingEnabled() {
        return this.rootPaneCheckingEnabled;
    }

    @BeanProperty(hidden=true, description="Mechanism for transfer of data into the component")
    public void setTransferHandler(TransferHandler newHandler) {
        TransferHandler oldHandler = this.transferHandler;
        this.transferHandler = newHandler;
        SwingUtilities.installSwingDropTargetAsNecessary(this, this.transferHandler);
        this.firePropertyChange("transferHandler", oldHandler, newHandler);
    }

    @Override
    public TransferHandler getTransferHandler() {
        return this.transferHandler;
    }

    @Override
    public void update(Graphics g) {
        this.paint(g);
    }

    @BeanProperty(hidden=true, description="Whether the add and setLayout methods are forwarded")
    protected void setRootPaneCheckingEnabled(boolean enabled) {
        this.rootPaneCheckingEnabled = enabled;
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        if (this.isRootPaneCheckingEnabled()) {
            this.getContentPane().add(comp, constraints, index);
        } else {
            super.addImpl(comp, constraints, index);
        }
    }

    @Override
    public void remove(Component comp) {
        if (comp == this.rootPane) {
            super.remove(comp);
        } else {
            this.getContentPane().remove(comp);
        }
    }

    @Override
    public void setLayout(LayoutManager manager) {
        if (this.isRootPaneCheckingEnabled()) {
            this.getContentPane().setLayout(manager);
        } else {
            super.setLayout(manager);
        }
    }

    @Override
    @BeanProperty(bound=false, hidden=true, description="the RootPane object for this window.")
    public JRootPane getRootPane() {
        return this.rootPane;
    }

    protected void setRootPane(JRootPane root) {
        if (this.rootPane != null) {
            this.remove(this.rootPane);
        }
        this.rootPane = root;
        if (this.rootPane != null) {
            boolean checkingEnabled = this.isRootPaneCheckingEnabled();
            try {
                this.setRootPaneCheckingEnabled(false);
                this.add((Component)this.rootPane, "Center");
            }
            finally {
                this.setRootPaneCheckingEnabled(checkingEnabled);
            }
        }
    }

    @Override
    public Container getContentPane() {
        return this.getRootPane().getContentPane();
    }

    @Override
    @BeanProperty(bound=false, hidden=true, description="The client area of the window where child components are normally inserted.")
    public void setContentPane(Container contentPane) {
        this.getRootPane().setContentPane(contentPane);
    }

    @Override
    public JLayeredPane getLayeredPane() {
        return this.getRootPane().getLayeredPane();
    }

    @Override
    @BeanProperty(bound=false, hidden=true, description="The pane which holds the various window layers.")
    public void setLayeredPane(JLayeredPane layeredPane) {
        this.getRootPane().setLayeredPane(layeredPane);
    }

    @Override
    public Component getGlassPane() {
        return this.getRootPane().getGlassPane();
    }

    @Override
    @BeanProperty(bound=false, hidden=true, description="A transparent pane used for menu rendering.")
    public void setGlassPane(Component glassPane) {
        this.getRootPane().setGlassPane(glassPane);
    }

    @Override
    @BeanProperty(bound=false)
    public Graphics getGraphics() {
        JComponent.getGraphicsInvoked(this);
        return super.getGraphics();
    }

    @Override
    public void repaint(long time, int x, int y, int width, int height) {
        if (RepaintManager.HANDLE_TOP_LEVEL_PAINT) {
            RepaintManager.currentManager(this).addDirtyRegion(this, x, y, width, height);
        } else {
            super.repaint(time, x, y, width, height);
        }
    }

    @Override
    protected String paramString() {
        String rootPaneCheckingEnabledString = this.rootPaneCheckingEnabled ? "true" : "false";
        return super.paramString() + ",rootPaneCheckingEnabled=" + rootPaneCheckingEnabledString;
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJWindow();
        }
        return this.accessibleContext;
    }

    protected class AccessibleJWindow
    extends Window.AccessibleAWTWindow {
        protected AccessibleJWindow() {
        }
    }
}

