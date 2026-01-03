/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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

@JavaBean(defaultProperty="JMenuBar", description="A toplevel window for creating dialog boxes.")
@SwingContainer(delegate="getContentPane")
public class JDialog
extends Dialog
implements WindowConstants,
Accessible,
RootPaneContainer,
TransferHandler.HasGetTransferHandler {
    private static final Object defaultLookAndFeelDecoratedKey = new StringBuffer("JDialog.defaultLookAndFeelDecorated");
    private int defaultCloseOperation = 1;
    protected JRootPane rootPane;
    protected boolean rootPaneCheckingEnabled = false;
    private TransferHandler transferHandler;
    protected AccessibleContext accessibleContext = null;

    public JDialog() {
        this((Frame)null, false);
    }

    public JDialog(Frame owner) {
        this(owner, false);
    }

    public JDialog(Frame owner, boolean modal) {
        this(owner, "", modal);
    }

    public JDialog(Frame owner, String title) {
        this(owner, title, false);
    }

    public JDialog(Frame owner, String title, boolean modal) {
        super(owner == null ? SwingUtilities.getSharedOwnerFrame() : owner, title, modal);
        if (owner == null) {
            WindowListener ownerShutdownListener = SwingUtilities.getSharedOwnerFrameShutdownListener();
            this.addWindowListener(ownerShutdownListener);
        }
        this.dialogInit();
    }

    public JDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
        super(owner == null ? SwingUtilities.getSharedOwnerFrame() : owner, title, modal, gc);
        if (owner == null) {
            WindowListener ownerShutdownListener = SwingUtilities.getSharedOwnerFrameShutdownListener();
            this.addWindowListener(ownerShutdownListener);
        }
        this.dialogInit();
    }

    public JDialog(Dialog owner) {
        this(owner, false);
    }

    public JDialog(Dialog owner, boolean modal) {
        this(owner, "", modal);
    }

    public JDialog(Dialog owner, String title) {
        this(owner, title, false);
    }

    public JDialog(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
        this.dialogInit();
    }

    public JDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
        this.dialogInit();
    }

    public JDialog(Window owner) {
        this(owner, Dialog.ModalityType.MODELESS);
    }

    public JDialog(Window owner, Dialog.ModalityType modalityType) {
        this(owner, "", modalityType);
    }

    public JDialog(Window owner, String title) {
        this(owner, title, Dialog.ModalityType.MODELESS);
    }

    public JDialog(Window owner, String title, Dialog.ModalityType modalityType) {
        super(owner, title, modalityType);
        this.dialogInit();
    }

    public JDialog(Window owner, String title, Dialog.ModalityType modalityType, GraphicsConfiguration gc) {
        super(owner, title, modalityType, gc);
        this.dialogInit();
    }

    protected void dialogInit() {
        boolean supportsWindowDecorations;
        this.enableEvents(72L);
        this.setLocale(JComponent.getDefaultLocale());
        this.setRootPane(this.createRootPane());
        this.setBackground(UIManager.getColor("control"));
        this.setRootPaneCheckingEnabled(true);
        if (JDialog.isDefaultLookAndFeelDecorated() && (supportsWindowDecorations = UIManager.getLookAndFeel().getSupportsWindowDecorations())) {
            this.setUndecorated(true);
            this.getRootPane().setWindowDecorationStyle(2);
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
            }
        }
    }

    @BeanProperty(preferred=true, enumerationValues={"WindowConstants.DO_NOTHING_ON_CLOSE", "WindowConstants.HIDE_ON_CLOSE", "WindowConstants.DISPOSE_ON_CLOSE"}, description="The dialog's default close operation.")
    public void setDefaultCloseOperation(int operation) {
        if (operation != 0 && operation != 1 && operation != 2) {
            throw new IllegalArgumentException("defaultCloseOperation must be one of: DO_NOTHING_ON_CLOSE, HIDE_ON_CLOSE, or DISPOSE_ON_CLOSE");
        }
        int oldValue = this.defaultCloseOperation;
        this.defaultCloseOperation = operation;
        this.firePropertyChange("defaultCloseOperation", oldValue, operation);
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

    @BeanProperty(bound=false, hidden=true, description="The menubar for accessing pulldown menus from this dialog.")
    public void setJMenuBar(JMenuBar menu) {
        this.getRootPane().setJMenuBar(menu);
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
    @BeanProperty(bound=false, hidden=true, description="the RootPane object for this dialog.")
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
    @BeanProperty(bound=false, hidden=true, description="The client area of the dialog where child components are normally inserted.")
    public void setContentPane(Container contentPane) {
        this.getRootPane().setContentPane(contentPane);
    }

    @Override
    public JLayeredPane getLayeredPane() {
        return this.getRootPane().getLayeredPane();
    }

    @Override
    @BeanProperty(bound=false, hidden=true, description="The pane which holds the various dialog layers.")
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
        String defaultCloseOperationString = this.defaultCloseOperation == 1 ? "HIDE_ON_CLOSE" : (this.defaultCloseOperation == 2 ? "DISPOSE_ON_CLOSE" : (this.defaultCloseOperation == 0 ? "DO_NOTHING_ON_CLOSE" : ""));
        String rootPaneString = this.rootPane != null ? this.rootPane.toString() : "";
        String rootPaneCheckingEnabledString = this.rootPaneCheckingEnabled ? "true" : "false";
        return super.paramString() + ",defaultCloseOperation=" + defaultCloseOperationString + ",rootPane=" + rootPaneString + ",rootPaneCheckingEnabled=" + rootPaneCheckingEnabledString;
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJDialog();
        }
        return this.accessibleContext;
    }

    protected class AccessibleJDialog
    extends Dialog.AccessibleAWTDialog {
        protected AccessibleJDialog() {
        }

        @Override
        public String getAccessibleName() {
            if (this.accessibleName != null) {
                return this.accessibleName;
            }
            if (JDialog.this.getTitle() == null) {
                return super.getAccessibleName();
            }
            return JDialog.this.getTitle();
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (JDialog.this.isResizable()) {
                states.add(AccessibleState.RESIZABLE);
            }
            if (JDialog.this.getFocusOwner() != null) {
                states.add(AccessibleState.ACTIVE);
            }
            if (JDialog.this.isModal()) {
                states.add(AccessibleState.MODAL);
            }
            return states;
        }
    }
}

