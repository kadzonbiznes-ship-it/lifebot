/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.CellRendererPane;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicLookAndFeel;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.basic.LazyActionMap;
import javax.swing.text.Position;
import sun.awt.AppContext;
import sun.swing.DefaultLookup;
import sun.swing.SwingUtilities2;
import sun.swing.UIAction;

public class BasicComboBoxUI
extends ComboBoxUI {
    protected JComboBox<Object> comboBox;
    protected boolean hasFocus = false;
    private boolean isTableCellEditor = false;
    private static final String IS_TABLE_CELL_EDITOR = "JComboBox.isTableCellEditor";
    protected JList<Object> listBox;
    protected CellRendererPane currentValuePane = new CellRendererPane();
    protected ComboPopup popup;
    protected Component editor;
    protected JButton arrowButton;
    protected KeyListener keyListener;
    protected FocusListener focusListener;
    protected PropertyChangeListener propertyChangeListener;
    protected ItemListener itemListener;
    protected MouseListener popupMouseListener;
    protected MouseMotionListener popupMouseMotionListener;
    protected KeyListener popupKeyListener;
    protected ListDataListener listDataListener;
    private Handler handler;
    private long timeFactor = 1000L;
    private long lastTime = 0L;
    private long time = 0L;
    JComboBox.KeySelectionManager keySelectionManager;
    protected boolean isMinimumSizeDirty = true;
    protected Dimension cachedMinimumSize = new Dimension(0, 0);
    private boolean isDisplaySizeDirty = true;
    private Dimension cachedDisplaySize = new Dimension(0, 0);
    private static final Object COMBO_UI_LIST_CELL_RENDERER_KEY = new StringBuffer("DefaultListCellRendererKey");
    static final StringBuffer HIDE_POPUP_KEY = new StringBuffer("HidePopupKey");
    private boolean sameBaseline;
    protected boolean squareButton = true;
    protected Insets padding;

    private static ListCellRenderer<Object> getDefaultListCellRenderer() {
        ListCellRenderer renderer = (ListCellRenderer)AppContext.getAppContext().get(COMBO_UI_LIST_CELL_RENDERER_KEY);
        if (renderer == null) {
            renderer = new DefaultListCellRenderer();
            AppContext.getAppContext().put(COMBO_UI_LIST_CELL_RENDERER_KEY, new DefaultListCellRenderer());
        }
        return renderer;
    }

    static void loadActionMap(LazyActionMap map) {
        map.put(new Actions("hidePopup"));
        map.put(new Actions("pageDownPassThrough"));
        map.put(new Actions("pageUpPassThrough"));
        map.put(new Actions("homePassThrough"));
        map.put(new Actions("endPassThrough"));
        map.put(new Actions("selectNext"));
        map.put(new Actions("selectNext2"));
        map.put(new Actions("togglePopup"));
        map.put(new Actions("spacePopup"));
        map.put(new Actions("selectPrevious"));
        map.put(new Actions("selectPrevious2"));
        map.put(new Actions("enterPressed"));
    }

    public static ComponentUI createUI(JComponent c) {
        return new BasicComboBoxUI();
    }

    @Override
    public void installUI(JComponent c) {
        JComboBox tmp;
        this.isMinimumSizeDirty = true;
        this.comboBox = tmp = (JComboBox)c;
        this.installDefaults();
        this.popup = this.createPopup();
        this.listBox = this.popup.getList();
        Boolean inTable = (Boolean)c.getClientProperty(IS_TABLE_CELL_EDITOR);
        if (inTable != null) {
            boolean bl = this.isTableCellEditor = inTable.equals(Boolean.TRUE);
        }
        if (this.comboBox.getRenderer() == null || this.comboBox.getRenderer() instanceof UIResource) {
            this.comboBox.setRenderer(this.createRenderer());
        }
        if (this.comboBox.getEditor() == null || this.comboBox.getEditor() instanceof UIResource) {
            this.comboBox.setEditor(this.createEditor());
        }
        this.installListeners();
        this.installComponents();
        this.comboBox.setLayout(this.createLayoutManager());
        this.comboBox.setRequestFocusEnabled(true);
        this.installKeyboardActions();
        this.comboBox.putClientProperty("doNotCancelPopup", HIDE_POPUP_KEY);
        if (this.keySelectionManager == null || this.keySelectionManager instanceof UIResource) {
            this.keySelectionManager = new DefaultKeySelectionManager();
        }
        this.comboBox.setKeySelectionManager(this.keySelectionManager);
    }

    @Override
    public void uninstallUI(JComponent c) {
        ComboBoxEditor comboBoxEditor;
        this.setPopupVisible(this.comboBox, false);
        this.popup.uninstallingUI();
        this.uninstallKeyboardActions();
        this.comboBox.setLayout(null);
        this.uninstallComponents();
        this.uninstallListeners();
        this.uninstallDefaults();
        if (this.comboBox.getRenderer() == null || this.comboBox.getRenderer() instanceof UIResource) {
            this.comboBox.setRenderer(null);
        }
        if ((comboBoxEditor = this.comboBox.getEditor()) instanceof UIResource) {
            if (comboBoxEditor.getEditorComponent().hasFocus()) {
                this.comboBox.requestFocusInWindow();
            }
            this.comboBox.setEditor(null);
        }
        if (this.keySelectionManager instanceof UIResource) {
            this.comboBox.setKeySelectionManager(null);
        }
        this.handler = null;
        this.keyListener = null;
        this.focusListener = null;
        this.listDataListener = null;
        this.propertyChangeListener = null;
        this.popup = null;
        this.listBox = null;
        this.comboBox = null;
    }

    protected void installDefaults() {
        LookAndFeel.installColorsAndFont(this.comboBox, "ComboBox.background", "ComboBox.foreground", "ComboBox.font");
        LookAndFeel.installBorder(this.comboBox, "ComboBox.border");
        LookAndFeel.installProperty(this.comboBox, "opaque", Boolean.TRUE);
        Long l = (Long)UIManager.get("ComboBox.timeFactor");
        this.timeFactor = l == null ? 1000L : l;
        Boolean b = (Boolean)UIManager.get("ComboBox.squareButton");
        this.squareButton = b == null ? true : b;
        this.padding = UIManager.getInsets("ComboBox.padding");
    }

    protected void installListeners() {
        this.itemListener = this.createItemListener();
        if (this.itemListener != null) {
            this.comboBox.addItemListener(this.itemListener);
        }
        if ((this.propertyChangeListener = this.createPropertyChangeListener()) != null) {
            this.comboBox.addPropertyChangeListener(this.propertyChangeListener);
        }
        if ((this.keyListener = this.createKeyListener()) != null) {
            this.comboBox.addKeyListener(this.keyListener);
        }
        if ((this.focusListener = this.createFocusListener()) != null) {
            this.comboBox.addFocusListener(this.focusListener);
        }
        if ((this.popupMouseListener = this.popup.getMouseListener()) != null) {
            this.comboBox.addMouseListener(this.popupMouseListener);
        }
        if ((this.popupMouseMotionListener = this.popup.getMouseMotionListener()) != null) {
            this.comboBox.addMouseMotionListener(this.popupMouseMotionListener);
        }
        if ((this.popupKeyListener = this.popup.getKeyListener()) != null) {
            this.comboBox.addKeyListener(this.popupKeyListener);
        }
        if (this.comboBox.getModel() != null && (this.listDataListener = this.createListDataListener()) != null) {
            this.comboBox.getModel().addListDataListener(this.listDataListener);
        }
    }

    protected void uninstallDefaults() {
        LookAndFeel.installColorsAndFont(this.comboBox, "ComboBox.background", "ComboBox.foreground", "ComboBox.font");
        LookAndFeel.uninstallBorder(this.comboBox);
    }

    protected void uninstallListeners() {
        if (this.keyListener != null) {
            this.comboBox.removeKeyListener(this.keyListener);
        }
        if (this.itemListener != null) {
            this.comboBox.removeItemListener(this.itemListener);
        }
        if (this.propertyChangeListener != null) {
            this.comboBox.removePropertyChangeListener(this.propertyChangeListener);
        }
        if (this.focusListener != null) {
            this.comboBox.removeFocusListener(this.focusListener);
        }
        if (this.popupMouseListener != null) {
            this.comboBox.removeMouseListener(this.popupMouseListener);
        }
        if (this.popupMouseMotionListener != null) {
            this.comboBox.removeMouseMotionListener(this.popupMouseMotionListener);
        }
        if (this.popupKeyListener != null) {
            this.comboBox.removeKeyListener(this.popupKeyListener);
        }
        if (this.comboBox.getModel() != null && this.listDataListener != null) {
            this.comboBox.getModel().removeListDataListener(this.listDataListener);
        }
    }

    protected ComboPopup createPopup() {
        return new BasicComboPopup(this.comboBox);
    }

    protected KeyListener createKeyListener() {
        return this.getHandler();
    }

    protected FocusListener createFocusListener() {
        return this.getHandler();
    }

    protected ListDataListener createListDataListener() {
        return this.getHandler();
    }

    protected ItemListener createItemListener() {
        return null;
    }

    protected PropertyChangeListener createPropertyChangeListener() {
        return this.getHandler();
    }

    protected LayoutManager createLayoutManager() {
        return this.getHandler();
    }

    protected ListCellRenderer<Object> createRenderer() {
        return new BasicComboBoxRenderer.UIResource();
    }

    protected ComboBoxEditor createEditor() {
        return new BasicComboBoxEditor.UIResource();
    }

    private Handler getHandler() {
        if (this.handler == null) {
            this.handler = new Handler();
        }
        return this.handler;
    }

    private void updateToolTipTextForChildren() {
        Component[] children = this.comboBox.getComponents();
        for (int i = 0; i < children.length; ++i) {
            if (!(children[i] instanceof JComponent)) continue;
            ((JComponent)children[i]).setToolTipText(this.comboBox.getToolTipText());
        }
    }

    protected void installComponents() {
        this.arrowButton = this.createArrowButton();
        if (this.arrowButton != null) {
            this.comboBox.add(this.arrowButton);
            this.configureArrowButton();
        }
        if (this.comboBox.isEditable()) {
            this.addEditor();
        }
        this.comboBox.add(this.currentValuePane);
    }

    protected void uninstallComponents() {
        if (this.arrowButton != null) {
            this.unconfigureArrowButton();
        }
        if (this.editor != null) {
            this.unconfigureEditor();
        }
        this.comboBox.removeAll();
        this.arrowButton = null;
    }

    public void addEditor() {
        this.removeEditor();
        this.editor = this.comboBox.getEditor().getEditorComponent();
        if (this.editor != null) {
            this.configureEditor();
            this.comboBox.add(this.editor);
            if (this.comboBox.isFocusOwner()) {
                this.editor.requestFocusInWindow();
            }
        }
    }

    public void removeEditor() {
        if (this.editor != null) {
            this.unconfigureEditor();
            this.comboBox.remove(this.editor);
            this.editor = null;
        }
    }

    protected void configureEditor() {
        this.editor.setEnabled(this.comboBox.isEnabled());
        this.editor.setFocusable(this.comboBox.isFocusable());
        this.editor.setFont(this.comboBox.getFont());
        if (this.focusListener != null) {
            this.editor.addFocusListener(this.focusListener);
        }
        this.editor.addFocusListener(this.getHandler());
        this.comboBox.getEditor().addActionListener(this.getHandler());
        if (this.editor instanceof JComponent) {
            ((JComponent)this.editor).putClientProperty("doNotCancelPopup", HIDE_POPUP_KEY);
            ((JComponent)this.editor).setInheritsPopupMenu(true);
        }
        this.comboBox.configureEditor(this.comboBox.getEditor(), this.comboBox.getSelectedItem());
        this.editor.addPropertyChangeListener(this.propertyChangeListener);
    }

    protected void unconfigureEditor() {
        if (this.focusListener != null) {
            this.editor.removeFocusListener(this.focusListener);
        }
        this.editor.removePropertyChangeListener(this.propertyChangeListener);
        this.editor.removeFocusListener(this.getHandler());
        this.comboBox.getEditor().removeActionListener(this.getHandler());
    }

    public void configureArrowButton() {
        if (this.arrowButton != null) {
            this.arrowButton.setEnabled(this.comboBox.isEnabled());
            this.arrowButton.setFocusable(this.comboBox.isFocusable());
            this.arrowButton.setRequestFocusEnabled(false);
            this.arrowButton.addMouseListener(this.popup.getMouseListener());
            this.arrowButton.addMouseMotionListener(this.popup.getMouseMotionListener());
            this.arrowButton.resetKeyboardActions();
            this.arrowButton.putClientProperty("doNotCancelPopup", HIDE_POPUP_KEY);
            this.arrowButton.setInheritsPopupMenu(true);
        }
    }

    public void unconfigureArrowButton() {
        if (this.arrowButton != null) {
            this.arrowButton.removeMouseListener(this.popup.getMouseListener());
            this.arrowButton.removeMouseMotionListener(this.popup.getMouseMotionListener());
        }
    }

    protected JButton createArrowButton() {
        BasicArrowButton button = new BasicArrowButton(5, UIManager.getColor("ComboBox.buttonBackground"), UIManager.getColor("ComboBox.buttonShadow"), UIManager.getColor("ComboBox.buttonDarkShadow"), UIManager.getColor("ComboBox.buttonHighlight"));
        button.setName("ComboBox.arrowButton");
        return button;
    }

    @Override
    public boolean isPopupVisible(JComboBox<?> c) {
        return this.popup != null && this.popup.isVisible();
    }

    @Override
    public void setPopupVisible(JComboBox<?> c, boolean v) {
        if (this.popup != null) {
            if (v) {
                this.popup.show();
            } else {
                this.popup.hide();
            }
        }
    }

    @Override
    public boolean isFocusTraversable(JComboBox<?> c) {
        return !this.comboBox.isEditable();
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        this.hasFocus = this.comboBox.hasFocus();
        if (!this.comboBox.isEditable()) {
            Rectangle r = this.rectangleForCurrentValue();
            this.paintCurrentValueBackground(g, r, this.hasFocus);
            this.paintCurrentValue(g, r, this.hasFocus);
        }
        this.currentValuePane.removeAll();
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        return this.getMinimumSize(c);
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        if (!this.isMinimumSizeDirty) {
            return new Dimension(this.cachedMinimumSize);
        }
        Dimension size = this.getDisplaySize();
        Insets insets = this.getInsets();
        int buttonHeight = size.height;
        int buttonWidth = this.squareButton ? buttonHeight : this.arrowButton.getPreferredSize().width;
        size.height += insets.top + insets.bottom;
        size.width += insets.left + insets.right + buttonWidth;
        this.cachedMinimumSize.setSize(size.width, size.height);
        this.isMinimumSizeDirty = false;
        return new Dimension(size);
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
    }

    @Override
    public int getBaseline(JComponent c, int width, int height) {
        super.getBaseline(c, width, height);
        int baseline = -1;
        this.getDisplaySize();
        if (this.sameBaseline) {
            Insets insets = c.getInsets();
            height = Math.max(height - insets.top - insets.bottom, 0);
            if (!this.comboBox.isEditable()) {
                JLabel label;
                String text;
                DefaultListCellRenderer renderer = this.comboBox.getRenderer();
                if (renderer == null) {
                    renderer = new DefaultListCellRenderer();
                }
                Object value = null;
                Object prototypeValue = this.comboBox.getPrototypeDisplayValue();
                if (prototypeValue != null) {
                    value = prototypeValue;
                } else if (this.comboBox.getModel().getSize() > 0) {
                    value = this.comboBox.getModel().getElementAt(0);
                }
                Component component = renderer.getListCellRendererComponent(this.listBox, value, -1, false, false);
                if (component instanceof JLabel && ((text = (label = (JLabel)component).getText()) == null || text.isEmpty())) {
                    label.setText(" ");
                }
                if (component instanceof JComponent) {
                    component.setFont(this.comboBox.getFont());
                }
                baseline = component.getBaseline(width, height);
            } else {
                baseline = this.editor.getBaseline(width, height);
            }
            if (baseline > 0) {
                baseline += insets.top;
            }
        }
        return baseline;
    }

    @Override
    public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
        super.getBaselineResizeBehavior(c);
        this.getDisplaySize();
        if (this.comboBox.isEditable()) {
            return this.editor.getBaselineResizeBehavior();
        }
        if (this.sameBaseline) {
            DefaultListCellRenderer renderer = this.comboBox.getRenderer();
            if (renderer == null) {
                renderer = new DefaultListCellRenderer();
            }
            Object value = null;
            Object prototypeValue = this.comboBox.getPrototypeDisplayValue();
            if (prototypeValue != null) {
                value = prototypeValue;
            } else if (this.comboBox.getModel().getSize() > 0) {
                value = this.comboBox.getModel().getElementAt(0);
            }
            if (value != null) {
                Component component = renderer.getListCellRendererComponent(this.listBox, value, -1, false, false);
                return component.getBaselineResizeBehavior();
            }
        }
        return Component.BaselineResizeBehavior.OTHER;
    }

    @Override
    public int getAccessibleChildrenCount(JComponent c) {
        if (this.comboBox.isEditable()) {
            return 2;
        }
        return 1;
    }

    @Override
    public Accessible getAccessibleChild(JComponent c, int i) {
        switch (i) {
            case 0: {
                if (!(this.popup instanceof Accessible)) break;
                AccessibleContext ac = ((Accessible)((Object)this.popup)).getAccessibleContext();
                ac.setAccessibleParent(this.comboBox);
                return (Accessible)((Object)this.popup);
            }
            case 1: {
                if (!this.comboBox.isEditable() || !(this.editor instanceof Accessible)) break;
                AccessibleContext ac = ((Accessible)((Object)this.editor)).getAccessibleContext();
                ac.setAccessibleParent(this.comboBox);
                return (Accessible)((Object)this.editor);
            }
        }
        return null;
    }

    protected boolean isNavigationKey(int keyCode) {
        return keyCode == 38 || keyCode == 40 || keyCode == 224 || keyCode == 225;
    }

    private boolean isNavigationKey(int keyCode, int modifiers) {
        InputMap inputMap = this.comboBox.getInputMap(1);
        KeyStroke key = KeyStroke.getKeyStroke(keyCode, modifiers);
        return inputMap != null && inputMap.get(key) != null;
    }

    protected void selectNextPossibleValue() {
        int si = this.comboBox.isPopupVisible() ? this.listBox.getSelectedIndex() : this.comboBox.getSelectedIndex();
        if (si < this.comboBox.getModel().getSize() - 1) {
            this.listBox.setSelectedIndex(si + 1);
            this.listBox.ensureIndexIsVisible(si + 1);
            if (!(this.isTableCellEditor || UIManager.getBoolean("ComboBox.noActionOnKeyNavigation") && this.comboBox.isPopupVisible())) {
                this.comboBox.setSelectedIndex(si + 1);
            }
            this.comboBox.repaint();
        }
    }

    protected void selectPreviousPossibleValue() {
        int si = this.comboBox.isPopupVisible() ? this.listBox.getSelectedIndex() : this.comboBox.getSelectedIndex();
        if (si > 0) {
            this.listBox.setSelectedIndex(si - 1);
            this.listBox.ensureIndexIsVisible(si - 1);
            if (!(this.isTableCellEditor || UIManager.getBoolean("ComboBox.noActionOnKeyNavigation") && this.comboBox.isPopupVisible())) {
                this.comboBox.setSelectedIndex(si - 1);
            }
            this.comboBox.repaint();
        }
    }

    protected void toggleOpenClose() {
        this.setPopupVisible(this.comboBox, !this.isPopupVisible(this.comboBox));
    }

    protected Rectangle rectangleForCurrentValue() {
        int width = this.comboBox.getWidth();
        int height = this.comboBox.getHeight();
        Insets insets = this.getInsets();
        int buttonSize = height - (insets.top + insets.bottom);
        if (this.arrowButton != null) {
            buttonSize = this.arrowButton.getWidth();
        }
        if (BasicGraphicsUtils.isLeftToRight(this.comboBox)) {
            return new Rectangle(insets.left, insets.top, width - (insets.left + insets.right + buttonSize), height - (insets.top + insets.bottom));
        }
        return new Rectangle(insets.left + buttonSize, insets.top, width - (insets.left + insets.right + buttonSize), height - (insets.top + insets.bottom));
    }

    protected Insets getInsets() {
        return this.comboBox.getInsets();
    }

    public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
        Component c;
        ListCellRenderer<Object> renderer = this.comboBox.getRenderer();
        if (hasFocus && !this.isPopupVisible(this.comboBox)) {
            c = renderer.getListCellRendererComponent(this.listBox, this.comboBox.getSelectedItem(), -1, true, false);
        } else {
            c = renderer.getListCellRendererComponent(this.listBox, this.comboBox.getSelectedItem(), -1, false, false);
            c.setBackground(UIManager.getColor("ComboBox.background"));
        }
        c.setFont(this.comboBox.getFont());
        if (hasFocus && !this.isPopupVisible(this.comboBox)) {
            c.setForeground(this.listBox.getSelectionForeground());
            c.setBackground(this.listBox.getSelectionBackground());
        } else if (this.comboBox.isEnabled()) {
            c.setForeground(this.comboBox.getForeground());
            c.setBackground(this.comboBox.getBackground());
        } else {
            c.setForeground(DefaultLookup.getColor(this.comboBox, this, "ComboBox.disabledForeground", null));
            c.setBackground(DefaultLookup.getColor(this.comboBox, this, "ComboBox.disabledBackground", null));
        }
        boolean shouldValidate = false;
        if (c instanceof JPanel) {
            shouldValidate = true;
        }
        int x = bounds.x;
        int y = bounds.y;
        int w = bounds.width;
        int h = bounds.height;
        if (this.padding != null) {
            x = bounds.x + this.padding.left;
            y = bounds.y + this.padding.top;
            w = bounds.width - (this.padding.left + this.padding.right);
            h = bounds.height - (this.padding.top + this.padding.bottom);
        }
        this.currentValuePane.paintComponent(g, c, this.comboBox, x, y, w, h, shouldValidate);
    }

    public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
        Color t = g.getColor();
        if (this.comboBox.isEnabled()) {
            g.setColor(DefaultLookup.getColor(this.comboBox, this, "ComboBox.background", null));
        } else {
            g.setColor(DefaultLookup.getColor(this.comboBox, this, "ComboBox.disabledBackground", null));
        }
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g.setColor(t);
    }

    void repaintCurrentValue() {
        Rectangle r = this.rectangleForCurrentValue();
        this.comboBox.repaint(r.x, r.y, r.width, r.height);
    }

    protected Dimension getDefaultSize() {
        Dimension d = this.getSizeForComponent(BasicComboBoxUI.getDefaultListCellRenderer().getListCellRendererComponent(this.listBox, " ", -1, false, false));
        return new Dimension(d.width, d.height);
    }

    protected Dimension getDisplaySize() {
        if (!this.isDisplaySizeDirty) {
            return new Dimension(this.cachedDisplaySize);
        }
        Dimension result = new Dimension();
        DefaultListCellRenderer renderer = this.comboBox.getRenderer();
        if (renderer == null) {
            renderer = new DefaultListCellRenderer();
        }
        this.sameBaseline = true;
        Object prototypeValue = this.comboBox.getPrototypeDisplayValue();
        if (prototypeValue != null) {
            result = this.getSizeForComponent(renderer.getListCellRendererComponent(this.listBox, prototypeValue, -1, false, false));
        } else {
            ComboBoxModel<Object> model = this.comboBox.getModel();
            int modelSize = model.getSize();
            int baseline = -1;
            if (modelSize > 0) {
                for (int i = 0; i < modelSize; ++i) {
                    Object value = model.getElementAt(i);
                    Component c = renderer.getListCellRendererComponent(this.listBox, value, -1, false, false);
                    Dimension d = this.getSizeForComponent(c);
                    if (!(!this.sameBaseline || value == null || value instanceof String && "".equals(value))) {
                        int newBaseline = c.getBaseline(d.width, d.height);
                        if (newBaseline == -1) {
                            this.sameBaseline = false;
                        } else if (baseline == -1) {
                            baseline = newBaseline;
                        } else if (baseline != newBaseline) {
                            this.sameBaseline = false;
                        }
                    }
                    result.width = Math.max(result.width, d.width);
                    result.height = Math.max(result.height, d.height);
                }
            } else {
                result = this.getDefaultSize();
                if (this.comboBox.isEditable()) {
                    result.width = 100;
                }
            }
        }
        if (this.comboBox.isEditable()) {
            Dimension d = this.editor.getPreferredSize();
            result.width = Math.max(result.width, d.width);
            result.height = Math.max(result.height, d.height);
        }
        if (this.padding != null) {
            result.width += this.padding.left + this.padding.right;
            result.height += this.padding.top + this.padding.bottom;
        }
        this.cachedDisplaySize.setSize(result.width, result.height);
        this.isDisplaySizeDirty = false;
        return result;
    }

    protected Dimension getSizeForComponent(Component comp) {
        this.currentValuePane.add(comp);
        comp.setFont(this.comboBox.getFont());
        Dimension d = comp.getPreferredSize();
        this.currentValuePane.remove(comp);
        return d;
    }

    protected void installKeyboardActions() {
        InputMap km = this.getInputMap(1);
        SwingUtilities.replaceUIInputMap(this.comboBox, 1, km);
        LazyActionMap.installLazyActionMap(this.comboBox, BasicComboBoxUI.class, "ComboBox.actionMap");
    }

    InputMap getInputMap(int condition) {
        if (condition == 1) {
            return (InputMap)DefaultLookup.get(this.comboBox, this, "ComboBox.ancestorInputMap");
        }
        return null;
    }

    boolean isTableCellEditor() {
        return this.isTableCellEditor;
    }

    protected void uninstallKeyboardActions() {
        SwingUtilities.replaceUIInputMap(this.comboBox, 1, null);
        SwingUtilities.replaceUIActionMap(this.comboBox, null);
    }

    private static class Actions
    extends UIAction {
        private static final String HIDE = "hidePopup";
        private static final String DOWN = "selectNext";
        private static final String DOWN_2 = "selectNext2";
        private static final String TOGGLE = "togglePopup";
        private static final String TOGGLE_2 = "spacePopup";
        private static final String UP = "selectPrevious";
        private static final String UP_2 = "selectPrevious2";
        private static final String ENTER = "enterPressed";
        private static final String PAGE_DOWN = "pageDownPassThrough";
        private static final String PAGE_UP = "pageUpPassThrough";
        private static final String HOME = "homePassThrough";
        private static final String END = "endPassThrough";

        Actions(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String key = this.getName();
            JComboBox comboBox = (JComboBox)e.getSource();
            BasicComboBoxUI ui = (BasicComboBoxUI)BasicLookAndFeel.getUIOfType(comboBox.getUI(), BasicComboBoxUI.class);
            if (key == HIDE) {
                comboBox.firePopupMenuCanceled();
                comboBox.setPopupVisible(false);
            } else if (key == PAGE_DOWN || key == PAGE_UP || key == HOME || key == END) {
                int index = this.getNextIndex(comboBox, key);
                if (index >= 0 && index < comboBox.getItemCount()) {
                    if (UIManager.getBoolean("ComboBox.noActionOnKeyNavigation") && comboBox.isPopupVisible()) {
                        ui.listBox.setSelectedIndex(index);
                        ui.listBox.ensureIndexIsVisible(index);
                        comboBox.repaint();
                    } else {
                        comboBox.setSelectedIndex(index);
                    }
                }
            } else if (key == DOWN) {
                if (comboBox.isShowing()) {
                    if (comboBox.isPopupVisible()) {
                        if (ui != null) {
                            ui.selectNextPossibleValue();
                        }
                    } else {
                        comboBox.setPopupVisible(true);
                    }
                }
            } else if (key == DOWN_2) {
                if (comboBox.isShowing()) {
                    if ((comboBox.isEditable() || ui != null && ui.isTableCellEditor()) && !comboBox.isPopupVisible()) {
                        comboBox.setPopupVisible(true);
                    } else if (ui != null) {
                        ui.selectNextPossibleValue();
                    }
                }
            } else if (key == TOGGLE || key == TOGGLE_2) {
                if (!(ui == null || key != TOGGLE && comboBox.isEditable())) {
                    if (ui.isTableCellEditor()) {
                        comboBox.setSelectedIndex(ui.popup.getList().getSelectedIndex());
                    } else {
                        comboBox.setPopupVisible(!comboBox.isPopupVisible());
                    }
                }
            } else if (key == UP) {
                if (ui != null) {
                    if (ui.isPopupVisible(comboBox)) {
                        ui.selectPreviousPossibleValue();
                    } else if (DefaultLookup.getBoolean(comboBox, ui, "ComboBox.showPopupOnNavigation", false)) {
                        ui.setPopupVisible(comboBox, true);
                    }
                }
            } else if (key == UP_2) {
                if (comboBox.isShowing() && ui != null) {
                    if (comboBox.isEditable() && !comboBox.isPopupVisible()) {
                        comboBox.setPopupVisible(true);
                    } else {
                        ui.selectPreviousPossibleValue();
                    }
                }
            } else if (key == ENTER) {
                if (comboBox.isPopupVisible()) {
                    if (UIManager.getBoolean("ComboBox.noActionOnKeyNavigation")) {
                        Object listItem = ui.popup.getList().getSelectedValue();
                        if (listItem != null) {
                            comboBox.getEditor().setItem(listItem);
                            comboBox.setSelectedItem(listItem);
                        }
                        comboBox.setPopupVisible(false);
                    } else {
                        Object listItem;
                        boolean isEnterSelectablePopup = UIManager.getBoolean("ComboBox.isEnterSelectablePopup");
                        if ((!comboBox.isEditable() || isEnterSelectablePopup || ui.isTableCellEditor) && (listItem = ui.popup.getList().getSelectedValue()) != null) {
                            comboBox.getEditor().setItem(listItem);
                            comboBox.setSelectedItem(listItem);
                        }
                        comboBox.setPopupVisible(false);
                    }
                } else {
                    JRootPane root;
                    if (ui.isTableCellEditor && !comboBox.isEditable()) {
                        comboBox.setSelectedItem(comboBox.getSelectedItem());
                    }
                    if ((root = SwingUtilities.getRootPane(comboBox)) != null) {
                        Action action;
                        Object obj;
                        InputMap im = root.getInputMap(2);
                        ActionMap am = root.getActionMap();
                        if (im != null && am != null && (obj = im.get(KeyStroke.getKeyStroke(10, 0))) != null && (action = am.get(obj)) != null) {
                            action.actionPerformed(new ActionEvent(root, e.getID(), e.getActionCommand(), e.getWhen(), e.getModifiers()));
                        }
                    }
                }
            }
        }

        private int getNextIndex(JComboBox<?> comboBox, String key) {
            int listHeight = comboBox.getMaximumRowCount();
            int selectedIndex = comboBox.getSelectedIndex();
            if (UIManager.getBoolean("ComboBox.noActionOnKeyNavigation") && comboBox.getUI() instanceof BasicComboBoxUI) {
                selectedIndex = ((BasicComboBoxUI)comboBox.getUI()).listBox.getSelectedIndex();
            }
            if (key == PAGE_UP) {
                int index = selectedIndex - listHeight;
                return index < 0 ? 0 : index;
            }
            if (key == PAGE_DOWN) {
                int index = selectedIndex + listHeight;
                int max = comboBox.getItemCount();
                return index < max ? index : max - 1;
            }
            if (key == HOME) {
                return 0;
            }
            if (key == END) {
                return comboBox.getItemCount() - 1;
            }
            return comboBox.getSelectedIndex();
        }

        @Override
        public boolean accept(Object c) {
            JRootPane root;
            if (this.getName() == HIDE) {
                return c != null && ((JComboBox)c).isPopupVisible();
            }
            if (this.getName() == ENTER && (root = SwingUtilities.getRootPane((JComboBox)c)) != null && c != null && !((JComboBox)c).isPopupVisible()) {
                Object obj;
                InputMap im = root.getInputMap(2);
                ActionMap am = root.getActionMap();
                if (im != null && am != null && (obj = im.get(KeyStroke.getKeyStroke(10, 0))) == null) {
                    return false;
                }
            }
            return true;
        }
    }

    class DefaultKeySelectionManager
    implements JComboBox.KeySelectionManager,
    UIResource {
        private String prefix = "";
        private String typedString = "";

        DefaultKeySelectionManager() {
        }

        @Override
        public int selectionForKey(char aKey, ComboBoxModel<?> aModel) {
            int index;
            if (BasicComboBoxUI.this.lastTime == 0L) {
                this.prefix = "";
                this.typedString = "";
            }
            boolean startingFromSelection = true;
            int startIndex = BasicComboBoxUI.this.comboBox.getSelectedIndex();
            if (BasicComboBoxUI.this.time - BasicComboBoxUI.this.lastTime < BasicComboBoxUI.this.timeFactor) {
                this.typedString = this.typedString + aKey;
                if (this.prefix.length() == 1 && aKey == this.prefix.charAt(0)) {
                    ++startIndex;
                } else {
                    this.prefix = this.typedString;
                }
            } else {
                ++startIndex;
                this.typedString = "" + aKey;
                this.prefix = this.typedString;
            }
            BasicComboBoxUI.this.lastTime = BasicComboBoxUI.this.time;
            if (startIndex < 0 || startIndex >= aModel.getSize()) {
                startingFromSelection = false;
                startIndex = 0;
            }
            if ((index = BasicComboBoxUI.this.listBox.getNextMatch(this.prefix, startIndex, Position.Bias.Forward)) < 0 && startingFromSelection) {
                index = BasicComboBoxUI.this.listBox.getNextMatch(this.prefix, 0, Position.Bias.Forward);
            }
            return index;
        }
    }

    private class Handler
    implements ActionListener,
    FocusListener,
    KeyListener,
    LayoutManager,
    ListDataListener,
    PropertyChangeListener {
        private Handler() {
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if (e.getSource() == BasicComboBoxUI.this.editor) {
                if ("border".equals(propertyName)) {
                    BasicComboBoxUI.this.isMinimumSizeDirty = true;
                    BasicComboBoxUI.this.isDisplaySizeDirty = true;
                    BasicComboBoxUI.this.comboBox.revalidate();
                }
            } else {
                JComboBox comboBox = (JComboBox)e.getSource();
                if (propertyName == "model") {
                    ComboBoxModel newModel = (ComboBoxModel)e.getNewValue();
                    ComboBoxModel oldModel = (ComboBoxModel)e.getOldValue();
                    if (oldModel != null && BasicComboBoxUI.this.listDataListener != null) {
                        oldModel.removeListDataListener(BasicComboBoxUI.this.listDataListener);
                    }
                    if (newModel != null && BasicComboBoxUI.this.listDataListener != null) {
                        newModel.addListDataListener(BasicComboBoxUI.this.listDataListener);
                    }
                    if (BasicComboBoxUI.this.editor != null) {
                        comboBox.configureEditor(comboBox.getEditor(), comboBox.getSelectedItem());
                    }
                    BasicComboBoxUI.this.isMinimumSizeDirty = true;
                    BasicComboBoxUI.this.isDisplaySizeDirty = true;
                    comboBox.revalidate();
                    comboBox.repaint();
                } else if (propertyName == "editor" && comboBox.isEditable()) {
                    BasicComboBoxUI.this.addEditor();
                    comboBox.revalidate();
                } else if (propertyName == "editable") {
                    if (comboBox.isEditable()) {
                        comboBox.setRequestFocusEnabled(false);
                        BasicComboBoxUI.this.addEditor();
                    } else {
                        comboBox.setRequestFocusEnabled(true);
                        BasicComboBoxUI.this.removeEditor();
                    }
                    BasicComboBoxUI.this.updateToolTipTextForChildren();
                    comboBox.revalidate();
                } else if (propertyName == "enabled") {
                    boolean enabled = comboBox.isEnabled();
                    if (BasicComboBoxUI.this.editor != null) {
                        BasicComboBoxUI.this.editor.setEnabled(enabled);
                    }
                    if (BasicComboBoxUI.this.arrowButton != null) {
                        BasicComboBoxUI.this.arrowButton.setEnabled(enabled);
                    }
                    comboBox.repaint();
                } else if (propertyName == "focusable") {
                    boolean focusable = comboBox.isFocusable();
                    if (BasicComboBoxUI.this.editor != null) {
                        BasicComboBoxUI.this.editor.setFocusable(focusable);
                    }
                    if (BasicComboBoxUI.this.arrowButton != null) {
                        BasicComboBoxUI.this.arrowButton.setFocusable(focusable);
                    }
                    comboBox.repaint();
                } else if (propertyName == "maximumRowCount") {
                    if (BasicComboBoxUI.this.isPopupVisible(comboBox)) {
                        BasicComboBoxUI.this.setPopupVisible(comboBox, false);
                        BasicComboBoxUI.this.setPopupVisible(comboBox, true);
                    }
                } else if (propertyName == "font") {
                    BasicComboBoxUI.this.listBox.setFont(comboBox.getFont());
                    if (BasicComboBoxUI.this.editor != null) {
                        BasicComboBoxUI.this.editor.setFont(comboBox.getFont());
                    }
                    BasicComboBoxUI.this.isMinimumSizeDirty = true;
                    BasicComboBoxUI.this.isDisplaySizeDirty = true;
                    comboBox.validate();
                } else if (SwingUtilities2.isScaleChanged(e)) {
                    BasicComboBoxUI.this.isMinimumSizeDirty = true;
                    BasicComboBoxUI.this.isDisplaySizeDirty = true;
                    comboBox.validate();
                } else if (propertyName == "ToolTipText") {
                    BasicComboBoxUI.this.updateToolTipTextForChildren();
                } else if (propertyName == BasicComboBoxUI.IS_TABLE_CELL_EDITOR) {
                    Boolean inTable = (Boolean)e.getNewValue();
                    BasicComboBoxUI.this.isTableCellEditor = inTable.equals(Boolean.TRUE);
                } else if (propertyName == "prototypeDisplayValue") {
                    BasicComboBoxUI.this.isMinimumSizeDirty = true;
                    BasicComboBoxUI.this.isDisplaySizeDirty = true;
                    comboBox.revalidate();
                } else if (propertyName == "renderer") {
                    BasicComboBoxUI.this.isMinimumSizeDirty = true;
                    BasicComboBoxUI.this.isDisplaySizeDirty = true;
                    comboBox.revalidate();
                }
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (BasicComboBoxUI.this.isNavigationKey(e.getKeyCode(), e.getModifiers())) {
                BasicComboBoxUI.this.lastTime = 0L;
            } else if (BasicComboBoxUI.this.comboBox.isEnabled() && BasicComboBoxUI.this.comboBox.getModel().getSize() != 0 && this.isTypeAheadKey(e) && e.getKeyChar() != '\uffff') {
                BasicComboBoxUI.this.time = e.getWhen();
                if (BasicComboBoxUI.this.comboBox.selectWithKeyChar(e.getKeyChar())) {
                    e.consume();
                }
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        private boolean isTypeAheadKey(KeyEvent e) {
            return !e.isAltDown() && !BasicGraphicsUtils.isMenuShortcutKeyDown(e);
        }

        @Override
        public void focusGained(FocusEvent e) {
            ComboBoxEditor comboBoxEditor = BasicComboBoxUI.this.comboBox.getEditor();
            if (comboBoxEditor != null && e.getSource() == comboBoxEditor.getEditorComponent()) {
                return;
            }
            BasicComboBoxUI.this.hasFocus = true;
            BasicComboBoxUI.this.comboBox.repaint();
            if (BasicComboBoxUI.this.comboBox.isEditable() && BasicComboBoxUI.this.editor != null) {
                BasicComboBoxUI.this.editor.requestFocus();
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            ComboBoxEditor editor = BasicComboBoxUI.this.comboBox.getEditor();
            if (editor != null && e.getSource() == editor.getEditorComponent()) {
                Object item = editor.getItem();
                Object selectedItem = BasicComboBoxUI.this.comboBox.getSelectedItem();
                if (!e.isTemporary() && item != null && !item.equals(selectedItem == null ? "" : selectedItem)) {
                    BasicComboBoxUI.this.comboBox.actionPerformed(new ActionEvent(editor, 0, "", EventQueue.getMostRecentEventTime(), 0));
                }
            }
            BasicComboBoxUI.this.hasFocus = false;
            if (!e.isTemporary()) {
                BasicComboBoxUI.this.setPopupVisible(BasicComboBoxUI.this.comboBox, false);
            }
            BasicComboBoxUI.this.comboBox.repaint();
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            if (e.getIndex0() != -1 || e.getIndex1() != -1) {
                BasicComboBoxUI.this.isMinimumSizeDirty = true;
                BasicComboBoxUI.this.comboBox.revalidate();
            }
            if (BasicComboBoxUI.this.comboBox.isEditable() && BasicComboBoxUI.this.editor != null) {
                BasicComboBoxUI.this.comboBox.configureEditor(BasicComboBoxUI.this.comboBox.getEditor(), BasicComboBoxUI.this.comboBox.getSelectedItem());
            }
            BasicComboBoxUI.this.isDisplaySizeDirty = true;
            BasicComboBoxUI.this.comboBox.repaint();
        }

        @Override
        public void intervalAdded(ListDataEvent e) {
            this.contentsChanged(e);
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            this.contentsChanged(e);
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return parent.getPreferredSize();
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return parent.getMinimumSize();
        }

        @Override
        public void layoutContainer(Container parent) {
            int buttonHeight;
            JComboBox cb = (JComboBox)parent;
            int width = cb.getWidth();
            int height = cb.getHeight();
            Insets insets = BasicComboBoxUI.this.getInsets();
            int buttonWidth = buttonHeight = height - (insets.top + insets.bottom);
            if (BasicComboBoxUI.this.arrowButton != null) {
                Insets arrowInsets = BasicComboBoxUI.this.arrowButton.getInsets();
                int n = buttonWidth = BasicComboBoxUI.this.squareButton ? buttonHeight : BasicComboBoxUI.this.arrowButton.getPreferredSize().width + arrowInsets.left + arrowInsets.right;
            }
            if (BasicComboBoxUI.this.arrowButton != null) {
                if (BasicGraphicsUtils.isLeftToRight(cb)) {
                    BasicComboBoxUI.this.arrowButton.setBounds(width - (insets.right + buttonWidth), insets.top, buttonWidth, buttonHeight);
                } else {
                    BasicComboBoxUI.this.arrowButton.setBounds(insets.left, insets.top, buttonWidth, buttonHeight);
                }
            }
            if (BasicComboBoxUI.this.editor != null) {
                Rectangle cvb = BasicComboBoxUI.this.rectangleForCurrentValue();
                BasicComboBoxUI.this.editor.setBounds(cvb);
            }
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            Object item = BasicComboBoxUI.this.comboBox.getEditor().getItem();
            if (item != null) {
                Action action;
                ActionMap am;
                if (!BasicComboBoxUI.this.comboBox.isPopupVisible() && !item.equals(BasicComboBoxUI.this.comboBox.getSelectedItem())) {
                    BasicComboBoxUI.this.comboBox.setSelectedItem(BasicComboBoxUI.this.comboBox.getEditor().getItem());
                }
                if ((am = BasicComboBoxUI.this.comboBox.getActionMap()) != null && (action = am.get("enterPressed")) != null) {
                    action.actionPerformed(new ActionEvent(BasicComboBoxUI.this.comboBox, evt.getID(), evt.getActionCommand(), evt.getModifiers()));
                }
            }
        }
    }

    public class ComboBoxLayoutManager
    implements LayoutManager {
        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return BasicComboBoxUI.this.getHandler().preferredLayoutSize(parent);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return BasicComboBoxUI.this.getHandler().minimumLayoutSize(parent);
        }

        @Override
        public void layoutContainer(Container parent) {
            BasicComboBoxUI.this.getHandler().layoutContainer(parent);
        }
    }

    public class PropertyChangeHandler
    implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            BasicComboBoxUI.this.getHandler().propertyChange(e);
        }
    }

    public class ItemHandler
    implements ItemListener {
        public ItemHandler(BasicComboBoxUI this$0) {
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
        }
    }

    public class ListDataHandler
    implements ListDataListener {
        @Override
        public void contentsChanged(ListDataEvent e) {
            BasicComboBoxUI.this.getHandler().contentsChanged(e);
        }

        @Override
        public void intervalAdded(ListDataEvent e) {
            BasicComboBoxUI.this.getHandler().intervalAdded(e);
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            BasicComboBoxUI.this.getHandler().intervalRemoved(e);
        }
    }

    public class FocusHandler
    implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) {
            BasicComboBoxUI.this.getHandler().focusGained(e);
        }

        @Override
        public void focusLost(FocusEvent e) {
            BasicComboBoxUI.this.getHandler().focusLost(e);
        }
    }

    public class KeyHandler
    extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            BasicComboBoxUI.this.getHandler().keyPressed(e);
        }
    }
}

