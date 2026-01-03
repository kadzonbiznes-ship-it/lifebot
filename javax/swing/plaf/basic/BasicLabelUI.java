/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentInputMapUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.InputMapUIResource;
import javax.swing.plaf.LabelUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.basic.BasicLookAndFeel;
import javax.swing.plaf.basic.LazyActionMap;
import javax.swing.text.View;
import sun.awt.AppContext;
import sun.swing.SwingUtilities2;
import sun.swing.UIAction;

public class BasicLabelUI
extends LabelUI
implements PropertyChangeListener {
    protected static BasicLabelUI labelUI = new BasicLabelUI();
    private static final Object BASIC_LABEL_UI_KEY = new Object();
    private Rectangle paintIconR = new Rectangle();
    private Rectangle paintTextR = new Rectangle();

    static void loadActionMap(LazyActionMap map) {
        map.put(new Actions("press"));
        map.put(new Actions("release"));
    }

    protected String layoutCL(JLabel label, FontMetrics fontMetrics, String text, Icon icon, Rectangle viewR, Rectangle iconR, Rectangle textR) {
        return SwingUtilities.layoutCompoundLabel(label, fontMetrics, text, icon, label.getVerticalAlignment(), label.getHorizontalAlignment(), label.getVerticalTextPosition(), label.getHorizontalTextPosition(), viewR, iconR, textR, label.getIconTextGap());
    }

    protected void paintEnabledText(JLabel l, Graphics g, String s, int textX, int textY) {
        int mnemIndex = l.getDisplayedMnemonicIndex();
        g.setColor(l.getForeground());
        SwingUtilities2.drawStringUnderlineCharAt(l, g, s, mnemIndex, textX, textY);
    }

    protected void paintDisabledText(JLabel l, Graphics g, String s, int textX, int textY) {
        int accChar = l.getDisplayedMnemonicIndex();
        Color background = l.getBackground();
        g.setColor(background.brighter());
        SwingUtilities2.drawStringUnderlineCharAt(l, g, s, accChar, textX + 1, textY + 1);
        g.setColor(background.darker());
        SwingUtilities2.drawStringUnderlineCharAt(l, g, s, accChar, textX, textY);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Icon icon;
        JLabel label = (JLabel)c;
        String text = label.getText();
        Icon icon2 = icon = label.isEnabled() ? label.getIcon() : label.getDisabledIcon();
        if (icon == null && text == null) {
            return;
        }
        FontMetrics fm = SwingUtilities2.getFontMetrics((JComponent)label, g);
        String clippedText = this.layout(label, fm, c.getWidth(), c.getHeight());
        if (icon != null) {
            icon.paintIcon(c, g, this.paintIconR.x, this.paintIconR.y);
        }
        if (text != null) {
            View v = (View)c.getClientProperty("html");
            if (v != null) {
                v.paint(g, this.paintTextR);
            } else {
                int textX = this.paintTextR.x;
                int textY = this.paintTextR.y + fm.getAscent();
                if (label.isEnabled()) {
                    this.paintEnabledText(label, g, clippedText, textX, textY);
                } else {
                    this.paintDisabledText(label, g, clippedText, textX, textY);
                }
            }
        }
    }

    private String layout(JLabel label, FontMetrics fm, int width, int height) {
        Insets insets = label.getInsets(null);
        String text = label.getText();
        Icon icon = label.isEnabled() ? label.getIcon() : label.getDisabledIcon();
        Rectangle paintViewR = new Rectangle();
        paintViewR.x = insets.left;
        paintViewR.y = insets.top;
        paintViewR.width = width - (insets.left + insets.right);
        paintViewR.height = height - (insets.top + insets.bottom);
        this.paintIconR.height = 0;
        this.paintIconR.width = 0;
        this.paintIconR.y = 0;
        this.paintIconR.x = 0;
        this.paintTextR.height = 0;
        this.paintTextR.width = 0;
        this.paintTextR.y = 0;
        this.paintTextR.x = 0;
        return this.layoutCL(label, fm, text, icon, paintViewR, this.paintIconR, this.paintTextR);
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        JLabel label = (JLabel)c;
        String text = label.getText();
        Icon icon = label.isEnabled() ? label.getIcon() : label.getDisabledIcon();
        Insets insets = label.getInsets(null);
        Font font = label.getFont();
        int dx = insets.left + insets.right;
        int dy = insets.top + insets.bottom;
        if (icon == null && (text == null || text != null && font == null)) {
            return new Dimension(dx, dy);
        }
        if (text == null || icon != null && font == null) {
            return new Dimension(icon.getIconWidth() + dx, icon.getIconHeight() + dy);
        }
        FontMetrics fm = label.getFontMetrics(font);
        Rectangle iconR = new Rectangle();
        Rectangle textR = new Rectangle();
        Rectangle viewR = new Rectangle();
        iconR.height = 0;
        iconR.width = 0;
        iconR.y = 0;
        iconR.x = 0;
        textR.height = 0;
        textR.width = 0;
        textR.y = 0;
        textR.x = 0;
        viewR.x = dx;
        viewR.y = dy;
        viewR.height = Short.MAX_VALUE;
        viewR.width = Short.MAX_VALUE;
        this.layoutCL(label, fm, text, icon, viewR, iconR, textR);
        int x1 = Math.min(iconR.x, textR.x);
        int x2 = Math.max(iconR.x + iconR.width, textR.x + textR.width);
        int y1 = Math.min(iconR.y, textR.y);
        int y2 = Math.max(iconR.y + iconR.height, textR.y + textR.height);
        Dimension rv = new Dimension(x2 - x1, y2 - y1);
        rv.width += dx;
        rv.height += dy;
        return rv;
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
        JLabel label = (JLabel)c;
        String text = label.getText();
        if (text == null || text.isEmpty() || label.getFont() == null) {
            return -1;
        }
        FontMetrics fm = label.getFontMetrics(label.getFont());
        this.layout(label, fm, width, height);
        return BasicHTML.getBaseline(label, this.paintTextR.y, fm.getAscent(), this.paintTextR.width, this.paintTextR.height);
    }

    @Override
    public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
        super.getBaselineResizeBehavior(c);
        if (c.getClientProperty("html") != null) {
            return Component.BaselineResizeBehavior.OTHER;
        }
        switch (((JLabel)c).getVerticalAlignment()) {
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

    @Override
    public void installUI(JComponent c) {
        this.installDefaults((JLabel)c);
        this.installComponents((JLabel)c);
        this.installListeners((JLabel)c);
        this.installKeyboardActions((JLabel)c);
    }

    @Override
    public void uninstallUI(JComponent c) {
        this.uninstallDefaults((JLabel)c);
        this.uninstallComponents((JLabel)c);
        this.uninstallListeners((JLabel)c);
        this.uninstallKeyboardActions((JLabel)c);
    }

    protected void installDefaults(JLabel c) {
        LookAndFeel.installColorsAndFont(c, "Label.background", "Label.foreground", "Label.font");
        LookAndFeel.installProperty(c, "opaque", Boolean.FALSE);
    }

    protected void installListeners(JLabel c) {
        c.addPropertyChangeListener(this);
    }

    protected void installComponents(JLabel c) {
        BasicHTML.updateRenderer(c, c.getText());
        c.setInheritsPopupMenu(true);
    }

    protected void installKeyboardActions(JLabel l) {
        int dka = l.getDisplayedMnemonic();
        Component lf = l.getLabelFor();
        if (dka != 0 && lf != null) {
            LazyActionMap.installLazyActionMap(l, BasicLabelUI.class, "Label.actionMap");
            InputMap inputMap = SwingUtilities.getUIInputMap(l, 2);
            if (inputMap == null) {
                inputMap = new ComponentInputMapUIResource(l);
                SwingUtilities.replaceUIInputMap(l, 2, inputMap);
            }
            inputMap.clear();
            inputMap.put(KeyStroke.getKeyStroke(dka, BasicLookAndFeel.getFocusAcceleratorKeyMask(), false), "press");
            inputMap.put(KeyStroke.getKeyStroke(dka, SwingUtilities2.setAltGraphMask(BasicLookAndFeel.getFocusAcceleratorKeyMask()), false), "press");
        } else {
            InputMap inputMap = SwingUtilities.getUIInputMap(l, 2);
            if (inputMap != null) {
                inputMap.clear();
            }
        }
    }

    protected void uninstallDefaults(JLabel c) {
    }

    protected void uninstallListeners(JLabel c) {
        c.removePropertyChangeListener(this);
    }

    protected void uninstallComponents(JLabel c) {
        BasicHTML.updateRenderer(c, "");
    }

    protected void uninstallKeyboardActions(JLabel c) {
        SwingUtilities.replaceUIInputMap(c, 0, null);
        SwingUtilities.replaceUIInputMap(c, 2, null);
        SwingUtilities.replaceUIActionMap(c, null);
    }

    public static ComponentUI createUI(JComponent c) {
        if (System.getSecurityManager() != null) {
            AppContext appContext = AppContext.getAppContext();
            BasicLabelUI safeBasicLabelUI = (BasicLabelUI)appContext.get(BASIC_LABEL_UI_KEY);
            if (safeBasicLabelUI == null) {
                safeBasicLabelUI = new BasicLabelUI();
                appContext.put(BASIC_LABEL_UI_KEY, safeBasicLabelUI);
            }
            return safeBasicLabelUI;
        }
        return labelUI;
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        String name = e.getPropertyName();
        if (name == "text" || "font" == name || "foreground" == name || SwingUtilities2.isScaleChanged(e)) {
            JLabel lbl = (JLabel)e.getSource();
            String text = lbl.getText();
            BasicHTML.updateRenderer(lbl, text);
        } else if (name == "labelFor" || name == "displayedMnemonic") {
            this.installKeyboardActions((JLabel)e.getSource());
        }
    }

    private static class Actions
    extends UIAction {
        private static final String PRESS = "press";
        private static final String RELEASE = "release";

        Actions(String key) {
            super(key);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JLabel label = (JLabel)e.getSource();
            String key = this.getName();
            if (key == PRESS) {
                this.doPress(label);
            } else if (key == RELEASE) {
                this.doRelease(label, e.getActionCommand() != null);
            }
        }

        private void doPress(JLabel label) {
            Component labelFor = label.getLabelFor();
            if (labelFor != null && labelFor.isEnabled()) {
                InputMap inputMap = SwingUtilities.getUIInputMap(label, 0);
                if (inputMap == null) {
                    inputMap = new InputMapUIResource();
                    SwingUtilities.replaceUIInputMap(label, 0, inputMap);
                }
                int dka = label.getDisplayedMnemonic();
                this.putOnRelease(inputMap, dka, BasicLookAndFeel.getFocusAcceleratorKeyMask());
                this.putOnRelease(inputMap, dka, SwingUtilities2.setAltGraphMask(BasicLookAndFeel.getFocusAcceleratorKeyMask()));
                this.putOnRelease(inputMap, dka, 0);
                this.putOnRelease(inputMap, 18, 0);
                label.requestFocus();
            }
        }

        private void doRelease(JLabel label, boolean isCommand) {
            Component labelFor = label.getLabelFor();
            if (labelFor != null && labelFor.isEnabled()) {
                if (label.hasFocus()) {
                    int dka;
                    InputMap inputMap = SwingUtilities.getUIInputMap(label, 0);
                    if (inputMap != null) {
                        dka = label.getDisplayedMnemonic();
                        this.removeOnRelease(inputMap, dka, BasicLookAndFeel.getFocusAcceleratorKeyMask());
                        this.removeOnRelease(inputMap, dka, SwingUtilities2.setAltGraphMask(BasicLookAndFeel.getFocusAcceleratorKeyMask()));
                        this.removeOnRelease(inputMap, dka, 0);
                        this.removeOnRelease(inputMap, 18, 0);
                    }
                    if ((inputMap = SwingUtilities.getUIInputMap(label, 2)) == null) {
                        inputMap = new InputMapUIResource();
                        SwingUtilities.replaceUIInputMap(label, 2, inputMap);
                    }
                    dka = label.getDisplayedMnemonic();
                    if (isCommand) {
                        this.putOnRelease(inputMap, 18, 0);
                    } else {
                        this.putOnRelease(inputMap, dka, BasicLookAndFeel.getFocusAcceleratorKeyMask());
                        this.putOnRelease(inputMap, dka, SwingUtilities2.setAltGraphMask(BasicLookAndFeel.getFocusAcceleratorKeyMask()));
                        this.putOnRelease(inputMap, dka, 0);
                    }
                    if (labelFor instanceof Container && ((Container)labelFor).isFocusCycleRoot()) {
                        labelFor.requestFocus();
                    } else {
                        SwingUtilities2.compositeRequestFocus(labelFor);
                    }
                } else {
                    InputMap inputMap = SwingUtilities.getUIInputMap(label, 2);
                    int dka = label.getDisplayedMnemonic();
                    if (inputMap != null) {
                        if (isCommand) {
                            this.removeOnRelease(inputMap, dka, BasicLookAndFeel.getFocusAcceleratorKeyMask());
                            this.removeOnRelease(inputMap, dka, SwingUtilities2.setAltGraphMask(BasicLookAndFeel.getFocusAcceleratorKeyMask()));
                            this.removeOnRelease(inputMap, dka, 0);
                        } else {
                            this.removeOnRelease(inputMap, 18, 0);
                        }
                    }
                }
            }
        }

        private void putOnRelease(InputMap inputMap, int keyCode, int modifiers) {
            inputMap.put(KeyStroke.getKeyStroke(keyCode, modifiers, true), RELEASE);
        }

        private void removeOnRelease(InputMap inputMap, int keyCode, int modifiers) {
            inputMap.remove(KeyStroke.getKeyStroke(keyCode, modifiers, true));
        }
    }
}

