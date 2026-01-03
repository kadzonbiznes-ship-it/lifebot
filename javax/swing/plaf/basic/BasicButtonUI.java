/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicButtonListener;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import sun.awt.AppContext;
import sun.swing.SwingUtilities2;

public class BasicButtonUI
extends ButtonUI {
    protected int defaultTextIconGap;
    private int shiftOffset = 0;
    protected int defaultTextShiftOffset;
    private static final String propertyPrefix = "Button.";
    private static final Object BASIC_BUTTON_UI_KEY = new Object();
    private KeyListener keyListener = null;
    private static Rectangle viewRect = new Rectangle();
    private static Rectangle textRect = new Rectangle();
    private static Rectangle iconRect = new Rectangle();

    public static ComponentUI createUI(JComponent c) {
        AppContext appContext = AppContext.getAppContext();
        BasicButtonUI buttonUI = (BasicButtonUI)appContext.get(BASIC_BUTTON_UI_KEY);
        if (buttonUI == null) {
            buttonUI = new BasicButtonUI();
            appContext.put(BASIC_BUTTON_UI_KEY, buttonUI);
        }
        return buttonUI;
    }

    protected String getPropertyPrefix() {
        return propertyPrefix;
    }

    @Override
    public void installUI(JComponent c) {
        this.installDefaults((AbstractButton)c);
        this.installListeners((AbstractButton)c);
        this.installKeyboardActions((AbstractButton)c);
        BasicHTML.updateRenderer(c, ((AbstractButton)c).getText());
    }

    protected void installDefaults(AbstractButton b) {
        String pp = this.getPropertyPrefix();
        this.defaultTextShiftOffset = UIManager.getInt(pp + "textShiftOffset");
        if (b.isContentAreaFilled()) {
            LookAndFeel.installProperty(b, "opaque", Boolean.TRUE);
        } else {
            LookAndFeel.installProperty(b, "opaque", Boolean.FALSE);
        }
        if (b.getMargin() == null || b.getMargin() instanceof UIResource) {
            b.setMargin(UIManager.getInsets(pp + "margin"));
        }
        LookAndFeel.installColorsAndFont(b, pp + "background", pp + "foreground", pp + "font");
        LookAndFeel.installBorder(b, pp + "border");
        Object rollover = UIManager.get(pp + "rollover");
        if (rollover != null) {
            LookAndFeel.installProperty(b, "rolloverEnabled", rollover);
        }
        LookAndFeel.installProperty(b, "iconTextGap", 4);
    }

    protected void installListeners(AbstractButton b) {
        BasicButtonListener listener = this.createButtonListener(b);
        if (listener != null) {
            b.addMouseListener(listener);
            b.addMouseMotionListener(listener);
            b.addFocusListener(listener);
            b.addPropertyChangeListener(listener);
            b.addChangeListener(listener);
        }
        if (b instanceof JToggleButton) {
            this.keyListener = this.createKeyListener();
            b.addKeyListener(this.keyListener);
            b.setFocusTraversalKeysEnabled(false);
            b.getActionMap().put("Previous", new SelectPreviousBtn());
            b.getActionMap().put("Next", new SelectNextBtn());
            b.getInputMap(1).put(KeyStroke.getKeyStroke("UP"), "Previous");
            b.getInputMap(1).put(KeyStroke.getKeyStroke("DOWN"), "Next");
            b.getInputMap(1).put(KeyStroke.getKeyStroke("LEFT"), "Previous");
            b.getInputMap(1).put(KeyStroke.getKeyStroke("RIGHT"), "Next");
        }
    }

    protected void installKeyboardActions(AbstractButton b) {
        BasicButtonListener listener = this.getButtonListener(b);
        if (listener != null) {
            listener.installKeyboardActions(b);
        }
    }

    @Override
    public void uninstallUI(JComponent c) {
        this.uninstallKeyboardActions((AbstractButton)c);
        this.uninstallListeners((AbstractButton)c);
        this.uninstallDefaults((AbstractButton)c);
        BasicHTML.updateRenderer(c, "");
    }

    protected void uninstallKeyboardActions(AbstractButton b) {
        BasicButtonListener listener = this.getButtonListener(b);
        if (listener != null) {
            listener.uninstallKeyboardActions(b);
        }
    }

    protected void uninstallListeners(AbstractButton b) {
        BasicButtonListener listener = this.getButtonListener(b);
        if (listener != null) {
            b.removeMouseListener(listener);
            b.removeMouseMotionListener(listener);
            b.removeFocusListener(listener);
            b.removeChangeListener(listener);
            b.removePropertyChangeListener(listener);
        }
        if (b instanceof JToggleButton) {
            b.getActionMap().remove("Previous");
            b.getActionMap().remove("Next");
            b.getInputMap(1).remove(KeyStroke.getKeyStroke("UP"));
            b.getInputMap(1).remove(KeyStroke.getKeyStroke("DOWN"));
            b.getInputMap(1).remove(KeyStroke.getKeyStroke("LEFT"));
            b.getInputMap(1).remove(KeyStroke.getKeyStroke("RIGHT"));
            if (this.keyListener != null) {
                b.removeKeyListener(this.keyListener);
                this.keyListener = null;
            }
        }
    }

    protected void uninstallDefaults(AbstractButton b) {
        LookAndFeel.uninstallBorder(b);
    }

    protected BasicButtonListener createButtonListener(AbstractButton b) {
        return new BasicButtonListener(b);
    }

    public int getDefaultTextIconGap(AbstractButton b) {
        return this.defaultTextIconGap;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton)c;
        ButtonModel model = b.getModel();
        String text = this.layout(b, SwingUtilities2.getFontMetrics((JComponent)b, g), b.getWidth(), b.getHeight());
        this.clearTextShiftOffset();
        if (model.isArmed() && model.isPressed()) {
            this.paintButtonPressed(g, b);
        }
        if (b.getIcon() != null) {
            this.paintIcon(g, c, iconRect);
        }
        if (text != null && !text.isEmpty()) {
            View v = (View)c.getClientProperty("html");
            if (v != null) {
                v.paint(g, textRect);
            } else {
                this.paintText(g, b, textRect, text);
            }
        }
        if (b.isFocusPainted() && b.hasFocus()) {
            this.paintFocus(g, b, viewRect, textRect, iconRect);
        }
    }

    protected void paintIcon(Graphics g, JComponent c, Rectangle iconRect) {
        AbstractButton b = (AbstractButton)c;
        ButtonModel model = b.getModel();
        Icon icon = b.getIcon();
        Icon tmpIcon = null;
        if (icon == null) {
            return;
        }
        Icon selectedIcon = null;
        if (model.isSelected() && (selectedIcon = b.getSelectedIcon()) != null) {
            icon = selectedIcon;
        }
        if (!model.isEnabled()) {
            if (model.isSelected() && (tmpIcon = b.getDisabledSelectedIcon()) == null) {
                tmpIcon = selectedIcon;
            }
            if (tmpIcon == null) {
                tmpIcon = b.getDisabledIcon();
            }
        } else if (model.isPressed() && model.isArmed()) {
            tmpIcon = b.getPressedIcon();
            if (tmpIcon != null) {
                this.clearTextShiftOffset();
            }
        } else if (b.isRolloverEnabled() && model.isRollover()) {
            if (model.isSelected() && (tmpIcon = b.getRolloverSelectedIcon()) == null) {
                tmpIcon = selectedIcon;
            }
            if (tmpIcon == null) {
                tmpIcon = b.getRolloverIcon();
            }
        }
        if (tmpIcon != null) {
            icon = tmpIcon;
        }
        if (model.isPressed() && model.isArmed()) {
            icon.paintIcon(c, g, iconRect.x + this.getTextShiftOffset(), iconRect.y + this.getTextShiftOffset());
        } else {
            icon.paintIcon(c, g, iconRect.x, iconRect.y);
        }
    }

    protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {
        AbstractButton b = (AbstractButton)c;
        ButtonModel model = b.getModel();
        FontMetrics fm = SwingUtilities2.getFontMetrics(c, g);
        int mnemonicIndex = b.getDisplayedMnemonicIndex();
        if (model.isEnabled()) {
            g.setColor(b.getForeground());
            SwingUtilities2.drawStringUnderlineCharAt(c, g, text, mnemonicIndex, textRect.x + this.getTextShiftOffset(), textRect.y + fm.getAscent() + this.getTextShiftOffset());
        } else {
            g.setColor(b.getBackground().brighter());
            SwingUtilities2.drawStringUnderlineCharAt(c, g, text, mnemonicIndex, textRect.x, textRect.y + fm.getAscent());
            g.setColor(b.getBackground().darker());
            SwingUtilities2.drawStringUnderlineCharAt(c, g, text, mnemonicIndex, textRect.x - 1, textRect.y + fm.getAscent() - 1);
        }
    }

    protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
        this.paintText(g, (JComponent)b, textRect, text);
    }

    protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect, Rectangle iconRect) {
    }

    protected void paintButtonPressed(Graphics g, AbstractButton b) {
    }

    protected void clearTextShiftOffset() {
        this.shiftOffset = 0;
    }

    protected void setTextShiftOffset() {
        this.shiftOffset = this.defaultTextShiftOffset;
    }

    protected int getTextShiftOffset() {
        return this.shiftOffset;
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        Dimension d = this.getPreferredSize(c);
        View v = (View)c.getClientProperty("html");
        if (v != null) {
            d.width = (int)((float)d.width - (v.getPreferredSpan(0) - v.getMinimumSpan(0)));
        }
        return d;
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        AbstractButton b = (AbstractButton)c;
        return BasicGraphicsUtils.getPreferredButtonSize(b, b.getIconTextGap());
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        Dimension d = this.getPreferredSize(c);
        View v = (View)c.getClientProperty("html");
        if (v != null) {
            d.width = (int)((float)d.width + (v.getMaximumSpan(0) - v.getPreferredSpan(0)));
        }
        return d;
    }

    @Override
    public int getBaseline(JComponent c, int width, int height) {
        super.getBaseline(c, width, height);
        AbstractButton b = (AbstractButton)c;
        String text = b.getText();
        if (text == null || text.isEmpty()) {
            return -1;
        }
        FontMetrics fm = b.getFontMetrics(b.getFont());
        this.layout(b, fm, width, height);
        return BasicHTML.getBaseline(b, BasicButtonUI.textRect.y, fm.getAscent(), BasicButtonUI.textRect.width, BasicButtonUI.textRect.height);
    }

    @Override
    public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
        super.getBaselineResizeBehavior(c);
        if (c.getClientProperty("html") != null) {
            return Component.BaselineResizeBehavior.OTHER;
        }
        switch (((AbstractButton)c).getVerticalAlignment()) {
            case 1: {
                return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
            }
            case 3: {
                return Component.BaselineResizeBehavior.CONSTANT_DESCENT;
            }
            case 0: {
                return Component.BaselineResizeBehavior.CENTER_OFFSET;
            }
        }
        return Component.BaselineResizeBehavior.OTHER;
    }

    private String layout(AbstractButton b, FontMetrics fm, int width, int height) {
        Insets i = b.getInsets();
        BasicButtonUI.viewRect.x = i.left;
        BasicButtonUI.viewRect.y = i.top;
        BasicButtonUI.viewRect.width = width - (i.right + BasicButtonUI.viewRect.x);
        BasicButtonUI.viewRect.height = height - (i.bottom + BasicButtonUI.viewRect.y);
        BasicButtonUI.textRect.height = 0;
        BasicButtonUI.textRect.width = 0;
        BasicButtonUI.textRect.y = 0;
        BasicButtonUI.textRect.x = 0;
        BasicButtonUI.iconRect.height = 0;
        BasicButtonUI.iconRect.width = 0;
        BasicButtonUI.iconRect.y = 0;
        BasicButtonUI.iconRect.x = 0;
        return SwingUtilities.layoutCompoundLabel(b, fm, b.getText(), b.getIcon(), b.getVerticalAlignment(), b.getHorizontalAlignment(), b.getVerticalTextPosition(), b.getHorizontalTextPosition(), viewRect, iconRect, textRect, b.getText() == null ? 0 : b.getIconTextGap());
    }

    private BasicButtonListener getButtonListener(AbstractButton b) {
        MouseMotionListener[] listeners = b.getMouseMotionListeners();
        if (listeners != null) {
            for (MouseMotionListener listener : listeners) {
                if (!(listener instanceof BasicButtonListener)) continue;
                return (BasicButtonListener)listener;
            }
        }
        return null;
    }

    private KeyListener createKeyListener() {
        if (this.keyListener == null) {
            this.keyListener = new KeyHandler();
        }
        return this.keyListener;
    }

    private boolean isValidToggleButtonObj(Object obj) {
        return obj instanceof JToggleButton && ((JToggleButton)obj).isVisible() && ((JToggleButton)obj).isEnabled();
    }

    private void selectToggleButton(ActionEvent event, boolean next) {
        Object eventSrc = event.getSource();
        if (!this.isValidToggleButtonObj(eventSrc)) {
            return;
        }
        ButtonGroupInfo btnGroupInfo = new ButtonGroupInfo((JToggleButton)eventSrc);
        btnGroupInfo.selectNewButton(next);
    }

    private class SelectPreviousBtn
    extends AbstractAction {
        public SelectPreviousBtn() {
            super("Previous");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BasicButtonUI.this.selectToggleButton(e, false);
        }
    }

    private class SelectNextBtn
    extends AbstractAction {
        public SelectNextBtn() {
            super("Next");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BasicButtonUI.this.selectToggleButton(e, true);
        }
    }

    private class KeyHandler
    implements KeyListener {
        private KeyHandler() {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            JToggleButton source;
            boolean next;
            AWTKeyStroke stroke = AWTKeyStroke.getAWTKeyStrokeForEvent(e);
            if (stroke != null && e.getSource() instanceof JToggleButton && ((next = this.isFocusTraversalKey(source = (JToggleButton)e.getSource(), 0, stroke)) || this.isFocusTraversalKey(source, 1, stroke))) {
                e.consume();
                ButtonGroupInfo btnGroupInfo = new ButtonGroupInfo(source);
                btnGroupInfo.jumpToNextComponent(next);
            }
        }

        private boolean isFocusTraversalKey(JComponent c, int id, AWTKeyStroke stroke) {
            Set<AWTKeyStroke> keys = c.getFocusTraversalKeys(id);
            return keys != null && keys.contains(stroke);
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }
    }

    private class ButtonGroupInfo {
        JToggleButton activeBtn = null;
        JToggleButton firstBtn = null;
        JToggleButton lastBtn = null;
        JToggleButton previousBtn = null;
        JToggleButton nextBtn = null;
        HashSet<JToggleButton> btnsInGroup = null;
        boolean srcFound = false;

        public ButtonGroupInfo(JToggleButton btn) {
            this.activeBtn = btn;
            this.btnsInGroup = new HashSet();
        }

        boolean containsInGroup(Object obj) {
            return this.btnsInGroup.contains(obj);
        }

        Component getFocusTransferBaseComponent(boolean next) {
            return this.firstBtn;
        }

        boolean getButtonGroupInfo() {
            if (this.activeBtn == null) {
                return false;
            }
            this.btnsInGroup.clear();
            ButtonModel model = this.activeBtn.getModel();
            if (!(model instanceof DefaultButtonModel)) {
                return false;
            }
            DefaultButtonModel bm = (DefaultButtonModel)model;
            ButtonGroup group = bm.getGroup();
            if (group == null) {
                return false;
            }
            Enumeration<AbstractButton> e = group.getElements();
            if (e == null) {
                return false;
            }
            while (e.hasMoreElements()) {
                AbstractButton curElement = e.nextElement();
                if (!BasicButtonUI.this.isValidToggleButtonObj(curElement)) continue;
                this.btnsInGroup.add((JToggleButton)curElement);
                if (null == this.firstBtn) {
                    this.firstBtn = (JToggleButton)curElement;
                }
                if (this.activeBtn == curElement) {
                    this.srcFound = true;
                } else if (!this.srcFound) {
                    this.previousBtn = (JToggleButton)curElement;
                } else if (this.nextBtn == null) {
                    this.nextBtn = (JToggleButton)curElement;
                }
                this.lastBtn = (JToggleButton)curElement;
            }
            return true;
        }

        void selectNewButton(boolean next) {
            if (!this.getButtonGroupInfo()) {
                return;
            }
            if (this.srcFound) {
                JToggleButton newSelectedBtn = null;
                if (next) {
                    newSelectedBtn = null == this.nextBtn ? this.firstBtn : this.nextBtn;
                } else {
                    JToggleButton jToggleButton = newSelectedBtn = null == this.previousBtn ? this.lastBtn : this.previousBtn;
                }
                if (newSelectedBtn != null && newSelectedBtn != this.activeBtn) {
                    ButtonModel btnModel = newSelectedBtn.getModel();
                    if (newSelectedBtn instanceof JRadioButton) {
                        btnModel.setPressed(true);
                        btnModel.setArmed(true);
                    }
                    newSelectedBtn.requestFocusInWindow();
                    newSelectedBtn.setSelected(true);
                    if (newSelectedBtn instanceof JRadioButton) {
                        btnModel.setPressed(false);
                        btnModel.setArmed(false);
                    }
                }
            }
        }

        void jumpToNextComponent(boolean next) {
            if (!this.getButtonGroupInfo()) {
                if (this.activeBtn != null) {
                    this.lastBtn = this.activeBtn;
                    this.firstBtn = this.activeBtn;
                } else {
                    return;
                }
            }
            JToggleButton compTransferFocusFrom = this.activeBtn;
            Component focusBase = this.getFocusTransferBaseComponent(next);
            if (focusBase != null) {
                if (next) {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent(focusBase);
                } else {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().focusPreviousComponent(focusBase);
                }
            }
        }
    }
}

