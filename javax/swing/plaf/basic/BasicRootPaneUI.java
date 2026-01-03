/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ComponentInputMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.LookAndFeel;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentInputMapUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.RootPaneUI;
import javax.swing.plaf.basic.LazyActionMap;
import sun.swing.DefaultLookup;
import sun.swing.UIAction;

public class BasicRootPaneUI
extends RootPaneUI
implements PropertyChangeListener {
    private static RootPaneUI rootPaneUI = new BasicRootPaneUI();

    public static ComponentUI createUI(JComponent c) {
        return rootPaneUI;
    }

    @Override
    public void installUI(JComponent c) {
        this.installDefaults((JRootPane)c);
        this.installComponents((JRootPane)c);
        this.installListeners((JRootPane)c);
        this.installKeyboardActions((JRootPane)c);
    }

    @Override
    public void uninstallUI(JComponent c) {
        this.uninstallDefaults((JRootPane)c);
        this.uninstallComponents((JRootPane)c);
        this.uninstallListeners((JRootPane)c);
        this.uninstallKeyboardActions((JRootPane)c);
    }

    protected void installDefaults(JRootPane c) {
        LookAndFeel.installProperty(c, "opaque", Boolean.FALSE);
    }

    protected void installComponents(JRootPane root) {
    }

    protected void installListeners(JRootPane root) {
        root.addPropertyChangeListener(this);
    }

    protected void installKeyboardActions(JRootPane root) {
        InputMap km = this.getInputMap(2, root);
        SwingUtilities.replaceUIInputMap(root, 2, km);
        km = this.getInputMap(1, root);
        SwingUtilities.replaceUIInputMap(root, 1, km);
        LazyActionMap.installLazyActionMap(root, BasicRootPaneUI.class, "RootPane.actionMap");
        this.updateDefaultButtonBindings(root);
    }

    protected void uninstallDefaults(JRootPane root) {
    }

    protected void uninstallComponents(JRootPane root) {
    }

    protected void uninstallListeners(JRootPane root) {
        root.removePropertyChangeListener(this);
    }

    protected void uninstallKeyboardActions(JRootPane root) {
        SwingUtilities.replaceUIInputMap(root, 2, null);
        SwingUtilities.replaceUIActionMap(root, null);
    }

    InputMap getInputMap(int condition, JComponent c) {
        if (condition == 1) {
            return (InputMap)DefaultLookup.get(c, this, "RootPane.ancestorInputMap");
        }
        if (condition == 2) {
            return this.createInputMap(condition, c);
        }
        return null;
    }

    ComponentInputMap createInputMap(int condition, JComponent c) {
        return new RootPaneInputMap(c);
    }

    static void loadActionMap(LazyActionMap map) {
        map.put(new Actions("press"));
        map.put(new Actions("release"));
        map.put(new Actions("postPopup"));
    }

    void updateDefaultButtonBindings(JRootPane root) {
        InputMap km;
        for (km = SwingUtilities.getUIInputMap(root, 2); km != null && !(km instanceof RootPaneInputMap); km = km.getParent()) {
        }
        if (km != null) {
            Object[] bindings;
            km.clear();
            if (root.getDefaultButton() != null && (bindings = (Object[])DefaultLookup.get(root, this, "RootPane.defaultButtonWindowKeyBindings")) != null) {
                LookAndFeel.loadKeyBindings(km, bindings);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("defaultButton")) {
            JRootPane rootpane = (JRootPane)e.getSource();
            this.updateDefaultButtonBindings(rootpane);
            if (rootpane.getClientProperty("temporaryDefaultButton") == null) {
                rootpane.putClientProperty("initialDefaultButton", e.getNewValue());
            }
        }
    }

    private static class RootPaneInputMap
    extends ComponentInputMapUIResource {
        public RootPaneInputMap(JComponent c) {
            super(c);
        }
    }

    static class Actions
    extends UIAction {
        public static final String PRESS = "press";
        public static final String RELEASE = "release";
        public static final String POST_POPUP = "postPopup";

        Actions(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            JRootPane root = (JRootPane)evt.getSource();
            JButton owner = root.getDefaultButton();
            String key = this.getName();
            if (key == POST_POPUP) {
                JComponent src;
                JPopupMenu jpm;
                Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (c instanceof JComponent && (jpm = (src = (JComponent)c).getComponentPopupMenu()) != null) {
                    Point pt = src.getPopupLocation(null);
                    if (pt == null) {
                        Rectangle vis = src.getVisibleRect();
                        pt = new Point(vis.x + vis.width / 2, vis.y + vis.height / 2);
                    }
                    jpm.show(c, pt.x, pt.y);
                }
            } else if (owner != null && SwingUtilities.getRootPane(owner) == root && key == PRESS) {
                owner.doClick(20);
            }
        }

        @Override
        public boolean accept(Object sender) {
            String key = this.getName();
            if (key == POST_POPUP) {
                MenuElement[] elems = MenuSelectionManager.defaultManager().getSelectedPath();
                if (elems != null && elems.length != 0) {
                    return false;
                }
                Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (c instanceof JComponent) {
                    JComponent src = (JComponent)c;
                    return src.getComponentPopupMenu() != null;
                }
                return false;
            }
            if (sender instanceof JRootPane) {
                JButton owner = ((JRootPane)sender).getDefaultButton();
                return owner != null && owner.getModel().isEnabled() && owner.isShowing();
            }
            return true;
        }
    }
}

