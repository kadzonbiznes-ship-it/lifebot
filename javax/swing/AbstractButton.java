/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.ItemSelectable;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Rectangle2D;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.Transient;
import java.io.Serializable;
import java.text.BreakIterator;
import java.util.Enumeration;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleAction;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleExtendedComponent;
import javax.accessibility.AccessibleIcon;
import javax.accessibility.AccessibleKeyBinding;
import javax.accessibility.AccessibleRelation;
import javax.accessibility.AccessibleRelationSet;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleText;
import javax.accessibility.AccessibleValue;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionPropertyChangeListener;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.OverlayLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.UIResource;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;

@JavaBean(defaultProperty="UI")
public abstract class AbstractButton
extends JComponent
implements ItemSelectable,
SwingConstants {
    public static final String MODEL_CHANGED_PROPERTY = "model";
    public static final String TEXT_CHANGED_PROPERTY = "text";
    public static final String MNEMONIC_CHANGED_PROPERTY = "mnemonic";
    public static final String MARGIN_CHANGED_PROPERTY = "margin";
    public static final String VERTICAL_ALIGNMENT_CHANGED_PROPERTY = "verticalAlignment";
    public static final String HORIZONTAL_ALIGNMENT_CHANGED_PROPERTY = "horizontalAlignment";
    public static final String VERTICAL_TEXT_POSITION_CHANGED_PROPERTY = "verticalTextPosition";
    public static final String HORIZONTAL_TEXT_POSITION_CHANGED_PROPERTY = "horizontalTextPosition";
    public static final String BORDER_PAINTED_CHANGED_PROPERTY = "borderPainted";
    public static final String FOCUS_PAINTED_CHANGED_PROPERTY = "focusPainted";
    public static final String ROLLOVER_ENABLED_CHANGED_PROPERTY = "rolloverEnabled";
    public static final String CONTENT_AREA_FILLED_CHANGED_PROPERTY = "contentAreaFilled";
    public static final String ICON_CHANGED_PROPERTY = "icon";
    public static final String PRESSED_ICON_CHANGED_PROPERTY = "pressedIcon";
    public static final String SELECTED_ICON_CHANGED_PROPERTY = "selectedIcon";
    public static final String ROLLOVER_ICON_CHANGED_PROPERTY = "rolloverIcon";
    public static final String ROLLOVER_SELECTED_ICON_CHANGED_PROPERTY = "rolloverSelectedIcon";
    public static final String DISABLED_ICON_CHANGED_PROPERTY = "disabledIcon";
    public static final String DISABLED_SELECTED_ICON_CHANGED_PROPERTY = "disabledSelectedIcon";
    protected ButtonModel model = null;
    private String text = "";
    private Insets margin = null;
    private Insets defaultMargin = null;
    private Icon defaultIcon = null;
    private Icon pressedIcon = null;
    private Icon disabledIcon = null;
    private Icon selectedIcon = null;
    private Icon disabledSelectedIcon = null;
    private Icon rolloverIcon = null;
    private Icon rolloverSelectedIcon = null;
    private boolean paintBorder = true;
    private boolean paintFocus = true;
    private boolean rolloverEnabled = false;
    private boolean contentAreaFilled = true;
    private int verticalAlignment = 0;
    private int horizontalAlignment = 0;
    private int verticalTextPosition = 0;
    private int horizontalTextPosition = 11;
    private int iconTextGap = 4;
    private int mnemonic;
    private int mnemonicIndex = -1;
    private long multiClickThreshhold = 0L;
    private boolean borderPaintedSet = false;
    private boolean rolloverEnabledSet = false;
    private boolean iconTextGapSet = false;
    private boolean contentAreaFilledSet = false;
    private boolean setLayout = false;
    boolean defaultCapable = true;
    private Handler handler;
    protected ChangeListener changeListener = null;
    protected ActionListener actionListener = null;
    protected ItemListener itemListener = null;
    protected transient ChangeEvent changeEvent;
    private boolean hideActionText = false;
    private Action action;
    private PropertyChangeListener actionPropertyChangeListener;

    protected AbstractButton() {
    }

    @BeanProperty(expert=true, description="Whether the text of the button should come from the <code>Action</code>.")
    public void setHideActionText(boolean hideActionText) {
        if (hideActionText != this.hideActionText) {
            this.hideActionText = hideActionText;
            if (this.getAction() != null) {
                this.setTextFromAction(this.getAction(), false);
            }
            this.firePropertyChange("hideActionText", !hideActionText, hideActionText);
        }
    }

    public boolean getHideActionText() {
        return this.hideActionText;
    }

    public String getText() {
        return this.text;
    }

    @BeanProperty(preferred=true, visualUpdate=true, description="The button's text.")
    public void setText(String text) {
        String oldValue = this.text;
        this.text = text;
        this.firePropertyChange(TEXT_CHANGED_PROPERTY, oldValue, text);
        this.updateDisplayedMnemonicIndex(text, this.getMnemonic());
        if (this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleVisibleData", oldValue, text);
        }
        if (text == null || oldValue == null || !text.equals(oldValue)) {
            this.revalidate();
            this.repaint();
        }
    }

    public boolean isSelected() {
        return this.model.isSelected();
    }

    public void setSelected(boolean b) {
        boolean oldValue = this.isSelected();
        this.model.setSelected(b);
    }

    public void doClick() {
        this.doClick(68);
    }

    public void doClick(int pressTime) {
        Dimension size = this.getSize();
        this.model.setArmed(true);
        this.model.setPressed(true);
        this.paintImmediately(new Rectangle(0, 0, size.width, size.height));
        try {
            Thread.sleep(pressTime);
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
        this.model.setPressed(false);
        this.model.setArmed(false);
    }

    @BeanProperty(visualUpdate=true, description="The space between the button's border and the label.")
    public void setMargin(Insets m) {
        if (m instanceof UIResource) {
            this.defaultMargin = m;
        } else if (this.margin instanceof UIResource) {
            this.defaultMargin = this.margin;
        }
        if (m == null && this.defaultMargin != null) {
            m = this.defaultMargin;
        }
        Insets old = this.margin;
        this.margin = m;
        this.firePropertyChange(MARGIN_CHANGED_PROPERTY, old, m);
        if (old == null || !old.equals(m)) {
            this.revalidate();
            this.repaint();
        }
    }

    public Insets getMargin() {
        return this.margin == null ? null : (Insets)this.margin.clone();
    }

    public Icon getIcon() {
        return this.defaultIcon;
    }

    @BeanProperty(visualUpdate=true, description="The button's default icon")
    public void setIcon(Icon defaultIcon) {
        Icon oldValue = this.defaultIcon;
        this.defaultIcon = defaultIcon;
        if (defaultIcon != oldValue && this.disabledIcon instanceof UIResource) {
            this.disabledIcon = null;
        }
        this.firePropertyChange(ICON_CHANGED_PROPERTY, oldValue, defaultIcon);
        if (this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleVisibleData", oldValue, defaultIcon);
        }
        if (defaultIcon != oldValue) {
            if (defaultIcon == null || oldValue == null || defaultIcon.getIconWidth() != oldValue.getIconWidth() || defaultIcon.getIconHeight() != oldValue.getIconHeight()) {
                this.revalidate();
            }
            this.repaint();
        }
    }

    public Icon getPressedIcon() {
        return this.pressedIcon;
    }

    @BeanProperty(visualUpdate=true, description="The pressed icon for the button.")
    public void setPressedIcon(Icon pressedIcon) {
        Icon oldValue = this.pressedIcon;
        this.pressedIcon = pressedIcon;
        this.firePropertyChange(PRESSED_ICON_CHANGED_PROPERTY, oldValue, pressedIcon);
        if (this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleVisibleData", oldValue, pressedIcon);
        }
        if (pressedIcon != oldValue && this.getModel().isPressed()) {
            this.repaint();
        }
    }

    public Icon getSelectedIcon() {
        return this.selectedIcon;
    }

    @BeanProperty(visualUpdate=true, description="The selected icon for the button.")
    public void setSelectedIcon(Icon selectedIcon) {
        Icon oldValue = this.selectedIcon;
        this.selectedIcon = selectedIcon;
        if (selectedIcon != oldValue && this.disabledSelectedIcon instanceof UIResource) {
            this.disabledSelectedIcon = null;
        }
        this.firePropertyChange(SELECTED_ICON_CHANGED_PROPERTY, oldValue, selectedIcon);
        if (this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleVisibleData", oldValue, selectedIcon);
        }
        if (selectedIcon != oldValue && this.isSelected()) {
            this.repaint();
        }
    }

    public Icon getRolloverIcon() {
        return this.rolloverIcon;
    }

    @BeanProperty(visualUpdate=true, description="The rollover icon for the button.")
    public void setRolloverIcon(Icon rolloverIcon) {
        Icon oldValue = this.rolloverIcon;
        this.rolloverIcon = rolloverIcon;
        this.firePropertyChange(ROLLOVER_ICON_CHANGED_PROPERTY, oldValue, rolloverIcon);
        if (this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleVisibleData", oldValue, rolloverIcon);
        }
        this.setRolloverEnabled(true);
        if (rolloverIcon != oldValue) {
            this.repaint();
        }
    }

    public Icon getRolloverSelectedIcon() {
        return this.rolloverSelectedIcon;
    }

    @BeanProperty(visualUpdate=true, description="The rollover selected icon for the button.")
    public void setRolloverSelectedIcon(Icon rolloverSelectedIcon) {
        Icon oldValue = this.rolloverSelectedIcon;
        this.rolloverSelectedIcon = rolloverSelectedIcon;
        this.firePropertyChange(ROLLOVER_SELECTED_ICON_CHANGED_PROPERTY, oldValue, rolloverSelectedIcon);
        if (this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleVisibleData", oldValue, rolloverSelectedIcon);
        }
        this.setRolloverEnabled(true);
        if (rolloverSelectedIcon != oldValue && this.isSelected()) {
            this.repaint();
        }
    }

    @Transient
    public Icon getDisabledIcon() {
        if (this.disabledIcon == null) {
            this.disabledIcon = UIManager.getLookAndFeel().getDisabledIcon(this, this.getIcon());
            if (this.disabledIcon != null) {
                this.firePropertyChange(DISABLED_ICON_CHANGED_PROPERTY, null, this.disabledIcon);
            }
        }
        return this.disabledIcon;
    }

    @BeanProperty(visualUpdate=true, description="The disabled icon for the button.")
    public void setDisabledIcon(Icon disabledIcon) {
        Icon oldValue = this.disabledIcon;
        this.disabledIcon = disabledIcon;
        this.firePropertyChange(DISABLED_ICON_CHANGED_PROPERTY, oldValue, disabledIcon);
        if (this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleVisibleData", oldValue, disabledIcon);
        }
        if (disabledIcon != oldValue && !this.isEnabled()) {
            this.repaint();
        }
    }

    public Icon getDisabledSelectedIcon() {
        if (this.disabledSelectedIcon == null) {
            if (this.selectedIcon != null) {
                this.disabledSelectedIcon = UIManager.getLookAndFeel().getDisabledSelectedIcon(this, this.getSelectedIcon());
            } else {
                return this.getDisabledIcon();
            }
        }
        return this.disabledSelectedIcon;
    }

    @BeanProperty(visualUpdate=true, description="The disabled selection icon for the button.")
    public void setDisabledSelectedIcon(Icon disabledSelectedIcon) {
        Icon oldValue = this.disabledSelectedIcon;
        this.disabledSelectedIcon = disabledSelectedIcon;
        this.firePropertyChange(DISABLED_SELECTED_ICON_CHANGED_PROPERTY, oldValue, disabledSelectedIcon);
        if (this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleVisibleData", oldValue, disabledSelectedIcon);
        }
        if (disabledSelectedIcon != oldValue) {
            if (disabledSelectedIcon == null || oldValue == null || disabledSelectedIcon.getIconWidth() != oldValue.getIconWidth() || disabledSelectedIcon.getIconHeight() != oldValue.getIconHeight()) {
                this.revalidate();
            }
            if (!this.isEnabled() && this.isSelected()) {
                this.repaint();
            }
        }
    }

    public int getVerticalAlignment() {
        return this.verticalAlignment;
    }

    @BeanProperty(visualUpdate=true, enumerationValues={"SwingConstants.TOP", "SwingConstants.CENTER", "SwingConstants.BOTTOM"}, description="The vertical alignment of the icon and text.")
    public void setVerticalAlignment(int alignment) {
        if (alignment == this.verticalAlignment) {
            return;
        }
        int oldValue = this.verticalAlignment;
        this.verticalAlignment = this.checkVerticalKey(alignment, VERTICAL_ALIGNMENT_CHANGED_PROPERTY);
        this.firePropertyChange(VERTICAL_ALIGNMENT_CHANGED_PROPERTY, oldValue, this.verticalAlignment);
        this.repaint();
    }

    public int getHorizontalAlignment() {
        return this.horizontalAlignment;
    }

    @BeanProperty(visualUpdate=true, enumerationValues={"SwingConstants.LEFT", "SwingConstants.CENTER", "SwingConstants.RIGHT", "SwingConstants.LEADING", "SwingConstants.TRAILING"}, description="The horizontal alignment of the icon and text.")
    public void setHorizontalAlignment(int alignment) {
        if (alignment == this.horizontalAlignment) {
            return;
        }
        int oldValue = this.horizontalAlignment;
        this.horizontalAlignment = this.checkHorizontalKey(alignment, HORIZONTAL_ALIGNMENT_CHANGED_PROPERTY);
        this.firePropertyChange(HORIZONTAL_ALIGNMENT_CHANGED_PROPERTY, oldValue, this.horizontalAlignment);
        this.repaint();
    }

    public int getVerticalTextPosition() {
        return this.verticalTextPosition;
    }

    @BeanProperty(visualUpdate=true, enumerationValues={"SwingConstants.TOP", "SwingConstants.CENTER", "SwingConstants.BOTTOM"}, description="The vertical position of the text relative to the icon.")
    public void setVerticalTextPosition(int textPosition) {
        if (textPosition == this.verticalTextPosition) {
            return;
        }
        int oldValue = this.verticalTextPosition;
        this.verticalTextPosition = this.checkVerticalKey(textPosition, VERTICAL_TEXT_POSITION_CHANGED_PROPERTY);
        this.firePropertyChange(VERTICAL_TEXT_POSITION_CHANGED_PROPERTY, oldValue, this.verticalTextPosition);
        this.revalidate();
        this.repaint();
    }

    public int getHorizontalTextPosition() {
        return this.horizontalTextPosition;
    }

    @BeanProperty(visualUpdate=true, enumerationValues={"SwingConstants.LEFT", "SwingConstants.CENTER", "SwingConstants.RIGHT", "SwingConstants.LEADING", "SwingConstants.TRAILING"}, description="The horizontal position of the text relative to the icon.")
    public void setHorizontalTextPosition(int textPosition) {
        if (textPosition == this.horizontalTextPosition) {
            return;
        }
        int oldValue = this.horizontalTextPosition;
        this.horizontalTextPosition = this.checkHorizontalKey(textPosition, HORIZONTAL_TEXT_POSITION_CHANGED_PROPERTY);
        this.firePropertyChange(HORIZONTAL_TEXT_POSITION_CHANGED_PROPERTY, oldValue, this.horizontalTextPosition);
        this.revalidate();
        this.repaint();
    }

    public int getIconTextGap() {
        return this.iconTextGap;
    }

    @BeanProperty(visualUpdate=true, description="If both the icon and text properties are set, this property defines the space between them.")
    public void setIconTextGap(int iconTextGap) {
        int oldValue = this.iconTextGap;
        this.iconTextGap = iconTextGap;
        this.iconTextGapSet = true;
        this.firePropertyChange("iconTextGap", oldValue, iconTextGap);
        if (iconTextGap != oldValue) {
            this.revalidate();
            this.repaint();
        }
    }

    protected int checkHorizontalKey(int key, String exception) {
        if (key == 2 || key == 0 || key == 4 || key == 10 || key == 11) {
            return key;
        }
        throw new IllegalArgumentException(exception);
    }

    protected int checkVerticalKey(int key, String exception) {
        if (key == 1 || key == 0 || key == 3) {
            return key;
        }
        throw new IllegalArgumentException(exception);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (this.isRolloverEnabled()) {
            this.getModel().setRollover(false);
        }
    }

    public void setActionCommand(String actionCommand) {
        this.getModel().setActionCommand(actionCommand);
    }

    public String getActionCommand() {
        String ac = this.getModel().getActionCommand();
        if (ac == null) {
            ac = this.getText();
        }
        return ac;
    }

    @BeanProperty(visualUpdate=true, description="the Action instance connected with this ActionEvent source")
    public void setAction(Action a) {
        Action oldValue = this.getAction();
        if (this.action == null || !this.action.equals(a)) {
            this.action = a;
            if (oldValue != null) {
                this.removeActionListener(oldValue);
                oldValue.removePropertyChangeListener(this.actionPropertyChangeListener);
                this.actionPropertyChangeListener = null;
            }
            this.configurePropertiesFromAction(this.action);
            if (this.action != null) {
                if (!this.isListener(ActionListener.class, this.action)) {
                    this.addActionListener(this.action);
                }
                this.actionPropertyChangeListener = this.createActionPropertyChangeListener(this.action);
                this.action.addPropertyChangeListener(this.actionPropertyChangeListener);
            }
            this.firePropertyChange("action", oldValue, this.action);
        }
    }

    private boolean isListener(Class<?> c, ActionListener a) {
        boolean isListener = false;
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != c || listeners[i + 1] != a) continue;
            isListener = true;
        }
        return isListener;
    }

    public Action getAction() {
        return this.action;
    }

    protected void configurePropertiesFromAction(Action a) {
        this.setMnemonicFromAction(a);
        this.setTextFromAction(a, false);
        AbstractAction.setToolTipTextFromAction(this, a);
        this.setIconFromAction(a);
        this.setActionCommandFromAction(a);
        AbstractAction.setEnabledFromAction(this, a);
        if (AbstractAction.hasSelectedKey(a) && this.shouldUpdateSelectedStateFromAction()) {
            this.setSelectedFromAction(a);
        }
        this.setDisplayedMnemonicIndexFromAction(a, false);
    }

    @Override
    void clientPropertyChanged(Object key, Object oldValue, Object newValue) {
        if (key == "hideActionText") {
            boolean current;
            boolean bl = current = newValue instanceof Boolean ? (Boolean)newValue : false;
            if (this.getHideActionText() != current) {
                this.setHideActionText(current);
            }
        }
    }

    boolean shouldUpdateSelectedStateFromAction() {
        return false;
    }

    protected void actionPropertyChanged(Action action, String propertyName) {
        if (propertyName == "Name") {
            this.setTextFromAction(action, true);
        } else if (propertyName == "enabled") {
            AbstractAction.setEnabledFromAction(this, action);
        } else if (propertyName == "ShortDescription") {
            AbstractAction.setToolTipTextFromAction(this, action);
        } else if (propertyName == "SmallIcon") {
            this.smallIconChanged(action);
        } else if (propertyName == "MnemonicKey") {
            this.setMnemonicFromAction(action);
        } else if (propertyName == "ActionCommandKey") {
            this.setActionCommandFromAction(action);
        } else if (propertyName == "SwingSelectedKey" && AbstractAction.hasSelectedKey(action) && this.shouldUpdateSelectedStateFromAction()) {
            this.setSelectedFromAction(action);
        } else if (propertyName == "SwingDisplayedMnemonicIndexKey") {
            this.setDisplayedMnemonicIndexFromAction(action, true);
        } else if (propertyName == "SwingLargeIconKey") {
            this.largeIconChanged(action);
        }
    }

    private void setDisplayedMnemonicIndexFromAction(Action a, boolean fromPropertyChange) {
        Integer iValue;
        Integer n = iValue = a == null ? null : (Integer)a.getValue("SwingDisplayedMnemonicIndexKey");
        if (fromPropertyChange || iValue != null) {
            int value;
            if (iValue == null) {
                value = -1;
            } else {
                value = iValue;
                String text = this.getText();
                if (text == null || value >= text.length()) {
                    value = -1;
                }
            }
            this.setDisplayedMnemonicIndex(value);
        }
    }

    private void setMnemonicFromAction(Action a) {
        Integer n = a == null ? null : (Integer)a.getValue("MnemonicKey");
        this.setMnemonic(n == null ? 0 : n);
    }

    private void setTextFromAction(Action a, boolean propertyChange) {
        boolean hideText = this.getHideActionText();
        if (!propertyChange) {
            this.setText(a != null && !hideText ? (String)a.getValue("Name") : null);
        } else if (!hideText) {
            this.setText((String)a.getValue("Name"));
        }
    }

    void setIconFromAction(Action a) {
        Icon icon = null;
        if (a != null && (icon = (Icon)a.getValue("SwingLargeIconKey")) == null) {
            icon = (Icon)a.getValue("SmallIcon");
        }
        this.setIcon(icon);
    }

    void smallIconChanged(Action a) {
        if (a.getValue("SwingLargeIconKey") == null) {
            this.setIconFromAction(a);
        }
    }

    void largeIconChanged(Action a) {
        this.setIconFromAction(a);
    }

    private void setActionCommandFromAction(Action a) {
        this.setActionCommand(a != null ? (String)a.getValue("ActionCommandKey") : null);
    }

    private void setSelectedFromAction(Action a) {
        boolean selected = false;
        if (a != null) {
            selected = AbstractAction.isSelected(a);
        }
        if (selected != this.isSelected()) {
            ButtonGroup group;
            this.setSelected(selected);
            if (!selected && this.isSelected() && this.getModel() instanceof DefaultButtonModel && (group = ((DefaultButtonModel)this.getModel()).getGroup()) != null) {
                group.clearSelection();
            }
        }
    }

    protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
        return this.createActionPropertyChangeListener0(a);
    }

    PropertyChangeListener createActionPropertyChangeListener0(Action a) {
        return new ButtonActionPropertyChangeListener(this, a);
    }

    public boolean isBorderPainted() {
        return this.paintBorder;
    }

    @BeanProperty(visualUpdate=true, description="Whether the border should be painted.")
    public void setBorderPainted(boolean b) {
        boolean oldValue = this.paintBorder;
        this.paintBorder = b;
        this.borderPaintedSet = true;
        this.firePropertyChange(BORDER_PAINTED_CHANGED_PROPERTY, oldValue, this.paintBorder);
        if (b != oldValue) {
            this.revalidate();
            this.repaint();
        }
    }

    @Override
    protected void paintBorder(Graphics g) {
        if (this.isBorderPainted()) {
            super.paintBorder(g);
        }
    }

    public boolean isFocusPainted() {
        return this.paintFocus;
    }

    @BeanProperty(visualUpdate=true, description="Whether focus should be painted")
    public void setFocusPainted(boolean b) {
        boolean oldValue = this.paintFocus;
        this.paintFocus = b;
        this.firePropertyChange(FOCUS_PAINTED_CHANGED_PROPERTY, oldValue, this.paintFocus);
        if (b != oldValue && this.isFocusOwner()) {
            this.revalidate();
            this.repaint();
        }
    }

    public boolean isContentAreaFilled() {
        return this.contentAreaFilled;
    }

    @BeanProperty(visualUpdate=true, description="Whether the button should paint the content area or leave it transparent.")
    public void setContentAreaFilled(boolean b) {
        boolean oldValue = this.contentAreaFilled;
        this.contentAreaFilled = b;
        this.contentAreaFilledSet = true;
        this.firePropertyChange(CONTENT_AREA_FILLED_CHANGED_PROPERTY, oldValue, this.contentAreaFilled);
        if (b != oldValue) {
            this.repaint();
        }
    }

    public boolean isRolloverEnabled() {
        return this.rolloverEnabled;
    }

    @BeanProperty(visualUpdate=true, description="Whether rollover effects should be enabled.")
    public void setRolloverEnabled(boolean b) {
        boolean oldValue = this.rolloverEnabled;
        this.rolloverEnabled = b;
        this.rolloverEnabledSet = true;
        this.firePropertyChange(ROLLOVER_ENABLED_CHANGED_PROPERTY, oldValue, this.rolloverEnabled);
        if (b != oldValue) {
            this.repaint();
        }
    }

    public int getMnemonic() {
        return this.mnemonic;
    }

    @BeanProperty(visualUpdate=true, description="the keyboard character mnemonic")
    public void setMnemonic(int mnemonic) {
        int oldValue = this.getMnemonic();
        this.model.setMnemonic(mnemonic);
        this.updateMnemonicProperties();
    }

    @BeanProperty(visualUpdate=true, description="the keyboard character mnemonic")
    public void setMnemonic(char mnemonic) {
        int vk = mnemonic;
        if (vk >= 97 && vk <= 122) {
            vk -= 32;
        }
        this.setMnemonic(vk);
    }

    @BeanProperty(visualUpdate=true, description="the index into the String to draw the keyboard character mnemonic at")
    public void setDisplayedMnemonicIndex(int index) throws IllegalArgumentException {
        int oldValue = this.mnemonicIndex;
        if (index == -1) {
            this.mnemonicIndex = -1;
        } else {
            int textLength;
            String text = this.getText();
            int n = textLength = text == null ? 0 : text.length();
            if (index < -1 || index >= textLength) {
                throw new IllegalArgumentException("index == " + index);
            }
        }
        this.mnemonicIndex = index;
        this.firePropertyChange("displayedMnemonicIndex", oldValue, index);
        if (index != oldValue) {
            this.revalidate();
            this.repaint();
        }
    }

    public int getDisplayedMnemonicIndex() {
        return this.mnemonicIndex;
    }

    private void updateDisplayedMnemonicIndex(String text, int mnemonic) {
        this.setDisplayedMnemonicIndex(SwingUtilities.findDisplayedMnemonicIndex(text, mnemonic));
    }

    private void updateMnemonicProperties() {
        int newMnemonic = this.model.getMnemonic();
        if (this.mnemonic != newMnemonic) {
            int oldValue = this.mnemonic;
            this.mnemonic = newMnemonic;
            this.firePropertyChange(MNEMONIC_CHANGED_PROPERTY, oldValue, this.mnemonic);
            this.updateDisplayedMnemonicIndex(this.getText(), this.mnemonic);
            this.revalidate();
            this.repaint();
        }
    }

    public void setMultiClickThreshhold(long threshold) {
        if (threshold < 0L) {
            throw new IllegalArgumentException("threshold must be >= 0");
        }
        this.multiClickThreshhold = threshold;
    }

    public long getMultiClickThreshhold() {
        return this.multiClickThreshhold;
    }

    public ButtonModel getModel() {
        return this.model;
    }

    @BeanProperty(description="Model that the Button uses.")
    public void setModel(ButtonModel newModel) {
        ButtonModel oldModel = this.getModel();
        if (oldModel != null) {
            oldModel.removeChangeListener(this.changeListener);
            oldModel.removeActionListener(this.actionListener);
            oldModel.removeItemListener(this.itemListener);
            this.changeListener = null;
            this.actionListener = null;
            this.itemListener = null;
        }
        this.model = newModel;
        if (newModel != null) {
            this.changeListener = this.createChangeListener();
            this.actionListener = this.createActionListener();
            this.itemListener = this.createItemListener();
            newModel.addChangeListener(this.changeListener);
            newModel.addActionListener(this.actionListener);
            newModel.addItemListener(this.itemListener);
            this.updateMnemonicProperties();
            super.setEnabled(newModel.isEnabled());
        } else {
            this.mnemonic = 0;
        }
        this.updateDisplayedMnemonicIndex(this.getText(), this.mnemonic);
        this.firePropertyChange(MODEL_CHANGED_PROPERTY, oldModel, newModel);
        if (newModel != oldModel) {
            this.revalidate();
            this.repaint();
        }
    }

    @Override
    public ButtonUI getUI() {
        return (ButtonUI)this.ui;
    }

    @BeanProperty(hidden=true, visualUpdate=true, description="The UI object that implements the LookAndFeel.")
    public void setUI(ButtonUI ui) {
        super.setUI(ui);
        if (this.disabledIcon instanceof UIResource) {
            this.setDisabledIcon(null);
        }
        if (this.disabledSelectedIcon instanceof UIResource) {
            this.setDisabledSelectedIcon(null);
        }
    }

    @Override
    public void updateUI() {
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        if (!this.setLayout) {
            this.setLayout(new OverlayLayout(this));
        }
        super.addImpl(comp, constraints, index);
    }

    @Override
    public void setLayout(LayoutManager mgr) {
        this.setLayout = true;
        super.setLayout(mgr);
    }

    public void addChangeListener(ChangeListener l) {
        this.listenerList.add(ChangeListener.class, l);
    }

    public void removeChangeListener(ChangeListener l) {
        this.listenerList.remove(ChangeListener.class, l);
    }

    @BeanProperty(bound=false)
    public ChangeListener[] getChangeListeners() {
        return (ChangeListener[])this.listenerList.getListeners(ChangeListener.class);
    }

    protected void fireStateChanged() {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ChangeListener.class) continue;
            if (this.changeEvent == null) {
                this.changeEvent = new ChangeEvent(this);
            }
            ((ChangeListener)listeners[i + 1]).stateChanged(this.changeEvent);
        }
    }

    public void addActionListener(ActionListener l) {
        this.listenerList.add(ActionListener.class, l);
    }

    public void removeActionListener(ActionListener l) {
        if (l != null && this.getAction() == l) {
            this.setAction(null);
        } else {
            this.listenerList.remove(ActionListener.class, l);
        }
    }

    @BeanProperty(bound=false)
    public ActionListener[] getActionListeners() {
        return (ActionListener[])this.listenerList.getListeners(ActionListener.class);
    }

    protected ChangeListener createChangeListener() {
        return this.getHandler();
    }

    protected void fireActionPerformed(ActionEvent event) {
        Object[] listeners = this.listenerList.getListenerList();
        ActionEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ActionListener.class) continue;
            if (e == null) {
                String actionCommand = event.getActionCommand();
                if (actionCommand == null) {
                    actionCommand = this.getActionCommand();
                }
                e = new ActionEvent(this, 1001, actionCommand, event.getWhen(), event.getModifiers());
            }
            ((ActionListener)listeners[i + 1]).actionPerformed(e);
        }
    }

    protected void fireItemStateChanged(ItemEvent event) {
        Object[] listeners = this.listenerList.getListenerList();
        ItemEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ItemListener.class) continue;
            if (e == null) {
                e = new ItemEvent(this, 701, this, event.getStateChange());
            }
            ((ItemListener)listeners[i + 1]).itemStateChanged(e);
        }
        if (this.accessibleContext != null) {
            if (event.getStateChange() == 1) {
                this.accessibleContext.firePropertyChange("AccessibleState", null, AccessibleState.SELECTED);
                this.accessibleContext.firePropertyChange("AccessibleValue", 0, 1);
            } else {
                this.accessibleContext.firePropertyChange("AccessibleState", AccessibleState.SELECTED, null);
                this.accessibleContext.firePropertyChange("AccessibleValue", 1, 0);
            }
        }
    }

    protected ActionListener createActionListener() {
        return this.getHandler();
    }

    protected ItemListener createItemListener() {
        return this.getHandler();
    }

    @Override
    public void setEnabled(boolean b) {
        if (!b && this.model.isRollover()) {
            this.model.setRollover(false);
        }
        super.setEnabled(b);
        this.model.setEnabled(b);
    }

    @Deprecated
    public String getLabel() {
        return this.getText();
    }

    @Deprecated
    @BeanProperty(description="Replace by setText(text)")
    public void setLabel(String label) {
        this.setText(label);
    }

    @Override
    public void addItemListener(ItemListener l) {
        this.listenerList.add(ItemListener.class, l);
    }

    @Override
    public void removeItemListener(ItemListener l) {
        this.listenerList.remove(ItemListener.class, l);
    }

    @BeanProperty(bound=false)
    public ItemListener[] getItemListeners() {
        return (ItemListener[])this.listenerList.getListeners(ItemListener.class);
    }

    @Override
    @BeanProperty(bound=false)
    public Object[] getSelectedObjects() {
        if (!this.isSelected()) {
            return null;
        }
        Object[] selectedObjects = new Object[]{this.getText()};
        return selectedObjects;
    }

    protected void init(String text, Icon icon) {
        if (text != null) {
            this.setText(text);
        }
        if (icon != null) {
            this.setIcon(icon);
        }
        this.updateUI();
        this.setAlignmentX(0.0f);
        this.setAlignmentY(0.5f);
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
        Icon iconDisplayed = null;
        if (!this.model.isEnabled()) {
            iconDisplayed = this.model.isSelected() ? this.getDisabledSelectedIcon() : this.getDisabledIcon();
        } else if (this.model.isPressed() && this.model.isArmed()) {
            iconDisplayed = this.getPressedIcon();
        } else if (this.isRolloverEnabled() && this.model.isRollover()) {
            iconDisplayed = this.model.isSelected() ? this.getRolloverSelectedIcon() : this.getRolloverIcon();
        } else if (this.model.isSelected()) {
            iconDisplayed = this.getSelectedIcon();
        }
        if (iconDisplayed == null) {
            iconDisplayed = this.getIcon();
        }
        if (iconDisplayed == null || !SwingUtilities.doesIconReferenceImage(iconDisplayed, img)) {
            return false;
        }
        return super.imageUpdate(img, infoflags, x, y, w, h);
    }

    @Override
    void setUIProperty(String propertyName, Object value) {
        if (propertyName == BORDER_PAINTED_CHANGED_PROPERTY) {
            if (!this.borderPaintedSet) {
                this.setBorderPainted((Boolean)value);
                this.borderPaintedSet = false;
            }
        } else if (propertyName == ROLLOVER_ENABLED_CHANGED_PROPERTY) {
            if (!this.rolloverEnabledSet) {
                this.setRolloverEnabled((Boolean)value);
                this.rolloverEnabledSet = false;
            }
        } else if (propertyName == "iconTextGap") {
            if (!this.iconTextGapSet) {
                this.setIconTextGap(((Number)value).intValue());
                this.iconTextGapSet = false;
            }
        } else if (propertyName == CONTENT_AREA_FILLED_CHANGED_PROPERTY) {
            if (!this.contentAreaFilledSet) {
                this.setContentAreaFilled((Boolean)value);
                this.contentAreaFilledSet = false;
            }
        } else {
            super.setUIProperty(propertyName, value);
        }
    }

    @Override
    protected String paramString() {
        String defaultIconString = this.defaultIcon != null && this.defaultIcon != this ? this.defaultIcon.toString() : "";
        String pressedIconString = this.pressedIcon != null && this.pressedIcon != this ? this.pressedIcon.toString() : "";
        String disabledIconString = this.disabledIcon != null && this.disabledIcon != this ? this.disabledIcon.toString() : "";
        String selectedIconString = this.selectedIcon != null && this.selectedIcon != this ? this.selectedIcon.toString() : "";
        String disabledSelectedIconString = this.disabledSelectedIcon != null && this.disabledSelectedIcon != this ? this.disabledSelectedIcon.toString() : "";
        String rolloverIconString = this.rolloverIcon != null && this.rolloverIcon != this ? this.rolloverIcon.toString() : "";
        String rolloverSelectedIconString = this.rolloverSelectedIcon != null && this.rolloverSelectedIcon != this ? this.rolloverSelectedIcon.toString() : "";
        String paintBorderString = this.paintBorder ? "true" : "false";
        String paintFocusString = this.paintFocus ? "true" : "false";
        String rolloverEnabledString = this.rolloverEnabled ? "true" : "false";
        return super.paramString() + ",defaultIcon=" + defaultIconString + ",disabledIcon=" + disabledIconString + ",disabledSelectedIcon=" + disabledSelectedIconString + ",margin=" + String.valueOf(this.margin) + ",paintBorder=" + paintBorderString + ",paintFocus=" + paintFocusString + ",pressedIcon=" + pressedIconString + ",rolloverEnabled=" + rolloverEnabledString + ",rolloverIcon=" + rolloverIconString + ",rolloverSelectedIcon=" + rolloverSelectedIconString + ",selectedIcon=" + selectedIconString + ",text=" + this.text;
    }

    private Handler getHandler() {
        if (this.handler == null) {
            this.handler = new Handler();
        }
        return this.handler;
    }

    private static class ButtonActionPropertyChangeListener
    extends ActionPropertyChangeListener<AbstractButton> {
        ButtonActionPropertyChangeListener(AbstractButton b, Action a) {
            super(b, a);
        }

        @Override
        protected void actionPropertyChanged(AbstractButton button, Action action, PropertyChangeEvent e) {
            if (AbstractAction.shouldReconfigure(e)) {
                button.configurePropertiesFromAction(action);
            } else {
                button.actionPropertyChanged(action, e.getPropertyName());
            }
        }
    }

    class Handler
    implements ActionListener,
    ChangeListener,
    ItemListener,
    Serializable {
        Handler() {
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            Object source = e.getSource();
            AbstractButton.this.updateMnemonicProperties();
            if (AbstractButton.this.isEnabled() != AbstractButton.this.model.isEnabled()) {
                AbstractButton.this.setEnabled(AbstractButton.this.model.isEnabled());
            }
            AbstractButton.this.fireStateChanged();
            AbstractButton.this.repaint();
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            AbstractButton.this.fireActionPerformed(event);
        }

        @Override
        public void itemStateChanged(ItemEvent event) {
            Action action;
            AbstractButton.this.fireItemStateChanged(event);
            if (AbstractButton.this.shouldUpdateSelectedStateFromAction() && (action = AbstractButton.this.getAction()) != null && AbstractAction.hasSelectedKey(action)) {
                boolean selected = AbstractButton.this.isSelected();
                boolean isActionSelected = AbstractAction.isSelected(action);
                if (isActionSelected != selected) {
                    action.putValue("SwingSelectedKey", selected);
                }
            }
        }
    }

    protected abstract class AccessibleAbstractButton
    extends JComponent.AccessibleJComponent
    implements AccessibleAction,
    AccessibleValue,
    AccessibleText,
    AccessibleExtendedComponent {
        protected AccessibleAbstractButton() {
            super(AbstractButton.this);
        }

        @Override
        public String getAccessibleName() {
            String name = this.accessibleName;
            if (name == null) {
                name = (String)AbstractButton.this.getClientProperty("AccessibleName");
            }
            if (name == null) {
                name = AbstractButton.this.getText();
            }
            if (name == null) {
                name = super.getAccessibleName();
            }
            return name;
        }

        @Override
        public AccessibleIcon[] getAccessibleIcon() {
            AccessibleContext ac;
            Icon defaultIcon = AbstractButton.this.getIcon();
            if (defaultIcon instanceof Accessible && (ac = ((Accessible)((Object)defaultIcon)).getAccessibleContext()) instanceof AccessibleIcon) {
                AccessibleIcon ai = (AccessibleIcon)((Object)ac);
                return new AccessibleIcon[]{ai};
            }
            return null;
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (AbstractButton.this.getModel().isArmed()) {
                states.add(AccessibleState.ARMED);
            }
            if (AbstractButton.this.isFocusOwner()) {
                states.add(AccessibleState.FOCUSED);
            }
            if (AbstractButton.this.getModel().isPressed()) {
                states.add(AccessibleState.PRESSED);
            }
            if (AbstractButton.this.isSelected()) {
                states.add(AccessibleState.CHECKED);
            }
            return states;
        }

        @Override
        public AccessibleRelationSet getAccessibleRelationSet() {
            DefaultButtonModel defaultModel;
            ButtonGroup group;
            ButtonModel model;
            AccessibleRelationSet relationSet = super.getAccessibleRelationSet();
            if (!relationSet.contains(AccessibleRelation.MEMBER_OF) && (model = AbstractButton.this.getModel()) instanceof DefaultButtonModel && (group = (defaultModel = (DefaultButtonModel)model).getGroup()) != null) {
                int len = group.getButtonCount();
                Object[] target = new Object[len];
                Enumeration<AbstractButton> elem = group.getElements();
                for (int i = 0; i < len; ++i) {
                    if (!elem.hasMoreElements()) continue;
                    target[i] = elem.nextElement();
                }
                AccessibleRelation relation = new AccessibleRelation(AccessibleRelation.MEMBER_OF);
                relation.setTarget(target);
                relationSet.add(relation);
            }
            return relationSet;
        }

        @Override
        public AccessibleAction getAccessibleAction() {
            return this;
        }

        @Override
        public AccessibleValue getAccessibleValue() {
            return this;
        }

        @Override
        public int getAccessibleActionCount() {
            return 1;
        }

        @Override
        public String getAccessibleActionDescription(int i) {
            if (i == 0) {
                return UIManager.getString("AbstractButton.clickText");
            }
            return null;
        }

        @Override
        public boolean doAccessibleAction(int i) {
            if (i == 0) {
                AbstractButton.this.doClick();
                return true;
            }
            return false;
        }

        @Override
        public Number getCurrentAccessibleValue() {
            if (AbstractButton.this.isSelected()) {
                return 1;
            }
            return 0;
        }

        @Override
        public boolean setCurrentAccessibleValue(Number n) {
            if (n == null) {
                return false;
            }
            int i = n.intValue();
            if (i == 0) {
                AbstractButton.this.setSelected(false);
            } else {
                AbstractButton.this.setSelected(true);
            }
            return true;
        }

        @Override
        public Number getMinimumAccessibleValue() {
            return 0;
        }

        @Override
        public Number getMaximumAccessibleValue() {
            return 1;
        }

        @Override
        public AccessibleText getAccessibleText() {
            View view = (View)AbstractButton.this.getClientProperty("html");
            if (view != null) {
                return this;
            }
            return null;
        }

        @Override
        public int getIndexAtPoint(Point p) {
            View view = (View)AbstractButton.this.getClientProperty("html");
            if (view != null) {
                Rectangle r = this.getTextRectangle();
                if (r == null) {
                    return -1;
                }
                Rectangle2D.Float shape = new Rectangle2D.Float(r.x, r.y, r.width, r.height);
                Position.Bias[] bias = new Position.Bias[1];
                return view.viewToModel(p.x, p.y, shape, bias);
            }
            return -1;
        }

        @Override
        public Rectangle getCharacterBounds(int i) {
            View view = (View)AbstractButton.this.getClientProperty("html");
            if (view != null) {
                Rectangle r = this.getTextRectangle();
                if (r == null) {
                    return null;
                }
                Rectangle2D.Float shape = new Rectangle2D.Float(r.x, r.y, r.width, r.height);
                try {
                    Shape charShape = view.modelToView(i, shape, Position.Bias.Forward);
                    return charShape.getBounds();
                }
                catch (BadLocationException e) {
                    return null;
                }
            }
            return null;
        }

        @Override
        public int getCharCount() {
            Document d;
            View view = (View)AbstractButton.this.getClientProperty("html");
            if (view != null && (d = view.getDocument()) instanceof StyledDocument) {
                StyledDocument doc = (StyledDocument)d;
                return doc.getLength();
            }
            return AbstractButton.this.accessibleContext.getAccessibleName().length();
        }

        @Override
        public int getCaretPosition() {
            return -1;
        }

        @Override
        public String getAtIndex(int part, int index) {
            if (index < 0 || index >= this.getCharCount()) {
                return null;
            }
            switch (part) {
                case 1: {
                    try {
                        return this.getText(index, 1);
                    }
                    catch (BadLocationException e) {
                        return null;
                    }
                }
                case 2: {
                    try {
                        String s = this.getText(0, this.getCharCount());
                        BreakIterator words = BreakIterator.getWordInstance(this.getLocale());
                        words.setText(s);
                        int end = words.following(index);
                        return s.substring(words.previous(), end);
                    }
                    catch (BadLocationException e) {
                        return null;
                    }
                }
                case 3: {
                    try {
                        String s = this.getText(0, this.getCharCount());
                        BreakIterator sentence = BreakIterator.getSentenceInstance(this.getLocale());
                        sentence.setText(s);
                        int end = sentence.following(index);
                        return s.substring(sentence.previous(), end);
                    }
                    catch (BadLocationException e) {
                        return null;
                    }
                }
            }
            return null;
        }

        @Override
        public String getAfterIndex(int part, int index) {
            if (index < 0 || index >= this.getCharCount()) {
                return null;
            }
            switch (part) {
                case 1: {
                    if (index + 1 >= this.getCharCount()) {
                        return null;
                    }
                    try {
                        return this.getText(index + 1, 1);
                    }
                    catch (BadLocationException e) {
                        return null;
                    }
                }
                case 2: {
                    try {
                        String s = this.getText(0, this.getCharCount());
                        BreakIterator words = BreakIterator.getWordInstance(this.getLocale());
                        words.setText(s);
                        int start = words.following(index);
                        if (start == -1 || start >= s.length()) {
                            return null;
                        }
                        int end = words.following(start);
                        if (end == -1 || end >= s.length()) {
                            return null;
                        }
                        return s.substring(start, end);
                    }
                    catch (BadLocationException e) {
                        return null;
                    }
                }
                case 3: {
                    try {
                        String s = this.getText(0, this.getCharCount());
                        BreakIterator sentence = BreakIterator.getSentenceInstance(this.getLocale());
                        sentence.setText(s);
                        int start = sentence.following(index);
                        if (start == -1 || start > s.length()) {
                            return null;
                        }
                        int end = sentence.following(start);
                        if (end == -1 || end > s.length()) {
                            return null;
                        }
                        return s.substring(start, end);
                    }
                    catch (BadLocationException e) {
                        return null;
                    }
                }
            }
            return null;
        }

        @Override
        public String getBeforeIndex(int part, int index) {
            if (index < 0 || index > this.getCharCount() - 1) {
                return null;
            }
            switch (part) {
                case 1: {
                    if (index == 0) {
                        return null;
                    }
                    try {
                        return this.getText(index - 1, 1);
                    }
                    catch (BadLocationException e) {
                        return null;
                    }
                }
                case 2: {
                    try {
                        String s = this.getText(0, this.getCharCount());
                        BreakIterator words = BreakIterator.getWordInstance(this.getLocale());
                        words.setText(s);
                        int end = words.following(index);
                        end = words.previous();
                        int start = words.previous();
                        if (start == -1) {
                            return null;
                        }
                        return s.substring(start, end);
                    }
                    catch (BadLocationException e) {
                        return null;
                    }
                }
                case 3: {
                    try {
                        String s = this.getText(0, this.getCharCount());
                        BreakIterator sentence = BreakIterator.getSentenceInstance(this.getLocale());
                        sentence.setText(s);
                        int end = sentence.following(index);
                        end = sentence.previous();
                        int start = sentence.previous();
                        if (start == -1) {
                            return null;
                        }
                        return s.substring(start, end);
                    }
                    catch (BadLocationException e) {
                        return null;
                    }
                }
            }
            return null;
        }

        @Override
        public AttributeSet getCharacterAttribute(int i) {
            StyledDocument doc;
            Element elem;
            Document d;
            View view = (View)AbstractButton.this.getClientProperty("html");
            if (view != null && (d = view.getDocument()) instanceof StyledDocument && (elem = (doc = (StyledDocument)d).getCharacterElement(i)) != null) {
                return elem.getAttributes();
            }
            return null;
        }

        @Override
        public int getSelectionStart() {
            return -1;
        }

        @Override
        public int getSelectionEnd() {
            return -1;
        }

        @Override
        public String getSelectedText() {
            return null;
        }

        private String getText(int offset, int length) throws BadLocationException {
            Document d;
            View view = (View)AbstractButton.this.getClientProperty("html");
            if (view != null && (d = view.getDocument()) instanceof StyledDocument) {
                StyledDocument doc = (StyledDocument)d;
                return doc.getText(offset, length);
            }
            return null;
        }

        private Rectangle getTextRectangle() {
            Icon icon;
            String text = AbstractButton.this.getText();
            Icon icon2 = icon = AbstractButton.this.isEnabled() ? AbstractButton.this.getIcon() : AbstractButton.this.getDisabledIcon();
            if (icon == null && text == null) {
                return null;
            }
            Rectangle paintIconR = new Rectangle();
            Rectangle paintTextR = new Rectangle();
            Rectangle paintViewR = new Rectangle();
            Insets paintViewInsets = new Insets(0, 0, 0, 0);
            paintViewInsets = AbstractButton.this.getInsets(paintViewInsets);
            paintViewR.x = paintViewInsets.left;
            paintViewR.y = paintViewInsets.top;
            paintViewR.width = AbstractButton.this.getWidth() - (paintViewInsets.left + paintViewInsets.right);
            paintViewR.height = AbstractButton.this.getHeight() - (paintViewInsets.top + paintViewInsets.bottom);
            String clippedText = SwingUtilities.layoutCompoundLabel(AbstractButton.this, this.getFontMetrics(this.getFont()), text, icon, AbstractButton.this.getVerticalAlignment(), AbstractButton.this.getHorizontalAlignment(), AbstractButton.this.getVerticalTextPosition(), AbstractButton.this.getHorizontalTextPosition(), paintViewR, paintIconR, paintTextR, 0);
            return paintTextR;
        }

        @Override
        AccessibleExtendedComponent getAccessibleExtendedComponent() {
            return this;
        }

        @Override
        public String getToolTipText() {
            return AbstractButton.this.getToolTipText();
        }

        @Override
        public String getTitledBorderText() {
            return super.getTitledBorderText();
        }

        @Override
        public AccessibleKeyBinding getAccessibleKeyBinding() {
            int mnemonic = AbstractButton.this.getMnemonic();
            if (mnemonic == 0) {
                return null;
            }
            return new ButtonKeyBinding(this, mnemonic);
        }

        class ButtonKeyBinding
        implements AccessibleKeyBinding {
            int mnemonic;

            ButtonKeyBinding(AccessibleAbstractButton this$1, int mnemonic) {
                this.mnemonic = mnemonic;
            }

            @Override
            public int getAccessibleKeyBindingCount() {
                return 1;
            }

            @Override
            public Object getAccessibleKeyBinding(int i) {
                if (i != 0) {
                    throw new IllegalArgumentException();
                }
                return KeyStroke.getKeyStroke(this.mnemonic, 0);
            }
        }
    }

    protected class ButtonChangeListener
    implements ChangeListener,
    Serializable {
        ButtonChangeListener() {
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            AbstractButton.this.getHandler().stateChanged(e);
        }
    }
}

