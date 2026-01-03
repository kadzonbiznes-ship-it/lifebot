/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.event.WindowEvent;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
import javax.swing.RepaintManager;
import javax.swing.RootPaneContainer;
import javax.swing.SwingContainer;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import sun.awt.SunToolkit;

@JavaBean(defaultProperty="JMenuBar", description="A toplevel window which can be minimized to an icon.")
@SwingContainer(delegate="getContentPane")
public class JFrame
extends Frame
implements WindowConstants,
Accessible,
RootPaneContainer,
TransferHandler.HasGetTransferHandler {
    private static final Object defaultLookAndFeelDecoratedKey = new StringBuffer("JFrame.defaultLookAndFeelDecorated");
    private int defaultCloseOperation = 1;
    private TransferHandler transferHandler;
    protected JRootPane rootPane;
    protected boolean rootPaneCheckingEnabled = false;
    protected AccessibleContext accessibleContext = null;

    public JFrame() throws HeadlessException {
        this.frameInit();
    }

    public JFrame(GraphicsConfiguration gc) {
        super(gc);
        this.frameInit();
    }

    public JFrame(String title) throws HeadlessException {
        super(title);
        this.frameInit();
    }

    public JFrame(String title, GraphicsConfiguration gc) {
        super(title, gc);
        this.frameInit();
    }

    protected void frameInit() {
        boolean supportsWindowDecorations;
        this.enableEvents(72L);
        this.setLocale(JComponent.getDefaultLocale());
        this.setRootPane(this.createRootPane());
        this.setBackground(UIManager.getColor("control"));
        this.setRootPaneCheckingEnabled(true);
        if (JFrame.isDefaultLookAndFeelDecorated() && (supportsWindowDecorations = UIManager.getLookAndFeel().getSupportsWindowDecorations())) {
            this.setUndecorated(true);
            this.getRootPane().setWindowDecorationStyle(1);
        }
        SunToolkit.checkAndSetPolicy(this);
    }

    protected JRootPane createRootPane() {
        JRootPane rp = new JRootPane();
        rp.setOpaque(true);
        return rp;
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == 201) {
            switch (this.defaultCloseOperation) {
                case 1: {
                    this.setVisible(false);
                    break;
                }
                case 2: {
                    this.dispose();
                    break;
                }
                case 3: {
                    System.exit(0);
                    break;
                }
            }
        }
    }

    @BeanProperty(preferred=true, enumerationValues={"WindowConstants.DO_NOTHING_ON_CLOSE", "WindowConstants.HIDE_ON_CLOSE", "WindowConstants.DISPOSE_ON_CLOSE", "WindowConstants.EXIT_ON_CLOSE"}, description="The frame's default close operation.")
    public void setDefaultCloseOperation(int operation) {
        SecurityManager security;
        if (operation != 0 && operation != 1 && operation != 2 && operation != 3) {
            throw new IllegalArgumentException("defaultCloseOperation must be one of: DO_NOTHING_ON_CLOSE, HIDE_ON_CLOSE, DISPOSE_ON_CLOSE, or EXIT_ON_CLOSE");
        }
        if (operation == 3 && (security = System.getSecurityManager()) != null) {
            security.checkExit(0);
        }
        if (this.defaultCloseOperation != operation) {
            int oldValue = this.defaultCloseOperation;
            this.defaultCloseOperation = operation;
            this.firePropertyChange("defaultCloseOperation", oldValue, operation);
        }
    }

    public int getDefaultCloseOperation() {
        return this.defaultCloseOperation;
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

    @BeanProperty(bound=false, hidden=true, description="The menubar for accessing pulldown menus from this frame.")
    public void setJMenuBar(JMenuBar menubar) {
        this.getRootPane().setJMenuBar(menubar);
    }

    public JMenuBar getJMenuBar() {
        return this.getRootPane().getJMenuBar();
    }

    protected boolean isRootPaneCheckingEnabled() {
        return this.rootPaneCheckingEnabled;
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
    @BeanProperty(bound=false, hidden=true, description="the RootPane object for this frame.")
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
    public void setIconImage(Image image) {
        super.setIconImage(image);
    }

    @Override
    public Container getContentPane() {
        return this.getRootPane().getContentPane();
    }

    @Override
    @BeanProperty(bound=false, hidden=true, description="The client area of the frame where child components are normally inserted.")
    public void setContentPane(Container contentPane) {
        this.getRootPane().setContentPane(contentPane);
    }

    @Override
    public JLayeredPane getLayeredPane() {
        return this.getRootPane().getLayeredPane();
    }

    @Override
    @BeanProperty(bound=false, hidden=true, description="The pane that holds the various frame layers.")
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

    public static void setDefaultLookAndFeelDecorated(boolean defaultLookAndFeelDecorated) {
        if (defaultLookAndFeelDecorated) {
            SwingUtilities.appContextPut(defaultLookAndFeelDecoratedKey, Boolean.TRUE);
        } else {
            SwingUtilities.appContextPut(defaultLookAndFeelDecoratedKey, Boolean.FALSE);
        }
    }

    public static boolean isDefaultLookAndFeelDecorated() {
        Boolean defaultLookAndFeelDecorated = (Boolean)SwingUtilities.appContextGet(defaultLookAndFeelDecoratedKey);
        if (defaultLookAndFeelDecorated == null) {
            defaultLookAndFeelDecorated = Boolean.FALSE;
        }
        return defaultLookAndFeelDecorated;
    }

    @Override
    protected String paramString() {
        String defaultCloseOperationString = this.defaultCloseOperation == 1 ? "HIDE_ON_CLOSE" : (this.defaultCloseOperation == 2 ? "DISPOSE_ON_CLOSE" : (this.defaultCloseOperation == 0 ? "DO_NOTHING_ON_CLOSE" : (this.defaultCloseOperation == 3 ? "EXIT_ON_CLOSE" : "")));
        String rootPaneString = this.rootPane != null ? this.rootPane.toString() : "";
        String rootPaneCheckingEnabledString = this.rootPaneCheckingEnabled ? "true" : "false";
        return super.paramString() + ",defaultCloseOperation=" + defaultCloseOperationString + ",rootPane=" + rootPaneString + ",rootPaneCheckingEnabled=" + rootPaneCheckingEnabledString;
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJFrame();
        }
        return this.accessibleContext;
    }

    protected class AccessibleJFrame
    extends Frame.AccessibleAWTFrame {
        protected AccessibleJFrame() {
        }

        @Override
        public String getAccessibleName() {
            if (this.accessibleName != null) {
                return this.accessibleName;
            }
            if (JFrame.this.getTitle() == null) {
                return super.getAccessibleName();
            }
            return JFrame.this.getTitle();
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (JFrame.this.isResizable()) {
                states.add(AccessibleState.RESIZABLE);
            }
            if (JFrame.this.getFocusOwner() != null) {
                states.add(AccessibleState.ACTIVE);
            }
            return states;
        }
    }
}

