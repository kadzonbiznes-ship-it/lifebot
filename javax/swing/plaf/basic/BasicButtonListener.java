/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentInputMapUIResource;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.basic.BasicLookAndFeel;
import javax.swing.plaf.basic.LazyActionMap;
import sun.swing.DefaultLookup;
import sun.swing.SwingUtilities2;
import sun.swing.UIAction;

public class BasicButtonListener
implements MouseListener,
MouseMotionListener,
FocusListener,
ChangeListener,
PropertyChangeListener {
    private long lastPressedTimestamp = -1L;
    private boolean shouldDiscardRelease = false;

    static void loadActionMap(LazyActionMap map) {
        map.put(new Actions("pressed"));
        map.put(new Actions("released"));
    }

    public BasicButtonListener(AbstractButton b) {
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        if (prop == "mnemonic") {
            this.updateMnemonicBinding((AbstractButton)e.getSource());
        } else if (prop == "contentAreaFilled") {
            this.checkOpacity((AbstractButton)e.getSource());
        } else if (prop == "text" || "font" == prop || "foreground" == prop || SwingUtilities2.isScaleChanged(e)) {
            AbstractButton b = (AbstractButton)e.getSource();
            BasicHTML.updateRenderer(b, b.getText());
        }
    }

    protected void checkOpacity(AbstractButton b) {
        b.setOpaque(b.isContentAreaFilled());
    }

    public void installKeyboardActions(JComponent c) {
        AbstractButton b = (AbstractButton)c;
        this.updateMnemonicBinding(b);
        LazyActionMap.installLazyActionMap(c, BasicButtonListener.class, "Button.actionMap");
        InputMap km = this.getInputMap(0, c);
        SwingUtilities.replaceUIInputMap(c, 0, km);
    }

    public void uninstallKeyboardActions(JComponent c) {
        SwingUtilities.replaceUIInputMap(c, 2, null);
        SwingUtilities.replaceUIInputMap(c, 0, null);
        SwingUtilities.replaceUIActionMap(c, null);
    }

    InputMap getInputMap(int condition, JComponent c) {
        BasicButtonUI ui;
        if (condition == 0 && (ui = (BasicButtonUI)BasicLookAndFeel.getUIOfType(((AbstractButton)c).getUI(), BasicButtonUI.class)) != null) {
            return (InputMap)DefaultLookup.get(c, ui, ui.getPropertyPrefix() + "focusInputMap");
        }
        return null;
    }

    void updateMnemonicBinding(AbstractButton b) {
        int m = b.getMnemonic();
        if (m != 0) {
            InputMap map = SwingUtilities.getUIInputMap(b, 2);
            if (map == null) {
                map = new ComponentInputMapUIResource(b);
                SwingUtilities.replaceUIInputMap(b, 2, map);
            }
            map.clear();
            map.put(KeyStroke.getKeyStroke(m, BasicLookAndFeel.getFocusAcceleratorKeyMask(), false), "pressed");
            map.put(KeyStroke.getKeyStroke(m, SwingUtilities2.setAltGraphMask(BasicLookAndFeel.getFocusAcceleratorKeyMask()), false), "pressed");
            map.put(KeyStroke.getKeyStroke(m, BasicLookAndFeel.getFocusAcceleratorKeyMask(), true), "released");
            map.put(KeyStroke.getKeyStroke(m, SwingUtilities2.setAltGraphMask(BasicLookAndFeel.getFocusAcceleratorKeyMask()), true), "released");
            map.put(KeyStroke.getKeyStroke(m, 0, true), "released");
        } else {
            InputMap map = SwingUtilities.getUIInputMap(b, 2);
            if (map != null) {
                map.clear();
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        AbstractButton b = (AbstractButton)e.getSource();
        b.repaint();
    }

    @Override
    public void focusGained(FocusEvent e) {
        BasicButtonUI ui;
        JRootPane root;
        AbstractButton b = (AbstractButton)e.getSource();
        if (b instanceof JButton && ((JButton)b).isDefaultCapable() && (root = b.getRootPane()) != null && (ui = (BasicButtonUI)BasicLookAndFeel.getUIOfType(b.getUI(), BasicButtonUI.class)) != null && DefaultLookup.getBoolean(b, ui, ui.getPropertyPrefix() + "defaultButtonFollowsFocus", true)) {
            root.putClientProperty("temporaryDefaultButton", b);
            root.setDefaultButton((JButton)b);
            root.putClientProperty("temporaryDefaultButton", null);
        }
        b.repaint();
    }

    @Override
    public void focusLost(FocusEvent e) {
        BasicButtonUI ui;
        JButton initialDefault;
        AbstractButton b = (AbstractButton)e.getSource();
        JRootPane root = b.getRootPane();
        if (root != null && b != (initialDefault = (JButton)root.getClientProperty("initialDefaultButton")) && (ui = (BasicButtonUI)BasicLookAndFeel.getUIOfType(b.getUI(), BasicButtonUI.class)) != null && DefaultLookup.getBoolean(b, ui, ui.getPropertyPrefix() + "defaultButtonFollowsFocus", true)) {
            root.setDefaultButton(initialDefault);
        }
        ButtonModel model = b.getModel();
        model.setPressed(false);
        model.setArmed(false);
        b.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        AbstractButton b;
        if (SwingUtilities.isLeftMouseButton(e) && (b = (AbstractButton)e.getSource()).contains(e.getX(), e.getY())) {
            long lastTime = this.lastPressedTimestamp;
            this.lastPressedTimestamp = e.getWhen();
            long timeSinceLastClick = this.lastPressedTimestamp - lastTime;
            if (lastTime != -1L && timeSinceLastClick > 0L && timeSinceLastClick < b.getMultiClickThreshhold()) {
                this.shouldDiscardRelease = true;
                return;
            }
            ButtonModel model = b.getModel();
            if (!model.isEnabled()) {
                return;
            }
            if (!model.isArmed()) {
                model.setArmed(true);
            }
            model.setPressed(true);
            if (!b.hasFocus() && b.isRequestFocusEnabled()) {
                b.requestFocus(FocusEvent.Cause.MOUSE_EVENT);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (this.shouldDiscardRelease) {
                this.shouldDiscardRelease = false;
                return;
            }
            AbstractButton b = (AbstractButton)e.getSource();
            ButtonModel model = b.getModel();
            model.setPressed(false);
            model.setArmed(false);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        AbstractButton b = (AbstractButton)e.getSource();
        ButtonModel model = b.getModel();
        if (b.isRolloverEnabled() && !SwingUtilities.isLeftMouseButton(e)) {
            model.setRollover(true);
        }
        if (model.isPressed()) {
            model.setArmed(true);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        AbstractButton b = (AbstractButton)e.getSource();
        ButtonModel model = b.getModel();
        if (b.isRolloverEnabled()) {
            model.setRollover(false);
        }
        model.setArmed(false);
    }

    private static class Actions
    extends UIAction {
        private static final String PRESS = "pressed";
        private static final String RELEASE = "released";

        Actions(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AbstractButton b = (AbstractButton)e.getSource();
            String key = this.getName();
            if (key == PRESS) {
                ButtonModel model = b.getModel();
                model.setArmed(true);
                model.setPressed(true);
                if (!b.hasFocus()) {
                    b.requestFocus();
                }
            } else if (key == RELEASE) {
                ButtonModel model = b.getModel();
                model.setPressed(false);
                model.setArmed(false);
            }
        }

        @Override
        public boolean accept(Object sender) {
            return !(sender instanceof AbstractButton) || ((AbstractButton)sender).getModel().isEnabled();
        }
    }
}

